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
package com.choicemaker.cm.urm.ejb;

import java.io.Serializable;

import com.choicemaker.cm.urm.config.AnalysisResultFormat;

/**
 * @author emoussikaev
 *
 */
public class TransSerializeData implements Serializable {

	static final long serialVersionUID = 271;

	public final String externalId;
	public final long transId;
	public final long batchId;
	public final long ownId;
	public final String groupMatchType;
	public final AnalysisResultFormat serializationType;

	public TransSerializeData(String externalId, long transId, long batchId,
			long ownId, String groupMatchType, String serializationType) {
		this(externalId, transId, batchId, ownId, groupMatchType,
				AnalysisResultFormat.valueOf(serializationType));
	}

	public TransSerializeData(String externalId, long transId, long batchId,
			long ownId, String groupMatchType,
			AnalysisResultFormat serializationType) {
		if (externalId == null || !externalId.equals(externalId.trim())
				|| externalId.isEmpty()) {
			String msg = "Invalid external id: '" + externalId + "'";
			throw new IllegalArgumentException(msg);
		}
		if (groupMatchType == null
				|| !groupMatchType.equals(groupMatchType.trim())
				|| groupMatchType.isEmpty()) {
			String msg = "Invalid group match type: '" + groupMatchType + "'";
			throw new IllegalArgumentException(msg);
		}
		if (serializationType == null) {
			String msg =
				"Invalid serialization type: '" + serializationType + "'";
			throw new IllegalArgumentException(msg);
		}
		this.externalId = externalId;
		this.transId = transId;
		this.batchId = batchId;
		this.ownId = ownId;
		this.groupMatchType = groupMatchType;
		this.serializationType = serializationType;
	}

}
