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
package com.choicemaker.cm.module.swing;

import javax.swing.JPanel;

/**
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/27 19:27:57 $
 */
public class DefaultPanelControl implements IPanelControl {
	
	private JPanel managedPanel = new JPanel();

	/** Manages a single panel */
	public DefaultPanelControl() {
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.module.swing.IPanelControl#getManagedPanel()
	 */
	public JPanel getManagedPanel() {
		return this.managedPanel;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.module.swing.IPanelControl#setManagedPanel(javax.swing.JPanel)
	 */
	public void setManagedPanel(JPanel panel) {
		// Precondition: the panel should not be null, otherwise
		// this module would become disconnected from any visible
		// UI. If you really want a disconnected module, you are on your own.
		if (panel == null) {
			throw new IllegalArgumentException("null panel");
		}
		this.managedPanel = panel;
	}

}

