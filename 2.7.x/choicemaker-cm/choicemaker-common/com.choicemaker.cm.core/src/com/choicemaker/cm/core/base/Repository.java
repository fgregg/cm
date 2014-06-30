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
package com.choicemaker.cm.core.base;

import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

import com.choicemaker.cm.core.train.Trainer;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/24 20:53:30 $
 */
public class Repository {
	protected Source source;
	protected Trainer trainer;
	protected List list;
	protected boolean includeHolds;
	protected WeakHashMap listenerList = new WeakHashMap();

	public Repository(Source source, Trainer trainer) {
		this.source = source;
		this.trainer = trainer;
	}
	
	public void addRepositoryChangeListener(RepositoryChangeListener l) {
		listenerList.put(l, null);
	}

	public void removeRepositoryChangeListener(RepositoryChangeListener l) {
		listenerList.remove(l);
	}

	public void fireRecordDataChanged(Object o) {
		if(trainer != null && o instanceof ImmutableMarkedRecordPair) {
			trainer.computeProbability((MutableMarkedRecordPair)o);
		}
		fireRepositoryChange(new RepositoryChangeEvent(this, o));
	}

	private void fireRepositoryChange(RepositoryChangeEvent evt) {
		for (Iterator i = listenerList.keySet().iterator(); i.hasNext(); ) {
			switch (evt.getID()) {
				case RepositoryChangeEvent.SET_CHANGED :
					 ((RepositoryChangeListener) i.next()).setChanged(evt);
					break;
				case RepositoryChangeEvent.RECORD_DATA_CHANGED :
					 ((RepositoryChangeListener) i.next()).recordDataChanged(evt);
					break;
				case RepositoryChangeEvent.MARKUP_DATA_CHANGED :
					 ((RepositoryChangeListener) i.next()).markupDataChanged(evt);
					break;
			}
		}
	}
	/**
	 * Returns the includeHolds.
	 * @return boolean
	 */
	public boolean isIncludeHolds() {
		return includeHolds;
	}

	/**
	 * Returns the list.
	 * @return List
	 */
	public List getList() {
		return list;
	}

	/**
	 * Returns the source.
	 * @return Source
	 */
	public Source getSource() {
		return source;
	}

	/**
	 * Returns the trainer.
	 * @return Trainer
	 */
	public Trainer getTrainer() {
		return trainer;
	}

	/**
	 * Sets the includeHolds.
	 * @param includeHolds The includeHolds to set
	 */
	public void setIncludeHolds(boolean includeHolds) {
		this.includeHolds = includeHolds;
	}

	/**
	 * Sets the list.
	 * @param list The list to set
	 */
	public void setList(List list) {
		this.list = list;
	}

	/**
	 * Sets the source.
	 * @param source The source to set
	 */
	public void setSource(Source source) {
		this.source = source;
	}

	/**
	 * Sets the trainer.
	 * @param trainer The trainer to set
	 */
	public void setTrainer(Trainer trainer) {
		this.trainer = trainer;
	}

}
