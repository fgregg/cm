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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyTemporal;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.TemporalType;

import com.choicemaker.cm.core.ClueDesc;
import com.choicemaker.cm.core.ClueSet;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.MachineLearner;
import com.choicemaker.cm.ml.me.base.MaximumEntropy;
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

	private static final Logger log = Logger.getLogger(CM_ModelBean.class
			.getName());

	/** Default value for non-persistent models */
	public static final int INVALID_MODEL_ID = 0;

	public static final String DATE_FORMAT_SPEC = "yyyy-MM-dd HH:mm";

	private static final String DEFAULT_MACHINE_LEARNING_CLASS_NAME =
		"com.choicemaker.cm.ml.me.base.MaximumEntropy";

	/**
	 * An abbreviation representing the
	 * {@link com.choicemaker.cm.ml.me.base.MaximumEntropy default machine
	 * learning technique}
	 * 
	 */
	public static final String DEFAULT_MACHINE_LEARNING_TYPE = "CMME";

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

	public static String createDefaultNote(ImmutableProbabilityModel ipm) {
		String modelName = ipm.getModelName();
		String user = System.getProperty(SystemPropertyUtils.USER_NAME);
		DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_SPEC);
		String timeStamp = dateFormat.format(new Date());
		String retVal =
			"Persisted from '" + modelName + "' by '" + user + "' ("
					+ timeStamp + ")";
		return retVal;
	}

	public static String getMachineLearningTypeName(MachineLearner ml) {
		String retVal = null;
		if (ml != null) {
			Class<?> mlClass = ml.getClass();
			String mlClassName = mlClass.getName();
			;
			if (DEFAULT_MACHINE_LEARNING_CLASS_NAME.equals(mlClassName)) {
				retVal = DEFAULT_MACHINE_LEARNING_TYPE;
			} else {
				retVal = mlClass.getSimpleName();
			}
		}
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

	@Column(name = "ML_TYPE")
	private String machineLearning;

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

	@ElementCollection
	@MapKeyColumn(name = "TIMESTAMP")
	@MapKeyTemporal(TemporalType.TIMESTAMP)
	@Column(name = "NOTE")
	@CollectionTable(name = "CM_MDB_MODEL_NOTE", joinColumns = @JoinColumn(
			name = "MODEL_ID"))
	private Map<Date, String> notes = new HashMap<>();

	 @ElementCollection
	 @MapKeyColumn(name = "CLUE_NUMBER")
	 @CollectionTable(name = "CM_MDB_MODEL_FEATURE", joinColumns = @JoinColumn(
	 name = "MODEL_ID"))
	 private Map<Integer, CM_Feature> features = new HashMap<>();
	
	 @ElementCollection
	 @MapKeyColumn(name = "NAME")
	 @Column(name = "VALUE")
	 @CollectionTable(name = "CM_MDB_MODEL_PROPERTY", joinColumns = @JoinColumn(
	 name = "MODEL_ID"))
	 private Map<String, String> configurationProperties = new HashMap<>();

	// -- Construction

	protected CM_ModelBean() {
	}

	public CM_ModelBean(ImmutableProbabilityModel ipm) {
		if (ipm == null) {
			throw new IllegalArgumentException("null model");
		}
		this.modelName = ipm.getModelName();
		this.modelSignature = ipm.getModelSignature();
		final MachineLearner ml = ipm.getMachineLearner();
		this.machineLearning = CM_ModelBean.getMachineLearningTypeName(ml);
		this.evaluatorSignature = ipm.getEvaluatorSignature();
		this.cluesetName = ipm.getClueSetName();
		this.cluesetSignature = ipm.getClueSetSignature();
		this.schemaName = ipm.getSchemaName();
		this.schemaSignature = ipm.getSchemaSignature();
		this.addNote(createDefaultNote(ipm));

		ClueSet cs = ipm.getClueSet();
		if (cs != null) {
			ClueDesc[] clueDescriptors = cs.getClueDesc();
			float[] weights = null;
			if (ml instanceof MaximumEntropy) {
				weights = ((MaximumEntropy) ml).getWeights();
				assert weights.length == clueDescriptors.length;
			}
			for (int i=0; i<clueDescriptors.length; i++) {
				CM_Feature cmf = new CM_Feature(this.machineLearning, clueDescriptors,
			weights, i);
				this.features.put(i,cmf);
			}
		}
		
		@SuppressWarnings("unchecked")
		Map<String,String> properties = ipm.properties();
		if (properties != null) {
			for (Map.Entry<String,String> e : properties.entrySet()) {
				this.configurationProperties.put(e.getKey(), e.getValue());
			}
		}
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

	/**
	 * Returns a type indicator, currently the simple class name of the machine
	 * learner used by a model. This column is used as a table discriminator so
	 * that additional machine learning implementations can be accommodated in
	 * the future, besides the Maximum Entropy machine learning currently
	 * implemented by {@link com.choicemaker.cm.ml.me.base.MaximumEntropy}.
	 */
	public String getMachineLearning() {
		return machineLearning;
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

	public Map<Date, String> getNotes() {
		return Collections.unmodifiableMap(notes);
	}

	public void addNote(String note) {
		if (note == null) {
			log.warning("Skipping null note");
			return;
		}
		note = note.trim();
		if (note.isEmpty()) {
			log.warning("Skipping blank note");
			return;
		}
		final Date now0 = new Date();
		Date now = now0;
		String existing = this.notes.get(now);

		// Weird corner case -- entry already exists
		final int MAX_ATTEMPTS = 1000;
		int count = 0;
		while (existing != null && count < MAX_ATTEMPTS) {
			// Hack: add a millisecond and try again
			now = new Date(now.getTime() + 1);
			existing = this.notes.get(now);
		}
		if (existing == null) {
			this.notes.put(now, note);
		} else {
			String msg =
				"Notes already exist: " + now0 + " to " + now
						+ "; ignoring new note: '" + note + "'";
			log.warning(msg);
		}
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

	// public List<CM_ModelConfiguration> getModelConfigurations() {
	// return modelConfigurations;
	// }
	//
	// public Map<Integer, String> getClueNames() {
	// return features;
	// }
	//
	// public Map<String, String> getModelProperties() {
	// return modelProperties;
	// }
	//
}
