package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import javax.ejb.Local;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.core.DatabaseException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.io.blocking.automated.AbaStatistics;
import com.choicemaker.cm.io.blocking.automated.AbaStatisticsCache;

@Local
public interface AbaStatisticsController extends AbaStatisticsCache {

	void updateReferenceStatistics(OabaParameters params)
			throws DatabaseException;

	void updateReferenceStatistics(String urlString) throws DatabaseException;

	void putStatistics(ImmutableProbabilityModel model, AbaStatistics counts);

	AbaStatistics getStatistics(ImmutableProbabilityModel model);

}