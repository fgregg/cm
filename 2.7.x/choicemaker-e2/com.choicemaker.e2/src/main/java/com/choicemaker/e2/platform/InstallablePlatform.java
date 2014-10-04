package com.choicemaker.e2.platform;

import java.net.URL;
import java.util.logging.Logger;

import com.choicemaker.e2.CMPlatform;
import com.choicemaker.e2.CMPlatformRunnable;
import com.choicemaker.e2.CMPluginRegistry;

/**
 * A singleton implementation that uses an installable delegate to implement
 * CMPlatform methods. In general, a delegate should be installed only once in
 * an application context, and this class encourages this practice by using a
 * {@link #INSTALLABLE_PLATFORM System property} to specify the delegate type.
 * If the property is not set, a {@link #getDefaultInstance() default platform}
 * is used. Currently, the default platform is the {@link #DoNothingPlatform},
 * which is unlikely to be at all useful.
 *
 * @see com.choicemaker.e2.embed.EmbeddedPlatform
 * @see com.choicemaker.e2.standard.StandardPlatform
 * 
 * @author rphall
 *
 */
public final class InstallablePlatform implements CMPlatform {

	private static final Logger logger = Logger
			.getLogger(InstallablePlatform.class.getName());

	/** Property name */
	public static final String INSTALLABLE_PLATFORM = "cmInstallablePlatform";

	/**
	 * The default instance is a {@link EmbeddedPlatform basic implementation}.
	 */
	static final CMPlatform getDefaultInstance() {
		return new DoNothingPlatform();
	}

	/** The singleton instance of this platform */
	private static InstallablePlatform singleton = new InstallablePlatform();

	/** A method that returns the platform singleton */
	public static InstallablePlatform getInstance() {
		assert singleton != null;
		return singleton;
	}

	/**
	 * The delegate used by the platform singleton to implement the Platform
	 * interface.
	 */
	private CMPlatform delegate;

	/**
	 * If a delegate hasn't been set, this method looks up a System property to
	 * determine which type of platform to set and then sets it. If the property
	 * exists but the specified platform type can not be set, throws an
	 * IllegalStateException. If the property doesn't exist, sets the
	 * {@link #getDefaultInstance() default type}. If the default type can not
	 * be set -- for example, if the default type is misconfigured -- throws a
	 * IllegalStateException.
	 *
	 * @throws IllegalStateException
	 *             if a delegate does not exist and can not be set.
	 */
	CMPlatform getDelegate() {
		if (delegate == null) {
			String msgPrefix = "Installing platform: ";
			String fqcn = System.getProperty(INSTALLABLE_PLATFORM);
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
				logger.severe(msg);
				assert delegate == null;
				throw new IllegalStateException(msg);
			}
		}
		assert delegate != null;
		return delegate;
	}

	private void setDelegate(CMPlatform newDelegate) {
		if (this.delegate != null) {
			String msg =
				"Replacing an installed delegate (" + this.delegate
						+ ") with a new delegate (" + newDelegate + ")";
			logger.warning(msg);
		}
		this.delegate = newDelegate;
	}

	/** For testing only; otherwise treat as private */
	InstallablePlatform() {
	}

	/**
	 * Sets the platform delegate explicitly.
	 *
	 * @throws IllegalArgumentException
	 *             if the delegate can not be updated.
	 * */
	public void install(CMPlatform newDelegate) {
		if (newDelegate == null) {
			throw new IllegalArgumentException("null delegate");
		}
		this.setDelegate(newDelegate);
	}

	/**
	 * An alternative method for setting a platform delegate using a FQCN class
	 * name.
	 *
	 * @throws IllegalArgumentException
	 *             if the delegate can not be updated.
	 */
	public void install(String fqcn) {
		if (fqcn == null || fqcn.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"null or blank class name for platform");
		}
		final String msgPrefix = "Installing platform: ";
		try {
			Class<?> c = Class.forName(fqcn);
			CMPlatform instance = (CMPlatform) c.newInstance();
			install(instance);
		} catch (Exception e) {
			String msg = msgPrefix + e.toString() + ": " + e.getCause();
			logger.severe(msg);
			throw new IllegalArgumentException(msg);
		}
	}

	public CMPluginRegistry getPluginRegistry() {
		return getDelegate().getPluginRegistry();
	}

	public CMPlatformRunnable loaderGetRunnable(String applicationName) {
		return getDelegate().loaderGetRunnable(applicationName);
	}

	public String getPluginDirectory(String id, String version) {
		return getDelegate().getPluginDirectory(id, version);
	}

	public URL getPluginDescriptorUrl(String id, String version,
			String descriptorFile) {
		return getDelegate()
				.getPluginDescriptorUrl(id, version, descriptorFile);
	}

}
