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

import java.awt.event.*;
import java.beans.*;

import javax.swing.JInternalFrame;

import com.choicemaker.cm.core.RecordData;

/**
 * .
 *
 * @author   Arturo Falck
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public abstract class InternalFrame extends JInternalFrame {
	private InternalFrameModel recordPairFrameModel;

	private boolean ignoreUpdateFromView = false;
	private boolean enableEditing;

	private PropertyChangeListener modelChangeListener;

	/**
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 * @param iconifiable
	 */
	public InternalFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable) {
		super(title, resizable, closable, maximizable, iconifiable);
	}

	public void destroy() {
		recordPairFrameModel.removePropertyChangeListener(modelChangeListener);
	}

	public abstract void setRecordData(RecordData recordData);

	/**
	 * This method is called once when the InternalFrameModel is set.
	 */
	public abstract void initInternalFrameModel();

	/**
	 * Returns the recordPairFrameModel.
	 * @return RecordPairFrameModel
	 */
	public InternalFrameModel getInternalFrameModel() {
		return recordPairFrameModel;
	}
	/**
	 * Sets the recordPairFrameModel.
	 * @param recordPairFrameModel The recordPairFrameModel to set
	 */
	public void setInternalFrameModel(InternalFrameModel recordPairFrameModel) {
		this.recordPairFrameModel = recordPairFrameModel;

		modelChangeListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				setIgnoreUpdateFromView(true);
				updateFromModel();
				setIgnoreUpdateFromView(false);
			}
		};
		recordPairFrameModel.addPropertyChangeListener(modelChangeListener);

		addComponentListener(new ComponentAdapter() {
			private void setBounds() {
				if (!isIgnoreUpdateFromView())
					getInternalFrameModel().setBounds(getBounds());
			}
			/**
			 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
			 */
			public void componentMoved(ComponentEvent e) {
				setBounds();
			}

			/**
			 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
			 */
			public void componentResized(ComponentEvent e) {
				setBounds();
			}
		});

		initInternalFrameModel();
		updateFromModel();

	}

	public void updateFromModel() {
		// AJW: added to fix exception on set layout editable
		if (getInternalFrameModel() == null) {
			return;
		}

		setTitle(getInternalFrameModel().getAlias());
		setBounds(getInternalFrameModel().getBounds());

		enableEditing = getInternalFrameModel().isEnableEditing();
		setClosable(enableEditing);
		setResizable(enableEditing);
	}

	protected void setIgnoreUpdateFromView(boolean ignoreUpdateFromView) {
		this.ignoreUpdateFromView = ignoreUpdateFromView;
	}

	protected boolean isIgnoreUpdateFromView() {
		return ignoreUpdateFromView;
	}

}
