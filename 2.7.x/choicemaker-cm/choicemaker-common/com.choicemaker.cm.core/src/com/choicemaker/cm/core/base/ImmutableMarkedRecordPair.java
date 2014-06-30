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

import java.util.Date;

/**
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 17:00:57 $
 */
public interface ImmutableMarkedRecordPair extends ImmutableRecordPair {

	/** Get a comment. */
	public abstract String getComment();

	/** Get the date the decision was made or last revised. */
	public abstract Date getDateMarked();
	/**
	 * The <code>Decision</code> that was marked by a human reviewer about 
	 * whether this pair matches or not. This distinct from the {@link IRecordPair#getCmDecision Decision}
	 * that ChoiceMaker assigns.
	 */
	public abstract Decision getMarkedDecision();

	/** Get the source of this record. */
	public abstract String getSource();

	/** Get the user who made the decision/last revised it. */
	public abstract String getUser();

}
