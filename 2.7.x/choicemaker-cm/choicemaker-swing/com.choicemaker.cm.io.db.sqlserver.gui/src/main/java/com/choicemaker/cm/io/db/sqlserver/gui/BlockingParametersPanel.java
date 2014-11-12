/*
 * Created on Jan 23, 2004
 *
 */
package com.choicemaker.cm.io.db.sqlserver.gui;

//import static com.choicemaker.cm.core.ImmutableProbabilityModel.*;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.choicemaker.cm.core.IProbabilityModel;

/**
 * @author ajwinkel
 *
 */
public class BlockingParametersPanel extends JPanel {

	private static final long serialVersionUID = 271L;
	
	public static final String BLOCKING_PARAMETERS_PROPERTY = "blockingParameters";

	private JComboBox dbConfiguration, blockingConfiguration;
	private JTextField lpbsField, stbsglField, lsbsField;

	private boolean firing = true;

	public BlockingParametersPanel() {
		this(new String[0], new String[0], -1, -1, -1);
	}
	
	public BlockingParametersPanel(String[] dbConfigurations,
								   String[] blockingConfigurations,
								   int lpbs, 
								   int stbsgl,
								   int lsbs) {
		createContent();
		createListeners();

		firing = false;
		try {
			setChoosableDbConfigurations(dbConfigurations);		
			setChoosableBlockingConfigurations(blockingConfigurations);
			setLPBS(lpbs);
			setSTBSGL(stbsgl);
			setLSBS(lsbs);
		} finally {
			firing = true;
		}
	}

	public void setChoosableBlockingConfigurations(String[] blockingConfigurations) {
		this.blockingConfiguration.setModel(new DefaultComboBoxModel(blockingConfigurations));
	}
	
	public void setSelectedBlockingConfiguration(String blockingConfiguration) {
		this.blockingConfiguration.setSelectedItem(blockingConfiguration);
	}
	
	public String getSelectedBlockingConfiguration() {
		return (String) this.blockingConfiguration.getSelectedItem();
	}

	public void setChoosableDbConfigurations(String[] dbConfigurations) {
		this.dbConfiguration.setModel(new DefaultComboBoxModel(dbConfigurations));
	}
	
	public void setSelectedDbConfiguration(String dbConfiguration) {
		this.dbConfiguration.setSelectedItem(dbConfiguration);
	}
	
	public String getSelectedDbConfiguration() {
		return (String) this.dbConfiguration.getSelectedItem();
	}
	
	public void setLPBS(int lpbs) {
		this.lpbsField.setText(String.valueOf(lpbs));
	}
	
	public int getLPBS() {
		return getInt(lpbsField);
	}
	
	public void setSTBSGL(int stbsgl) {
		this.stbsglField.setText(String.valueOf(stbsgl));
	}

	public int getSTBSGL() {
		return getInt(stbsglField);
	}
	
	public void setLSBS(int lsbs) {
		this.lsbsField.setText(String.valueOf(lsbs));
	}

	public int getLSBS() {
		return getInt(lsbsField);
	}

	public void setFromProperties(IProbabilityModel prodModel) {
		firing = false;
		try {
			// BUG The getBlockingConfigurationName() and
			// getDatabaseConfigurationName() call are likely to throw
			// IllegalStateExceptions, because the Analyzer configuration no
			// longer defines production model configurations
			setSelectedBlockingConfiguration(prodModel
					.getBlockingConfigurationName());
			setSelectedDbConfiguration(prodModel.getDatabaseConfigurationName());
			// END BUG
			setInt(lpbsField, (String)prodModel.properties().get("limitPerBlockingSet"));
			setInt(stbsglField, (String)prodModel.properties().get("singleTableBlockingSetGraceLimit"));
			setInt(lsbsField, (String)prodModel.properties().get("limitSingleBlockingSet"));
		} finally {
			firing = true;
		}
		
		fireEvent();
	}

	public void setEnabled(boolean enabled) {
		blockingConfiguration.setEnabled(enabled);
		dbConfiguration.setEnabled(enabled);
		lpbsField.setEnabled(enabled);
		stbsglField.setEnabled(enabled);
		lsbsField.setEnabled(enabled);
		
		super.setEnabled(false);
	}

	private int getInt(JTextField tf) {
		try {
			return Integer.parseInt(tf.getText().trim());
		} catch (NumberFormatException ex) {
			return -1;
		}		
	}
	
	private void setInt(JTextField tf, String s) {
		try {
			int val = Integer.parseInt(s);
			tf.setText(String.valueOf(val));
		} catch (NumberFormatException ex) {
			// do nothing
		}
	} 
	
	private void createContent() {
		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] {0, 1};
		setLayout(layout);
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 4, 2, 4);
		c.fill = GridBagConstraints.HORIZONTAL;

		//
		
		c.gridy++;
		
		c.gridx = 0;
		add(new JLabel("DB Configuration: "), c);
		
		c.gridx = 1;
		dbConfiguration = new JComboBox();
		add(dbConfiguration, c);

		//
		
		c.gridy++;
		
		c.gridx = 0;
		add(new JLabel("Blocking Configuration: "), c);
		
		c.gridx = 1;
		blockingConfiguration = new JComboBox();
		add(blockingConfiguration, c);
		
		//
		
		c.gridy++;
		add(Box.createVerticalStrut(10), c);
		
		//
		
		c.gridy++;
		
		c.gridx = 0;
		add(new JLabel("Max Expected Blocking Set Size"), c);
		
		c.gridx = 1;
		lpbsField = new JTextField(10);
		add(lpbsField, c);
		
		//
		
		c.gridy++;
		
		c.gridx = 0;
		add(new JLabel("Single Table Grace Limit"), c);
		
		c.gridx = 1;
		stbsglField = new JTextField(10);
		add(stbsglField, c);
		
		//
		
		c.gridy++;
		
		c.gridx = 0;
		add(new JLabel("Max Records Blocked"), c);
		
		c.gridx = 1;
		lsbsField = new JTextField(10);
		add(lsbsField, c);				
	}
	
	private void createListeners() {
		ItemListener il = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				fireEvent();
			}
		};
		
		dbConfiguration.addItemListener(il);
		blockingConfiguration.addItemListener(il);
		
		DocumentListener dl = new DocumentListener() {
			public void insertUpdate(DocumentEvent e) { fireEvent(); }
			public void removeUpdate(DocumentEvent e) { fireEvent(); }
			public void changedUpdate(DocumentEvent e) { fireEvent(); }
		};
		
		lpbsField.getDocument().addDocumentListener(dl);
		stbsglField.getDocument().addDocumentListener(dl);
		lsbsField.getDocument().addDocumentListener(dl);
	}

	private void fireEvent() {
		if (firing) {
			this.firePropertyChange(BLOCKING_PARAMETERS_PROPERTY, null, null);
		}
	}
}
