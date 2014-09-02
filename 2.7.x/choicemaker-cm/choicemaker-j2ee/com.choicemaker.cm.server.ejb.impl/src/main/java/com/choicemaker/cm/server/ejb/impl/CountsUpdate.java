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

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.choicemaker.cm.io.blocking.automated.base.db.DbbCountsCreator;
import com.choicemaker.cm.server.base.DatabaseException;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.2 $ $Date: 2010/03/27 22:03:55 $
 */
public class CountsUpdate {

	private static Logger logger = Logger.getLogger(CountsUpdate.class.getName());

	public void updateCounts(DataSource dataSource, boolean neverComputedOnly) throws DatabaseException, RemoteException {
		Connection connection = null;
		DbbCountsCreator countsCreator = null;
		try {
			connection = dataSource.getConnection();
			countsCreator = new DbbCountsCreator(connection);
			
			countsCreator.install();
			countsCreator.create(neverComputedOnly);
			countsCreator.setCacheCountSources();
			
			countsCreator.commit();
		} catch (Exception ex) {
			logger.error(ex.toString(), ex);
			if (connection != null) {
				try {
					connection.rollback();
				} catch (SQLException e) {
					logger.error(e.toString(), e);
				}
			}
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e1) {
				logger.error(e1.toString(), e1);
			}
		}
	}

	public void cacheCounts(DataSource dataSource) throws DatabaseException, RemoteException {
		Connection connection = null;
		DbbCountsCreator countsCreator = null;
		try {
			connection = dataSource.getConnection();
			countsCreator = new DbbCountsCreator(connection);
			countsCreator.setCacheCountSources();
		} catch (Exception ex) {
			logger.error(ex.toString(), ex);
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e1) {
				logger.error(e1.toString(), e1);
			}
		}
	}
	
}
