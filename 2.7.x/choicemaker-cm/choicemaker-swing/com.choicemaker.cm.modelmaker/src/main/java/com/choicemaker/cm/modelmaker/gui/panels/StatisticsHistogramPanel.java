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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
//import org.jfree.chart.axis.HorizontalCategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
//import org.jfree.chart.renderer.CategoryItemRenderer;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
//import org.jfree.data.category.CategoryDataset;

//import org.apache.log4j.Logger;
import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.utils.HistoCategoryDataset;
import com.choicemaker.cm.modelmaker.gui.utils.HistoChartPanel;

/**
 * Panel that contains the histogram showing the ChoiceMaker system accuracy.  This
 * histogram listens for mouse clicks on its bars.
 *
 * @author S. Yoakum-Stover
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:09 $
 */
public class StatisticsHistogramPanel extends JPanel {

	private static final long serialVersionUID = -9207566748507473389L;

	private TestingControlPanel parent;
	private HistoChartPanel histoPanel;
	private JFreeChart histogram;
	private HistoCategoryDataset data;
	private JLabel binWidthLabel;
	private JTextField binWidthField;
	private float binWidth = 5.0f;
	private boolean dirty;
	private static final String[] SERIES = { "human differ", "human hold", "human match" };
	private Paint[] whSeriesPaint;
	private Paint[] wohSeriesPaint;

	public StatisticsHistogramPanel(TestingControlPanel g) {
		super();
		parent = g;
		setBorder(BorderFactory.createEmptyBorder(15, 5, 5, 5));
		buildPanel();
		addListeners();
		layoutPanel();
	}

	private int getNumBins() {
		return (int) Math.ceil(100 / binWidth);
	}

	private void buildPanel() {
		final PlotOrientation orientation = PlotOrientation.VERTICAL;
		data = new HistoCategoryDataset(SERIES, getNumBins());
		histogram =
			ChartFactory.createBarChart(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.panel.histogram.cm.accuracy"),
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.panel.histogram.cm.matchprob"),
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.panel.histogram.cm.numpairs"),
				data,
				orientation,
				true,
				true,
				true);
		histogram.setBackgroundPaint(getBackground());
		CategoryPlot plot = (CategoryPlot) histogram.getPlot();
		plot.setForegroundAlpha(0.9f);
//		HorizontalCategoryAxis axis = (HorizontalCategoryAxis) plot.getDomainAxis();
		CategoryAxis axis = plot.getDomainAxis();
		axis.setLowerMargin(0.02);
		axis.setUpperMargin(0.02);
		axis.setCategoryMargin(0.2);
//		axis.setVerticalCategoryLabels(true);
		CategoryItemRenderer renderer = plot.getRenderer();
		whSeriesPaint = new Paint[3];
		for (int i = 0; i < whSeriesPaint.length; ++i) {
//			whSeriesPaint[i] = renderer.getSeriesPaint(0, i);
			whSeriesPaint[i] = renderer.getSeriesPaint(i);
		}
		wohSeriesPaint = new Paint[2];
		wohSeriesPaint[0] = whSeriesPaint[0];
		wohSeriesPaint[1] = whSeriesPaint[2];
		//	plot.setRangeAxis(new VerticalLogarithmicAxis());
		histoPanel = new HistoChartPanel(histogram, false, false, false, true, true, parent.getModelMaker());
		//	histoPanel.setEnabled(false);

		binWidthLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.panel.histogram.binwidth"));
		binWidthField = new JTextField(Float.toString(binWidth), 4);
		binWidthField.setMinimumSize(new Dimension(50, 20));
	}

	private void addListeners() {
		//        histo.addEditListener(this);

		//binWidthField
		binWidthField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				setBinWidth(Float.parseFloat(binWidthField.getText()));
			}
		});

	}

	private void setBinWidth(float bw) {
		if (bw > 0.1 && bw <= 100) {
			binWidth = bw;
			data.setNumCategories(getNumBins());
			display();
		}
	}

	private void layoutPanel() {
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 10, 5, 10);

		//Row 0..........................................................
		//histo
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 5;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		add(histoPanel, c);

		//Row 1..........................................................
		//binWidthLabel
		c.gridy = 1;
		c.gridx = 1;
		c.gridwidth = 1;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.EAST;
		layout.setConstraints(binWidthLabel, c);
		add(binWidthLabel);
		//binWidthField
		c.gridx = 2;
		c.ipadx = 10;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(binWidthField, c);
		add(binWidthField);
	}

	public void reset() {
		data.setData(null);
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

	private void display() {
		dirty = false;
		if (parent.isEvaluated()) {
			ModelMaker mm = parent.getModelMaker();
			int[][] h = mm.getStatistics().getHistogram(getNumBins());
			final int len = h[0].length;
			Integer[][] v = new Integer[h.length][len];
			for (int i = 0; i < h.length; ++i) {
				int[] hi = h[i];
				Integer[] vi = v[i];
				for (int j = 0; j < len; ++j) {
					vi[j] = new Integer(hi[j]);
				}
			}
			boolean ih = mm.isIncludeHolds();
			data.setIncludeHolds(ih);
			CategoryItemRenderer renderer = ((CategoryPlot) histogram.getPlot()).getRenderer();
			for(int i = 0; i < (ih ? 3 : 2); ++i) {
//				renderer.setSeriesPaint(0, i, ih ? whSeriesPaint[i] : wohSeriesPaint[i]);
				renderer.setSeriesPaint(i, ih ? whSeriesPaint[i] : wohSeriesPaint[i]);
			}
			data.setData(v);
		}
	}
}
