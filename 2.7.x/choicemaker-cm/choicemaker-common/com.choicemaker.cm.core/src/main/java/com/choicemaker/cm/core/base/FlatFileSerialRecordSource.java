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

import java.io.IOException;
import java.util.logging.Logger;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.SerializableRecordSource;
import com.choicemaker.cm.core.Sink;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.xmlconf.RecordSourceXmlConf;

/**
 * This is a wrapper object around ?? and it can be serialized, because it
 * stores string values with which to create the DbRecordSource.
 * <p><pre>
 * BUG 2009-08-29 rphall
 * FIXME FlatFileSerialRecordSource appears misnamed, or rather the names of this class
 * and com.choicemaker.cm.io.flatfile.FlatSerialRecordSource appear reversed.
 * This basically holds RecordSource descriptors.
 * A better name for this class might be FlatRecordData (or, as noted above, FlatRecordSource)
 * or FlatRecordDataSource or SerializedRecordData or SerializedRecords
 * or SerializedRecordSourceDescriptor or SerializedRecordSource?
 * END BUG
 * </pre>
 * @author pcheung
 *
 */
public class FlatFileSerialRecordSource implements SerializableRecordSource {

	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(FlatFileSerialRecordSource.class.getName());

	private String rsFile;
	private String modelName;

	private transient RecordSource rs;
	private transient ImmutableProbabilityModel model;


	public FlatFileSerialRecordSource (String rsFile, String modelName) {
		this.modelName = modelName;
		this.rsFile = rsFile;
	}


	private RecordSource getRS () {
		if (rs == null) {
			try {
				rs = RecordSourceXmlConf.getRecordSource(rsFile);
				rs.setModel(getModel ());
			} catch (XmlConfException e) {
				log.severe(e.toString());
			}
		}
		return rs;
	}

	public ImmutableProbabilityModel getModel () {
		if (model == null) {
			model = PMManager.getModelInstance(modelName);
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
		getRS().open ();
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

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((modelName == null) ? 0 : modelName.hashCode());
		result = prime * result + ((rsFile == null) ? 0 : rsFile.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FlatFileSerialRecordSource other = (FlatFileSerialRecordSource) obj;
		if (modelName == null) {
			if (other.modelName != null)
				return false;
		} else if (!modelName.equals(other.modelName))
			return false;
		if (rsFile == null) {
			if (other.rsFile != null)
				return false;
		} else if (!rsFile.equals(other.rsFile))
			return false;
		return true;
	}

	/**
	 * Obsolete method for {@link #equals(Object)}. Used for testing only.
	 * @deprecated
	 */
	public boolean equals_00(Object o) {
		if (o instanceof FlatFileSerialRecordSource) {
			FlatFileSerialRecordSource rs = (FlatFileSerialRecordSource) o;
			return rs.rsFile.equals(this.rsFile) &&
				rs.modelName.equals(this.modelName);
		} else {
			return false;
		}
	}


}
