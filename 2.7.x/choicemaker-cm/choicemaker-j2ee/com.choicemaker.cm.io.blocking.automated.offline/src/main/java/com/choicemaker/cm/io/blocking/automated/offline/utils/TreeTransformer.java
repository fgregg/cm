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

import java.util.List;
import java.util.logging.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.ComparisonTreeNode;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IIDSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdTranslator2;
import com.choicemaker.cm.io.blocking.automated.offline.core.ITransformer;
import com.choicemaker.cm.io.blocking.automated.offline.core.SuffixTreeNode;

/**
 * This object takes a tree of internal ids and transform it into a tree of
 * stage and master ids.
 * 
 * @author pcheung
 *
 */
@SuppressWarnings({
		"rawtypes", "unchecked" })
public class TreeTransformer implements ITransformer {

	private static final Logger logger = Logger.getLogger(TreeTransformer.class
			.getName());
	protected static final String SOURCE = TreeTransformer.class
			.getSimpleName();

	private IRecordIdTranslator2 translator;
	private IComparisonTreeSinkSourceFactory cFactory;
	private IComparisonTreeSink cOut = null;

	/**
	 * This constructor takes in the translator and comparison tree sink source
	 * factory.
	 * 
	 * @param translator
	 * @param cFactory
	 * @throws BlockingException
	 */
	public TreeTransformer(IRecordIdTranslator2 translator,
			IComparisonTreeSinkSourceFactory cFactory) throws BlockingException {

		this.translator = translator;
		this.cFactory = cFactory;
	}

	@Override
	public void init() throws BlockingException {
		final String METHOD = "init()";
		logger.entering(SOURCE, METHOD);
		translator.initReverseTranslation();
		cOut = cFactory.getNextSink();
		cOut.open();
		logger.exiting(SOURCE, METHOD);
	}

	@Override
	public int getSplitIndex() {
		final String METHOD = "getSplitIndex()";
		int retVal = translator.getSplitIndex();
		logger.exiting(SOURCE, METHOD, retVal);
		return retVal;
	}

	@Override
	public void useNextSink() throws BlockingException {
		final String METHOD = "useNextSink()";
		logger.entering(SOURCE, METHOD);
		cOut.close();
		cOut = cFactory.getNextSink();
		cOut.open();
		logger.exiting(SOURCE, METHOD);
	}

	@Override
	public void close() throws BlockingException {
		final String METHOD = "close()";
		logger.entering(SOURCE, METHOD);
		cOut.close();
		logger.exiting(SOURCE, METHOD);
	}

	@Override
	public void transform(IIDSet bs) throws BlockingException {
		final String METHOD = "transform(IIDSet)";
		logger.entering(SOURCE, METHOD, bs);
		if (bs instanceof SuffixTreeNode) {
			transformTree((SuffixTreeNode) bs);
		} else {
			throw new BlockingException(
					"Expecting instanceof SuffixTreeNode, but got "
							+ bs.getClass());
		}
		logger.exiting(SOURCE, METHOD);
	}

	/**
	 * This method transforms the internal id tree to a stage and master id
	 * tree. The parameter node should be a root node with recordId = -1.
	 * 
	 * @param node
	 */
	private void transformTree(SuffixTreeNode node) throws BlockingException {
		ComparisonTreeNode tree = ComparisonTreeNode.createRootNode();

		copyTree(node, tree);

		// don't write the root.
		List al = tree.getAllChildren();
		ComparisonTreeNode kid = (ComparisonTreeNode) al.get(0);
		cOut.writeComparisonTree(kid);
	}

	/**
	 * This method copies node1 into node2.
	 * 
	 * @param node1
	 * @param node2
	 */
	private void copyTree(SuffixTreeNode node1, ComparisonTreeNode node2) {
		List kids = node1.getAllChildren();
		for (int i = 0; i < kids.size(); i++) {
			SuffixTreeNode kid = (SuffixTreeNode) kids.get(i);
			int id = (int) kid.getRecordId();
			Comparable c = translator.reverseLookup(id);

			char stageOrMaster = ComparisonTreeNode.STAGE;
			if (translator.getSplitIndex() > 0) {
				// two record sources
				if (id >= translator.getSplitIndex())
					stageOrMaster = ComparisonTreeNode.MASTER;
			}

			if (kid.hasBlockingSetId()) {
				// leaf
				node2.putChild(c, stageOrMaster, kid.getBlockingSetId());
			} else {
				// node
				ComparisonTreeNode kid2 = node2.putChild(c, stageOrMaster);

				// call this recursively
				copyTree(kid, kid2);
			}

		}
	}

	@Override
	public void cleanUp() throws BlockingException {
		final String METHOD = "cleanUp()";
		logger.entering(SOURCE, METHOD);
		cOut.remove();
		logger.exiting(SOURCE, METHOD);
	}

}
