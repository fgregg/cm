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
package com.choicemaker.cm.io.blocking.exact.base.gen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.gen.CoreTags;
import com.choicemaker.cm.core.gen.GenException;
import com.choicemaker.cm.core.gen.GeneratorHelper;
import com.choicemaker.cm.core.gen.GeneratorPlugin;
import com.choicemaker.cm.core.gen.IGenerator;
import com.choicemaker.cm.io.blocking.base.gen.BlockingTags;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:54 $
 */
public class BlockingConfigurationsGenerator implements GeneratorPlugin {
	public static BlockingConfigurationsGenerator instance = new BlockingConfigurationsGenerator();
	private String name;

	private BlockingConfigurationsGenerator() {
	}

	private BlockingConfigurationGenerator[] addConfigurations(IGenerator g) throws IOException, GenException {
		List confList = new ArrayList();
		List blockingDefs = GeneratorHelper.getGlobalExts(g.getRootElement(), BlockingTags.BLOCKING);
		Iterator iBlockingDefs = blockingDefs.iterator();
		while (iBlockingDefs.hasNext()) {
			Element d = (Element) iBlockingDefs.next();
			if (GeneratorHelper.getBooleanAttribute(d, BlockingTags.EXACT, false)) {
				BlockingConfigurationGenerator c =
					new BlockingConfigurationGenerator(g, d.getAttributeValue(CoreTags.CONF), d);
				if (c.generated) {
					confList.add(c);
				}
			}
		}
		return (BlockingConfigurationGenerator[]) confList.toArray(new BlockingConfigurationGenerator[confList.size()]);
	}

	public synchronized void generate(IGenerator g) throws GenException {
		try {
			BlockingConfigurationGenerator[] confs = addConfigurations(g);
			String packageName = g.getPackage() + ".blocking";
			g.addAccessorBody(
				"public Object getExactInMemoryBlockingConfiguration(String name, Object pm) {"
					+ Constants.LINE_SEPARATOR);
			g.addAccessorBody("PositionMap positionMap = (PositionMap)pm;" + Constants.LINE_SEPARATOR);
			for (int i = 0; i < confs.length; ++i) {
				g.addAccessorBody(
					"if(\""
						+ confs[i].name
						+ "\".equals(name)) return new "
						+ packageName
						+ "."
						+ confs[i].className
						+ "(positionMap);"
						+ Constants.LINE_SEPARATOR);
			}
			if (confs.length == 0) {
				g.addAccessorBody("return null;" + Constants.LINE_SEPARATOR);
			} else {
				BlockingGenerator.filesAdded = true;
				g.addAccessorBody(
					"return new "
						+ packageName
						+ "."
						+ confs[0].className
						+ "(positionMap);"
						+ Constants.LINE_SEPARATOR);
			}
			g.addAccessorBody("}" + Constants.LINE_SEPARATOR);
			g.addAccessorBody("private static String[] exactInMemoryBlockingConfigurations = {");
			for (int i = 0; i < confs.length; i++) {
				if (i != 0) {
					g.addAccessorBody(",");
				}
				g.addAccessorBody("\"" + confs[i].name + "\"");
			}
			g.addAccessorBody("};" + Constants.LINE_SEPARATOR);
			g.addAccessorBody("public String[] getExactInMemoryBlockingConfigurations() {");
			g.addAccessorBody("return exactInMemoryBlockingConfigurations;" + Constants.LINE_SEPARATOR);
			g.addAccessorBody("}" + Constants.LINE_SEPARATOR);
		} catch (IOException ex) {
			throw new GenException("Problem writing file.", ex);
		}
	}
}
