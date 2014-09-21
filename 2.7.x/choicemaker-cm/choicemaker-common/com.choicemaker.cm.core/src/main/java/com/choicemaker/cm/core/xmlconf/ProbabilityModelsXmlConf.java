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
package com.choicemaker.cm.core.xmlconf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import com.choicemaker.cm.core.Accessor;
import com.choicemaker.cm.core.ChoiceMakerExtensionPoint;
import com.choicemaker.cm.core.ClueDesc;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.MachineLearner;
import com.choicemaker.cm.core.ModelAttributeNames;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.base.MutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.core.base.ProbabilityModel;
import com.choicemaker.cm.core.compiler.CompilationArguments;
import com.choicemaker.cm.core.compiler.CompilerException;
import com.choicemaker.cm.core.compiler.ICompiler;
import com.choicemaker.util.ArrayHelper;
import com.choicemaker.util.FileUtilities;

/**
 * Handling of probability models in XML configuration.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.3 $ $Date: 2011/09/14 00:14:15 $
 */
public class ProbabilityModelsXmlConf {

	private static final Logger logger =
		Logger.getLogger(ProbabilityModelsXmlConf.class.getName());

	public static void saveModel(IProbabilityModel model)
		throws XmlConfException {
		Element m = new Element("ProbabilityModel");
		//m.setAttribute(ModelAttributeNames.AN_CLUE_FILE_NAME, model.getClueFileName());
		m.setAttribute(ModelAttributeNames.AN_CLUE_FILE_NAME, model.getClueFilePath());
		String ts = model.getTrainingSource();
		m.setAttribute(ModelAttributeNames.AN_TRAINING_SOURCE, ts == null ? "" : ts);
		m.setAttribute(
			ModelAttributeNames.AN_TRAINED_WITH_HOLDS,
			String.valueOf(model.isTrainedWithHolds()));
		Date lt = model.getLastTrainingDate();
		m.setAttribute(
			ModelAttributeNames.AN_LAST_TRAINING_DATE,
			lt == null ? "" : String.valueOf(lt.getTime()));
		m.setAttribute(
			ModelAttributeNames.AN_FIRING_THRESHOLD,
			String.valueOf(model.getFiringThreshold()));
		String userName = model.getUserName();
		if (userName == null)
			userName = "";
		m.setAttribute(ModelAttributeNames.AN_USER_NAME, userName);
		m.setAttribute(
			ModelAttributeNames.AN_ENABLE_ALL_CLUES_BEFORE_TRAINING,
			String.valueOf(model.isEnableAllCluesBeforeTraining()));
		m.setAttribute(
			ModelAttributeNames.AN_ENABLE_ALL_RULES_BEFORE_TRAINING,
			String.valueOf(model.isEnableAllRulesBeforeTraining()));
		Accessor acc = model.getAccessor();
		// AJW 1/8/04: the actual accessor class is a dynamic proxy...
		//m.setAttribute(ModelAttributeNames.AN_ACCESSOR_CLASS, acc.getClass().getName());
		m.setAttribute(ModelAttributeNames.AN_ACCESSOR_CLASS, model.getAccessorClassName());
		m.setAttribute(ModelAttributeNames.AN_USE_ANT, String.valueOf(model.isUseAnt()));
		m.setAttribute(ModelAttributeNames.AN_ANT_COMMAND, model.getAntCommand());
		MlModelConf mlc = model.getMachineLearner().getModelConf();
		Element mle = new Element("machineLearner");
		mle.setAttribute("class", mlc.getExtensionPointId());
		m.addContent(mle);
		mlc.saveMachineLearner(mle);
		boolean[] cluesToEvaluate = model.getCluesToEvaluate();
		ClueDesc[] clueDesc = acc.getClueSet().getClueDesc();
		for (int i = 0; i < clueDesc.length; ++i) {
			Element c = new Element("clue");
			m.addContent(c);
			c.setAttribute("name", clueDesc[i].getName());
			c.setAttribute("evaluate", String.valueOf(cluesToEvaluate[i]));
			mlc.saveClue(c, i);
		}
		try {
			String fileName = model.getModelFilePath();
			FileOutputStream fs =
				new FileOutputStream(new File(fileName).getAbsoluteFile());
			XMLOutputter o = new XMLOutputter("    ", true);
			//o.setTextNormalize(true);
			o.output(m, fs);
			fs.close();
		} catch (IOException ex) {
			throw new XmlConfException("Problem writing file.", ex);
		}
	}

