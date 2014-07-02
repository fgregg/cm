/*
 * @(#)$RCSfile: ChoiceMakerInit_02.java,v $        $Revision: 1.6 $ $Date: 2006/01/28 14:32:25 $
 *
 * Copyright (c) 2006 ChoiceMaker Technologies, Inc.
 * 48 Wall Street, 11th Floor, New York, NY 10005
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * ChoiceMaker Technologies Inc. ("Confidential Information").
 */
package com.choicemaker.cm.compiler.app;

import java.io.File;

import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.configure.ChoiceMakerConfiguration;
import com.choicemaker.cm.core.configure.ChoiceMakerConfigurator;
import com.choicemaker.cm.core.configure.InstallableConfigurator;
import com.choicemaker.cm.core.util.FileUtilities;
import com.choicemaker.cm.core.xmlconf.GeneratorXmlConf;

/**
 * Initializes the ChoiceMaker environment once per JVM
 * @author rphall
 */
public final class ChoiceMakerInit_02 {

	private static ChoiceMakerConfiguration configuration;

	public static synchronized boolean isInitialized() {
		return configuration != null;
	}

	public static synchronized void initialize(
		File projectFile,
		boolean reload,
		boolean initGui)
		throws XmlConfException {

		if (!isInitialized()) {
			// Preconditions
			if (projectFile == null) {
				throw new IllegalArgumentException("null project file");
			}
			assert configuration == null;

			ChoiceMakerConfigurator configurator =
					InstallableConfigurator.getInstance();
			String fn = projectFile.getAbsolutePath();
			configuration = configurator.init(fn, reload, initGui);
		}

		// Postconditions
		assert isInitialized();

		return;
	} // initialize(File,String,boolean,boolean)

	public static synchronized void deleteGeneratedCode() {
		if (isInitialized()) {
//			String codeRoot = GeneratorXmlConf.getCodeRoot();
			File f = new File(GeneratorXmlConf.getCodeRoot()).getAbsoluteFile();
			if (f.exists()) {
				System.out.print(
					"Deleting codeRoot('" + f.getAbsoluteFile() + "') ...");
				FileUtilities.removeDir(f);
				System.out.println(" deleted");
			}
		} else {
			throw new IllegalStateException("ChoiceMaker not initialized");
		}
	}

}
