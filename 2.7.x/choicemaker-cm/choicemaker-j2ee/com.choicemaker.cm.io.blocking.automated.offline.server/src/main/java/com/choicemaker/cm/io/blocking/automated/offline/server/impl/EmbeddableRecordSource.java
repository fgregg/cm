package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import com.choicemaker.cm.args.PersistableRecordSource;

public class EmbeddableRecordSource implements PersistableRecordSource {
	
	private static final long serialVersionUID = 271L;

	private final long id;
	private final String type;

	public EmbeddableRecordSource(long id, String type) {
		if (type == null || !type.equals(type.trim()) || type.isEmpty()) {
			throw new IllegalArgumentException("invalid type: '" + type + "'");
		}
		this.id = id;
		this.type = type;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		EmbeddableRecordSource other = (EmbeddableRecordSource) obj;
		if (id != other.id) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "RecordSource [id=" + id + ", type=" + type + "]";
	}

}
