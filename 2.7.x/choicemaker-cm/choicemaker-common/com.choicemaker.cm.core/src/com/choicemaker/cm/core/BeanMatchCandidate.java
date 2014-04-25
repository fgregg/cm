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
package com.choicemaker.cm.core;


/**
 * Match candidate that holds the actual match record in form of a Java bean constructed
 * of the generated holder classes.
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.2 $ $Date: 2010/03/24 20:59:50 $
 */
public class BeanMatchCandidate extends MatchCandidate {
	private static final long serialVersionUID = 1L;
	private Object profile;
	
	/**
	 * Constructs a <code>BeanMatchCandidate</code> with the specified parameters.
	 * Note that this constructor does not clone the profile.
	 * 
	 * @param   id  The ID of the record (unique key field of the root node type).
	 * @param   probability  The match probability (between 0 and 1).
	 * @param   decision  The decision (DIFFER, HOLD, MATCH).
	 * @param   profile  The Java bean representation of the match record.
	 * @param   notes  The names of the active clues and rule with note or report modifier.
	 */
	public BeanMatchCandidate(Object id, float probability, int decision, Object profile, String[] notes) {
		super(id, probability, decision, notes);
		this.profile = profile;
	}
		
	public BeanMatchCandidate(Match match, IProbabilityModel model) {
		this(match.id, match.probability, match.decision.toInt(), model.getAccessor().toHolder(match.m), match.ac.getNotes(model));
	}
	
	/**
	 * Returns the Java bean representation of the match record.
	 * 
	 * @return  The Java bean representation of the match record.
	 */
	public Object getProfile() {
		return profile;
	}
}

