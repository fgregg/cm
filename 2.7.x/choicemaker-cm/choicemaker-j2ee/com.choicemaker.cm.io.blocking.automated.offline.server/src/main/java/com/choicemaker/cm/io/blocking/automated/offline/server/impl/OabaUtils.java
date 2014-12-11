package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.SettingsController;

public class OabaUtils {

	private OabaUtils() {
	}

	/**
	 * Looks up or computes default OABA settings for the specified model.
	 * (If default settings are computed, they are likely to be less than
	 * optimal.)
	 * @param settingsController a non-null settings controller.
	 * @param modelId a valid model name or configuration identifier.
	 * @return
	 */
	public static OabaSettings getDefaultOabaSettings(
			SettingsController settingsController, String modelId) {
		if (settingsController == null) {
			throw new IllegalArgumentException("null controller");
		}
		if (modelId == null || !modelId.equals(modelId.trim())
				|| modelId.isEmpty()) {
			throw new IllegalArgumentException("invalid model '" + modelId
					+ "'");
		}
	
		// Get the default OABA settings, ignoring the maxSingle value.
		// If no default settings exist, create them using the maxSingle value.
		ImmutableProbabilityModel model =
			PMManager.getImmutableModelInstance(modelId);
		OabaSettings retVal = settingsController.findDefaultOabaSettings(model);
		if (retVal == null) {
			// Creates generic settings and saves them
			retVal = new OabaSettingsEntity();
			retVal = settingsController.save(retVal);
		}
		return retVal;
	}

	/**
	 * Looks up or computes the default OABA server configuration <em><strong>
	 * for the host on which this method is invoked</strong></em>. In other
	 * words, this is a server-side utility, not a client-side utility.
	 * @param serverController a non-null manager of server configurations
	 * @return
	 */
	public static ServerConfiguration getDefaultServerConfiguration(
			ServerConfigurationController serverController) {
		if (serverController == null) {
			throw new IllegalArgumentException("null controller");
		}
	
		// Get the default server configuration for this host (or guess at it)
		final String hostName =
			ServerConfigurationControllerBean.computeHostName();
		final boolean computeFallback = true;
		ServerConfiguration retVal =
			serverController.getDefaultConfiguration(hostName, computeFallback);
		return retVal;
	}

}
