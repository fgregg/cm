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
package com.choicemaker.cm.transitivity.util;

/**
 * This singleton object generates a unique sequence.
 * 
 * @author pcheung
 *
 */
public class UniqueSequence {
	
	private int num = 1000;
	private static UniqueSequence seq;
	
	
	private UniqueSequence () {
	}
	
	public static UniqueSequence getInstance () {
		if (seq == null) seq = new UniqueSequence ();
		return seq;
	}
	
	
	public synchronized int getNext () {
		return ++num;
	}

	public synchronized Integer getNextInteger () {
		return new Integer (++num);
	}

}
