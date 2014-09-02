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
package com.choicemaker.cm.modelmaker.gui.tables.filtercluetable;

import com.choicemaker.cm.analyzer.filter.BooleanFilterCondition;
import com.choicemaker.cm.analyzer.filter.FilterCondition;
import com.choicemaker.cm.analyzer.filter.IntFilterCondition;
import com.choicemaker.cm.analyzer.filter.RuleFilterCondition;
import com.choicemaker.cm.core.ClueDesc;
import com.choicemaker.cm.core.ClueSet;
import com.choicemaker.cm.core.ClueSetType;
import com.choicemaker.cm.modelmaker.gui.tables.ClTableRow;
import com.choicemaker.cm.modelmaker.gui.utils.NullInteger;

/**
 *
 * @author S. Yoakum-Stover
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:09 $
 */
public class FilterClueTableRow extends ClTableRow {

	private ClueDesc _desc;
	private FilterCondition condition;

	FilterClueTableRow(ClueSet clueSet, ClueDesc desc) {
		super(
			desc,
			new Object[] {
				new NullInteger(desc.getNumber(), "  "),
				desc.getName(),
				desc.getDecision().toString(),
				ClTableRow.getType(desc),
				ClTableRow.getModifier(desc)});
		this.condition = createDefaultCondition(clueSet, desc);
		this._desc = desc;
	}

	protected FilterCondition createDefaultCondition(ClueSet clueSet, ClueDesc desc) {
		if (desc.rule) {
			return new RuleFilterCondition();
		} else if (ClueSetType.BOOLEAN.equals(clueSet.getType())) {
			return new BooleanFilterCondition();
		} else if (ClueSetType.INT.equals(clueSet.getType())) {
			return new IntFilterCondition();
		}

		return null;
	}

	public FilterCondition getFilterCondition() {
		return condition;
	}

	public void setFilterCondition(FilterCondition condition) {
		if (_desc == null) {
			throw new IllegalStateException("TODO: how can desc be null?");
		}
		if (condition == null) {
			this.condition = null;
		} else {
			this.condition = condition.createFilterCondition(_desc.getNumber());
		}
	}
}
