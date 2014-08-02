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

import java.awt.BorderLayout;
import java.awt.Dimension;

import com.choicemaker.cm.core.base.RecordData;


/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class CompositeFrame extends InternalFrame{

	private static final long serialVersionUID = 1L;
	private boolean contentEditable;
	private CompositePane pane;
	private boolean pair;

	public CompositeFrame(boolean pair, boolean contentEditable) {
		super("", true, true, false, false);
		this.pair = pair;
		this.contentEditable = contentEditable;
		setFrameIcon(null);
		setMinimumSize(new Dimension(10, 10));
		
		getContentPane().setLayout(new BorderLayout());
	}
	
	public void destroy() {
		super.destroy();
		pane.destroy();
	}

	/**
	 * Sets the recordPairFrameModel.
	 * @param recordPairFrameModel The recordPairFrameModel to set
	 */
	public void initInternalFrameModel() {
		
		pane = new CompositePane(pair, contentEditable);
		pane.setCompositePaneModel(((CompositeFrameModel)getInternalFrameModel()).getCompositePaneModel());
		getContentPane().add(pane);
		
	}
	

	public void setRecordData(RecordData recordData) {
		pane.setRecordData(recordData);
	}
}
