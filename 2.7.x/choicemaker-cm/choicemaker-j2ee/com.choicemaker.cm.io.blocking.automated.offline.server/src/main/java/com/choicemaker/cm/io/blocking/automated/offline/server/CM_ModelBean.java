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
package com.choicemaker.cm.io.blocking.automated.offline.server;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.util.SystemPropertyUtils;

/**
 * Persistent information about a probability model. This information is not
 * sufficient to reproduce a matching model, but it is sufficient to check
 * whether model is the same as some previously saved model.
 * 
 * @author rphall
 *
 */
@NamedQuery(name = "modelFindAll",
		query = "Select model from CM_ModelBean model")
@Entity
@Table(/* schema = "CHOICEMAKER", */name = "CM_MDB_MODEL")
public class CM_ModelBean implements Serializable {

	private static final long serialVersionUID = 271L;

	/** Default value for non-persistent models */
	public static final int INVALID_MODEL_ID = 0;

	public static final String DATE_FORMAT_SPEC = "yyyy-MM-dd HH:mm";

	public static enum NamedQuery {
		FIND_ALL("modelFindAll");
		public final String name;

		NamedQuery(String name) {
			this.name = name;
		}
	}

	static boolean isInvalidModelId(long id) {
		return id == INVALID_MODEL_ID;
	}

	static boolean isNonPersistent(CM_ModelBean model) {
		boolean retVal = true;
		if (model != null) {
			retVal = isInvalidModelId(model.getId());
		}
		return retVal;
	}

	public static String createDefaultNotes(ImmutableProbabilityModel ipm) {
		String modelName = ipm.getModelName();
		String user = System.getProperty(SystemPropertyUtils.USER_NAME);
		DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_SPEC);
		String timeStamp = dateFormat.format(new Date());
		String retVal =
			"Persisted from '" + modelName + "' by '" + user + "' ("
					+ timeStamp + "); ";
		return retVal;
	}

	@Id
	@Column(name = "ID")
	@TableGenerator(name = "CMT_MODEL", table = "CMT_SEQUENCE",
			pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT",
			pkColumnValue = "CMT_MODEL")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "CMT_MODEL")
	private long id;

	@Column(name = "MODEL_SIGNATURE")
	private String modelSignature;

	@Column(name = "MODEL_NAME")
	private String modelName;

	@Column(name = "EVALUATOR_SIGNATURE")
	private String evaluatorSignature;

	@Column(name = "CLUESET_NAME")
	private String cluesetName;

	@Column(name = "CLUESET_SIGNATURE")
	private String cluesetSignature;

	@Column(name = "SCHEMA_NAME")
	private String schemaName;

	@Column(name = "SCHEMA_SIGNATURE")
	private String schemaSignature;

	@Column(name = "NOTES")
	private String notes;

