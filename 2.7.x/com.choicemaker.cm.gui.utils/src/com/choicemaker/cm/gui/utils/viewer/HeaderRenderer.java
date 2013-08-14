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

import javax.swing.*;
import javax.swing.table.*;

/**
 * Rendered used to give different colors to the data in the RecordTables.
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 17:39:18 $
 */
public class HeaderRenderer extends DefaultTableCellRenderer {
	private boolean italics;

	public HeaderRenderer(boolean italics) {
		this.italics = italics;
	}

	public Component getTableCellRendererComponent(
		JTable table,
		Object value,
		boolean isSelected,
		boolean hasFocus,
		int row,
		int column) {
		if (italics) {
			setHorizontalAlignment(JLabel.CENTER);
		}
		if (table != null) {
			JTableHeader header = table.getTableHeader();
			if (header != null) {
				setForeground(header.getForeground());
				setBackground(header.getBackground());
				if (italics) {
					setFont(header.getFont().deriveFont(Font.ITALIC));
				} else {
					setFont(header.getFont().deriveFont(0));
				}
			}
		}

		setText((value == null) ? "" : value.toString());
		setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		return this;
	}
}
