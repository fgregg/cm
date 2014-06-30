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
package com.choicemaker.cm.core.report;

import com.choicemaker.cm.core.base.Constants;

/**
 * Reports exceptions thrown during matching.
 * @author    Rick Hall
 * @version   $Revision: 1.1 $ $Date: 2010/03/24 20:46:24 $
 */
public class ErrorReporter implements ReporterPlugin {

	private final Throwable thrown;

	public ErrorReporter(Throwable thrown) {
		this.thrown = thrown;
	}

	public void report(StringBuffer b, boolean newLines) {
		b.append("<error>");
		if (newLines)
			b.append(Constants.LINE_SEPARATOR);
		if (this.thrown != null) {
			b.append("<ex cls=\"").append(thrown.getClass().getName()).append(
				"\"");
			b.append(" msg=\"").append(thrown.getMessage()).append("\">");
			if (newLines)
				b.append(Constants.LINE_SEPARATOR);
			Throwable cause = thrown.getCause();
			if (cause != null) {
				b.append("<cause cls=\"").append(
					cause.getClass().getName()).append(
					"\"");
				b.append(" msg=\"").append(cause.getMessage()).append("\"/>");
			}
			b.append("</ex>");
			if (newLines)
				b.append(Constants.LINE_SEPARATOR);
		}
		b.append("</error>");
		if (newLines)
			b.append(Constants.LINE_SEPARATOR);
	}

}

