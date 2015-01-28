package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;

public class RecordIdSinkTest {

	public static final String SINK_PREFIX = "Sink";
	public static final String FILE_EXTENTION = ".test";

	@Test
	public void testIntegerFileData() throws IOException, BlockingException {
		String fileName = RecordIdTestUtils.createTempFileName();
		List<Integer> identifiers = RecordIdTestUtils.getIdentifiersAsIntegers();
		RecordIdTestUtils.writeIdentifiersToFile(fileName, identifiers);

		File f = new File(fileName);
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			List<String> expected = RecordIdTestUtils.getFileData(identifiers);
			for (String s : expected) {
				String computed = br.readLine();
				assertTrue(computed != null);
				assertTrue(s.equals(computed));
			}
			assertTrue(br.readLine() == null);
		}
	}

	@Test
	public void testLongFileData() throws IOException, BlockingException {
		String fileName = RecordIdTestUtils.createTempFileName();
		List<Long> identifiers = RecordIdTestUtils.getIdentifiersAsLongs();
		RecordIdTestUtils.writeIdentifiersToFile(fileName, identifiers);

		File f = new File(fileName);
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			List<String> expected = RecordIdTestUtils.getFileData(identifiers);
			for (String s : expected) {
				String computed = br.readLine();
				assertTrue(computed != null);
				assertTrue(s.equals(computed));
			}
			assertTrue(br.readLine() == null);
		}
	}

	@Test
	public void testStringFileData() throws IOException, BlockingException {
		String fileName = RecordIdTestUtils.createTempFileName();
		List<String> identifiers = RecordIdTestUtils.getIdentifiersAsStrings();
		RecordIdTestUtils.writeIdentifiersToFile(fileName, identifiers);

		File f = new File(fileName);
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			List<String> expected = RecordIdTestUtils.getFileData(identifiers);
			for (String s : expected) {
				String computed = br.readLine();
				assertTrue(computed != null);
				assertTrue(s.equals(computed));
			}
			assertTrue(br.readLine() == null);
		}
	}

	@Test
	public void testGetRecordIdType() throws IOException, BlockingException {

		String fileName = RecordIdTestUtils.createTempFileName();
		List<String> identifiers = RecordIdTestUtils.getIdentifiersAsStrings();
		RecordIdSink ris = RecordIdTestUtils.writeIdentifiersToFile(fileName, identifiers);
		assertTrue(ris.getRecordIdType() == RECORD_ID_TYPE.TYPE_STRING);

		fileName = RecordIdTestUtils.createTempFileName();
		List<Long> identifiers2 = RecordIdTestUtils.getIdentifiersAsLongs();
		RecordIdSink ris2 = RecordIdTestUtils.writeIdentifiersToFile(fileName, identifiers2);
		assertTrue(ris2.getRecordIdType() == RECORD_ID_TYPE.TYPE_LONG);

		fileName = RecordIdTestUtils.createTempFileName();
		List<Integer> identifiers3 = RecordIdTestUtils.getIdentifiersAsIntegers();
		RecordIdSink ris3 = RecordIdTestUtils.writeIdentifiersToFile(fileName, identifiers3);
		assertTrue(ris3.getRecordIdType() == RECORD_ID_TYPE.TYPE_INTEGER);
	}

	@Test
	public void testExistsCount() throws IOException, BlockingException {
		String fileName = RecordIdTestUtils.createTempFileName();
		RecordIdSink ris = new RecordIdSink(fileName);
		assertTrue(ris.getCount() == 0);
		assertTrue(ris.exists() == false);

		ris.open();
		assertTrue(ris.exists());

		List<String> identifiers = RecordIdTestUtils.getIdentifiersAsStrings();
		int count = 0;
		for (String id : identifiers) {
			ris.writeRecordID(id);
			++count;
			assertTrue(ris.getCount() == count);
			assertTrue(ris.exists());
		}
		ris.close();
		assertTrue(ris.getCount() == count);
		assertTrue(ris.exists());

		ris.remove();
		assertTrue(ris.getCount() == 0);
		assertTrue(ris.exists() == false);
	}

	@Test
	public void testAppend() throws IOException, BlockingException {
		String fileName = RecordIdTestUtils.createTempFileName();
		RecordIdSink ris = new RecordIdSink(fileName);
		List<String> identifiers = RecordIdTestUtils.getIdentifiersAsStrings();
		assertTrue(identifiers.size() > 2);

		ris.open();
		int count = 0;
		String id = identifiers.get(count);
		ris.writeRecordID(id);
		++count;
		ris.close();

		while (count < identifiers.size()) {
			id = identifiers.get(count);
			ris.append();
			ris.writeRecordID(id);
			++count;
			assertTrue(ris.getCount() == count);
			ris.close();
		}

		count = 0;
		for (String id2 : identifiers) {
			try {
				assertTrue(ris.isOpen() == false);
				ris.writeRecordID(id2);
				fail("Sink is closed -- writing new ids is not allowed");
			} catch (IllegalStateException x) {
				// Expected
			} catch (Exception x) {
				fail("Unexpected exception: " + x.toString());
			}
			
			// Once is enough
			++count;
			if (count > 0) {
				break;
			}
		}
	}

	@Test
	public void testCompatibleSource() throws IOException, BlockingException {

		String fileName = RecordIdTestUtils.createTempFileName();
		List<String> identifiers = RecordIdTestUtils.getIdentifiersAsStrings();
		RecordIdSink ris = RecordIdTestUtils.writeIdentifiersToFile(fileName, identifiers);
		assertTrue(ris.getRecordIdType() == RECORD_ID_TYPE.TYPE_STRING);
		RecordIdSource<String> src =
			new RecordIdSource<>(String.class, fileName);
		src.open();
		for (String id : identifiers) {
			assertTrue(src.hasNext());
			String id2 = src.next();
			assertTrue(id2 != null);
			assertTrue(id2.equals(id));
		}
		assertTrue(src.hasNext() == false);

		fileName = RecordIdTestUtils.createTempFileName();
		List<Long> identifiers2 = RecordIdTestUtils.getIdentifiersAsLongs();
		RecordIdSink ris2 = RecordIdTestUtils.writeIdentifiersToFile(fileName, identifiers2);
		assertTrue(ris2.getRecordIdType() == RECORD_ID_TYPE.TYPE_LONG);
		RecordIdSource<Long> src2 = new RecordIdSource<>(Long.class, fileName);
		src2.open();
		for (Long id : identifiers2) {
			assertTrue(src2.hasNext());
			Long id2 = src2.next();
			assertTrue(id2 != null);
			assertTrue(id2.equals(id));
		}
		assertTrue(src2.hasNext() == false);

		fileName = RecordIdTestUtils.createTempFileName();
		List<Integer> identifiers3 = RecordIdTestUtils.getIdentifiersAsIntegers();
		RecordIdSink ris3 = RecordIdTestUtils.writeIdentifiersToFile(fileName, identifiers3);
		assertTrue(ris3.getRecordIdType() == RECORD_ID_TYPE.TYPE_INTEGER);
		RecordIdSource<Integer> src3 =
			new RecordIdSource<>(Integer.class, fileName);
		src3.open();
		for (Integer id : identifiers3) {
			assertTrue(src3.hasNext());
			Integer id2 = src3.next();
			assertTrue(id2 != null);
			assertTrue(id2.equals(id));
		}
		assertTrue(src3.hasNext() == false);
	}

	// @Test
	// public void testGetInfo() {
	// fail("Not yet implemented");
	// }

}
