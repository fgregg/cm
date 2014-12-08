/*
 * ScoreNoteType.java       Revision: 2.5  Date: Sep 9, 2005 3:25:10 PM 
 *
 * Copyright (c) 2001 ChoiceMaker Technologies, Inc.
 * 48 Wall Street, 11th Floor, New York, NY 10005
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * ChoiceMaker Technologies Inc. ("Confidential Information").
 */
package com.choicemaker.cm.urm.config;


/**
 * A type of the match score
 * <ul>
 * <li>NO_NOTE - match score note member will not assigned.</li>
 * <li>RULE_LIST_NOTE match score note member will contain names of the fired
 * clues or notes. Only clues and notes marked in the model clue file by the
 * <code>note</code> modifier will be included into the note.</li>
 * </ul>
 * <p>
 *
 * @author emoussikaev
 * @version Revision: 2.5 Date: Nov 1, 2005 1:46:30 PM
 */
public enum ScoreType {
	NO_NOTE, RULE_LIST_NOTE;
}
