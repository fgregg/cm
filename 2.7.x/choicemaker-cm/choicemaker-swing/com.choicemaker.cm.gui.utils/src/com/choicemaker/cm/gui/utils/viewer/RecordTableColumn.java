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

import javax.swing.table.TableColumn;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class RecordTableColumn extends TableColumn {
	private String fieldName;
	private boolean visible;
	private int displayIndex;

	/**
	 * Constructor for RecordTableColumn.
	 * @param modelIndex
	 * @param width
	 */
	public RecordTableColumn(int modelIndex, int width, String name, String alias) {
		super(modelIndex, width);
		setFieldName(name);
		setHeaderValue(alias);
	}

	public void setFieldName(String name) {
		this.fieldName = name;
	}

	public String getFieldName() {
		return fieldName;
	}	
	/**
	 * Returns the visible.
	 * @return boolean
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Sets the visible.
	 * @param visible The visible to set
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	/**
	 * Returns the displayIndex.
	 * @return int
	 */
	public int getDisplayIndex() {
		return displayIndex;
	}

	/**
	 * Sets the displayIndex.
	 * @param displayIndex The displayIndex to set
	 */
	public void setDisplayIndex(int displayIndex) {
		this.displayIndex = displayIndex;
	}

}
