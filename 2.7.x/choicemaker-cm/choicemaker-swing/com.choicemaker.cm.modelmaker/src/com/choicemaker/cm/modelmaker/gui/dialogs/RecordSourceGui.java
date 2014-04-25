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
package com.choicemaker.cm.modelmaker.gui.dialogs;

import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:08 $
 */
public abstract class RecordSourceGui extends SourceGui {
	private static final long serialVersionUID = 1L;

	protected RecordSourceGui(ModelMaker parent, String title) {
		super(parent, title);
	}

	/**
	 * @see com.choicemaker.cm.train.gui.dialogs.SourceGui#getCustomFileFilter()
	 */
	protected String getFileTypeDescription() {
		return "Record source descriptors (*.rs)";
	}
	
	protected String getExtension() {
		return Constants.RS_EXTENSION;
	}
}
