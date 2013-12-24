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
package com.choicemaker.cm.compiler.backend;

import com.choicemaker.cm.compiler.Tags;

/**
 * Additional tags used in the abstract syntax tree for the target language
 *
 * @author   Matthias Zenger
 * @author   Martin Buechi
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:36 $
 */
public interface TargetTags extends Tags {
	// tree tags
	int CLASSDECL = 100;
	int JMETHODDECL = 101;
	int BLOCK = 102;
	int WHILE = 103;
	int DOWHILE = 104;
	int FOR = 105;
	int TAGED = 106;
	int SWITCH = 107;
	int CASE = 108;
	int BREAK = 109;
	int CONTINUE = 110;
	int RETURN = 111;
	int ASSIGN = 112;
	int TRY = 113;
	int CATCH = 114;
	int COND = 115;

	// self tags
	int THIS = 120;
	int SUPER = 121;
}
