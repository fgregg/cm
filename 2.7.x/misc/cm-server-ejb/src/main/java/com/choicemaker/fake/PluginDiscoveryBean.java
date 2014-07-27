/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.choicemaker.fake;

import java.util.LinkedList;
import java.util.List;

//import javax.ejb.Local;
//import javax.ejb.LocalBean;
//import javax.ejb.Stateless;

/**
 * Bean that uses PluginDiscovery
 * rphall
 */
public class PluginDiscoveryBean implements PluginDiscovery {

    public List<String> listPluginIds() {
    	// FIXME hard-coded, fake list
    	List<String> retVal = new LinkedList<>();
    	retVal.add("com.choicemaker.fake.plugin01");
    	return retVal;
    }

}
