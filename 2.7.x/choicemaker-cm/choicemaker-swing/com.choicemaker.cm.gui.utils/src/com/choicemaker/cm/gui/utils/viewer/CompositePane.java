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
package com.choicemaker.cm.gui.utils.viewer;

import java.awt.*;
import java.beans.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import com.choicemaker.cm.core.RecordData;
import com.choicemaker.cm.core.datamodel.*;

/**
 * TODO: we still have to deal with the Tab Label...
 * 
 * @author  Arturo Falck
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class CompositePane extends JPanel implements ObservableDataListener {
	private static final long serialVersionUID = 1L;
	private boolean contentEditable;
	private JComponent pane;

	private List recordPairViewers;
	private RecordData recordData;
	private CompositePaneModel compositePaneModel;
	private boolean pair;

	private WeakHashMap listeners = new WeakHashMap();

	public CompositePane(boolean pair, boolean contentEditable) {
		this.pair = pair;
		this.contentEditable = contentEditable;
		setLayout(new BorderLayout());
		recordPairViewers = new ArrayList();
	}

	public void destroy() {
		compositePaneModel.removeCompositeObservableDataListener(this);
		for (Iterator iListeners = listeners.entrySet().iterator(); iListeners.hasNext();) {
			Map.Entry e = (Map.Entry) iListeners.next();
			((RecordPairViewer) e.getValue()).getRecordPairViewerModel().removePropertyChangeListener(
				(PropertyChangeListener) e.getKey());
		}
		for (Iterator iRecordPairViewers = recordPairViewers.iterator(); iRecordPairViewers.hasNext();) {
			RecordPairViewer rpv = (RecordPairViewer) iRecordPairViewers.next();
			rpv.destroy();
		}
		listeners = null;
	}

	public void setRecordData(RecordData recordData) {
		this.recordData = recordData;
		for (Iterator iter = recordPairViewers.iterator(); iter.hasNext();) {
			RecordPairViewer internalFrame = (RecordPairViewer) iter.next();
			internalFrame.setRecordData(recordData);
		}
	}

	public RecordData getRecordData() {
		return recordData;
	}

	/**
	 * Sets the recordPairFrameModel.
	 * @param recordPairFrameModel The recordPairFrameModel to set
	 */
	public void setCompositePaneModel(CompositePaneModel compositePaneModel) {
		recordData = null;
		if (compositePaneModel != null) {
			compositePaneModel.removeCompositeObservableDataListener(this);
		}
		this.compositePaneModel = compositePaneModel;
		createTabbedPane();
		if (compositePaneModel != null) {
			compositePaneModel.addCompositeObservableDataListener(this);
		}
		//		validate();
		fix();
	}

	private void fix() {
		invalidate();
		Component r = this;
		Component n = null;
		while ((n = r.getParent()) != null) {
			r = n;
		}
		r.validate();
		r.repaint();
	}

	/**
	 * Returns the compositePaneModel.
	 * @return CompositePaneModel
	 */
	public CompositePaneModel getCompositePaneModel() {
		return compositePaneModel;
	}

	private void addViewer(RecordPairViewer viewer) {
		resetPane();
		recordPairViewers.add(viewer);
		viewer.setRecordData(recordData);
		viewer.setVisible(true);
		addToUnderlyingComponent(viewer);

		viewer.setParentComposite(this);
	}

	private void removeViewer(RecordPairViewer viewer) {
		removeFromUnderlyingComponent(viewer);
		resetPane();
		recordPairViewers.remove(viewer);

		viewer.setParentComposite(null);

		repaint();
	}

	// ***************** Pane Creation Helper Methods

	private void createTabbedPane() {
		if (pane != null) {
			remove(pane);
		}
		if (compositePaneModel != null) {
			pane = createPane();
			add(pane, BorderLayout.CENTER);
			createViewersFromModel();
		}
	}

	private void resetPane() {
		RecordPairViewer[] children = getChildrenFromUnderlyingComponent();
		remove(pane);
		pane = createPane();
		for (int i = 0; i < children.length; i++) {
			addToUnderlyingComponent(children[i]);
		}
		add(pane, BorderLayout.CENTER);
	}

	private JComponent createPane() {
		JComponent returnValue;
		if (hasMultipleViewers()) {
			returnValue = new JTabbedPane();
			((JTabbedPane) returnValue).setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		} else {
			returnValue = new JPanel();
			returnValue.setLayout(new BorderLayout());
		}

		return returnValue;

	}

	public boolean hasMultipleViewers() {
		return compositePaneModel.getViewerModels().length > 1;
	}

	private void addToUnderlyingComponent(RecordPairViewer viewer) {
		JScrollPane scrollPane = new JScrollPane(viewer);
		addWeakListener(viewer);

		if (pane instanceof JTabbedPane) {
			((JTabbedPane) pane).addTab(viewer.getRecordPairViewerModel().getAlias(), null, scrollPane, null);
		} else {
			pane.add(scrollPane, BorderLayout.CENTER);
		}
	}

	/**
	 * This ugliness is necessary to update the Tab's Name... the references to the listeners are all Weak, so
	 * this shouldn't create a memory leak, but Note that we are never explicitly removing the references.
	 * 
	 * NOTE: this is an abuse of the WeakHashMap!
	 */
	private void addWeakListener(RecordPairViewer viewer) {
		PropertyChangeListener listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName() == RecordPairViewerModel.ALIAS) {
					RecordPairViewer tabbedViewer = (RecordPairViewer) listeners.get(this);
					((JTabbedPane) pane).setTitleAt(
						getTabIndex((JTabbedPane) pane, tabbedViewer),
						(String) evt.getNewValue());
				}
			}
		};
		viewer.getRecordPairViewerModel().addPropertyChangeListener(listener);
		listeners.put(listener, viewer);
	}

	private void removeFromUnderlyingComponent(RecordPairViewer viewer) {
		JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, viewer);

		if (pane instanceof JTabbedPane) {
			((JTabbedPane) pane).removeTabAt(getTabIndex((JTabbedPane) pane, viewer));
		} else {
			pane.remove(scrollPane);
		}
	}

	private int getTabIndex(JTabbedPane tabbedPane, RecordPairViewer viewer) {
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			if (viewer == ((JScrollPane) tabbedPane.getComponentAt(i)).getViewport().getComponent(0)) {
				return i;
			}
		}

		throw new IllegalArgumentException(viewer + " is not a child of " + tabbedPane);
	}

	private RecordPairViewer[] getChildrenFromUnderlyingComponent() {
		RecordPairViewer[] returnValue = null;
		if (pane instanceof JTabbedPane) {
			JTabbedPane tabbedPane = (JTabbedPane) pane;
			returnValue = new RecordPairViewer[tabbedPane.getTabCount()];
			for (int i = 0; i < returnValue.length; i++) {
				//                                                    JTabbedPane->JScrollPane->ViewPort->RecordPairViewer
				//                                                       |             |           |           |      
				returnValue[i] =
					(RecordPairViewer) ((JScrollPane) tabbedPane.getComponentAt(i)).getViewport().getComponent(0);
			}
		} else {
			Component[] components = pane.getComponents();
			returnValue = new RecordPairViewer[components.length];

			for (int i = 0; i < returnValue.length; i++) {
				//                                                 JScrollPane->ViewPort->RecordPairViewer
				//                                                       |           |           |   
				returnValue[i] = (RecordPairViewer) ((JScrollPane) components[i]).getViewport().getComponent(0);
			}
		}

		return returnValue;
	}

	private void createViewersFromModel() {
		recordPairViewers = new ArrayList();
		RecordPairViewerModel[] viewerModels = compositePaneModel.getViewerModels();
		for (int i = 0; i < viewerModels.length; i++) {
			RecordPairViewer viewer = new RecordPairViewer(pair, contentEditable);
			viewer.setRecordPairViewerModel(viewerModels[i]);
			addViewer(viewer);
		}
	}

	// ***************** ObservableDataListener Methods

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.reviewmaker.gui.datamodel.ObservableDataListener#observableDataAdded(com.choicemaker.cm.reviewmaker.gui.datamodel.ObservableDataEvent)
	 */
	public void observableDataAdded(ObservableDataEvent event) {
		RecordPairViewer viewer = new RecordPairViewer(pair, contentEditable);
		viewer.setRecordPairViewerModel((RecordPairViewerModel) event.getChild());
		addViewer(viewer);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.reviewmaker.gui.datamodel.ObservableDataListener#observableDataRemoved(com.choicemaker.cm.reviewmaker.gui.datamodel.ObservableDataEvent)
	 */
	public void observableDataRemoved(ObservableDataEvent event) {
		Object viewerModel = event.getChild();
		Iterator internalFramesIterator = recordPairViewers.iterator();
		while (internalFramesIterator.hasNext()) {
			RecordPairViewer viewer = (RecordPairViewer) internalFramesIterator.next();
			if (viewer.getRecordPairViewerModel().equals(viewerModel)) {
				removeViewer(viewer);
				return;
			}
		}
	}

}
