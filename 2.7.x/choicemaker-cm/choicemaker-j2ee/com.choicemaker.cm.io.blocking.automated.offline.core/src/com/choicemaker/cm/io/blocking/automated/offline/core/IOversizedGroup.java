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

import com.choicemaker.cm.core.base.BlockingException;

/**
 * This interface handles the writing of oversized blocks.  It manages where a oversized block is written to
 * based on the maximum column id of the oversized block.
 * 
 * @author pcheung
 *
 */
public interface IOversizedGroup {
	
	/** This method writes the oversized block set to the appropiate file/table based on the maximum
	 * column id of the block set.
	 * 
	 * @param bs
	 * @throws IOException
	 */
	public void writeBlock (BlockSet bs) throws BlockingException;
	
	
	/**
	 * Initialization method to open all sinks.
	 *
	 */
	public void openAllSinks () throws BlockingException;
	
	
	/** This method opens the sinks for append.  It is used in recovery.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void appendAllSinks() throws BlockingException;
	
	
	/** This method returns the correct set of oversized block set with the same maxColumn.
	 * 
	 * @param maxColumn - maximum column id of the bloct sets
	 */
	public IBlockSource getSource (int maxColumn) throws BlockingException;
	
	
	/** This method close all the sinks
	 * 
	 * @throws IOException
	 */
	public void closeAllSinks () throws BlockingException;
	
	
	/** The cleanUp method removes the files that are no longer in use.
	 * 
	 *
	 */
	public void cleanUp () throws BlockingException;

}
