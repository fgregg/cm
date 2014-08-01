/*
 * Copyright (c) 2001, 2009 ChoiceMaker Technologies, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ChoiceMaker Technologies, Inc. - initial API and implementation
 */
package com.choicemaker.cm.core.datamodel;

import java.util.Set;
import java.util.WeakHashMap;

/**
 * Notifies its listeners when observableData is added and removed from this composite.
 *
 * @author   Arturo Falck
 * @version  $Revision: 1.1 $ $Date: 2010/01/20 15:05:06 $
 */
public abstract class DefaultCompositeObservableData extends DefaultObservableData implements CompositeObservableData {
	
	private WeakHashMap compositeListeners = new WeakHashMap();
	private WeakHashMap observableData = new WeakHashMap();

    /**
     * adds a Listener to Composite related Events
     */
    public void addCompositeObservableDataListener(ObservableDataListener listener) {
        compositeListeners.put(listener, null);
    }

    /**
     * @return list of CompositeObservableDataListeners
     */
    public ObservableDataListener[] getCompositeObservableDataListeners() {
    	Set listeners = compositeListeners.keySet();
    	ObservableDataListener[] returnValue = new ObservableDataListener[listeners.size()];
    	listeners.toArray(returnValue);
    	
        return returnValue;
    }

    /**
     * removes a Listener of Composite related Events
     */
    public void removeCompositeObservableDataListener(ObservableDataListener listener) {
        compositeListeners.remove(listener);
    }
    
    /**
     * Notifies all ObservableDataListener that a new DefaultObservableData item was added to the Composite.
     */	
	public void fireObservableDataAdded(ObservableData newValue){
		observableData.put(newValue, null);
		
		ObservableDataListener[] listeners = getCompositeObservableDataListeners();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] != null){
				listeners[i].observableDataAdded(new ObservableDataEvent(this, newValue));
			}
		}
	}
    	
    /**
     * Notifies all ObservableDataListener that an DefaultObservableData item was removed from the Composite.
     */	
	public void fireObservableDataRemoved(ObservableData oldValue){
		observableData.remove(oldValue);
		
		ObservableDataListener[] listeners = getCompositeObservableDataListeners();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] != null){
				listeners[i].observableDataRemoved(new ObservableDataEvent(this, oldValue));
			}
		}
	}

    /**
     * @return list of DefaultObservableData items
     */
    public ObservableData[] getObservableData() {
    	Set data = observableData.keySet();
    	ObservableData[] returnValue = new ObservableData[data.size()];
    	data.toArray(returnValue);
    	
        return returnValue;
    }

}
