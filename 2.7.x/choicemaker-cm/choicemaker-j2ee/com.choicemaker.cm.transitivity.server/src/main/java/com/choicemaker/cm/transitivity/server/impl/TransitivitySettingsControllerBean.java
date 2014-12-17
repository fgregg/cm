package com.choicemaker.cm.transitivity.server.impl;

import javax.ejb.Stateless;

import com.choicemaker.cm.args.TransitivitySettings;
import com.choicemaker.cm.transitivity.server.ejb.TransitivitySettingsController;

@Stateless
public class TransitivitySettingsControllerBean implements
		TransitivitySettingsController {

	public TransitivitySettingsControllerBean() {
	}

	@Override
	public void save(TransitivitySettings settings) {
		// TODO Auto-generated method stub
		throw new Error("not yet implemented");
	}

}
