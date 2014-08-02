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
package com.choicemaker.cm.module.properties;

import java.beans.PropertyChangeEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import com.choicemaker.cm.module.IPropertyControl;
import com.choicemaker.cm.module.IPropertyListener;


/**
 * Manages properties and property listeners for some object
 * (typically a module).
 * @author rphall
 */
public class PropertyControlSupport implements IPropertyControl {
	
	private final Object propertyOwner;
	private Properties properties = new Properties();
	private final Observable delegate = new Observable();
	
	/** Implements the Observer method for an instance of PropertyPropertyListener */
	private class PropertyObserver implements Observer {
		private final IPropertyListener propertyListener;
		public PropertyObserver(IPropertyListener propertyListener) {
			this.propertyListener = propertyListener;
			// Fail fast
			if (propertyListener == null) {
				throw new IllegalArgumentException("null listener");
			}
		}
		public void update(Observable src, Object evt) {
			this.propertyListener.propertyChanged((PropertyChangeEvent) evt);
		}
		public boolean equals(Object o) {
			boolean retVal = false;
			if (o instanceof PropertyObserver) {
				retVal = this.propertyListener.equals(((PropertyObserver)o).propertyListener);
			}
			return retVal;
		}
		public int hashCode() {
			return this.propertyListener.hashCode();
		}
	}
	
	public PropertyControlSupport(Object source) {
		this.propertyOwner = source;
		// Fail fast
		if (source == null) {
			throw new IllegalArgumentException("null source");
		}
	}
	
	public Object getPropertyOwner() {
		return this.propertyOwner;
	}

	// -- Property management
	
	public Properties getProperties() {
		return new Properties(this.properties);
	}

	public String getProperty(String name) {
		return this.properties.getProperty(name);
	}

	public String getProperty(String name, String strDefault) {
		return this.properties.getProperty(name, strDefault);
	}

	public void setProperty(String name, String value) {
		String oldValue = this.properties.getProperty(name);
		this.properties.setProperty(name,value);
		PropertyChangeEvent pce = new PropertyChangeEvent(this.propertyOwner,name,oldValue,value);
		this.delegate.notifyObservers(pce);
	}

	public void removeProperty(String name) {
		String oldValue = this.properties.getProperty(name);
		this.properties.remove(name);
		PropertyChangeEvent pce = new PropertyChangeEvent(this.propertyOwner,name,oldValue,null);
		this.delegate.notifyObservers(pce);
	}

	// -- Listener management
	
	public void addPropertyListener(IPropertyListener l) {
		this.delegate.addObserver(new PropertyObserver(l));
	}

	public void removePropertyListener(IPropertyListener l) {
		this.delegate.deleteObserver(new PropertyObserver(l));
	}
	
	/**
	 * @param event must be non-null
	 */
	public void notifyListeners(PropertyChangeEvent event) {
		// Fail fast
		if (event == null) {
			throw new IllegalArgumentException("null event");
		}
		this.delegate.notifyObservers(event);
	}
	
	// -- Property utilties
	
	public static class PU {
		
		private PU() {}
		
		public static void setProperty(IPropertyControl g, String k, String v) {
			g.setProperty(k, v);
		}

		public static void setBooleanProperty(
			IPropertyControl g,
			String k,
			boolean b) {
			g.setProperty(k, "" + b);
		}
	
		/**
		 * @param d the default value
		 */
		public static String getProperty(
			IPropertyControl g,
			String k,
			String d) {
			String retVal = g.getProperty(k);
			if (retVal == null) {
				g.setProperty(k, d);
				retVal = d;
			}
			return retVal;
		}

		/**
		 * @param d the default value
		 */
		public static boolean getBooleanProperty(
			IPropertyControl g,
			String k,
			boolean d) {
			String s = getProperty(g, k, "" + d);
			boolean retVal = Boolean.valueOf(s).booleanValue();
			return retVal;
		}

	}

}

