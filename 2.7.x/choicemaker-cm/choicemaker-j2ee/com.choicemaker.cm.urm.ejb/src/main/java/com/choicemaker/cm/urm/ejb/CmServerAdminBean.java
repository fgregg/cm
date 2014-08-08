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

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.core.configure.xml.NotFoundException;
import com.choicemaker.cm.core.configure.xml.XmlConfigurablesRegistry;
import com.choicemaker.cm.core.xmlconf.EmbeddedXmlConfigurator;
import com.choicemaker.cm.server.base.DatabaseException;
import com.choicemaker.cm.server.ejb.impl.CountsUpdate;
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

	protected static boolean initialized = false;

	private static final Logger log = Logger.getLogger(CmServerAdminBean.class);
	SessionContext sc = null;

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
		log.debug("starting ejbCreate...");
		try {
			if (!initialized) {
				EmbeddedXmlConfigurator.getInstance().embeddedInit(null);
				initialized = true;
			}
		} catch (Exception ex) {
			log.error(ex.toString(), ex);
			throw new CreateException(ex.toString());
		}
		log.debug("...finished ejbCreate");
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
			log.debug("DataSource URI: " + uri);
			Context ctx = new InitialContext();
			Object o = ctx.lookup(uri);
			retVal = (DataSource) o;
			if (log.isDebugEnabled()) {
				Connection conn = retVal.getConnection();
				String connUrl = conn.getMetaData().getURL();
				log.debug("DB connection URL: " + connUrl);
				conn.close();
			}
		} catch (NamingException x) {
			String msg =
				"Unable to obtain JNDI context or lookup up database URI '"
					+ uri
					+ "': "
					+ x.toString();
			log.error(msg, x);
			throw new RecordCollectionException(msg, x);
		} catch (SQLException x) {
			String msg = "Unable to check JDBC connection of datasource";
			log.warn(msg);
		}
		return retVal;
	}

	/** Returns the updator specified by the model */
	public static IUpdateDerivedFields getUpdator(String modelName)
		throws ModelException, ConfigException {
		IProbabilityModel model = PMManager.getModelInstance(modelName);
		if (model == null) {
			log.error("Invalid probability accessProvider: " + modelName);
			throw new ModelException(modelName);
		}
		String delegateExtension =
			(String) model.properties().get(
				IUpdateDerivedFields.PN_MODEL_CONFIGURATION_UPDATOR_DELEGATE);
		XmlConfigurablesRegistry registry =
			DefaultUpdateDerivedFieldsRegistry.getInstance();
		IUpdateDerivedFields retVal;
		try {
			retVal = (IUpdateDerivedFields) registry.get(delegateExtension);
		} catch (NotFoundException x) {
			String msg = x.getMessage();
			log.error(msg,x);
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
		Precondition.assertNonNullArgument("null model", probabilityModel);
		Precondition.assertNonNullArgument("null record collection", rc);
		try {
			DataSource ds = getDataSource(rc);
			IUpdateDerivedFields updator = getUpdator(probabilityModel);
			updator.updateDirtyDerivedFields(ds);
		} catch (IOException x) {
			String msg = "Unable to access records: " + x.toString();
			log.error(msg, x);
			throw new RecordCollectionException(msg);
		} catch (SQLException x) {
			String msg = "Unable to query records: " + x.toString();
			log.error(msg, x);
			throw new RecordCollectionException(msg);
		}
	}

	public void updateAllDerivedFields(
		String probabilityModel,
		DbRecordCollection rc)
		throws ConfigException, RecordCollectionException, ModelException, RemoteException {
		Precondition.assertNonNullArgument("null model", probabilityModel);
		Precondition.assertNonNullArgument("null record collection", rc);
		try {
			DataSource ds = getDataSource(rc);
			IUpdateDerivedFields updator = getUpdator(probabilityModel);
			updator.updateDirtyDerivedFields(ds);
		} catch (IOException x) {
			String msg = "Unable to access records: " + x.toString();
			log.error(msg, x);
			throw new RecordCollectionException(msg);
		} catch (SQLException x) {
			String msg = "Unable to query records: " + x.toString();
			log.error(msg);
			throw new RecordCollectionException(msg, x);
		}
	}

	public void updateCounts(String probabilityModel, String urlString)
		throws RecordCollectionException, ConfigException, RemoteException {
		DataSource ds = null;
		try {
			log.debug("url" + urlString);
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
			new CountsUpdate().updateCounts(ds, false);
		} catch (NamingException e) {
			log.error(e);
			throw new ConfigException(e.toString());
		} catch (DatabaseException e) {
			log.error(e);
			throw new RecordCollectionException(e.toString());
		}
	}

}
