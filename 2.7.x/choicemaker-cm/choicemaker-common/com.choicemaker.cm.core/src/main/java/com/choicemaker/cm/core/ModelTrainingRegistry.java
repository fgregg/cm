package com.choicemaker.cm.core;

public interface ModelTrainingRegistry {

	String INSTANCE = "instance";

	ModelTrainer getModelTrainer(ImmutableProbabilityModel model);

	MachineLearner getMachineLearning(ImmutableProbabilityModel model);

	void register(ImmutableProbabilityModel model, MachineLearner ml);

	PairEvaluator getPairEvaluator(ImmutableProbabilityModel model);

	void register(ImmutableProbabilityModel model, PairEvaluator pe);

}
