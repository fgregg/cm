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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import com.choicemaker.cm.analyzer.filter.IntFilterCondition;
import com.choicemaker.cm.gui.utils.swing.IntegerField;

/**
 * .
 *
 * @author   Arturo Falck
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:09 $
 */
public class IntFilterParamsCellEditor extends AbstractCellEditor implements TableCellEditor {
	private static final long serialVersionUID = 1L;
	private OneTextFields oneTextField = new OneTextFields(4, this);
	private TwoTextFields twoTextFields = new TwoTextFields(4, this);
	private Dimension PREFFERED_SIZE = twoTextFields.getPreferredSize();

	private Component currentEditor;
	private IntFilterCondition filterCondition;

	/**
	 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
	 */
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		if (value instanceof IntFilterCondition) {
			filterCondition = (IntFilterCondition) value;

			switch (filterCondition.getCondition()) {
				case IntFilterCondition.NULL_CONDITION :
					currentEditor = null;
					break;

				case IntFilterCondition.BETWEEN :
					twoTextFields.setValues("[", filterCondition.getA(), "...", filterCondition.getB(), "]");
					currentEditor = twoTextFields;
					break;

				case IntFilterCondition.OUTSIDE :
					twoTextFields.setValues(")", filterCondition.getA(), "...", filterCondition.getB(), "(");
					currentEditor = twoTextFields;
					break;

				default :
					oneTextField.setValue(filterCondition.getA());
					currentEditor = oneTextField;
					break;
			}
		} else {
			currentEditor = null;
		}

		table.setRowHeight(row, PREFFERED_SIZE.height);

