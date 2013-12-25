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

import javax.swing.table.DefaultTableCellRenderer;

import com.choicemaker.cm.modelmaker.filter.IntFilterCondition;

/**
 * .
 *
 * @author   Arturo Falck
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:09 $
 */
public class IntFilterParamsCellRenderer extends DefaultTableCellRenderer {

	/**
	 * @see javax.swing.table.DefaultTableCellRenderer#setValue(java.lang.Object)
	 */
	protected void setValue(Object value) {
		if (value instanceof IntFilterCondition) {
			IntFilterCondition filterCondition = (IntFilterCondition) value;

			switch (filterCondition.getCondition()) {
				case IntFilterCondition.NULL_CONDITION:
					setText("");
					break;
					
				case IntFilterCondition.BETWEEN :
					if (filterCondition.getA() != IntFilterCondition.NULL_PARAM
					&&  filterCondition.getB() != IntFilterCondition.NULL_PARAM){
						setText("[" + filterCondition.getA() + " ... " + filterCondition.getB() + "]");
					}
					break;
					
				case IntFilterCondition.OUTSIDE :
					if (filterCondition.getA() != IntFilterCondition.NULL_PARAM
					&&  filterCondition.getB() != IntFilterCondition.NULL_PARAM){
						setText(")" + filterCondition.getA() + " ... " + filterCondition.getB() + "(");
					}
					break;

				case IntFilterCondition.LESS_THAN :
					//FALLS THROUGH
				case IntFilterCondition.LESS_THAN_EQUAL :
					if (filterCondition.getA() != IntFilterCondition.NULL_PARAM){
						setText("" + filterCondition.getB());
					}
					break;

				default :
					if (filterCondition.getA() != IntFilterCondition.NULL_PARAM){
						setText("" + filterCondition.getA());
					}
					break;
			}
		} else {
			setText("");
		}
	}

}
