package com.choicemaker.cmit.oaba;

import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

public class OabaIT extends AbstractJeeTestClass {

	@Test
	public void testTrivialTrue() {
		System.out.println("testTrivialTrue: TRIVIALLY VALID TEST");
		assertTrue(true);
	}

}

