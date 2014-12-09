package com.choicemaker.cm.transitivity.server.impl;

import com.choicemaker.cm.args.TransitivitySettings;
import com.choicemaker.cm.core.SerializableRecordSource;

public class TransitivitySettingsEntity implements TransitivitySettings {

	private static final long serialVersionUID = 271L;

	public TransitivitySettingsEntity(
			String createRandomModelConfigurationName, float differThreshold,
			float matchThreshold, SerializableRecordSource stage,
			SerializableRecordSource master) {
		throw new Error("not yet implemented");
	}

	@Override
	public int getMaxBlockSize() {
		throw new Error("not yet implemented");
	}

	@Override
	public int getMaxChunkSize() {
		throw new Error("not yet implemented");
	}

	@Override
	public int getMaxMatches() {
		throw new Error("not yet implemented");
	}

	@Override
	public int getMaxOversized() {
		throw new Error("not yet implemented");
	}

	@Override
	public int getMinFields() {
		throw new Error("not yet implemented");
	}

	@Override
	public int getInterval() {
		throw new Error("not yet implemented");
	}

	@Override
	public int getMaxSingle() {
		throw new Error("not yet implemented");
	}

	@Override
	public long getId() {
		throw new Error("not yet implemented");
	}

	@Override
	public int getLimitPerBlockingSet() {
		throw new Error("not yet implemented");
	}

	@Override
	public int getLimitSingleBlockingSet() {
		throw new Error("not yet implemented");
	}

	@Override
	public int getSingleTableBlockingSetGraceLimit() {
		throw new Error("not yet implemented");
	}

}
