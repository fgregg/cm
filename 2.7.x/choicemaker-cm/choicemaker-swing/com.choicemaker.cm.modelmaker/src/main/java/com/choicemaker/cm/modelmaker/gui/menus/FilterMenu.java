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
package com.choicemaker.cm.modelmaker.gui.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.panels.HumanReviewPanel;

/**
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.2 $ $Date: 2010/03/29 13:09:43 $
 */
public class FilterMenu extends JMenu implements ActionListener {

    private static final long serialVersionUID = 1L;


	// private AbstractApplication humanReview;
    private HumanReviewPanel humanReview;
//    private ButtonGroup bGroup;
    private static final String ENABLE = ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.filter.set");

    public FilterMenu(HumanReviewPanel g) {
        super(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.filter"));
        humanReview = g;
        buildMenu();
        setMnemonic(KeyEvent.VK_R);
    }

    public void buildMenu() {
        JMenuItem item = new JMenuItem(ENABLE);
        item.addActionListener(this);
        add(item);
        add(new AbstractAction("Select all") {

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
//				humanReview.getFilter().reset();
//				humanReview.filterMarkedRecordPairList();
				 ModelMaker modelMaker = humanReview.getModelMaker();
				 modelMaker.getFilter().reset();
				 modelMaker.filterMarkedRecordPairList();
			}
        });
    }

    public void actionPerformed(ActionEvent ev) {
        humanReview.displayRecordPairFilterDialog();
    }

}
