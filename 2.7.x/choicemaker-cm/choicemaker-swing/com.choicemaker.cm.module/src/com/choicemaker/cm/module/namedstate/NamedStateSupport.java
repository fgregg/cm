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
package com.choicemaker.cm.module.namedstate;

import java.util.Observable;
import java.util.Observer;

import com.choicemaker.cm.module.IModuleController;
import com.choicemaker.cm.module.INamedEvent;
import com.choicemaker.cm.module.INamedStateControl;
import com.choicemaker.cm.module.INamedStateListener;


/**
 * Support for notifying listeners to state changes of a module
 * @author rphall
 */
public abstract class NamedStateSupport implements INamedStateControl {
	
	// Change the access of various methods so that subclasses use them
	protected static class ObservableState extends Observable {
		public void setChanged(boolean b) {
			if (b) {
				super.setChanged();
			} else {
				super.clearChanged();
			}
		}
	}
	
	private final Object source;
	private final ObservableState delegate = new ObservableState();
	
	/**
	 * Unless the setChanged method is called, listeners won't be contacted
	 * during the notifyListeners method. This flag allows subclasses to optimize
	 * notifications based on whether state has actually changed; i.e. by
	 * comparing the new value for setCurrentState to the existing value
	 * obtained by getCurrentState.
	 */
	protected void setChanged(boolean b) {
		this.delegate.setChanged(b);
	}
	
	/** Implements the Observer method for an instance of INamedStateListener */
	private class NamedStateObserver implements Observer {
		private final INamedStateListener namedStateListener;
		public NamedStateObserver(INamedStateListener namedStateListener) {
			this.namedStateListener = namedStateListener;
			// Fail fast
			if (namedStateListener == null) {
				throw new IllegalArgumentException("null listener");
			}
		}
		public void update(Observable src, Object evt) {
			// assert src instance of IClusterControlSupport.this ;
			this.namedStateListener.stateChanged((INamedEvent) evt);
		}
		public boolean equals(Object o) {
			boolean retVal = false;
			if (o instanceof IModuleController) {
				retVal = this.equals(o);
			}
			return retVal;
		}
		public int hashCode() {
			return this.namedStateListener.hashCode();
		}
	}
	
	public NamedStateSupport(Object source) {
		this.source = source;
		// Fail fast
		if (source == null) {
			throw new IllegalArgumentException("null source");
		}
	}
	
	public void addStateListener(INamedStateListener l) {
		this.delegate.addObserver(new NamedStateObserver(l));
	}

	public void removeStateListener(INamedStateListener l) {
		this.delegate.deleteObserver(new NamedStateObserver(l));
	}
	
	/**
	 * This method won't contact listeners unless the most recent invocation
	 * of setChanged was called with <code>true</code>.
	 * @param event must be non-null
	 */
	public void notifyListeners(INamedEvent event) {
		// Fail fast
		if (event == null) {
			throw new IllegalArgumentException("null event");
		}
		this.delegate.notifyObservers(event);
	}

	/**
	 * Specify whether the this object has changed and
	 * then {@link #notifyListeners(INamedEvent) notify listeners}
	 * @param event must be non-null
	 * @boolean b whether this object has changed
	 */
	public void notifyListeners(INamedEvent event, boolean b) {
		this.setChanged(b);
		this.notifyListeners(event);
	}

	public Object getSource() {
		return source;
	}
	
}

