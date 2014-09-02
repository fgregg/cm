/*
 * Copyright (c) 2001, 2009 ChoiceMaker Technologies, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ChoiceMaker Technologies, Inc. - initial API and implementation
 */
package com.choicemaker.cm.urm.ejb;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

import com.choicemaker.cm.core.Accessor;
import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.base.Match;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.core.base.RecordDecisionMaker;
import com.choicemaker.cm.core.report.ErrorReporter;
import com.choicemaker.cm.core.report.Report;
import com.choicemaker.cm.core.report.ReporterPlugin;
import com.choicemaker.cm.core.xmlconf.EmbeddedXmlConfigurator;
import com.choicemaker.cm.io.blocking.automated.base.AutomatedBlocker;
import com.choicemaker.cm.io.blocking.automated.base.Blocker2;
import com.choicemaker.cm.io.blocking.automated.base.BlockingSetReporter;
import com.choicemaker.cm.io.blocking.automated.base.DatabaseAccessor;
import com.choicemaker.cm.io.blocking.automated.base.IncompleteBlockingSetsException;
import com.choicemaker.cm.io.blocking.automated.base.UnderspecifiedQueryException;
import com.choicemaker.cm.io.db.base.DbAccessor;
import com.choicemaker.cm.io.db.base.DbReaderParallel;
import com.choicemaker.cm.io.xml.base.XmlSingleRecordWriter;
import com.choicemaker.cm.server.base.DatabaseException;
import com.choicemaker.cm.server.ejb.impl.CountsUpdate;
import com.choicemaker.cm.urm.adaptor.tocmcore.UrmRecordBuilder;
import com.choicemaker.cm.urm.base.DbRecordCollection;
import com.choicemaker.cm.urm.base.Decision3;
import com.choicemaker.cm.urm.base.EvalRecordFormat;
import com.choicemaker.cm.urm.base.EvaluatedRecord;
import com.choicemaker.cm.urm.base.ISingleRecord;
import com.choicemaker.cm.urm.base.MatchScore;
import com.choicemaker.cm.urm.base.RecordRef;
import com.choicemaker.cm.urm.base.RecordType;
import com.choicemaker.cm.urm.base.ScoreType;
import com.choicemaker.cm.urm.base.SubsetDbRecordCollection;
import com.choicemaker.cm.urm.exceptions.ArgumentException;
import com.choicemaker.cm.urm.exceptions.CmRuntimeException;
import com.choicemaker.cm.urm.exceptions.ConfigException;
import com.choicemaker.cm.urm.exceptions.ModelException;
import com.choicemaker.cm.urm.exceptions.RecordCollectionException;
import com.choicemaker.cm.urm.exceptions.RecordException;
import com.choicemaker.cm.urm.exceptions.UrmIncompleteBlockingSetsException;
import com.choicemaker.cm.urm.exceptions.UrmUnderspecifiedQueryException;
import com.choicemaker.util.StringUtils;

/**
 * @author emoussikaev
 * @version Revision: 2.5  Date: Sep 29, 2005 3:27:49 PM
 * @see
 */
public class OnlineMatchBaseBean implements SessionBean {

	private static final long serialVersionUID = 1L;
	protected transient SessionContext sessionContext;
	protected static Logger log = Logger.getLogger(OnlineMatchBaseBean.class.getName());
	protected static boolean initialized = false;

	/**
	 * Now a flag for whether counts have been cached in memory.
	 */
	protected static boolean isCountsUpdated = false;

	/**
	 * Returns the model that has the specified name.
	 * @exception IllegalArgumentException if the specified name is null
	 * @exception ModelException if a model with the specified name
	 * does not exist
	 */
	IProbabilityModel getProbabilityModel(String modelName)
		throws ModelException {
		if (modelName == null) {
			throw new IllegalArgumentException("null model name");
		}
		IProbabilityModel retVal = PMManager.getModelInstance(modelName);
		if (retVal == null) {
			log.severe("Invalid probability accessProvider: " + modelName);
			throw new ModelException(modelName);
		}
		return retVal;
	}

	Record getInternalRecord(
		ImmutableProbabilityModel model,
		ISingleRecord queryRecord) {
		UrmRecordBuilder irb = new UrmRecordBuilder(model);
		queryRecord.accept(irb);
		Record retVal = irb.getResultRecord();
		return retVal;
	}

