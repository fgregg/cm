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
package com.choicemaker.cm.io.xml.gui;

import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.Source;
import com.choicemaker.cm.io.xml.base.XmlRecordSource;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.dialogs.SourceGui;
import com.choicemaker.cm.modelmaker.gui.sources.SourceGuiFactory;

/**
 * Description
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:59 $
 */
public class XmlRecordSourceGuiFactory implements SourceGuiFactory {
	public String getName() {
		return "XML";
	}

	public SourceGui createGui(ModelMaker parent, Source s) {
		return new XmlRecordSourceGui(parent, (RecordSource)s);
	}

	public SourceGui createGui(ModelMaker parent) {
		return createGui(parent, new XmlRecordSource());
	}

	public SourceGui createSaveGui(ModelMaker parent) {
		return createGui(parent, new XmlRecordSource());
	}

	public Object getHandler() {
		return this;
	}

	public Class getHandledType() {
		return XmlRecordSource.class;
	}

	public String toString() {
		return "XML";
	}

	public boolean hasSink() {
		return true;
	}
}
