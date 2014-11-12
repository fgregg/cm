/*
 * Created on Jan 19, 2004
 *
 */
package com.choicemaker.cm.io.db.sqlserver.gui;

import java.awt.Component;
import java.awt.Cursor;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Vector;

import javax.sql.DataSource;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

// import com.choicemaker.cm.compiler.impl.CompilerFactory;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.io.blocking.automated.base.BlockingAccessor;
import com.choicemaker.cm.io.blocking.automated.base.DatabaseAccessor;
import com.choicemaker.cm.io.blocking.automated.base.db.DatabaseAbstraction;
import com.choicemaker.cm.io.blocking.automated.base.db.DbbCountsCreator;
import com.choicemaker.cm.io.db.base.DataSources;
import com.choicemaker.cm.io.db.base.DbAccessor;
import com.choicemaker.cm.io.db.base.DbReaderSequential;
import com.choicemaker.cm.io.db.base.xmlconf.ConnectionPoolDataSourceXmlConf;
import com.choicemaker.cm.io.db.sqlserver.RecordReader;
import com.choicemaker.cm.io.db.sqlserver.blocking.SqlDatabaseAbstraction;
import com.choicemaker.cm.io.db.sqlserver.blocking.SqlDatabaseAccessor;

/**
 * @author ajwinkel
 *
 */
public class SqlServerUtils {

	private static boolean connectionPoolsInited = false;
// 	private static boolean productionModelsInited = false;

	static void maybeInitConnectionPools() {
		if (!connectionPoolsInited) {
			// init the connections.  Note: this is only for ConnectionPool elements, 
			// and not OraConnectionCache elements...
			// NOTE: if we want to do Oracle here, we should use 
			// the OraConnectionCache.
			ConnectionPoolDataSourceXmlConf.init();
			connectionPoolsInited = true;
		}
	}
	
// 	static void maybeInitProductionModels() {
// 		if (!productionModelsInited) {
// 			try {
// 				CompilerFactory factory = CompilerFactory.getInstance ();
// 				ICompiler compiler = factory.getDefaultCompiler();
// 				ProbabilityModelsXmlConf.loadProductionProbabilityModels(compiler);
// 			} catch (XmlConfException ex) {
// 				ex.printStackTrace();
// 			}
// 			
// 			//try {
// 			//	// populates the "multi" query in each accessProvider's properties object.
// 			//	SqlDbObjectMaker.getAllModels();
// 			//} catch (IOException ex) {
// 			//	ex.printStackTrace();
// 			//}
// 			
// 			productionModelsInited = true;
// 		}
// 	}

	private static ImmutableProbabilityModel lastModel;
	private static DataSource lastDs;
	private static String lastBlockingConfiguration;
	private static String lastDbConfiguration;

	static void maybeUpdateCounts(DataSource ds, IProbabilityModel model) throws SQLException {
		
		if (model == lastModel &&
			ds == lastDs &&
			lastBlockingConfiguration != null &&
			lastBlockingConfiguration.equals(model.properties().get("blockingConfiguration")) &&
			lastDbConfiguration != null && 
			lastDbConfiguration.equals(model.properties().get("dbConfiguration")) &&
			model.properties().get("countSource") != null) {
			
			return;
		}

		DbbCountsCreator cr = null;
		try {
			cr = new DbbCountsCreator(ds.getConnection(), new IProbabilityModel[] {model});
			cr.install();
			// "true" means to do them only if we haven't done them before...
			cr.create(SqlServerUtils.createDatabaseAbstraction(ds), true);
			cr.setCacheCountSources();
			cr.commit();
		} finally {
			if (cr != null) {
				try {
					cr.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
					return;
				}
			}
		}
		
		lastModel = model;
		lastDs = ds;
		lastBlockingConfiguration = (String)model.properties().get("blockingConfiguration");
		lastDbConfiguration = (String)model.properties().get("dbConfiguration");
	}

