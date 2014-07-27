package com.choicemaker.fake;

import java.util.List;
import javax.ejb.Local;

@Local
public interface PluginDiscovery {

    List<String> listPluginIds();
    
}