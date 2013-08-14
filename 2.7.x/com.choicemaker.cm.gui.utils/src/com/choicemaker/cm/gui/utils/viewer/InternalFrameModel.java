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
import com.choicemaker.cm.core.datamodel.ObservableData;
/**
 * Common interface to .
 *
 * @author   Arturo Falck
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public interface InternalFrameModel extends ObservableData {
	
	//****************** Constants
	
	public static final String ENABLE_EDITING = "ENABLE_EDITING";
	public static final String BOUNDS = "BOUNDS";
	public static final String ALIAS = "ALIAS";
	public static final String DESCRIPTOR = "DESCRIPTOR";
	
	//****************** Accessor Methods
	
	Descriptor getDescriptor();
	void setDescriptor(Descriptor newValue);
	
	String getAlias();
	void setAlias(String newValue);
	
	Rectangle getBounds();
	void setBounds(Rectangle newValue);
	
	boolean isEnableEditing();
	void setEnableEditing(boolean newValue);
}
