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

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.base.Match;
import com.choicemaker.cm.core.base.MatchCandidate;

/**
 * Match candidate that holds the actual match record in XML format. See the ChoiceMaker User's Guide
 * for details of the format.
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.2 $ $Date: 2010/03/28 09:18:16 $
 */
public class XmlMatchCandidate extends MatchCandidate {
	private static final long serialVersionUID = 1L;
	private String profile;
	
	/**
	 * Constructs a <code>XmlMatchCandidate</code> with the specified parameters.
	 * 
	 * @param   id  The ID of the record (unique key field of the root node type).
	 * @param   probability  The match probability (between 0 and 1).
	 * @param   decision  The decision (DIFFER, HOLD, MATCH).
	 * @param   profile  The XML representation of the match record.
	 * @param   notes  The names of the active clues and rule with note or report modifier.
	 */
	public XmlMatchCandidate(Object id, float probability, int decision, String profile, String[] notes) {
		super(id, probability, decision, notes);
		this.profile = profile;
	}
	
	public XmlMatchCandidate(Match match, IProbabilityModel model) {
		this(match.id, match.probability, match.decision.toInt(), XmlSingleRecordWriter.writeRecord(model, match.m, false), match.ac.getNotes(model));
	}

	
	/**
	 * Returns the XML representation of the match record.
	 * 
	 * @return  The XML representation of the match record.
	 */
	public Object getProfile() {
		return profile;
	}
}
