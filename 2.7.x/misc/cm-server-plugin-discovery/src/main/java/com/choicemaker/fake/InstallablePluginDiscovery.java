package com.choicemaker.fake;

import java.util.List;

import org.apache.log4j.Logger;

/**
 * A singleton implementation that uses an installable delegate to implement
 * PluginDiscovery methods. In general, a delegate should be installed only once
 * in an application context, and this class encourages this restriction by
 * using a {@link #INSTALLABLE_PLUGIN_DISCOVERY System property} to specify the
 * delegate type. If the property is not set, a {@link #getDefaultInstance()
 * default plugin-discovery} is used.
 *
 * @author rphall
 *
 */
public final class InstallablePluginDiscovery implements PluginDiscovery {

	private static final Logger logger = Logger
			.getLogger(InstallablePluginDiscovery.class.getName());

	/** Property name */
	public static final String INSTALLABLE_PLUGIN_DISCOVERY =
		"cmInstallablePluginDiscovery";

	/**
	 * The default instance is a {@link EmbeddedPluginDiscovery basic
	 * implementation}.
	 */
	static final PluginDiscovery getDefaultInstance() {
		return new EmbeddedPluginDiscovery();
	}

	/** The singleton instance of this plugin-discovery */
	private static InstallablePluginDiscovery singleton =
		new InstallablePluginDiscovery();

	/** A method that returns the plugin-discovery singleton */
	public static InstallablePluginDiscovery getInstance() {
		assert singleton != null;
		return singleton;
	}

	/**
	 * The delegate used by the plugin-discovery singleton to implement the
	 * PluginDiscovery interface.
	 */
	private PluginDiscovery delegate;

	/**
	 * If a delegate hasn't been set, this method looks up a System property to
	 * determine which type of plugin-discovery to set and then sets it. If the
	 * property exists but the specified plugin-discovery type can not be set,
	 * throws an IllegalStateException. If the property doesn't exist, sets the
	 * {@link #getDefaultInstance() default type}. If the default type can not
	 * be set -- for example, if the default type is misconfigured -- throws a
	 * IllegalStateException.
	 *
	 * @throws IllegalStateException
	 *             if a delegate does not exist and can not be set.
	 */
	PluginDiscovery getDelegate() {
		if (delegate == null) {
			String msgPrefix = "Installing plugin discovery: ";
			String fqcn = System.getProperty(INSTALLABLE_PLUGIN_DISCOVERY);
			try {
				if (fqcn != null) {
					logger.info(msgPrefix + fqcn);
					install(fqcn);
				} else {
					logger.info(msgPrefix
							+ getDefaultInstance().getClass().getName());
					install(getDefaultInstance());
				}
			} catch (Exception x) {
				String msg = msgPrefix + x.toString() + ": " + x.getCause();
				logger.error(msg, x);
				assert delegate == null;
				throw new IllegalStateException(msg);
			}
		}
		assert delegate != null;
		return delegate;
	}

	@Override
	public List<String> listPluginIds() {
		return getDelegate().listPluginIds();
	}

	/** For testing only; otherwise treat as private */
	InstallablePluginDiscovery() {
	}

	/**
	 * Sets the plugin-discovery delegate explicitly.
	 *
	 * @throws IllegalArgumentException
	 *             if the delegate can not be updated.
	 * */
	public void install(PluginDiscovery delegate) {
		if (delegate == null) {
			throw new IllegalArgumentException("null delegate");
		}
		this.delegate = delegate;
	}

	/**
	 * An alternative method for setting a plugin-discovery delegate using a
	 * FQCN plugin-discovery name.
	 *
	 * @throws IllegalArgumentException
	 *             if the delegate can not be updated.
	 */
	public void install(String fqcn) {
		if (fqcn == null || fqcn.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"null or blank class name for plugin discovery");
		}
		final String msgPrefix = "Installing plugin discovery: ";
		try {
			Class<?> c = Class.forName(fqcn);
			PluginDiscovery instance = (PluginDiscovery) c.newInstance();
			install(instance);
		} catch (Exception e) {
			String msg = msgPrefix + e.toString() + ": " + e.getCause();
			logger.error(msg, e);
			throw new IllegalArgumentException(msg);
		}
	}

}
