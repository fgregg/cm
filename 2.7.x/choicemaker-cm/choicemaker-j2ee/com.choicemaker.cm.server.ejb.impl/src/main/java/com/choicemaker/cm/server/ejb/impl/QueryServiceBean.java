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
package com.choicemaker.cm.server.ejb.impl;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.AccessControlException;
import java.util.Iterator;
import java.util.SortedSet;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.InvalidProfileException;
import com.choicemaker.cm.core.Profile;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.base.Match;
import com.choicemaker.cm.core.base.MatchCandidate;
import com.choicemaker.cm.core.base.MatchCandidateFactory;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.core.base.RecordDecisionMaker;
import com.choicemaker.cm.core.report.Report;
import com.choicemaker.cm.core.report.ReporterPlugin;
import com.choicemaker.cm.core.xmlconf.EmbeddedXmlConfigurator;
import com.choicemaker.cm.io.blocking.automated.base.AutomatedBlocker;
import com.choicemaker.cm.io.blocking.automated.base.Blocker2;
import com.choicemaker.cm.io.blocking.automated.base.BlockingSetReporter;
import com.choicemaker.cm.io.blocking.automated.base.DatabaseAccessor;
import com.choicemaker.cm.io.blocking.automated.base.UnderspecifiedQueryException;
import com.choicemaker.cm.server.base.DatabaseException;
import com.choicemaker.cm.server.base.InvalidModelException;
import com.choicemaker.cm.server.base.Result;
import com.choicemaker.cm.server.base.UnderspecifiedProfileException;
//import com.choicemaker.cm.core.base.Accessor;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.2 $ $Date: 2010/03/27 22:04:17 $
 */
public class QueryServiceBean implements SessionBean {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(QueryServiceBean.class.getName());

	public static final String BLOCKING_SOURCE = "java:comp/env/jdbc/blockingSource";
	public static final String REPORTING_SOURCE = "java:comp/env/jdbc/reportingSource";
	public static final String REPORTING_FILE = "reportingFile";
	public static final String DATABASE_ACCESSOR = "com.choicemaker.cm.io.blocking.automated.base.databaseAccessor";
	public static final String MATCH_CANDIDATE = "com.choicemaker.cm.core.matchCandidate";

	private static boolean inited;
//	private static transient Reporter fileReporter;
	protected transient DataSource blockingSource;
//	private transient DataSource reportingSource;
	private NameServiceLookup nameServiceLookup = new NameServiceLookup();

	private static synchronized void init(DataSource dataSource) throws XmlConfException, RemoteException, DatabaseException {
		if (!inited) {
//			ICompiler compiler = DoNothingCompiler.instance;
//			XmlConfigurator.embeddedInit(compiler);
			EmbeddedXmlConfigurator.getInstance().embeddedInit(null);
			// BUG 2009-08-21 rphall
			// The following code can cause unnecessary recalculations of counts
			// that are persistent in a database. For example, if CM Server is
			// restarted, this will update counts that are already valid in the DB.
			// Recalculating counts can be quite slow for large databases; e.g.
			// 30 minutes for a 4.3M record database.
			//
			// It is really not the responsibility of this service to update counts.
			// That responsibility belongs to an administrative service.
			//new CountsUpdate().updateCounts(dataSource, true);
			// END BUG
			// BUG FIX 2009-08-21 rphall
			new CountsUpdate().cacheCounts(dataSource);
			// END BUGFIX
			inited = true;
		}
	}

	public void ejbCreate() throws CreateException {
		try {
			blockingSource = (DataSource) nameServiceLookup.lookup(BLOCKING_SOURCE, DataSource.class);
			init(blockingSource);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new CreateException(ex.toString());
		}
	}

