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
package com.choicemaker.cm.core.datamodel;
/**
 * .
 *
 * @author   Arturo Falck
 * @version  $Revision: 1.1 $ $Date: 2010/01/20 15:05:06 $
 */
public interface CompositeObservableData extends ObservableData{
	void addCompositeObservableDataListener(ObservableDataListener listener);
	ObservableDataListener[] getCompositeObservableDataListeners();
	void removeCompositeObservableDataListener(ObservableDataListener listener);
	ObservableData[] getObservableData();
}
