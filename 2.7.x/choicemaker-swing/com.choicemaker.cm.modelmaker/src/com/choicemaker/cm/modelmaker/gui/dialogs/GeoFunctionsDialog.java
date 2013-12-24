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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.util.UpperCaseTextField;
import com.choicemaker.cm.gui.utils.JavaHelpUtils;
import com.choicemaker.cm.matching.geo.GeoHelper;
import com.choicemaker.cm.matching.geo.GeoMap;
import com.choicemaker.cm.matching.geo.GeoPoint;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.utils.Enable;
import com.choicemaker.cm.modelmaker.gui.utils.EnablednessGuard;

/**
 * Dialog that demonstrates geo function calculations. It uses geo-entities loaded by the current project.
 * 
 * @author   Elmer Moussikaev
 * @since 2.7
 */

public class GeoFunctionsDialog extends JDialog {
	private static DecimalFormat df = new DecimalFormat("####0.00000");
	private static Logger logger = Logger.getLogger(GeoFunctionsDialog.class);

	private ModelMaker parent;

	// keep Enableness Guard as a separate object to be able to handle differently changes in the different parts of the dialog
	GeEnablenessGuard  entityGuard = new GeEnablenessGuard(this);
	EnablednessGuard dle = new EnablednessGuard(entityGuard);
	
	private JTextField gpLatEnt1;
	private JTextField gpLatEnt2;
	private JTextField gpLonEnt1;
	private JTextField gpLonEnt2;

	private JTextField dist;

	
	private JComboBox unitBox;
	private JComboBox geTypeBox1;
	private JComboBox geTypeBox2;
	private DefaultComboBoxModel geTypeBoxModel1 = new DefaultComboBoxModel();
	private DefaultComboBoxModel geTypeBoxModel2 = new DefaultComboBoxModel();
	
	private JButton closeButton;
	private JButton clearButton;
	
	JPanel entPanel1 = new JPanel();
	JPanel entPanel2 = new JPanel();

	JPanel entGroup1 = new JPanel();
	JPanel entGroup2 = new JPanel();
		
	Component	ent1Comp[] = new Component[10];
	int 		ent1CompNumb = 0;

	Component	ent2Comp[] = new Component[10];
	int 		ent2CompNumb = 0;
	


	public GeoFunctionsDialog(ModelMaker parent) {
		super(parent, "Geographical Entities Distance Functions", false);
		
		this.parent = parent;
		
		buildPanel();
		addListeners();

		repopulateAvailableCollections();

		refreshEntityDist();
		pack();
		
		setLocationRelativeTo(parent);	
	}

	private void buildPanel() {
		
		// Add a content 
		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] {1};
		layout.rowWeights = new double[] {1};
		getContentPane().setLayout(layout);			

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(4, 5, 5, 5);
		c.fill = GridBagConstraints.BOTH;
		
		JPanel content = new JPanel();
		getContentPane().add(content, c);
			
		layout = new GridBagLayout();
		layout.columnWeights = new double[] {0, 1, 1,1,1,1};
		layout.rowWeights = new double[] {0, 1, 1,1,1,1,1};
		content.setLayout(layout);
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 5;
		JPanel gentPannel = new JPanel();
		gentPannel.setBorder(new TitledBorder("Distance between geo-entities"));
		content.add(gentPannel, c);
		
		layout = new GridBagLayout();
		layout.columnWeights = new double[] {1, 1, 1, 1, 1};
		layout.rowWeights = new double[] {1, 1, 1, 1, 1};
		gentPannel.setLayout(layout);	

		c.gridx = 3;
		c.gridy = 1;
		c.gridwidth = 1;
		clearButton = new JButton("Clear");
		content.add(clearButton , c);

		c.gridx = 4;
		c.gridy = 1;
		c.gridwidth = 1;
		closeButton = new JButton("Close");
		content.add(closeButton , c);
		
		//geo-entity pannel		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 6;
		entGroup1.setBorder(new TitledBorder("Entity 1"));
		gentPannel.add(entGroup1, c);
		
