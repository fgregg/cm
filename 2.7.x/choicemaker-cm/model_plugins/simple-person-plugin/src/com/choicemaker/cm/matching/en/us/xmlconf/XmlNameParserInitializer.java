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

import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.xmlconf.XmlModuleInitializer;
import com.choicemaker.cm.matching.en.us.NameParser;
import com.choicemaker.cm.matching.gen.Sets;

/**
 * XML initializer for collections (sets).
 * 
 * The name parser can be customized through the configuration file. 
 * The following gives a sample configuration:
 * <pre>
&LTmodule class="com.choicemaker.cm.xmlconf.XmlNameParserInitializer"&GT
		&LTgenericFirstNames&GTgenericFirstNames&LT/genericFirstNames&GT
		&LTinvalidLastNames&GTinvalidLastNames&LT/invalidLastNames&GT
		&LTnameTitles&GTnameTitles&LT/nameTitles&GT
		&LTchildOfIndicators&GTchildOfIndicators&LT/childOfIndicators&GT
&LT/module&GT
   </pre>
 *
 * The value of <code>genericFirstNames</code> defines the set of generic first names, such 
 * as "unknown" or "baby", that are to be filtered out. Dually, the field 
 * <code>invalidLastNames</code> defines the set of invalid last names.
 * The <code>nameTitles</code> defines the set of titles, such as "MR" and "JR".
 * The <code>childOfIndicators</code> is specific to parsing names of children. 
 * In some applications values like "MC Amanda" standing for 
 * "male child of Amanda" are common. In this case, the child's first name is 
 * undefined and the mother's first name is set to Amanda. 
 * 
 * <br/>
 * All four elements are optional. If present, their values must define sets 
 * (Section 5.1) that are defined before the name parser initialization.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/27 22:13:46 $
 * @see       com.choicemaker.cm.matching.en.us.NameParser
 */
public class XmlNameParserInitializer implements XmlModuleInitializer {
	public final static XmlNameParserInitializer instance = new XmlNameParserInitializer();

	private XmlNameParserInitializer() {
	}

	public void init(Element e) throws XmlConfException {
		String gfn = e.getChildText("genericFirstNames");
		if (gfn != null) {
			NameParser.genericFirstNames = Sets.getCollection(gfn);
		}
		String coi = e.getChildText("childOfIndicators");
		if (coi != null) {
			NameParser.childOfIndicators = Sets.getCollection(coi);
		}
		String iln = e.getChildText("invalidLastNames");
		if (iln != null) {
			NameParser.invalidLastNames = Sets.getCollection(iln);
		}
		String nt = e.getChildText("nameTitles");
		if (nt != null) {
			NameParser.nameTitles = Sets.getCollection(nt);
		}
		String lnp = e.getChildText("lastNamePrefixes");
		if (lnp != null) {
			NameParser.lastNamePrefixes = Sets.getCollection(lnp);
		}
	}
}
