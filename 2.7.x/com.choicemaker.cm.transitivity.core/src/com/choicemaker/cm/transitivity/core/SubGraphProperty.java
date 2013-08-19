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
package com.choicemaker.cm.transitivity.core;

/**
 * This interface defines a way to check sub graph property.
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
public interface SubGraphProperty {
	
	/** This method returns true if the give graph has this property.
	 * 
	 * @param ce
	 * @return boolean.
	 */
	public boolean hasProperty (CompositeEntity ce);
	
}
