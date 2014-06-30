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
package com.choicemaker.cm.reviewmaker.base;

import com.choicemaker.cm.core.base.*;
import com.choicemaker.cm.core.datamodel.DefaultObservableData;

/**
 * Each ReviewCase consists of a Record that was marked for human review, and a
 * list of potential matches.
 * 
 * This class encapsulates the ReviewCase, the relationship between the
 * records involved, and the Descriptor that describes the records.  It also
 * includes the information necessary for maintaining Views informed of changes.
 *
 * @author   Arturo Falck
 * @version  $Revision: 1.2 $ $Date: 2010/04/15 20:55:13 $
 */
public class PotentialMatch extends DefaultObservableData {

    public static final String HUMAN_REVIEWED = "HUMAN_REVIEWED";
    public static final String HUMAN_DECIDED = "HUMAN_DECIDED";
    public static final String HUMAN_DECISION_MADE = "HUMAN_DECISION_MADE";

    private boolean wasHumanReviewed = false;
    private boolean wasHumanDecisionMade = false;
    private MutableMarkedRecordPair recordPair = null;

	public PotentialMatch(MutableMarkedRecordPair recordPair){
	    this.recordPair = recordPair;
	}

    /**
     * Returns the wasHumanReviewed.
     * @return boolean
     */
    public boolean wasHumanReviewed() {
        return wasHumanReviewed;
    }
    
    public boolean getHumanReviewed(){
    	return wasHumanReviewed();
    }

	/**
	 * Returns the wasHumanDecisionMade.
	 * @return boolean
	 */
	public boolean wasHumanDecisionMade() {
		return wasHumanDecisionMade;
	}

    /**
     * Sets the wasHumanReviewed.
     * @param wasHumanReviewed The wasHumanReviewed to set
     */
    public void setHumanReviewed(boolean newValue) {
        boolean oldValue = wasHumanReviewed;
        wasHumanReviewed = newValue;
        firePropertyChange(HUMAN_REVIEWED, new Boolean(oldValue), new Boolean(newValue));
    }

    /**
     * Sets the wasHumanReviewed.
     * @param wasHumanReviewed The wasHumanReviewed to set
     */
    public void setHumanDecisionMade(boolean newValue) {
        boolean oldValue = wasHumanDecisionMade;
        wasHumanDecisionMade = newValue;
        firePropertyChange(HUMAN_DECISION_MADE, new Boolean(oldValue), new Boolean(newValue));
    }

    public Decision getHumanDecision() {
        return recordPair.getMarkedDecision();
    }

	public float getProbability(){
		return recordPair.getProbability();
	}

    public void setHumanDecision(Decision newValue) {
        Decision oldValue = recordPair.getMarkedDecision();
        recordPair.setMarkedDecision(newValue);
        firePropertyChange(HUMAN_DECIDED, oldValue, newValue);
        
        setHumanDecisionMade(true);
    }
    

    /**
     * Returns the recordPair.
     * @return MarkedRecordPair
     */
    public MutableMarkedRecordPair getRecordPair() {
        return recordPair;
    }
    
    public boolean hasRecord(Record record){
    	return recordPair.getMatchRecord() == record || recordPair.getQueryRecord() == record;
    }
    
    public String toString(){
    	return "PotentialMatch [ " + recordPair.getQueryRecord() + " , " + recordPair.getMatchRecord() + " ]";
    }

}
