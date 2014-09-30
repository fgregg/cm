package com.choicemaker.e2.ejb;

import java.net.URL;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.choicemaker.e2.CMPlatform;
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
// Could be Stateless, rather than a Singleton? Is all instance data is
// read-only, even for the EmbeddedPlatform? Is the CMPlatform and its
// related interfaces read-only?
@Singleton
public class EjbPlatformBean implements EjbPlatform {

	private CMPlatform embeddedPlatform;

	@PostConstruct
	public void initialize() {
		embeddedPlatform = new EmbeddedPlatform();
		
		// Configure the InstallablePlatform for consistency
		String pn = InstallablePlatform.INSTALLABLE_PLATFORM;
		String pv = EmbeddedPlatform.class.getName();
		System.setProperty(pn, pv);
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
