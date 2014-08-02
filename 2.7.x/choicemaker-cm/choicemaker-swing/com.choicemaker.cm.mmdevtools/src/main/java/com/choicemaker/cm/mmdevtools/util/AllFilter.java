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
package com.choicemaker.cm.mmdevtools.util;

import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.base.RecordData;

/**
 * @author Owner
 *
 */
public class AllFilter implements Filter {
	public boolean satisfy(Record r) {
		return true;	
	}
	public boolean satisfy(RecordData rd) {
		return true;
	}
}
