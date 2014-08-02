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

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.choicemaker.cm.core.Descriptor;
import com.choicemaker.cm.core.base.DescriptorCollection;
import com.choicemaker.cm.core.datamodel.DefaultCompositeObservableData;

/**
 * Description
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class RecordPairViewerModel extends DefaultCompositeObservableData {
	
	//****************** Constants
	
	public static final String ENABLE_EDITING = "ENABLE_EDITING";
	public static final String ALIAS = "alias";
	
	//****************** Fields
	
	private Descriptor descriptor;
	private List frameModels;
	private String alias;
	private boolean enableEditing;
	private Dimension preferredSize;
	
	//****************** Constructors
	
	public RecordPairViewerModel(Descriptor descriptor) {
		this(descriptor, createFrameModels(descriptor));
		preferredSize = new Dimension(0, 0);
	}
	
	public RecordPairViewerModel(Descriptor descriptor, InternalFrameModel[] frameModels) {
		this.descriptor = descriptor;
		this.frameModels = new ArrayList();
		preferredSize = new Dimension(0, 0);
		for (int i = 0; i < frameModels.length; i++) {
			this.frameModels.add(frameModels[i]);
		}
	}
	
	private static InternalFrameModel[] createFrameModels(Descriptor descriptor) {
		Descriptor[] descriptors = new DescriptorCollection(descriptor).getDescriptors();
		InternalFrameModel[] res = new InternalFrameModel[descriptors.length];
		int y = 0;
		for (int i = 0; i < descriptors.length; i++) {
			res[i] = new RecordPairFrameModel(descriptors[i], 0, y);
			y += res[i].getBounds().height;
		}
		return res;
	}

	public InternalFrameModel[] getFrameModels() {
		return (InternalFrameModel[])frameModels.toArray(new InternalFrameModel[frameModels.size()]);
	}
	
	public void removeFrameModel(InternalFrameModel frameModel) {
		frameModels.remove(frameModel);
		fireObservableDataRemoved(frameModel);
	}
	
	public void addFrameModel(InternalFrameModel frameModel) {
		frameModels.add(frameModel);
		fireObservableDataAdded(frameModel);
		setEnableEditing(isEnableEditing());
	}

	/**
	 * Returns the descriptor.
	 * @return Descriptor
	 */
	public Descriptor getDescriptor() {
		return descriptor;
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
	public void setAlias(String alias) {
		String oldValue = this.alias;
		this.alias = alias;
		firePropertyChange(ALIAS, oldValue, alias);
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
		Iterator iterator = frameModels.iterator();
		while (iterator.hasNext()) {
			InternalFrameModel frameModel = (InternalFrameModel) iterator.next();
			frameModel.setEnableEditing(newValue);
		}
	}
	/**
	 * @return
	 */
	public Dimension getPreferredSize() {
		return preferredSize;
	}

	/**
	 * @param dimension
	 */
	public void setPreferredSize(Dimension dimension) {
		preferredSize = dimension;
	}

}
