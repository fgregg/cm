/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.choicemaker.fake;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

//import javax.ejb.Local;
//import javax.ejb.LocalBean;
//import javax.ejb.Stateless;

/**
 * Singleton that implements PluginDiscovery
 * @author rphall
 */
@Singleton
@Lock(LockType.READ)
public class PluginDiscoverySingleton implements PluginDiscovery {

    private List<String> pluginIds;
    
    @Override
    public List<String> listPluginIds() {
    	return Collections.unmodifiableList(pluginIds);
    }

    @PostConstruct
    void initialize() {
    	pluginIds = new LinkedList<>();
    	// FIXME hard-coded, fake list
    	pluginIds.add("com.choicemaker.fake.plugin01");
    }

}
