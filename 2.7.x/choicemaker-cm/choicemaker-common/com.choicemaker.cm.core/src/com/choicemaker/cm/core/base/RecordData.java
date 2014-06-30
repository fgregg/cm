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


/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1 $ $Date: 2010/01/20 15:05:04 $
 */
public abstract class RecordData {
	private Repository repository;

	/**
	 * Returns the repository.
	 * @return Repository
	 */
	public Repository getRepository() {
		return repository;
	}

	/**
	 * Sets the repository.
	 * @param repository The repository to set
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void fireRecordDataChanged() {
		if (repository != null) {
			repository.fireRecordDataChanged(this);
		}
	}

	public void addRepositoryChangeListener(RepositoryChangeListener l) {
		if (repository != null) {
			repository.addRepositoryChangeListener(l);
		}
	}

	public void removeRepositoryChangeListener(RepositoryChangeListener l) {
		if (repository != null) {
			repository.removeRepositoryChangeListener(l);
		}
	}

	public abstract Record getFirstRecord();

	public abstract Record getSecondRecord();
}
