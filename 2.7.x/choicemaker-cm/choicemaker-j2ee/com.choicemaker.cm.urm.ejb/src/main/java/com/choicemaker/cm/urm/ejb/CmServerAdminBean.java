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
import java.sql.Connection;
import java.sql.SQLException;
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

import com.choicemaker.cm.core.DatabaseException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.core.configure.xml.NotFoundException;
import com.choicemaker.cm.core.configure.xml.XmlConfigurablesRegistry;
import com.choicemaker.cm.io.blocking.automated.base.db.DbbCountsCreator;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.AbaStatisticsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.AggregateDatabaseAbstractionManager;
import com.choicemaker.cm.io.db.base.DatabaseAbstraction;
import com.choicemaker.cm.io.db.base.DatabaseAbstractionManager;
import com.choicemaker.cm.urm.IUpdateDerivedFields;
import com.choicemaker.cm.urm.base.DbRecordCollection;
import com.choicemaker.cm.urm.exceptions.ConfigException;
import com.choicemaker.cm.urm.exceptions.ModelException;
import com.choicemaker.cm.urm.exceptions.RecordCollectionException;
import com.choicemaker.util.Precondition;

/**
 * @author emoussikaev
 * @version Revision: 2.5  Date: Jul 7, 2005 10:35:24 AM
 * @see
 */
public class CmServerAdminBean implements SessionBean {
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(CmServerAdminBean.class.getName());
	SessionContext sc = null;

	// @EJB
	AbaStatisticsController statsController;

	public CmServerAdminBean() {
		super();
	}

	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException {
	}

	/**
	 * Instantiates and configures the EJB.
	 */
	public void ejbCreate() throws CreateException, RemoteException {
	} // ejbCreate()

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

	/** Returns the DataSource specified by the database record collection */
	public static DataSource getDataSource(DbRecordCollection rc)
		throws RecordCollectionException {
		String uri = null;
		DataSource retVal = null;
		try {
			uri = rc.getUrl();
			log.fine("DataSource URI: " + uri);
			Context ctx = new InitialContext();
			Object o = ctx.lookup(uri);
			retVal = (DataSource) o;
			if (log.isLoggable(Level.FINE)) {
				Connection conn = retVal.getConnection();
				String connUrl = conn.getMetaData().getURL();
				log.fine("DB connection URL: " + connUrl);
				conn.close();
			}
		} catch (NamingException x) {
			String msg =
				"Unable to obtain JNDI context or lookup up database URI '"
					+ uri
					+ "': "
					+ x.toString();
			log.severe(msg);
			throw new RecordCollectionException(msg, x);
		} catch (SQLException x) {
			String msg = "Unable to check JDBC connection of datasource";
			log.warning(msg);
		}
		return retVal;
	}

	/** Returns the updator specified by the modelId */
	public static IUpdateDerivedFields getUpdator(String modelName)
		throws ModelException, ConfigException {
		ImmutableProbabilityModel model = PMManager.getModelInstance(modelName);
		if (model == null) {
			log.severe("Invalid probability accessProvider: " + modelName);
			throw new ModelException(modelName);
		}
		// HACK compilation hack until this class is eliminated
		String delegateExtension =
		// (String) model.properties().get(
		// IUpdateDerivedFields.PN_MODEL_CONFIGURATION_UPDATOR_DELEGATE);
			null;
		// END compilation hack
		XmlConfigurablesRegistry registry =
			DefaultUpdateDerivedFieldsRegistry.getInstance();
		IUpdateDerivedFields retVal;
		try {
			retVal = (IUpdateDerivedFields) registry.get(delegateExtension);
		} catch (NotFoundException x) {
			String msg = x.getMessage();
			log.severe(msg);
			throw new ConfigException(msg);
		}
		return retVal;
	}

	public String getVersion(Object context) throws RemoteException {
		return Single.getInst().getVersion();
	}

	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext ses)
		throws EJBException, RemoteException {
		this.sc = ses;

	}

	public void updateDerivedFields(
		String probabilityModel,
		DbRecordCollection rc)
		throws ConfigException, RecordCollectionException, ModelException, RemoteException {
		Precondition.assertNonNullArgument("null modelId", probabilityModel);
		Precondition.assertNonNullArgument("null record collection", rc);
		try {
			DataSource ds = getDataSource(rc);
			IUpdateDerivedFields updator = getUpdator(probabilityModel);
			updator.updateDirtyDerivedFields(ds);
		} catch (IOException x) {
			String msg = "Unable to access records: " + x.toString();
			log.severe(msg);
			throw new RecordCollectionException(msg);
		} catch (SQLException x) {
			String msg = "Unable to query records: " + x.toString();
			log.severe(msg);
			throw new RecordCollectionException(msg);
		}
	}

	public void updateAllDerivedFields(
		String probabilityModel,
		DbRecordCollection rc)
		throws ConfigException, RecordCollectionException, ModelException, RemoteException {
		Precondition.assertNonNullArgument("null modelId", probabilityModel);
		Precondition.assertNonNullArgument("null record collection", rc);
		try {
			DataSource ds = getDataSource(rc);
			IUpdateDerivedFields updator = getUpdator(probabilityModel);
			updator.updateDirtyDerivedFields(ds);
		} catch (IOException x) {
			String msg = "Unable to access records: " + x.toString();
			log.severe(msg);
			throw new RecordCollectionException(msg);
		} catch (SQLException x) {
			String msg = "Unable to query records: " + x.toString();
			log.severe(msg);
			throw new RecordCollectionException(msg, x);
		}
	}

	public void updateCounts(String probabilityModel, String urlString)
		throws RecordCollectionException, ConfigException, RemoteException, DatabaseException {
		DataSource ds = null;
		try {
			log.fine("url" + urlString);
			if (urlString == null || urlString.length() == 0)
				throw new RecordCollectionException("empty url");
			Context ctx = new InitialContext();
			Object o = ctx.lookup(urlString);
			ds = (DataSource) o;
			// <BUGFIX>
			// 2008-12-04 rphall
			// This public method should force updates on all
			// counts, since that's what a user probably intends.
			// See the old com.choicemaker.cm.server.ejb.impl.AdminServiceBean
			// which forces an update on all counts in the updateCounts method.
			// NOTE: This fix has not been implemented yet, even though it is simple.
			//
			//new CountsUpdate().updateCounts(ds, true);
			// </BUGFIX>
			DatabaseAbstractionManager mgr = new AggregateDatabaseAbstractionManager();
			DatabaseAbstraction dba = mgr.lookupDatabaseAbstraction(ds);
			DbbCountsCreator countsCreator = new DbbCountsCreator();
			countsCreator.install(ds);
			final boolean neverComputeOnly = false;
			final boolean commitChanges = false;
			countsCreator.create(ds, dba, neverComputeOnly, commitChanges);
			countsCreator.setCacheCountSources(ds, dba, statsController);
		} catch (NamingException e) {
			log.severe(e.toString());
			throw new ConfigException(e.toString());
		} catch (SQLException e) {
			log.severe(e.toString());
			throw new RecordCollectionException(e.toString());
		}
	}

}