		layout = new GridBagLayout();
		layout.columnWeights = new double[] {1, 1, 0, 1};
		layout.rowWeights = new double[] {1, 1, 0, 1};
		entGroup1.setLayout(layout);
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 6;
		entGroup2.setBorder(new TitledBorder("Entity 2"));
		gentPannel.add(entGroup2, c);

		layout = new GridBagLayout();
		layout.columnWeights = new double[] {1, 1, 0, 1};
		layout.rowWeights = new double[] {1, 1, 0, 1};
		entGroup2.setLayout(layout);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 6;
		c.fill = GridBagConstraints.HORIZONTAL;
		gentPannel.add(new JSeparator(), c);
		c.fill = GridBagConstraints.NONE;

		
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		gentPannel.add(new JLabel("Distance"), c);

		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 1;
		dist = new UpperCaseTextField(10);
		dist.setEditable(false);
		dist.setHorizontalAlignment(JTextField.LEFT);
		gentPannel.add(dist, c);		

		c.gridx = 2;
		c.gridy = 3;
		c.gridwidth = 1;		
		c.anchor = GridBagConstraints.WEST;
		
		unitBox = new JComboBox();
		unitBox.addItem("mi");
		unitBox.addItem("km");
		gentPannel.add(unitBox, c);
		c.anchor = GridBagConstraints.CENTER;

		//entity 1 pannel child of geo-entity pannel		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 6;
		c.anchor = GridBagConstraints.WEST;
		entGroup1.add(entPanel1, c);
		c.anchor = GridBagConstraints.CENTER;

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;		
		geTypeBox1 = new JComboBox(geTypeBoxModel1);
		c.fill = GridBagConstraints.HORIZONTAL;
		entPanel1.add(geTypeBox1, c);
		c.fill = GridBagConstraints.NONE;
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		entGroup1.add(new JLabel("Latitude"), c);
		
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		gpLatEnt1 = new UpperCaseTextField(10);
		gpLatEnt1.setEditable(false);
		entGroup1.add(gpLatEnt1, c);		

		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(1, 1, 1, 1);
		c.anchor = GridBagConstraints.NORTHWEST;
		entGroup1.add(new JLabel("\260"), c);
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(4, 5, 5, 5);

		c.gridx = 3;
		c.gridy = 1;
		c.gridwidth = 1;
		entGroup1.add(new JLabel("Longitude"), c);

		c.gridx = 4;
		c.gridy = 1;
		c.gridwidth = 1;
		gpLonEnt1 = new UpperCaseTextField(10);
		gpLonEnt1.setEditable(false);
		entGroup1.add(gpLonEnt1, c);
	
		c.gridx = 5;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(1, 1, 1, 1);
		c.anchor = GridBagConstraints.NORTHWEST;
		entGroup1.add(new JLabel("\260"), c);
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(4, 5, 5, 5);
		
		//entity 2 panel child of geo-entity pannel
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 6;
		c.anchor = GridBagConstraints.WEST;
		entGroup2.add(entPanel2, c);
		c.anchor = GridBagConstraints.CENTER;
	
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;		
		geTypeBox2 = new JComboBox(geTypeBoxModel2);
		c.fill = GridBagConstraints.HORIZONTAL;
		entPanel2.add(geTypeBox2, c);		
		c.fill = GridBagConstraints.NONE;
		

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		entGroup2.add(new JLabel("Latitude"), c);
		
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		gpLatEnt2 = new UpperCaseTextField(10);
		gpLatEnt2.setEditable(false);  
		entGroup2.add(gpLatEnt2, c);		

		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(1, 1, 1, 1);
		c.anchor = GridBagConstraints.NORTHWEST;
		entGroup2.add(new JLabel("\260"), c);
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(4, 5, 5, 5);

		c.gridx = 3;
		c.gridy = 1;
		c.gridwidth = 1;
		entGroup2.add(new JLabel("Longitude"), c);

		c.gridx = 4;
		c.gridy = 1;
		c.gridwidth = 1;
		gpLonEnt2 = new UpperCaseTextField(10);
		gpLonEnt2.setEditable(false);
		entGroup2.add(gpLonEnt2, c);		

