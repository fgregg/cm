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

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * <code>DerivedSource</code> is a helper class used for determining whether
 * the value of a field must be computed (derived) or whether it has been
 * read in, depending upon the source of the record.
 *
 * Common <code>DerivedSource</code>s are created from the following
 * values:
 * <ol>
 *   <li><code>"all"</code>: This special <code>DerivedSource</code> is used to
 *     indicate that the field must be computed for all sources.</li>
 *   <li><code>"xml,ora"</code>: This example indicates that the field must be
 *     computed if read from the <code>xml</code> or <code>ora</code> source.</li>
 *   <li><code>"xml"</code>: Single value <code>DerivedSource</code> like this are
 *     used to say that the field must be computed only if read from this particular
 *     source. Furthermore, they are used by sources as parameters to
 *     <code>BaseRecord.computeValidityAndDerived()</code>.
 * </ol>
 *
 * Instances of this class are immutable. This class uses instance control.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/27 21:33:20 $
 */
public class DerivedSource implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = 577834304306480791L;

	private Set elems;
	private String str;
	private static List ds = new LinkedList();
	/** The DerivedSource "all". */
	public static final DerivedSource ALL;
	public static final DerivedSource NONE;

	static {
		Set s = new TreeSet();
		s.add("all");
		ALL = new DerivedSource("all", s);
		NONE = new DerivedSource("none", new TreeSet());
	}

	private DerivedSource(String str, Set elems) {
		this.str = str;
		this.elems = elems;
		ds.add(this);
	}

	/**
	 * Returns a <code>DerivedSource</code> object representing the specified
	 * <code>String</code>.
	 *
	 * @param   s  The textual representation of the <code>DerivedSource</code>.
	 * @return  a <code>DerivedSource</code> object representing the specified
	 *              <code>String</code>.
	 */
	public synchronized static DerivedSource valueOf(String s) {
		// First, fast attempt: interned String reference comparison
		s = s.intern();
		Iterator i = ds.iterator();
		while (i.hasNext()) {
			DerivedSource src = (DerivedSource) i.next();
			if (src.str == s) {
				return src;
			}
		}

		// Second, complete attempt: set representation comparison
		StringTokenizer st = new StringTokenizer(s, ", ");
		Set el = new TreeSet();
		while (st.hasMoreTokens()) {
			String t = st.nextToken().intern();
			if (t == "all") {
				return ALL;
			} else {
				el.add(t);
			}
		}
		i = ds.iterator();
		while (i.hasNext()) {
			DerivedSource src = (DerivedSource) i.next();
			if (src.elems.equals(el)) {
				return src;
			}
		}

		// No match, create new
		return new DerivedSource(s, el);
	}

	/**
	 * Returns <code>true</code> if this includes <code>s</code>.
	 * Inclusion holds if this is <code>all</code> or if the set
	 * of this <code>DerivedSource</code> sources includes the set
	 * of <code>s</code>'s sources.
	 *
	 * @param   s The <code>DerivedSource</code> to be checked.
	 * @return  <code>true</code> if this includes <code>s</code>.
	 */
	public boolean includes(DerivedSource s) {
		if (this == ALL) {
			return true;
		}
		return elems.containsAll(s.elems);
	}

	/**
	 * Returns <code>true</code> if this includes <code>s</code>.
	 * Inclusion holds if this is <code>all</code> or if the set
	 * of this <code>DerivedSource</code> sources includes the set
	 * of <code>s</code>'s sources.
	 *
	 * @param   s The <code>String</code> representation of the
	 *            <code>DerivedSource</code> to be checked.
	 * @return  <code>true</code> if this includes <code>s</code>.
	 */
	public boolean includes(String s) {
		return includes(valueOf(s));
	}

	public String toString() {
		return str;
	}
}
