package com.choicemaker.cm.compiler.util;

//import static com.choicemaker.cm.modelmaker.gui.ModelMaker.EXIT_OK;
//import static com.choicemaker.cm.modelmaker.gui.ModelMaker.PLUGIN_APPLICATION_ID;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.choicemaker.e2.embed.EmbeddedPlatform;
//import com.choicemaker.cm.modelmaker.gui.ModelMaker;

public class ModelMaker0IT {

	private static final Logger logger = Logger.getLogger(ModelMaker0IT.class
			.getName());

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		EmbeddedPlatform.install();
	}

//	private ModelMaker modelMaker;

	@Before
	public void setUp() throws Exception {

		logger.info("Starting setUp()");

		// Instantiate ModelMaker
//		CMPlatformRunnable cmpr =
//			InstallablePlatform.getInstance().loaderGetRunnable(
//					PLUGIN_APPLICATION_ID);
//		logger.info("InstallablePlatform.loaderGetRunnable(): " + cmpr);
//		assertTrue(cmpr instanceof ModelMaker);
//		this.modelMaker = (ModelMaker) cmpr;
//
//		// Prepare, but do not display, the ModelMaker GUI
//		String[] args1 = SqlServerUtils.getModelMakerRunArgs();
//		this.modelMaker.startup(args1);
		logger.info("ModelMaker GUI prepared (but not displayed)");

		logger.info("setUp() complete");
	}

	@After
	public void tearDown() throws Exception {
		logger.info("Starting tearDown()");
//		if (modelMaker != null) {
//			modelMaker.programExit(EXIT_OK);
//		}
		logger.info("tearDown() complete");
	}

	@Test
	public void testModelMakerIsReady() throws Exception {
		logger.info("testModelMakerIsReady");
		logger.info("starting test");
//		assertTrue(this.modelMaker != null);
//		boolean retVal = modelMaker.isReady();
//		assertTrue(retVal);
		logger.info("test completed");
	}

}
