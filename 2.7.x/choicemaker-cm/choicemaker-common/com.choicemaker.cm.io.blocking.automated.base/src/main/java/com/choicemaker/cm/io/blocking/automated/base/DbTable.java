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
package com.choicemaker.cm.io.blocking.automated.base;

import java.io.Serializable;

import com.choicemaker.cm.io.blocking.automated.IDbTable;

/**
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:47 $
 */
public class DbTable implements Serializable, IDbTable {
	
	private static final long serialVersionUID = 271;

	private final String name;
	private final int num;
	private final String uniqueId;

	public DbTable(String name, int num, String uniqueId) {
		this.name = name;
		this.num = num;
		this.uniqueId = uniqueId;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getNum() {
		return num;
	}

	@Override
	public String getUniqueId() {
		return uniqueId;
	}

	public boolean equals(Object o) {
		if (o instanceof DbTable) {
			IDbTable ot = (IDbTable) o;
			// don't compare num
			return getName().equals(ot.getName()) && getUniqueId().equals(ot.getUniqueId());
		} else {
			return false;
		}
	}

	public int hashCode() {
		return getName().hashCode() + getUniqueId().hashCode();
	}

}
