package com.choicemaker.cm.core.gen;

import org.apache.log4j.Logger;

public class FakeGeneratorPlugin implements GeneratorPlugin {

	private static final Logger logger = Logger.getLogger(FakeGeneratorPlugin.class.getName());

	public void generate(IGenerator g) throws GenException {
		logger.info("generate invoked with argument: '" + g + "'");
	}

}
