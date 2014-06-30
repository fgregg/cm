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

import java.awt.Rectangle;

import com.choicemaker.cm.core.base.*;
import com.choicemaker.cm.core.datamodel.DefaultCompositeObservableData;

/**
 * Description
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class RecordPairFrameModel extends DefaultCompositeObservableData implements InternalFrameModel {
	
	//****************** Constants
	
	public static final String DIVIDER_LOCATION = "DIVIDER_LOCATION";
	public static final String RECORD_TABLE_COLUMN_MODEL = "RECORD_TABLE_COLUMN_MODEL";
	
	//****************** Fields
	
	private Descriptor descriptor;
	private String alias;
	private Rectangle bounds;
	private int dividerLocation;
	private RecordTableColumnModel recordTableColumnModel;
	private boolean enableEditing;
	
	//****************** Constructors
	
	public RecordPairFrameModel(
		Descriptor descriptor,
		String alias,
		Rectangle bounds,
		int dividerLocation,
		RecordTableColumnModel recordTableColumnModel) {
			
		init(descriptor, alias, bounds, dividerLocation, recordTableColumnModel);
	}
	
	public RecordPairFrameModel(Descriptor descriptor, int x, int y) {
		this(descriptor, x, y, Integer.MAX_VALUE);
	}

	public RecordPairFrameModel(Descriptor descriptor, int x, int y, int desktopPaneWidth) {
		int width = 0;
		ColumnDefinition[] columnDefinitions = descriptor.getColumnDefinitions();
		for (int i = 0; i < columnDefinitions.length; i++) {
			width += columnDefinitions[i].getWidth();
		}
		if (x + width > desktopPaneWidth) {
			width = desktopPaneWidth - x;
		}
		
		Rectangle bounds = new Rectangle(x, y, width, 100);
		recordTableColumnModel = new RecordTableColumnModel(descriptor, true);
		init(descriptor, descriptor.getName(), bounds, 45, recordTableColumnModel);		
	}

	protected void init(Descriptor descriptor, String alias, Rectangle bounds, int dividerLocation, RecordTableColumnModel recordTableColumnModel) {
		this.descriptor = descriptor;
		this.alias = alias;
		this.bounds = bounds;
		this.dividerLocation = dividerLocation;
		this.recordTableColumnModel = recordTableColumnModel;
	}

	public void setDividerLocation(int newValue) {
		int oldValue = dividerLocation;
		dividerLocation = newValue;
		firePropertyChange(DIVIDER_LOCATION, new Integer(oldValue), new Integer(newValue));
	}

	public int getDividerLocation() {
		return dividerLocation;
	}
	/**
	 * Returns the descriptor.
	 * @return Descriptor
	 */
	public Descriptor getDescriptor() {
		return descriptor;
	}

	/**
	 * Returns the recordTableColumnModel.
	 * @return RecordTableColumnModel
	 */
	public RecordTableColumnModel getRecordTableColumnModel() {
		return recordTableColumnModel;
	}

	/**
	 * Sets the descriptor.
	 * @param descriptor The descriptor to set
	 */
	public void setDescriptor(Descriptor newValue) {
		Descriptor oldValue = descriptor;
		descriptor = newValue;
		firePropertyChange(DESCRIPTOR, oldValue, newValue);
	}

	/**
	 * Sets the recordTableColumnModel.
	 * @param recordTableColumnModel The recordTableColumnModel to set
	 */
	public void setRecordTableColumnModel(RecordTableColumnModel newValue) {
		RecordTableColumnModel oldValue = recordTableColumnModel;
		recordTableColumnModel = newValue;
		firePropertyChange(RECORD_TABLE_COLUMN_MODEL, oldValue, newValue);
	}

	/**
	 * Returns the alias.
	 * @return String
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * Sets the alias.
	 * @param alias The alias to set
	 */
	public void setAlias(String newValue) {
		String oldValue = alias;
		alias = newValue;
		firePropertyChange(ALIAS, oldValue, newValue);
	}

	/**
	 * Returns the bounds.
	 * @return Rectangle
	 */
	public Rectangle getBounds() {
		return bounds;
	}

	/**
	 * Sets the bounds.
	 * @param bounds The bounds to set
	 */
	public void setBounds(Rectangle newValue) {
		Rectangle oldValue = bounds;
		bounds = newValue;
		firePropertyChange(BOUNDS, oldValue, newValue);
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getAlias() == null) ? 0 : getAlias().hashCode());
		result = prime * result + ((getBounds() == null) ? 0 : getBounds().hashCode());
		result = prime * result
				+ ((getDescriptor() == null) ? 0 : getDescriptor().hashCode());
		result = prime * result + getDividerLocation();
		return result;
	}
	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RecordPairFrameModel other = (RecordPairFrameModel) obj;
		if (getAlias() == null) {
			if (other.getAlias() != null)
				return false;
		} else if (!getAlias().equals(other.getAlias()))
			return false;
		if (getBounds() == null) {
			if (other.getBounds() != null)
				return false;
		} else if (!getBounds().equals(other.getBounds()))
			return false;
		if (getDescriptor() == null) {
			if (other.getDescriptor() != null)
				return false;
		} else if (!getDescriptor().equals(other.getDescriptor()))
			return false;
		if (getDividerLocation() != other.getDividerLocation())
			return false;
		return true;
	}
	
	/**
	 * Obsolete method for {@link #equals(Object)}. Used for testing only.
	 * @deprecated
	 */
	public boolean equals_00(Object o){
		if (o instanceof RecordPairFrameModel){
			RecordPairFrameModel other = (RecordPairFrameModel)o;
			boolean returnValue = true;
			
			returnValue &= getDescriptor() == other.getDescriptor();
			returnValue &= getAlias() == other.getAlias();
			returnValue &= getBounds() == other.getBounds();
			returnValue &= getDividerLocation() == other.getDividerLocation();
			
			return returnValue;
		}
		else{
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.train.gui.viewer.InternalFrameModel#isEnableEditing()
	 */
	public boolean isEnableEditing() {
		return enableEditing;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.train.gui.viewer.InternalFrameModel#setEnableEditing(boolean)
	 */
	public void setEnableEditing(boolean newValue) {
		Boolean oldValue = new Boolean(enableEditing);
		enableEditing = newValue;
		firePropertyChange(ENABLE_EDITING, oldValue, new Boolean(newValue));
		
		getRecordTableColumnModel().setEnableEditing(newValue);
	}
}