	public Result findMatches(
		Profile profile,
		Object constraint,
		String probabilityModel,
		float differThreshold,
		float matchThreshold,
		int maxNumMatches,
		String returnDataFormat,
		String purpose)
		throws InvalidModelException, InvalidProfileException, UnderspecifiedProfileException, DatabaseException, AccessControlException, RemoteException {
		try {
			long startTime = System.currentTimeMillis();
			logger.debug("findMatches:begin");
			if (logger.isDebugEnabled()) {
				writeDebugInfo(
					profile,
					constraint,
					probabilityModel,
					differThreshold,
					matchThreshold,
					maxNumMatches,
					returnDataFormat,
					purpose,
					Priority.DEBUG);
			}

			IProbabilityModel model = PMManager.getModelInstance(probabilityModel);
			if (model == null) {
				logger.error("Invalid probability accessProvider: " + probabilityModel);
				throw new InvalidModelException(probabilityModel);
			}
			// 2014-04-24 rphall: Commented out unused local variable
			// Any side effects?
//			Accessor accessor = model.getAccessor();
			Record q = profile.getRecord(model);
			RecordDecisionMaker dm = new RecordDecisionMaker();
			DatabaseAccessor databaseAccessor;
			try {
				IExtension dbaExt = Platform.getPluginRegistry().getExtension(DATABASE_ACCESSOR, (String) model.properties().get(DATABASE_ACCESSOR));
				databaseAccessor = (DatabaseAccessor) dbaExt.getConfigurationElements()[0].createExecutableExtension("class");
				databaseAccessor.setCondition(constraint);
				databaseAccessor.setDataSource(blockingSource);
			} catch (Exception ex) {
				throw new InvalidModelException(ex.toString());
			}
			SortedSet s;
			AutomatedBlocker rs = new Blocker2(databaseAccessor, model, q);
			try {
				s = dm.getMatches(q, rs, model, differThreshold, matchThreshold);
			} catch (UnderspecifiedQueryException ex) {
				logger.warn("", ex);
				throw new UnderspecifiedProfileException("", ex);
			} catch (IOException ex) {
				logger.error("Database error: " + ex, ex);
				if (!logger.isDebugEnabled()) {
					writeDebugInfo(
						profile,
						constraint,
						probabilityModel,
						differThreshold,
						matchThreshold,
						maxNumMatches,
						returnDataFormat,
						purpose,
						Priority.ERROR);
				}
				throw new DatabaseException("", ex);
			}
			MatchCandidateFactory matchCandidateFactory;
			if (returnDataFormat == null) {
				matchCandidateFactory = new MatchCandidateFactory();
			} else {
				try {
					matchCandidateFactory =
						(MatchCandidateFactory) Platform
							.getPluginRegistry()
							.getExtension(MATCH_CANDIDATE, returnDataFormat)
							.getConfigurationElements()[0]
							.createExecutableExtension(
							"class");
				} catch (Exception ex) {
					throw new InvalidProfileException("", ex);
				}
			}
			MatchCandidate[] matchCandidates = new MatchCandidate[Math.min(s.size(), maxNumMatches)];
			Iterator iS = s.iterator();
			for (int i = 0; i < matchCandidates.length; i++) {
				Match match = (Match) iS.next();
				matchCandidates[i] = matchCandidateFactory.createMatchCandidate(match, model);
			}
			ReporterPlugin[] reporterPlugins = new ReporterPlugin[] { new BlockingSetReporter(rs)};
			try {
				model.report(
					new Report(
						differThreshold,
						matchThreshold,
						maxNumMatches,
						model,
						startTime,
						System.currentTimeMillis(),
						purpose,
						q,
						rs.getNumberOfRecordsRetrieved(),
						s,
						reporterPlugins));
			} catch (IOException ex) {
				logger.error("reporting", ex);
			}
			return new Result(matchCandidates);
		} catch (RuntimeException ex) {
			logger.error("runtime exception", ex);
			throw new RemoteException("runtime exception", ex);
		}
	}

	protected void writeDebugInfo(
		Profile profile,
		Object constraint,
		String probabilityModel,
		float differThreshold,
		float matchThreshold,
		int maxNumMatches,
		String returnDataFormat,
		String purpose,
		Priority priority) {
		logger.log(priority, "profile: " + profile);
		logger.log(priority, "constraint: " + constraint);
		logger.log(priority, "probabilityModel: " + probabilityModel);
		logger.log(priority, "differThreshold: " + differThreshold);
		logger.log(priority, "matchThreshold: " + matchThreshold);
		logger.log(priority, "maxNumMatches: " + maxNumMatches);
		logger.log(priority, "returnDataFormat: " + returnDataFormat);
		logger.log(priority, "purpose: " + purpose);
	}

	public void ejbActivate() throws EJBException, RemoteException {
		throw new UnsupportedOperationException("Stateless bean should never be passivated");
	}

	public void ejbPassivate() throws EJBException, RemoteException {
		throw new UnsupportedOperationException("Stateless bean should never be passivated");
	}

	public void ejbRemove() throws EJBException, RemoteException {
	}

	public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException {
	}
}
