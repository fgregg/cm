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
package com.choicemaker.cm.io.blocking.automated.base;

import java.util.Iterator;

import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.report.ReporterPlugin;
import com.choicemaker.cm.core.util.XmlOutput;
import com.choicemaker.cm.io.blocking.automated.AutomatedBlocker;
import com.choicemaker.cm.io.blocking.automated.IBlockingSet;
import com.choicemaker.cm.io.blocking.automated.IBlockingValue;

/**
 * Description
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/24 21:34:39 $
 */
public class BlockingSetReporter implements ReporterPlugin {
	private final AutomatedBlocker blocker;

	public BlockingSetReporter(AutomatedBlocker blocker) {
		this.blocker = blocker;
	}

	public void report(StringBuffer b, boolean newLines) {
		b.append("<blocking>");
		if(newLines) b.append(Constants.LINE_SEPARATOR);
		Iterator<IBlockingSet> iBlockingSets = blocker.getBlockingSets().iterator();
		while (iBlockingSets.hasNext()) {
			IBlockingSet bs = (IBlockingSet) iBlockingSets.next();
			b.append("<bs ec=\"").append(bs.getExpectedCount()).append("\">");
			if(newLines) b.append(Constants.LINE_SEPARATOR);			
			int size = bs.numFields();
			for (int j = 0; j < size; ++j) {
				IBlockingValue bv = bs.getBlockingValue(j);
				b.append("<bv n=\"").append(bv.getBlockingField().getNumber());
				b.append("\" v=\"").append(XmlOutput.escapeAttributeEntities(bv.getValue())).append("\"/>");
				if(newLines) b.append(Constants.LINE_SEPARATOR);
			}
			b.append("</bs>");
			if(newLines) b.append(Constants.LINE_SEPARATOR);
		}
		b.append("</blocking>");
		if(newLines) b.append(Constants.LINE_SEPARATOR);
	}
}
