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
package com.choicemaker.cm.module.namedevent;

import java.util.Observable;
import java.util.Observer;

import com.choicemaker.cm.module.IModule;
import com.choicemaker.cm.module.INamedEvent;
import com.choicemaker.cm.module.INamedEventControl;
import com.choicemaker.cm.module.INamedEventListener;


/**
 * Support for notifying listeners to state changes of a module
 * @author rphall
 */
public class NamedEventSupport implements INamedEventControl {
	
	private final IModule source;
	private final Observable delegate = new Observable();
	
	/** Implements the Observer method for an instance of INamedEventListener */
	private class NamedEventObserver implements Observer {
		private final INamedEventListener namedEventListener;
		public NamedEventObserver(INamedEventListener namedEventListener) {
			this.namedEventListener = namedEventListener;
			// Fail fast
			if (namedEventListener == null) {
				throw new IllegalArgumentException("null listener");
			}
		}
		public void update(Observable src, Object evt) {
			// assert src instance of IClusterControlSupport.this ;
			this.namedEventListener.eventOccurred((INamedEvent) evt);
		}
		public boolean equals(Object o) {
			boolean retVal = false;
			if (o instanceof IModule) {
				retVal = this.equals((IModule)o);
			}
			return retVal;
		}
		public int hashCode() {
			return this.namedEventListener.hashCode();
		}
	}
	
	public NamedEventSupport(IModule source) {
		this.source = source;
		// Fail fast
		if (source == null) {
			throw new IllegalArgumentException("null source");
		}
	}
	
	public void addEventListener(INamedEventListener l) {
		this.delegate.addObserver(new NamedEventObserver(l));
	}

	public void removeEventListener(INamedEventListener l) {
		this.delegate.deleteObserver(new NamedEventObserver(l));
	}
	
	/**
	 * @param event must be non-null
	 */
	public void notifyListeners(INamedEvent event) {
		// Fail fast
		if (event == null) {
			throw new IllegalArgumentException("null event");
		}
		this.delegate.notifyObservers(event);
	}
	
}

