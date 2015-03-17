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
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaPairResultController;

@Stateless
public class OabaPairResultControllerBean implements OabaPairResultController {

	private static final Logger logger = Logger
			.getLogger(OabaPairResultControllerBean.class.getName());

	/**
	 * The index in the {@link #createRecordIdTypeQuery(BatchJob)
	 * RecordIdTypeQuery} of the RECORD_ID_TYPE field
	 */
	protected static final int QUERY_INDEX_RECORD_ID_TYPE = 1;

	/**
	 * The index in the {@link #createRecordCountQuery(BatchJob)
	 * RecordIdTypeQuery} of the count field
	 */
	protected static final int QUERY_INDEX_RECORD_COUNT = 1;

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@EJB(beanName = "OabaJobControllerBean")
	private OabaJobController jobController;

	@Override
	public int getResultCount(BatchJob job) throws BlockingException {
		if (job == null) {
			throw new IllegalArgumentException("null OABA job");
		}

		// This method requires EclipseLink (it won't work for Hibernate)
		int retVal = 0;
		int resultCount = 0;
		em.getTransaction().begin();
		Connection connection = null;
		Statement stmt = null;
		try {
			connection = em.unwrap(Connection.class);
			connection.setReadOnly(true);
			connection.setAutoCommit(true);
			String query = createRecordCountQuery(job);
			logger.fine(query);

			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				++resultCount;
				Integer i = rs.getInt(QUERY_INDEX_RECORD_COUNT);
				if (i == null) {
					String msg = "null record count";
					throw new IllegalStateException(msg);
				}
				retVal = i;
				logger.info("Result count (job " + job.getId() + "): " + retVal);
				if (resultCount > 1) {
					String msg =
						"Multiple result counts for job " + job.getId() + ". "
								+ "Second count: " + retVal;
					throw new IllegalStateException(msg);
				}
			}
			if (resultCount == 0) {
				String msg = "No results from count query";
				throw new IllegalStateException(msg);
			}
		} catch (SQLException e) {
			em.getTransaction().rollback();
			String msg =
				this.getClass().getSimpleName() + ".getResultType(BatchJob): "
						+ "unable to get pair-wise results: " + e;
			throw new BlockingException(msg, e);
		} finally {
			em.getTransaction().commit();
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					String msg =
						this.getClass().getSimpleName()
								+ ".getResultType(BatchJob): "
								+ "unable to close JDBC connection: " + e;
					logger.severe(msg);
				}
			}
		}
		assert resultCount == 1;
		assert retVal >= 0;
		return retVal;
	}

	protected String createRecordCountQuery(BatchJob job) {
		final long jobId = job.getId();
		StringBuffer b = new StringBuffer();
		b.append("SELECT COUNT(*) ").append(
				RecordIdTranslationJPA.CN_RECORD_TYPE);
		b.append(" FROM ").append(RecordIdTranslationJPA.TABLE_NAME);
		b.append(" WHERE ").append(RecordIdTranslationJPA.CN_JOB_ID);
		b.append(" = ").append(jobId);
		return b.toString();
	}

	@Override
	public RECORD_ID_TYPE getResultType(BatchJob job) throws BlockingException {
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
				String msg = "No OABA pair-wise results for job " + job;
				throw new BlockingException(msg);
			} else if (dataTypes.size() > 1) {
				String msg =
					"Inconsistent OABA pair-wise results for job " + job + "("
							+ dataTypes + ")";
				throw new BlockingException(msg);
			}
			assert dataTypes.size() == 1;
			retVal = dataTypes.iterator().next();
		} catch (SQLException e) {
			em.getTransaction().rollback();
			String msg =
				this.getClass().getSimpleName() + ".getResultType(BatchJob): "
						+ "unable to get pair-wise results: " + e;
			throw new BlockingException(msg, e);
		} finally {
			em.getTransaction().commit();
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					String msg =
						this.getClass().getSimpleName()
								+ ".getResultType(BatchJob): "
								+ "unable to close JDBC connection: " + e;
					logger.severe(msg);
				}
			}
		}
		assert retVal != null;
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

	/**
	 * Reads results for the specified job from the database and writes them to
	 * the specified sink.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void getResults(BatchJob job, IMatchRecord2Sink<?> results)
			throws BlockingException {

		if (job == null || results == null) {
			throw new IllegalArgumentException("null argument");
		}

		RECORD_ID_TYPE dataType = this.getResultType(job);
		assert dataType != null;
		switch (dataType) {
		case TYPE_INTEGER:
			getIntegerResults(job, (IMatchRecord2Sink<Integer>) results);
			break;
		case TYPE_LONG:
			getLongResults(job, (IMatchRecord2Sink<Long>) results);
			break;
		case TYPE_STRING:
			getStringResults(job, (IMatchRecord2Sink<String>) results);
			break;
		default:
			throw new Error("unexpected record source type: " + dataType);
		}
	}

	protected void getIntegerResults(BatchJob job,
			IMatchRecord2Sink<Integer> results) throws BlockingException {
		Query query =
			em.createNamedQuery(OabaPairResultJPA.QN_PAIRRESULTINTEGER_FIND_BY_JOBID);
		@SuppressWarnings("unchecked")
		List<OabaPairResultInteger> entities = query.getResultList();
		results.open();
		for (OabaPairResultInteger entity : entities) {
			MatchRecord2<Integer> mr =
				new MatchRecord2<>(entity.getRecord1Id(),
						entity.getRecord2Id(), entity.getRecord2Source(),
						entity.getProbability(), entity.getDecision(),
						entity.getNotesAsDelimitedString());
			results.writeMatch(mr);
		}
		results.flush();
	}

	protected void getLongResults(BatchJob job, IMatchRecord2Sink<Long> results)
			throws BlockingException {
		Query query =
			em.createNamedQuery(RecordIdTranslationJPA.QN_TRANSLATEDLONGID_FIND_BY_JOBID);
		@SuppressWarnings("unchecked")
		List<OabaPairResultLong> entities = query.getResultList();
		results.open();
		for (OabaPairResultLong entity : entities) {
			MatchRecord2<Long> mr =
				new MatchRecord2<>(entity.getRecord1Id(),
						entity.getRecord2Id(), entity.getRecord2Source(),
						entity.getProbability(), entity.getDecision(),
						entity.getNotesAsDelimitedString());
			results.writeMatch(mr);
		}
		results.flush();
	}

	protected void getStringResults(BatchJob job,
			IMatchRecord2Sink<String> results) throws BlockingException {
		Query query =
			em.createNamedQuery(RecordIdTranslationJPA.QN_TRANSLATEDSTRINGID_FIND_BY_JOBID);
		@SuppressWarnings("unchecked")
		List<OabaPairResultString> entities = query.getResultList();
		results.open();
		for (OabaPairResultString entity : entities) {
			MatchRecord2<String> mr =
				new MatchRecord2<>(entity.getRecord1Id(),
						entity.getRecord2Id(), entity.getRecord2Source(),
						entity.getProbability(), entity.getDecision(),
						entity.getNotesAsDelimitedString());
			results.writeMatch(mr);
		}
		results.flush();
	}

	/**
	 * Reads results from a source and writes them to the database for a
	 * specified job
	 */
	@Override
	public void saveResults(BatchJob job, IMatchRecord2Source<?> results)
			throws BlockingException {

		if (job == null || results == null) {
			throw new IllegalArgumentException("null argument");
		}

		final int count = getResultCount(job);
		if (count > 0) {
			throw new BlockingException("results already saved: " + count);
		}

		// Check the data type of the first record identifier
		if (!results.hasNext()) {
			String msg = "No results saved: source is empty";
			logger.warning(msg);

		} else {
			MatchRecord2<?> firstResult = results.next();
			if (firstResult == null) {
				String msg =
					"Invalid source: the first MatchRecord2 element is null";
				throw new IllegalStateException(msg);
			}
			Object firstRecordId = firstResult.getRecordID1();
			if (firstRecordId == null) {
				String msg = "Invalid source: the first record id is null";
				throw new IllegalStateException(msg);
			}
			Class<?> firstRecordIdType = firstRecordId.getClass();
			final RECORD_ID_TYPE dataType =
				RECORD_ID_TYPE.fromClass(firstRecordIdType);

			switch (dataType) {
			case TYPE_INTEGER:
				saveIntegerResults(job, firstResult, results);
				break;
			case TYPE_LONG:
				saveLongResults(job, firstResult, results);
				break;
			case TYPE_STRING:
				saveStringResults(job, firstResult, results);
				break;
			default:
				throw new Error("unexpected record id type: " + dataType);
			}

		}
	}

	protected void saveIntegerResults(BatchJob job, MatchRecord2<?> rawResult,
			IMatchRecord2Source<?> rawResults) throws BlockingException {

		@SuppressWarnings("unchecked")
		final MatchRecord2<Integer> firstResult =
			(MatchRecord2<Integer>) rawResult;
		OabaPairResultInteger opr =
			new OabaPairResultInteger(job, firstResult.getRecordID1(),
					firstResult.getRecordID2(), firstResult.getRecord2Role(),
					firstResult.getProbability(), firstResult.getMatchType(),
					firstResult.getNotes());
		em.persist(opr);

		@SuppressWarnings("unchecked")
		final IMatchRecord2Source<Integer> results =
			(IMatchRecord2Source<Integer>) rawResults;
		while (results.hasNext()) {
			MatchRecord2<Integer> result = results.next();
			opr =
				new OabaPairResultInteger(job, result.getRecordID1(),
						result.getRecordID2(), result.getRecord2Role(),
						result.getProbability(), result.getMatchType(),
						result.getNotes());
			em.persist(opr);
		}
	}

	protected void saveLongResults(BatchJob job, MatchRecord2<?> rawResult,
			IMatchRecord2Source<?> rawResults) throws BlockingException {

		@SuppressWarnings("unchecked")
		MatchRecord2<Long> firstResult = (MatchRecord2<Long>) rawResult;
		OabaPairResultLong opr =
			new OabaPairResultLong(job, firstResult.getRecordID1(),
					firstResult.getRecordID2(), firstResult.getRecord2Role(),
					firstResult.getProbability(), firstResult.getMatchType(),
					firstResult.getNotes());
		em.persist(opr);

		@SuppressWarnings("unchecked")
		IMatchRecord2Source<Long> results =
			(IMatchRecord2Source<Long>) rawResults;
		while (results.hasNext()) {
			MatchRecord2<Long> result = results.next();
			opr =
				new OabaPairResultLong(job, result.getRecordID1(),
						result.getRecordID2(), result.getRecord2Role(),
						result.getProbability(), result.getMatchType(),
						result.getNotes());
			em.persist(opr);
		}
	}

	protected void saveStringResults(BatchJob job, MatchRecord2<?> rawResult,
			IMatchRecord2Source<?> rawResults) throws BlockingException {

		@SuppressWarnings("unchecked")
		MatchRecord2<String> firstResult = (MatchRecord2<String>) rawResult;
		OabaPairResultString opr =
			new OabaPairResultString(job, firstResult.getRecordID1(),
					firstResult.getRecordID2(), firstResult.getRecord2Role(),
					firstResult.getProbability(), firstResult.getMatchType(),
					firstResult.getNotes());
		em.persist(opr);

		@SuppressWarnings("unchecked")
		IMatchRecord2Source<String> results =
			(IMatchRecord2Source<String>) rawResults;
		while (results.hasNext()) {
			MatchRecord2<String> result = results.next();
			opr =
				new OabaPairResultString(job, result.getRecordID1(),
						result.getRecordID2(), result.getRecord2Role(),
						result.getProbability(), result.getMatchType(),
						result.getNotes());
			em.persist(opr);
		}
	}

}
