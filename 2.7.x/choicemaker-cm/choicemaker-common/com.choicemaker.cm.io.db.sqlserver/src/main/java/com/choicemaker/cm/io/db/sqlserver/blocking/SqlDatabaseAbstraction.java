/**
 * @(#)$RCSfile: SqlDatabaseAbstraction.java,v $  $Revision: 1.1 $ $Date: 2003/04/29 19:38:12 $
 * 
 * Copyright (c) 2003 ChoiceMaker Technologies, Inc. 
 * 41 East 11th Street, New York, NY 10003 
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of 
 * ChoiceMaker Technologies Inc. ("Confidential Information"). 
 */
package com.choicemaker.cm.io.db.sqlserver.blocking;

import com.choicemaker.cm.io.db.base.DatabaseAbstraction;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1 $ $Date: 2003/04/29 19:38:12 $
 */
public class SqlDatabaseAbstraction implements DatabaseAbstraction {

	/**
	 * @see com.choicemaker.cm.io.db.base.plugin.automatedblocking.db.DatabaseAbstraction#getSetDateFormatExpression()
	 */
	public String getSetDateFormatExpression() {
		return "SET DATEFORMAT ymd"; // doesn't really matter
	}

	/**
	 * @see com.choicemaker.cm.io.db.base.plugin.automatedblocking.db.DatabaseAbstraction#getSysdateExpression()
	 */
	public String getSysdateExpression() {
		return "getdate()";
	}

	/**
	 * @see com.choicemaker.cm.io.db.base.plugin.automatedblocking.db.DatabaseAbstraction#getDateFieldExpression(java.lang.String)
	 */
	public String getDateFieldExpression(String field) {
		return "Convert(VARCHAR(10), " + field + ", 120)";
	}
}
