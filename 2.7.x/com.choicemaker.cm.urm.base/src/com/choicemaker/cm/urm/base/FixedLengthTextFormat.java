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
package com.choicemaker.cm.urm.base;

/**
 * Fixed length text format
 * <p>  
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 12:16:24 PM
 * @see
 */
public class FixedLengthTextFormat implements ITextFormat {
	
	/** As of 2010-11-12 */
	static final long serialVersionUID = -7447824055947924959L;

	public FixedLengthTextFormat() {
		super();
	}

	public void accept(ITextFormatVisitor ext) {
		ext.visit(this);
	}
}
