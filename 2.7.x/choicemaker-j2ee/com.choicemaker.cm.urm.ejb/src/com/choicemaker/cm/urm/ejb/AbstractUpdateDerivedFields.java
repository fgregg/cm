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
package com.choicemaker.cm.urm.ejb;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.configure.AbstractXmlSpecification;
import com.choicemaker.cm.core.configure.XmlConfigurable;
import com.choicemaker.cm.urm.IUpdateDerivedFields;

/**
 * Provides standardized methods for
 * {@link IUpdateDerivedFields.toXML toXML()},
 * {@link IUpdateDerivedFields.getProperties() getProperties()} and
 * {@link IUpdateDerivedFields.setProperties(Properties) setProperties(..)}
 * operations.
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/25 00:17:22 $
 */
public abstract class AbstractUpdateDerivedFields
	extends AbstractXmlSpecification
	implements IUpdateDerivedFields {
		
	private static Logger logger = Logger.getLogger(AbstractUpdateDerivedFields.class);
		
	/**
	 * Most implementations of IUpdateDerivedFields will not need
	 * XmlConfigurable delegates, so this default implementation
	 * does nothing beside logging the ignored argument.
	 * see #getChildren()
	 */
	public void add(XmlConfigurable ignored) {
		String msg = "Ignoring XmlConfigurable delegate (type "
		+ (ignored == null ? "null" : ignored.getClass().getName() + ")");
		logger.warn(msg);
	}

	/**
	 * Most implementations of IUpdateDerivedFields will not need
	 * XmlConfigurable delegates, so this default implementation
	 * does nothing beside logging the ignored argument.
	 * @see #add(XmlConfigurable)
	 */
	public XmlConfigurable[] getChildren() {
		return new XmlConfigurable[0];
	}

	/**
	 * Most implementations of IUpdateDerivedFields will not <em>require</em>
	 * any configuration properties, so this method returns an
	 * empty (but non-null) array.
	 */
	public String[] getRequiredPropertyNames() {
		return new String[0];
	}

	/**
	 * Some implementations of IUpdateDerivedFields <em>might</em> use
	 * optional configuration properties, so this method returns an
	 * empty (but non-null) array, which indicates all property names
	 * are allowed.
	 */
	public String[] getAllowedPropertyNames() {
		return new String[0];
	}

	/**
	 * Some implementations of IUpdateDerivedFields <em>might</em> use
	 * optional configuration properties, so this default method always
	 * returns for any non-blank property name and any non-null property
	 * value. (Subclasses may be more fastidious about checking
	 * property values.)
	 * @param name the property name
	 * @param value the property value
	 */
	public boolean isAllowedPropertyValue(String name, String value) {
		return (name != null && name.trim().length() > 0 && value != null);
	}

}
