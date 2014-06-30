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
package com.choicemaker.cm.io.blocking.automated.offline.impl;

import java.io.EOFException;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Stack;

import com.choicemaker.cm.core.base.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.ISuffixTreeSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.SuffixTreeNode;

/**
 * @author pcheung
 *
 */
public class SuffixTreeSource extends BaseFileSource implements ISuffixTreeSource {

	private SuffixTreeNode nextTree = null;


	/** This contructor creates a string source with the given name.
	 * 
	 * @param fileName
	 */
	public SuffixTreeSource (String fileName) {
		init (fileName, Constants.STRING);
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISuffixTreeSource#getNext()
	 */
	public SuffixTreeNode getNext() throws BlockingException {
		if (this.nextTree == null) {
			try {
				this.nextTree = readNext();
			} catch (EOFException x) {
				throw new NoSuchElementException(
					"EOFException: " + x.getMessage());
			} catch (IOException x) {
				throw new NoSuchElementException(
					"BlockingException: " + x.getMessage());
			}
		}
		SuffixTreeNode retVal = this.nextTree;
		count ++;
		this.nextTree = null;

		return retVal;
	}
	

	private SuffixTreeNode readNext () throws EOFException, IOException {
		SuffixTreeNode ret = null;
		
		String str = br.readLine();

		if (str == null || str.equals("")) throw new EOFException ();
			
		int ind = 0;
			
		ret = SuffixTreeNode.createRootNode();

		int size = str.length();
			
		//setting up the stack
		Stack stack = new Stack ();
		stack.push(ret);
			
		while (ind < size) {
			if (str.charAt(ind) == Constants.OPEN_NODE) {
				int i = getNextMarker (str, ind);
				long id = Long.parseLong( str.substring(ind+1,i));
					
				SuffixTreeNode kid = null;
					
				//use peek here because we don't want to remove from the stack.
				SuffixTreeNode parent = (SuffixTreeNode) stack.peek();
					
				if (str.charAt(i) == Constants.CLOSE_NODE) {
					//leaf
					kid = parent.putChild(id, count);
				} else {
					//has at least 1 child
					kid = parent.putChild(id);
				} 
					
				stack.push(kid);
					
				ind = i;
			} else if (str.charAt(ind) == Constants.CLOSE_NODE) {
				stack.pop();
				ind ++;
			}
		}
			
		//at the end, there should only be the root on the stack.
		if (stack.size() != 1) throw new IOException ("Could not parse this tree: " + str);
		
		return ret;
	}	


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#hasNext()
	 */
	public boolean hasNext() throws BlockingException {
		if (this.nextTree == null) {
			try {
				this.nextTree = readNext();
			} catch (EOFException x) {
				this.nextTree = null;
			} catch (IOException x) {
				throw new BlockingException (x.toString());
			}
		}
		return this.nextTree != null;
	}
	
	
	/** This method returns the location of the next OPEN_NODE or CLOSE_NODE starting from index from+1.
	 * 
	 * @param str
	 * @param from
	 * @return
	 */
	private int getNextMarker (String str, int from) {
		boolean found = false;
		int i = from + 1;
		int size = str.length();
		while (!found && i < size) {
			if (str.charAt(i) == Constants.OPEN_NODE || str.charAt(i) == Constants.CLOSE_NODE) found = true;
			else i ++;
		}
		return i;
	}
	

}
