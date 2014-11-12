/*
 * @(#)$RCSfile: SqlServerRecordSourceGuiFactory.java,v $        $Revision: 1.1.96.1 $ $Date: 2009/11/18 01:00:11 $
 *
 * Copyright (c) 2001 ChoiceMaker Technologies, Inc.
 * 41 East 11th Street, New York, NY 10003
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * ChoiceMaker Technologies Inc. ("Confidential Information").
 */

package com.choicemaker.cm.io.db.sqlserver.gui;

import com.choicemaker.cm.core.Source;
import com.choicemaker.cm.io.db.base.xmlconf.ConnectionPoolDataSourceXmlConf;
import com.choicemaker.cm.io.db.sqlserver.SqlServerRecordSource;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.dialogs.SourceGui;
import com.choicemaker.cm.modelmaker.gui.sources.SourceGuiFactory;

/**
 * Description
 *
 * @author   Adam Winkel
 * @version   $Revision: 1.1.96.1 $ $Date: 2009/11/18 01:00:11 $
 */
public class SqlServerRecordSourceGuiFactory implements SourceGuiFactory {

	public String getName() {
		return "SQL Server";
	}

	public SourceGui createGui(ModelMaker parent, Source s) {
		return new SqlServerRecordSourceGui(parent, (SqlServerRecordSource)s);
	}

	public SourceGui createGui(ModelMaker parent) {
		return createGui(parent, new SqlServerRecordSource());
	}

	public boolean hasSink() {
		return false;
	}

	public SourceGui createSaveGui(ModelMaker parent) {
		throw new UnsupportedOperationException();
	}

	public Object getHandler() {
		return this;
	}

	public Class getHandledType() {
		return SqlServerRecordSource.class;
	}

	public String toString() {
		return "SQL Server RS";
	}
	
	static {
		ConnectionPoolDataSourceXmlConf.maybeInit();
	}

}
