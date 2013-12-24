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
package com.choicemaker.cm.core.util;

/**
 * Implementation of a bounded buffer that we can &quot;close&quot;, that is, 
 * we can tell Threads waiting in or calling get() that there will never be
 * more elements added to the queue (and thus they should wake up and return null
 * or not wait at all, respectively).
 */
public final class BoundedQueue {

	private Object[] buffer;
	private int putIndex;
	private int getIndex;
	private int size;
	
	private boolean closed = false;
	
	public BoundedQueue(int capacity) {
		if (capacity < 1) {
			throw new IllegalArgumentException();
		}
		buffer = new Object[capacity];
	}
	
	public int size() {
		return size;
	}
	
	public synchronized void put(Object obj) {
		// can't put null Objects
		if (obj == null) {
			throw new IllegalArgumentException("Can't put a null into the buffer!");
		}
		
		// check if this buffer has been closed
		if (closed) {
			throw new IllegalStateException("Buffer closed!");
		}

		// wait until there is room to put the object
		while (size == buffer.length) {
			tryWait();
		}

		// check if this buffer has been closed
		if (closed) {
			throw new IllegalStateException("Buffer closed by a different thread!");
		}
		
		// put the object
		buffer[putIndex] = obj;
		putIndex = (putIndex + 1) % buffer.length;
		
		// if this was empty, notify everyone that was
		// waiting on it.
		if (size++ == 0) {
			notifyAll();
		}
	}
	
	public synchronized Object get() {
		// wait until there is an object to get 
		while (!closed && size == 0) {
			tryWait(); 
		}
		
		// if the buffer will never have another element, return null to indicate it
		if (closed && size == 0) {
			return null;
		}
		
		// get an object
		Object obj = buffer[getIndex];
		buffer[getIndex] = null;
		getIndex = (getIndex + 1) % buffer.length;
		
		// if the buffer was full, notify everyone that was 
		// waiting on it.
		if (size-- == buffer.length) {
			notifyAll();
		}
		
		return obj;
	}

	public synchronized void close() {
		closed = true;
		notifyAll();
	}

	private synchronized void tryWait() {
		try {
			wait();
		} catch (InterruptedException ex) {
			// this was caused because another thread called interrupt().
			// ignore it....
		}
	}

}
