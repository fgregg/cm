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

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer used to implement a checkbox inside a table cell.
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 17:39:49 $
 */
public class CheckCellRenderer extends JCheckBox implements TableCellRenderer {
	protected static Border noFocusBorder;
	public CheckCellRenderer() {
		super();
		noFocusBorder = new EmptyBorder(1, 2, 1, 2);
		setOpaque(true);
		setBorder(noFocusBorder);
	}
	/**
	 *  Returns the component used for drawing the cell.  This method is
	 *  used to configure the renderer appropriately before drawing.
	 *
	 * @param   table       the <code>JTable</code> that is asking the
	 *              renderer to draw; can be <code>null</code>
	 * @param   value       the value of the cell to be rendered.  It is
	 *              up to the specific renderer to interpret
	 *              and draw the value.  For example, if
	 *              <code>value</code>
	 *              is the string "true", it could be rendered as a
	 *              string or it could be rendered as a check
	 *              box that is checked.  <code>null</code> is a
	 *              valid value
	 * @param   isSelected  true if the cell is to be rendered with the
	 *              selection highlighted; otherwise false
	 * @param   hasFocus    if true, render cell appropriately.  For
	 *              example, put a special border on the cell, if
	 *              the cell can be edited, render in the color used
	 *              to indicate editing
	 * @param   row         the row index of the cell being drawn.  When
	 *              drawing the header, the value of
	 *              <code>row</code> is -1
	 * @param   column          the column index of the cell being drawn
	 */
	public Component getTableCellRendererComponent(
		JTable table,
		Object value,
		boolean isSelected,
		boolean hasFocus,
		int row,
		int column) {

		if (value instanceof Boolean) {
			Boolean b = (Boolean) value;
			setSelected(b.booleanValue());
		}
		setBackground(isSelected && !hasFocus ? table.getSelectionBackground() : table.getBackground());
		setForeground(isSelected && !hasFocus ? table.getSelectionForeground() : table.getForeground());
		Border br = hasFocus ? UIManager.getBorder("Table.focusCellHighlightBorder") : noFocusBorder;
		setBorder(br);
		setHorizontalAlignment(SwingConstants.CENTER);
		setVerticalAlignment(SwingConstants.CENTER);
		return this;
	}
}
