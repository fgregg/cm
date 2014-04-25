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
package com.choicemaker.cm.modelmaker.gui.utils;

import java.util.Comparator;

import org.apache.log4j.Logger;

import com.choicemaker.cm.modelmaker.gui.tables.ClTableRow;

/**
 * Comparator used for sorting the rows of ClueTableRow in the ClueDataTable.
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:10 $
 */

public class ClueDataComparator implements Comparator {

	private static Logger logger = Logger.getLogger(ClueDataComparator.class);
	protected int sortCol;
	protected boolean sortAsc;

	public ClueDataComparator(int sortCol, boolean sortAsc) {
		this.sortCol = sortCol;
		this.sortAsc = sortAsc;
	}

	/**
	 * Compares its two arguments for order.  Returns a negative integer,
	 * zero, or a positive integer as the first argument is less than, equal
	 * to, or greater than the second.<p>
	 *
	 * The implementor must ensure that <tt>sgn(compare(x, y)) ==
	 * -sgn(compare(y, x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
	 * implies that <tt>compare(x, y)</tt> must throw an exception if and only
	 * if <tt>compare(y, x)</tt> throws an exception.)<p>
	 *
	 * The implementor must also ensure that the relation is transitive:
	 * <tt>((compare(x, y)&gt;0) &amp;&amp; (compare(y, z)&gt;0))</tt> implies
	 * <tt>compare(x, z)&gt;0</tt>.<p>
	 *
	 * Finally, the implementer must ensure that <tt>compare(x, y)==0</tt>
	 * implies that <tt>sgn(compare(x, z))==sgn(compare(y, z))</tt> for all
	 * <tt>z</tt>.<p>
	 *
	 * It is generally the case, but <i>not</i> strictly required that
	 * <tt>(compare(x, y)==0) == (x.equals(y))</tt>.  Generally speaking,
	 * any comparator that violates this condition should clearly indicate
	 * this fact.  The recommended language is "Note: this comparator
	 * imposes orderings that are inconsistent with equals."
	 *
	 * @param o1 the first object to be compared.
	 * @param o2 the second object to be compared.
	 * @return a negative integer, zero, or a positive integer as the
	 *         first argument is less than, equal to, or greater than the
	 *         second.
	 * @throws ClassCastException if the arguments' types prevent them from
	 *         being compared by this Comparator.
	 */
	public int compare(Object o1, Object o2) {
		int result;
		Object v1 = ((ClTableRow) o1).getColumn(sortCol);
		Object v2 = ((ClTableRow) o2).getColumn(sortCol);
		if (v1 instanceof Comparable) {
			result = ((Comparable) v1).compareTo(v2);
		} else {
			boolean b1 = ((Boolean) v1).booleanValue();
			boolean b2 = ((Boolean) v2).booleanValue();
			if (b1 == b2) {
				result = 0;
			} else if (b1) {
				result = 1;
			} else {
				result = -1;
			}
		}
		if (!sortAsc) {
			result = -result;
		}
		return result;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (sortAsc ? 1231 : 1237);
		result = prime * result + sortCol;
		return result;
	}

 	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClueDataComparator other = (ClueDataComparator) obj;
		if (sortAsc != other.sortAsc)
			return false;
		if (sortCol != other.sortCol)
			return false;
		return true;
	}

	/**
	 * Obsolete method for {@link #equals(Object)}. Used for testing only.
	 * @deprecated
	 */
	public boolean equals_00(Object obj){
		if (obj instanceof ClueDataComparator) {
			ClueDataComparator compObj = (ClueDataComparator) obj;
			return (compObj.sortCol == sortCol) && (compObj.sortAsc == sortAsc);
		}
		return false;
	}

}
