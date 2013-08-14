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
package com.choicemaker.cm.io.xml.base;


/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:58 $
 */
public interface XmlReporterAccessor {
	/**
	 * Returns a <code>XMLRecordOutputter</code> for storing parts of the
	 * query and match record for later reporting.
	 *
	 * @todo     Generalize to work with any format outputter, e.g., flat file and
	 *           Oracle.
	 * @return   a <code>XMLRecordOutputter</code> for storing reportable data.
	 */
	XmlRecordOutputter getReportOutputter();
}
