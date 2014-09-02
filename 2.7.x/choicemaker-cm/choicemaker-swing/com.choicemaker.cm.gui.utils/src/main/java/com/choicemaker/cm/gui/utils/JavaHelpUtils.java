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
package com.choicemaker.cm.gui.utils;


import java.awt.event.ActionEvent;
import java.net.URL;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

/**
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public final class JavaHelpUtils {

	private static final Logger logger = Logger.getLogger(JavaHelpUtils.class.getName());

	private static final KeyStroke F1 = KeyStroke.getKeyStroke("F1");
	
	private static final String HELP_ACTION_COMMAND = "HELP_ACTION";
	
	private static final String HELP_SET_NAME = "UsersGuide";

	private static boolean initSuccessful = false;

	private static HelpSet helpSet;
	private static HelpBroker helpBroker;
	
	public static JButton createHelpButton(JDialog dialog, String id, String text) {
		return createHelpButton(dialog.getRootPane(), id, text);	
	}
	
	public static JButton createHelpButton(JComponent component, String id, String text) {
		if (initSuccessful) {
			helpBroker.enableHelp(component, id, helpSet);
			
			JButton button = new JButton(text);
			button.addActionListener(new CSH.DisplayHelpFromSource(helpBroker));
			return button;
		} else {
			return new JButton("No Help!");	
		}
	}

	//
	// NOTE: This doesn't seem to work.  Instead, we have to call the 
	// enableHelpKey(component, id) method below.
	//
	//public static void enableHelpKey(Component component, String id) {
	//	helpBroker.enableHelpKey(component, id, helpSet);
	//}

	public static void enableHelpKey(JFrame frame, String id) {
		enableHelpKey(frame.getRootPane(), id);
	}
	
	public static void enableHelpKey(JDialog dialog, String id) {
		enableHelpKey(dialog.getRootPane(), id);	
	}
	
	public static void enableHelpKey(final JComponent component, final String id) {
		if (initSuccessful) {
			CSH.setHelpIDString(component, id);
			component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(F1, HELP_ACTION_COMMAND);
		
			Action helpAction = new AbstractAction() {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					ActionEvent newE = new ActionEvent(component, 0, id);
					new CSH.DisplayHelpFromSource(helpBroker).actionPerformed(newE);
				}	
			};	
		
			component.getActionMap().put(HELP_ACTION_COMMAND, helpAction);
		}
	}

	public static void enableHelp(AbstractButton button, String id) {
		if (initSuccessful) {
			helpBroker.enableHelpOnButton(button, id, helpSet);
		}
	}

	public static void init(ClassLoader cl) {
		try {
//			ClassLoader cl = JavaHelpUtils.class.getClassLoader();
			URL url = HelpSet.findHelpSet(cl, HELP_SET_NAME);
			helpSet = new HelpSet(cl, url);
			initSuccessful = true;
		} catch (HelpSetException e) {
			logger.error("Help Set " + HELP_SET_NAME + " not found", e);
		}

		helpBroker = helpSet.createHelpBroker();
	}

	private JavaHelpUtils() { }

}
