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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.transitivity.core.CompositeEntity;

/**
 * This object takes a CompositeEntitySource and wraps it as an Iterator.
 * This also sorts the CompositeEntities by the smallest node id.
 * 
 * @author pcheung
 *
 */
public class CompositeEntityIterator2 implements Iterator {
	
	private static final Logger log = Logger.getLogger(CompositeEntityIterator2.class);
	
	private Iterator it;

	public CompositeEntityIterator2 (CompositeEntitySource source) {
		try {
			ArrayList list = new ArrayList ();
			source.open();
			while (source.hasNext()) {
				CompositeEntity ce = source.getNext();
				list.add(ce);
			}
			
			source.close();
			
			Collections.sort(list, new CEComparator ());
			
			it = list.iterator();
		} catch (BlockingException e) {
			log.error(e,e);
		}
	}
	

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		it.remove();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return it.hasNext();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		return it.next();
	}


	private static class CEComparator implements Comparator {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			CompositeEntity ce1 = (CompositeEntity) o1;
			CompositeEntity ce2 = (CompositeEntity) o2;
			
			Comparable c1 = ce1.getFirstNode();
			Comparable c2 = ce2.getFirstNode();

			return c1.compareTo(c2);
		}
	}

}
