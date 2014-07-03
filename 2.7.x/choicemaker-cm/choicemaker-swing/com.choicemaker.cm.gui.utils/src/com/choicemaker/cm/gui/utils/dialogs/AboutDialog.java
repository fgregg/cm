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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import com.choicemaker.cm.core.util.MessageUtil;

/**
 * Description
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class AboutDialog extends JDialog {

	private static final long serialVersionUID = 1L;
//	private static Logger logger = Logger.getLogger(AboutDialog.class);

	public AboutDialog(Frame g, String title, String message) {
		super(g, title, false);

		JPanel content = new JPanel(new BorderLayout());

		ImageIcon cmIcon = new ImageIcon(AboutDialog.class.getResource("choicemaker_logo.gif"));
		JLabel lbl = new JLabel(cmIcon);
		JPanel p = new JPanel();
		Border b1 = new BevelBorder(BevelBorder.LOWERED);
		Border b2 = new EmptyBorder(5, 5, 5, 5);
		lbl.setBorder(new CompoundBorder(b1, b2));
		p.add(lbl);
		content.add(p, BorderLayout.WEST);

		JLabel txt = new JLabel(message);
		content.add(txt, BorderLayout.CENTER);

		JButton okay = new JButton(MessageUtil.m.formatMessage("ok"));
		okay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		p = new JPanel();
		p.add(okay);
		content.add(p, BorderLayout.SOUTH);
		setContentPane(content);
		pack();
		// setLocation();
		setLocationRelativeTo(g);
	}
}
