package com.choicemaker.fake;

import java.util.List;

import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class PluginDiscoverySingletonTest {
	
	@Inject
	PluginDiscovery pluginDiscovery;

    @Deployment
    public static JavaArchive createDeployment() {
    	JavaArchive retVal =  ShrinkWrap.create(JavaArchive.class)
            .addClass(PluginDiscoverySingleton.class)
            .addClass(PluginDiscovery.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    	System.out.println(retVal.toString(true));
    	return retVal;
    }

	@Test
	public void testListPluginIds() {
		List<String> pluginIds = pluginDiscovery.listPluginIds();
		Assert.assertTrue(pluginIds != null);
		for (String pluginId : pluginIds) {
			System.out.println(pluginId);
		}
	}

}
