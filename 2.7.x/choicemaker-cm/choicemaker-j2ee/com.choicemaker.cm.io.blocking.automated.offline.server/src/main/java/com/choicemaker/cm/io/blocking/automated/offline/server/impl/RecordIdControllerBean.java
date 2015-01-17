package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.ImmutableRecordIdTranslator;
import com.choicemaker.cm.io.blocking.automated.offline.core.MutableRecordIdTranslator;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_SOURCE_ROLE;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdController;

@Stateless
public class RecordIdControllerBean implements RecordIdController {

	private static final Logger logger = Logger
			.getLogger(RecordIdControllerBean.class.getName());

	public static final String BASENAME_RECORDID_TRANSLATOR = "translator";

	public static final String BASENAME_RECORDID_STORE = "recordID";

	/**
	 * The index in the {@link #createRecordIdTypeQuery(OabaJob)
	 * RecordIdTypeQuery} of the RECORD_ID_TYPE field
	 */
	protected static final int QUERY_INDEX_RECORD_ID_TYPE = 1;

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@EJB
	private OabaJobControllerBean jobController;

	@Override
	public ImmutableRecordIdTranslator<?> getImmutableRecordIdTranslator(
			BatchJob job) throws BlockingException {
		// FIXME look up, don't create
		ImmutableRecordIdTranslatorImpl retVal =
			new ImmutableRecordIdTranslatorImpl(job);
		return retVal;
	}

	@Override
	public MutableRecordIdTranslator<?> createMutableRecordIdTranslator(
			BatchJob job) throws BlockingException {
		RecordIdSinkSourceFactory idFactory = getTransIDFactory(job);
		@SuppressWarnings("rawtypes")
		MutableRecordIdTranslator retVal =
			new MutableRecordIdTranslatorImpl(idFactory);
		return retVal;
	}

	protected String createRecordIdTypeQuery(BatchJob job) {
		final long jobId = job.getId();
		StringBuffer b = new StringBuffer();
		b.append("SELECT ").append(RecordIdTranslationJPA.CN_RECORD_TYPE);
		b.append(" FROM ").append(RecordIdTranslationJPA.TABLE_NAME);
		b.append(" WHERE ").append(RecordIdTranslationJPA.CN_JOB_ID);
		b.append(" = ").append(jobId);
		b.append(" GROUP BY ").append(RecordIdTranslationJPA.CN_JOB_ID);
		return b.toString();
	}

