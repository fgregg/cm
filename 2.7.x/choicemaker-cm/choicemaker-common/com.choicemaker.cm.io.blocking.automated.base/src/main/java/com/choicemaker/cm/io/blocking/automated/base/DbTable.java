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

/**
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:47 $
 */
public class DbTable {
	public final String name;
	public final int num;
	public final String uniqueId;

	public DbTable(String name, int num, String uniqueId) {
		this.name = name;
		this.num = num;
		this.uniqueId = uniqueId;
	}

	public boolean equals(Object o) {
		if (o instanceof DbTable) {
			DbTable ot = (DbTable) o;
			// don't compare num
			return name.equals(ot.name) && uniqueId.equals(ot.uniqueId);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return name.hashCode() + uniqueId.hashCode();
	}
}
