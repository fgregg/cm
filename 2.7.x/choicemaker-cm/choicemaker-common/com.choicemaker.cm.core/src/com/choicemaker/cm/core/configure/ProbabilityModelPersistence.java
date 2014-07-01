package com.choicemaker.cm.core.configure;

import java.io.InputStream;
import java.io.Writer;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.XmlConfException;

public interface ProbabilityModelPersistence {

	void saveModel(ImmutableProbabilityModel model) throws XmlConfException;

	ImmutableProbabilityModel readModel(String fileName, Compiler compiler, Writer w)
			throws XmlConfException;

	ImmutableProbabilityModel readModel(String fileName, InputStream is,
			Compiler compiler, Writer statusOutput) throws XmlConfException;

	ImmutableProbabilityModel readModel(String fileName, InputStream is,
			Compiler compiler, Writer statusOutput,
			ClassLoader customClassLoader) throws XmlConfException;

	void loadProductionProbabilityModels(Compiler compiler, boolean fromResource)
			throws XmlConfException;

	void loadProductionProbabilityModels(Compiler compiler)
			throws XmlConfException;

}