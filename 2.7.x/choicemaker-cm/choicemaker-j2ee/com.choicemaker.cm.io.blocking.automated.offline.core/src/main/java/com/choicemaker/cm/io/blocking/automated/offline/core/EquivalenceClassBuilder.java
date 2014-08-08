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
package com.choicemaker.cm.io.blocking.automated.offline.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author ajwinkel
 *
 */
public class EquivalenceClassBuilder {

	private Map<Long, EquivalenceClass> ecMap;

	public EquivalenceClassBuilder() {
		ecMap = new HashMap<>();
	}

	/**
	 * The equivalence class-building algorithm works as follows. 1) Throw an
	 * Exception if either id is null or they are equal. 2) Get the existing
	 * EquivalenceClasses for each id. 3) Take action based on the following
	 * four cases: - Case 1: if both are null, we create a new EquivalenceClass
	 * containing just those two records and add it to the map - Case 2: if
	 * either one is null, just add the dangling id to the existing equivalence
	 * class (changing the identifier of the equivalence class if necessary) -
	 * Case 3: if the EC's are the same, then we don't need to do anything
	 * because that link is already in the EC - Case 4: if they are different,
	 * then we must combine them, making sure that there was previously no
	 * overlap (indicates a bug elsewhere), and re-put the new EC value in the
	 * ecMap for each of the pairs in the combined EquivalenceClass.
	 */
	public void addLink(Long objId1, Long objId2) {
		if (objId1 == null || objId2 == null || objId1.equals(objId2)) {
			throw new IllegalArgumentException();
		}

		EquivalenceClass ec1 = getEquivalenceClass(objId1);
		EquivalenceClass ec2 = getEquivalenceClass(objId2);

		if (ec1 == null && ec2 == null) { // Case 1
			createNewEquivalenceClass(objId1, objId2);
		} else if (ec1 == null) { // Case 2
			addToEquivalenceClass(objId1, ec2);
		} else if (ec2 == null) { // Case 2 also
			addToEquivalenceClass(objId2, ec1);
		} else if (ec1 == ec2) { // Case 3
			// nothing to do.
		} else { // Case 4
			combineEquivalenceClasses(ec1, ec2);
		}
	}

	public SortedSet<EquivalenceClass> getEquivalenceClasses() {
		return new TreeSet<EquivalenceClass>(ecMap.values());
	}

	public Map<Long, Long> getIdToEquivalenceClassMap() {
		Map<Long, Long> ret = new HashMap<>();

		for (Entry<Long, EquivalenceClass> entry : ecMap.entrySet()) {
			Long recordId = entry.getKey();
			EquivalenceClass ec = (EquivalenceClass) entry.getValue();
			Long ecId = ec.getMemberIds().first();

			ret.put(recordId, ecId);
		}

		return ret;
	}

	private EquivalenceClass getEquivalenceClass(Object objId) {
		return (EquivalenceClass) ecMap.get(objId);
	}

	private void createNewEquivalenceClass(Long objId1, Long objId2) {
		EquivalenceClass ec = new EquivalenceClass();
		ec.addMember(objId1);
		ec.addMember(objId2);

		ecMap.put(objId1, ec);
		ecMap.put(objId2, ec);
	}

	private void addToEquivalenceClass(Long objId, EquivalenceClass ec) {
		ec.addMember(objId);
		ecMap.put(objId, ec);
	}

	private void combineEquivalenceClasses(EquivalenceClass ec1,
			EquivalenceClass ec2) {
		final int preSize = ec1.size() + ec2.size();
		EquivalenceClass ec = new EquivalenceClass();
		for (Long objId : ec1.getMemberIds()) {
			ec.addMember(objId);
			ecMap.put(objId, ec);
		}
		for (Long objId : ec2.getMemberIds()) {
			ec.addMember(objId);
			ecMap.put(objId, ec);
		}
		if (preSize != ec.size()) {
			throw new IllegalStateException(
					"Equivalence classes were not disjoint!");
		}
	}

}
