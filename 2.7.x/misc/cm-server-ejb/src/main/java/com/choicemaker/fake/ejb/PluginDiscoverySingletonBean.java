/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.choicemaker.fake.ejb;

import java.util.Collections;
import java.util.List;

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

    private List<String> pluginIds;
    
    @Override
    public List<String> listPluginIds() {
    	return Collections.unmodifiableList(pluginIds);
    }

    @PostConstruct
    void initialize() {
    	PluginDiscovery pd = InstallablePluginDiscovery.getInstance();
    	pluginIds = pd.listPluginIds();
    }

}
