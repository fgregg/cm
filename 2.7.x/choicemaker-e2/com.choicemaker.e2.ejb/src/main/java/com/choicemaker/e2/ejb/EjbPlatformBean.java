package com.choicemaker.e2.ejb;

import static com.choicemaker.cm.core.PropertyNames.INSTALLABLE_CHOICEMAKER_CONFIGURATOR;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.choicemaker.cm.core.ModelConfigurationException;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.core.xmlconf.XmlConfigurator;
import com.choicemaker.e2.CMPlatformRunnable;
import com.choicemaker.e2.CMPluginRegistry;
import com.choicemaker.e2.embed.EmbeddedPlatform;
import com.choicemaker.e2.platform.InstallablePlatform;

/**
 * An EJB implementation of CMPlatform that delegates to an Embedded platform.
 * In addition, this extension of CMPlatform initializes the
 * {@link 
 *
 * @author rphall
 *
 */
@Startup
@Singleton
public class EjbPlatformBean implements EjbPlatform {

	private static final Logger logger = Logger.getLogger(EjbPlatformBean.class
			.getName());

	@PostConstruct
	public void initialize() {
		final String METHOD = "EjbPlatformBean.initialize: ";
		EmbeddedPlatform.install();
		String pn = INSTALLABLE_CHOICEMAKER_CONFIGURATOR;
		String pv = XmlConfigurator.class.getName();
		System.setProperty(pn, pv);
		// FIXME Remove dependence on cm.core
		// This initialization belongs in a separate bean, one that implements
		// IProbabilityModelManager as an EJB. This issue is connected with
		// making the DefaultProbabilityModelManager an installable component.
		try {
			int count = PMManager.loadModelPlugins();
			if (count == 0) {
				String msg = METHOD + "No probability models loaded";
				logger.warning(msg);
			}
		} catch (ModelConfigurationException | IOException e) {
			String msg = METHOD + "Unable to load model plugins: " + e.toString();
			logger.severe(msg);
			throw new IllegalStateException(msg);
		}
		// END FIXME
	}

	public CMPluginRegistry getPluginRegistry() {
		return InstallablePlatform.getInstance().getPluginRegistry();
	}

	public CMPlatformRunnable loaderGetRunnable(String applicationName) {
		return InstallablePlatform.getInstance().loaderGetRunnable(
				applicationName);
	}

	public String getPluginDirectory(String id, String version) {
		return InstallablePlatform.getInstance()
				.getPluginDirectory(id, version);
	}

	public URL getPluginDescriptorUrl(String id, String version,
			String descriptorFile) {
		return InstallablePlatform.getInstance().getPluginDescriptorUrl(id,
				version, descriptorFile);
	}

}
