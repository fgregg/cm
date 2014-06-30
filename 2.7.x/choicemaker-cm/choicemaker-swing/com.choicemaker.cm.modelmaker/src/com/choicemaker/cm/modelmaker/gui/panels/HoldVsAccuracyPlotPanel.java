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
package com.choicemaker.cm.modelmaker.gui.panels;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;

//import org.apache.log4j.Logger;



import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.core.util.MessageUtil;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.tables.AccuracyTable;
import com.choicemaker.cm.modelmaker.stats.StatPoint;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Hold percentage versus accuracy using a symmetric match/differ threshold window.
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:09 $
 */
public class HoldVsAccuracyPlotPanel extends JPanel {

	private static final long serialVersionUID = -2486951345210387891L;

	//	private static Logger logger = Logger.getLogger(HoldVsAccuracyPlotPanel.class);
	private TestingControlPanel parent;
	private XYSeries data;
	private JFreeChart chart;
	private boolean dirty;
	private float[] errorRates;
	private float[] accuracies =
		{ 0.95f, 0.96f, 0.97f, 0.98f, 0.985f, 0.99f, 0.992f, 0.994f, 0.995f, 0.996f, 0.997f, 0.998f, 0.999f, 1f };
	private float[] accErrs;
	private float[] hrs = { 0f, 0.01f, 0.02f, 0.03f, 0.04f, 0.05f, 0.06f, 0.08f, 0.1f, 0.15f, 0.20f };
	private float[][] accuracyData;
	private float[][] hrData;
	private JPanel accuracyPanel;
	private AccuracyTable accuracyTable;
	private JPanel hrPanel;
	private AccuracyTable hrTable;

	public HoldVsAccuracyPlotPanel(TestingControlPanel g) {
		super();
		parent = g;
		setBorder(BorderFactory.createEmptyBorder(15, 5, 5, 5));
		buildPanel();
		layoutPanel();
		setAccErrs();
	}

	private void setAccErrs() {
		accErrs = new float[accuracies.length];
		for (int i = 0; i < accuracies.length; ++i) {
			accErrs[i] = 1f - accuracies[i];
		}
	}

