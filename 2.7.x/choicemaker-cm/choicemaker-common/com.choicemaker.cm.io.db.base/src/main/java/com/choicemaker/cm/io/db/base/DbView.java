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
package com.choicemaker.cm.io.db.base;

/**
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:55 $
 */
public class DbView {
	public final int number;
	public final DbField[] fields;
	public final String from;
	public final String where;
	public final DbField[] orderBy;

	public DbView(int number, DbField[] fields, String from, String where, DbField[] orderBy) {
		this.number = number;
		this.fields = fields;
		this.from = from;
		this.where = where;
		this.orderBy = orderBy;
	}
}
