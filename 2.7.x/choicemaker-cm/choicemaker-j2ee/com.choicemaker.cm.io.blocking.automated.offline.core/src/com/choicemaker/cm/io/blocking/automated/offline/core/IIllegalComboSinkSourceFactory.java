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
package com.choicemaker.cm.io.blocking.automated.offline.core;

import com.choicemaker.cm.core.base.BlockingException;

/**
 * This factory handles getting IIllegalComboSink and IIllegalComboSource.
 * 
 * @author pcheung
 *
 */
public interface IIllegalComboSinkSourceFactory {


	/** Gets the next IIllegalComboSink in the sequence. */
	public IIllegalComboSink getNextSink () throws BlockingException;
	
	/** Gets the next IIllegalComboSource in the sequence. */
	public IIllegalComboSource getNextSource () throws BlockingException;
	
	/** Gets the number of sequence sinks created. */
	public int getNumSink ();
	
	/** Gets the number of sequence sources created. */
	public int getNumSource ();
	
	/** Creates an IIllegalComboSource for an existing IIllegalComboSink. */
	public IIllegalComboSource getSource (IIllegalComboSink sink) throws BlockingException;
	
	/** Creates an IIllegalComboSink for an existing IIllegalComboSource. */
	public IIllegalComboSink getSink (IIllegalComboSource source) throws BlockingException;

	/** Removes a given IIllegalComboSink. */
	public void removeSink (IIllegalComboSink sink) throws BlockingException;

	/** Removes a given IIllegalComboSource. */
	public void removeSource (IIllegalComboSource source) throws BlockingException;
	

}
