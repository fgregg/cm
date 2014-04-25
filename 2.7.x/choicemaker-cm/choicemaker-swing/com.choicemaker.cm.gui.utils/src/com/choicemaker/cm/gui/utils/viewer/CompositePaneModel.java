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

import java.util.*;

import com.choicemaker.cm.core.Descriptor;
import com.choicemaker.cm.core.datamodel.DefaultCompositeObservableData;

/**
 * 
 * 
 * @author  Arturo Falck
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class CompositePaneModel extends DefaultCompositeObservableData {
	
	//****************** Constants
	
	public static final String ENABLE_EDITING = "ENABLE_EDITING";
	public static final String DESCRIPTOR = "DESCRIPTOR";
	public static final String FILE_NAME = "FILE_NAME";
	
	//****************** Static Methods
	
	/**
	 * Creates an array that contains an single RecordPairViewerModel initialized with the descriptor.
	 */
	private static RecordPairViewerModel[] createViewerModels(Descriptor descriptor, boolean withData) {
		RecordPairViewerModel[] res = new RecordPairViewerModel[1];
		if (withData){
			res[0] = new RecordPairViewerModel(descriptor);
		}
		else{
			res[0] = new RecordPairViewerModel(descriptor, new InternalFrameModel[0]);
		}
		return res;
	}
	
	//****************** Fields
	
	private String fileName;
	private boolean enableEditing;
	private Descriptor descriptor;
	private List viewerModels;
	
	//****************** Constructors
	
	public CompositePaneModel(Descriptor descriptor) {
		this(descriptor, true);
	}
	
	public CompositePaneModel(Descriptor descriptor, boolean withData) {
		this(descriptor, createViewerModels(descriptor, withData));
	}

	public CompositePaneModel(
		Descriptor descriptor, 
		RecordPairViewerModel[] viewerModels) {
			
		this.descriptor = descriptor;
		
		this.viewerModels = new ArrayList();
		for (int i = 0; i < viewerModels.length; i++) {
			this.viewerModels.add(viewerModels[i]);
		}
	}
	
	/**
	 * Returns the descriptor.
	 * @return Descriptor
	 */
	public Descriptor getDescriptor() {
		return descriptor;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((descriptor == null) ? 0 : descriptor.hashCode());
		return result;
	}

	public boolean equals(Object o){
		if (o instanceof CompositePaneModel){
			CompositePaneModel other = (CompositePaneModel)o;
			boolean returnValue = true;
			
			returnValue &= getDescriptor() == other.getDescriptor();
			
			RecordPairViewerModel[] viewers = getViewerModels();
			RecordPairViewerModel[] otherViewers = other.getViewerModels();
			if (viewers.length == otherViewers.length){
				for (int i = 0; i < viewers.length; i++) {
					returnValue &= viewers[i].equals(otherViewers[i]);
				}
			}
			else{
				returnValue = false;
			}
			
			return returnValue;
		}
		else{
			return false;
		}
	}
	
	public RecordPairViewerModel[] getViewerModels() {
		return (RecordPairViewerModel[])viewerModels.toArray(new RecordPairViewerModel[viewerModels.size()]);
	}
	
	public void removeViewerModel(RecordPairViewerModel viewerModel) {
		viewerModels.remove(viewerModel);
		fireObservableDataRemoved(viewerModel);
	}
	
	public void addViewerModel(RecordPairViewerModel viewerModel) {
		viewerModels.add(viewerModel);
		viewerModel.setEnableEditing(enableEditing);
		fireObservableDataAdded(viewerModel);
	}
	
	public void addTab(){
		addViewerModel(new RecordPairViewerModel(descriptor, new InternalFrameModel[0]));
	}

	/**
	 * Returns the fileName.
	 * @return String
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets the fileName.
	 * @param fileName The fileName to set
	 */
	public void setFileName(String newValue) {
		String oldValue = fileName;
		fileName = newValue;
		firePropertyChange(FILE_NAME, oldValue, newValue);
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
		
		Iterator iterator = viewerModels.iterator();
		while (iterator.hasNext()) {
			RecordPairViewerModel viewerModel = (RecordPairViewerModel) iterator.next();
			viewerModel.setEnableEditing(newValue);
		}
	}
}
