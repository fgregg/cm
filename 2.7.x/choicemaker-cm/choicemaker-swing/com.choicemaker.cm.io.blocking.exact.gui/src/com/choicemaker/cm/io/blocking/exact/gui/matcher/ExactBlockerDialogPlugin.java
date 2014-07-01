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
package com.choicemaker.cm.io.blocking.exact.gui.matcher;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.blocking.InMemoryBlocker;
import com.choicemaker.cm.io.blocking.exact.base.CompositeExactInMemoryBlocker;
import com.choicemaker.cm.io.blocking.exact.base.ExactInMemoryBlocker;
import com.choicemaker.cm.io.blocking.exact.base.ExactInMemoryBlockerAccessor;
import com.choicemaker.cm.io.blocking.exact.base.PositionMap;
import com.choicemaker.cm.modelmaker.gui.matcher.MatchDialogBlockerPlugin;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/28 15:30:29 $
 */
public class ExactBlockerDialogPlugin extends MatchDialogBlockerPlugin {
	private static final long serialVersionUID = 1L;
	private JList configurationsList;
	private IProbabilityModel model;

	ExactBlockerDialogPlugin(IProbabilityModel model) {
		this.model = model;
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(new JLabel("Configurations"));
		configurationsList = new JList(((ExactInMemoryBlockerAccessor)model.getAccessor()).getExactInMemoryBlockingConfigurations());
		configurationsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane sp = new JScrollPane();
		sp.getViewport().add(configurationsList);
		sp.setPreferredSize(new Dimension(200, 200));
		add(sp);		
	}

	public InMemoryBlocker getBlocker() {
		PositionMap positionMap = new PositionMap();
		Object[] vals = configurationsList.getSelectedValues();
		ExactInMemoryBlocker[] imbs = new ExactInMemoryBlocker[vals.length];
		ExactInMemoryBlockerAccessor acc = (ExactInMemoryBlockerAccessor)model.getAccessor();
		for (int i = 0; i < imbs.length; i++) {
			imbs[i] = acc.getExactInMemoryBlockingConfiguration((String)vals[i], positionMap);
		}
		return new CompositeExactInMemoryBlocker(imbs, positionMap);
	}
}
