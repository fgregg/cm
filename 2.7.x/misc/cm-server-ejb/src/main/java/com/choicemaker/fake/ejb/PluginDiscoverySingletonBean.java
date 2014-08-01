/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.choicemaker.fake.ejb;

import java.net.URL;
import java.util.Collections;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

import com.choicemaker.fake.InstallablePluginDiscovery;
import com.choicemaker.fake.PluginDiscovery;

/**
 * Singleton that implements PluginDiscoveryEJB
 * @author rphall
 */
@Singleton
@Lock(LockType.READ)
public class PluginDiscoverySingletonBean implements PluginDiscoveryEJB {

    private Set<URL> pluginIds;
    
    @Override
    public Set<URL> listPluginIds() {
    	return Collections.unmodifiableSet(pluginIds);
    }

    @PostConstruct
    void initialize() {
    	PluginDiscovery pd = InstallablePluginDiscovery.getInstance();
    	pluginIds = pd.getPluginUrls();
    }

}