		return currentEditor;
	}
	/**
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	public Object getCellEditorValue() {
		IntFilterCondition returnValue;

		switch (filterCondition.getCondition()) {
			case IntFilterCondition.OUTSIDE :
				returnValue = validateOutsideInput();
				break;
			case IntFilterCondition.BETWEEN :
				returnValue = validateBetweenInput();
				break;

			default :
				returnValue = validateSingleInput();
				break;
		}

		return returnValue;
	}

	protected IntFilterCondition validateSingleInput() {
		IntFilterCondition returnValue;
		// _ -> -
		if (oneTextField.getValue() == IntegerField.DEFAULT){
			returnValue =
				new IntFilterCondition(filterCondition.getClueNum(), IntFilterCondition.NULL_CONDITION, IntFilterCondition.NULL_PARAM);
		}
		else {
			returnValue = new IntFilterCondition(filterCondition.getClueNum(), filterCondition.getCondition(), oneTextField.getValue());
		}
		return returnValue;
	}

	protected IntFilterCondition validateBetweenInput() {
		IntFilterCondition returnValue;
		// [_ ... _] -> -
		if (twoTextFields.getFistValue() == IntegerField.DEFAULT && twoTextFields.getSecondValue() == IntegerField.DEFAULT) {
			returnValue =
				new IntFilterCondition(filterCondition.getClueNum(), IntFilterCondition.NULL_CONDITION, IntFilterCondition.NULL_PARAM);
		// [a ... a] -> x = a
		} else if (twoTextFields.getLowerValue() == twoTextFields.getHigherValue()) {
			returnValue =
				new IntFilterCondition(filterCondition.getClueNum(), IntFilterCondition.EQUALS, twoTextFields.getLowerValue());
		// [a ... _] -> x >= a
		} else if (twoTextFields.getFistValue() != IntegerField.DEFAULT && twoTextFields.getSecondValue() == IntegerField.DEFAULT){
			returnValue =
				new IntFilterCondition(filterCondition.getClueNum(), IntFilterCondition.GREATER_THAN_EQUAL, twoTextFields.getFistValue());
		// [_ ... a] -> x <= a
		} else if (twoTextFields.getFistValue() == IntegerField.DEFAULT && twoTextFields.getSecondValue() != IntegerField.DEFAULT){
			returnValue =
				new IntFilterCondition(filterCondition.getClueNum(), IntFilterCondition.LESS_THAN_EQUAL, twoTextFields.getSecondValue());
		} else {
			returnValue =
				new IntFilterCondition(
					filterCondition.getClueNum(),
					filterCondition.getCondition(),
					twoTextFields.getLowerValue(),
					twoTextFields.getHigherValue());
		}
		return returnValue;
	}

	protected IntFilterCondition validateOutsideInput() {
		IntFilterCondition returnValue;
		// )_ ... _( -> -
		if (twoTextFields.getFistValue() == IntegerField.DEFAULT && twoTextFields.getSecondValue() == IntegerField.DEFAULT) {
			returnValue =
				new IntFilterCondition(filterCondition.getClueNum(), IntFilterCondition.NULL_CONDITION, IntFilterCondition.NULL_PARAM);
		// )a ... a( -> x != a
		} else if (twoTextFields.getLowerValue() == twoTextFields.getHigherValue()) {
			returnValue =
				new IntFilterCondition(filterCondition.getClueNum(), IntFilterCondition.NOT_EQUALS, twoTextFields.getLowerValue());
		// )a ... _( -> x < a
		} else if (twoTextFields.getFistValue() != IntegerField.DEFAULT && twoTextFields.getSecondValue() == IntegerField.DEFAULT){
			returnValue =
				new IntFilterCondition(filterCondition.getClueNum(), IntFilterCondition.LESS_THAN, twoTextFields.getFistValue());
		// )_ ... a( -> x > a
		} else if (twoTextFields.getFistValue() == IntegerField.DEFAULT && twoTextFields.getSecondValue() != IntegerField.DEFAULT){
			returnValue =
				new IntFilterCondition(filterCondition.getClueNum(), IntFilterCondition.GREATER_THAN, twoTextFields.getSecondValue());
		} else {
			returnValue =
				new IntFilterCondition(
					filterCondition.getClueNum(),
					filterCondition.getCondition(),
					twoTextFields.getLowerValue(),
					twoTextFields.getHigherValue());
		}
		return returnValue;
	}

	public static class OneTextFields extends JPanel implements FocusListener {
		private static final long serialVersionUID = 1L;
		private IntegerField firstField;
		private IntFilterParamsCellEditor editor;

		public OneTextFields(int columns, IntFilterParamsCellEditor editor) {
			this.editor = editor;
			firstField = new IntegerField(columns);

			firstField.addFocusListener(this);

			setLayout(new BorderLayout());
			add(firstField, BorderLayout.WEST);
		}

		//TODO: consider not doing this in addNotify but in requestFocus, or something like that.
		//		this should work but it seems a bit dirty.
		public void addNotify() {
			super.addNotify();
			firstField.requestFocus();
		}

		public void setValue(int first) {
			firstField.clear();
			if (first != IntFilterCondition.NULL_PARAM) {
				firstField.setValue(first);
			}
		}

		public int getValue() {
			return firstField.getValue();
		}

		//***************************** FocusListener Methods

		/**
		 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
		 */
		public void focusGained(FocusEvent e) {
		}

		/**
		 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
		 */
		public void focusLost(FocusEvent e) {
			editor.stopCellEditing();
		}
	}

	public static class TwoTextFields extends JPanel implements FocusListener {
		private static final long serialVersionUID = 1L;
		private IntFilterParamsCellEditor editor;
		private IntegerField firstField;
		private IntegerField secondField;
		private JLabel preField = new JLabel();
		private JLabel postField = new JLabel();
		private JLabel labelField = new JLabel();

		public TwoTextFields(int columns, IntFilterParamsCellEditor editor) {
			this.editor = editor;
			firstField = new IntegerField(columns);
			secondField = new IntegerField(columns);

			firstField.addFocusListener(this);
			secondField.addFocusListener(this);

			GridBagLayout gridbag = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.NONE;
			c.anchor = GridBagConstraints.WEST;
			c.weightx = 1.0;
			GridBagConstraints c2 = new GridBagConstraints();
			c2.fill = GridBagConstraints.NONE;
			c2.weightx = 1.0;
			c2.gridwidth = GridBagConstraints.REMAINDER; //end

			setLayout(gridbag);

			addComponent(preField, gridbag, c);
			addComponent(firstField, gridbag, c);

			addComponent(labelField, gridbag, c);

			addComponent(secondField, gridbag, c);
			addComponent(postField, gridbag, c2);

			labelField.setHorizontalAlignment(JLabel.CENTER);

			initializeFocus();
		}

		protected void addComponent(Component component, GridBagLayout gridbag, GridBagConstraints constraints) {
			gridbag.setConstraints(component, constraints);
			add(component);
		}

		protected void initializeFocus() {
			//TODO: figure out how to set up the focus cycle so that we can tab between fields.
		}

		//TODO: consider not doing this in addNotify but in requestFocus, or something like that.
		//		this should work but it seems a bit dirty.
		public void addNotify() {
			super.addNotify();
			firstField.requestFocus();
		}

		public void setValues(String pre, int first, String label, int second, String post) {
			firstField.clear();
			secondField.clear();

			preField.setText(pre);
			if (first != IntFilterCondition.NULL_PARAM) {
				firstField.setValue(first);
			}
			labelField.setText(label);
			if (second != IntFilterCondition.NULL_PARAM) {
				secondField.setValue(second);
			}
			postField.setText(post);
		}

		public int getFistValue() {
			return firstField.getValue();
		}

		public int getSecondValue() {
			return secondField.getValue();
		}

		public int getLowerValue() {
			return Math.min(getFistValue(), getSecondValue());
		}

		public int getHigherValue() {
			return Math.max(getFistValue(), getSecondValue());
		}

		//***************************** FocusListener Methods

		/**
		 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
		 */
		public void focusGained(FocusEvent e) {
		}

		/**
		 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
		 */
		public void focusLost(FocusEvent e) {
			if (e.getOppositeComponent() != firstField && e.getOppositeComponent() != secondField){
				editor.stopCellEditing();
			}
		}

	}
}
