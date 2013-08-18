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

/**
 * A MergeGroup contains a list of records that should be merged together.
 * 
 * @deprecated
 * 
 * @author pcheung
 *
 */
public interface IMergeGroup {

	/** These are the staging record ids in this group that should be merged together. 
	 * 
	 * @return long [] - list of staging record ids that should be merged.
	 */
	public long [] getStagingRecords ();


	/** These are the master record ids in this group that should be merged together. 
	 * 
	 * @return long [] - list of master record ids that should be merged.
	 */
	public long [] getMasterRecords ();


	/** This returns true if the new record should be held for human review.
	 * 
	 * @return boolean - true if the new merged record should be held.
	 */
	public boolean isHold ();
	
	
	/** These are the staging record ids that are held against members of this merge group.
	 * 
	 * @return long [] - list of staging record ids that are held against.
	 */
	public long [] getStagingHolds ();
	
	
	/** These are the master record ids that are held against members of this merge group.
	 * 
	 * @return long [] - list of master record ids that are held against.
	 */
	public long [] getMasterHolds ();
	
}
