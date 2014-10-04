package com.choicemaker.e2.ejb;

import java.net.URL;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.choicemaker.e2.CMPlatformRunnable;
import com.choicemaker.e2.CMPluginRegistry;
import com.choicemaker.e2.embed.EmbeddedPlatform;
import com.choicemaker.e2.platform.InstallablePlatform;

/**
 * An EJB implementation of CMPlatform that delegates to an Embedded platform.
 *
 * @author rphall
 *
 */
@Startup
// Probably could be Stateless, rather than a Singleton, assuming all plugins
// are read-only. For now, there's no real performance hit if this remains
// a singleton -- performance bottlenecks lie elsewhere.
@Singleton
public class EjbPlatformBean implements EjbPlatform {

	@PostConstruct
	public void initialize() {
		EmbeddedPlatform.install();
	}

	public CMPluginRegistry getPluginRegistry() {
		return InstallablePlatform.getInstance().getPluginRegistry();
	}

	public CMPlatformRunnable loaderGetRunnable(String applicationName) {
		return InstallablePlatform.getInstance().loaderGetRunnable(applicationName);
	}

	public String getPluginDirectory(String id, String version) {
		return InstallablePlatform.getInstance().getPluginDirectory(id, version);
	}

	public URL getPluginDescriptorUrl(String id, String version,
			String descriptorFile) {
		return InstallablePlatform.getInstance().getPluginDescriptorUrl(id, version,
				descriptorFile);
	}

}
