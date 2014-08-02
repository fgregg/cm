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
 * Panels are often displayed in tabbed panes, in which case they
 * need to supply at least a unique name (within the pane) for a
 * tab. The name is visible to the user, so it is should be maintained
 * in a property file.
 * @author rphall
 */
public abstract class AbstractTabbedPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public abstract String getTabName();
	// public abstract void setVisible(boolean b);
}

