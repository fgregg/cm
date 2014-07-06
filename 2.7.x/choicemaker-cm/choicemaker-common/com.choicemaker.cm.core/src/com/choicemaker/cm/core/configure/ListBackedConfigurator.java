package com.choicemaker.cm.core.configure;

import com.choicemaker.cm.core.PropertyNames;
import com.choicemaker.cm.core.WellKnownPropertyValues;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.util.SystemPropertyUtils;

public class ListBackedConfigurator implements ChoiceMakerConfigurator {

	public static final boolean DEFAULT_RELOAD = false;

	public static final boolean DEFAULT_INIT_GUI = false;

	public static final ChoiceMakerConfigurator instance = new ListBackedConfigurator();

	public ListBackedConfigurator() {
	}

	/**
	 * Looks for the name of a configuration file stored as a System property
	 * under the key {@link PropertyNames#CHOICEMAKER_CONFIGURATION_FILE}. Uses this
	 * value to invoke {@link #init(String, boolean, boolean)} with default
	 * values {@link #DEFAULT_RELOAD} and {@link #DEFAULT_INIT_GUI} for
	 * <code>reload</code> and <code>initGui</code>, respectively. Equivalent to
	 * the following code:
	 *
	 * <pre>
	 * String fn = System.getProperty(CHOICEMAKER_CONFIGURATION_FILE);
	 * boolean reload = DEFAULT_RELOAD;
	 * boolean initGui = DEFAULT_INIT_GUI;
	 * init(fn, reload, initiGui);
	 * </pre>
	 *
	 * @throws IllegalArgumentException
	 *             if no System property has been set for
	 *             {@link PropertyNames#CHOICEMAKER_CONFIGURATION_FILE}
	 */
	public ChoiceMakerConfiguration init() throws XmlConfException {
		String fn = System.getProperty(PropertyNames.CHOICEMAKER_CONFIGURATION_FILE);
		boolean reload = DEFAULT_RELOAD;
		boolean initGui = DEFAULT_INIT_GUI;
		return init(fn, reload, initGui);
	}

	public ChoiceMakerConfiguration init(String fn, boolean reload,
			boolean initGui) throws XmlConfException {
		SystemPropertyUtils.setPropertyIfMissing(
				PropertyNames.INSTALLABLE_GENERATOR_PLUGIN_FACTORY,
				WellKnownPropertyValues.LIST_BACKED_GENERATOR_PLUGIN_FACTORY);
		ChoiceMakerConfiguration retVal = new ListBackedConfiguration(fn);
		return retVal;
	}

	/**
	 * For backward compatibility with ChoiceMaker 2.3. The
	 * <code>log4jConfName</code> parameter is ignored. Equivalent to invoking
	 * <code>init(fn, reload, initGui)</code>.
	 */
	public ChoiceMakerConfiguration init(String fn, String log4jConfName,
			boolean reload, boolean initGui) throws XmlConfException {
		return init(fn, reload, initGui);
	}

}
