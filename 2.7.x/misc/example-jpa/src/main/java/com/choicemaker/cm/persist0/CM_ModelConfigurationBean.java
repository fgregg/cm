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
package com.choicemaker.cm.persist0;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyTemporal;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TemporalType;

/**
 * Persistent information about a probability model. This information is not
 * sufficient to reproduce a matching model, but it is sufficient to check
 * whether model is the same as some previously saved model.
 * 
 * @author rphall
 *
 */
@NamedQuery(name = "modelConfigFindAll",
		query = "Select config from CM_ModelConfigurationBean config")
@Entity
@Table(/* schema = "CHOICEMAKER", */name = "CMP_MODEL_CONFIG")
public class CM_ModelConfigurationBean implements Serializable {

	private static final long serialVersionUID = 271L;

	private static final String CLASS_LOG_NAME =
		CM_ModelConfigurationBean.class.getSimpleName();

	private static final Logger log = Logger
			.getLogger(CM_ModelConfigurationBean.class.getName());

	public static enum NamedQuery {
		FIND_ALL("modelConfigFindAll");
		public final String name;

		NamedQuery(String name) {
			this.name = name;
		}
	}

	private static class NameValue {
		private final String name;
		private final String value;

		NameValue(String name, String value) {
			this.name = name;
			this.value = value;
		}

		public String toString() {
			return "[" + name + ":" + value + "]";
		}
	}

	public static String validateAndStandardizePropertyName(final String name) {
		if (name == null) {
			throw new IllegalArgumentException("null argument");
		}
		String retVal = name.trim().toUpperCase();
		if (retVal.isEmpty()) {
			throw new IllegalArgumentException("blank name");
		}
		return retVal;
	}
	
	// -- Instance data

	@Id
	private CM_ModelConfigurationPK id;

	@ElementCollection
	@MapKeyColumn(name = "NAME")
	@Column(name = "VALUE")
	@CollectionTable(name = "CM_MDB_MODEL_CONFIG_PROPERTY",
			joinColumns = @JoinColumn(name = "MODEL_CONFIG"))
	private Map<String, String> configurationProperties = new HashMap<>();

	@ElementCollection
	@MapKeyColumn(name = "TIMESTAMP")
	@MapKeyTemporal(TemporalType.TIMESTAMP)
	@Column(name = "NOTE")
	@CollectionTable(name = "CM_MDB_MODEL_CONFIG_AUDIT", joinColumns = @JoinColumn(
			name = "MODEL_ID"))
	private Map<Date, String> audit = new HashMap<>();

	// -- Construction

	protected CM_ModelConfigurationBean() {
	}

	public CM_ModelConfigurationBean(CM_ModelBean model, String name) {
		this.id = new CM_ModelConfigurationPK(model, name);
	}

	public CM_ModelConfigurationBean(CM_ModelConfigurationPK pk) {
		if (pk == null) {
			throw new IllegalArgumentException("null primary key");
		}
		this.id = pk;
	}

	// -- Accessors

	public CM_ModelConfigurationPK getId() {
		return id;
	}

	public long getModelId() {
		return id.getModelId();
	}

	public String getConfigurationName() {
		return id.getConfigurationName();
	}
	
	public String getProperty(String name) {
		name = validateAndStandardizePropertyName(name);
		String retVal = this.configurationProperties.get(name);
		log.exiting(CLASS_LOG_NAME, "getProperty", retVal);
		return retVal;
	}

	public Map<String, String> getProperties() {
		log.exiting(CLASS_LOG_NAME, "getProperties",
				configurationProperties.size());
		return Collections.unmodifiableMap(configurationProperties);
	}

	// -- Modifiers

	public void addProperty(String name, String value) {
		if (value == null) {
			throw new IllegalArgumentException("null argument");
		}
		name = validateAndStandardizePropertyName(name);
		NameValue newNV = new NameValue(name, value);
		log.entering(CLASS_LOG_NAME, "addProperty", new NameValue(name, value));
		String old = this.configurationProperties.put(name, value);
		if (old != null) {
			NameValue oldNV = new NameValue(name, old);
			log.info(CLASS_LOG_NAME + "addProperty replaced " + oldNV
					+ " with " + newNV);
		}
	}

	/** <strong>Adds</strong> the specified properties to the existing ones */
	public void addProperties(Map<String, String> properties) {
		if (properties == null) {
			throw new IllegalArgumentException("null properties");
		}
		log.entering(CLASS_LOG_NAME, "addProperties", properties.size());
		for (Map.Entry<String, String> e : properties.entrySet()) {
			addProperty(e.getKey(), e.getValue());
		}
	}

	/**
	 * <strong>Replaces</strong> the existing properties with the specified ones
	 */
	public void setProperties(Map<String, String> properties) {
		if (properties == null) {
			throw new IllegalArgumentException("null properties");
		}
		log.entering(CLASS_LOG_NAME, "setProperties", properties.size());
		clearProperties();
		for (Map.Entry<String, String> e : properties.entrySet()) {
			addProperty(e.getKey(), e.getValue());
		}
	}

	public void clearProperty(String name) {
		name = validateAndStandardizePropertyName(name);
		log.entering(CLASS_LOG_NAME, "clearProperty", name);
		this.configurationProperties.remove(name);
	}

	public void clearProperties() {
		log.entering(CLASS_LOG_NAME, "clearProperties",
				configurationProperties.size());
		this.configurationProperties.clear();
	}

	// -- Identity

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result =
			prime
					* result
					+ ((configurationProperties == null) ? 0
							: configurationProperties.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		CM_ModelConfigurationBean other = (CM_ModelConfigurationBean) obj;
		if (configurationProperties == null) {
			if (other.configurationProperties != null) {
				return false;
			}
		} else if (!configurationProperties
				.equals(other.configurationProperties)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "CM_ModelConfigurationBean [id=" + id
				+ ", configurationProperties=" + configurationProperties + "]";
	}
	
}
