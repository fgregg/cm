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

import java.util.*;

import com.choicemaker.cm.core.*;
import com.choicemaker.cm.core.datamodel.DefaultCompositeObservableData;

/**
 * .
 *
 * @author   Arturo Falck
 * @version  $Revision: 1.2 $ $Date: 2010/04/15 20:56:20 $
 */
public class ReviewCase extends DefaultCompositeObservableData {

    public static final String HUMAN_CURRENTLY_REVIEWING = "HUMAN_CURRENTLY_REVIEWING";

	// ***************************** Fields that DON'T get CLEARED
	
	private Repository repository;
	
	private ServerData data;
	
	// ***************************** Fields that get CLEARED
	
	private Record baseRecord;
	private PotentialMatch[] potentialMatches;
	private PotentialMatch currentlyReviewing;

	private String usr = System.getProperty("user.name");



	/**
	 * Creates a ReviewCase by extracting the required information from the ServerData.
	 * 
	 * @param source the original data.
	 */
	public ReviewCase(Repository repository, ServerData data) throws IllegalArgumentException {
		this.repository = repository;
		this.data = data;
		if (getType() == ServerData.DATA) {
			resetData(data);
		}
	}

	public int getType() {
		return data.getType();
	}

	protected void resetData(ServerData data) {
		baseRecord = data.getBaseRecord();
		MatchRecord[] potentialPairs = data.getPotentiallyMatchingRecords();
		if (potentialPairs != null){
			potentialMatches = new PotentialMatch[potentialPairs.length];
			for (int i = 0; i < potentialPairs.length; i++) {
				potentialMatches[i] = new PotentialMatch(createMarkedRecordPair(baseRecord, potentialPairs[i]));			
			}
		}
	}
	
	protected MutableMarkedRecordPair createMarkedRecordPair(Record base, MatchRecord potentialPair) {
		Date date = new Date();
		String src = "";
		String comment = "";
		MutableMarkedRecordPair returnValue = new MutableMarkedRecordPair(base, potentialPair.getRecord(), potentialPair.getChoiceMakerDecision(), date, usr, src, comment);
		returnValue.setProbability(potentialPair.getProbability());		
		returnValue.setRepository(repository);
		return returnValue;
	}

	public Record getBaseRecord(){
		return baseRecord;
	}

	public PotentialMatch[] getPotentialMatches() {
		return potentialMatches;
	}
	
	public ClientData getClientData(){
		return new ClientData(getId(), getDecisions());
	}

	public int getId() {
		return data.getId();
	}
	
	protected Decision[] getDecisions(){
		PotentialMatch[] matches = getPotentialMatches();		
		Decision[] returnValue = new Decision[matches.length];
		for (int i = 0; i < returnValue.length; i++) {
			returnValue[i] = matches[i].getHumanDecision();
		}
		return returnValue;
	}
	
	/**
	 * Returns the currentlyReviewing.
	 * @return PotentialMatch
	 */
	public PotentialMatch getCurrentlyReviewing() {
		return currentlyReviewing;
	}

	/**
	 * Sets the currentlyReviewing.
	 * @param currentlyReviewing The currentlyReviewing to set
	 */
	public void setCurrentlyReviewing(PotentialMatch newValue) {
        PotentialMatch oldValue = this.currentlyReviewing;
		this.currentlyReviewing = newValue;
        firePropertyChange(HUMAN_CURRENTLY_REVIEWING, oldValue, newValue);
        
        if (newValue != null){
	        newValue.setHumanReviewed(true);
        }
	}

	/**
	 * Resets the PotentialMatches and notifies listeners.
	 */	
	public void resetDecisions() {
		MatchRecord[] potentialPairs = data.getPotentiallyMatchingRecords();
		for (int i = 0; i < potentialPairs.length; i++) {
			potentialMatches[i].setHumanDecision(potentialPairs[i].getChoiceMakerDecision());
		}
	}
}