	private void buildPanel() {
		XYSeriesCollection dataset = new XYSeriesCollection();
		String title = MessageUtil.m.formatMessage("train.gui.modelmaker.panel.holdvsacc.title");
		data = new XYSeries(title);
		dataset.addSeries(data);
		final PlotOrientation orientation = PlotOrientation.VERTICAL;
		chart =
			ChartFactory.createXYLineChart(
				title,
				MessageUtil.m.formatMessage("train.gui.modelmaker.panel.holdvsacc.cm.accuracy"),
				MessageUtil.m.formatMessage("train.gui.modelmaker.panel.holdvsacc.holdpercentage"),
				dataset,
				orientation, true, true, true);
		MouseListener tableMouseListener = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				Point origin = e.getPoint();
				JTable src = (JTable) e.getSource();
				int row = src.rowAtPoint(origin);
				int col = src.columnAtPoint(origin);
				ModelMaker mm = parent.getModelMaker();
				if (src == accuracyTable) {
					if (col < 2) {
						if (!Float.isNaN(accuracyData[row][2]) && !Float.isNaN(accuracyData[row][3]))
							mm.setThresholds(new Thresholds(accuracyData[row][2], accuracyData[row][3]));
					} else if (col == 2) {
						if (!Float.isNaN(accuracyData[row][2]))
							mm.setDifferThreshold(accuracyData[row][2]);
					} else {
						if (!Float.isNaN(accuracyData[row][3]))
							mm.setMatchThreshold(accuracyData[row][3]);
					}
				} else {
					if (col < 2) {
						if (!Float.isNaN(hrData[row][2]) && !Float.isNaN(hrData[row][3]))
							mm.setThresholds(new Thresholds(hrData[row][2], hrData[row][3]));
					} else if (col == 2) {
						if (!Float.isNaN(hrData[row][2]))
							mm.setDifferThreshold(hrData[row][2]);
					} else {
						if (!Float.isNaN(hrData[row][3]))
							mm.setMatchThreshold(hrData[row][3]);
					}
				}
			}
		};
		chart.setBackgroundPaint(getBackground());
		accuracyTable = new AccuracyTable(true, accuracies);
		accuracyTable.addMouseListener(tableMouseListener);
		accuracyPanel =
			getPanel(accuracyTable, MessageUtil.m.formatMessage("train.gui.modelmaker.panel.holdvsacc.table.hrvsacc"));

		hrTable = new AccuracyTable(false, hrs);
		hrTable.addMouseListener(tableMouseListener);
		hrPanel = getPanel(hrTable, MessageUtil.m.formatMessage("train.gui.modelmaker.panel.holdvsacc.table.accvshr"));

		accuracyTable.setEnabled(false);
		hrTable.setEnabled(false);
	}

	JPanel getPanel(JTable table, String title) {
		JScrollPane pane = new JScrollPane();
		pane.getViewport().add(table);
		pane.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(5, 10, 5, 5),
				BorderFactory.createLoweredBevelBorder()));
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(title));
		panel.add(pane, BorderLayout.CENTER);
		return panel;
	}

	public void setVisible(boolean b) {
		super.setVisible(b);
		if (b && dirty) {
			display();
		}
	}

	public void plot() {
		setDirty();
	}

	private void setDirty() {
		if (isVisible()) {
			display();
		} else {
			dirty = true;
		}
	}

	// Broken: moves thresholds instead of accuracy symmetrically
	private void display() {
		com.choicemaker.cm.modelmaker.stats.Statistics stats = parent.getModelMaker().getStatistics();
		StatPoint ptb = new StatPoint();
		ptb.humanReview = 0f;
		stats.computeStatPoint(ptb);
		float from =
			Math.max(
				0.001f,
				Math.min(
					0.1f,
					Float.isNaN(ptb.falseNegatives)
						|| Float.isNaN(ptb.falsePositives)
							? Float.MAX_VALUE
							: Math.max(ptb.falseNegatives, ptb.falsePositives)));
		int numPoints = 101;
		float step = from / (numPoints - 1);
		errorRates = new float[numPoints];
		for (int i = 0; i < numPoints; ++i) {
			errorRates[i] = from - i * step;
		}
		dirty = false;
		if (parent.isEvaluated()) {
			reset();
			float[][] res = stats.getHoldPercentageVsAccuracy(errorRates);
			final int len = res.length;
			float lastX = Float.NaN;
			for (int i = 0; i < len; ++i) {
				final float hr = 100 * res[i][1];
				final float x = 100f - 100 * res[i][0];
				if (!Float.isNaN(x) && x != lastX && !Float.isNaN(hr)) {
					data.add(x, hr);
					lastX = x;
				}
			}
		}
		accuracyData = stats.getHoldPercentageVsAccuracy(accErrs);
		accuracyTable.refresh(accuracyData);
		StatPoint pt = new StatPoint();
		hrData = new float[hrs.length][4];
		for (int i = 0; i < hrs.length; ++i) {
			pt.reset();
			pt.humanReview = hrs[i];
			stats.computeStatPoint(pt);
			hrData[i][0] = hrs[i];
			hrData[i][1] = 1f - (pt.falseNegatives + pt.falsePositives) / 2;
			hrData[i][2] = pt.differThreshold;
			hrData[i][3] = pt.matchThreshold;
		}
		hrTable.refresh(hrData);
		accuracyTable.setEnabled(true);
		hrTable.setEnabled(true);
	}

	public void reset() {
		data.clear();
		accuracyTable.reset();
		hrTable.reset();
		accuracyTable.setEnabled(false);
		hrTable.setEnabled(false);
		hrData = null;
		accuracyData = null;
	}

	private void layoutPanel() {
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		layout.columnWeights = new double[] { 1f, 0f };
		layout.columnWidths = new int[] { 200, 300 };
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 5, 10);

		//Row 0..........................................................
		//histo
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		ChartPanel p = new ChartPanel(chart, false, false, false, true, true);
//		p.setHorizontalZoom(true);
//		p.setVerticalZoom(true);
		add(p, c);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setDividerSize(2);
		splitPane.setContinuousLayout(true);
		splitPane.setDividerLocation(0.5d);
		splitPane.setResizeWeight(0.5f);
		splitPane.setOneTouchExpandable(true);
		splitPane.setTopComponent(accuracyPanel);
		splitPane.setBottomComponent(hrPanel);

		c.gridx = 1;
		c.gridheight = 1;
		add(splitPane, c);
	}
}
