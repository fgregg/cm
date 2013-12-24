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
package com.choicemaker.cm.io.db.base;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

/**
 *
 * @author    
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 09:05:59 $
 */
public class DataSources {
	private static Map sources = new HashMap();

	public static void addDataSource(String name, DataSource ds) {
		sources.put(name, ds);
	}

	public static DataSource getDataSource(String name) {
		return (DataSource) sources.get(name);
	}

	public static Collection getDataSourceNames() {
		return sources.keySet();
	}
}
