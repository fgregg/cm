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
package com.choicemaker.cm.io.db.oracle.blocking;

import com.choicemaker.cm.io.db.base.DatabaseAbstraction;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1 $ $Date: 2010/01/28 02:02:10 $
 */
public class OraDatabaseAbstraction implements DatabaseAbstraction {

	/**
	 * @see com.choicemaker.cm.io.db.base.plugin.automatedblocking.db.DatabaseAbstraction#getSetDateFormatExpression()
	 */
	public String getSetDateFormatExpression() {
		return "ALTER SESSION SET nls_date_format = 'YYYY-MM-DD'";
	}

	/**
	 * @see com.choicemaker.cm.io.db.base.plugin.automatedblocking.db.DatabaseAbstraction#getSysdateExpression()
	 */
	public String getSysdateExpression() {
		return "sysdate";
	}

	/**
	 * @see com.choicemaker.cm.io.db.base.plugin.automatedblocking.db.DatabaseAbstraction#getDateFieldExpression(java.lang.String)
	 */
	public String getDateFieldExpression(String field) {
		return field;
	}
}
