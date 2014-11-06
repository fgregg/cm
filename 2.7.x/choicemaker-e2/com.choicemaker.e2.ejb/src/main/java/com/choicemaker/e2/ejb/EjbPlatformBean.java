package com.choicemaker.e2.ejb;

import static com.choicemaker.cm.core.ChoiceMakerExtensionPoint.CM_CORE_MODELCONFIGURATION;
import static com.choicemaker.cm.core.PropertyNames.INSTALLABLE_CHOICEMAKER_CONFIGURATOR;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.ModelConfigurationException;
import com.choicemaker.cm.core.base.DefaultProbabilityModelManager;
import com.choicemaker.cm.core.base.MutableProbabilityModel;
import com.choicemaker.cm.core.compiler.DoNothingCompiler;
import com.choicemaker.cm.core.compiler.ICompiler;
import com.choicemaker.cm.core.xmlconf.ProbabilityModelsXmlConf;
import com.choicemaker.cm.core.xmlconf.XmlConfigurator;
import com.choicemaker.e2.CMConfigurationElement;
import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.CMPlatformRunnable;
import com.choicemaker.e2.CMPluginRegistry;
import com.choicemaker.e2.embed.EmbeddedPlatform;
import com.choicemaker.e2.platform.CMPlatformUtils;
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
		EmbeddedPlatform.install();
		String pn = INSTALLABLE_CHOICEMAKER_CONFIGURATOR;
		String pv = XmlConfigurator.class.getName();
		System.setProperty(pn, pv);
		loadModelPlugins();
	}

	@SuppressWarnings("unchecked")
	protected void loadModelPlugins() {
		CMExtension[] extensions =
			CMPlatformUtils.getExtensions(CM_CORE_MODELCONFIGURATION);
		for (CMExtension ext : extensions) {
			URL pUrl = ext.getDeclaringPluginDescriptor().getInstallURL();
			CMConfigurationElement[] els = ext.getConfigurationElements();
			for (CMConfigurationElement el : els) {
				final String KEY_FILE = "model";
				final String KEY_DATABASE_CONFIG =
					ImmutableProbabilityModel.PN_DATABASE_CONFIGURATION;
				final String KEY_BLOCKING_CONFIG =
					ImmutableProbabilityModel.PN_BLOCKING_CONFIGURATION;
				String file = el.getAttribute(KEY_FILE);
				String databaseConfig = el.getAttribute(KEY_DATABASE_CONFIG);
				String blockingConfig = el.getAttribute(KEY_BLOCKING_CONFIG);
				try {
					final String fileName = new File(file).getName();
					final URL rUrl = new URL(pUrl, file);
					final InputStream is = rUrl.openStream();
					final ICompiler compiler = new DoNothingCompiler();
					final StringWriter compilerMessages = new StringWriter();
					final ClassLoader cl =
						ext.getDeclaringPluginDescriptor()
								.getPluginClassLoader();
					final boolean allowCompile = false;
					IProbabilityModel model =
						ProbabilityModelsXmlConf.readModel(fileName, is,
								compiler, compilerMessages, cl, allowCompile);
					model.properties().put(KEY_DATABASE_CONFIG, databaseConfig);
					model.properties().put(KEY_BLOCKING_CONFIG, blockingConfig);
					// HACK
					assert model instanceof MutableProbabilityModel;
					((MutableProbabilityModel)model).setModelName(ext.getUniqueIdentifier());
					// END HACK
					DefaultProbabilityModelManager.getInstance()
							.addModel(model);
				} catch (ModelConfigurationException | IOException ex) {
					logger.severe(ex.toString());
				}

			}
		}
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
