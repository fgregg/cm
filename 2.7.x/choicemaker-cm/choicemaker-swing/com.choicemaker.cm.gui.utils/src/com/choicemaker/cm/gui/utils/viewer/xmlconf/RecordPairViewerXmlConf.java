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
package com.choicemaker.cm.gui.utils.viewer.xmlconf;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import com.choicemaker.cm.core.*;
import com.choicemaker.cm.core.xmlconf.*;
import com.choicemaker.cm.gui.utils.viewer.*;

/**
 *
 * @author    S. Yoakum-Stover
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class RecordPairViewerXmlConf {
	private static Logger logger = Logger.getLogger(RecordPairViewerXmlConf.class);

	private static final String COMPOSITE_PANE_MODEL = "compositePaneModel";
	private static final String COMPOSITE_FRAME_MODEL = "compositeFrameModel";
	private static final String RECORD_PAIR_VIEWER_MODEL = "recordPairViewerModel";
	private static final String RECORD_PAIR_FRAME_MODEL = "recordPairFrameModel";
	private static final String DESCRIPTOR_NAME = "descriptorName";
	private static final String ALIAS = "alias";
	private static final String X = "x";
	private static final String Y = "y";
	private static final String WIDTH = "width";
	private static final String HEIGHT = "height";
	private static final String DIVIDER_LOCATION = "dividerLocation";
	private static final String RECORD_TABLE_COLUMN_MODEL = "recordTableColumnModel";
	private static final String RECORD_TABLE_COLUMN = "recordTableColumn";
	private static final String NAME = "name";
	private static final String VISIBLE = "visible";
	private static final String DISPLAY_INDEX = "displayIndex";

	public static CompositePaneModel readLayout(String fileName, Descriptor descriptor) throws XmlConfException {
		Document document = null;
		SAXBuilder builder = XmlParserFactory.createSAXBuilder(false);
		try {
			document = builder.build(new File(fileName).getAbsoluteFile());
		} catch (Exception ex) {
			throw new XmlConfException("Internal error.", ex);
		}
		return readLayout(document, descriptor, fileName);
	}
	
	public static CompositePaneModel readLayoutFromJar(String name, Descriptor descriptor) throws XmlConfException {
		Document document = null;
		SAXBuilder builder = XmlParserFactory.createSAXBuilder(false);
		try {
			InputStream is = RecordPairViewerXmlConf.class.getResourceAsStream(name);
			document = builder.build(new InputStreamReader(is));
		} catch (Exception ex) {
			throw new XmlConfException("Internal error.", ex);
		}
		return readLayout(document, descriptor, name);
	}
	
	public static CompositePaneModel readLayout(URL url, Descriptor descriptor) throws XmlConfException {
		Document document = null;
		SAXBuilder builder = XmlParserFactory.createSAXBuilder(false);
		try {
			document = builder.build(url);
		} catch (Exception ex) {
			throw new XmlConfException("Internal error.", ex);
		}
		return readLayout(document, descriptor, url.toString());
	}

	
	public static CompositePaneModel readLayout(Document document, Descriptor descriptor, String fileName) throws XmlConfException {
		CompositePaneModel compositePaneModel =
			compositePaneModelFromXml(document.getRootElement(), new DescriptorCollection(descriptor));
		compositePaneModel.setFileName(fileName);
		return compositePaneModel;
	}
	
	public static CompositePaneModel compositePaneModelFromXml(Element e, DescriptorCollection d) {
		List t = new ArrayList();
		List viewerModels = e.getChildren(RECORD_PAIR_VIEWER_MODEL);
		for (Iterator iViewerModels = viewerModels.iterator(); iViewerModels.hasNext();) {
			Element f = (Element) iViewerModels.next();
			t.add(recordPairViewerModelFromXml(f, d));
		}
		return new CompositePaneModel(d.getDescriptor(), (RecordPairViewerModel[])t.toArray(new RecordPairViewerModel[t.size()]));
	}

	public static RecordPairViewerModel recordPairViewerModelFromXml(Element e, DescriptorCollection d) {
		List t = new ArrayList();
		List frameModels = e.getChildren(RECORD_PAIR_FRAME_MODEL);
		for (Iterator iFrameModels = frameModels.iterator(); iFrameModels.hasNext();) {
			Element f = (Element) iFrameModels.next();
			RecordPairFrameModel frameModel = recordPairFrameModelFromXml(f, d);
			if (frameModel != null) {
				t.add(frameModel);
			}
		}
		List compositeModels = e.getChildren(COMPOSITE_FRAME_MODEL);
		for (Iterator iCompositeModels = compositeModels.iterator(); iCompositeModels.hasNext();) {
			Element f = (Element) iCompositeModels.next();
			t.add(compositeFrameModelFromXml(f, d));
		}
		RecordPairViewerModel recordPairViewerModel =
			new RecordPairViewerModel(
				d.getDescriptor(),
				(InternalFrameModel[]) t.toArray(new InternalFrameModel[t.size()]));
		String alias = e.getAttributeValue(ALIAS);
		recordPairViewerModel.setAlias(alias);
		String w = e.getAttributeValue(WIDTH);
		String h = e.getAttributeValue(HEIGHT);
		if(w != null && h != null) {
			recordPairViewerModel.setPreferredSize(new Dimension(Integer.parseInt(w), Integer.parseInt(h)));
		}
		return recordPairViewerModel;
	}
	
	public static CompositeFrameModel compositeFrameModelFromXml(Element e, DescriptorCollection d) {
		String alias = e.getAttributeValue(ALIAS);
		int x = Integer.parseInt(e.getAttributeValue(X));
		int y = Integer.parseInt(e.getAttributeValue(Y));
		int width = Integer.parseInt(e.getAttributeValue(WIDTH));
		int height = Integer.parseInt(e.getAttributeValue(HEIGHT));
		Rectangle bounds = new Rectangle(x, y, width, height);
		CompositePaneModel compositePaneModel = compositePaneModelFromXml(e.getChild(COMPOSITE_PANE_MODEL), d);
		return new CompositeFrameModel(d.getDescriptor(), compositePaneModel, alias, bounds);
	}

	public static RecordPairFrameModel recordPairFrameModelFromXml(Element e, DescriptorCollection d) {
		RecordPairFrameModel recordPairFrameModel = null;
		String descriptorName = e.getAttributeValue(DESCRIPTOR_NAME);
		Descriptor descriptor = d.getDescriptor(e.getAttributeValue(DESCRIPTOR_NAME));
		if (descriptor != null) {
			String alias = e.getAttributeValue(ALIAS);
			int x = Integer.parseInt(e.getAttributeValue(X));
			int y = Integer.parseInt(e.getAttributeValue(Y));
			int width = Integer.parseInt(e.getAttributeValue(WIDTH));
			int height = Integer.parseInt(e.getAttributeValue(HEIGHT));
			Rectangle bounds = new Rectangle(x, y, width, height);
			int dividerLocation = Integer.parseInt(e.getAttributeValue(DIVIDER_LOCATION));
			RecordTableColumnModel recordTableColumnModel =
				recordTableColumnModelFromXml(e.getChild(RECORD_TABLE_COLUMN_MODEL), descriptor);
			recordPairFrameModel =
				new RecordPairFrameModel(descriptor, alias, bounds, dividerLocation, recordTableColumnModel);
		}
		return recordPairFrameModel;
	}

	public static RecordTableColumnModel recordTableColumnModelFromXml(Element e, Descriptor descriptor) {
		RecordTableColumnModel recordTableColumnModel = new RecordTableColumnModel(descriptor, false);
		Map m = new HashMap();
		RecordTableColumn[] columns = recordTableColumnModel.getVisibleAndInvisibleColumns();
		for (int i = 0; i < columns.length; i++) {
			RecordTableColumn column = columns[i];
			m.put(column.getFieldName(), column);
		}
		SortedSet visibleColumns = new TreeSet(new Comparator() {
			public int compare(Object o1, Object o2) {
				int pos1 = ((RecordTableColumn) o1).getDisplayIndex();
				int pos2 = ((RecordTableColumn) o2).getDisplayIndex();
				if (pos1 < pos2) {
					return -1;
				} else if (pos1 > pos2) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		List l = e.getChildren(RECORD_TABLE_COLUMN);
		for (Iterator iL = l.iterator(); iL.hasNext();) {
			Element f = (Element) iL.next();
			String name = f.getAttributeValue(NAME);
			RecordTableColumn column = (RecordTableColumn) m.get(name);
			if (column != null) {
				column.setPreferredWidth(Integer.parseInt(f.getAttributeValue(WIDTH)));
				column.setHeaderValue(f.getAttributeValue(ALIAS));
				boolean visible = Boolean.valueOf(f.getAttributeValue(VISIBLE)).booleanValue();
				if (visible) {
					column.setVisible(true);
					int displayIndex = Integer.parseInt(f.getAttributeValue(DISPLAY_INDEX));
					column.setDisplayIndex(displayIndex);
					visibleColumns.add(column);
				}
			}
		}
		for (Iterator iVisibleColumns = visibleColumns.iterator(); iVisibleColumns.hasNext();) {
			RecordTableColumn recordTableColumn = (RecordTableColumn) iVisibleColumns.next();
			recordTableColumnModel.addColumn(recordTableColumn);
		}
		return recordTableColumnModel;
	}

	public static void saveLayout(CompositePaneModel compositePaneModel) throws XmlConfException {
		Element layout = modelToXml(compositePaneModel);
		try {
			FileOutputStream fs = new FileOutputStream(new File(compositePaneModel.getFileName()).getAbsoluteFile());
			XMLOutputter o = new XMLOutputter("    ", true);
			o.setTextNormalize(true);
			o.output(layout, fs);
			fs.close();
		} catch (IOException ex) {
			throw new XmlConfException("Problem writing file.", ex);
		}
	}
	
	public static Element modelToXml(CompositePaneModel compositePaneModel) {
		Element res = new Element(COMPOSITE_PANE_MODEL);
		RecordPairViewerModel[] recordPairViewerModels = compositePaneModel.getViewerModels();
		for (int i = 0; i < recordPairViewerModels.length; i++) {
			res.addContent(modelToXml(recordPairViewerModels[i]));
		}
		return res;
	}

	public static Element modelToXml(RecordPairViewerModel recordPairViewerModel) {
		Element res = new Element(RECORD_PAIR_VIEWER_MODEL);
		String alias = recordPairViewerModel.getAlias();
		if(alias == null) {
			alias = "";
		}
		res.setAttribute(ALIAS, alias);
		Dimension preferredSize = recordPairViewerModel.getPreferredSize();
		res.setAttribute(WIDTH, String.valueOf((int)preferredSize.getWidth()));
		res.setAttribute(HEIGHT, String.valueOf((int)preferredSize.getHeight()));
		InternalFrameModel[] recordPairFrameModels = recordPairViewerModel.getFrameModels();
		for (int i = 0; i < recordPairFrameModels.length; i++) {
			res.addContent(modelToXml(recordPairFrameModels[i]));
		}
		return res;
	}

	public static Element modelToXml(InternalFrameModel internalFrameModel) {
		if(internalFrameModel instanceof RecordPairFrameModel) {
			return modelToXml((RecordPairFrameModel)internalFrameModel);
		} else {
			return modelToXml((CompositeFrameModel)internalFrameModel);
		}
	}

	public static Element modelToXml(RecordPairFrameModel recordPairViewerModel) {
		Element res = new Element(RECORD_PAIR_FRAME_MODEL);
		res.setAttribute(DESCRIPTOR_NAME, recordPairViewerModel.getDescriptor().getRecordName());
		res.setAttribute(ALIAS, recordPairViewerModel.getAlias());
		Rectangle bounds = recordPairViewerModel.getBounds();
		res.setAttribute(X, String.valueOf(bounds.x));
		res.setAttribute(Y, String.valueOf(bounds.y));
		res.setAttribute(WIDTH, String.valueOf(bounds.width));
		res.setAttribute(HEIGHT, String.valueOf(bounds.height));
		res.setAttribute(DIVIDER_LOCATION, String.valueOf(recordPairViewerModel.getDividerLocation()));
		res.addContent(modelToXml(recordPairViewerModel.getRecordTableColumnModel()));
		return res;
	}

	public static Element modelToXml(CompositeFrameModel compositeFrameModel) {
		Element res = new Element(COMPOSITE_FRAME_MODEL);
		Rectangle bounds = compositeFrameModel.getBounds();
		res.setAttribute(ALIAS, compositeFrameModel.getAlias());
		res.setAttribute(X, String.valueOf(bounds.x));
		res.setAttribute(Y, String.valueOf(bounds.y));
		res.setAttribute(WIDTH, String.valueOf(bounds.width));
		res.setAttribute(HEIGHT, String.valueOf(bounds.height));
		res.addContent(modelToXml(compositeFrameModel.getCompositePaneModel()));	
		return res;
	}

	private static Element modelToXml(RecordTableColumnModel recordTableColumnModel) {
		Element res = new Element(RECORD_TABLE_COLUMN_MODEL);
		RecordTableColumn[] visibleAndInvisibleColumns = recordTableColumnModel.getVisibleAndInvisibleColumns();
		for (int i = 0; i < visibleAndInvisibleColumns.length; i++) {
			res.addContent(modelToXml(visibleAndInvisibleColumns[i]));
		}
		return res;
	}

	private static Element modelToXml(RecordTableColumn recordTableColumn) {
		Element res = new Element(RECORD_TABLE_COLUMN);
		res.setAttribute(DISPLAY_INDEX, String.valueOf(recordTableColumn.getDisplayIndex()));
		res.setAttribute(NAME, recordTableColumn.getFieldName());
		res.setAttribute(ALIAS, recordTableColumn.getHeaderValue().toString());
		res.setAttribute(WIDTH, String.valueOf(recordTableColumn.getWidth()));
		res.setAttribute(VISIBLE, String.valueOf(recordTableColumn.isVisible()));
		return res;
	}
}
