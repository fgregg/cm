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
package com.choicemaker.cm.core.configure.xml;

import java.util.List;

/**
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 18:04:31 $
 */
public interface IElement {

	/**
	 * This returns the (local) name of the
	 * <code>IElement</code>, without any
	 * namespace prefix, if one exists.
	 *
	 * @return <code>String</code> - element name.
	 */
	public abstract String getName();

	/**
	 * This returns a <code>List</code> of all the child elements
	 * nested directly (one level deep) within this element, as
	 * <code>IElement</code> objects.  If this target element has no nested
	 * elements, an empty List is returned.  The returned list is "live"
	 * in document order and changes to it affect the element's actual
	 * contents.
	 *
	 * <p>
	 * Sequential traversal through the List is best done with a Iterator
	 * since the underlying implement of List.size() may not be the most
	 * efficient.
	 * </p>
	 *
	 * <p>
	 * No recursion is performed, so elements nested two levels deep
	 * would have to be obtained with:
	 * <pre>
	 * <code>
	 *   Iterator itr = (currentElement.getChildren()).iterator();
	 *   while(itr.hasNext()) {
	 *     IElement oneLevelDeep = (IElement)itr.next();
	 *     List twoLevelsDeep = oneLevelDeep.getChildren();
	 *     // Do something with these children
	 *   }
	 * </code>
	 * </pre>
	 * </p>
	 *
	 * @return list of child <code>IElement</code> objects for this element
	 */
	public abstract List getChildren();
	/**
	 * This returns a <code>List</code> of all the child elements
	 * nested directly (one level deep) within this element with the given
	 * local name and belonging to no namespace, returned as
	 * <code>IElement</code> objects.  If this target element has no nested
	 * elements with the given name outside a namespace, an empty List
	 * is returned.  The returned list is "live" in document order
	 * and changes to it affect the element's actual contents.
	 * <p>
	 * Please see the notes for <code>{@link #getChildren}</code>
	 * for a code example.
	 * </p>
	 *
	 * @param name local name for the children to match
	 * @return all matching child elements
	 */
	public abstract List getChildren(String name);

	/**
	 * This returns the first child element within this element with the
	 * given local name and belonging to no namespace.
	 * If no elements exist for the specified name and namespace, null is
	 * returned.
	 *
	 * @param name local name of child element to match
	 * @return the first matching child element, or null if not found
	 */
	public abstract IElement getChild(String name);

	/**
	 * <p>
	 * This returns the attribute value for the attribute with the given name
	 * and within no namespace, null if there is no such attribute, and the
	 * empty string if the attribute value is empty.
	 * </p>
	 *
	 * @param name name of the attribute whose value to be returned
	 * @return the named attribute's value, or null if no such attribute
	 */
	public abstract String getAttributeValue(String name);

}
