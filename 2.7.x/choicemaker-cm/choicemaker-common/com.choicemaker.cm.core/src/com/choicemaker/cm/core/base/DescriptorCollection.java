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
package com.choicemaker.cm.core.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1 $ $Date: 2010/01/20 15:05:04 $
 */
public class DescriptorCollection {
	private Descriptor descriptor;
	private Descriptor[] descriptors;
	private Map map;
	
	public DescriptorCollection(Descriptor descriptor) {
		this.descriptor = descriptor;
		List l = new ArrayList();
		map = new HashMap();
		processRecordDescriptor(descriptor, l);
		descriptors = new Descriptor[l.size()];
		l.toArray(descriptors);
	}
	
	private void processRecordDescriptor(Descriptor d, List l) {
		map.put(d.getRecordName(), d);
		l.add(d);
		Descriptor[] children = d.getChildren();
		for (int i = 0; i < children.length; ++i) {
			processRecordDescriptor(children[i], l);
		}
	}
	
	
	/**
	 * Returns the descriptors.
	 * @return Descriptor[]
	 */
	public Descriptor[] getDescriptors() {
		return descriptors;
	}

	/**
	 * Returns the map.
	 * @return Map
	 */
	public Map getMap() {
		return map;
	}

	public Descriptor getDescriptor(String name) {
		return (Descriptor)map.get(name);
	}
	/**
	 * Returns the descriptor.
	 * @return Descriptor
	 */
	public Descriptor getDescriptor() {
		return descriptor;
	}

}
