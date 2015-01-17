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
package com.choicemaker.cm.io.blocking.automated.offline.data;

/**
 * @author pcheung
 *
 * @deprecated
 */
@Deprecated
public class CacheObject {
	int count;
	Object obj;

	public CacheObject(Object o) {
		count = 1;
		this.obj = o;
	}

	public Object getObject() {
		return obj;
	}

	public int getCount() {
		return count;
	}

	public void addCount() {
		count++;
	}

	public void subtractCount() {
		count--;
	}

}
