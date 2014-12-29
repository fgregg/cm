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

import java.util.List;

import com.choicemaker.cm.core.BlockingException;

/**
 * This interface handles reading of oversized blocks.  An oversized block is an BlockSet object.  It has
 * field id, field value, and a list of recIDs.
 *
 * @author pcheung
 *
 */
public interface IBlockSource extends ISource<BlockSet> {
	
	/** This method returns true if there is another block that is blocked by the
	 * given number of fields and the given maximum column.
	 * 
	 * @param fields - number of blocking fields this block needs to have
	 * @param col - maximum column id that this block needs to have
	 * @return - an ArrayList of BlockSet with each containing the oversized blocking set
	 */
	public boolean hasNext (int fields, int col) throws BlockingException;
	
	
	/** This method gets the oversized blocks written by writeOversizedInt.
	 * 
	 * @param fields - number of blocking fields this need to have
	 * @param col - maximum column id that this needs to have
	 * @return - an ArrayList of BlockSet with each containing the oversized blocking set
	 */
	public List<BlockSet> readOversizedInt (int fields, int col) throws BlockingException;
	
	
	/** Returns the number of blocks read so far */
	public int getCount ();
	
	
	/** Skips n BlockSets. */
	public void skip (int n) throws BlockingException;

}
