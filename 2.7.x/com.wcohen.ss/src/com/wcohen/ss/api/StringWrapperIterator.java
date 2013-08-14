package com.wcohen.ss.api;

import java.io.Serializable;

/**
 * An iterator over StringWrapper objects.
 */
public interface StringWrapperIterator extends java.util.Iterator, Serializable {
	public boolean hasNext();
	public Object next();
	public StringWrapper nextStringWrapper();
}
