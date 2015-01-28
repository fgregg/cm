package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;

public class RecordIdTestUtils {

	private static final Random random = new Random();

	private static final int DEFAULT_MAX_COUNT = 100;

	private static final int MIN_COUNT = 1;

	private static int getRandomSize(int max) {
		if (max < MIN_COUNT) {
			throw new IllegalArgumentException("max < " + MIN_COUNT);
		}
		int retVal;
		if (max == 1) {
			retVal = 1;
		} else {
			retVal = random.nextInt(max - 1);
			retVal += 1;
		}
		assert retVal >= 1 && retVal <= max;
		return retVal;
	}

	@SuppressWarnings("rawtypes")
	private static final Class[] allowedTypes = new Class[] {
			Integer.class, Long.class, String.class };

	@SuppressWarnings("rawtypes")
	public static List<Class> getAllowedRecordIdTypes() {
		return Collections.unmodifiableList(Arrays.asList(allowedTypes));
	}

	public static List<Integer> getIdentifiersAsIntegers() {
		return getIdentifiersAsIntegers(DEFAULT_MAX_COUNT);
	}

	public static List<Integer> getIdentifiersAsIntegers(int maxCount) {
		List<Integer> retVal = new ArrayList<>();
		int count = Math.max(getRandomSize(maxCount), 2);
		for (int i = 0; i < count; i++) {
			retVal.add(random.nextInt());
		}
		return Collections.unmodifiableList(retVal);
	}

	public static List<Long> getIdentifiersAsLongs() {
		List<Long> retVal = new ArrayList<>();
		for (Integer i : getIdentifiersAsIntegers()) {
			Long id = Long.valueOf(i);
			retVal.add(id);
		}
		return Collections.unmodifiableList(retVal);
	}

	public static List<String> getIdentifiersAsStrings() {
		List<String> retVal = new ArrayList<>();
		for (Integer i : getIdentifiersAsIntegers()) {
			String id = String.valueOf(i);
			retVal.add(id);
		}
		return Collections.unmodifiableList(retVal);
	}

	public static <T extends Comparable<T>> List<String> getFileData(List<T> ids) {
		if (ids == null || ids.size() == 0) {
			throw new IllegalArgumentException("null or empty identifier list");
		}
		List<String> retVal = new ArrayList<>();
		int count = 0;
		T id = ids.get(count);
		RECORD_ID_TYPE rit = RECORD_ID_TYPE.fromInstance(id);
		retVal.add(rit.getStringSymbol());
		for (; count < ids.size(); ++count) {
			id = ids.get(count);
			retVal.add(String.valueOf(id));
		}
		return Collections.unmodifiableList(retVal);
	}

	public static String createTempFileName() {
		String fileName = null;
		try {
			File f =
				File.createTempFile(RecordIdSinkTest.SINK_PREFIX,
						RecordIdSinkTest.FILE_EXTENTION);
			fileName = f.getAbsolutePath();
			f.delete();
		} catch (IOException e) {
			fail(e.toString());
		}
		assert fileName != null;
		return fileName;
	}

	public static <T extends Comparable<T>> RecordIdSink writeIdentifiersToFile(
			String fileName, List<T> identifiers) throws IOException,
			BlockingException {
		RecordIdSink ris = new RecordIdSink(fileName);

		ris.open();
		for (T id : identifiers) {
			ris.writeRecordID(id);
		}
		ris.close();

		return ris;
	}

	private RecordIdTestUtils() {
	}

}
