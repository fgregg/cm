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
package com.choicemaker.cm.matching.gen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A Relation is essentially a Map from keys to sets of values.
 * For example, "A" might map to {"B", "C"}, "D" might map to
 * {"E", "F"}, etc.
 * 
 * A relation can also be reflexive.  If a relation is constructed
 * as reflexive, each key implicitly maps to itself.  Thus, if the
 * above relation is <code>r</code> is reflexive, <code>r.get("A")</code>
 * returns {"A", "B", "C"}.
 * 
 * @author Adam Winkel
 */
public class Relation {

	private Map m = new HashMap();
	private boolean reflexive;

	private Relation inverse;

	/**
	 * Construct a new Relation, which is reflexive by default.
	 */
	public Relation() {
		this(true);
	}

	/**
	 * Construct a new Relation.
	 * 
	 * @param reflexive whether or not the new relation is reflexive
	 */
	public Relation(boolean reflexive) {
		this.reflexive = reflexive;
	}
	
	/**
	 * Returns true iff this Relation is reflexive.
	 * 
	 * @return true iff this Relation is reflexive
	 */
	public boolean isReflexive() {
		return reflexive;	
	}

	/**
	 * Adds y to x's mapped set in this relation.
	 * 
	 * @param x the key
	 * @param y the value to add to x's mapped set
	 */
	public void add(Object x, Object y) {
		Set s = (Set) m.get(x);
		if (s == null) {
			s = new HashSet();
			s.add(y);
			if (reflexive) {
				s.add(x);
			}
			m.put(x, s);
		} else {
			s.add(y);
		}
		
		inverse = null;
	}

	/**
	 * Returns x's mapped set.  If this Relation is reflexive,
	 * the mapped set will always contain x.
	 * 
	 * @param x the key
	 * @return x's mapped set
	 */
	public Set get(Object x) {
		return get(x, true);
	}

	/**
	 * Returns x's mapped set.  If this Relation is reflexive
	 * and <code>considerReflexive</code> is true, then 
	 * the mapped set will always contain x.
	 * 
	 * User code must not modify the returned set.
	 * 
	 * @param x the key
	 * @param considerReflexive whether or not to include x in the mapped set (only for reflexive Relations)
	 * @return x's mapped set
	 */
	public Set get(Object x, boolean considerReflexive) {
		Set s = (Set) m.get(x);
		if (s == null) {
			if (reflexive && considerReflexive) {
				s = new HashSet();
				s.add(x);
			} else {
				s = new HashSet();
			}
		}
		return s;
	}
	
	/**
	 * Returns the inverse of this Relation.  The inverse of
	 * a relation is defined as
	 * <ul>
	 * <li>If Relation r is reflexive, r.getInverse() is also reflexive.
	 * <li>For each y in r.get(x), r.getInverse().get(y) contains x.
	 * </ul>
	 * 
	 * @return the inverse of this Relation
	 */
	public Relation getInverse() {
		if (inverse == null) {
			inverse = new Relation(reflexive);
			Iterator itKeys = m.keySet().iterator();
			while (itKeys.hasNext()) {
				Object key = itKeys.next();
				Iterator itValues = get(key).iterator();
				while (itValues.hasNext()) {
					inverse.add(itValues.next(), key);	
				}
			}		
		}
		
		return inverse;
	}

}
