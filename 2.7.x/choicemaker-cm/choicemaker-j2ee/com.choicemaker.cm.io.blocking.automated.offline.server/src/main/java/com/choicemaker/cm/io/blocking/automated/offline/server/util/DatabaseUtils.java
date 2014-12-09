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
package com.choicemaker.cm.io.blocking.automated.offline.server.util;

import java.util.logging.Logger;

import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.core.ISerializableRecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.PersistableRecordSourceControllerBean;

/**
 * @author pcheung
 *
 */
public class DatabaseUtils {
	
	private static final Logger logger = Logger.getLogger(DatabaseUtils.class.getName());
	
	public static ISerializableRecordSource getRecordSource(
			PersistableRecordSourceControllerBean controller,
			PersistableRecordSource prs) {
		if (controller == null) {
			throw new IllegalArgumentException("null controller");
		}
		ISerializableRecordSource retVal = null;
		if (prs == null) {
			logger.warning("null persistable record source");
		} else {
//			retVal = controller.
		}
		return retVal;
	}

}
