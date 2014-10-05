package com.choicemaker.demo.simple_person_matching;

import static com.choicemaker.cm.core.PropertyNames.INSTALLABLE_CHOICEMAKER_CONFIGURATOR;

import org.junit.BeforeClass;
import org.junit.Test;

import com.choicemaker.cm.core.xmlconf.XmlConfigurator;
import com.choicemaker.e2.embed.EmbeddedPlatform;

public class SimplePersonPluginTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		EmbeddedPlatform.install();
		String pn = INSTALLABLE_CHOICEMAKER_CONFIGURATOR;
		String pv = XmlConfigurator.class.getName();
		System.setProperty(pn, pv);
	}
	
	@Test
	public void testLoadSimplePersonModels() {
		SimplePersonPluginTesting.testLoadSimplePersonModels();
	}
	
	@Test
	public void testSimplePersonPluginExtensions() {
		SimplePersonPluginTesting.testSimplePersonPluginExtensions();
	}

}
