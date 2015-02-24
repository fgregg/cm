/*
 * @(#)$RCSfile: SqlServerMarkedRecordPairSourceXmlConf.java,v $        $Revision: 1.2.102.1 $ $Date: 2009/11/18 01:00:11 $
 *
 * Copyright (c) 2001 ChoiceMaker Technologies, Inc.
 * 41 East 11th Street, New York, NY 10003
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * ChoiceMaker Technologies Inc. ("Confidential Information").
 */

package com.choicemaker.cm.io.db.sqlserver.xmlconf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.MarkedRecordPairSource;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.xmlconf.MarkedRecordPairSourceXmlConfigurator;
import com.choicemaker.cm.io.db.base.xmlconf.ConnectionPoolDataSourceXmlConf;
import com.choicemaker.cm.io.db.sqlserver.SqlServerMarkedRecordPairSource;
import com.choicemaker.cm.io.db.sqlserver.SqlServerXmlUtils;

/**
 * Handling of Db Marked Record Pair sources.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2.102.1 $ $Date: 2009/11/18 01:00:11 $
 */
public class SqlServerMarkedRecordPairSourceXmlConf implements MarkedRecordPairSourceXmlConfigurator {
	
	public static final String EXTENSION_POINT_ID = "com.choicemaker.cm.io.db.sqlserver.sqlServerMrpsReader";

	public Object getHandler() {
		return this;
	}

	public Class getHandledType() {
		return SqlServerMarkedRecordPairSource.class;
	}

	/**
	 * Add a Db record source to the configuration.
	 *
	 * @param   name  The name of the source.
	 * @param   probabilityModel  The name of the probability accessProvider.
	 * @param   probabilityModelConfig  The name of the configuration containing the probability accessProvider.
	 * @param   selection  The selection.
	 * @param   connectionName  The name of the connection to access this source.
	 * @param   replace  Whether an exiting probability accessProvider of the same name should be replaced.
	 *            If the value of <code>replace</code> is <code>false</code> and a accessProvider of the
	 *            same name already exists, an exception is thrown.
	 * @throws  XmlConfException  if an exception occurs.
	 */
	public void add(MarkedRecordPairSource s) throws XmlConfException {
		try {
			SqlServerMarkedRecordPairSource src = (SqlServerMarkedRecordPairSource) s;
			String fileName = src.getFileName();
			Element e = new Element(SqlServerXmlUtils.EN_MARKEDRECORDPAIRSOURCE);
			e.setAttribute(SqlServerXmlUtils.AN_CLASS, EXTENSION_POINT_ID);
			e.setAttribute(SqlServerXmlUtils.AN_DATASOURCENAME, src.getDataSourceName());
			e.setAttribute(SqlServerXmlUtils.AN_DBCONFIGURATION, src.getDbConfiguration());
			e.addContent(new Element(SqlServerXmlUtils.EN_MRPSQUERY).setText(src.getMrpsQuery()));
			FileOutputStream fs = new FileOutputStream(new File(fileName).getAbsoluteFile());
			XMLOutputter o = new XMLOutputter("    ", true);
			o.setTextNormalize(true);
			o.output(new Document(e), fs);
			fs.close();
		} catch (IOException ex) {
			throw new XmlConfException("Internal error.", ex);
		}
	}

	public MarkedRecordPairSource getMarkedRecordPairSource(String fileName, Element e, ImmutableProbabilityModel model)
		throws XmlConfException {
		String dataSourceName = e.getAttributeValue(SqlServerXmlUtils.AN_DATASOURCENAME);
		String dbConfiguration = e.getAttributeValue(SqlServerXmlUtils.AN_DBCONFIGURATION);
		String mrpsQuery = e.getChildText(SqlServerXmlUtils.EN_MRPSQUERY);
		return new SqlServerMarkedRecordPairSource(fileName, model, dataSourceName, dbConfiguration, mrpsQuery);
	}
	
	static {
		ConnectionPoolDataSourceXmlConf.maybeInit();
	}
	
}
