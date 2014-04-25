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
package com.choicemaker.cm.mmdevtools.gui;

import java.text.DecimalFormat;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * @author Adam Winkel
 */
public class DoubleTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	public static final TableCellRenderer INSTANCE = new DoubleTableCellRenderer();

	private static DecimalFormat df = new DecimalFormat("#####0.000");

	private DoubleTableCellRenderer() {
		this.setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);
	}

	public void setValue(Object value) {
		if (value instanceof Float || value instanceof Double) {
			double v = ((Number)value).doubleValue();
			value = df.format(v);
		}
		
		super.setValue(value);
	}

}
