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

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.BlockSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.ComparisonArray;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IIDSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDTranslator2;
import com.choicemaker.cm.io.blocking.automated.offline.core.ITransformer;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;
import com.choicemaker.util.LongArrayList;

/**
 * This object takes an array or tree of internal id and transforms them back to stage and master IDs.
 * 
 * 
 * @author pcheung
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Transformer implements ITransformer{
	
	private IRecordIDTranslator2 translator;
	private IComparisonArraySinkSourceFactory cFactory;
	private IComparisonArraySink cOut = null ;


	public Transformer (IRecordIDTranslator2 translator, IComparisonArraySinkSourceFactory cFactory) 
		throws BlockingException {
			
		this.translator = translator;
		this.cFactory = cFactory;
	}
	
	
	public void init() throws BlockingException {
		//initialize the translator
		translator.initReverseTranslation();
		
		cOut = cFactory.getNextSink();
		cOut.open();
	}
	
	
	public int getSplitIndex () {
		return translator.getSplitIndex(); 
	}
	
	
	public void useNextSink () throws BlockingException {
		cOut.close();
		cOut = cFactory.getNextSink();
		cOut.open();
	}
	
	
	public void transform (IIDSet bs) throws BlockingException {
		if (bs instanceof BlockSet) { 
			transformBlockSet ((BlockSet) bs);
		} else {
			throw new BlockingException ("Expecting instanceof BlockSet, but got "+ bs.getClass());
		}
	}
	
	
	private void transformBlockSet (BlockSet bs) throws BlockingException {
		LongArrayList block = bs.getRecordIDs();
		
		//set up comparison group
		ArrayList stage = new ArrayList ();
		ArrayList master = new ArrayList ();
		RECORD_ID_TYPE stageType = null;
		RECORD_ID_TYPE masterType = null;
			
		//add to the set of distinct record ids
		for (int i=0; i< block.size(); i++) {
			//get the original record id
			Comparable comp = translator.reverseLookup((int) block.get(i) );
				
			if (translator.getSplitIndex() == 0) {
				//only staging record source
					
				if (stage.size()== 0) stageType = RECORD_ID_TYPE.fromInstance(comp);
				stage.add(comp);

			} else {
				//two record sources
				if (block.get(i) < translator.getSplitIndex()) {
					//stage
					if (stage.size()== 0) stageType = RECORD_ID_TYPE.fromInstance(comp);
					stage.add(comp);
				} else {
					//master
					if (master.size()== 0) masterType = RECORD_ID_TYPE.fromInstance(comp);
					master.add(comp);
				}
			}
		}
		ComparisonArray cg = new ComparisonArray (stage, master, stageType, masterType);
		cOut.writeComparisonArray (cg); 
		
	}
	
	
	
	public void close () throws BlockingException {
		cOut.close();
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ITransformer#cleanUp()
	 */
	public void cleanUp() throws BlockingException {
		cOut.remove();
	}
	
}
