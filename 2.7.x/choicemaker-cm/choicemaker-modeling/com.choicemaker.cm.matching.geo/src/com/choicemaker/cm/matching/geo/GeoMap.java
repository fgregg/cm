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
package com.choicemaker.cm.matching.geo;

import java.util.*;
/**
 * A map that defines co-ordinates for geo-entities of a certain type.
 * Each map can be either TreeMap or HashMap. In addition to map itselt the object stores
 * information about key type, key length (-1 if undefined), and key fields.
 * Key fields can be used to build an adequate GUI. If a key is a simple scalar number or string
 * key fields are undefined (null). 
 * 
 * @author emoussikaev
 *
 * @since 2.7
 *  
 */
public class GeoMap {
	
	/**
	 * Supply information about name and lenght of a key filed.
	 * 
	 */
	public class KeyField {
		public String name;
		public int length;
		public KeyField(String name, int length){
			this.name = name;
			this.length = length; 
		}
	}

	Map		map;
	String	type;
	String	keyType;
	int 	keyLen = -1;
	Vector	fields;
	
	
	public GeoMap(String mapType, String keyType, int keyLen) {
		if(mapType.equals("tree")) {
			this.map = new TreeMap();
			this.type = "tree";
		}
		else {
			this.map = new HashMap();
			this.type = "hash";  			
		}
		this.keyType = keyType;
		this.keyLen = keyLen;
	}
	
	public void setFields(Vector fields){
		this.fields = fields;
	}

	public Vector getFields(){
		return this.fields;
	}
	
	public Map getMap(){
		return map;
	}

	public String getKeyType(){
		return this.keyType;
	}

	public int getKeyLegth(){
		return this.keyLen;
	}
}
