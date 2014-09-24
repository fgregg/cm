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

/**
 * @author pcheung
 *
 */
public class MatchRecord2Factory {
	
	/**
	 * This method returns a special MatchRecord2 that serves as a separator when
	 * in the MatchRecord2Source to build CompositeEntities.
	 * 
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static MatchRecord2<?> getSeparator (Comparable<?> C) {
		Comparable<?> id = null;
		if (C instanceof Integer) id = new Integer (0);
		else if (C instanceof Long) id = new Long (0);
		else if (C instanceof String) id = "@";
		else throw new IllegalArgumentException ("This id type is not supported: " + C.getClass().getName());
		
		// 2009-08-17 rphall
		// NO BUG: clue notes are not required for separator records
		final String noteInfo = null;
		return new MatchRecord2 (id, id, MatchRecord2.STAGE_SOURCE, 
			0.0f, MatchRecord2.DIFFER,noteInfo);
		// END NO BUG
	}

}
