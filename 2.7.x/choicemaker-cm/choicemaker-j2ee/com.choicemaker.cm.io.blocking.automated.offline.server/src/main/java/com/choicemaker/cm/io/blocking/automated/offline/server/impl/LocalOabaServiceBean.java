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

import java.util.logging.Logger;

import javax.ejb.Stateless;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
//import javax.naming.InitialContext;

/**
 * @author pcheung
 *
 */
@Stateless
public class LocalOabaServiceBean extends OabaServiceBean implements LocalOabaService {

	private static final long serialVersionUID = 271L;

	private static final String SOURCE_CLASS = LocalOabaServiceBean.class
			.getSimpleName();

	private static final Logger logger = Logger
			.getLogger(LocalOabaServiceBean.class.getName());

	@Override
	public long startLocalDeduplication(String externalID,
			OabaParametersEntity batchParams, OabaSettingsEntity oabaSettings,
			ServerConfigurationEntity serverConfiguration)
			throws ServerConfigurationException {
		final String METHOD = "startLocalDeduplication";
		logger.entering(SOURCE_CLASS,METHOD);
		return startDeduplication(externalID, (OabaParameters) batchParams,
				(OabaSettings) oabaSettings,
				(ServerConfiguration) serverConfiguration);
	}

	@Override
	public long startLocalLinkage(String externalID,
			OabaParametersEntity batchParams, OabaSettingsEntity oabaSettings,
			ServerConfigurationEntity serverConfiguration)
			throws ServerConfigurationException {
		final String METHOD = "startLocalLinkage";
		logger.entering(SOURCE_CLASS,METHOD);
		return startLinkage(externalID, (OabaParameters) batchParams,
				(OabaSettings) oabaSettings,
				(ServerConfiguration) serverConfiguration);
	}

}
