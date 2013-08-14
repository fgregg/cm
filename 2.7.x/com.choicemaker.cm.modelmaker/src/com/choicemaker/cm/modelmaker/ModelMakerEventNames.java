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
package com.choicemaker.cm.modelmaker;

/**
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/28 17:14:24 $
 */
public class ModelMakerEventNames {

	private ModelMakerEventNames() {
	}

	public static final String PROBABILITY_MODEL = "probabilityModel".intern();

	public static final String MARKED_RECORD_PAIR_SOURCE =
		"markedRecordPairSource".intern();

	public static final String THRESHOLDS = "thresholds".intern();

	public static final String MARKED_RECORD_PAIR_SOURCE_SET =
		"markedRecordPairSourceSet".intern();

	public static final String SOURCE_RECORD_DATA = "sourceRecordData".intern();

	public static final String SOURCE_MARKUP_DATA = "sourceMarkupData".intern();

	public static final String MARKED_RECORD_PAIR = "markedRecordPair".intern();

	public static final String CHECKED_INDICES = "checkedIndices".intern();

	public static final String MULTI_INCLUDE_HOLDS = "multiIncludeHolds".intern();

}

