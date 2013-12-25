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
package com.choicemaker.cm.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/24 18:26:12 $
 */
public class RecordBinder implements RecordSource {
	
	public static List getList(RecordSource rs) throws IOException {
		rs.open();
		List list = new ArrayList();
		while (rs.hasNext()) {
			list.add(rs.getNext());
		}
		rs.close();
		
		return list;
	}

	public static void store(Collection l, RecordSink snk) throws IOException {
		snk.open();
		Iterator i = l.iterator();
		while (i.hasNext()) {
			snk.put((Record) i.next());
		}
		snk.close();
	}
	
	private Collection collection;
	private Iterator iterator;
	private String name;
	private ImmutableProbabilityModel probabilityModel;
	private int startPosition;
	
	public RecordBinder(Collection collection) {
		this.collection = collection;
	}
	
	public RecordBinder(List collection, int startPosition) {
		this.collection = collection;
		this.startPosition = startPosition;
	}

	/**
	 * @see com.choicemaker.cm.core.RecordSource#getNext()
	 */
	public Record getNext() throws IOException {
		return (Record)iterator.next();
	}

	/**
	 * @see com.choicemaker.cm.core.Source#open()
	 */
	public void open() throws IOException {
		if(startPosition == 0) {
			iterator = collection.iterator();
		} else {
			iterator = ((List)collection).listIterator(startPosition);
		}
	}

	/**
	 * @see com.choicemaker.cm.core.Source#close()
	 */
	public void close() throws IOException {
		iterator = null;
	}

	/**
	 * @see com.choicemaker.cm.core.Source#hasNext()
	 */
	public boolean hasNext() {
		return iterator.hasNext();
	}

	/**
	 * @see com.choicemaker.cm.core.Source#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see com.choicemaker.cm.core.Source#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @see com.choicemaker.cm.core.Source#getModel()
	 */
	public ImmutableProbabilityModel getModel() {
		return probabilityModel;
	}

	/**
	 * @see com.choicemaker.cm.core.Source#setModel(com.choicemaker.cm.core.ProbabilityModel)
	 */
	public void setModel(ImmutableProbabilityModel probabilityModel) {
		this.probabilityModel = probabilityModel;
	}

	/**
	 * @see com.choicemaker.cm.core.Source#hasSink()
	 */
	public boolean hasSink() {
		return false;
	}

	/**
	 * @see com.choicemaker.cm.core.Source#getSink()
	 */
	public Sink getSink() {
		return null;
	}

	/**
	 * @see com.choicemaker.cm.core.Source#getFileName()
	 */
	public String getFileName() {
		return null;
	}

	/**
	 * NOP for now
	 * @see com.choicemaker.cm.core.Sink#flush()
	 */
	public void flush() {
	}

}
