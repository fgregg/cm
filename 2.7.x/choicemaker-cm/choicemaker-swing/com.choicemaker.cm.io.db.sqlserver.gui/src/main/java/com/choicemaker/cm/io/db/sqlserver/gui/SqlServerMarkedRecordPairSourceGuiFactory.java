/*
 * @(#)$RCSfile: SqlServerMarkedRecordPairSourceGuiFactory.java,v $        $Revision: 1.2 $ $Date: 2004/12/22 16:05:34 $
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
import com.choicemaker.cm.io.db.sqlserver.SqlServerMarkedRecordPairSource;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.dialogs.SourceGui;
import com.choicemaker.cm.modelmaker.gui.sources.SourceGuiFactory;

/**
 * Description
 *
 * @author   Adam Winkel
 * @version   $Revision: 1.2 $ $Date: 2004/12/22 16:05:34 $
 */
public class SqlServerMarkedRecordPairSourceGuiFactory implements SourceGuiFactory {

	public String getName() {
		return "SQL Server";
	}

	public SourceGui createGui(ModelMaker parent, Source s) {
		return new SqlServerMarkedRecordPairSourceGui(parent, (SqlServerMarkedRecordPairSource)s);
	}

	public SourceGui createGui(ModelMaker parent) {
		return createGui(parent, new SqlServerMarkedRecordPairSource());
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
		return SqlServerMarkedRecordPairSource.class;
	}

	public String toString() {
		return "SQL Server MRPS";
	}
	
	static {
		ConnectionPoolDataSourceXmlConf.maybeInit();
	}

}
