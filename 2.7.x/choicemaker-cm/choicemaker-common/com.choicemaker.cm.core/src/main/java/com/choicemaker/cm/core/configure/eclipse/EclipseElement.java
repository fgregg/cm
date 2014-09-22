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
package com.choicemaker.cm.core.configure.eclipse;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.choicemaker.cm.core.configure.xml.IElement;
import com.choicemaker.e2.CMConfigurationElement;
import com.choicemaker.util.Precondition;

/**
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 18:04:31 $
 */
public class EclipseElement implements IElement {

	private static Logger logger = Logger.getLogger(EclipseElement.class.getName());

	private final CMConfigurationElement element;

	public EclipseElement(CMConfigurationElement element) {
		Precondition.assertNonNullArgument("null element", element);
		this.element = element;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.configure.IElement#getName()
	 */
	public String getName() {
		return this.getElement().getName();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.configure.IElement#getChildren()
	 */
	public List getChildren() {
		List retVal = new LinkedList();
		CMConfigurationElement[] eclipseChildren = this.getElement().getChildren();
		for (int i=0; i<eclipseChildren.length; i++) {
			CMConfigurationElement eclipseChild = eclipseChildren[i];
			EclipseElement returnable = new EclipseElement(eclipseChild);
			retVal.add(returnable);
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.configure.IElement#getChildren(java.lang.String)
	 */
	public List getChildren(String name) {
		List retVal = new LinkedList();
		CMConfigurationElement[] eclipseChildren = this.getElement().getChildren(name);
		for (int i=0; i<eclipseChildren.length; i++) {
			CMConfigurationElement eclipseChild = eclipseChildren[i];
			EclipseElement returnable = new EclipseElement(eclipseChild);
			retVal.add(returnable);
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.configure.IElement#getChild(java.lang.String)
	 */
	public IElement getChild(String name) {
		IElement retVal = null;
		List list = getChildren(name);
		if (list != null && list.size() >= 1) {
			retVal = (IElement) list.get(0);
		}
		if (list != null && list.size() > 1) {
			StringBuffer sb = new StringBuffer();
			int ignored = list.size() - 1;
			sb.append("ignoring " + ignored + " IConfigurableElements: [");
			int count = -1;
			for (Iterator i=list.iterator(); i.hasNext(); ) {
				++count;
				// 2014-04-24 rphall: Added use for unused local variable.
				CMConfigurationElement orphan = (CMConfigurationElement) i.next();
				sb.append(orphan.getName());
				if (count < list.size()-1) {
					sb.append(",");
				}
			}
			sb.append("]");
			String msg = sb.toString();
			logger.warning(msg);
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.configure.IElement#getAttributeValue(java.lang.String)
	 */
	public String getAttributeValue(String name) {
		return this.getElement().getAttribute(name);
	}

	protected CMConfigurationElement getElement() {
		return element;
	}

}
