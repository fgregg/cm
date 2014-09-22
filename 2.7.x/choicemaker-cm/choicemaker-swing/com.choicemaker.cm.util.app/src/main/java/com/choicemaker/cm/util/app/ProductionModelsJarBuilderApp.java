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
package com.choicemaker.cm.util.app;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.choicemaker.cm.compiler.util.ProductionModelsJarBuilder;
import com.choicemaker.cm.core.ChoiceMakerExtensionPoint;
import com.choicemaker.cm.core.configure.ConfigurationManager;
import com.choicemaker.cm.core.util.CommandLineArguments;
import com.choicemaker.cm.core.util.ObjectMaker;
import com.choicemaker.e2.CMConfigurationElement;
import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.CMPlatformRunnable;
import com.choicemaker.e2.platform.CMPlatformUtils;

/**
 * @author    Adam Winkel
 * @version
 */
public class ProductionModelsJarBuilderApp implements CMPlatformRunnable {
	
	public static final String CONFIGURATION = "-conf";
	
	public static final String OUTDIR = "-outDir";
	
	public static final String OBJECT_GENERATOR_EXTENSION_POINT =
		ChoiceMakerExtensionPoint.CM_CORE_OBJECTGENERATOR;
	
	public static final String EXECUTABLE_EXTENSION = "class";

	/**
	 * Runs ProductionModelsJarBuilder as an Eclipse command-line application.
	 * 
	 * @param args
	 *            command-line arguments for the application. One is required:
	 *            <ul>
	 *            <li>-conf &lt;<em>path-to-project.xml</em>&gt>;<br>
	 *            e.g.
	 *            <code>-conf C:/eclipse/workspace/projects/citeseer/project.xml</code>
	 *            <li>
	 *            </ul>
	 *            Another is optional:
	 *            <ul>
	 *            <li>-outDir &lt;<em>path-to-output-directory</em>&gt;<br>
	 *            The default value is
	 *            {@link ConfigurationManager#getPackageCodeDirectory() the
	 *            packaged code directory}</li>
	 *            </ul>
	 *            In addition, the usual Java and Eclipse command-line arguments
	 *            are required, namely the extension id for this application. A
	 *            minimal command line for starting this application from the
	 *            Eclipse directory would look like the following:
	 * 
	 *            <pre>
	 * java -cp startup.jar org.eclipse.core.launcher.Main
	 *         -application com.choicemaker.cm.compiler.ProductionModelsJarBuilderApp
	 *         - conf C:/eclipse/workspace/projects/citeseer/project.xml
	 *         - outDir C:/temp
	 * </pre>
	 * 
	 *            This command should be typed on a single line; it is broken
	 *            across multiple lines here for readability.
	 */
	public Object run(Object args) throws Exception {

		CommandLineArguments cla = new CommandLineArguments(true);
		cla.addArgument(CONFIGURATION);
		String outDirName =
			ConfigurationManager.getInstance().getPackagedCodeRoot();
		cla.addArgument(OUTDIR, outDirName);
		cla.enter((String[]) args);

		String conf = cla.getArgument(CONFIGURATION);
		if (conf == null) {
			throw new IllegalArgumentException("Must provide a -conf argument");
		}

		outDirName = cla.getArgument(OUTDIR);
		File outDir = new File(outDirName).getAbsoluteFile();
		if (!outDir.isDirectory()) {
			outDir.mkdirs();
		}

		List omList = new LinkedList();
		CMExtension[] exts =
			CMPlatformUtils.getExtensions(OBJECT_GENERATOR_EXTENSION_POINT);
		for (int i = 0; i < exts.length; i++) {
			CMExtension extension = exts[i];
			CMConfigurationElement[] els = extension.getConfigurationElements();
			for (int j = 0; j < els.length; j++) {
				CMConfigurationElement element = els[j];
				ObjectMaker maker =
					(ObjectMaker) element
							.createExecutableExtension(EXECUTABLE_EXTENSION);
				omList.add(maker);
			}
		}
		ObjectMaker[] objectMakers =
			(ObjectMaker[]) omList.toArray(new ObjectMaker[omList.size()]);
		ProductionModelsJarBuilder pmjb = new ProductionModelsJarBuilder();
		pmjb.run(conf, outDir, objectMakers);

		return null;
	}

}