	/**
	 * 
	 */
	public OnlineMatchBaseBean() {
		super();
	}

	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException {
	}

	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException, RemoteException {
	}

	public void ejbCreate() throws CreateException, RemoteException {
		try {
			if (!initialized) {
				EmbeddedXmlConfigurator.getInstance().embeddedInit(null);
				initialized = true;
			}
		} catch (Exception ex) {
			log.severe(ex.toString());
			throw new CreateException(ex.toString());
		}

	} // ejbCreate()

	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#ejbRemove()
	 */
	public void ejbRemove() throws EJBException, RemoteException {
	}

	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext sc)
		throws EJBException, RemoteException {
		this.sessionContext = sc;
	}

	protected void writeDebugInfo(
		ISingleRecord record,
		String probabilityModel,
		float differThreshold,
		float matchThreshold,
		int maxNumMatches,
		EvalRecordFormat returnDataFormat,
		String purpose,
		Level priority) {

		log.log(priority, "record: " + record);
		log.log(priority, "probabilityModel: " + probabilityModel);
		log.log(priority, "differThreshold: " + differThreshold);
		log.log(priority, "matchThreshold: " + matchThreshold);
		log.log(priority, "maxNumMatches: " + maxNumMatches);
		log.log(
			priority,
			"returnData.RecordType: " + returnDataFormat.getRecordType());
		log.log(
			priority,
			"returnData.scoreFormat: " + returnDataFormat.getScoreType());
		log.log(priority, "externalId: " + purpose);

		// Try to dump detail of the query record
		try {
			ImmutableProbabilityModel _model =
				PMManager.getModelInstance(probabilityModel);
			Record _internalRecord = getInternalRecord(_model, record);
			boolean _doXmlHeader = false;
			String _details =
				XmlSingleRecordWriter.writeRecord(
					_model,
					_internalRecord,
					_doXmlHeader);
			log.log(
				priority,
				"record detaills: " + Constants.LINE_SEPARATOR + _details);
		} catch (Exception x) {
			log.log(
				priority,
				"Unable to dump details of record '" + record.getId() + "'");
		}
	}

	protected SortedSet getMatches(
		long startTime,
		ISingleRecord queryRecord,
		DbRecordCollection masterCollection,
		String modelName,
		float differThreshold,
		float matchThreshold,
		int maxNumMatches,
		String externalId)
		throws
			ModelException,
			ArgumentException,
			RecordException,
			RecordCollectionException,
			CmRuntimeException,
			ConfigException,
			UrmIncompleteBlockingSetsException,
			UrmUnderspecifiedQueryException,
			RemoteException {

		IProbabilityModel model = null;
		Record q = null;
		AutomatedBlocker recordSource = null;
		SortedSet retVal = null;

		try {
			//validate input parameters
			if (maxNumMatches < -1) {
				throw new ConfigException(
					"invalid maxNumMatches:" + maxNumMatches);
			}
			if (maxNumMatches == -1) {
				maxNumMatches = Integer.MAX_VALUE;
			}
			String urlString = masterCollection.getUrl().trim();
			log.fine("url" + urlString);
			if (urlString == null || urlString.length() == 0)
				throw new RecordCollectionException("empty URL");
			Context ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup(urlString);

			if (!isCountsUpdated) {
				// BUG FIX 2009-08-21 rphall
				// It is not the responsibility of this service to update counts.
				// Treat the flag isCountsUpdated as a check for whether
				// counts have been cached in memory.
				new CountsUpdate().cacheCounts(ds);
				isCountsUpdated = true;
				// END BUGFIX
			}

			model = getProbabilityModel(modelName);
			String modelDbrName =
				(String) model.properties().get("dbConfiguration");
			if (!modelDbrName.equals(masterCollection.getName()))
				throw new RecordCollectionException("dbConfig should match accessProvider dbConfig attribute");

			q = getInternalRecord(model, queryRecord);
			RecordDecisionMaker dm = new RecordDecisionMaker();
			DatabaseAccessor databaseAccessor;
			try {
				IExtension dbaExt =
					Platform.getPluginRegistry().getExtension(
						Single.DATABASE_ACCESSOR,
						(String) model.properties().get(
							Single.DATABASE_ACCESSOR));
				databaseAccessor =
					(DatabaseAccessor) dbaExt
						.getConfigurationElements()[0]
						.createExecutableExtension("class");

				//PC 3/27/07
				if (masterCollection instanceof SubsetDbRecordCollection) {
					SubsetDbRecordCollection subset =
						(SubsetDbRecordCollection) masterCollection;
					Accessor acc = model.getAccessor();
					DbReaderParallel dbr =
						((DbAccessor) acc).getDbReaderParallel(modelDbrName);

					String masterId = dbr.getMasterId();
					String condition = parseSQL(subset.getIdsQuery(), masterId);
					log.fine("Condition: " + condition);
					String[] cs = new String[2];
					cs[0] = " ";
					cs[1] = condition;
					databaseAccessor.setCondition(cs);
				} else {
					databaseAccessor.setCondition("");
				}

				databaseAccessor.setDataSource(ds);
			} catch (Exception ex) {
				throw new ModelException(ex.toString());
			}

			String dbConfigName = masterCollection.getName();
			String blockingConfigName =
				(String) model.properties().get("blockingConfiguration");
			recordSource =
				new Blocker2(
					databaseAccessor,
					model,
					q,
					dbConfigName,
					blockingConfigName);
			retVal =
				dm.getMatches(
					q,
					recordSource,
					model,
					differThreshold,
					matchThreshold);

			reportSuccessfulQuery(
				startTime,
				q,
				model,
				differThreshold,
				matchThreshold,
				maxNumMatches,
				externalId,
				recordSource,
				retVal);

		} catch (DatabaseException ex) {
			log.severe(ex.toString());
			throw new RecordCollectionException(ex.toString());

		} catch (NamingException ex) {
			log.severe(ex.toString());
			throw new RecordCollectionException(ex.toString());

		} catch (RuntimeException ex) {
			log.severe(ex.toString());
			throw new CmRuntimeException(ex.toString());

		} catch (IncompleteBlockingSetsException ex) {
			// This is a data issue, so report it, then throw it.
			log.warning(ex.toString());
			UrmIncompleteBlockingSetsException thrown =
				new UrmIncompleteBlockingSetsException(ex.toString());
			reportUnsuccessfulQuery(
				startTime,
				q,
				model,
				differThreshold,
				matchThreshold,
				maxNumMatches,
				externalId,
				thrown);
			throw thrown;

		} catch (UnderspecifiedQueryException ex) {
			log.warning(ex.toString());
			// This is a data issue, so report it, then throw it.
			UrmUnderspecifiedQueryException thrown =
				new UrmUnderspecifiedQueryException(ex.toString());
			reportUnsuccessfulQuery(
				startTime,
				q,
				model,
				differThreshold,
				matchThreshold,
				maxNumMatches,
				externalId,
				thrown);
			throw thrown;

		} catch (IOException ex) {
			log.severe(ex.toString());
			throw new RecordCollectionException(ex.toString()); //TODO
		}

		return retVal;
	}

	void reportSuccessfulQuery(
		long startTime,
		Record q,
		IProbabilityModel model,
		float differThreshold,
		float matchThreshold,
		int maxNumMatches,
		String externalId,
		AutomatedBlocker recordSource,
		SortedSet retVal) {
		ReporterPlugin[] reporterPlugins =
			new ReporterPlugin[] { new BlockingSetReporter(recordSource)};
		try {
			model.report(
				new Report(
					differThreshold,
					matchThreshold,
					maxNumMatches,
					model,
					startTime,
					System.currentTimeMillis(),
					externalId,
					q,
					recordSource.getNumberOfRecordsRetrieved(),
					retVal,
					reporterPlugins));
		} catch (Exception ex) {
			log.severe("reporting: " + ex);
		}
	}

	void reportUnsuccessfulQuery(
		long startTime,
		Record q,
		IProbabilityModel model,
		float differThreshold,
		float matchThreshold,
		int maxNumMatches,
		String externalId,
		Throwable thrown) {
		final int numRecordsRetrieved = 0;
		final SortedSet results = null;
		ReporterPlugin[] reporterPlugins =
			new ReporterPlugin[] { new ErrorReporter(thrown)};
		try {
			model.report(
				new Report(
					differThreshold,
					matchThreshold,
					maxNumMatches,
					model,
					startTime,
					System.currentTimeMillis(),
					externalId,
					q,
					numRecordsRetrieved,
					results,
					reporterPlugins));
		} catch (Exception ex) {
			log.severe("reporting: " + ex);
		}
	}

	/** This method takes the SQL string specified in 
	 * <code>SubsetDbRecordCollection</code> and converts that to the condition
	 * string as expected by <code>DatabaseAccessor</code>, particularly by
	 * <code>OraDatabaseAccessor</code>.
	 * <p>
	 * For example: SQL String =
	 * "select mci_id from tb_patient where id_stat_cd = 'A' order by mci_id"
	 * <p>
	 * return = 
	 * "TB_PATIENT T WHERE B.MCI_ID = T.MCI_ID AND ID_STAT_CD = 'A'"
	 * <p>
	 * @author PC (3/27/2007)
	 * 
	 * @param input
	 * @param key
	 * @return
	 */
	protected static String parseSQL(String input, String key)
		throws CmRuntimeException {
		StringBuffer ret = new StringBuffer();

		// FIXME use ANTLR for more robust parsing

		if (!StringUtils.nonEmptyString(input)) {
			throw new IllegalArgumentException("null or blank SQL record id selection statement");
		}
		if (!StringUtils.nonEmptyString(key)) {
			throw new IllegalArgumentException("null or blank SQL key");
		}

		//get "WHERE" and "FROM"
		input = input.toUpperCase();
		key = key.toUpperCase();
		int w = input.indexOf("WHERE");
		int o = input.indexOf("ORDER");

		// Must be a FROM clause and exactly one TABLE
		int t = input.indexOf("FROM");
		if (t < 0) {
			throw new CmRuntimeException(
				"Missing FROM clause in SQL record id selection statement: '"
					+ input
					+ "'");
		}
		String str = null;
		if (w < 0 && o < 0) {
			str = input.substring(t + 5);
		} else if (w > 0) {
			str = input.substring(t + 5, w - 1);
		} else if (o > 0) {
			str = input.substring(t + 5, o - 1);
		}
		if (str.indexOf(',') != -1) {
			throw new IllegalArgumentException("cannot have more than 1 table.");
		}

		// Append the (table) name which occurs after the FROM clause
		int i = str.indexOf(' ');
		if (i == -1) {
			ret.append(str);
		} else {
			ret.append(str.substring(0, i));
		}

		ret.append(" T ");
		ret.append("WHERE");
		ret.append(" B.");
		ret.append(key);
		ret.append(" = T.");
		ret.append(key);

		if (w > 0) {
			ret.append(" AND ");
			if (o > 0) {
				ret.append(input.substring(w + 6, o - 1));
			} else {
				ret.append(input.substring(w + 6));
			}
		}

		return ret.toString();
	}

	/**
	 * @throws IllegalArgumentException if model is null
	 */
	MatchScore getMatchScore(
		ScoreType st,
		Match match,
		ImmutableProbabilityModel model) {
		if (model == null) {
			throw new IllegalArgumentException("null model");
		}
		MatchScore ms;
		String note = "";
		if (st.equals(ScoreType.RULE_LIST_NOTE)) {
			String[] notes = match.ac.getNotes(model);
			for (int n = 0; n < notes.length; n++)
				note = note + "\t" + notes[n];
		}
		ms =
			new MatchScore(
				match.probability,
				Decision3.valueOf(match.decision.toString()),
				note);
		return ms;
	}

	/**
	 * @throws IllegalArgumentException if model is null
	 */
	ISingleRecord getSingleRecord(
		EvalRecordFormat resultFormat,
		Match match,
		ImmutableProbabilityModel model) {
		if (model == null) {
			throw new IllegalArgumentException("null model");
		}
		ISingleRecord resRecord;
		if (resultFormat.getRecordType() == RecordType.HOLDER) {
			Object o = model.getAccessor().toRecordHolder(match.m);
			resRecord = (ISingleRecord) o;
		} else if (resultFormat.getRecordType() == RecordType.NONE)
			resRecord = null;
		else
			resRecord = new RecordRef(match.m.getId());

		return resRecord;
	}

	/**
	 * @throws IllegalArgumentException if model is null
	 */
	EvaluatedRecord getEvaluatedRecord(
		EvalRecordFormat resultFormat,
		Match match,
		IProbabilityModel model) {
		if (model == null) {
			throw new IllegalArgumentException("null model");
		}
		return new EvaluatedRecord(
			getSingleRecord(resultFormat, match, model),
			getMatchScore(resultFormat.getScoreType(), match, model));
	}
}
