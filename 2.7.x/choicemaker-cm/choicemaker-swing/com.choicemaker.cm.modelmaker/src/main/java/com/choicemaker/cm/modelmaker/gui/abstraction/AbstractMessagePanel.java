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
package com.choicemaker.cm.modelmaker.gui.abstraction;

import javax.swing.JPanel;
import javax.swing.text.Document;

/**
 * The panel which displays status messages to the user.
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.1 $ $Date: 2010/03/28 17:13:15 $
 */
public abstract class AbstractMessagePanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public AbstractMessagePanel() {
		super();
		buildPanel();
	}
	
	/** Provides the document used to display messages */
	public abstract Document getDocument();

	/** Callback from within constructor */
	protected abstract void buildPanel();

}

