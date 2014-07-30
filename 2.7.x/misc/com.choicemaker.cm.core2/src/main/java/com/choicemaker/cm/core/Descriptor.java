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
package com.choicemaker.cm.core;

import java.io.Serializable;

/**
 * Descriptor of record for display in the human review panel of ModelMaker.
 * For each schema, a set of descriptors is generated. They form a hierarchy
 * parallel to that of the record types.
 *
 * Assume the following simplified ChoiceMaker schema:
 * <pre>
 * <ChoiceMakerSchema>
 *   <nodeType name="person">
 *     <field name="firstName" type="String"/>
 *     <nodeType name="contact">
 *       <field name="relationshipCd" type="char"/>
 *     </nodeType>
 *   </nodeType>
 * </ChoiceMakerSchema>
 * </pre>
 *
 * This would create two Descriptor's. One named person and one named contact.
 * The descriptions of the methods refer to this example.
 *
 * @author   Martin Buechi
 * @author   S. Yoakum-Stover
 * @version  $Revision: 1.1 $ $Date: 2010/01/20 15:05:04 $
 */
public interface Descriptor extends Serializable {
	/**
	 * Returns the column definitions of this record.
	 * E.g., the person Descriptor returns a single column firstName.
	 * 
	 * @return   The column definitions of this record.
	 */
	ColumnDefinition[] getColumnDefinitions();

	/**
	 * Returns the descriptors of the nested records.
	 * E.g., the person Descriptor returns a one element array with
	 * the contact Descriptor. The latter returns a zero-length array.
	 *
	 * @return   The descriptors of the nested records.
	 */
	Descriptor[] getChildren();

	/**
	 * Return null.
	 * Should probably return the direct child records of the specified record, grouped
	 * by record type.
	 *
	 * @return   null
	 */
	Record[][] getChildRecords(Record ri);

	/**
	 * Returns the value of the specied record at the given row and column as String.
	 * E.g., the person Descriptor would return the firstName for a call
	 * getValue(r, 0, 0). The contact Descriptor would return the relationshipCd
	 * of the 3rd contact in r for a call getValue(r, 3, 0). 
	 *
	 * @param    r  The record.
	 * @param    row The row, i.e., the index of the stacked row.
	 * @param    col The column.
	 * @return   The value of the specied record at the given row and column.
	 */
	String getValueAsString(Record r, int row, int col);
	
	/**
	 * Returns the value of the specied record at the given row and column.
	 * E.g., the person Descriptor would return the firstName for a call
	 * getValue(r, 0, 0). The contact Descriptor would return the relationshipCd
	 * of the 3rd contact in r for a call getValue(r, 3, 0). 
	 *
	 * @param    r  The record.
	 * @param    row The row, i.e., the index of the stacked row.
	 * @param    col The column.
	 * @return   The value of the specied record at the given row and column.
	 */
	Object getValue(Record r, int row, int col);

	/**
	 * Returns the validity of the value of the specified record at the given row
	 * and column. E.g., the person Descriptor would return the validity of the firstName for a call
	 * getValue(r, 0, 0). The contact Descriptor would return the validity of relationshipCd
	 * of the 3rd contact in r for a call getValue(r, 3, 0). 
	 *
	 * @param    r  The record.
	 * @param    row The row, i.e., the index of the stacked row.
	 * @param    col The column.
	 * @return   The validity of the value of the specified record at the given row
	 *           and column.
	 */
	boolean getValidity(Record r, int row, int col);

	/**
	 * Sets the value of the specified record at the given row and column.
	 * E.g., for the person Descriptor the call setValue(r, 0, 0, "James") would
	 * set the value of firstName to "James". For the contact Descriptor
	 * setValue(r, 3, 0, "M") would set the relationshipCd of the
	 * 3rd contact in r to 'M'.
	 * @param    r  The record.
	 * @param    row The row, i.e., the index of the stacked row.
	 * @param    col The column.
	 * @param    value The value as String.
	 * @return   Whether the operation succeeded. This may not be the case if the
	 *           conversion from String to the type of the field fails.
	 */
	boolean setValue(Record r, int row, int col, String value);

	/**
	 * Deletes the specified row from the record. Not applicable for the
	 * root record.
	 * E.g., deleteRow is not applicable for the person Descriptor. For the
	 * contact Descriptor, deleteRow(r, 2) deletes the 2nd row. 
	 *
	 * @throws  UnsupportedOperationException when called on the descriptor
	 *          of the root record.
	 */
	void deleteRow(Record r, int row);

	/**
	 * Adds a row. If this is a nested stacked record, outer is set to
	 * the 0th row of the outer. If no rows exist on the outer record, a
	 * row is added.
	 *
	 * @param   position  The position at which the row should be added.
	 * @param   above  Whether the row should be added above or be low the specified
	 *          position.
	 * @param   r  The record to which the row should be added.
	 * @throws  UnsupportedOperationException when called on the descriptor
	 *          of the root record.
	 */
	void addRow(int position, boolean above, Record r);

	/**
	 * Returns the number of columns.
	 *
	 * @return  The number of columns.
	 */
	int getColumnCount();

	/**
	 * Returns the number of rows.
	 * E.g., the person Descriptor returns 1. The contacts Descriptor returns
	 * the number of contacts on r.
	 *
	 * @return   The number of rows.
	 */
	int getRowCount(Record r);

	/**
	 * Returns the displayed name.
	 *
	 * @return  The displayed name.
	 */
	String getName();

	/**
	 * Returns the name of the record.
	 *
	 * @return  The name of the record.
	 */
	String getRecordName();

	/**
	 * Returns whether the record is stackable. True for all descriptors
	 * except the one for the root record.
	 *
	 * @return   Whether the record is stackable.
	 */
	boolean isStackable();

	/**
	 * Returns the column index with the specified field name or
	 * -1 if no such column exists.
	 *
	 * @return   The column index with the specified field name or
	 *           -1 if no such column exists.
	 */
	int getColumnIndexByName(String name);

	/**
	 * Returns an array indicating which fields can be modified. This
	 * is the case for all non-derived values.
	 *
	 * @return    An array indicating which fields can be modified.
	 */
	boolean[] getEditable(DerivedSource src);
	
	Class getHandledClass();
}
