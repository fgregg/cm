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
package com.choicemaker.cm.core;

import java.io.Serializable;

/**
 * Base interface for all profiles describing a query record.
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.2 $ $Date: 2010/03/24 20:59:50 $
 */
public interface Profile extends Serializable {
	Record getRecord(ImmutableProbabilityModel model) throws InvalidProfileException;
}
