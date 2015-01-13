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
	 * OABA job. The value is defined in
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
	 * The total number of chunk files -- regular and over-sized -- that are
	 * created by the OABA. The value is defined in
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

	/**
	 * The number of regular chunk files created by the OABA. The value is
	 * defined in
	 * <ul>
	 * <li>Chunk2MDB</li>
	 * </ul>
	 * 
	 * It is used in
	 * <ul>
	 * <li>AbstractScheduler</li>
	 * <li>MatchSchedulerMDB</li>
	 * <li>StartTransitivityMDB</li>
	 * </ul>
	 */
	String PN_REGULAR_CHUNK_FILE_COUNT = "REGULAR_CHUNK_FILE_COUNT";

	/**
	 * The index of the chunk file that is currently being processed. The value
	 * is defined in
	 * <ul>
	 * <li>AbstractScheduler</li>
	 * </ul>
	 * 
	 * It is used in
	 * <ul>
	 * <li>AbstractMatcher</li>
	 * <li>AbstractScheduler</li>
	 * </ul>
	 */
	String PN_CURRENT_CHUNK_INDEX = "CURRENT_CHUNK_INDEX";

//	/**
//	 * An index used to split a task across a set of processing agents that are
//	 * running in parallel. The value is defined in
//	 * <ul>
//	 * <li>AbstractScheduler</li>
//	 * </ul>
//	 * 
//	 * It is used in
//	 * <ul>
//	 * <li>AbstractMatcher</li>
//	 * <li>AbstractScheduler</li>
//	 * </ul>
//	 */
//	String PN_PROCESSING_INDEX = "PROCESSING_INDEX";

	/**
	 * The name of an enum representing the type of the primary key for records
	 * used in a batch job. The value
	 * is defined in
	 * <ul>
	 * <li>StartOabaMDB</li>
	 * </ul>
	 * 
	 * It is used in
	 * <ul>
	 * <li>AbstractMatcher</li>
	 * <li>ChunkMDB</li>
	 * <li>Chunk2MDB</li>
	 * <li>MatchSchedulerMDB</li>
	 * </ul>
	 */
	String PN_RECORD_ID_TYPE = "RECORD_ID_TYPE";

}
