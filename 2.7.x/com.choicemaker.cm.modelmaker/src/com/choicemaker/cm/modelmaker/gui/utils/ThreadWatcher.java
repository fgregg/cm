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
package com.choicemaker.cm.modelmaker.gui.utils;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.choicemaker.cm.core.util.MessageUtil;

/**
 *
 * @author    
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:10 $
 */
public class ThreadWatcher extends JDialog implements Runnable {
	private Thread worker;
	private Frame owner;
	private String title;
	private String message;
	private boolean interrupted;

	public static boolean watchThread(Thread worker, Frame owner, String title, String message) {
		ThreadWatcher t = new ThreadWatcher(worker, owner, title, message);
		Thread tt = new Thread(t);
		tt.start();
		t.show();
		try {
			tt.join();
		} catch (InterruptedException ex) {
			t.interrupted = true;
		}
		return t.interrupted;
	}

	private ThreadWatcher(final Thread worker, Frame owner, String title, String message) {
		super(owner, title);
		this.worker = worker;
		JPanel content = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		content.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 5, 5);
		c.gridy = 0;
		JLabel l = new JLabel(message);
		content.add(l, c);
		JButton stop = new JButton(MessageUtil.m.formatMessage("cancel"));
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		content.add(stop, c);
		stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				interrupted = true;
				worker.interrupt();
			}
		});
		setContentPane(content);
		setModal(true);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		pack();
		setSize(l.getPreferredSize().width + 20, 100);
		Dimension d1 = getSize();
		Dimension d2 = getToolkit().getScreenSize();
		int x = Math.max((d2.width - d1.width) / 2, 0);
		int y = Math.max((d2.height - d1.height) / 2, 0);
		super.setBounds(x, y, d1.width, d1.height);
	}

	public void run() {
		worker.start();
		try {
			worker.join();
		} catch (InterruptedException ex) {
			interrupted = true;
		}
		dispose();
	}
}
