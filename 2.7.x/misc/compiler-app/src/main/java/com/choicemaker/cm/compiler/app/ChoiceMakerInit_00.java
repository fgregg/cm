/*
 * @(#)$RCSfile: ChoiceMakerInit.java,v $        $Revision: 1.6 $ $Date: 2006/01/28 14:32:25 $
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
import com.choicemaker.cm.core.util.FileUtilities;
import com.choicemaker.cm.core.xmlconf.GeneratorXmlConf;
import com.choicemaker.cm.core.xmlconf.XmlConfigurator;

/**
 * Initializes the ChoiceMaker environment once per JVM
 * @author rphall
 */
public final class ChoiceMakerInit_00 {

	private static File theProjectFile = null;
	private static String theLogConfiguration = null;
	private static boolean isInitialized = false;

	public static synchronized boolean isInitialized() {
		return isInitialized;
	}

	public static synchronized void initialize(
		File projectFile,
		String logConfiguration,
		boolean reload,
		boolean initGui)
		throws XmlConfException {

		if (!isInitialized) {
			// Preconditions
			if (projectFile == null) {
				throw new IllegalArgumentException("null project file");
			}
//			if (logConfiguration == null) {
//				throw new IllegalArgumentException("null log configuration name");
//			}

			XmlConfigurator.getInstance().init(
				projectFile.getAbsolutePath(),
				logConfiguration,
				reload,
				initGui);
			theProjectFile = projectFile;
			theLogConfiguration = logConfiguration;
			isInitialized = true;

		} else if (
			!theProjectFile.getAbsolutePath().equals(projectFile.getAbsolutePath())) {
			String msg =
				"Attempt to reinitialize ChoiceMaker with a new project file "
					+ "(current == '"
					+ theProjectFile.getAbsolutePath()
					+ "', new == '"
					+ projectFile.getAbsolutePath() + "')";
			throw new IllegalStateException(msg);

		} else if (!theLogConfiguration.equals(logConfiguration)) {
			String msg =
				"Attempt to reinitialize ChoiceMaker with a new log configuration "
					+ "(current == '"
					+ theLogConfiguration
					+ "', new == '"
					+ logConfiguration
					+ "')";
			throw new IllegalStateException(msg);
		}

		return;
	} // initialize(File,String,boolean,boolean)

	public static synchronized void deleteGeneratedCode() {
		if (isInitialized) {
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
