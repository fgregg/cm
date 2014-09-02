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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import java.util.logging.Logger;

import com.choicemaker.cm.core.util.UpperCaseTextField;
import com.choicemaker.cm.gui.utils.JavaHelpUtils;
import com.choicemaker.cm.matching.gen.Maps;
import com.choicemaker.cm.matching.gen.Relation;
import com.choicemaker.cm.matching.gen.Relations;
import com.choicemaker.cm.matching.gen.Sets;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.utils.Enable;
import com.choicemaker.cm.modelmaker.gui.utils.EnablednessGuard;

/**
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:08 $
 */
public class CollectionsDialog extends JDialog implements Enable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(CollectionsDialog.class.getName());

	private ModelMaker parent;

	private JTextField sText;

	private JComboBox setBox;
	private DefaultComboBoxModel setBoxModel;

	private JComboBox mapBox;
	private DefaultComboBoxModel mapBoxModel;

	private JComboBox relBox;
	private DefaultComboBoxModel relBoxModel;

	private JCheckBox relReflexiveBox;

	private ScrollableTable setTable;
	private SetTableModel setTableModel;
	private JScrollPane setPane;

	private ScrollableTable mapTable;
	private MapTableModel mapTableModel;
	private JScrollPane mapPane;

	private JTable relFwdTable;
	private SetTableModel relFwdTableModel;

	private JTable relBkwdTable;
	private SetTableModel relBkwdTableModel;

	public CollectionsDialog(ModelMaker parent) {
		super(parent, "Collections Lookup Utility", false);

		this.parent = parent;

		buildPanel();
		addListeners();

		repopulateAvailableCollections();

		setEnabledness();
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
		layout.columnWeights = new double[] {0, 1, 1};
		layout.rowWeights = new double[] {0, 1, 1};
		content.setLayout(layout);

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		content.add(new JLabel("  Key: "), c);

		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		sText = new UpperCaseTextField(20);
		content.add(sText, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		JPanel setsPanel = new JPanel();
		setsPanel.setBorder(new TitledBorder("Sets"));
		content.add(setsPanel, c);

		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 1;
		JPanel mapsPanel = new JPanel();
		mapsPanel.setBorder(new TitledBorder("Maps"));
		content.add(mapsPanel, c);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 3;
		JPanel relsPanel = new JPanel();
		relsPanel.setBorder(new TitledBorder("Relations"));
		content.add(relsPanel, c);

		// Sets Panel
		layout = new GridBagLayout();
		layout.columnWeights = new double[] {0, 1};
		layout.rowWeights = new double[] {0, 1};
		setsPanel.setLayout(layout);

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		setsPanel.add(new JLabel("Set Name: "), c);

		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		setBoxModel = new DefaultComboBoxModel();
		setBox = new JComboBox(setBoxModel);
		setsPanel.add(setBox, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		setTableModel = new SetTableModel("Members");
		setTable = new ScrollableTable(setTableModel);
		setPane = new JScrollPane(setTable);
		setTable.setScrollPane(setPane);
		setPane.setPreferredSize(new Dimension(200, 150));
		setPane.setMinimumSize(new Dimension(200, 150));
		setsPanel.add(setPane, c);

		setsPanel.setMinimumSize(setsPanel.getPreferredSize());

		// Maps Panel
		layout = new GridBagLayout();
		layout.columnWeights = new double[] {0, 1};
		layout.rowWeights = new double[] {0, 1};
		mapsPanel.setLayout(layout);

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		mapsPanel.add(new JLabel("Map Name: "), c);

		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		mapBoxModel = new DefaultComboBoxModel();
		mapBox = new JComboBox(mapBoxModel);
		mapsPanel.add(mapBox, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		mapTableModel = new MapTableModel();
		mapTable = new ScrollableTable(mapTableModel);
		mapPane = new JScrollPane(mapTable);
		mapTable.setScrollPane(mapPane);
		mapPane.setPreferredSize(new Dimension(300, 150));
		mapPane.setMinimumSize(new Dimension(300, 150));
		mapsPanel.add(mapPane, c);

		mapsPanel.setMinimumSize(mapsPanel.getPreferredSize());

		// Relations Panel
		layout = new GridBagLayout();
		layout.columnWeights = new double[] {0, 1, 1, 0};
		layout.rowWeights = new double[] {0, 1};
		relsPanel.setLayout(layout);

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		relsPanel.add(new JLabel("Relation Name: "), c);

		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 2;
		relBoxModel = new DefaultComboBoxModel();
		relBox = new JComboBox(relBoxModel);
		relsPanel.add(relBox, c);

		c.gridx = 3;
		c.gridy = 0;
		c.gridwidth = 1;
		relReflexiveBox = new JCheckBox("Reflexive?");
		relsPanel.add(relReflexiveBox, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		relFwdTableModel = new SetTableModel("Forward Set");
		relFwdTable = new JTable(relFwdTableModel);
		JScrollPane fwdSP = new JScrollPane(relFwdTable);
		fwdSP.setPreferredSize(new Dimension(200, 150));
		fwdSP.setMinimumSize(new Dimension(200, 150));
		relsPanel.add(fwdSP, c);

		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 2;
		relBkwdTableModel = new SetTableModel("Backward Set");
		relBkwdTable = new JTable(relBkwdTableModel);
		JScrollPane bkwdSP = new JScrollPane(relBkwdTable);
		bkwdSP.setPreferredSize(new Dimension(200, 150));
		bkwdSP.setMinimumSize(new Dimension(200, 150));
		relsPanel.add(bkwdSP, c);

		relsPanel.setMinimumSize(relsPanel.getPreferredSize());
	}

	private void addListeners() {

		EnablednessGuard dl = new EnablednessGuard(this);
		sText.getDocument().addDocumentListener(dl);

		JavaHelpUtils.enableHelpKey(this, "train.gui.dialog.collections");

		setBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshSetTable();
			}
		});

		mapBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshMapTable();
			}
		});

		relBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshRelsTables();
			}
		});

		relReflexiveBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				refreshRelsTables();
			}
		});

	}

	/**
	 * @see com.choicemaker.cm.train.gui.utils.Enable#setEnabledness()
	 */
	public void setEnabledness() {
		if (parent.getProbabilityModel() != null) {
			updateSetScrollPrefix();
			updateMapScrollPrefix();
			refreshRelsTables();
		}
	}

	private void refreshSetTable() {
		String setName = (String) setBox.getSelectedItem();
		Collection set = Sets.getCollection(setName);
		setTableModel.setCollection(set);

		updateSetScrollPrefix();
	}

	private void updateSetScrollPrefix() {
		setTable.setScrollPrefix(sText.getText().trim());
	}

	private void refreshMapTable() {
		String mapName = (String) mapBox.getSelectedItem();
		Map map = Maps.getMap(mapName);
		mapTableModel.setMap(map);

		updateMapScrollPrefix();
	}

	private void updateMapScrollPrefix() {
		mapTable.setScrollPrefix(sText.getText().trim());
	}

	private void refreshRelsTables() {
		String s = sText.getText().trim();

		String relName = (String) relBox.getSelectedItem();
		Relation rel = Relations.getRelation(relName);

		if (rel != null && rel.isReflexive()) {
			relReflexiveBox.setSelected(true);
			relReflexiveBox.setEnabled(false);
		} else {
			relReflexiveBox.setEnabled(true);
		}

		boolean reflexive = relReflexiveBox.isSelected();

		if (s.length() > 0 && rel != null) {
			// First the forward one...
			Collection fwd = rel.get(s, reflexive);
			relFwdTableModel.setCollection(fwd);

			// Then the backward one...
			Relation inv = rel.getInverse();
			Collection bkwd = inv.get(s, reflexive);
			relBkwdTableModel.setCollection(bkwd);
		} else {
			relFwdTableModel.setCollection(null);
			relBkwdTableModel.setCollection(null);
		}

	}

	private void repopulateAvailableCollections() {

		setBoxModel.removeAllElements();
		Iterator itSets = new TreeSet(Sets.getCollectionNames()).iterator();
		while (itSets.hasNext()) {
			setBoxModel.addElement(itSets.next());
		}

		mapBoxModel.removeAllElements();
		Iterator itMaps = new TreeSet(Maps.getMapNames()).iterator();
		while (itMaps.hasNext()) {
			mapBoxModel.addElement(itMaps.next());
		}

		relBoxModel.removeAllElements();
		Iterator itRels = new TreeSet(Relations.getRelationNames()).iterator();
		while (itRels.hasNext()) {
			relBoxModel.addElement(itRels.next());
		}

	}

	private class ScrollableTable extends JTable {

		private static final long serialVersionUID = 1L;
		SortedTableModel sortedModel;
		JScrollPane scroller;
		String prefix;

		TableCellRenderer highlightingRenderer;

		public ScrollableTable(SortedTableModel model) {
			super(model);
			sortedModel = model;

			highlightingRenderer = new HighlightingTableCellRenderer();
		}

		public void setScrollPane(JScrollPane sp) {
			this.scroller = sp;
		}

		public void setScrollPrefix(String s) {
			prefix = s;
			if (s == null || s.length() == 0) {
				prefix = "";
				scroller.repaint();
				return;
			}

			int scrollIndex = getScrollIndex(s);
			if (scrollIndex >= getRowCount()) {
				scrollIndex = getRowCount() - 1;
			}

			Rectangle rect = getCellRect(scrollIndex, 0, true);
			int scrollY = (int) rect.getY();

			Dimension viewSize = scroller.getViewport().getViewSize();
			int viewHeight = (int) viewSize.getHeight();

			Dimension d = scroller.getViewport().getExtentSize();
			int visibleHeight = (int) d.getHeight();

			if (scrollY + visibleHeight > viewHeight) {
				scrollY = Math.max(0, viewHeight - visibleHeight);
			}

			// move the view
			scroller.getViewport().setViewPosition( new Point(0, scrollY) );

			// refresh the cells.
			scroller.repaint();
		}

		public TableCellRenderer getCellRenderer(int row, int column) {
			if (column == 0) {
				return highlightingRenderer;
			} else {
				return super.getCellRenderer(row, column);
			}
		}

		protected int getScrollIndex(String s) {
			Object[][] data = sortedModel.getSortedData();
			return binarySearch(s, data, 0, data.length);
		}

		protected int binarySearch(String s, Object[][] data, int from, int to) {

			if (to - from == 0) {
				return to;
			} else if (to - from == 1) {
				return compare(s, data[from][0]) > 0 ? to : from;
			}

			int middle = (from + to) / 2;

			int comp = compare(s, data[middle][0]);
			if (comp == 0) {
				return middle;
			} else if (comp < 0) {
				return binarySearch(s, data, from, middle);
			} else {
				return binarySearch(s, data, middle + 1, to);
			}
		}

		protected int compare(String s, Object t) {
			if (t instanceof String) {
				return s.compareTo((String)t);
			} else if (t != null) {
				return s.compareTo(t.toString());
			} else {
				return s.compareTo("");
			}
		}

		private class HighlightingTableCellRenderer extends JTextPane implements TableCellRenderer {
			private static final long serialVersionUID = 1L;
			DefaultStyledDocument doc;
			Style regular, highlighted;

			public HighlightingTableCellRenderer() {
				setBorder(null);

				StyleContext sc = new StyleContext();
				doc = new DefaultStyledDocument(sc);
				regular = sc.addStyle(null, null);
				highlighted = sc.addStyle(null, null);
				StyleConstants.setForeground(highlighted, Color.RED);
				StyleConstants.setBold(highlighted, true);

				setDocument(doc);
			}

			public Component getTableCellRendererComponent(JTable table, Object value,
								boolean isSelected, boolean hasFocus, int row, int column) {

				if (isSelected) {
					super.setForeground(table.getSelectionForeground());
					super.setBackground(table.getSelectionBackground());
				} else {
					super.setForeground(table.getForeground());
					super.setBackground(table.getBackground());
				}

				setFont(table.getFont());

				// the equivalent of setValue(value).
				try {
					Document d = getDocument();
					d.remove(0, d.getLength());

					String s = "";
					if (value != null) {
						s = value.toString();
					}

					if (prefix == null || prefix.length() == 0 || !s.startsWith(prefix)) {
						d.insertString(0, s, regular);
					} else {
						d.insertString(0, prefix, highlighted);
						d.insertString(prefix.length(), s.substring(prefix.length()), regular);
					}
				} catch (BadLocationException ex) {
					logger.severe(ex.toString());
				}

				return this;
			}
		}

	}

	private class SetTableModel extends SortedTableModel {
		private static final long serialVersionUID = 1L;
		private Collection collection;
		private Object[][] data;
		private final Object[] colNames = new Object[1];

		public SetTableModel(String colName) {
			colNames[0]	= colName;
			setColumnIdentifiers(colNames);
		}

		public void setCollection(Collection c) {
			if (c != null && c.equals(collection)) {
				return;
			}

			collection = c;

			if (collection == null) {
				data = new Object[0][];
			} else {
				data = new Object[collection.size()][];
				Iterator it = new TreeSet(collection).iterator();
				int index = 0;
				while (it.hasNext()) {
					data[index] = new Object[1];
					data[index][0] = it.next();
					index++;
				}
			}

			setDataVector(data, colNames);
		}

		public Object[][] getSortedData() {
			return data;
		}

		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}

	private class MapTableModel extends SortedTableModel {
		private static final long serialVersionUID = 1L;
		private Map map;
		private Object[][] data;
		private Object[] colNames = {"Key", "Value"};

		public MapTableModel() {
			this.setColumnIdentifiers(colNames);
		}

		public void setMap(Map m) {
			if (m != null && m.equals(map)) {
				return;
			}

			map = m;

			if (map == null) {
				data = new Object[0][];
			} else {
				data = new Object[map.size()][];
				Iterator it = new TreeSet(map.keySet()).iterator();
				int index = 0;
				while (it.hasNext()) {
					data[index] = new Object[2];
					data[index][0] = it.next();
					data[index][1] = map.get(data[index][0]);
					index++;
				}
			}

			setDataVector(data, colNames);
		}

		public Object[][] getSortedData() {
			return data;
		}

		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}

	public abstract class SortedTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;

		public abstract Object[][] getSortedData();
	}
}
