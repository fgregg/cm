package com.choicemaker.cmit.utils;

import java.util.UUID;

import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.batch.impl.AbstractPersistentObject;

public class FakePersistableRecordSource extends AbstractPersistentObject
		implements PersistableRecordSource {

	public static final String TYPE = "FAKE";

	private static final long serialVersionUID = 271L;

	public static String createFakeNameStem(String tag) {
		if (tag == null) {
			tag = EntityManagerUtils.DEFAULT_RECORDSOURCE_TAG;
		}
		tag = tag.trim();
		if (tag.isEmpty()) {
			tag = EntityManagerUtils.DEFAULT_RECORDSOURCE_TAG;
		}
		return tag;
	}

	public static String createFakeRecordSourceName(String tag) {
		String nameStem = createFakeNameStem(tag);
		StringBuilder sb = new StringBuilder(nameStem);
		if (tag.startsWith(EntityManagerUtils.PREFIX_FAKE_RECORDSOURCE)) {
			sb.append(tag);
		} else {
			sb.append(EntityManagerUtils.PREFIX_FAKE_RECORDSOURCE);
			sb.append(tag);
		}
		sb.append(EntityManagerUtils.TAG_DELIMITER);
		sb.append(UUID.randomUUID().toString());
		final String retVal = sb.toString();
		return retVal;
	}

	public static String createFakeRecordSourceFileName(String tag) {
		String nameStem = createFakeNameStem(tag);
		StringBuilder sb = new StringBuilder(nameStem);
		if (tag.startsWith(EntityManagerUtils.PREFIX_FAKE_RECORDSOURCE_FILE)) {
			sb.append(tag);
		} else {
			sb.append(EntityManagerUtils.PREFIX_FAKE_RECORDSOURCE_FILE);
			sb.append(tag);
		}
		sb.append(EntityManagerUtils.TAG_DELIMITER);
		sb.append(UUID.randomUUID().toString());
		final String retVal = sb.toString();
		return retVal;
	}

	private final String name;
	private final String fileName;

	public FakePersistableRecordSource(String tag) {
		this.name = createFakeRecordSourceName(tag);
		this.fileName = createFakeRecordSourceFileName(tag);
	}

	@Override
	public String toString() {
		return "FakeSerialRecordSource [name=" + name + "]";
	}

	@Override
	public long getId() {
		return UUID.randomUUID().hashCode();
	}

	@Override
	public String getType() {
		return TYPE;
	}

	public String getFileName() {
		return fileName;
	}

}