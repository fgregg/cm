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

import com.choicemaker.cm.io.blocking.automated.IField;
import com.choicemaker.cm.io.blocking.automated.IQueryField;

/**
 * A field on a query record, which is compared against {@link DbField master}
 * records to find matches.
 * @author    mbuechi
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:47 $
 */
public class QueryField extends Field implements IQueryField {
	
	private static final long serialVersionUID = 271;

	public QueryField() {
		this(NN_FIELD);
	}

	public QueryField(IField[][] illegalCombinations) {
		super(illegalCombinations);
	}
}
