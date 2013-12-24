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
package com.choicemaker.cm.core.gen;

public class CoreTags {
	// XML element and attribute names
	public static final String NODE_TYPE = "nodeType";
	public static final String FIELD = "field";
	public static final String GLOBAL = "global";
	public static final String GLOBAL_EXT = "globalExt";
	public static final String NODE_TYPE_EXT = "nodeTypeExt";
	public static final String STRINGS = "strings";
	public static final String USE = "use";
	public static final String NAME = "name";
	public static final String TYPE = "type";
	public static final String VALID = "valid";
	public static final String SCOPE = "scope";
	public static final String NODE_INIT = "nodeInit";

	// derived fields
	public static final String DERIVED = "derived";
	public static final String SRC = "src";
	public static final String PRE = "pre";
	public static final String VALUE = "value";
	
	// auxiliary names
	public static final String CLASS_NAME = "className";
	public static final String BASE_INTERFACE_NAME = "baseInterfaceName";
	public static final String URM_BASE_INTERFACE_NAME = "urmBaseInterfaceName";	
	public static final String INTERFACE_NAME = "interfaceName";
	public static final String HOLDER_CLASS_NAME = "holderClassName";
	public static final String URM_HOLDER_CLASS_NAME = "urmHolderClassName";	
	public static final String RECORD_NUMBER = "recordNumber";
	public static final String LEVEL = "level";
	public static final String FQ_NAME = "fqName";
	public static final String JAVA_EXTENSION = ".java";

	// common elements in extensions
	public static final int IGNORE = 0;
	public static final int WARN = 1;
	public static final int EXCEPTION = 2;
	public static final String CONF = "conf";
	public static final String VIRTUAL = "virtual";
}
