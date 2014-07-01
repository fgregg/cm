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
package com.choicemaker.cm.core.base;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.ISerializableFileBasedRecordSource;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.IncompleteSpecificationException;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.Sink;
import com.choicemaker.cm.core.util.Equal;
import com.choicemaker.cm.core.util.HashUtils;
import com.choicemaker.cm.core.util.Precondition;
import com.choicemaker.cm.core.util.StringUtils;
import com.choicemaker.cm.core.xmlconf.RecordSourceXmlConf;
import com.choicemaker.cm.core.xmlconf.XmlConfException;

/**
 * This is a wrapper object around a Record Source Descriptor. It can be serialized, because it
 * stores string values with which to create a RecordSource.
 * @author pcheung
 * @author rphall (Renamed from FlatFileSerialRecordSource)
 *
 */
public class SerializedRecordSourceDescriptor implements ISerializableFileBasedRecordSource {

	private static final long serialVersionUID = 1L;

	private static final Logger log =
		Logger.getLogger(SerializedRecordSourceDescriptor.class);

	private String descriptorFileName;
	private String modelName;

	private transient RecordSource rs;
	private transient ImmutableProbabilityModel model;

	public SerializedRecordSourceDescriptor(String rsFile, String modelName) {
		setModelName(modelName);
		setRsFile(rsFile);
	}

	private RecordSource getRS() {
		if (rs == null) {
			try {
				rs = RecordSourceXmlConf.getRecordSource(getRsFile());
				rs.setModel(getModel());
			} catch (XmlConfException e) {
				log.error(e);
			}
		}
		return rs;
	}

	public ImmutableProbabilityModel getModel() {
		if (model == null) {
			model = PMManager.getModelInstance(getModelName());
		}
		return model;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.RecordSource#getNext()
	 */
	public Record getNext() throws IOException {
		return getRS().getNext();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.Source#open()
	 */
	public void open() throws IOException {
		getRS().open();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.Source#close()
	 */
	public void close() throws IOException {
		getRS().close();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.Source#hasNext()
	 */
	public boolean hasNext() throws IOException {
		return getRS().hasNext();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.Source#getName()
	 */
	public String getName() {
		return getRS().getName();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.Source#setName(java.lang.String)
	 */
	public void setName(String name) {
		getRS().setName(name);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.Source#setModel(com.choicemaker.cm.core.base.ProbabilityModel)
	 */
	public void setModel(ImmutableProbabilityModel m) {
		getRS().setModel(m);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.Source#hasSink()
	 */
	public boolean hasSink() {
		return getRS().hasSink();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.Source#getSink()
	 */
	public Sink getSink() {
		return getRS().getSink();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.core.base.Source#getFileName()
	 */
	public String getFileName() {
		return getRS().getFileName();
	}

	public boolean equals(Object o) {
		boolean retVal = false;
		if (o instanceof SerializedRecordSourceDescriptor) {
			SerializedRecordSourceDescriptor rs = (SerializedRecordSourceDescriptor) o;
			retVal = Equal.and(retVal, this.getModelName(), rs.getModelName());
			if (retVal) {
				if (this.getRsFile() == null || rs.getRsFile() == null) {
					retVal = false;
				} else {
					try {
						File thisFile = new File(this.getRsFile()).getAbsoluteFile();
						File thatFile = new File(rs.getRsFile()).getAbsoluteFile();
						retVal = Equal.and(retVal, thisFile, thatFile);
					} catch (Exception x) {
						log.warn(
							"Unable to get absolute files for equality test",
							x);
						retVal = Equal.and(retVal, this.getRsFile(), rs.getRsFile());
					}
				}
			}
		}
		return retVal;
	}

	public int hashCode() {
		int retVal = HashUtils.hashCode(this.getModelName());
		retVal = HashUtils.hashCode(retVal, this.getRsFile());
		return retVal;
	}

	public Properties getProperties() {
		Properties retVal = new Properties();
		retVal.setProperty(PN_DESCRIPTOR_FILE_NAME, this.getRsFile());
		retVal.setProperty(PN_MODEL_NAME, this.getModelName());
		return retVal;
	}

	public void  setProperties(Properties properties) throws IncompleteSpecificationException {

		Precondition.assertNonNullArgument("null properties",properties);

		String s = properties.getProperty(PN_DESCRIPTOR_FILE_NAME);
		if (!StringUtils.nonEmptyString(s)) {
			String msg = "Missing property '" + PN_DESCRIPTOR_FILE_NAME + "'";
			log.error(msg);
			throw new IncompleteSpecificationException(msg);
		}
		setRsFile(s);

		s = properties.getProperty(PN_MODEL_NAME);
		if (!StringUtils.nonEmptyString(s)) {
			String msg = "Missing property '" + PN_MODEL_NAME + "'";
			log.error(msg);
			throw new IncompleteSpecificationException(msg);
		}
		setModelName(s);

		try {
			rs.setModel(null);
			if (rs != null) {
				rs.close();
			}
		} catch (Exception x) {
			String msg = "Unable to close " + (rs == null ? "record source" : rs.getName());
			log.warn(msg);
		} finally {
			rs = null;
		}
	}

	public String toXML() {
		String retVal = AbstractRecordSourceSerializer.toXML(this);
		return retVal;
	}

	private void setModelName(String s) {
		Precondition.assertNonEmptyString(s);
		this.modelName = s;
	}

	public String getModelName() {
		return this.modelName;
	}

	private void setRsFile(String s) {
		Precondition.assertNonEmptyString(s);
		this.descriptorFileName = s;
	}

	public String getRsFile() {
		return this.descriptorFileName;
	}

}
