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

import com.choicemaker.cm.core.Descriptor;
import com.choicemaker.cm.core.datamodel.DefaultObservableData;

/**
 * 
 * 
 * @author  Arturo Falck
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class CompositeFrameModel extends DefaultObservableData implements InternalFrameModel {
	
	//****************** Fields
	
	private Descriptor descriptor;
	private String alias;
	private Rectangle bounds;
	private boolean enableEditing;
	
	private CompositePaneModel compositePaneModel;
	
	//****************** Constructors
	
	public CompositeFrameModel(Descriptor descriptor, int x, int y) {
		this(descriptor, new CompositePaneModel(descriptor, false), "<New Frame>", new Rectangle(x, y, 300, 300));
	}

	public CompositeFrameModel(
		Descriptor descriptor, 
		CompositePaneModel compositePaneModel,
		String alias,
		Rectangle bounds) {
			
		this.descriptor = descriptor;
		this.alias = alias;
		this.bounds = bounds;
		this.compositePaneModel = compositePaneModel;
	}
	
	/**
	 * Returns the descriptor.
	 * @return Descriptor
	 */
	public Descriptor getDescriptor() {
		return descriptor;
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

	public boolean equals(Object o){
		if (o instanceof CompositeFrameModel){
			CompositeFrameModel other = (CompositeFrameModel)o;
			boolean returnValue = true;
			
			returnValue &= getDescriptor() == other.getDescriptor();
			returnValue &= getAlias() == other.getAlias();
			returnValue &= getBounds() == other.getBounds();
			
			returnValue &= getCompositePaneModel().equals(other.getCompositePaneModel());
			
			return returnValue;
		}
		else{
			return false;
		}
	}
	
	/**
	 * @return CompositePaneModel
	 */
	public CompositePaneModel getCompositePaneModel() {
		return compositePaneModel;
	}

	/**
	 * @return boolean
	 */
	public boolean isEnableEditing() {
		return enableEditing;
	}

	/**
	 * Recursively sets the enableEditing property in the whole Model.
	 * @param enableEditing The enableEditing to set
	 */
	public void setEnableEditing(boolean newValue) {
		Boolean oldValue = new Boolean(enableEditing);
		enableEditing = newValue;
		firePropertyChange(ENABLE_EDITING, oldValue, new Boolean(newValue));
		
		getCompositePaneModel().setEnableEditing(newValue);
	}

}
