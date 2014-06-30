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
package com.choicemaker.cm.io.blocking.automated.offline.services;

import com.choicemaker.cm.core.base.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.ISuffixTreeSource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparablePairSink;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparablePairSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableSTSource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.SimpleControl;

/**
 * This object takes in a source of SuffixTrees, creates pairs from the trees, dedups
 * the trees, and write the deduped pairs out to file.
 * 
 * It sets us the correct interfaces and relies on GenericDedpService to do the work.
 * 
 * @author pcheung
 *
 */
public class PairDedupService {
	
	private ISuffixTreeSource source;
	private IPairIDSinkSourceFactory factory;
	private IPairIDSink sink;
	private int max;
	
	
	private int numBefore = 0; //this counts the number of input matches
	private int numAfter = 0; //this counts the number of output matches
	
	private long time; //this keeps track of time


	/** This constructor takes these inputs:
	 * 1.	A suffix tree source
	 * 2.	A factory to create temporary pair sources and sinks
	 * 3.	An output pair sink to store the dedup pairs.
	 * 4.	Maximum number of pairs to fit into memory at once.
	 * 
	 * @param source
	 * @param factory
	 * @param sink
	 * @param max
	 */
	public PairDedupService (ISuffixTreeSource source, IPairIDSinkSourceFactory factory,
		IPairIDSink sink, int max) {
		this.source = source;
		this.factory = factory;
		this.sink = sink;
		this.max = max;
	}
	
	
	public void run() throws BlockingException {
		ComparableSTSource stSource = new ComparableSTSource(source);
		ComparablePairSink pSink = new ComparablePairSink (sink);
		ComparablePairSinkSourceFactory pFactory = new
			ComparablePairSinkSourceFactory (factory);
			
		SimpleControl control = new SimpleControl ();
		GenericDedupService service = new GenericDedupService (stSource, pSink, pFactory, max, control);
		service.runDedup();
		
		time = service.getTime();
		numBefore = service.getNumBefore();
		numAfter = service.getNumAfter();
	}


	public int getNumBefore () {return numBefore;}
	public int getNumAfter () {return numAfter;}
	public long getTime () {return time;}


}