	@Override
	public RECORD_ID_TYPE getTranslatorType(BatchJob job)
			throws BlockingException {
		if (job == null) {
			throw new IllegalArgumentException("null OABA job");
		}

		// This method requires EclipseLink (it won't work for Hibernate)
		RECORD_ID_TYPE retVal = null;
		em.getTransaction().begin();
		Connection connection = null;
		Statement stmt = null;
		try {
			connection = em.unwrap(Connection.class);
			connection.setReadOnly(true);
			connection.setAutoCommit(true);
			String query = createRecordIdTypeQuery(job);
			logger.fine(query);

			Set<RECORD_ID_TYPE> dataTypes =
				EnumSet.noneOf(RECORD_ID_TYPE.class);
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				Integer i = rs.getInt(QUERY_INDEX_RECORD_ID_TYPE);
				if (i == null) {
					String msg = "null record-id type";
					throw new IllegalStateException(msg);
				}
				RECORD_ID_TYPE rit = RECORD_ID_TYPE.fromSymbol(i);
				assert rit != null;
				dataTypes.add(rit);
			}
			if (dataTypes.isEmpty()) {
				String msg = "No translated record identifier for job " + job;
				throw new BlockingException(msg);
			} else if (dataTypes.size() > 1) {
				String msg =
					"Inconsistent record identifiers for job " + job + "("
							+ dataTypes + ")";
				throw new BlockingException(msg);
			}
			assert dataTypes.size() == 1;
			retVal = dataTypes.iterator().next();
		} catch (SQLException e) {
			em.getTransaction().rollback();
			String msg =
				this.getClass().getSimpleName()
						+ ".getTranslatorType(OabaJob): "
						+ "unable to get record-id type: " + e;
			throw new BlockingException(msg, e);
		} finally {
			em.getTransaction().commit();
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					String msg =
						this.getClass().getSimpleName()
								+ ".getTranslatorType(OabaJob): "
								+ "unable to close JDBC connection: " + e;
					logger.severe(msg);
				}
			}
		}
		assert retVal != null;
		return retVal;
	}

	protected void restoreIntegerTranslations(BatchJob job,
			MutableRecordIdTranslator<Integer> translator)
			throws BlockingException {
		Query query =
			em.createNamedQuery(RecordIdTranslationJPA.QN_TRANSLATEDINTEGERID_FIND_BY_JOBID);
		@SuppressWarnings("unchecked")
		List<RecordIdIntegerTranslation> rits = query.getResultList();
		int index = 0;
		RECORD_SOURCE_ROLE role = RECORD_SOURCE_ROLE.STAGING;
		for (RecordIdIntegerTranslation rit : rits) {
			assert rit.getTranslatedId() == index;
			if (rit.getRecordSource() != role) {
				translator.split();
			}
			translator.translate(rit.getRecordId());
			++index;
		}
	}

	/**
	 * This gets the factory that is used to get translator id sink and source.
	 */
	private static RecordIdSinkSourceFactory getTransIDFactory(BatchJob job) {
		String wd = OabaFileUtils.getWorkingDir(job);
		return new RecordIdSinkSourceFactory(wd, BASENAME_RECORDID_TRANSLATOR,
				OabaFileUtils.BINARY_SUFFIX);
	}

	private static RecordIdSinkSourceFactory getRecordIDFactory(BatchJob job) {
		String wd = OabaFileUtils.getWorkingDir(job);
		return new RecordIdSinkSourceFactory(wd, BASENAME_RECORDID_STORE,
				OabaFileUtils.TEXT_SUFFIX);
	}

	@Override
	public IRecordIdSinkSourceFactory getRecordIdSinkSourceFactory(BatchJob job) {
		return getRecordIDFactory(job);
	}

	@SuppressWarnings("unchecked")
	public MutableRecordIdTranslator<?> restoreIRecordIdTranslator(BatchJob job)
			throws BlockingException {
		if (job == null) {
			throw new IllegalArgumentException("null OABA job");
		}
		RecordIdSinkSourceFactory idFactory = getTransIDFactory(job);
		MutableRecordIdTranslatorImpl retVal =
			new MutableRecordIdTranslatorImpl(idFactory);

		// Cleanup any files on disk
		retVal.cleanUp();

		RECORD_ID_TYPE dataType = this.getTranslatorType(job);
		assert dataType != null;
		switch (dataType) {
		case TYPE_INTEGER:
			restoreIntegerTranslations(job, retVal);
			break;
		case TYPE_LONG:
			restoreLongTranslations(job, retVal);
			break;
		case TYPE_STRING:
			restoreStringTranslations(job, retVal);
			break;
		default:
			throw new Error("unexpected record source type: " + dataType);
		}

		return retVal;
	}

	protected void restoreLongTranslations(BatchJob job,
			MutableRecordIdTranslator<Long> translator)
			throws BlockingException {
		Query query =
			em.createNamedQuery(RecordIdTranslationJPA.QN_TRANSLATEDLONGID_FIND_BY_JOBID);
		@SuppressWarnings("unchecked")
		List<RecordIdLongTranslation> rits = query.getResultList();
		int index = 0;
		RECORD_SOURCE_ROLE role = RECORD_SOURCE_ROLE.STAGING;
		for (RecordIdLongTranslation rit : rits) {
			assert rit.getTranslatedId() == index;
			if (rit.getRecordSource() != role) {
				translator.split();
			}
			translator.translate(rit.getRecordId());
			++index;
		}
	}

	protected void restoreStringTranslations(BatchJob job,
			MutableRecordIdTranslator<String> translator)
			throws BlockingException {
		Query query =
			em.createNamedQuery(RecordIdTranslationJPA.QN_TRANSLATEDSTRINGID_FIND_BY_JOBID);
		@SuppressWarnings("unchecked")
		List<RecordIdStringTranslation> rits = query.getResultList();
		int index = 0;
		RECORD_SOURCE_ROLE role = RECORD_SOURCE_ROLE.STAGING;
		for (RecordIdStringTranslation rit : rits) {
			assert rit.getTranslatedId() == index;
			if (rit.getRecordSource() != role) {
				translator.split();
			}
			translator.translate(rit.getRecordId());
			++index;
		}
	}

	@Override
	public void save(BatchJob job, MutableRecordIdTranslator<?> translator)
			throws BlockingException {
		if (job == null || translator == null) {
			throw new IllegalArgumentException("null argument");
		}
		if (!(translator instanceof MutableRecordIdTranslatorImpl)) {
			String msg = "Unhandled type: " + translator.getClass().getName();
			throw new IllegalStateException(msg);
		}
		ImmutableRecordIdTranslator<?> rit =
			translator.toImmutableTranslator();

		// Check the data type of the record ids handled by the translator
		final RECORD_ID_TYPE dataType = rit.getRecordIdType();
		if (dataType == null) {
			String msg =
				"Invalid translator: null record-id type. "
						+ "(Has the translator translated any record ids?)";
			throw new IllegalArgumentException(msg);
		}

		switch (dataType) {
		case TYPE_INTEGER:
			saveIntegerTranslations(job, rit);
			break;
		case TYPE_LONG:
			saveLongTranslations(job, rit);
			break;
		case TYPE_STRING:
			saveStringTranslations(job, rit);
			break;
		default:
			throw new Error("unexpected record source type: " + dataType);
		}
	}

	protected void saveIntegerTranslations(BatchJob job,
			ImmutableRecordIdTranslator<?> translator) {
		int translatedId = 0;
		Integer recordId = (Integer) translator.reverseLookup(translatedId);
		while (recordId != null) {
			RecordIdIntegerTranslation rit =
				new RecordIdIntegerTranslation(job, recordId,
						RECORD_SOURCE_ROLE.STAGING, translatedId);
			em.persist(rit);
			++translatedId;
		}
	}

	protected void saveLongTranslations(BatchJob job,
			ImmutableRecordIdTranslator<?> translator) {
		int translatedId = 0;
		Long recordId = (Long) translator.reverseLookup(translatedId);
		while (recordId != null) {
			RecordIdLongTranslation rit =
				new RecordIdLongTranslation(job, recordId,
						RECORD_SOURCE_ROLE.STAGING, translatedId);
			em.persist(rit);
			++translatedId;
		}
	}

	protected void saveStringTranslations(BatchJob job,
			ImmutableRecordIdTranslator<?> translator) {
		int translatedId = 0;
		String recordId = (String) translator.reverseLookup(translatedId);
		while (recordId != null) {
			RecordIdStringTranslation rit =
				new RecordIdStringTranslation(job, recordId,
						RECORD_SOURCE_ROLE.STAGING, translatedId);
			em.persist(rit);
			++translatedId;
		}
	}

}
