package com.choicemaker.cm.io.blocking.automated.offline.core;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RECORD_ID_TYPE_Test {

	@Test
	public void testIntSymbol() {
		for (RECORD_ID_TYPE rit : RECORD_ID_TYPE.values()) {
			int i = rit.getIntValue();
			RECORD_ID_TYPE rit2 = RECORD_ID_TYPE.fromValue(i);
			assertTrue(rit == rit2);
		}
	}

	@Test
	public void testCharSymbol() {
		for (RECORD_ID_TYPE rit : RECORD_ID_TYPE.values()) {
			char c = rit.getCharSymbol();
			RECORD_ID_TYPE rit2 = RECORD_ID_TYPE.fromSymbol(c);
			assertTrue(rit == rit2);
		}
	}

	@Test
	public void testStringSymbol() {
		for (RECORD_ID_TYPE rit : RECORD_ID_TYPE.values()) {
			String s = rit.getStringSymbol();
			RECORD_ID_TYPE rit2 = RECORD_ID_TYPE.fromSymbol(s);
			assertTrue(rit == rit2);
		}
	}

	@Test
	public void testRecordIdClass() {
		for (RECORD_ID_TYPE rit : RECORD_ID_TYPE.values()) {
			Class<?> c = rit.getRecordIdClass();
			RECORD_ID_TYPE rit2 = RECORD_ID_TYPE.fromClass(c);
			assertTrue(rit == rit2);
		}
	}

	@Test
	public void testFromInstance() {
		Integer i= Integer.valueOf(0);
		RECORD_ID_TYPE ritI = RECORD_ID_TYPE.fromInstance(i);
		assertTrue(ritI.getRecordIdClass().equals(i.getClass()));
		
		Long l = Long.valueOf(0);
		RECORD_ID_TYPE ritL = RECORD_ID_TYPE.fromInstance(l);
		assertTrue(ritL.getRecordIdClass().equals(l.getClass()));
		
		String s = String.valueOf(0);
		RECORD_ID_TYPE ritS = RECORD_ID_TYPE.fromInstance(s);
		assertTrue(ritS.getRecordIdClass().equals(s.getClass()));
	}

}
