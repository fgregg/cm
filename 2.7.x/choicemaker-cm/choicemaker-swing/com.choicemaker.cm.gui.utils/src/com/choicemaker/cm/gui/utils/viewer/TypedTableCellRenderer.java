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
package com.choicemaker.cm.gui.utils.viewer;

import java.awt.*;

import javax.swing.table.DefaultTableCellRenderer;


/**
 * Rendered used to give different colors to the data in the RecordTables.
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 17:39:36 $
 */
public class TypedTableCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;
	private Color black = new Color(0f, 0f, 0f);
	private Color red = new Color(1f, 0f, 0f);
	private Color invalidDerived = Color.lightGray;
	private Color defaultBackground;

	public TypedTableCellRenderer(Color backgroundColor) {
		defaultBackground = backgroundColor;
	}

	public void setValue(Object value) {
		if (value instanceof TypedValue) {
			TypedValue v = (TypedValue) value;

			if (v.isUnique) {
				setForeground(red);
			} else if (!v.isUnique) {
				setForeground(black);
			}

			if (v.isValid) {
				setBackground(defaultBackground);
			} else {
				setBackground(Color.lightGray);
			}
			if (v.isDerived) {
				setFont(getFont().deriveFont(Font.ITALIC));
			} else {
				setFont(getFont().deriveFont(0));
			}
			setText(v.toString());

		} else {
			setForeground(black);
			setBackground(defaultBackground);
			super.setValue(value);
		}
	}

}
