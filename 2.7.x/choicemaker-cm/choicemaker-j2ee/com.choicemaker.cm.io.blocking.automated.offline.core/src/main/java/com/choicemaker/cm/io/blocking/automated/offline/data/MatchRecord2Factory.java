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
package com.choicemaker.cm.io.blocking.automated.offline.data;

import static com.choicemaker.cm.core.Decision.DIFFER;
import static com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_SOURCE_ROLE.STAGING;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_SOURCE_ROLE;

/**
 * @author pcheung
 *
 */
public class MatchRecord2Factory {

	public static final Integer DEFAULT_INTEGER_ID = Integer.valueOf(0);
	public static final Long DEFAULT_LONG_ID = Long.valueOf(0);
	public static final String DEFAULT_STRING_ID = "@";
	public static final RECORD_SOURCE_ROLE DEFAULT_ROLE = STAGING;
	public static final float DEFAULT_PROBABILITY = 0.0f;
	public static final Decision DEFAULT_DECISION = DIFFER;
	public static final String DEFAULT_NOTES = null;

	/**
	 * This method returns a special MatchRecord2 that serves as a separator
	 * when in the MatchRecord2Source to build CompositeEntities.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> MatchRecord2<?> getSeparator(T t) {
		T id = null;
		if (t instanceof Integer) {
			id = (T) DEFAULT_INTEGER_ID;
		} else if (t instanceof Long) {
			id = (T) DEFAULT_LONG_ID;
		} else if (t instanceof String) {
			id = (T) DEFAULT_STRING_ID;
		} else {
			throw new IllegalArgumentException(
					"This id type is not supported: " + t.getClass().getName());
		}

		return new MatchRecord2<T>(id, id, DEFAULT_ROLE, DEFAULT_PROBABILITY,
				DEFAULT_DECISION, DEFAULT_NOTES);
	}

}
