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
package com.choicemaker.cm.io.xml.base;

import org.xml.sax.SAXException;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.InvalidProfileException;
import com.choicemaker.cm.core.Profile;
import com.choicemaker.cm.core.Record;

/**
 * Profile that represents a query record as a XML document. Refer to the ChoiceMaker
 * User's Guide for exact representation.
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.2 $ $Date: 2010/03/28 09:18:16 $
 */
public class XmlProfile implements Profile {
	private static final long serialVersionUID = 1L;
	private String profile;
	
	/**
	 * Constructs a <code>XmlProfile</code> with the specified XML representaion of the query record.
	 * 
	 * @param   profile  The XML representation of the query record.
	 */
	public XmlProfile(String profile) {
		this.profile = profile;
	}
	
	/**
	 * Returns the XML representation of the query record.
	 * 
	 * @return  The XML representation of the query record.
	 */
	public String getProfile() {
		return profile;
	}
	
	public String toString() {
		return profile;
	}
	
	public Record getRecord(ImmutableProbabilityModel model) throws InvalidProfileException {
		try {
			return XmlSingleRecordReader.getRecord(model, getProfile());
		} catch (SAXException ex) {
			throw new InvalidProfileException("", ex);
		}
	}
}