	/**
	 * NOTE: RecordReader is a SQLServer-specific piece of code.
	 */
	static Record readRecord(IProbabilityModel model, DataSource ds, String id) throws IOException {
		String condition = createCondition(model, id);
		RecordReader reader = new RecordReader(model, ds, condition);
		
		Record ret = null;
		try {
			reader.open();
			ret = reader.getNext();
		} finally {
			try {
				reader.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return ret;
	}

	/**
	 * NOTE: this only works when the id type is integer (as the value is not quoted).
	 */
	private static String createCondition(ImmutableProbabilityModel model, String id) {
		String dbConf = (String) model.properties().get("dbConfiguration");
		DbReaderSequential dbr = ((DbAccessor)model.getAccessor()).getDbReaderSequential(dbConf);
		String masterType = dbr.getMasterIdType().toUpperCase();
		if (masterType.indexOf("INT") >= 0) {
			return "VALUES(" + id + ")";
		} else if (masterType.startsWith("VARCHAR")) {
			return "VALUES('" + id + "')";
		} else {
			throw new IllegalStateException("Unknown master ID type: " + masterType);
		}
	}

	static DatabaseAccessor createDatabaseAccessor(DataSource ds) {
		DatabaseAccessor dba = new SqlDatabaseAccessor();
		dba.setDataSource(ds);
		return dba;
	}
	
	static DatabaseAbstraction createDatabaseAbstraction(DataSource ds) {
		return new SqlDatabaseAbstraction();
	}
	
	//
	// GUI stuff
	//

	static JComboBox createDataSource() {
		JComboBox ds = new JComboBox();
		ds.setEditable(false);
		ds.setModel(new DefaultComboBoxModel(new Vector(DataSources.getDataSourceNames())));
		return ds;
	}

	static final String DEFAULT = "(default)";

	// TODO FIXME change the labeling convention for the default model
	// The blank string convention causes a bunch of annoying issues.
	// Also, even if the convention is maintained in the user interface,
	// it should be removed from the programming interface.
	static JComboBox createProductionConfigurationsComboBox() {
//		IProbabilityModel[] models = PMManager.getModels();
//		Vector v = new Vector(models.length);
//		for (int i=0; i<models.length; i++) {
//			String name = models[i].getModelName();
//			if ("".equals(name)) {
//				v.add(DEFAULT);
//			} else {
//				v.add(name);
//			}
//		}
//		return new JComboBox(v);
		
		return new JComboBox();
	}

	// PROBLEM:
	// This method is very problematic. It **changes** the name
	// of model -- it requires this entire  module to work with mutable
	// models, unlike every other DB GUI plugin -- just to pretty up
	// a user interface.
	//
	// TODO FIXME change the labeling convention for the default model
	// The blank string convention causes a bunch of annoying issues.
	// Also, even if the convention is maintained in the user interface,
	// it should be removed from the programming interface.
//	static IProbabilityModel getProductionConfiguration(String name) {
//		if (DEFAULT.equals(name)) {
//			name = "";
//		}		
//		return PMManager.getModelInstance(name);
//	}

	static String[] getDbConfigurations(ImmutableProbabilityModel model) {
		return ((DbAccessor)model.getAccessor()).getDbConfigurations();
	}

	static JComboBox createDbConfigurationsBox(IProbabilityModel model) {
		return new JComboBox(getDbConfigurations(model));
	}

	static String[] getBlockingConfigurations(ImmutableProbabilityModel model) {
		return ((BlockingAccessor)model.getAccessor()).getBlockingConfigurations();
	}

	static JComboBox createBlockingConfigurationsBox(IProbabilityModel model) {
		return new JComboBox(getBlockingConfigurations(model));
	}

	static String getText(JTextField tf) {
		String s = tf.getText().trim();
		if (s.length() == 0) {
			return null;
		} else {
			return s;
		}			
	}

	static final void setWaitCursor(Component c1, Component c2) {
		setWaitCursor(c1);
		setWaitCursor(c2);
	}

	static final void setWaitCursor(Component c) {
		c.setCursor(new Cursor(Cursor.WAIT_CURSOR));
	}

	static final void setDefaultCursor(Component c1, Component c2) {
		setDefaultCursor(c1);
		setDefaultCursor(c2);		
	}

	static final void setDefaultCursor(Component c) {
		c.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));	
	}
	
}
