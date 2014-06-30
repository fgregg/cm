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
package com.choicemaker.cm.modelmaker.gui.tables;

import com.choicemaker.cm.core.base.ClueDesc;

public class ClTableRow {
	protected ClueDesc desc;
	protected Object[] vals;

	protected ClTableRow(ClueDesc desc, Object[] vals) {
		this.desc = desc;
		this.vals = vals;
	}

	public void set(int index, Object val) {
		vals[index] = val;
	}

	public Object getColumn(int index) {
		return vals[index];
	}

	public boolean isRule() {
		return desc.rule;
	}

	public static String getModifier(ClueDesc desc) {
		if (desc.modifier == ClueDesc.NONE) {
			return "";
		} else if (desc.modifier == ClueDesc.REPORT) {
			return "report";
		} else {
			return "note";
		}
	}

	public static String getType(ClueDesc desc) {
		if (desc.rule) {
			return "rule";
		} else {
			return "clue";
		}
	}
}