		c.gridx = 5;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(1, 1, 1, 1);
		c.anchor = GridBagConstraints.NORTHWEST;
		entGroup2.add(new JLabel("\260"), c);
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(4, 5, 5, 5);
		
		gentPannel.setMinimumSize(gentPannel.getPreferredSize());
		
	}
	
	private class GeEnablenessGuard implements Enable {
		GeoFunctionsDialog gfd;
		GeEnablenessGuard(GeoFunctionsDialog gfd){
			this.gfd = gfd;
		}
		public void setEnabledness(){
			gfd.refreshEntityDist();
		}
		
	}
		
	private void addListeners() {
		
	
		geTypeBox1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeEntity1Type();
			}
		});
		
		unitBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshEntityDist();
			}
		});

		geTypeBox2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeEntity2Type();	
			}	
		});
		
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clear();
			}
		});

		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				dispose();
			}
		});


		JavaHelpUtils.enableHelpKey(this, "train.gui.dialog.collections");
	}

	public void clear() {
		unitBox.setSelectedIndex(0);
		geTypeBox1.setSelectedIndex(0);
		geTypeBox2.setSelectedIndex(0);
		for(int n=0; n<ent1CompNumb; n+=2){
			((JTextField)ent1Comp[n+1]).setText("");
		}
		for(int n=0; n<ent2CompNumb; n+=2){
			((JTextField)ent2Comp[n+1]).setText("");
		}
	}

	private void changeEntity1Type() {
		String type1 = (String) geTypeBox1.getSelectedItem();
		GeoMap gm  = GeoHelper.getMap(type1);
		Vector fields = gm.getFields();
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(4, 5, 5, 5);
		c.fill = GridBagConstraints.BOTH;
		
		for(int n = 0; n<ent1CompNumb; n++){
			if(n%2 ==1)
				((JTextField)ent1Comp[n]).getDocument().removeDocumentListener(dle);
			entPanel1.remove(ent1Comp[n]);
		}
		ent1CompNumb = 0;	
			
		Component comp;
		JTextField comp1;
		if(gm.getFields() == null){
			c.gridx = 1;
			c.gridy = 0;
			c.gridwidth = 1;
			comp = new JLabel("Value"); 
			entPanel1.add(comp, c);
			ent1Comp[ent1CompNumb++] = comp; 
		
			c.gridx = 2;
			c.gridy = 0;
			c.gridwidth = 1;
			comp1 = new UpperCaseTextField(15);
			entPanel1.add(comp1, c);	
			ent1Comp[ent1CompNumb++] = comp1;
			comp1.getDocument().addDocumentListener(dle);
		} 
		else {
			for(int n = 0; n<fields.size(); n++){
				GeoMap.KeyField keyFiled= (GeoMap.KeyField)fields.elementAt(n);
				c.gridx = n+n+1;
				c.gridy = 0;
				c.gridwidth = 1;
				comp = new JLabel(keyFiled.name); 
				entPanel1.add(comp, c);
				ent1Comp[ent1CompNumb++] = comp; 

				c.gridx = n+n+2;
				c.gridy = 0;
				c.gridwidth = 1;
				if(keyFiled.length>=0){
					comp1 = new JTextField(keyFiled.length);
					comp1.setDocument(new UpperCaseTextField.LimitDocument(keyFiled.length));
				}
				else
					comp1 = new UpperCaseTextField(15);
				entPanel1.add(comp1, c);	
				ent1Comp[ent1CompNumb++] = comp1;
				comp1.getDocument().addDocumentListener(dle);
			}
		}
		entGroup1.validate();
		refreshEntityDist();
	}

	private void changeEntity2Type() {
		String type = (String) geTypeBox2.getSelectedItem();
		GeoMap gm  = GeoHelper.getMap(type);
		Vector fields = gm.getFields();
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(4, 5, 5, 5);
		c.fill = GridBagConstraints.BOTH;
		
		for(int n = 0; n<ent2CompNumb; n++){
			if(n%2 ==1)
				((JTextField)ent2Comp[n]).getDocument().removeDocumentListener(dle);
			entPanel2.remove(ent2Comp[n]);
		}
		
		ent2CompNumb = 0;	
			
		Component comp;
		JTextField comp1;
		if(gm.getFields() == null){
			c.gridx = 1;
			c.gridy = 0;
			c.gridwidth = 1;
			comp = new JLabel("Value"); 
			entPanel2.add(comp, c);
			ent2Comp[ent2CompNumb++] = comp; 
		
			c.gridx = 2;
			c.gridy = 0;
			c.gridwidth = 1;
			comp1 = new UpperCaseTextField(15);
			entPanel2.add(comp1, c);	
			ent2Comp[ent2CompNumb++] = comp1;
			comp1.getDocument().addDocumentListener(dle);
		} 
		else {
			for(int n = 0; n<fields.size(); n++){
				GeoMap.KeyField keyFiled= (GeoMap.KeyField)fields.elementAt(n);
				c.gridx = n+n+1;
				c.gridy = 0;
				c.gridwidth = 1;
				comp = new JLabel(keyFiled.name); 
				entPanel2.add(comp, c);
				ent2Comp[ent2CompNumb++] = comp; 

				c.gridx = n+n+2;
				c.gridy = 0;
				c.gridwidth = 1;
				if(keyFiled.length>=0){
					comp1 = new JTextField(keyFiled.length);
					comp1.setDocument(new UpperCaseTextField.LimitDocument(keyFiled.length));
				}
				else
					comp1 = new UpperCaseTextField(15);
				entPanel2.add(comp1, c);	
				ent2Comp[ent2CompNumb++] = comp1;
				comp1.getDocument().addDocumentListener(dle);
			}
		}
		entGroup2.validate();
		refreshEntityDist();
	}


	private void refreshEntityDist() {
		
		String unit = (String) unitBox.getSelectedItem();
		String type1 = (String) geTypeBox1.getSelectedItem();
		String temp;
		String val1 = "";
		for(int n=0; n<ent1CompNumb; n+=2){
			temp = ((JTextField)ent1Comp[n+1]).getText().trim();
			val1 = val1.concat(temp);		
		}
		String type2 = (String) geTypeBox2.getSelectedItem();
		String val2 = "";
		for(int n=0; n<ent2CompNumb; n+=2){
			temp = ((JTextField)ent2Comp[n+1]).getText().trim();
			val2 = val2.concat(temp);		
		}
		String latStr1 = "";
		String latStr2 = "";
		String lonStr1 = "";
		String lonStr2 = "";
		String distStr = "";
		GeoPoint gp1 = null;
		GeoPoint gp2 = null;
		
		if(val1.length()!=0 ){
			gp1 = GeoHelper.geoPoint(type1,val1);
			if(gp1 != null){
				latStr1 = Float.toString(((float)gp1.lat)/10000);
				lonStr1 = Float.toString(((float)gp1.lon)/10000);
			}
		}
		if(val2.length()!=0){
			gp2 = GeoHelper.geoPoint(type2,val2);	
			if(gp2 != null){
				latStr2 = Float.toString(((float)gp2.lat)/10000);
				lonStr2 = Float.toString(((float)gp2.lon)/10000);
			}
		}
		if(gp1 != null && gp2 != null){	
			double d = GeoHelper.dist(gp1,gp2);
			if(d>=0){
				if(unit.intern() == "km")
					d = GeoHelper.mileToKm(d);
				distStr = df.format(d);
			}
		} 
		gpLatEnt1.setText(latStr1);
		gpLatEnt2.setText(latStr2);
		gpLonEnt1.setText(lonStr1);
		gpLonEnt2.setText(lonStr2);
		dist.setText(distStr);
		 
	}


	private void repopulateAvailableCollections() {
		
		geTypeBoxModel1.removeAllElements();
		geTypeBoxModel2.removeAllElements();
		Iterator gtRels = new TreeSet(GeoHelper.getGeoTypes()).iterator();
		Object typeName;
		while (gtRels.hasNext()) {
			typeName = gtRels.next();
			geTypeBoxModel1.addElement(typeName);
			geTypeBoxModel2.addElement(typeName);
		}
		
	}
	

	

}
