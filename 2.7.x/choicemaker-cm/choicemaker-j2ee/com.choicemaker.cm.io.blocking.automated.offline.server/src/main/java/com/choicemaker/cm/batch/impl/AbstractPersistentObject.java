package com.choicemaker.cm.batch.impl;

import static com.choicemaker.cm.batch.impl.AbstractPersistentObjectJPA.*;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import com.choicemaker.cm.batch.PersistentObject;

@MappedSuperclass
public abstract class AbstractPersistentObject implements PersistentObject {

	@Column(name = CN_UUID)
	private String uuid = IdGenerator.createId();

	@Version
	@Column(name = CN_OPTLOCK, columnDefinition = CD_OPTLOCK,
			nullable = NULLABLE_OPTLOCK)
	private int optLock = 0;

	protected AbstractPersistentObject() {
	}

	// @Override
	// public long getId() {
	// return id;
	// }

	// protected void setId(long id) {
	// this.id = id;
	// }

	@Override
	public String getUUID() {
		return uuid;
	}

	// protected void setUuid(String uuid) {
	// this.uuid = uuid;
	// }

	@Override
	public int getOptLock() {
		return optLock;
	}

	// protected void setOptLock(int optLock) {
	// this.optLock = optLock;
	// }

}
