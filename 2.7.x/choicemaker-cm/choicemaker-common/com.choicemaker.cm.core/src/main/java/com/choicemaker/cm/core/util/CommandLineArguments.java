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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.choicemaker.cm.core.ChoiceMakerExtensionPoint;
import com.choicemaker.e2.CMConfigurationElement;
import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.platform.CMPlatformUtils;

/**
 * Managing of command line arguments.
 *
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/27 21:11:09 $
 */
public class CommandLineArguments {
	private static final String NOT_DEFINED = "$NOT_DEFINED$";
	public static final String COMMAND_LINE_ARGUMENT = ChoiceMakerExtensionPoint.CM_CORE_COMMANDLINEARGUMENT;
	public static final String NAME = "name";
	public static final String DEFAULT_VALUE = "defaultValue";
	public static final String OPTION = "option";
	public static final String ARGUMENT = "argument";

	private boolean error;
	private boolean ignoreUnknown;
	private HashMap arguments;

	public CommandLineArguments() {
		this(false);
	}

	public CommandLineArguments(boolean ignoreUnknown) {
		this.ignoreUnknown = ignoreUnknown;
		arguments = new HashMap();
	}

	public void addExtensions() {
		CMExtension[] extensions = CMPlatformUtils.getExtensions(COMMAND_LINE_ARGUMENT);
		for (int i = 0; i < extensions.length; i++) {
			CMConfigurationElement[] configElems =
				extensions[i].getConfigurationElements();
			for (int j = 0; j < configElems.length; j++) {
				CMConfigurationElement elem = configElems[j];
				String name = elem.getName().intern();
				if (name == OPTION) {
					addOption(elem.getAttribute(NAME));
				} else if (name == ARGUMENT) {
					String defaultValue = elem.getAttribute(DEFAULT_VALUE);
					if (defaultValue == null) {
						addArgument(elem.getAttribute(NAME));
					} else {
						addArgument(elem.getAttribute(NAME), defaultValue);
					}
				}
			}
		}
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

	public void addArgument(String option) {
		arguments.put(option, NOT_DEFINED);
	}

	/** enter arguments from the command line 'args' and return index
	 *  of illegal argument; returns -1 for correct command lines */
	public void enter(String[] args) {
		int i = 0;
		while (i < args.length) {
			if (args[i].startsWith("-")) {
				Object val = arguments.get(args[i]);
				if (val == null) {
					if (ignoreUnknown) {
						++i;
					} else {
						error = true;
						return;
					}
				} else if (val instanceof Boolean) {
					arguments.put(args[i++], Boolean.TRUE);
				} else if (i < args.length - 1) {
					arguments.put(args[i++], args[i++]);
				} else {
					error = true;
					return;
				}
			} else if (ignoreUnknown) {
				++i;
			} else {
				error = true;
				return;
			}
		}
	}
	
	public String[] toStringArray() {
		Map snapshot = new HashMap(this.arguments);
		List list = new ArrayList();
		Set keys = snapshot.keySet();
		for (Iterator i=keys.iterator(); i.hasNext(); ) {
			String key = (String) i.next();
			Object o = snapshot.get(key);
			if (o instanceof Boolean) {
				Boolean value = (Boolean) o;
				if (value.booleanValue())  {
					list.add(key);
				}
			} else if (o instanceof String) {
				String value = (String) o;
				boolean isDefined = !value.equals(NOT_DEFINED);
				if (isDefined) {
					list.add(key);
					list.add(value);
				}
			} else {
				// Something  unexpected crept into the map
				String unexpectedType = o == null ? "null" : o.getClass().getName() ;
				String msg = "Unexpected value type: '" + unexpectedType + "'";
				throw new Error(msg);
			}
		}
		String[] retVal = (String[]) list.toArray(new String[list.size()]);
		return retVal;
	}

	/** check if option was set */
	public boolean optionSet(String option) {
		return ((Boolean) arguments.get(option)).booleanValue();
	}

	/** check the argument value */
	public String getArgument(String option) {
		String s = (String) arguments.get(option);
		return s.equals(NOT_DEFINED) ? null : s;
	}

	public boolean isError() {
		return error;
	}

	public static String[] eclipseArgsMapper(Object o) {
		String[] os = (String[]) o;
		if (os[0].equals("-pdelaunch")) {
			String[] res = new String[os.length - 3];
			System.arraycopy(os, 3, res, 0, res.length);
			return res;
		} else {
			return os;
		}
	}
}
