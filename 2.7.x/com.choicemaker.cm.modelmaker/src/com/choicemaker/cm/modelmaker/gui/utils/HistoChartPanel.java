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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Rectangle2D;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.util.MessageUtil;
import com.choicemaker.cm.modelmaker.filter.MarkedRecordPairFilter;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.jrefinery.chart.ChartMouseEvent;
import com.jrefinery.chart.ChartMouseListener;
import com.jrefinery.chart.ChartPanel;
import com.jrefinery.chart.JFreeChart;
import com.jrefinery.chart.axis.Axis;
import com.jrefinery.chart.axis.CategoryAxis;
import com.jrefinery.chart.axis.VerticalLogarithmicAxis;
import com.jrefinery.chart.axis.VerticalNumberAxis;
import com.jrefinery.chart.entity.CategoryItemEntity;
import com.jrefinery.chart.entity.ChartEntity;
import com.jrefinery.chart.event.ChartChangeEvent;
import com.jrefinery.chart.plot.CategoryPlot;

/**
 *
 * @author    
 * @version   $Revision: 1.2 $ $Date: 2010/03/29 12:53:08 $
 */
public class HistoChartPanel extends ChartPanel {
	protected int popupX;
	protected int popupY;
	protected JMenu select;
	protected float rangeFrom;
	protected float rangeTo;
	protected ModelMaker modelMaker;

	public HistoChartPanel(
		final JFreeChart chart,
		boolean properties,
		boolean save,
		boolean print,
		boolean zoom,
		boolean tooltips,
		final ModelMaker modelMaker) {
		super(chart, properties, save, print, zoom, tooltips);
		this.modelMaker = modelMaker;
		// horizontal zoom doesn't work, by setting false we don't get bogus menu item
		setHorizontalZoom(false);
		setVerticalZoom(true);
		final JCheckBoxMenuItem logYScale =
			new JCheckBoxMenuItem(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.test.logscale.y"));
		logYScale.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				CategoryPlot p = (CategoryPlot) chart.getPlot();
				Axis oldAxis = p.getRangeAxis();
				if (logYScale.isSelected()) {
					p.setRangeAxis(
						new VerticalLogarithmicAxis(
							MessageUtil.m.formatMessage("train.gui.modelmaker.panel.histogram.cm.numpairs")));
				} else {
					p.setRangeAxis(
						new VerticalNumberAxis(
							MessageUtil.m.formatMessage("train.gui.modelmaker.panel.histogram.cm.numpairs")));
				}
				oldAxis.setPlot(null);
				chartChanged(new ChartChangeEvent(this));
			}
		});
		JPopupMenu popup = getPopupMenu();
		popup.addSeparator();
		popup.add(logYScale);
		popup.addSeparator();
		select = new JMenu("Select");
		final JMenuItem all = new JMenuItem("All");
		select.add(all);
		final JMenuItem cmDiffer = new JMenuItem("Human marked differ");
		select.add(cmDiffer);
		final JMenuItem cmHold = new JMenuItem("Human marked hold");
		select.add(cmHold);
		final JMenuItem cmMatch = new JMenuItem("Human marked match");
		select.add(cmMatch);
		ActionListener l = new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				MarkedRecordPairFilter filter = modelMaker.getFilter();
				filter.reset();
				filter.setFromPercentage(rangeFrom);
				filter.setToPercentage(rangeTo);
				Object src = ev.getSource();
				if (src != all) {
					boolean[] b = new boolean[Decision.NUM_DECISIONS];
					if (src == cmDiffer) {
						b[Decision.DIFFER.toInt()] = true;
					} else if (src == cmHold) {
						b[Decision.HOLD.toInt()] = true;
					} else if (src == cmMatch) {
						b[Decision.MATCH.toInt()] = true;
					}
					filter.setHumanDecision(b);
				}
				modelMaker.filterMarkedRecordPairList();
			}
		};
		all.addActionListener(l);
		cmDiffer.addActionListener(l);
		cmHold.addActionListener(l);
		cmMatch.addActionListener(l);
		popup.add(select);
		addChartMouseListener(new ChartMouseListener() {
			public void chartMouseClicked(ChartMouseEvent evt) {
				ChartEntity e = evt.getEntity();
				if (e instanceof CategoryItemEntity) {
					CategoryItemEntity c = (CategoryItemEntity) e;
					int cat = c.getCategoryIndex();
					HistoCategoryDataset data = (HistoCategoryDataset)((CategoryPlot) getChart().getPlot()).getCategoryDataset();
					int len = data.getColumnCount();
					float step = 1f / len;
					rangeFrom = cat * step;
					rangeTo = rangeFrom + step;
					MarkedRecordPairFilter filter = modelMaker.getFilter();
					filter.reset();
					filter.setFromPercentage(rangeFrom);
					filter.setToPercentage(rangeTo);
					boolean[] b = new boolean[Decision.NUM_DECISIONS];
					int series = c.getSeries();
					if(data.isIncludeHolds() && series != 0) {
						if(series == 1) {
							series = 2;
						} else {
							series = 1;
						}
					}
					b[series] = true;
					filter.setHumanDecision(b);
					modelMaker.filterMarkedRecordPairList();

				}
			}
			public void chartMouseMoved(ChartMouseEvent arg0) {
			}
		});
	}

	protected void setContextMenu() {
		CategoryPlot categoryPlot = (CategoryPlot) getChart().getPlot();
		HistoCategoryDataset data = (HistoCategoryDataset) categoryPlot.getCategoryDataset();
		if (data.hasData()) {
			CategoryAxis axis = (CategoryAxis) categoryPlot.getDomainAxis();

			//axis.set
			Rectangle2D plotArea = getScaledDataArea();
			int cat = 0;
			int len = data.getColumnCount();
			for (int i = 0; i < len; ++i) {
				if (popupX >= axis.getCategoryStart(i, len, plotArea)
					&& popupX <= axis.getCategoryEnd(i, len, plotArea)) {
					cat = i;
					break;
				}
			}
			float step = 1f / len;
			rangeFrom = cat * step;
			rangeTo = rangeFrom + step;
			select.setText("Select range " + data.getColumnKey(cat));
		}
		select.setEnabled(data.hasData());
	}

	protected void displayPopupMenu(int x, int y) {
		popupX = x;
		popupY = y;
		setContextMenu();
		getPopupMenu().show(this, x, y);
	}

}
