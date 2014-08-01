package com.choicemaker.fake.ejb;

import java.net.URL;
import java.util.Set;

import javax.ejb.Local;

@Local
public interface PluginDiscoveryEJB {

    Set<URL> listPluginIds();
    
}