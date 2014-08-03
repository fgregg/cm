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

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IIDSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IIDSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.ISuffixTreeSource;

/**
 * @author pcheung
 *
 */
public class IDTreeSetSource implements IIDSetSource {
	
	private ISuffixTreeSource bSource;
//	private IIDSet next;
	
	public IDTreeSetSource (ISuffixTreeSource bSource) {
		this.bSource = bSource;
	}
	

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IIDSetSource#getNext()
	 */
	public IIDSet getNext() throws BlockingException {
		return bSource.getNext();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#exists()
	 */
	public boolean exists() {
		return bSource.exists();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#open()
	 */
	public void open() throws BlockingException {
		bSource.open();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#hasNext()
	 */
	public boolean hasNext() throws BlockingException {
		return bSource.hasNext();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#close()
	 */
	public void close() throws BlockingException {
		bSource.close();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#getInfo()
	 */
	public String getInfo() {
		return bSource.getInfo();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#remove()
	 */
	public void remove() throws BlockingException {
		bSource.remove();
	}

}
