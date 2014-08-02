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
package com.choicemaker.cm.modelmaker.gui.sources;

import com.choicemaker.cm.core.DynamicDispatchHandler;
import com.choicemaker.cm.core.Source;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.dialogs.SourceGui;
/**
 * Description
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:09 $
 */
public interface SourceGuiFactory extends DynamicDispatchHandler {
	String getName();

	SourceGui createGui(ModelMaker parent, Source s);

	SourceGui createGui(ModelMaker parent);

	SourceGui createSaveGui(ModelMaker parent);

	boolean hasSink();

	Class getHandledType();
}
