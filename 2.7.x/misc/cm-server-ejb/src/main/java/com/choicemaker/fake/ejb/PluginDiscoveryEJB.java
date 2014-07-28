package com.choicemaker.fake.ejb;

import java.util.List;
import javax.ejb.Local;

@Local
public interface PluginDiscoveryEJB {

    List<String> listPluginIds();
    
}