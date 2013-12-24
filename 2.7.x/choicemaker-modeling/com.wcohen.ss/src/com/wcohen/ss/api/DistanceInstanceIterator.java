package com.wcohen.ss.api;

import java.io.Serializable;

/**
 * An iterator over DistanceInstance objects.
 */
public interface DistanceInstanceIterator extends java.util.Iterator, Serializable {
	public boolean hasNext();
	public Object next();
	public DistanceInstance nextDistanceInstance();
}
