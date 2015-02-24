/*
 * @(#)$RCSfile: SqlDbObjectMaker.java,v $        $Revision: 1.9.100.3 $ $Date: 2010/03/16 13:07:26 $
 *
 * Copyright (c) 2002 ChoiceMaker Technologies, Inc.
 * 41 East 11th Street, New York, NY 10003
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * ChoiceMaker Technologies Inc. ("Confidential Information").
 */

package com.choicemaker.cm.io.db.sqlserver.dbom;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.StringTokenizer;

import com.choicemaker.cm.compiler.impl.CompilerFactory;
import com.choicemaker.cm.core.Accessor;
import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.core.compiler.ICompiler;
import com.choicemaker.cm.core.util.CommandLineArguments;
import com.choicemaker.cm.core.util.ObjectMaker;
import com.choicemaker.cm.core.xmlconf.ProbabilityModelsXmlConf;
import com.choicemaker.cm.core.xmlconf.XmlConfigurator;
import com.choicemaker.cm.io.db.base.DbAccessor;
import com.choicemaker.cm.io.db.base.DbField;
import com.choicemaker.cm.io.db.base.DbReader;
import com.choicemaker.cm.io.db.base.DbReaderSequential;
import com.choicemaker.cm.io.db.base.DbView;
import com.choicemaker.e2.CMPlatformRunnable;

/**
 * Writes a Sql Server script (SqlServer_Custom_Objects.txt) that creates DB
 * objects (views) based on the schemas of models specified in the
 * productionModels section of a CM Analyzer configuration file.
 * @author    
 * @version   $Revision: 1.9.100.3 $ $Date: 2010/03/16 13:07:26 $
 */
public class SqlDbObjectMaker implements CMPlatformRunnable, ObjectMaker {

	public Object run(Object args) throws Exception {
		CommandLineArguments cla = new CommandLineArguments();
		cla.addExtensions();
		cla.addArgument("-output");
		cla.enter((String[])args);
		main(new String[] {cla.getArgument("-conf"), cla.getArgument("-log"), cla.getArgument("-output")});
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		XmlConfigurator.getInstance().init(args[0], args[1], false, false);

		// TODO FIXME replace default compiler with configurable compiler
		CompilerFactory factory = CompilerFactory.getInstance ();
		ICompiler compiler = factory.getDefaultCompiler();

		final boolean fromResource = false;
		ProbabilityModelsXmlConf.loadProductionProbabilityModels(compiler,
				fromResource);
		Writer w = new FileWriter(args[2]);
		processAllModels(w, true);
		w.close();
	}
	
	public void generateObjects(File outDir) throws IOException {
		File outFile = new File(outDir, "SqlServer_Custom_Objects.txt").getAbsoluteFile();
		Writer w = new FileWriter(outFile);
		processAllModels(w, true);
		w.close();
	}
	
	public static String[] getAllModels() throws IOException {
		StringWriter w = new StringWriter();
		processAllModels(w, false);
		StringTokenizer st = new StringTokenizer(w.toString(), Constants.LINE_SEPARATOR);
		String[] res = new String[st.countTokens()];
		for (int i = 0; i < res.length; i++) {
			res[i] = st.nextToken();
		}
		return res;
	}

	public static void processAllModels(Writer w, boolean insertGo) throws IOException {
		ImmutableProbabilityModel[] models = PMManager.getModels();
		for (int i=0; i<models.length; i++) {
			createObjects(w, models[i], insertGo);
		}
	}
	
