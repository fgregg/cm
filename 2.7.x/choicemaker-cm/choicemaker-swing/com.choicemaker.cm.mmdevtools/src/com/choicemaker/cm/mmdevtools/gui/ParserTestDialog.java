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
package com.choicemaker.cm.mmdevtools.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.choicemaker.cm.matching.cfg.ParseTreeNode;
import com.choicemaker.cm.matching.cfg.Parser;
import com.choicemaker.cm.matching.cfg.Parsers;
import com.choicemaker.cm.matching.cfg.Token;
import com.choicemaker.cm.matching.cfg.TokenType;
import com.choicemaker.cm.matching.cfg.Variable;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;

/**
 * @author ajwinkel
 *
 */
public class ParserTestDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JComboBox parserBox;
	private JTextField dataField;

	public ParserTestDialog(ModelMaker modelMaker) {
		super(modelMaker, "Parse Address", false);
		
		buildContent();
		
		pack();
		setLocationRelativeTo(modelMaker);
	}

	private void buildContent() {
		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] {0, 1, 0};
		getContentPane().setLayout(layout);
	
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 5, 2, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
	
		// row
		
		c.gridy = 0;
		
		c.gridx = 0;
		getContentPane().add(new JLabel("Parser:"), c);
		
		c.gridx = 1;
		Vector v = new Vector(Parsers.getParserKeys());
		Collections.sort(v);
		parserBox = new JComboBox(new DefaultComboBoxModel(v));
		parserBox.setEditable(false);
		getContentPane().add(parserBox, c);

		// row
		
		c.gridy++;
		c.gridx = 0;
		getContentPane().add(new JLabel("Data:  "), c);

		c.gridx = 1;
		dataField = new JTextField(40);
		getContentPane().add(dataField, c);
		
		c.gridx = 2;
		JButton button = new JButton(new ParseAction());
		getContentPane().add(button, c);
	}
	
	private class ParseAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		public ParseAction() {
			super("Parse");
		}
		public void actionPerformed(ActionEvent e) {
			String s = dataField.getText().trim();
			Parser parser = Parsers.get((String) parserBox.getSelectedItem());
				
			System.out.println("Tokenizations");
			System.out.println("-------------");
			List[] toks = parser.getAllTokenizations(s);
			for (int i = 0; i < toks.length; i++) {
				System.out.println(toks[i].toString());
			}
			System.out.println("\n");
			
			System.out.println("Tokenization Detail");
			System.out.println("-------------------");
			toks = parser.getAllTokenizations(s);
			List tokenTypes = parser.getGrammar().getVariables();
			for (int i = 0; i < toks.length; i++) {
				List tokens = (List) toks[i];
				
				System.out.println(tokens + ": ");

				for (int j = 0; j < tokens.size(); j++) {
					Token tok = (Token) tokens.get(j);
					System.out.print("\t" + tok + ": ");
					for (int k = 0; k < tokenTypes.size(); k++) {
						Variable v = (Variable) tokenTypes.get(k);
						if (v instanceof TokenType) {
							if (((TokenType)v).canHaveToken(tok)) {
								System.out.print(v + ", ");
							}
						}
					}
					System.out.println();
				}
				System.out.println();
			}
			System.out.println("\n");
				
			System.out.println("Parse Trees");
			System.out.println("-----------");
			ParseTreeNode[] parseTrees = parser.getAllParseTrees(s);
			if (parseTrees.length == 0) {
				parseTrees = parser.getAllParseTrees(new String[] {s});
			}
			for (int i = 0; i < parseTrees.length; i++) {
				System.out.println(parseTrees[i].getProbability());
				System.out.println(parseTrees[i].prettyPrint());
				System.out.println();
			}
			System.out.println("\n\n");
		}
	}
		
}
