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
package com.choicemaker.cm.gui.utils.dialogs;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicFileChooserUI;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.base.Constants;
import com.choicemaker.cm.core.util.CustomFileFilter;
import com.choicemaker.cm.core.util.MessageUtil;
import com.choicemaker.cm.core.xmlconf.MarkedRecordPairSourceXmlConf;

/**
 * Contains static methods to 
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public final class FileChooserFactory {

	private static final Logger logger = Logger.getLogger(FileChooserFactory.class);

	//
	// methods that return a JFileChooser
	//

	public static JFileChooser getFileChooser() {
		return getFileChooser(null, null);	
	}

	//
	// methods that return a File or files.
	//

	public static File selectDirectory(Component parent, File sel) {
		return selectFile(parent, sel, dirFilter);
	}

	public static File selectDirectory(Component parent) {
		return selectDirectory(parent, new File(System.getProperty("user.dir")));
	}

	public static File selectConfFile(Component parent, File sel) {
		return selectFile(parent, sel, xmlFilter);
	}

	public static File selectFlatFile(Component parent) {
		File f = selectFile(parent, getTrainDataDir(), flatFileFilter);
		if (f != null) {
			trainDataDir = f.getParentFile();
		}
		return f;
	}

	public static File selectXmlFile(Component parent) {
		File f = selectFile(parent, getTrainDataDir(), xmlFilter);
		if (f != null) {
			trainDataDir = f.getParentFile();	
		}
		return f;
	}

	public static File selectRsFile(Component parent) {
		File f = selectFile(parent, getTrainDataDir(), rsFilter);
		if (f != null) {
			trainDataDir = f.getParentFile();	
		}
		return f;
	}
	
	public static File[] selectRsFiles(Component parent) {
		File[] f = selectFiles(parent, getTrainDataDir(), rsFilter);
		if (f.length > 0) {
			trainDataDir = f[0].getParentFile();	
		}
		return f;
	}

	public static File selectRpsFile(Component parent) {
		File f = selectFile(parent, getTrainDataDir(), rpsFilter);	
		if (f != null) {
			trainDataDir = f.getParentFile();	
		}
		return f;
	}

	public static File[] selectRpsFiles(Component parent) {
		File[] f = selectFiles(parent, getTrainDataDir(), rpsFilter);
		if (f.length > 0) {
			trainDataDir = f[0].getParentFile();
		}
		return f;
	}

	public static File selectMrpsFile(Component parent) {
		File f = selectFile(parent, getTrainDataDir(), getMrpsFilter());	
		if (f != null) {
			trainDataDir = f.getParentFile();	
		}
		return f;
	}

	public static File[] selectMrpsFiles(Component parent) {
		File[] f = selectFiles(parent, getTrainDataDir(), getMrpsFilter());
		if (f.length > 0) {
			trainDataDir = f[0].getParentFile();
		}
		return f;
	}
	
	public static File selectModelFile(Component parent) {
		File f = selectFile(parent, getModelDir(), modelFilter);
		if (f != null) {
			modelDir = f.getParentFile();	
		}
		return f;
	}
	
	public static File selectCluesFile(Component parent) {
		File f = selectFile(parent, getModelDir(), cluesFilter);
		if (f != null) {
			modelDir = f.getParentFile();
		}
		return f;
	}

	public static File selectLayoutFile(Component parent) {
		return selectLayoutFile(parent, JFileChooser.OPEN_DIALOG);
	}

	public static File selectLayoutFile(Component parent, int dialogType) {
		File f = selectFile(parent, getLayoutDir(), layoutFilter, dialogType);
		if (f != null) {
			layoutDir = f.getParentFile();
		}
		return f;			
	}

	//
	// private methods
	//

	private static File selectFile(Component parent, File prev, FileFilter filter) {
		return selectFile(parent, prev, filter, JFileChooser.OPEN_DIALOG);	
	}

	private static File selectFile(Component parent, File prev, FileFilter filter, int dialogType) {
		JFileChooser chooser = getFileChooser(prev, filter);
		chooser.setDialogType(dialogType);
		if (chooser.showDialog(parent, null) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		} else {
			return null;	
		}
	}

	private static File[] selectFiles(Component parent, File prev, FileFilter filter) {
		return selectFiles(parent, prev, filter, JFileChooser.OPEN_DIALOG);
	}
	
	private static File[] selectFiles(Component parent, File prev, FileFilter filter, int dialogType) {
		JFileChooser chooser = getFileChooser(prev, filter);
		chooser.setMultiSelectionEnabled(true);
		chooser.setDialogType(dialogType);
		if (chooser.showDialog(parent, null) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFiles();	
		} else {
			return new File[0];
		}
	}

	private static JFileChooser getFileChooser(File file) {
		resetFileChooser(file, null);
		return chooser;	
	}

	private static JFileChooser getFileChooser(File file, FileFilter filter) {
		resetFileChooser(file, filter);
		return chooser;
	}

	private static void resetFileChooser(File file, FileFilter filter) {
		if (chooser == null) {
			chooser = new JFileChooser();
		}
		
		FileFilter[] filters = chooser.getChoosableFileFilters();
		for (int i = 0; i < filters.length; i++) {
			chooser.removeChoosableFileFilter(filters[i]);	
		}

		if (filter != null)
			chooser.addChoosableFileFilter(filter);
		chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
		if (filter != null)
			chooser.setFileFilter(filter);
		
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(false);

		chooser.setSelectedFile(null);
		try {
			((BasicFileChooserUI)chooser.getUI()).setFileName("");
		} catch (ClassCastException ex) {
			logger.error("Unable to clear file name field", ex);	
		}
		if (file != null) {
			chooser.setCurrentDirectory(file);
		}

		chooser.setApproveButtonText(null);
		chooser.setApproveButtonToolTipText(null);
	}

	private static File getModelDir() {
		if (modelDir != null) {
			return modelDir;	
		} else {
			return new File(Constants.MODELS_DIRECTORY).getAbsoluteFile();	
		}
	}

	private static File getTrainDataDir() {
		if (trainDataDir != null) {
			return trainDataDir;	
		} else {
			return new File(Constants.TRAINDATA_DIRECTORY).getAbsoluteFile();
		}
	}

	private static File getLayoutDir() {
		if (layoutDir != null) {
			return layoutDir;	
		} else {
			return new File(Constants.LAYOUT_DIRECTORY).getAbsoluteFile();	
		}
	}

	private static FileFilter getMrpsFilter() {
		if (mrpsFilter == null) {
			String[] extensions = MarkedRecordPairSourceXmlConf.getMrpsExtensions();
			String message = "Marked Record Pair Sources (";
			if (extensions.length > 0) {
				message += "*." + extensions[0];	
			}
			for (int i = 1; i < extensions.length; i++) {
				message += ", *." + extensions[i];
			}
			message += ")";
			mrpsFilter = new CustomFileFilter(message, extensions, true);
		}
		return mrpsFilter;
	}

	private static JFileChooser chooser;
	
	private static File modelDir;
	private static File trainDataDir;
	private static File layoutDir;

	private static final FileFilter dirFilter = new CustomFileFilter(
		"Directories", new String[0], true);

	private static final FileFilter flatFileFilter = new CustomFileFilter(
		MessageUtil.m.formatMessage("io.flatfile.gui.filefilter"),
		new String[] {Constants.TXT_EXTENSION, Constants.CSV_EXTENSION}, true);

	private static final FileFilter xmlFilter = new CustomFileFilter(
		MessageUtil.m.formatMessage("io.xml.gui.filefilter"),
		Constants.XML_EXTENSION, true);

	private static final FileFilter rsFilter = new CustomFileFilter(
		MessageUtil.m.formatMessage("io.rs.gui.filefilter"),
		Constants.RS_EXTENSION, true);

	private static final FileFilter rpsFilter = new CustomFileFilter(
		MessageUtil.m.formatMessage("io.rps.gui.filefilter"),
		new String[] {Constants.RPS_EXTENSION, Constants.MRPS_EXTENSION}, 
		true);

	private static FileFilter mrpsFilter;
	//private static final FileFilter mrpsFilter = new CustomFileFilter(
	//	MessageUtil.m.formatMessage("io.mrps.gui.filefilter"),
	//	Constants.MRPS_EXTENSION, true);

	private static final FileFilter modelFilter = new CustomFileFilter(
		MessageUtil.m.formatMessage("io.model.gui.filefilter"),
		Constants.MODEL_EXTENSION, true);

	private static final FileFilter cluesFilter = new CustomFileFilter(
		MessageUtil.m.formatMessage("io.clues.gui.filefilter"),
		Constants.CLUES_EXTENSION, true);

	private static final FileFilter layoutFilter = new CustomFileFilter(
		MessageUtil.m.formatMessage("io.layout.gui.filefilter"),
		Constants.LAYOUT_EXTENSION, true);

	private FileChooserFactory() { }

}