	public static void createObjects(Writer w, ImmutableProbabilityModel model, boolean insertGo) throws IOException {
		String dbConfiguration = model.getDatabaseConfigurationName();
		if (dbConfiguration != null) {
			Accessor accessor = model.getAccessor();
			if (accessor instanceof DbAccessor) {
				String viewBase = "vw_cmt_" + accessor.getSchemaName() + "_r_" + dbConfiguration;
				String dbConf = accessor.getSchemaName() + ":r:" + dbConfiguration;
				// 		w.write("DELETE FROM TB_CMT_CURSORS WHERE config = '" +
				// 			dbConf + "'" + Constants.LINE_SEPARATOR);
				// 		w.write("Go" + Constants.LINE_SEPARATOR);
				DbReaderSequential dbr = ((DbAccessor) accessor).getDbReaderSequential(dbConfiguration);
				DbView[] views = dbr.getViews();
				String masterId = dbr.getMasterId();
				StringBuffer multi = new StringBuffer(4000);
				for (int i = 0; i < views.length; ++i) {
					String viewName = viewBase + i;
					DbView v = views[i];
					boolean first = i == 0 || v.number != views[i - 1].number;
					boolean more = i + 1 < views.length && v.number == views[i + 1].number;
					if (first) {
						if (i != 0) {
							multi.append(Constants.LINE_SEPARATOR);
						}
						if (more) {
							multi.append("SELECT * FROM (");
						}
					} else {
						multi.append(" UNION ");
					}
					multi.append("SELECT * FROM " + viewName + " WHERE " + masterId + " IN " + "(SELECT ID FROM @ids)");
					if (!more) {
						if (!first) {
							multi.append(")");
						}
						if (v.orderBy.length > 0) {
							multi.append(" ORDER BY ");
							multi.append(getOrderBy(v));
						}
					}
					w.write(
						"IF EXISTS (SELECT TABLE_NAME FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_NAME = '"
							+ viewName
							+ "') DROP VIEW "
							+ viewName
							+ Constants.LINE_SEPARATOR + (insertGo ? "Go" + Constants.LINE_SEPARATOR : ""));
					w.write("CREATE VIEW dbo." + viewName + " AS SELECT ");
					for (int j = 0; j < v.fields.length; ++j) {
						DbField f = v.fields[j];
						if (j != 0)
							w.write(",");
						if (f.table != null)
							w.write(f.table + ".");
						w.write(f.name);
					}
					w.write(" FROM " + v.from);
					if (v.where != null)
						w.write(" WHERE " + v.where);
					w.write(Constants.LINE_SEPARATOR + (insertGo ? "Go" + Constants.LINE_SEPARATOR : ""));
				}
				String multiStr = multi.toString();
				//w.write("INSERT INTO TB_CMT_CURSORS VALUES('" + dbConf + "','" + multiStr + "');" + Constants.LINE_SEPARATOR);
				model.properties().put(dbConf + ":SQLServer", multiStr);
			}
		}
	}

	public static String getMultiKey(ImmutableProbabilityModel model, String dbConfiguration) {
		return model.getAccessor().getSchemaName() + ":r:" + dbConfiguration + ":SQLServer";
	}
	
	public static String getMultiKey(DbReader dbReader) {
		return dbReader.getName() + ":SQLServer";
	}

	public static String getMultiQuery(ImmutableProbabilityModel model) {
		return getMultiQuery(model, model.getDatabaseConfigurationName());
	}
	
	public static String getMultiQuery(ImmutableProbabilityModel model, String dbConfiguration) {
		Accessor accessor = model.getAccessor();
		DbReaderSequential dbr = ((DbAccessor)accessor).getDbReaderSequential(dbConfiguration);
		String viewBase = "vw_cmt_" + accessor.getSchemaName() + "_r_" + dbConfiguration;
		DbView[] views = dbr.getViews();
		String masterId = dbr.getMasterId();
		StringBuffer multi = new StringBuffer(4000);
		for (int i = 0; i < views.length; ++i) {
			String viewName = viewBase + i;
			DbView v = views[i];
			boolean first = i == 0 || v.number != views[i - 1].number;
			boolean more = i + 1 < views.length && v.number == views[i + 1].number;
			if (first) {
				if (i != 0) {
					multi.append(Constants.LINE_SEPARATOR);
				}
				if (more) {
					multi.append("SELECT * FROM (");
				}
			} else {
				multi.append(" UNION ");
			}
			multi.append("SELECT * FROM " + viewName + " WHERE " + masterId + " IN (SELECT ID FROM @ids)");
			if (!more) {
				if (!first) {
					multi.append(")");
				}
				if (v.orderBy.length > 0) {
					multi.append(" ORDER BY ");
					multi.append(getOrderBy(v));
				}
			}
		}
		return multi.toString();
	}
	
	private static String getOrderBy(DbView v) {
		StringBuffer ob = new StringBuffer();
		for (int j = 0; j < v.orderBy.length; ++j) {
			if (j != 0)
				ob.append(",");
			ob.append(v.orderBy[j].name);
		}
		return ob.toString();
	}
}
