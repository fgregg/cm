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

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import com.choicemaker.cm.core.util.MessageUtil;
import com.choicemaker.cm.gui.utils.JavaHelpUtils;

/**
 * An error dialog for displaying 
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class ErrorDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	public static void showErrorDialog(Frame frame, String message) {
		showErrorDialog(frame, message, null);	
	}
	
	public static void showErrorDialog(Frame frame, Throwable ex) {
		showErrorDialog(frame, ex.getMessage(), ex);
	}
	
	public static void showErrorDialog(Frame frame, String message, Throwable ex) {
		showErrorDialogImpl(frame, message, ex);
	}

	public static void showErrorDialog(Dialog dialog, String message) {
		showErrorDialog(dialog, message, null);
	}
	
	public static void showErrorDialog(Dialog dialog, Throwable ex) {
		showErrorDialog(dialog, ex.getMessage(), ex);
	}
	
	public static void showErrorDialog(Dialog dialog, String message, Throwable ex) {
		showErrorDialogImpl(dialog, message, ex);
	}
	
	protected static void showErrorDialogImpl(Object owner, String message, Throwable ex) {		
		ErrorDialog instance = null;
		if (owner instanceof Frame) {
			instance = new ErrorDialog((Frame)owner);
		} else if (owner instanceof Dialog) {
			instance = new ErrorDialog((Dialog)owner);
		} else {
			throw new IllegalArgumentException("Non frame or dialog argument given");
		}
		
		// set some things...
		instance.setMessage(message);
		instance.setThrowable(ex);
		
		instance.setShowsDetail(false);
		
		// show
		if (owner instanceof Frame) {
			instance.setLocationRelativeTo((Frame)owner);
		} else if (owner instanceof Dialog) {
			instance.setLocationRelativeTo((Dialog)owner);
		} else {
			throw new IllegalArgumentException("Non frame or dialog argument given");
		}
		instance.setVisible(true);
	}

	//
	// Instance stuff
	//
		
	private JTextArea exceptionMessage;
		
	private JButton detailsButton;
	private JButton copyButton;
	private JButton closeButton;
	
	private JScrollPane exceptionScroller;
	private JTextArea exceptionDetail;
	
	private String moreString, lessString;
	
	//
	// non-UI stuff
	//
	
	private String message, stackTraceString;
	
//	private Throwable ex;
	private boolean showsDetail;
	
	protected ErrorDialog(Dialog owner) {
		super(owner);
		init();
	}
	
	protected ErrorDialog(Frame owner) {
		super(owner);
		init();
	}
	
	protected void init() {
		buildPanel();
		addListeners();
		
		setModal(true);

		setTitle(MessageUtil.m.formatMessage("error"));

		moreString = MessageUtil.m.formatMessage("train.gui.modelmaker.dialog.error.more");
		lessString = MessageUtil.m.formatMessage("train.gui.modelmaker.dialog.error.less");

		setShowsDetail(false);
	}
		
	public void setMessage(String message) {
		this.message = message;
		exceptionMessage.setText(message);
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setThrowable(Throwable ex) {
//		this.ex = ex;

		stackTraceString = "";
		
		if (ex != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			ex.printStackTrace(pw);
			stackTraceString = sw.toString();
		}

		exceptionDetail.setText(stackTraceString);
		detailsButton.setEnabled(ex != null);
	}

	public String getStackTraceString() {
		return stackTraceString;
	}

	public boolean getShowsDetail() {
		return showsDetail;
	}
	
	public void setShowsDetail(boolean showsDetail) {
		this.showsDetail = showsDetail;
		
		if (showsDetail) {
			detailsButton.setText(lessString);
			exceptionScroller.setVisible(true);
			pack();
			exceptionScroller.getViewport().setViewPosition(new Point(0,0));
			exceptionScroller.repaint();
		} else {
			detailsButton.setText(moreString);
			exceptionScroller.setVisible(false);
			pack();
		}
	}
	
	private void buildPanel() {
		
		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] {1};
		layout.rowWeights = new double[] {1};
		getContentPane().setLayout(layout);			

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.BOTH;
		
		JPanel content = new JPanel();
		getContentPane().add(content, c);
			
		layout = new GridBagLayout();
		layout.columnWeights = new double[] {0, 0, 0, 0, 1};
		layout.rowWeights = new double[] {0, 0, 1};
		content.setLayout(layout);
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		Icon icon = UIManager.getIcon("OptionPane.errorIcon");
		content.add(new JLabel(icon), c);

		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 4;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.BOTH;
		exceptionMessage = new JTextArea();
		exceptionMessage.setEditable(false);
		JButton fake = new JButton();
		exceptionMessage.setBackground(fake.getBackground());
		exceptionMessage.setFont(fake.getFont());
		exceptionMessage.setLineWrap(true);
		exceptionMessage.setWrapStyleWord(true);
		content.add(exceptionMessage, c);
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		
		c.gridx = 0;
		c.gridy = 1;
		content.add(JavaHelpUtils.createHelpButton(this, "train.gui.dialog.error", "Help"), c);
		
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.SOUTHWEST;
		detailsButton = new JButton();
		detailsButton.setMinimumSize(detailsButton.getPreferredSize());
		content.add(detailsButton, c);
		
		c.gridx = 2;
		c.gridy = 1;
		copyButton = new JButton(MessageUtil.m.formatMessage("train.gui.modelmaker.dialog.error.copytoclipboard"));
		content.add(copyButton, c);
		
		c.gridx = 3;
		c.gridy = 1;
		closeButton = new JButton(MessageUtil.m.formatMessage("close"));
		content.add(closeButton, c);
		
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 5;
		c.fill = GridBagConstraints.BOTH;
		exceptionDetail = new JTextArea(10, 60);
		exceptionDetail.setEditable(false);
		exceptionDetail.setBackground(Color.LIGHT_GRAY);
		exceptionScroller = new JScrollPane(exceptionDetail);
		content.add(exceptionScroller, c);
		
	}

	private void addListeners() {
		
		JavaHelpUtils.enableHelpKey(this, "train.gui.dialog.error");

		detailsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setShowsDetail(!getShowsDetail());
			}
		});
		
		copyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StringSelection ss = new StringSelection(getMessage() + "\n" + getStackTraceString());
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
			}
		});
		
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);	
			}
		});
	}
}
