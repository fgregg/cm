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
package com.choicemaker.demo.persist0;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Composite primary key for model configuration instances.
 * 
 * @author rphall
 */
@Embeddable
public class CMP_ModelConfigurationPK implements Serializable {

	private static final long serialVersionUID = 271L;

	@Column(name = "MODEL_ID")
	private long modelId;

	@Column(name = "CONFIG_NAME")
	private String configurationName;

	// -- Construction

	protected CMP_ModelConfigurationPK() {
	}

	public CMP_ModelConfigurationPK(CMP_ModelBean model, String name) {
		if (model == null) {
			throw new IllegalArgumentException("null model");
		}
		if (!CMP_ModelBean.isNonPersistent(model)) {
			throw new IllegalArgumentException("non-persistent model");
		}
		if (name == null) {
			throw new IllegalArgumentException("null name");
		}
		name = name.trim();
		if (name.isEmpty()) {
			throw new IllegalArgumentException("blank name");
		}

		this.modelId = model.getId();
		this.configurationName = name;
	}

	// -- Accessors

	public long getModelId() {
		return modelId;
	}

	public String getConfigurationName() {
		return configurationName;
	}

	// -- Identity

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result =
			prime
					* result
					+ ((configurationName == null) ? 0 : configurationName
							.hashCode());
		result = prime * result + (int) (modelId ^ (modelId >>> 32));
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
		CMP_ModelConfigurationPK other = (CMP_ModelConfigurationPK) obj;
		if (configurationName == null) {
			if (other.configurationName != null) {
				return false;
			}
		} else if (!configurationName.equals(other.configurationName)) {
			return false;
		}
		if (modelId != other.modelId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "CMP_ModelConfigurationPK [" + modelId + "/" + configurationName
				+ "]";
	}

}
