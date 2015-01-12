package com.choicemaker.cm.io.blocking.automated.offline.core;

/**
 * An operational property is some property computed during an OABA job that
 * needs to be retained temporarily between OABA stages. In the current
 * implementation, that means between MDB instances. This interface defines the
 * names of OABA operational properties.
 *
 * @author rphall
 */
public interface OabaOperationalPropertyNames {

	/**
	 * The number of blocking fields defined by the matching model used in an
	 * OABA job.<br/><br/>
	 * The property is defined in:
	 * <ul>
	 * <li>StartOabaMDB</li>
	 * <li>SingleRecordMatchMDB</li>
	 * </ul>
	 * 
	 * It is used in
	 * <ul>
	 * <li>BlockingMDB</li>
	 * <li>SingleRecordMatchMDB</li>
	 * </ul>
	 */
	String PN_BLOCKING_FIELD_COUNT = "BLOCKING_FIELD_COUNT";

	/**
	 * The number of chunk files created by the OABA.<br/><br/>
	 * The property is defined in:
	 * <ul>
	 * <li>ChunkMDB</li>
	 * <li>Chunk2MDB</li>
	 * </ul>
	 * 
	 * It is used in
	 * <ul>
	 * <li>AbstractScheduler</li>
	 * <li>MatchSchedulerMDB</li>
	 * <li>SingleRecordMatchMDB</li>
	 * <li>StartTransitivityMDB</li>
	 * </ul>
	 */
	String PN_CHUNK_FILE_COUNT = "CHUNK_FILE_COUNT";

}
