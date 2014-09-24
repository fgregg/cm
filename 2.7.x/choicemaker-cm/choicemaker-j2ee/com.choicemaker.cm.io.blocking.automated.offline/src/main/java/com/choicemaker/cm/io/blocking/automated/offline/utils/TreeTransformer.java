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
package com.choicemaker.cm.io.blocking.automated.offline.utils;

import java.util.ArrayList;
import java.util.List;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.ComparisonTreeNode;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IIDSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDTranslator2;
import com.choicemaker.cm.io.blocking.automated.offline.core.ITransformer;
import com.choicemaker.cm.io.blocking.automated.offline.core.SuffixTreeNode;

/**
 * This object takes a tree of internal ids and transform it into a tree of stage and master
 * ids.
 * 
 * @author pcheung
 *
 */
public class TreeTransformer implements ITransformer {

	private IRecordIDTranslator2 translator;
	private IComparisonTreeSinkSourceFactory cFactory;
	private IComparisonTreeSink cOut = null ;


	/** This constructor takes in the translator and comparison tree sink source factory.
	 * 
	 * @param translator
	 * @param cFactory
	 * @throws BlockingException
	 */
	public TreeTransformer (IRecordIDTranslator2 translator, IComparisonTreeSinkSourceFactory cFactory) 
		throws BlockingException {
			
		this.translator = translator;
		this.cFactory = cFactory;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ITransformer#init()
	 */
	public void init() throws BlockingException {
		//initialize the translator
		translator.initReverseTranslation();
		
		cOut = cFactory.getNextSink();
		cOut.open();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ITransformer#getSplitIndex()
	 */
	public int getSplitIndex() {
		return translator.getSplitIndex(); 
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ITransformer#useNextSink()
	 */
	public void useNextSink() throws BlockingException {
		cOut.close();
		cOut = cFactory.getNextSink();
		cOut.open();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ITransformer#close()
	 */
	public void close() throws BlockingException {
		cOut.close();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ITransformer#transform(com.choicemaker.cm.io.blocking.automated.offline.core.IIDSet)
	 */
	public void transform(IIDSet bs) throws BlockingException {
		if (bs instanceof SuffixTreeNode) { 
			transformTree ((SuffixTreeNode) bs);
		} else {
			throw new BlockingException ("Expecting instanceof SuffixTreeNode, but got " + bs.getClass());
		}
	}


	/** This method transforms the internal id tree to a stage and master id tree.
	 * The parameter node should be a root node with recordId = -1.
	 * 
	 * @param node
	 */
	private void transformTree(SuffixTreeNode node) throws BlockingException {
		ComparisonTreeNode tree = ComparisonTreeNode.createRootNode();
		
		copyTree (node, tree);
		
		//don't write the root.
		List al = tree.getAllChildren();
		ComparisonTreeNode kid = (ComparisonTreeNode) al.get(0);
		cOut.writeComparisonTree(kid);
	}
	
	
	/** This method copies node1 into node2.
	 * 
	 * @param node1
	 * @param node2
	 */
	private void copyTree (SuffixTreeNode node1, ComparisonTreeNode node2) {
		List kids = node1.getAllChildren();
		for (int i=0; i<kids.size(); i++) {
			SuffixTreeNode kid = (SuffixTreeNode) kids.get(i);
			int id = (int) kid.getRecordId();
			Comparable c = translator.reverseLookup(id);

			char stageOrMaster = ComparisonTreeNode.STAGE;
			if (translator.getSplitIndex() > 0) {
				//two record sources
				if (id >= translator.getSplitIndex()) stageOrMaster = ComparisonTreeNode.MASTER;
			}
			
			if (kid.hasBlockingSetId()) {
				//leaf		
				node2.putChild(c, stageOrMaster, kid.getBlockingSetId());
			} else {
				//node
				ComparisonTreeNode kid2 = node2.putChild(c, stageOrMaster);
				
				//call this recursively
				copyTree (kid, kid2);
			}
			
		}
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ITransformer#cleanUp()
	 */
	public void cleanUp() throws BlockingException {
		cOut.remove();
	}
	
	

}
