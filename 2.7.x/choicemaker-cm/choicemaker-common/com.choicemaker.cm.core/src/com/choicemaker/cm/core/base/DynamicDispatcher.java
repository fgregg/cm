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
package com.choicemaker.cm.core.base;

import java.util.Iterator;
import java.util.List;

/**
 * Helper class for finding the best matching <code>DynamicDispatchHandler</code>.
 *
 * @see       DynamicDispatchHandler
 * @author    Martin Buechi
 * @version   $Revision: 1.1 $ $Date: 2010/01/20 15:05:04 $
 */
public class DynamicDispatcher {
	/**
	 * Returns the best matching <code>DynamicDispatchHandler</code> for the
	 * specified object from the specified list; <code>null</code> if none exists.
	 *
	 * A <code>DynamicDispatchHandler</code> can handle <code>o</code> if its handled
	 * type is a supertype (reflexive) of the type of <code>o</code>.
	 * A <code>DynamicDispatchHandler</code> a is a better match than a
	 * <code>DynamicDispatchHandler</code> b if the handled type of a is a subtype of
	 * the handled type of b.
	 *
	 * If the list contains multiple <code>DynamicDispatchHandler</code>s that
	 * handle the same type, an arbitrary one will be returned.
	 *
	 * @param   l  The list of <code>DynamicDispatchHandler</code>s.
	 * @param   r  The object to be handled.
	 * @return  the best matching <code>DynamicDispatchHandler</code> for the
	 *            specified object from the specified list; <code>null</code> if none exists.
	 */
	public static Object getBestMatch(List l, Object r) {
		Class rc = r.getClass();
		DynamicDispatchHandler best = null;
		Iterator i = l.iterator();
		while (i.hasNext()) {
			DynamicDispatchHandler t = (DynamicDispatchHandler) i.next();
			if (t.getHandledType().isAssignableFrom(rc)) {
				if (best == null || best.getHandledType().isAssignableFrom(t.getHandledType())) {
					best = t;
				}
			}
		}
		return best;
	}
}
