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
package com.choicemaker.cm.gui.utils.viewer.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import java.util.logging.Logger;

import com.choicemaker.cm.core.Descriptor;
import com.choicemaker.cm.core.util.LoggingObject;
import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.gui.utils.viewer.CompositeFrame;
import com.choicemaker.cm.gui.utils.viewer.CompositeFrameModel;
import com.choicemaker.cm.gui.utils.viewer.RecordPairFrameModel;
import com.choicemaker.cm.gui.utils.viewer.RecordPairViewer;
import com.choicemaker.cm.gui.utils.viewer.dialog.InternalFrameDialog;
import com.choicemaker.cm.gui.utils.viewer.dialog.TabRenameDialog;

/**
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class DesktopPaneMouseListener extends MouseAdapter {
	private static Logger logger = Logger.getLogger(DesktopPaneMouseListener.class.getName());
	
	private boolean enabled;
	private JPopupMenu popup;
	private JMenuItem fitContents;
	private AbstractAction renameFrame;
	private AbstractAction addTab;
	private AbstractAction renameTab;
	private AbstractAction removeTab;
	private RecordPairViewer parent;
	private Descriptor parentDescriptor;
	private Descriptor[] descs;
	private int x;
	private int y;

	public DesktopPaneMouseListener(RecordPairViewer parent, Descriptor parentDescriptor, Descriptor[] descs) {
		this.parent = parent;
		this.parentDescriptor = parentDescriptor;
		this.descs = descs;
		buildMenu();
	}

	private void buildMenu() {
		popup = new JPopupMenu();
		JMenu insert = new JMenu(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.listener.desktop.insert"));
		popup.add(insert);
		for (int i = 0; i < descs.length; ++i) {
			Descriptor d = descs[i];
			JMenuItem ins = new JMenuItem(d.getName());
			insert.add(ins);
			ins.addActionListener(new InsertListener(d));
		}
		
		popup.addSeparator();
		
		popup.add(
			ChoiceMakerCoreMessages.m.formatMessage(
				"train.gui.modelmaker.listener.desktop.add.space.right")).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.growDesktopPane(200, 0);
			};
		});
		
		popup.add(
			ChoiceMakerCoreMessages.m.formatMessage(
				"train.gui.modelmaker.listener.desktop.add.space.bottom")).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.growDesktopPane(0, 200);
			};
		});

		fitContents = popup.add(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.listener.desktop.fitcontents"));
		fitContents.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.fitDesktopPane();
			};
		});

		popup.addSeparator();
		
		JMenu insert2 = new JMenu(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.listener.desktop.layout"));
		popup.add(insert2);
		insert2.add(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.listener.desktop.insert.frame")).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					parent.getRecordPairViewerModel().addFrameModel(new CompositeFrameModel(parentDescriptor, x, y));
				} catch (Exception ex) {
					logger.severe(new LoggingObject("CM-100001").toString() + ": " + ex);
				}
			};
		});
		
		renameFrame = new AbstractAction(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.listener.desktop.rename.frame")) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ev) {
				try {
					new InternalFrameDialog(getParentFrame(), ((CompositeFrame)SwingUtilities.getAncestorOfClass(CompositeFrame.class, parent)).getInternalFrameModel());
				} catch (Exception ex) {
					logger.severe(new LoggingObject("CM-100002").toString() + "; " + ex);
				}
			};
		};
		insert2.add(renameFrame);
		
		insert2.addSeparator();
		
		
		addTab = new AbstractAction(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.listener.desktop.insert.tab")) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ev) {
				try {
					parent.addTab();
				} catch (Exception ex) {
					logger.severe(new LoggingObject("CM-100003").toString() + ": " + ex);
				}
			};
		};
		insert2.add(addTab);
		
		renameTab = new AbstractAction(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.listener.desktop.rename.tab")) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ev) {
				try {
					new TabRenameDialog(getParentFrame(), parent.getRecordPairViewerModel());
				} catch (Exception ex) {
					logger.severe(new LoggingObject("CM-100004").toString() + ": " + ex);
				}
			};
		};
		insert2.add(renameTab);
		
		removeTab = new AbstractAction(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.listener.desktop.remove.tab")) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ev) {
				try {
					parent.removeCurrentTab();
				} catch (Exception ex) {
					logger.severe(new LoggingObject("CM-100005").toString() + ": " + ex);
				}
			};
		};
		insert2.add(removeTab);
		
	}

	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			displayPopup(e);
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			displayPopup(e);
		}
	}

	private void displayPopup(MouseEvent e) {
		if (enabled){
			x = e.getX();
			y = e.getY();
			fitContents.setEnabled(parent.getRecordPairViewerModel().getFrameModels().length > 0);
			renameFrame.setEnabled(SwingUtilities.getAncestorOfClass(CompositeFrame.class, parent) != null);
			addTab.setEnabled(parent.hasParentComposite());
			renameTab.setEnabled(parent.hasSiblings());
			removeTab.setEnabled(parent.hasSiblings());
			popup.show(e.getComponent(), x, y);
		}
	}
	
	private JFrame getParentFrame(){
		return (JFrame)SwingUtilities.getRoot(parent);
	}
	

	private class InsertListener implements ActionListener {
		Descriptor d;
		InsertListener(Descriptor d) {
			this.d = d;
		}
		public void actionPerformed(ActionEvent ev) {
			try {
				int desktopPaneWidth = (int)parent.getVisibleRect().getWidth();
				parent.getRecordPairViewerModel().addFrameModel(new RecordPairFrameModel(d, x, y, desktopPaneWidth));
				parent.fitDesktopPane();
			} catch (Exception ex) {
				logger.severe(new LoggingObject("CM-100006").toString() + ": " + ex);
			}
		}
	}
	

	/**
	 * Sets the enableEditing.
	 * @param enableEditing The enableEditing to set
	 */
	public void setEnableEditing(boolean enableEditing) {
		enabled = enableEditing;
	}

}
