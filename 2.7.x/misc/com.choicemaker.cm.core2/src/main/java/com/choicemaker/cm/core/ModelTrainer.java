package com.choicemaker.cm.core;

import java.util.Collection;

public interface ModelTrainer {

	ImmutableProbabilityModel trainProbabilityModel(ImmutableProbabilityModel model, Collection source) throws ModelTrainingException;

	PairEvaluator trainPairEvaluator(ImmutableProbabilityModel model, Collection source) throws ModelTrainingException;

}