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
package com.choicemaker.cm.core.base;

import java.io.Serializable;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.IProbabilityModel;

/**
 * Holder class for a returned record. If the actual record data should be returned
 * to the caller, a subclass instance must be used.
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.2 $ $Date: 2010/03/24 21:05:49 $
 */
public class MatchCandidate implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = -3049844892658731777L;

	/** The differ decision. */
	public static final int DIFFER = Decision.DIFFER.toInt();
	/** The hold decision. */
	public static final int HOLD = Decision.HOLD.toInt();
	/** The match decision. */
	public static final int MATCH = Decision.MATCH.toInt();
	
	private Object id;
	private float probability;
	private int decision;
	private String[] notes;
	
	/**
	 * Constructs a <code>MatchCandidate</code> with the specified parameters.
	 * 
	 * @param   id  The ID of the record (unique key field of the root node type).
	 * @param   probability  The match probability (between 0 and 1).
	 * @param   decision  The decision (DIFFER, HOLD, MATCH).
	 * @param   notes  The names of the active clues and rule with note or report modifier.
	 */
	public MatchCandidate(Object id, float probability, int decision, String[] notes) {
		this.id = id;
		this.probability = probability;
		this.decision = decision;
		this.notes = notes;
	}
	
	public MatchCandidate(Match match, IProbabilityModel model) {
		this(match.id, match.probability, match.decision.toInt(), match.ac.getNotes(model));
	}
		
	/**
	 * Returns the decision.
	 * 
	 * @return  The decision.
	 */
	public int getDecision() {
		return decision;
	}

	/**
	 * Return the ID of the record.
	 * 
	 * @return  The ID of the record 
 	 */ 
	public Object getId() {
		return id;
	}

	/**
	 * Returns the match probability.
	 * 
	 * @return  The match probability (between 0 and 1).
	 */
	public float getProbability() {
		return probability;
	}
	
	/**
	 * Returns the names of the active clues and rule with note or report modifier.
	 * 
	 * @return  The names of the active clues and rule with note or report modifier.
	 */
	public String[] getNotes() {
		return notes;
	}
	public Object getProfile() {
		throw new UnsupportedOperationException("Use subclass, e.g., XmlMatchCandidate or BeanMatchCandidate, if you need profile.");
	}
}
