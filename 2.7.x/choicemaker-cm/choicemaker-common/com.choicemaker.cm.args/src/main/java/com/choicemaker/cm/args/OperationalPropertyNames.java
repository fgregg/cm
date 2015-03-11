package com.choicemaker.cm.args;

/**
 * An operational property is some property computed during a batch job that
 * needs to be retained temporarily during batch processing. In the current
 * implementation, that means between MDB instances. This interface defines the
 * names of OABA operational properties.
 *
 * @author rphall
 */
public interface OperationalPropertyNames {

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

	// /**
	// * An index used to split a task across a set of processing agents that
	// are
	// * running in parallel. The value is defined in
	// * <ul>
	// * <li>AbstractScheduler</li>
	// * </ul>
	// *
	// * It is used in
	// * <ul>
	// * <li>AbstractMatcher</li>
	// * <li>AbstractScheduler</li>
	// * </ul>
	// */
	// String PN_PROCESSING_INDEX = "PROCESSING_INDEX";

	/**
	 * The name of an enum representing the type of the primary key for records
	 * used in a batch job. The value is defined in
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

	/**
	 * The name of a file storing the cached results of an OABA processing job.
	 * The value is defined in
	 * <ul>
	 * <li>StartOabaMDB</li>
	 * </ul>
	 * 
	 * It is used in OABA and non-OABA classes
	 * <ul>
	 * <li>OabaServiceBean (OABA)</li>
	 * <li>BatchRecordMatcherBean (URM)</li>
	 * </ul>
	 */
	String PN_OABA_CACHED_RESULTS_FILE = "OABA_CACHED_RESULTS";

	/**
	 * The name of a file storing the cached, pair-wise results of an
	 * transitivity processing job. The value is defined in
	 * <ul>
	 * <li>???</li>
	 * </ul>
	 * 
	 * It is used in transitivity and non-transitivity classes
	 * <ul>
	 * <li>TransitivityServiceBean (transitivity)</li>
	 * <li>BatchMatchAnalyzerBean (URM)</li>
	 * <li>TransSerializerMsgBean (URM)</li>
	 * </ul>
	 */
	String PN_TRANSITIVITY_CACHED_PAIRS_FILE = "TRANS_CACHED_PAIRS_FILE";

	/**
	 * The name of a boolean-valued property that is used to indicate that a
	 * failed or aborted job should clean up temporary resources. The value is
	 * defined in
	 * <ul>
	 * <li>OabaServiceBean</li>
	 * <li>TransitivityServiceBean</li>
	 * </ul>
	 * <p>
	 * It is used indirectly by various classes via the <code>stopJob</code>
	 * method of the <code>MessageBeanUtils</code> class.
	 * </p>
	 * If the value of this property is a String value that
	 * <code>Boolean.valueOf(String)</code> interprets as <code>true</code>,
	 * then the property is considered <em>set</em>. If the property is null
	 * (i.e. not persistent in the database) or if the value of the property is
	 * not interpreted as <code>true</code>, then the property is considered
	 * <em>not set</em>.
	 */
	String PN_CLEAR_RESOURCES = "CLEAR_RESOURCES";

}
