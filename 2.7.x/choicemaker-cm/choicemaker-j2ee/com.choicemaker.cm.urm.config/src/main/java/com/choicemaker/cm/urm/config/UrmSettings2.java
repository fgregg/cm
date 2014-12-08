/*
 * Copyright (c) 2011 Rick Hall and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Rick Hall - initial API and implementation
 */
package com.choicemaker.cm.urm.config;

/**
 * Extends the UrmSettings interface to add new properties. Ideally, the
 * UrmSettings interface would be broken up into functional subgroups:
 * pair-wise matching, group-wise matching (a.k.a. transitivity analysis),
 * realtime matching, offline matching and so on.
 * 
 * @author rphall
 */
public interface UrmSettings2 extends UrmSettings {

	/**
	 * Returns whether offline matching is actually linkage of two master
	 * databases. Master databases are presumed to be de-duplicated, so
	 * master-to-master matching does not look for duplicates within either
	 * individual database, but rather looks for common records between both
	 * databases.<br/>
	 * This method should never return <code>null</code>, but rather
	 * <code>false</code> as a default if this property hasn't been explicitly
	 * set.<br/>
	 * The value returned by this accessor should be the complement of the value
	 * returned by {@link #isStagingToMasterMatching()}.
	 * 
	 * @return true for master-to-master linkage, false for stage-to-master
	 *         matching.
	 */
	Boolean isMasterToMasterLinkage();

	/**
	 * Returns whether offline matching is between a staging database and a
	 * master database. Master databases are presumed to be de-duplicated,
	 * whereas staging databases are presumed to have internal duplicates.
	 * Staging-to-master matching looks for duplicates within the staging
	 * database as well as common records between the staging and master
	 * database. It does not look for duplicates within the master database.<br/>
	 * This method should never return <code>null</code>, but rather
	 * <code>true</code> as a default if this property hasn't been explicitly
	 * set.<br/>
	 * The value returned by this accessor should be the complement of the value
	 * returned by {@link #isMasterToMasterLinkage()}.
	 * 
	 * @return true for master-to-master linkage, false for stage-to-master
	 *         matching.
	 */
	Boolean isStagingToMasterMatching();

	/**
	 * This operation should be equivalent to invoking
	 * <code>setStagingToMaster(!boolean1)</code>. Null arguments to this method
	 * should be treated as <code>false</code>.
	 * 
	 * @param boolean1
	 *            true to set master-to-master matching.
	 */
	void setMasterToMasterLinkage(Boolean boolean1);

	/**
	 * This operation should be equivalent to invoking
	 * <code>setMasterToMaster(!boolean1)</code> Null arguments to this method
	 * should be treated as <code>true</code>.
	 * 
	 * @param boolean1
	 *            true to set staging-to-master matching.
	 */
	void setStagingToMasterMatching(Boolean boolean1);

}
