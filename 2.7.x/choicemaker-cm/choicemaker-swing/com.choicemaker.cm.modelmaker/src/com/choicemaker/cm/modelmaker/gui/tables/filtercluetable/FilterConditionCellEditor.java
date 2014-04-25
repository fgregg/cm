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

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;

import com.choicemaker.cm.analyzer.filter.BooleanFilterCondition;
import com.choicemaker.cm.analyzer.filter.IntFilterCondition;
import com.choicemaker.cm.analyzer.filter.RuleFilterCondition;

/**
 * .
 *
 * @author   Arturo Falck
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:09 $
 */
public class FilterConditionCellEditor extends DefaultCellEditor {

	//***************************** Constants

	private static final long serialVersionUID = 1L;

	private static final Object[] VALUES_DEFAULT =
		{
			new BooleanFilterCondition(BooleanFilterCondition.NULL_CONDITION),
		};

	/**
	 * Even though not technically a constant, this variable is used as a constant.
	 */
	private static Object[] VALUES_BOOLEAN =
		{
			new BooleanFilterCondition(BooleanFilterCondition.NULL_CONDITION),
			new BooleanFilterCondition(BooleanFilterCondition.ACTIVE_CONDITION),
			new BooleanFilterCondition(BooleanFilterCondition.INACTIVE_CONDITION),
		};

	private static Object[] VALUES_RULE =
		{
			new RuleFilterCondition(RuleFilterCondition.NULL_CONDITION),
			new RuleFilterCondition(RuleFilterCondition.ACTIVE_CONDITION),
			new RuleFilterCondition(RuleFilterCondition.INACTIVE_CONDITION),
		};


	/**
	 * Even though not technically a constant, this variable is used as a constant
	 * (initialized in static{}).
	 */
	private static Object[] VALUES_INT;


	//***************************** Static Initialization

	static {
		populateBoolean();
		populateInt();
	}

	private static void populateBoolean(){
		// NOTE: already done in initializer.
	}

	private static void populateInt(){
		VALUES_INT = new Object[IntFilterCondition.MAX - IntFilterCondition.MIN + 1];
		for (int i = 0; i < VALUES_INT.length; i++) {
			VALUES_INT[i] = new IntFilterCondition(i);
		}
	}

	//***************************** Fields

	private JComboBox options;


	//***************************** Construction

	public FilterConditionCellEditor() {
		this(new JComboBox(VALUES_DEFAULT));
	}

	protected FilterConditionCellEditor(JComboBox options) {
		super(options);
		this.options = options;
	}

	/**
	 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
	 */
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		resetItems(value);
		return super.getTableCellEditorComponent(table, value, isSelected, row, column);
	}

	/**
	 * Changes the Options to match the type of the current value.
	 */
	protected void resetItems(Object value){
		options.removeAllItems();
		Object[] items = VALUES_DEFAULT;
		if (value instanceof BooleanFilterCondition){
			items = VALUES_BOOLEAN;
		} else if (value instanceof IntFilterCondition){
			items = VALUES_INT;
		} else if (value instanceof RuleFilterCondition) {
			items = VALUES_RULE;
		}

		for (int i = 0; i < items.length; i++) {
			options.addItem(items[i]);
		}
	}

}
