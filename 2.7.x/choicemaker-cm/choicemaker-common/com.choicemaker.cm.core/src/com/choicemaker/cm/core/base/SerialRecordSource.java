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

import java.io.Serializable;


/**
 * This is a RecordSource that can be serialized.
 * This interface may be deprecated or removed.
 * Use the {@link ISerializableRecordSource} instead.
 * 
 * @author pcheung
 * @see ISerializableRecordSource (preferred).
 */
public interface SerialRecordSource extends Serializable, RecordSource {

}
