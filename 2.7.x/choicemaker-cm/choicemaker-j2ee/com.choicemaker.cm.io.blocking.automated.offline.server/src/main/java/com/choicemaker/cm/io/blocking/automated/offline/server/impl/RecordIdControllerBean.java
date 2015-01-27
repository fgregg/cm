package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.PN_TRANSLATEDID_DELETE_BY_JOBID_JOBID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.QN_TRANSLATEDID_DELETE_BY_JOBID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdTranslationJPA.QN_TRANSLATEDID_FIND_ALL;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.impl.BatchJobEntity;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.ImmutableRecordIdTranslator;
import com.choicemaker.cm.io.blocking.automated.offline.core.MutableRecordIdTranslator;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_SOURCE_ROLE;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ImmutableRecordIdTranslatorLocal;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdTranslation;

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

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@EJB
	private OabaJobControllerBean jobController;

	@Override
	public <T extends Comparable<T>> ImmutableRecordIdTranslatorLocal<T> findRecordIdTranslator(
			BatchJob job) throws BlockingException {
		ImmutableRecordIdTranslatorImpl irit = findTranslatorImpl(job);
		@SuppressWarnings("unchecked")
		ImmutableRecordIdTranslatorLocal<T> retVal = irit;
		return retVal;
	}

	protected <T extends Comparable<T>> List<AbstractRecordIdTranslationEntity<T>> findTranslationImpls(
			BatchJob job) throws BlockingException {
		Query query =
			em.createNamedQuery(RecordIdTranslationJPA.QN_TRANSLATEDID_FIND_BY_JOBID);
		query.setParameter(
				RecordIdTranslationJPA.PN_TRANSLATEDID_FIND_BY_JOBID_JOBID,
				job.getId());
		@SuppressWarnings("unchecked")
		List<AbstractRecordIdTranslationEntity<T>> retVal =
			query.getResultList();
		return retVal;
	}

	protected <T extends Comparable<T>> ImmutableRecordIdTranslatorImpl findTranslatorImpl(
			BatchJob job) throws BlockingException {
		logger.info("findTranslatorImpl " + job);
		if (job == null) {
			throw new IllegalArgumentException("null batch job");
		}
		List<AbstractRecordIdTranslationEntity<T>> translations =
			findTranslationImpls(job);
		final RECORD_ID_TYPE expectedRecordIdType = null;
		ImmutableRecordIdTranslatorImpl retVal =
			ImmutableRecordIdTranslatorImpl.createTranslator(job,
					expectedRecordIdType, translations);
		return retVal;
	}

	@Override
	public <T extends Comparable<T>> List<RecordIdTranslation<T>> findAllRecordIdTranslations() {
		Query query = em.createNamedQuery(QN_TRANSLATEDID_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<RecordIdTranslation<T>> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<RecordIdTranslation<T>>();
		}
		return entries;
	}

	@Override
	public int deleteTranslationsByJob(BatchJob job) {
		Query query = em.createNamedQuery(QN_TRANSLATEDID_DELETE_BY_JOBID);
		query.setParameter(PN_TRANSLATEDID_DELETE_BY_JOBID_JOBID, job.getId());
		int deletedCount = query.executeUpdate();
		return deletedCount;
	}

	/**
	 * Implements
	 * {@link RecordIdController#toImmutableTranslator(BatchJob, MutableRecordIdTranslator)
	 * save} for instances of {@link MutableRecordIdTranslatorImpl}.
	 * 
	 * @throws ClassCastException
	 *             if the specified translator is not an instance of
	 *             <code>MutableRecordIdTranslatorImpl</code>
	 * @throws BlockingException
	 *             if an immutable translator can not be created
	 */
	@Override
	public <T extends Comparable<T>> ImmutableRecordIdTranslator<T> toImmutableTranslator(
			MutableRecordIdTranslator<T> translator) throws BlockingException {
		if (translator == null) {
			throw new IllegalArgumentException("null translator");
		}
		// HACK FIXME and update the Javadoc to comply with the interface
		MutableRecordIdTranslatorImpl impl =
			(MutableRecordIdTranslatorImpl) translator;
		// END HACK
		@SuppressWarnings("unchecked")
		ImmutableRecordIdTranslator<T> retVal = toImmutableTranslatorImpl(impl);
		assert impl.isClosed();
		assert retVal != null;
		return retVal;
	}

	protected ImmutableRecordIdTranslatorImpl toImmutableTranslatorImpl(
			MutableRecordIdTranslatorImpl mrit) throws BlockingException {

		logger.entering("toImmutableTranslatorImpl", mrit.toString());

		final BatchJob job = mrit.getBatchJob();
		assert job != null && BatchJobEntity.isPersistent(job);

		ImmutableRecordIdTranslatorImpl retVal = null;
		if (mrit.isClosed() && !mrit.doTranslatorCachesExist()) {
			logger.finer("finding immutable translator");
			assert mrit.isClosed() && !mrit.doTranslatorCachesExist();
			retVal = findTranslatorImpl(job);
			logger.finer("found immutable translator: " + retVal);

		} else {
			logger.fine("constructing immutable translator");
			assert !mrit.isClosed() || mrit.doTranslatorCachesExist();
			mrit.close();
			final BatchJob j = mrit.getBatchJob();
			final IRecordIdSource<?> s1 =
				mrit.getFactory().getSource(mrit.getSink1());
			final IRecordIdSource<?> s2 =
				mrit.getFactory().getSource(mrit.getSink2());

			// Translator caches are removed by the constructor
			// ImmutableRecordIdTranslatorImpl
			retVal = new ImmutableRecordIdTranslatorImpl(j, s1, s2);
			assert !mrit.doTranslatorCachesExist();
			logger.fine("constructed immutable translator");

			// Save the immutable translator to persistent storage
			this.saveTranslatorImpl(job, retVal);
		}
		assert retVal != null;
		assert mrit.isClosed() && !mrit.doTranslatorCachesExist();

		return retVal;
	}

	@Override
	public MutableRecordIdTranslator<?> createMutableRecordIdTranslator(
			BatchJob job) throws BlockingException {

		logger.entering("createMutableRecordIdTranslator", job.toString());

		RecordIdSinkSourceFactory rFactory = getTransIDFactory(job);
		IRecordIdSink sink1 = rFactory.getNextSink();
		logger.finer("sink1: " + sink1);
		IRecordIdSink sink2 = rFactory.getNextSink();
		logger.finer("sink2: " + sink2);

		// Does an immutable translator already exist?
		// FIXME count translations, don't retrieve them
		List<?> translations = findTranslationImpls(job);
		if (translations != null && !translations.isEmpty()) {
			String msg =
				"Record-id translations already exist for job " + job.getId();
			throw new BlockingException(msg);
		}

		// Have the translator caches already been created?
		if (sink1.exists() || sink2.exists()) {
			logger.severe("Sink 1 already exists: " + sink1);
			if (sink1.exists() || sink2.exists()) {
				logger.severe("Sink 2 already exists: " + sink1);
			}
			File wd = job.getWorkingDirectory();
			String location = wd == null ? "unknown" : wd.getAbsolutePath();
			String msg =
				"A mutable translator appears to have been created already. "
						+ "A new translator can not be created until the "
						+ "existing caches have been removed in the working "
						+ "directory: " + location;
			throw new BlockingException(msg);
		}
		logger.finer("Sink 1: " + sink1);
		logger.finer("Sink 2: " + sink2);

		@SuppressWarnings("rawtypes")
		MutableRecordIdTranslator retVal =
			new MutableRecordIdTranslatorImpl(job, rFactory, sink1, sink2);
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
				RECORD_ID_TYPE rit = RECORD_ID_TYPE.fromValue(i);
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

	@Override
	public IRecordIdSinkSourceFactory getRecordIdSinkSourceFactory(BatchJob job) {
		return getRecordIDFactory(job);
	}

	/**
	 * Implements
	 * {@link RecordIdController#save(BatchJob, MutableRecordIdTranslator) save}
	 * for instances of {@link MutableRecordIdTranslatorImpl}. If the translator
	 * is not {@link MutableRecordIdTranslatorImpl#isClosed() closed}:
	 * <ol>
	 * <li>The translator is
	 * {@link IRecordIdFactory#toImmutableTranslator(MutableRecordIdTranslator)
	 * converted} to an immutable translator.</li>
	 * <li>The mutable translator is
	 * {@link MutableRecordIdTranslatorImpl#close() closed}.</li>
	 * <li>The translations of the immutable translator are saved in persistent
	 * storage.</li>
	 * </ol>
	 * If the mutable translator is already closed, its translations are
	 * presumed to be stored already. The translations are restored to an
	 * immutable translator which is then returned. If the translations are not
	 * found or can not be restored, an exception is thrown.
	 * 
	 * @throws ClassCastException
	 *             if the specified translator is not an instance of
	 *             <code>MutableRecordIdTranslatorImpl</code>
	 * @throws BlockingException
	 *             if the translations of the translator can not be saved or
	 *             found in persistent storage.
	 */
	@Override
	public <T extends Comparable<T>> ImmutableRecordIdTranslatorLocal<T> save(
			BatchJob job, ImmutableRecordIdTranslator<T> translator)
			throws BlockingException {
		if (job == null || translator == null) {
			throw new IllegalArgumentException("null argument");
		}

		// HACK FIXME and update the Javadoc to comply with the interface
		ImmutableRecordIdTranslatorImpl impl =
			(ImmutableRecordIdTranslatorImpl) translator;
		// END HACK

		@SuppressWarnings("unchecked")
		ImmutableRecordIdTranslatorLocal<T> retVal =
			(ImmutableRecordIdTranslatorLocal<T>) saveTranslatorImpl(job, impl);

		return retVal;
	}

	protected <T extends Comparable<T>> ImmutableRecordIdTranslatorImpl saveTranslatorImpl(
			BatchJob job, ImmutableRecordIdTranslatorImpl impl)
			throws BlockingException {

		ImmutableRecordIdTranslatorImpl retVal = null;
		// Check if the translations already exist in the database
		List<AbstractRecordIdTranslationEntity<T>> translations =
			findTranslationImpls(job);
		if (!translations.isEmpty()) {
			logger.info("Translations: " + translations.size());
			impl.assertPersistent(translations);
			retVal = impl;
			logger.fine("Returning unaltered translator: " + retVal);

		} else if (impl.isEmpty()) {
			String msg =
				"Translator is empty. "
						+ "(Has the translator translated any record ids?)";
			logger.info(msg);
			retVal = impl;
			logger.warning("No translations saved: " + retVal);

		} else {
			final RECORD_ID_TYPE dataType = impl.getRecordIdType();
			switch (dataType) {
			case TYPE_INTEGER:
				saveIntegerTranslations(job, impl);
				break;
			case TYPE_LONG:
				saveLongTranslations(job, impl);
				break;
			case TYPE_STRING:
				saveStringTranslations(job, impl);
				break;
			default:
				throw new Error("unexpected record source type: " + dataType);
			}
			translations = findTranslationImpls(job);
			retVal =
				ImmutableRecordIdTranslatorImpl.createTranslator(job, dataType,
						translations);
			logger.fine("Returning new translator: " + retVal);
		}
		assert retVal != null;
		return retVal;
	}

	protected void saveIntegerTranslations(BatchJob job,
			ImmutableRecordIdTranslatorImpl impl) {

		if (impl.isSplit()) {
			Integer recordId = RecordIdIntegerTranslation.RECORD_ID_PLACEHOLDER;
			int index = impl.getSplitIndex();
			RECORD_SOURCE_ROLE rsr = RECORD_SOURCE_ROLE.SPLIT_INDEX;
			RecordIdIntegerTranslation rit =
				new RecordIdIntegerTranslation(job, recordId, rsr, index);
			em.persist(rit);
		}

		@SuppressWarnings({
				"unchecked", "rawtypes" })
		Set<Map.Entry> entries1 = impl.ids1_To_Indices.entrySet();
		for (@SuppressWarnings("rawtypes")
		Map.Entry e1 : entries1) {
			Integer recordId = (Integer) e1.getKey();
			Integer index = (Integer) e1.getValue();
			RECORD_SOURCE_ROLE rsr = RECORD_SOURCE_ROLE.STAGING;
			RecordIdIntegerTranslation rit =
				new RecordIdIntegerTranslation(job, recordId, rsr, index);
			em.persist(rit);
		}

		@SuppressWarnings({
				"unchecked", "rawtypes" })
		Set<Map.Entry> entries2 = impl.ids2_To_Indices.entrySet();
		for (@SuppressWarnings("rawtypes")
		Map.Entry e2 : entries2) {
			Integer recordId = (Integer) e2.getKey();
			Integer index = (Integer) e2.getValue();
			RECORD_SOURCE_ROLE rsr = RECORD_SOURCE_ROLE.MASTER;
			RecordIdIntegerTranslation rit =
				new RecordIdIntegerTranslation(job, recordId, rsr, index);
			em.persist(rit);
		}
	}

	protected void saveLongTranslations(BatchJob job,
			ImmutableRecordIdTranslatorImpl impl) {

		if (impl.isSplit()) {
			Long recordId = RecordIdLongTranslation.RECORD_ID_PLACEHOLDER;
			int index = impl.getSplitIndex();
			RECORD_SOURCE_ROLE rsr = RECORD_SOURCE_ROLE.SPLIT_INDEX;
			RecordIdLongTranslation rit =
				new RecordIdLongTranslation(job, recordId, rsr, index);
			em.persist(rit);
		}

		@SuppressWarnings({
				"unchecked", "rawtypes" })
		Set<Map.Entry> entries1 = impl.ids1_To_Indices.entrySet();
		for (@SuppressWarnings("rawtypes")
		Map.Entry e1 : entries1) {
			Long recordId = (Long) e1.getKey();
			Integer index = (Integer) e1.getValue();
			RECORD_SOURCE_ROLE rsr = RECORD_SOURCE_ROLE.STAGING;
			RecordIdLongTranslation rit =
				new RecordIdLongTranslation(job, recordId, rsr, index);
			em.persist(rit);
		}

		@SuppressWarnings({
				"unchecked", "rawtypes" })
		Set<Map.Entry> entries2 = impl.ids2_To_Indices.entrySet();
		for (@SuppressWarnings("rawtypes")
		Map.Entry e2 : entries2) {
			Long recordId = (Long) e2.getKey();
			Integer index = (Integer) e2.getValue();
			RECORD_SOURCE_ROLE rsr = RECORD_SOURCE_ROLE.MASTER;
			RecordIdLongTranslation rit =
				new RecordIdLongTranslation(job, recordId, rsr, index);
			em.persist(rit);
		}
	}

	protected void saveStringTranslations(BatchJob job,
			ImmutableRecordIdTranslatorImpl impl) {

		if (impl.isSplit()) {
			String recordId = RecordIdStringTranslation.RECORD_ID_PLACEHOLDER;
			int index = impl.getSplitIndex();
			RECORD_SOURCE_ROLE rsr = RECORD_SOURCE_ROLE.SPLIT_INDEX;
			RecordIdStringTranslation rit =
				new RecordIdStringTranslation(job, recordId, rsr, index);
			em.persist(rit);
		}

		@SuppressWarnings({
				"unchecked", "rawtypes" })
		Set<Map.Entry> entries1 = impl.ids1_To_Indices.entrySet();
		for (@SuppressWarnings("rawtypes")
		Map.Entry e1 : entries1) {
			String recordId = (String) e1.getKey();
			Integer index = (Integer) e1.getValue();
			RECORD_SOURCE_ROLE rsr = RECORD_SOURCE_ROLE.STAGING;
			RecordIdStringTranslation rit =
				new RecordIdStringTranslation(job, recordId, rsr, index);
			em.persist(rit);
		}

		@SuppressWarnings({
				"unchecked", "rawtypes" })
		Set<Map.Entry> entries2 = impl.ids2_To_Indices.entrySet();
		for (@SuppressWarnings("rawtypes")
		Map.Entry e2 : entries2) {
			String recordId = (String) e2.getKey();
			Integer index = (Integer) e2.getValue();
			RECORD_SOURCE_ROLE rsr = RECORD_SOURCE_ROLE.MASTER;
			RecordIdStringTranslation rit =
				new RecordIdStringTranslation(job, recordId, rsr, index);
			em.persist(rit);
		}
	}

}
