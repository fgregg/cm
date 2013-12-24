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

import java.io.File;

/**
 * Various constants.
 */
public final class Constants {
	/** The directory in which the models are stored, e.g., etc/models. */
	public static final String MODELS_DIRECTORY = "etc" + File.separator + "models";
	/** The extension of the model files, e.g., model. */
	public static final String MODEL_EXTENSION = "model";
	/** The extension of the clues files, e.g., clues. */
	public static final String CLUES_EXTENSION = "clues";
	/** The directory in which marked record pair descriptors are stored, e.g., etc/traindata. */
	public static final String TRAINDATA_DIRECTORY = "etc" + File.separator + "traindata";
	/** The extension of marked record pair descriptors. */
	public static final String MRPS_EXTENSION = "mrps";
	public static final String RPS_EXTENSION = "rps";
	public static final String RS_EXTENSION = "rs";
	/** The directory in which layouts are stored, e.g., etc/layouts. */
	public static final String LAYOUT_DIRECTORY = "etc" + File.separator + "layouts";
	/** The extension of layout files, e.g., layout. */
	public static final String LAYOUT_EXTENSION = "layout";
	
	public static final String TXT_EXTENSION = "txt";
	public static final String CSV_EXTENSION = "csv";
	public static final String XML_EXTENSION = "xml";
	
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	private Constants() {
	}
}