	public static IProbabilityModel readModel(
		String fileName,
		ICompiler compiler,
		Writer w)
		throws XmlConfException {
		// Preconditions checked by called to overloaded readModel(..)
		try {
			return readModel(
				fileName,
				new FileInputStream(new File(fileName).getAbsoluteFile()),
				compiler,
				w);
		} catch (FileNotFoundException ex) {
			throw new XmlConfException("Internal error.", ex);
		}
	}

	public static IProbabilityModel readModel(
		String fileName,
		InputStream is,
		ICompiler compiler,
		Writer statusOutput)
		throws XmlConfException {
		return readModel(fileName, is, compiler, statusOutput, null);
	}

	public static IProbabilityModel readModel(
		String fileName,
		InputStream is,
		ICompiler compiler,
		Writer statusOutput,
		ClassLoader customClassLoader)
		throws XmlConfException {

		// Preconditions
		if (is == null) {
			throw new IllegalArgumentException("null input stream");
		}
		if (compiler == null) {
			throw new IllegalArgumentException("null compiler");
		}
		if (statusOutput == null) {
			throw new IllegalArgumentException("null writer");
		}

		Document document = null;
		SAXBuilder builder = XmlParserFactory.createSAXBuilder(false);
		try {
			document = builder.build(is);
		} catch (Exception ex) {
			throw new XmlConfException("Internal error.", ex);
		}
		Element m = document.getRootElement();
		String clueFileName = m.getAttributeValue(ModelAttributeNames.AN_CLUE_FILE_NAME);
		String trainingSource = m.getAttributeValue(ModelAttributeNames.AN_TRAINING_SOURCE);
		if (trainingSource == null)
			trainingSource = "";
		String ltd = m.getAttributeValue(ModelAttributeNames.AN_LAST_TRAINING_DATE);
		boolean trainedWithHolds =
			"true".equals(m.getAttributeValue(ModelAttributeNames.AN_TRAINED_WITH_HOLDS));
		Date lastTrainingDate =
			ltd == null
				|| ltd.length() == 0 ? null : new Date(Long.parseLong(ltd));
		String ft = m.getAttributeValue(ModelAttributeNames.AN_FIRING_THRESHOLD);
		int firingThreshold = 3;
		if (ft != null)
			firingThreshold = Integer.parseInt(ft);
		boolean enableAllCluesBeforeTraining =
			"true".equals(m.getAttributeValue(ModelAttributeNames.AN_ENABLE_ALL_CLUES_BEFORE_TRAINING));
		boolean enableAllRulesBeforeTraining =
			"true".equals(m.getAttributeValue(ModelAttributeNames.AN_ENABLE_ALL_RULES_BEFORE_TRAINING));
		String userName = m.getAttributeValue(ModelAttributeNames.AN_USER_NAME);
		boolean useAnt = "true".equals(m.getAttributeValue(ModelAttributeNames.AN_USE_ANT));
		String antCommand = m.getAttributeValue(ModelAttributeNames.AN_ANT_COMMAND);
		if (antCommand == null)
			antCommand = "";
		String accessorName = m.getAttributeValue(ModelAttributeNames.AN_ACCESSOR_CLASS);
		Accessor accessor = null;

		ClassLoader classLoader = customClassLoader;
		if (classLoader == null) {
			classLoader = XmlConfigurator.getInstance().reload();
		}
		logger.fine("classLoader == " + customClassLoader);

		if (classLoader instanceof URLClassLoader) {
			String resourcePath = accessorName.replace('.', '/') + ".class";
			URL resourceUrl =
				((URLClassLoader) classLoader).findResource(resourcePath);
			if (resourceUrl == null) {
				CompilationArguments arguments = new CompilationArguments();
				//String[] args = { clueFileName };
				String[] args =
					{
						FileUtilities
							.getAbsoluteFile(
								new File(fileName).getParentFile(),
								clueFileName)
							.toString()};
				arguments.enter(args);
				try {
					accessorName = compiler.compile(arguments, statusOutput);
				} catch (CompilerException x) {
					throw new XmlConfException(
						"Compilation error:" + x.toString(),
						x);
				}
				if (accessorName == null) {
					throw new XmlConfException(
						"Compilation error: " + statusOutput.toString());
				}
			}
		}
		try {
			accessor = PMManager.createAccessor(accessorName, classLoader);
		} catch (ClassNotFoundException ex2) {
			throw new XmlConfException("Internal error.", ex2);
		} catch (InstantiationException ex2) {
			throw new XmlConfException("Internal error.", ex2);
		} catch (IllegalAccessException ex2) {
			throw new XmlConfException("Internal error.", ex2);
		}
		ClueDesc[] clueDesc = accessor.getClueSet().getClueDesc();
		Map cm = new HashMap();
		for (int i = 0; i < clueDesc.length; ++i) {
			cm.put(clueDesc[i].getName(), new Integer(i));
		}
		boolean[] cluesToEvaluate = ArrayHelper.getTrueArray(clueDesc.length);
		List cl = m.getChildren("clue");
		int[] oldClueNums = new int[cl.size()];
		int i = 0;
		Iterator iCl = cl.iterator();
		while (iCl.hasNext()) {
			Element c = (Element) iCl.next();
			String name = c.getAttributeValue("name");
			Object o = cm.get(name);
			if (o == null) {
				o = getByOldName(clueDesc, name);
			}
			if (o != null) {
				int index = ((Integer) o).intValue();
				oldClueNums[i] = index;
				cluesToEvaluate[index] =
					Boolean
						.valueOf(c.getAttributeValue("evaluate"))
						.booleanValue();
			} else {
				oldClueNums[i] = -1;
			}
			++i;
		}
		Element mle = m.getChild("machineLearner");
		MachineLearner ml = null;
		try {
			String name = mle.getAttributeValue("class");
			MlModelConf mc =
				(MlModelConf) ExtensionPointMapper.getInstance(
					ChoiceMakerExtensionPoint.CM_CORE_MACHINELEARNER,
					name);
			ml = mc.readMachineLearner(mle, accessor, cl, oldClueNums);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new XmlConfException("Internal error.", ex);
		}
		IProbabilityModel model =
			new ProbabilityModel(
				fileName,
				clueFileName,
				accessor,
				ml,
				cluesToEvaluate,
				trainingSource,
				trainedWithHolds,
				lastTrainingDate,
				useAnt,
				antCommand);
		model.setFiringThreshold(firingThreshold);
		model.setEnableAllCluesBeforeTraining(enableAllCluesBeforeTraining);
		model.setEnableAllRulesBeforeTraining(enableAllRulesBeforeTraining);
		model.setUserName(userName);
		return model;
	}

