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

import java.util.Collections;
import java.util.List;

/**
 * This represents a basic record entity in the transitivity graph.
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies Inc.
 */
public class Entity<T extends Comparable<T>> implements INode<T> {
	
	private Integer marking;
	private final T recordID;
	private final char type;
	
	public Entity (T ID, char type) {
		recordID = ID;
		this.type = type;
	}
	

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.transitivity.core.INode#getNodeId()
	 */
	public T getNodeId() {
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
	public List<INode<T>> getChildren() {
		return Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public int compareTo(INode<T> o) {
		if (o instanceof Entity) {
			@SuppressWarnings("rawtypes")
			Entity e = (Entity) o;
			if (type == e.type) {
				return this.recordID.compareTo((T) e.recordID);
			} else {
				
				if (type == STAGE_TYPE) return -1;
				else return 1;
			}
		} else {
			return 1;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((marking == null) ? 0 : marking.hashCode());
		result =
			prime * result + ((recordID == null) ? 0 : recordID.hashCode());
		result = prime * result + type;
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		Entity other = (Entity) obj;
		if (marking == null) {
			if (other.marking != null) {
				return false;
			}
		} else if (!marking.equals(other.marking)) {
			return false;
		}
		if (recordID == null) {
			if (other.recordID != null) {
				return false;
			}
		} else if (!recordID.equals(other.recordID)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
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
