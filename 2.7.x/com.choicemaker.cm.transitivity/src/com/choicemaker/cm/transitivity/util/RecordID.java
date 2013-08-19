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
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
public class RecordID implements Comparable {
	
	public Comparable recordId;
	public char source;
	
	public RecordID (Comparable id, char source) {
		this.recordId = id;
		this.source = source;
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		if (o instanceof RecordID) {
			RecordID id = (RecordID) o;
			if (this.source == id.source) {
				return this.recordId.compareTo(id.recordId);
			} else if (this.source > id.source){
				return 1;
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}

	public boolean equals(Object o) {
		if (o instanceof RecordID) {
			RecordID id = (RecordID) o;
			return this.recordId.equals(id.recordId) && this.source == id.source;		
		} else {
			return false;
		}
	}


	public int hashCode() {
		return recordId.hashCode();
	}
	
	
	public String toString () {
		StringBuffer sb = new StringBuffer ();
		sb.append(this.source);
		sb.append(' ');
		sb.append(recordId.toString());
		return sb.toString();
	}


}
