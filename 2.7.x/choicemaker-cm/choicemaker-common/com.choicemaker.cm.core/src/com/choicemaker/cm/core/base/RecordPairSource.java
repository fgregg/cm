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
package com.choicemaker.cm.core.base;

import java.io.IOException;

/**
 * Source of record pairs.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/24 22:50:38 $
 */
public interface RecordPairSource extends Source {
	/**
	 * Returns the next record pair.
	 *
	 * @return  The next record pair.
	 * @throws  IOException  if there is a problem retrieving the data.
	 */
	ImmutableRecordPair getNext() throws IOException;
}
