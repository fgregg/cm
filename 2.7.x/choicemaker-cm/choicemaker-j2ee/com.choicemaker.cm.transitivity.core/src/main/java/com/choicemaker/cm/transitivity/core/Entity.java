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

import java.util.ArrayList;
import java.util.List;

/**
 * This represents a basic record entity in the transitivity graph.
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies Inc.
 */
public class Entity implements INode {
	
	private Integer marking;
	
	private Comparable recordID;
	
	private char type;
	
	public Entity (Comparable ID, char type) {
		recordID = ID;
		this.type = type;
	}
	

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.transitivity.core.INode#getNodeId()
	 */
	public Comparable getNodeId() {
		return recordID;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.transitivity.core.INode#hasChildren()
	 */
	public boolean hasChildren() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.transitivity.core.INode#getChildren()
	 */
	public List getChildren() {
		return new ArrayList ();
	}


	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		if (o instanceof Entity) {
			Entity e = (Entity) o;
			
			if (type == e.type) {
				return this.recordID.compareTo(e.recordID);
			} else {
				
				if (type == STAGE_TYPE) return -1;
				else return 1;
			}
		} else {
			return 1;
		}
	}

	public boolean equals(Object o) {
		if (o instanceof Entity) {
			Entity e = (Entity) o;
			
			if (type != e.type) return false;
			else return this.recordID.equals(e.recordID);		
		} else {
			return false;
		}
	}
	
	public int hashCode() {
		return recordID.hashCode();
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.transitivity.core.INode#mark(java.lang.Integer)
	 */
	public void mark(Integer I) {
		marking = I;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.transitivity.core.INode#getMarking()
	 */
	public Integer getMarking() {
		return marking;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.transitivity.core.INode#getType()
	 */
	public char getType() {
		return type;
	}

}