	private static Integer getByOldName(ClueDesc[] clueDescs, String name) {
		int u = name.lastIndexOf('_');
		if (u != -1 && u < name.length() - 1) {
			try {
				int num = Integer.parseInt(name.substring(u + 1));
				String prefix = name.substring(0, u) + "[";
				int i = 0;
				while (i < clueDescs.length
					&& !clueDescs[i].name.startsWith(prefix)) {
					++i;
				}
				i += num;
				if (i < clueDescs.length
					&& clueDescs[i].name.startsWith(prefix)) {
					return new Integer(i);
				}
			} catch (NumberFormatException ex) {
				logger.info("Caught NumberFormatException: " + ex);
			}
		}
		return null;
	}

	public static void loadProductionProbabilityModels(
		ICompiler compiler,
		boolean fromResource)
		throws XmlConfException {

		// Precondition
		if (compiler == null) {
			throw new IllegalArgumentException("null compiler");
		}

		logger.info("loadProductionProbabilityModels");
		Element x =
			XmlConfigurator.getInstance().getCore().getChild("productionProbabilityModels");
		if (x != null) {
			List l = x.getChildren("model");
			Iterator i = l.iterator();
			while (i.hasNext()) {
				Element e = (Element) i.next();
				String name = e.getAttributeValue("name");
				String fileName = e.getAttributeValue("file");

				IProbabilityModel m;
				if (fromResource) {
					m =
						readModel(
							null,
							ProbabilityModelsXmlConf
								.class
								.getClassLoader()
								.getResourceAsStream(
								"META-INF/" + fileName),
							compiler,
							new StringWriter());
				} else {
					m = readModel(fileName, compiler, new StringWriter());
				}
				
				assert m != null;
				assert m instanceof MutableProbabilityModel;
				((MutableProbabilityModel)m).setModelName(name);

				List props = e.getChildren("property");
				Iterator iProps = props.iterator();
				while (iProps.hasNext()) {
					Element p = (Element) iProps.next();
					m.properties().put(
						p.getAttributeValue("name").intern(),
						p.getAttributeValue("value").intern());
				}
				logger.info("Loaded probability model: " + name);
				PMManager.addModel(m);
			}
		} else {
			logger.warning("Missing Element 'productionProbabilityModels'");
		}
	}

	public static void loadProductionProbabilityModels(ICompiler compiler)
		throws XmlConfException {
		loadProductionProbabilityModels(compiler, false);
	}
}
