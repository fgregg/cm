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

import java.util.EventObject;

/**
 * .
 *
 * @author   Arturo Falck
 * @version  $Revision: 1.1 $ $Date: 2010/01/20 15:05:06 $
 */
public class ObservableDataEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	private ObservableData child;
	
	public ObservableDataEvent(CompositeObservableData parent, ObservableData child){
		super(parent);
		this.child = child;
	}
	
	/**
	 * Returns the child added or removed from the Composite.
	 * @return DefaultObservableData
	 */
	public ObservableData getChild() {
		return child;
	}

	/**
	 * Returns the parent Composite gaining or loosing an item.
	 * @return DefaultCompositeObservableData
	 */
	public CompositeObservableData getParent() {
		return (CompositeObservableData)getSource();
	}

}
