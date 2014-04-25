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
package com.choicemaker.cm.modelmaker.gui.matcher;

import java.awt.FlowLayout;

import javax.swing.JLabel;

import com.choicemaker.cm.analyzer.matcher.AllBlocker;
import com.choicemaker.cm.core.blocking.InMemoryBlocker;

/**
 * Description
 *
 * @author  Martin Buechi
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:09 $
 */
public class AllBlockerDialogPlugin extends MatchDialogBlockerPlugin {
	private static final long serialVersionUID = 1L;

	AllBlockerDialogPlugin() {
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(new JLabel("Returns all records from small source."));
	}

	/**
	 * @see com.choicemaker.cm.train.matcher.MatchDialogBlockerPlugin#getBlocker()
	 */
	public InMemoryBlocker getBlocker() {
		return new AllBlocker();
	}

}
