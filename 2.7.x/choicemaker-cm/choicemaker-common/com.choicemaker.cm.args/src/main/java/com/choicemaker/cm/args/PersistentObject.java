package com.choicemaker.cm.args;

/**
 * Base interface for (most) ChoiceMaker persistent objects. Uses UUID to solve
 * the issue of object identity in the VM versus the database. Adapted from:
 * <ul>
 * <li>Code samples by James Brundege,
 * "Don't Let Hibernate Steal Your Identity", O'Reilly OnJava,
 * http://links.rph.cx/1K3bYir</li>
 * <li>Comments on StackOverflow by 'Grzesiek D.', in discussion titled
 * "Java - JPA - @Version annotation", http://links.rph.cx/1CSeA09</li>
 * </ul>
 */
public interface PersistentObject {
	
	/** Physical key */
	public long getId();

	/** Logical key */
	public String getUUID();

	/** Optimistic locking */
	public int getOptLock();

	/** Checks if an instance has been persisted to the database */
	public boolean isPersistent();

}
