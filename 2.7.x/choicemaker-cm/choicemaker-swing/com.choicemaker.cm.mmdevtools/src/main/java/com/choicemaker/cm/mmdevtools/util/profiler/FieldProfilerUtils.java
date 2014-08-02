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
package com.choicemaker.cm.mmdevtools.util.profiler;

import java.text.DecimalFormat;

/**
 * @author Adam Winkel
 */
public class FieldProfilerUtils {

	private static DecimalFormat percentFormat = new DecimalFormat("###0.00%");
	private static DecimalFormat doubleTruncator = new DecimalFormat("###########0.000");

	public static String formatPercent(double d) {
		return percentFormat.format(d);
	}

	public static String formatDouble(double d) {
		return doubleTruncator.format(d);
	}

}
