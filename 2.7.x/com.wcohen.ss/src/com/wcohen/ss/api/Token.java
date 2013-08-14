package com.wcohen.ss.api;

import java.io.Serializable;

/**
 * An interned version of a string.    
 *
 */
public interface Token extends Serializable {
	public String getValue();
	public int getIndex();
}
