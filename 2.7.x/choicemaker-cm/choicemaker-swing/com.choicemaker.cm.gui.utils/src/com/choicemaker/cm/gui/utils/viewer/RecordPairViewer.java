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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultDesktopManager;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import com.choicemaker.cm.core.base.DescriptorCollection;
import com.choicemaker.cm.core.base.RecordData;
import com.choicemaker.cm.core.datamodel.ObservableDataEvent;
import com.choicemaker.cm.core.datamodel.ObservableDataListener;
import com.choicemaker.cm.gui.utils.viewer.event.DesktopPaneMouseListener;

/**
 * .
 *
 * @author arturof
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class RecordPairViewer extends JPanel implements ObservableDataListener, PropertyChangeListener, Scrollable {

	//********************** Fields

	private static final long serialVersionUID = 1L;
	private boolean contentEditable;
	private CompositePane parentComposite;

	private RecordPairViewerModel recordPairViewerModel;
	private JDesktopPane desktopPane;
	private DesktopPaneMouseListener desktopPaneManager;
	private RecordData recordData;
	private List internalFrames;
	private boolean pair;

	private EnableableDesktopManager desktopManager;

	//********************** Construction

	/**
	 * Constructs a RecordPairViewer.
	 */
	public RecordPairViewer(boolean pair, boolean contentEditable) {
		this.pair = pair;
		this.contentEditable = contentEditable;
		setLayout(new GridLayout(1, 1));
		//		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}
	
	public void destroy() {
		for (Iterator iInternalFrames = internalFrames.iterator(); iInternalFrames.hasNext();) {
			((InternalFrame) iInternalFrames.next()).destroy();
			
		}
		recordPairViewerModel.removeCompositeObservableDataListener(this);
		recordPairViewerModel.removePropertyChangeListener(this);
	}

	//********************** Accessors

	/**
	 * Sets the parentComposite.
	 * @param parentComposite The parentComposite to set
	 */
	public void setParentComposite(CompositePane parentComposite) {
		this.parentComposite = parentComposite;
	}

	public boolean hasParentComposite() {
		return parentComposite != null;
	}

	public boolean hasSiblings() {
		return hasParentComposite() && parentComposite.hasMultipleViewers();
	}

	public void addTab() {
		if (hasParentComposite()) {
			parentComposite.getCompositePaneModel().addTab();
		}
	}

	public void removeCurrentTab() {
		if (hasParentComposite()) {
			parentComposite.getCompositePaneModel().removeViewerModel(getRecordPairViewerModel());
		}
	}

	/**
	 * Returns the recordPairViewerModel.
	 * @return RecordPairViewerModel
	 */
	public RecordPairViewerModel getRecordPairViewerModel() {
		return recordPairViewerModel;
	}

	/**
	 * Sets the recordPairViewerModel.
	 * @param recordPairViewerModel The recordPairViewerModel to set
	 */
	public void setRecordPairViewerModel(RecordPairViewerModel recordPairViewerModel) {
		if (this.recordPairViewerModel != null) {
			this.recordPairViewerModel.removePropertyChangeListener(this);
			this.recordPairViewerModel.removeCompositeObservableDataListener(this);
		}
		this.recordPairViewerModel = recordPairViewerModel;
		createDesktopPane();
		this.recordPairViewerModel.addPropertyChangeListener(this);
		recordPairViewerModel.addCompositeObservableDataListener(this);

		updateFromModel();
	}

	private void createDesktopPane() {
		if (desktopPane != null) {
			this.remove(desktopPane);
			desktopPane.removeMouseListener(desktopPaneManager);
			desktopManager = null;
			desktopPaneManager = null;
		}
		if (recordPairViewerModel != null) {
			desktopPane = new JDesktopPane();
			desktopManager = new EnableableDesktopManager();
			desktopPane.setDesktopManager(desktopManager);

			desktopPaneManager =
				new DesktopPaneMouseListener(
					this,
					recordPairViewerModel.getDescriptor(),
					new DescriptorCollection(recordPairViewerModel.getDescriptor()).getDescriptors());

			desktopPane.addMouseListener(desktopPaneManager);

			add(desktopPane, BorderLayout.CENTER);
			createFramesFromModel();
			Dimension s = recordPairViewerModel.getPreferredSize();
			if (s == null) {
				fitDesktopPane();
			} else {
				setPreferredSize(s);
			}
		}
	}

	private void createFramesFromModel() {
		internalFrames = new ArrayList();
		InternalFrameModel[] frameModels = recordPairViewerModel.getFrameModels();
		for (int i = 0; i < frameModels.length; i++) {
			InternalFrame frame = createInternalFrame(frameModels[i]);
			frame.setInternalFrameModel(frameModels[i]);
			addInternalFrame(frame);
		}
	}

	public RecordData getRecordData() {
		return recordData;
	}

	public void setRecordData(RecordData markedRecordPair) {
		this.recordData = markedRecordPair;
		for (Iterator iter = internalFrames.iterator(); iter.hasNext();) {
			InternalFrame internalFrame = (InternalFrame) iter.next();
			internalFrame.setRecordData(markedRecordPair);
		}
	}

	private void addInternalFrame(InternalFrame internalFrame) {
		internalFrames.add(internalFrame);
		internalFrame.addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosing(InternalFrameEvent e) {
				recordPairViewerModel.removeFrameModel(((InternalFrame) e.getInternalFrame()).getInternalFrameModel());
			}
		});
		internalFrame.setRecordData(recordData);
		internalFrame.setVisible(true);
		desktopPane.add(internalFrame);
		//		fitDesktopPane();
	}

	private void removeInternalFrame(InternalFrame f) {
		internalFrames.remove(f);
		desktopPane.remove(f);
		repaint();
	}

	public void fitDesktopPane() {
		if (internalFrames.size() != 0) {
			int minX = Integer.MAX_VALUE;
			int maxX = Integer.MIN_VALUE;
			int minY = Integer.MAX_VALUE;
			int maxY = Integer.MIN_VALUE;
			for (Iterator internalFramesIterator = internalFrames.iterator(); internalFramesIterator.hasNext();) {
				InternalFrame f = (InternalFrame) internalFramesIterator.next();
				int x = f.getX();
				int y = f.getY();
				minX = Math.min(minX, x);
				maxX = Math.max(maxX, x + f.getWidth());
				minY = Math.min(minY, y);
				maxY = Math.max(maxY, y + f.getHeight());
			}
			Dimension size = new Dimension(maxX - minX, maxY - minY);
			if (minX != 0 || minY != 0) {
				for (Iterator internalFramesIterator = internalFrames.iterator(); internalFramesIterator.hasNext();) {
					InternalFrame f = (InternalFrame) internalFramesIterator.next();
					f.setBounds(f.getX() - minX, f.getY() - minY, f.getWidth(), f.getHeight());
				}
			}
			setPreferredSize(size);
		}
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

	public void growDesktopPane(int x, int y) {
		Dimension d = getPreferredSize();
		setPreferredSize(new Dimension((int) d.getWidth() + x, (int) d.getHeight() + y));
	}

	//********************** ObservableDataListener Methods

	/**
	 * @see com.choicemaker.cm.reviewmaker.gui.datamodel.ObservableDataListener#observableDataAdded(com.choicemaker.cm.reviewmaker.gui.datamodel.ObservableDataEvent)
	 */
	public void observableDataAdded(ObservableDataEvent event) {
		InternalFrame frame = createInternalFrame((InternalFrameModel) event.getChild());
		frame.setInternalFrameModel((InternalFrameModel) event.getChild());
		addInternalFrame(frame);
	}

	/**
	 * @see com.choicemaker.cm.reviewmaker.gui.datamodel.ObservableDataListener#observableDataRemoved(com.choicemaker.cm.reviewmaker.gui.datamodel.ObservableDataEvent)
	 */
	public void observableDataRemoved(ObservableDataEvent event) {
		Object frameModel = event.getChild();
		Iterator internalFramesIterator = internalFrames.iterator();
		while (internalFramesIterator.hasNext()) {
			InternalFrame frame = (InternalFrame) internalFramesIterator.next();
			if (frame.getInternalFrameModel().equals(frameModel)) {
				removeInternalFrame(frame);
				return;
			}
		}
	}

	//********************** PropertyChangeListener Methods

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		updateFromModel();
	}

	protected void updateFromModel() {
		// AJW: added to avoid set layout editable exception
		if (getRecordPairViewerModel() == null) {
			return;
		}
		
		boolean enableEditing = getRecordPairViewerModel().isEnableEditing();
		desktopPaneManager.setEnableEditing(enableEditing);
		desktopManager.setEnabled(enableEditing);
	}

	//********************** Helper methods

	protected InternalFrame createInternalFrame(InternalFrameModel model) {
		InternalFrame frame = null;
		if (model instanceof RecordPairFrameModel) {
			frame = new RecordPairFrame(pair, contentEditable);
		} else if (model instanceof CompositeFrameModel) {
			frame = new CompositeFrame(pair, contentEditable);
		} else {
			throw new IllegalArgumentException("event type unsuported. " + model);
		}

		return frame;
	}

	//********************** Inner Class

	public class EnableableDesktopManager extends DefaultDesktopManager {

		private static final long serialVersionUID = 1L;
		private boolean enabled;

		/* (non-Javadoc)
		 * @see javax.swing.DesktopManager#beginResizingFrame(javax.swing.JComponent, int)
		 */
		public void beginResizingFrame(JComponent f, int direction) {
			if (enabled) {
				super.beginResizingFrame(f, direction);
				desktopPane.repaint();
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.DesktopManager#dragFrame(javax.swing.JComponent, int, int)
		 */
		public void dragFrame(JComponent f, int newX, int newY) {
			if (enabled) {
				super.dragFrame(f, newX, newY);
				desktopPane.repaint();
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.DesktopManager#endDraggingFrame(javax.swing.JComponent)
		 */
		public void endDraggingFrame(JComponent f) {
			if (enabled) {
				super.endDraggingFrame(f);
				desktopPane.repaint();
			}
		}

		/**
		 * @return boolean
		 */
		public boolean isEnabled() {
			return enabled;
		}

		/**
		 * Sets the enabled.
		 * @param enabled The enabled to set
		 */
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#setPreferredSize(java.awt.Dimension)
	 */
	public void setPreferredSize(Dimension preferredSize) {
		super.setPreferredSize(preferredSize);
		if (preferredSize == null
			? recordPairViewerModel.getPreferredSize() != null
			: !preferredSize.equals(recordPairViewerModel.getPreferredSize())) {
			recordPairViewerModel.setPreferredSize(preferredSize);
			fix();
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getPreferredScrollableViewportSize()
	 */
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableUnitIncrement(java.awt.Rectangle, int, int)
	 */
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 100;
	}

	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableBlockIncrement(java.awt.Rectangle, int, int)
	 */
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		if (orientation == SwingConstants.VERTICAL) {
			return visibleRect.height;
		} else {
			return visibleRect.width;
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableTracksViewportWidth()
	 */
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableTracksViewportHeight()
	 */
	public boolean getScrollableTracksViewportHeight() {
		return true;
	}
}
