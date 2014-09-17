package com.choicemaker.eclipse2.ejb;

import java.net.URL;

import javax.annotation.PostConstruct;
import javax.ejb.Startup;
import javax.ejb.Stateless;

import com.choicemaker.eclipse2.core.boot.CMPlatformRunnable;
import com.choicemaker.eclipse2.core.runtime.CMPlatform;
import com.choicemaker.eclipse2.core.runtime.CMPluginRegistry;
import com.choicemaker.eclipse2.core.runtime.InstallablePlatform;
import com.choicemaker.eclipse2.embed.EmbeddedPlatform;

/**
 * A singleton implementation that uses an installable delegate to implement
 * Eclipse2Service methods.
 *
 * @author rphall
 *
 */
@Startup
@Stateless
public class Eclipse2ServiceBean implements Eclipse2Service {

	private CMPlatform embeddedPlatform;

	@PostConstruct
	public void initialize() {
		embeddedPlatform = new EmbeddedPlatform();
		InstallablePlatform.getInstance().install(embeddedPlatform);
	}

	public CMPluginRegistry getPluginRegistry() {
		return embeddedPlatform.getPluginRegistry();
	}

	public CMPlatformRunnable loaderGetRunnable(String applicationName) {
		return embeddedPlatform.loaderGetRunnable(applicationName);
	}

	public String getPluginDirectory(String id, String version) {
		return embeddedPlatform.getPluginDirectory(id, version);
	}

	public URL getPluginDescriptorUrl(String id, String version,
			String descriptorFile) {
		return embeddedPlatform.getPluginDescriptorUrl(id, version,
				descriptorFile);
	}

}
