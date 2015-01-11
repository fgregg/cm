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

import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_SOURCE_ROLE;

/**
 * @author pcheung
 */
public class RecordID<T extends Comparable<T>> implements
		Comparable<RecordID<T>> {

	private final T recordId;
	private final char role;

	public RecordID(T id, RECORD_SOURCE_ROLE role) {
		if (id == null || role == null) {
			throw new IllegalArgumentException("null argument");
		}
		this.recordId = id;
		this.role = role.getCharSymbol();
	}

	public T getRecordId() {
		return recordId;
	}

	public RECORD_SOURCE_ROLE getRole() {
		return RECORD_SOURCE_ROLE.fromSymbol(role);
	}

	@Override
	public int compareTo(RecordID<T> o) {
		final int EQUALS = 0;
		int retVal = this.getRole().compareTo(o.getRole());
		if (retVal == EQUALS) {
			retVal = this.getRecordId().compareTo(o.getRecordId());
		}
		return retVal;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result =
			prime * result + ((getRecordId() == null) ? 0 : getRecordId().hashCode());
		result = prime * result + role;
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
		RecordID other = (RecordID) obj;
		if (getRecordId() == null) {
			if (other.getRecordId() != null) {
				return false;
			}
		} else if (!getRecordId().equals(other.getRecordId())) {
			return false;
		}
		if (getRole() == null) {
			if (other.getRole() != null) {
				return false;
			}
		} else if (!getRole().equals(other.getRole())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "RecordID [id=" + getRecordId() + ", role=" + getRole() + "]";
	}

}
