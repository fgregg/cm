package com.choicemaker.cm.transitivity.server.ejb;

import javax.ejb.Local;

import com.choicemaker.cm.args.TransitivitySettings;

@Local
public interface TransitivitySettingsController {

	void save(TransitivitySettings settings);

}
