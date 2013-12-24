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

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer to implement a table inside a table.
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 17:40:17 $
 */
public class InnerTableCellRenderer extends JTable implements TableCellRenderer {
	protected static Border noFocusBorder;
	public InnerTableCellRenderer() {
		super();
		noFocusBorder = new EmptyBorder(1, 2, 1, 2);
		setOpaque(true);
		setBorder(noFocusBorder);
	}

	public Component getTableCellRendererComponent(
		JTable table,
		Object value,
		boolean isSelected,
		boolean hasFocus,
		int row,
		int column) {
		setBackground(isSelected && !hasFocus ? table.getSelectionBackground() : table.getBackground());
		setForeground(isSelected && !hasFocus ? table.getSelectionForeground() : table.getForeground());

		Border br = hasFocus ? UIManager.getBorder("Table.focusCellHighlightBorder") : noFocusBorder;
		setBorder(br);
		return this;
	}
}
