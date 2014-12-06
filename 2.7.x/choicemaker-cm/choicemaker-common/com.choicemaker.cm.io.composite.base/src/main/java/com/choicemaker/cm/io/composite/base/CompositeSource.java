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
package com.choicemaker.cm.io.composite.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Sink;
import com.choicemaker.cm.core.Source;
import com.choicemaker.cm.core.util.NameUtils;
import com.choicemaker.util.IntArrayList;

/**
 * Collection of other sources.
 * Abstract base class for specific sources. Specific sources exist because
 * Java does not support parametric polymorphism in JDK &lt; 1.5.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 08:56:16 $
 */
public abstract class CompositeSource implements Source {
	protected ArrayList sources;
	protected IntArrayList sourceSizes;
	protected ArrayList saveAsRelative;
	protected int curSource;
	private String name;
	protected String fileName;
	private ImmutableProbabilityModel model;

	public CompositeSource() {
		sources = new ArrayList();
		sourceSizes = new IntArrayList();
		saveAsRelative = new ArrayList();
	}

	/**
	 * Get the value of name.
	 * @return value of name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the value of name.
	 * @param v  Value to assign to name.
	 */
	public void setName(String v) {
		this.name = v;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
		setName(NameUtils.getNameFromFilePath(fileName));
	}

	public String getFileName() {
		return fileName;
	}

	public void open() throws java.io.IOException {
		try {
			curSource = 0;
			if (sources.size() > 0) {
				Source s = ((Source) sources.get(curSource));
				s.open();
				nextValid();
			}
		} catch (Exception ex) {
			throw new IOException("Error reading file referenced by " + fileName, ex);
		}
	}

	public void close() throws java.io.IOException {
		if (curSource < sources.size()) {
			((Source) sources.get(curSource)).close();
		}
	}

	public boolean hasNext() throws IOException {
		if (curSource < sources.size()) {
			return ((Source) sources.get(curSource)).hasNext();
		} else {
			return false;
		}
	}

	/**
	 * Returns the current source from the collection of sources.
	 *
	 * @return The current source.
	 */
	protected Source getCurSource() {
		return ((Source) sources.get(curSource));
	}

	/**
	 * Advances to the next valid element--possibly closing the current
	 * record source and opening the next.
	 *
	 * @throws  IOException if the source throws one.
	 */
	protected void nextValid() throws IOException {
		Source s = ((Source) sources.get(curSource));
		while (s != null && !s.hasNext()) {
			s.close();
			++curSource;
			if (curSource < sources.size()) {
				s = ((Source) sources.get(curSource));
				s.open();
			} else {
				if (s != null) {
					s.close();
				}
				s = null;
			}
		}
		if (s != null) {
			sourceSizes.set(curSource, sourceSizes.get(curSource) + 1);
		}
	}

	/**
	 * Add a source at the end of the collection, saving
	 * its filename as relative or absolute, depending on the
	 * flag.
	 */
	protected void add(Source s, boolean saveAsRel) {
		if (s != null) {
			s.setModel(model);
			sources.add(s);
			sourceSizes.add(0);
			saveAsRelative.add(new Boolean(saveAsRel));
		} else {
			throw new NullPointerException();
		}
	}

	/**
	 * Add a source at the end of the collection,
	 * and by default, save its filename as an absolute path.
	 */
	protected void add(Source s) {
		add(s, false);
	}

	protected Source getSourceAtIndex(int i) {
		return (Source) sources.get(i);
	}

	public boolean saveAsRelative(int i) {
		return ((Boolean) saveAsRelative.get(i)).booleanValue();
	}

	/**
	 * Returns the number of sources in the collection.
	 *
	 * @return  The number of sources in the collection.
	 */
	public int getNumSources() {
		return sources.size();
	}

	/**
	 * Removes the source at the specified position in the list.
	 *
	 * @param   index The index.
	 * @throws  IndexOutOfBoundsException if the index is out of the range
	 *            <code>(index < 0 || index >= getNumSources())</code>.
	 */
	public void remove(int index) {
		sources.remove(index);
		sourceSizes.remove(index);
		saveAsRelative.remove(index);
	}

	public void removeAll() {
		sources.clear();
		sourceSizes.clear();
		saveAsRelative.clear();
	}

	public ImmutableProbabilityModel getModel() {
		return model;
	}

	public void setModel(ImmutableProbabilityModel m) {
		model = m;
		Iterator i = sources.iterator();
		while (i.hasNext()) {
			((Source) i.next()).setModel(m);
		}
	}

	public String[] getConstituentSourcesName() {
		String[] res = new String[sources.size()];
		for (int i = 0; i < sources.size(); ++i) {
			res[i] = ((Source) sources.get(i)).getName();
		}
		return res;
	}

	public boolean hasSink() {
		return false;
	}

	public Sink getSink() {
		return null;
	}

	public int[] getSizes() {
		return sourceSizes.toArray();
	}
}
