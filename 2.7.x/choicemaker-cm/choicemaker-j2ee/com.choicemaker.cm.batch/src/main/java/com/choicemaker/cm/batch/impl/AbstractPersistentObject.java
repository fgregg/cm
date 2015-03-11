package com.choicemaker.cm.batch.impl;

import static com.choicemaker.cm.batch.impl.AbstractPersistentObjectJPA.CD_OPTLOCK;
import static com.choicemaker.cm.batch.impl.AbstractPersistentObjectJPA.CN_OPTLOCK;
import static com.choicemaker.cm.batch.impl.AbstractPersistentObjectJPA.CN_UUID;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import com.choicemaker.cm.args.PersistentObject;

/**
 * Base class for (most) ChoiceMaker persistent objects. Uses UUID to solve the
 * issue of object identity in the VM versus the database. Adapted from:
 * <ul>
 * <li>Code samples by James Brundege,
 * "Don't Let Hibernate Steal Your Identity", O'Reilly OnJava,
 * http://links.rph.cx/1K3bYir</li>
 * <li>Comments on StackOverflow by 'Grzesiek D.', in discussion titled
 * "Java - JPA - @Version annotation", http://links.rph.cx/1CSeA09</li>
 * </ul>
 * 
 * @author rphall
 */
@MappedSuperclass
public abstract class AbstractPersistentObject implements PersistentObject {

	public static final long NONPERSISTENT_ID = 0L;

	public static boolean isPersistentId(long id) {
		return id > NONPERSISTENT_ID;
	}

	@Column(name = CN_UUID, unique = true, nullable = false)
	private String uuid = IdGenerator.createId();

	@Version
	@Column(name = CN_OPTLOCK, columnDefinition = CD_OPTLOCK, nullable = false)
	private int optLock = 0;

	protected AbstractPersistentObject() {
	}

	@Override
	public final String getUUID() {
		return uuid;
	}

	@Override
	public final int getOptLock() {
		return optLock;
	}

	/** Assumes that physical keys are positive */
	@Override
	public boolean isPersistent() {
		return isPersistentId(getId());
	}

	// -- Identity

	@Override
	public final int hashCode() {
		return (uuid == null) ? super.hashCode() : uuid.hashCode();
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PersistentObject)) {
			return false;
		}
		PersistentObject other = (PersistentObject) obj;
		if (uuid == null) {
			return false;
		}
		return uuid.equals(other.getUUID());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[uuid=" + uuid + "]";
	}

}
