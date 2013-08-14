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
package com.choicemaker.cm.module;

import java.beans.PropertyChangeEvent;


/**
 * Reacts to a change in some property of a IPropertyControl object.
 * <p>For example, a concrete class implementing IPropertyControl might send out
 * notifications when the controller is configured to include or exclude
 * holds from a graph.</p>
 * <p><em>NOTE:</em> IPropertyControl properties should managed separately from
 * the properties of Swing or AWT components. Instances of
 * IPropertyControl should not have to handle PropertyChangeEvents
 * from a JPanel, and a listener of Swing events should not have to handle
 * PropertyChangeEvent from any instance of IPropertyControl.</p>
 * @author rphall
 */
public interface IPropertyListener {
	
	void propertyChanged(PropertyChangeEvent evt);

}

