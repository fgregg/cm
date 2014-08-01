package com.choicemaker.cm.core;

public interface ProbabilityModelSpecification {

	/** A path to the model weights file (*.model) */
	String getWeightFilePath();

	/**
	 * A relative path from the model weights file (*.model) to the clue set
	 * file (*.clues)
	 */
	String getClueFilePath();

	MachineLearner getMachineLearner();

}