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
package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import javax.ejb.Local;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;

/**
 * @author pcheung
 *
 */
@Local
public interface LocalOabaService extends OabaService {

	public long startLocalDeduplication(String externalID, OabaParametersEntity batchParams,
			OabaSettingsEntity oabaSettings, ServerConfigurationEntity serverConfiguration)
			throws ServerConfigurationException;

	public long startLocalLinkage(String externalID, OabaParametersEntity batchParams,
			OabaSettingsEntity oabaSettings, ServerConfigurationEntity serverConfiguration)
			throws ServerConfigurationException;

}
