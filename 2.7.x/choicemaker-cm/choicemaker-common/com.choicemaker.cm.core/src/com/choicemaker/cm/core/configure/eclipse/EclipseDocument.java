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
package com.choicemaker.cm.core.configure.eclipse;

import org.eclipse.core.runtime.IConfigurationElement;

import com.choicemaker.cm.core.configure.IDocument;
import com.choicemaker.cm.core.configure.IElement;
import com.choicemaker.cm.core.util.Precondition;

/**
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 18:04:31 $
 */
public class EclipseDocument implements IDocument {
	
	private final IConfigurationElement root;

	public EclipseDocument(IConfigurationElement root) {
		Precondition.assertNonNullArgument("null configuration element",root);
		this.root = root;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.configure.IDocument#getConfigurableElement()
	 */
	public IElement getConfigurableElement() {
		IElement retVal = new EclipseElement(root);
		return retVal;
	}

}
