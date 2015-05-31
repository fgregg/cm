package com.choicemaker.cm.io.db.sqlserver.dbom;

import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_CORE_OBJECTGENERATOR;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.util.ObjectMaker;
import com.choicemaker.e2.CMConfigurationElement;
import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.platform.CMPlatformUtils;
import com.choicemaker.e2.utils.ExtensionDeclaration;

class SqlServerUtils {

	public static final int EXPECTED_OBJECTGENERATOR_COUNT = 1;

	private static String[] _expected_model_names = new String[] {
			"Model1", "Model2" };

	private static List<String> _EXPECTED_MODEL_NAMES = Arrays
			.asList(_expected_model_names);

	public static List<String> EXPECTED_MODEL_NAMES = Collections
			.unmodifiableList(_EXPECTED_MODEL_NAMES);

	public static final String SQLSERVER_PLUGIN_ID =
		"com.choicemaker.cm.io.db.sqlserver";

	public static ObjectMaker getObjectMaker(String uid) {
		CMExtension extension =
			CMPlatformUtils.getExtension(CM_CORE_OBJECTGENERATOR, uid);
		CMConfigurationElement[] els = extension.getConfigurationElements();
		List<ObjectMaker> sqlServerMakers = new ArrayList<>();
		for (CMConfigurationElement element : els) {
			try {
				Object o = element.createExecutableExtension("class");
				assertTrue(o instanceof ObjectMaker);
				ObjectMaker maker = (ObjectMaker) o;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		assertTrue(EXPECTED_OBJECTGENERATOR_COUNT == sqlServerMakers.size());
		ObjectMaker retVal = sqlServerMakers.get(0);
		return retVal;
	}

//	public static Set<ImmutableProbabilityModel> getModels() {
//
//	}

	static Set<ExtensionDeclaration> getExpectedExtensions() {
		Set<ExtensionDeclaration> retVal = new HashSet<>();
		retVal.add(new ExtensionDeclaration(uid("SqlDbObjectMakerApp"),
				"com.choicemaker.e2.applications"));
		retVal.add(new ExtensionDeclaration(uid("sqlDbObjectMaker"),
				"com.choicemaker.cm.core.objectGenerator"));
		retVal.add(new ExtensionDeclaration(uid("sqlServerDatabaseAccessor"),
				"com.choicemaker.cm.io.blocking.automated.base.databaseAccessor"));
		retVal.add(new ExtensionDeclaration(
				uid("sqlServerDatabaseAbstraction"),
				"com.choicemaker.cm.io.db.base.databaseAbstraction"));
		retVal.add(new ExtensionDeclaration(uid("sqlServerRsReader"),
				"com.choicemaker.cm.core.rsReader"));
		retVal.add(new ExtensionDeclaration(uid("sqlServerMrpsReader"),
				"com.choicemaker.cm.core.mrpsReader"));
		return retVal;
	}

	/**
	 * Returns a unique identifier given the id specified in the SqlServer
	 * plugin descriptor
	 */
	static String uid(String id) {
		return SQLSERVER_PLUGIN_ID + "." + id;
	}

	private SqlServerUtils() {
	}

}
