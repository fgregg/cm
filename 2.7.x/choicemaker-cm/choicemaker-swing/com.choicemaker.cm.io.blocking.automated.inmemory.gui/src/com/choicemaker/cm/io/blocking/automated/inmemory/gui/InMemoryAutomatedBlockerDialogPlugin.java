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
package com.choicemaker.cm.io.blocking.automated.inmemory.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.blocking.InMemoryBlocker;
import com.choicemaker.cm.io.blocking.automated.base.BlockingAccessor;
import com.choicemaker.cm.io.blocking.automated.inmemory.InMemoryAutomatedBlocker;
//import com.choicemaker.cm.io.blocking.automated.inmemory.InMemoryDataSource;
import com.choicemaker.cm.io.db.base.DbAccessor;
import com.choicemaker.cm.modelmaker.gui.matcher.MatchDialogBlockerPlugin;

/**
 * Description
 *
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/28 09:40:53 $
 */
public class InMemoryAutomatedBlockerDialogPlugin extends MatchDialogBlockerPlugin {

	private static final long serialVersionUID = 1L;
	private static final int MIN_LPBS = 10;
	private static final int MIN_STBSGL = MIN_LPBS;
	private static final int MIN_LSBS = MIN_STBSGL * 2;

	private static final String DEFAULT_DBCONFIG = "default";
	private static final int DEFAULT_LPBS = 50;
	private static final int DEFAULT_STBSGL = 100;
	private static final int DEFAULT_LSBS = 200;

	private IProbabilityModel model;

	private JComboBox dbConfBox, blockingConfBox;
	private JTextField lpbsField, stbsglField, lsbsField;

	InMemoryAutomatedBlockerDialogPlugin(IProbabilityModel model) {
		this.model = model;
		createContent();
	}

	public InMemoryBlocker getBlocker() {

		// easy stuff...
		int limitPerBlockingSet = getInt(lpbsField, MIN_LPBS, DEFAULT_LPBS);
		int singleTableBlockingSetGraceLimit = getInt(stbsglField, Math.min(limitPerBlockingSet, MIN_STBSGL), DEFAULT_STBSGL);
		int limitSingleBlockingSet = getInt(lsbsField, Math.min(singleTableBlockingSetGraceLimit*2, MIN_LSBS), DEFAULT_LSBS);
		String dbConfiguration = (String)dbConfBox.getSelectedItem();
		String blockingConfiguration = (String)blockingConfBox.getSelectedItem();

		// harder stuff...
		// 2014-04-24 rphall: Commented out unused local variable.
//		InMemoryDataSource imds = null;

		InMemoryAutomatedBlocker imab =
			new InMemoryAutomatedBlocker(model,
										 limitPerBlockingSet,
										 singleTableBlockingSetGraceLimit,
										 limitSingleBlockingSet,
										 dbConfiguration,
										 blockingConfiguration);

		// TODO: possibly get/create counts from somewhere else...

		return imab;
	}

	private int getInt(JTextField tf, int min, int def) {
		String text = tf.getText().trim();
		try {
			int res = Integer.parseInt(text);
			return res < min ? min : res;
		} catch (NumberFormatException ex) {
			return def;
		}
	}

	private void createContent() {
		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] {0, 0, 0, 1};
		setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);

		Dimension tfDimension = null;

		//

		c.gridy = 0;
		c.gridx = 0;
		add(Box.createVerticalStrut(10), c);

		//

		c.gridy++;

		c.gridx = 0;
		add(Box.createHorizontalStrut(10), c);

		c.gridx = 1;
		c.anchor = GridBagConstraints.WEST;
		add(new JLabel("Database Configuration"), c);

		c.gridx = 2;
		add(Box.createHorizontalStrut(5), c);

		c.gridx = 3;
		add(new JLabel("Blocking Configuration"), c);

		c.gridx = 4;
		c.fill = GridBagConstraints.BOTH;
		add(new JPanel(), c);
		c.fill = GridBagConstraints.NONE;

		//

		c.gridy++;

		c.gridx = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		dbConfBox = new JComboBox(((DbAccessor)model.getAccessor()).getDbConfigurations());
		dbConfBox.setMinimumSize(dbConfBox.getPreferredSize());
		add(dbConfBox, c);

		c.gridx = 3;
		blockingConfBox = new JComboBox(((BlockingAccessor)model.getAccessor()).getBlockingConfigurations());
		blockingConfBox.setEditable(false);
		tfDimension = blockingConfBox.getPreferredSize();
		blockingConfBox.setMinimumSize(tfDimension);
		add(blockingConfBox, c);

		//

		c.gridy++;
		add(Box.createVerticalStrut(10), c);

		//

		c.gridy++;

		c.gridx = 1;
		add(new JLabel("Max Expected Blocking Set Size"), c);

		c.gridx = 3;
		lpbsField = new JTextField("" + DEFAULT_LPBS, 20);
		lpbsField.setMinimumSize(tfDimension);
		add(lpbsField, c);

		//

		c.gridy++;

		c.gridx = 1;
		add(new JLabel("Single Table Grace Limit"), c);

		c.gridx = 3;
		stbsglField = new JTextField("" + DEFAULT_STBSGL, 20);
		stbsglField.setMinimumSize(tfDimension);
		add(stbsglField, c);

		//

		c.gridy++;

		c.gridx = 1;
		add(new JLabel("Max Records Blocked"), c);

		c.gridx = 3;
		lsbsField = new JTextField("" + DEFAULT_LSBS, 20);
		lsbsField.setMinimumSize(tfDimension);
		add(lsbsField, c);

	}

}
