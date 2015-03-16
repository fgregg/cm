package com.choicemaker.cm.transitivity.server.ejb;

import javax.ejb.Local;

import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;

/**
 * Manages a database of ABA and OABA settings.
 * 
 * @author rphall
 *
 */
@Local
public interface TransitivitySettingsController extends OabaSettingsController {

	OabaSettings findSettingsByTransitivityJobId(long jobId);

}