//	@ElementCollection
//	@CollectionTable(name = "CMT_MODEL_CONFIGURATION",
//			joinColumns = @JoinColumn(name = "MODEL_ID"))
//	private List<CM_ModelConfiguration> modelConfigurations = new ArrayList<>();
//
//	@ElementCollection
//	@MapKeyColumn(name = "CLUE_NUMBER")
//	@Column(name = "CLUE_NAME")
//	@CollectionTable(name = "CMT_MODEL_CLUE", joinColumns = @JoinColumn(
//			name = "MODEL_ID"))
//	private Map<Integer, String> clueNames = new HashMap<>();
//
//	@ElementCollection
//	@MapKeyColumn(name = "NAME")
//	@Column(name = "VALUE")
//	@CollectionTable(name = "CMT_MODEL_PROPERTY", joinColumns = @JoinColumn(
//			name = "MODEL_ID"))
//	private Map<String, String> modelProperties = new HashMap<>();

	// -- Construction

	protected CM_ModelBean() {
	}

	public CM_ModelBean(ImmutableProbabilityModel ipm) {
		if (ipm == null) {
			throw new IllegalArgumentException("null model");
		}
		this.modelName = ipm.getModelName();
		this.modelSignature = ipm.getModelSignature();
		this.evaluatorSignature = ipm.getEvaluatorSignature();
		this.cluesetName = ipm.getClueSetName();
		this.cluesetSignature = ipm.getClueSetSignature();
		this.schemaName = ipm.getSchemaName();
		this.schemaSignature = ipm.getSchemaSignature();
		this.setNotes(createDefaultNotes(ipm));
		
//		final Accessor a = ipm.getAccessor();
//		DbAccessor dba = (DbAccessor) a;
//		String[] db
//		String dbConf = ipm.getA
	}

	// -- Accessors

	public long getId() {
		return id;
	}

	public String getCluesetName() {
		return cluesetName;
	}

	public String getCluesetSignature() {
		return cluesetSignature;
	}

	public String getEvaluatorSignature() {
		return evaluatorSignature;
	}

	public String getModelSignature() {
		return modelSignature;
	}

	public String getModelName() {
		return modelName;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public String getSchemaSignature() {
		return schemaSignature;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		if (id == 0) {
			result = hashCode0();
		} else {
			result = prime * result + (int) (id ^ (id >>> 32));
		}
		return result;
	}

	public int hashCode0() {
		final int prime = 31;
		int result = 1;
		result =
			prime * result
					+ ((cluesetName == null) ? 0 : cluesetName.hashCode());
		result =
			prime
					* result
					+ ((cluesetSignature == null) ? 0 : cluesetSignature
							.hashCode());
		result =
			prime
					* result
					+ ((evaluatorSignature == null) ? 0 : evaluatorSignature
							.hashCode());
		result =
			prime * result + ((modelName == null) ? 0 : modelName.hashCode());
		result =
			prime
					* result
					+ ((modelSignature == null) ? 0 : modelSignature.hashCode());
		result =
			prime * result + ((schemaName == null) ? 0 : schemaName.hashCode());
		result =
			prime
					* result
					+ ((schemaSignature == null) ? 0 : schemaSignature
							.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CM_ModelBean other = (CM_ModelBean) obj;
		if (id != other.id) {
			return false;
		}
		if (id == 0) {
			return equals0(other);
		}
		return true;
	}

	public boolean equals0(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CM_ModelBean other = (CM_ModelBean) obj;
		if (cluesetName == null) {
			if (other.cluesetName != null) {
				return false;
			}
		} else if (!cluesetName.equals(other.cluesetName)) {
			return false;
		}
		if (cluesetSignature == null) {
			if (other.cluesetSignature != null) {
				return false;
			}
		} else if (!cluesetSignature.equals(other.cluesetSignature)) {
			return false;
		}
		if (evaluatorSignature == null) {
			if (other.evaluatorSignature != null) {
				return false;
			}
		} else if (!evaluatorSignature.equals(other.evaluatorSignature)) {
			return false;
		}
		if (modelName == null) {
			if (other.modelName != null) {
				return false;
			}
		} else if (!modelName.equals(other.modelName)) {
			return false;
		}
		if (modelSignature == null) {
			if (other.modelSignature != null) {
				return false;
			}
		} else if (!modelSignature.equals(other.modelSignature)) {
			return false;
		}
		if (schemaName == null) {
			if (other.schemaName != null) {
				return false;
			}
		} else if (!schemaName.equals(other.schemaName)) {
			return false;
		}
		if (schemaSignature == null) {
			if (other.schemaSignature != null) {
				return false;
			}
		} else if (!schemaSignature.equals(other.schemaSignature)) {
			return false;
		}
		return true;
	}

//	public List<CM_ModelConfiguration> getModelConfigurations() {
//		return modelConfigurations;
//	}
//
//	public Map<Integer, String> getClueNames() {
//		return clueNames;
//	}
//
//	public Map<String, String> getModelProperties() {
//		return modelProperties;
//	}
//
}
