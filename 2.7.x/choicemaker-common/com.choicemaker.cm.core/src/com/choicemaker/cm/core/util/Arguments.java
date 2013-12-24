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
package com.choicemaker.cm.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Managing of command line arguments.
 *
 * @author   Matthias Zenger
 * @version  $Revision: 1.2 $ $Date: 2010/03/27 21:05:01 $
 */
public class Arguments {
	public static final String DEFAULT = "$default$";
	private HashMap arguments = new HashMap();
	private List files = new ArrayList();
	private int unknownOptionsHandling;

	public static final int STOP_PROCESSING = 0;
	public static final int SKIP_UNKNOWN = 1;
	public static final int ERROR = 2;

	public Arguments() {
		this(STOP_PROCESSING);
	}

	public Arguments(int unknownOptionsHandling) {
		this.unknownOptionsHandling = unknownOptionsHandling;
	}

	/** define a new option; i.e. an argument that is either present *  or not */
	public void addOption(String option) {
		arguments.put(option, Boolean.FALSE);
	}

	/** define a new argument consisting of an argument name and a default
	 *  value */
	public void addArgument(String option, String defaultVal) {
		arguments.put(option, defaultVal);
	}

	public void addArgument(String option, int defaultVal) {
		arguments.put(option, Integer.toString(defaultVal));
	}

	public void addArgument(String option) {
		arguments.put(option, DEFAULT);
	}

	/** enter arguments from the command line 'args' and return index
	 *  of illegal argument; returns -1 for correct command lines */
	public int enter(String[] args) {
		if (args == null)
			return -1;
		int i = 0;
		while (i < args.length) {
			if (args[i].startsWith("-")) {
				Object val = arguments.get(args[i]);
				if (val == null)
					if (unknownOptionsHandling == STOP_PROCESSING) {
						return -1;
					} else if (unknownOptionsHandling == SKIP_UNKNOWN) {
						++i;
						if (i < args.length && !args[i].startsWith("-")) {
							++i;
						}
					} else { // ERROR
						return i;
					}
				else if (val instanceof Boolean)
					arguments.put(args[i++], Boolean.TRUE);
				else if (i == (args.length - 1))
					return i;
				else
					arguments.put(args[i++], args[i++]);
			} else {
				files.add(args[i++]);
			}
		}
		return -1;
	}

	/** check if option was set */
	public boolean optionSet(String option) {
		return ((Boolean) arguments.get(option)).booleanValue();
	}

	/** check the argument value */
	public String argumentVal(String option) {
		Object o = arguments.get(option);
		return (String) ((o == DEFAULT) ? null : o);
	}

	/** return all files */
	public String[] files() {
		return (String[]) files.toArray(new String[files.size()]);
	}
}
