package com.choicemaker.cmit.utils.j2ee;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import com.choicemaker.cm.core.ISerializableRecordSource;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.Sink;

public class FakeSerialRecordSource implements ISerializableRecordSource {

	public static final String PN_NAME = "name";
	public static final String PN_FILENAME = "fileName";

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

	public FakeSerialRecordSource(String tag) {
		this.name = createFakeRecordSourceName(tag);
		this.fileName = createFakeRecordSourceFileName(tag);
	}

	@Override
	public Record getNext() throws IOException {
		return null;
	}

	@Override
	public void open() throws IOException {
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public boolean hasNext() throws IOException {
		return false;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
	}

	@Override
	public ImmutableProbabilityModel getModel() {
		return null;
	}

	@Override
	public void setModel(ImmutableProbabilityModel m) {
	}

	@Override
	public boolean hasSink() {
		return false;
	}

	@Override
	public Sink getSink() {
		return null;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result =
			prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		FakeSerialRecordSource other = (FakeSerialRecordSource) obj;
		if (fileName == null) {
			if (other.fileName != null) {
				return false;
			}
		} else if (!fileName.equals(other.fileName)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "FakeSerialRecordSource [name=" + name + "]";
	}

	@Override
	public Properties getProperties() {
		Properties retVal = new Properties();
		retVal.setProperty(PN_NAME, name);
		retVal.setProperty(PN_FILENAME, fileName);
		return retVal;
	}

	@Override
	public void setProperties(Properties properties) {
	}

	@Override
	public String toXML() {
		return "<fakeSerialRecordSource name=\"" + name + "\" fileName=\""
				+ fileName + "\" />";
	}

}