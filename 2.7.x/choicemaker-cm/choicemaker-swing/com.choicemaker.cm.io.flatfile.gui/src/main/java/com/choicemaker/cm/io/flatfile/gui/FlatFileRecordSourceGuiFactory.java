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
package com.choicemaker.cm.io.flatfile.gui;

import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.Source;
import com.choicemaker.cm.io.flatfile.base.FlatFileRecordSource;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.dialogs.SourceGui;
import com.choicemaker.cm.modelmaker.gui.sources.SourceGuiFactory;

/**
 * Description
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:58 $
 */
public class FlatFileRecordSourceGuiFactory implements SourceGuiFactory {
	public String getName() {
		return "FlatFile";
	}

	public SourceGui createGui(ModelMaker parent, Source s) {
		return new FlatFileRecordSourceGui(parent, (RecordSource)s);
	}

	public SourceGui createGui(ModelMaker parent) {
		return createGui(parent, new FlatFileRecordSource());
	}

	public SourceGui createSaveGui(ModelMaker parent) {
		return new FlatFileRecordSourceGui(parent, new FlatFileRecordSource());
	}

	public Object getHandler() {
		return this;
	}

	public Class getHandledType() {
		return FlatFileRecordSource.class;
	}

	public String toString() {
		return "FlatFile";
	}

	public boolean hasSink() {
		return true;
	}
}
