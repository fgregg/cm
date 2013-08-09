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
package com.choicemaker.cm.matching.en.us.xmlconf;

import org.jdom.Element;

import com.choicemaker.cm.core.xmlconf.XmlConfException;
import com.choicemaker.cm.core.xmlconf.XmlModuleInitializer;
import com.choicemaker.cm.matching.en.us.StreetParser;
import com.choicemaker.cm.matching.gen.Maps;
import com.choicemaker.cm.matching.gen.Sets;

/**
 * XML initializer for collections (sets).
 * 
 * The street parser can be customized through the configuration file. 
 * The following gives a sample configuration:
 * <pre>
&LTmodule class="com.choicemaker.cm.xmlconf.XmlStreetParserInitializer"&GT
		&LTdirections&GTdirections&LT/directions&GT
		&LTstreetTypes&GTstreetTypes&LT/streetTypes&GT
		&LTordinalExtensions&GTordinalExtensions&LT/ordinalExtensions&GT
		&LTstandardDirections&GTstandardDirections&LT/standardDirections&GT
		&LTstandardOrdinalTypes&GTstandardOrdinalTypes&LT/standardOrdinalTypes&GT
		&LTcommonStreetNameShortHands&GTcommonStreetNameShortHands&LT/commonStreetNameShortHands&GT
		&LTstandardStreetTypes&GTstandardStreetTypes&LT/standardStreetTypes&GT
&LT/module&GT
   </pre>
 *
 * The value of <code>directions</code> defines the set of directions, such as 
 * "NORTH', "E", etc. 
 * <code>streetTypes</code> defines the set of words denoting a street type, 
 * such as "ST" and "AVENUE". 
 * <code>ordinalExtensions</code> defines the set of suffixes for ordinal 
 * numbers, e.g., "ST", "ND", "RD", and "TH". 
 * <br/>
 * All three elements are optional. If present, their values must define sets (Section 5.1) 
 * that are defined before the street parser initialization.
 * <br/>
 * <code>standardDirections</code> defines the canonical forms for directions, 
 * e.g., "N" for "NORTH". 
 * <code>standardOrdinalTypes</code> defines the written out ordinal names, 
 * e.g., "FIRST" for "1". 
 * <code>commonStreetNameShortHands</code> defines the canonical form for common 
 * street names, e.g., "BROADWAY" for "BWAY". 
 * Finally, the <code>standardStreetTypes</code> define the canonical name for 
 * street types, e.g., "AVE" for "AVENUE" and "AV". 
 * <br/>
 * All four elements are optional. If present, their values must define maps (Section 5.2) 
 * that are defined before the street parser initialization.

 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:02 $
 * @see       com.choicemaker.cm.matching.en.us.StreetParser
 */
public class XmlStreetParserInitializer implements XmlModuleInitializer {
	public final static XmlStreetParserInitializer instance = new XmlStreetParserInitializer();

	private XmlStreetParserInitializer() {
	}

	public void init(Element e) throws XmlConfException {
		String s = e.getChildText("directions");
		if (s != null) {
			StreetParser.directions = Sets.getCollection(s);
		}
		s = e.getChildText("streetTypes");
		if (s != null) {
			StreetParser.streetTypes = Sets.getCollection(s);
		}
		s = e.getChildText("ordinalExtensions");
		if (s != null) {
			StreetParser.ordinalExtensions = Sets.getCollection(s);
		}
		s = e.getChildText("standardDirections");
		if (s != null) {
			StreetParser.standardDirections = Maps.getMap(s);
		}
		s = e.getChildText("standardOrdinalTypes");
		if (s != null) {
			StreetParser.standardOrdinalTypes = Maps.getMap(s);
		}
		s = e.getChildText("commonStreetNameShortHands");
		if (s != null) {
			StreetParser.commonStreetNameShortHands = Maps.getMap(s);
		}
		s = e.getChildText("standardStreetTypes");
		if (s != null) {
			StreetParser.standardStreetTypes = Maps.getMap(s);
		}

	}
}
