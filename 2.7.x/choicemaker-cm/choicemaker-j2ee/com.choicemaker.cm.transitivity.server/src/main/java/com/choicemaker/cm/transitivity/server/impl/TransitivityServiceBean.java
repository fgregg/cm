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
package com.choicemaker.cm.transitivity.server.impl;

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
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.InvalidProfileException;
import com.choicemaker.cm.core.Profile;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.XmlConfException;
//import com.choicemaker.cm.core.base.Accessor;
import com.choicemaker.cm.core.base.BeanMatchCandidate;
import com.choicemaker.cm.core.base.MatchCandidate;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.core.base.RecordDecisionMaker;
import com.choicemaker.cm.core.xmlconf.EmbeddedXmlConfigurator;
import com.choicemaker.cm.io.blocking.automated.base.AutomatedBlocker;
import com.choicemaker.cm.io.blocking.automated.base.Blocker2;
import com.choicemaker.cm.io.blocking.automated.base.DatabaseAccessor;
import com.choicemaker.cm.io.blocking.automated.base.UnderspecifiedQueryException;
import com.choicemaker.cm.server.base.DatabaseException;
import com.choicemaker.cm.server.base.InvalidModelException;
import com.choicemaker.cm.server.base.UnderspecifiedProfileException;
import com.choicemaker.cm.server.ejb.impl.CountsUpdate;
import com.choicemaker.cm.server.ejb.impl.NameServiceLookup;
import com.choicemaker.cm.transitivity.core.TransitivityException;
import com.choicemaker.cm.transitivity.core.TransitivityResult;
import com.choicemaker.cm.transitivity.server.util.MatchBiconnectedIterator;
import com.choicemaker.cm.transitivity.util.CEFromMatchCandidatesBuilder;
import com.choicemaker.cm.transitivity.util.CEFromMatchesBuilder;


/**
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
public class TransitivityServiceBean implements SessionBean {


	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(TransitivityServiceBean.class);

	public static final String DATABASE_ACCESSOR = "com.choicemaker.cm.io.blocking.automated.base.databaseAccessor";
	public static final String BLOCKING_SOURCE = "java:comp/env/jdbc/blockingSource";

	private transient DataSource blockingSource;
	private static boolean inited;
	private NameServiceLookup nameServiceLookup = new NameServiceLookup();


	/** This method finds the matches and the cluster for the input record.
	 *
	 * @param profile - Profile containing the input record
	 * @param constraint - constraint
	 * @param probabilityModel - name of the probability accessProvider
	 * @param differThreshold - differ threshold
	 * @param matchThreshold - match threshold
	 * @param maxNumMatches - maximum number of matches to return
	 * @param returnDataFormat - return format
	 * @param purpose - string purpose identifier
	 * @return TransitivityResult
	 * @throws AccessControlException
	 * @throws InvalidProfileException
	 * @throws RemoteException
	 * @throws InvalidModelException
	 * @throws UnderspecifiedProfileException
	 * @throws DatabaseException
	 */
	public TransitivityResult findClusters(
		Profile profile,
		Object constraint,
		String probabilityModel,
		float differThreshold,
		float matchThreshold,
		int maxNumMatches,
		String returnDataFormat,
		String purpose,
		boolean compact)
		throws AccessControlException, InvalidProfileException, RemoteException,
		InvalidModelException, UnderspecifiedProfileException, DatabaseException {

		logger.info("starting findCluster");


		IProbabilityModel model = PMManager.getModelInstance(probabilityModel);
		if (model == null) {
			logger.error("Invalid probability accessProvider: " + probabilityModel);
			throw new InvalidModelException(probabilityModel);
		}
		// 2014-04-24 rphall: Commented out unused local variable
		// Any side effects?
//		Accessor accessor = model.getAccessor();
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
			throw new DatabaseException("", ex);
		}

		TransitivityResult tr = null;
		try {
			Iterator ces = getCompositeEntities (q, s, probabilityModel, differThreshold,
				matchThreshold);

			if (compact) {
				tr = new TransitivityResult (probabilityModel, differThreshold,
					matchThreshold, new MatchBiconnectedIterator(ces));
			} else {
				tr = new TransitivityResult (probabilityModel, differThreshold,
					matchThreshold, ces);
			}
		} catch (TransitivityException e) {
			logger.error(e.toString(), e);
		}

		return tr;
	}


	/** This method takes the output of findMatches and runs the match result through the
	 * Transitivity Engine.
	 *
	 * @param profile - contains the query record
	 * @param candidates - match candidates to the query record
	 * @param modelName - probability accessProvider name
	 * @param differThreshold - differ threshold
	 * @param matchThreshold - match threshold
	 * @param compact - set this to true if you want the CompositeEntity in the
	 * 			TransitivityResult to be compacted before returning.
	 * @return A TransitivityResult object
	 * @throws   RemoteException  If a communication problem occurs.
	 * @throws InvalidProfileException
	 * @throws TransitivityException
	 * @throws InvalidModelException  if the accessProvider does not exist or is not properly configured.
	 */
	public TransitivityResult findClusters(
		Profile profile,
		MatchCandidate[] candidates,
		String modelName,
		float differThreshold,
		float matchThreshold,
		boolean compact) throws
		RemoteException, InvalidProfileException, TransitivityException, InvalidModelException {

		BeanMatchCandidate [] bCandidates = new BeanMatchCandidate [candidates.length];
		for (int i=0; i<candidates.length; i++) {
			bCandidates[i] = (BeanMatchCandidate) candidates[i];
		}

		CEFromMatchCandidatesBuilder ceb = new CEFromMatchCandidatesBuilder
			(profile, bCandidates, modelName, differThreshold, matchThreshold);

		TransitivityResult tr = null;
		if (compact) {
			tr = new TransitivityResult (modelName, differThreshold,
				matchThreshold, new MatchBiconnectedIterator(ceb.getCompositeEntities()));
		} else {
			tr = new TransitivityResult (modelName, differThreshold,
				matchThreshold, ceb.getCompositeEntities());
		}

		return tr;
	}


	/** This takes in a set of Matches and returns an Iterator of CompositeEntity.
	 *
	 * @param s
	 * @return
	 */
	private Iterator getCompositeEntities (Record q, SortedSet s, String modelName,
		float low, float high) throws TransitivityException {

		CEFromMatchesBuilder builder = new CEFromMatchesBuilder (q, s.iterator(),
			modelName, low, high);

		return builder.getCompositeEntities();
	}



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

	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#ejbRemove()
	 */
	public void ejbRemove() throws EJBException, RemoteException {
	}

	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException {
	}

}
