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
package com.choicemaker.cm.modelmaker.gui.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import com.choicemaker.cm.core.base.Source;
import com.choicemaker.cm.core.util.MessageUtil;
import com.choicemaker.cm.gui.utils.ExtensionHolder;
import com.choicemaker.cm.gui.utils.JavaHelpUtils;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.sources.SourceGuiFactory;

/**
 * Description
 *
 * @author S. Yoakum-Stover
 * @version $Revision: 1.2 $ $Date: 2010/03/29 12:47:39 $
 */
public class SourceTypeSelectorDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(SourceTypeSelectorDialog.class);

	public static int MRPS = -1;
	public static int RS = -2;

	protected ModelMaker parent;
	private JPanel content;
	private JList sourceTypeList;
	private JScrollPane sourceTypeScrollPane;
	private JLabel sourceTypeLabel;
	private JButton okayButton;
	private JButton cancelButton;
	private boolean save;
	private Source source;

	private int type;

	public SourceTypeSelectorDialog(ModelMaker g, boolean save) {
		this(g, MRPS, save);
	}

	public SourceTypeSelectorDialog(ModelMaker g, int type, boolean save) {
		super(g, MessageUtil.m.formatMessage("train.gui.modelmaker.dialog.source.typeselector.label"), true);
		parent = g;
		this.type = type;
		this.save = save;
		buildContent();
		addContentListeners();
		layoutContent();
		this.setContentPane(content);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(parent);
		setEnabledness();
	}

	public Source define() {
		show();
		setEnabledness();
		return source;
	}

	private void setEnabledness() {
		okayButton.setEnabled(!sourceTypeList.isSelectionEmpty());
	}

	private void buildContent() {
		content = new JPanel();
		sourceTypeLabel =
			new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.dialog.source.typeselector.available"));
		IExtensionPoint extensionPoint = null;
		if (type == RS) {
			extensionPoint = Platform.getPluginRegistry().getExtensionPoint("com.choicemaker.cm.modelmaker.rsReaderGui");
		} else {
			extensionPoint = Platform.getPluginRegistry().getExtensionPoint("com.choicemaker.cm.modelmaker.mrpsReaderGui");
		}
		if (save) {
			Vector l = new Vector();
			IExtension[] extensions = extensionPoint.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IExtension ext = extensions[i];
				if(Boolean.valueOf(ext.getConfigurationElements()[0].getAttribute("hasSink")).booleanValue()) {
					l.add(new ExtensionHolder(ext));
				}
			}
			sourceTypeList = new JList(l);
		} else {
			sourceTypeList =
				new JList(
					ExtensionHolder.getExtensionHolders(extensionPoint
						));
		}
		sourceTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sourceTypeList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				setEnabledness();
			}
		});
		sourceTypeScrollPane = new JScrollPane();
		sourceTypeScrollPane.getViewport().add(sourceTypeList);

		okayButton = new JButton(MessageUtil.m.formatMessage("new.elipsis"));
		cancelButton = new JButton(MessageUtil.m.formatMessage("cancel"));
	}

	private void showSource() {
		SourceGuiFactory sourceGuiFactory = null;
		try {
			sourceGuiFactory = (SourceGuiFactory) ((ExtensionHolder) sourceTypeList.getSelectedValue()).getInstance();
			dispose();
			SourceGui sourceGui = save ? sourceGuiFactory.createSaveGui(parent) : sourceGuiFactory.createGui(parent);
			source = sourceGui.define();
		} catch (CoreException e) {
			logger.error(e.toString(),e);;
		}
	}

	private void addContentListeners() {
		sourceTypeList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					showSource();
				}
			}
		});

		//cancelButton
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				dispose();
			}
		});
		//okayButton
		okayButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showSource();
			}
		});

		JavaHelpUtils.enableHelpKey(this, "train.gui.dialog.sourcetypeselector");
	}

	private void layoutContent() {
		GridBagLayout layout = new GridBagLayout();
		content.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 5, 5);

		//Row 1......................................
		//sourceTypeLabel
		c.gridy = 0;
		c.gridx = 0;
		c.anchor = GridBagConstraints.WEST;
		content.add(sourceTypeLabel, c);

		//Row 2 ........................................
		//sourceTypeScrollPane
		c.gridy = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		layout.setConstraints(sourceTypeScrollPane, c);
		content.add(sourceTypeScrollPane);

		JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 10));
		buttons.add(okayButton);
		buttons.add(cancelButton);
		c.gridy = 2;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(buttons, c);
	}
}
