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
public interface IMarkedRecordPair extends IRecordPair, ImmutableMarkedRecordPair {
	/**
	 * The mark a pair as to whether it matches or not. Marking is performed
	 * by human reviewers, as opposed to the decision
	 * {@link IRecordPair#setCmDecision assignments} made by
	 * ChoiceMaker.
	 */
	public abstract void setMarkedDecision(Decision decision);
	/**
	 * Set the date the decision was made or last revised.
	 * This field is <em>not</em> updated automatically
	 * when the decision field is modified.
	 */
	public abstract void setDateMarked(Date date);
	/** Set the user who made the decision/last revised it. */
	public abstract void setUser(String user);
	/** Set the source of this record. */
	public abstract void setSource(String src);
	/** Set a comment. */
	public abstract void setComment(String comment);
}
