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
package com.choicemaker.cm.modelmaker.gui.renderers;

import java.awt.Color;

import javax.swing.table.DefaultTableCellRenderer;

import com.choicemaker.cm.modelmaker.gui.utils.ValueError;

/**
 * Renderer used to give the ConfusionTable display its different colored text.
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 17:39:59 $
 */
public class ColorTableErrorCellRenderer extends DefaultTableCellRenderer {
	public void setValue(Object value) {
		if (value instanceof ValueError) {
			ValueError v = (ValueError) value;
			setForeground(v.color);
			setText(v.toString());
		} else if (value instanceof String) {
			String v = (String) value;
			setForeground(Color.black);
			setText(v);
		} else {
			super.setValue(value);
		}
	}

}
