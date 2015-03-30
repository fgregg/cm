package com.choicemaker.cm.io.blocking.automated;

import com.choicemaker.cm.core.ImmutableProbabilityModel;

public interface AbaStatisticsCache {

	public void putStatistics(ImmutableProbabilityModel model,
			AbaStatistics counts);

	public AbaStatistics getStatistics(ImmutableProbabilityModel model);

}
