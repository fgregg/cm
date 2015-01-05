package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.*;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaEvent.*;

import java.util.HashMap;
import java.util.Map;

import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEvent;

/**
 * A deprecated map of event sequence numbers to events. In principle, there's
 * no guarantee that two events might not have the same sequencing number; for
 * example, an OABA process might branch into two or more alternate paths, and
 * some processing events on the alternate paths might share the same sequence
 * number.
 * 
 * @deprecated
 */
class OabaEventUtil {

	private static Map<Integer, OabaEvent> mapOabaIdEvent = new HashMap<>();

	static {
		mapOabaIdEvent.put(EVT_INIT, INIT);
		mapOabaIdEvent.put(EVT_CREATE_REC_VAL, CREATE_REC_VAL);
		mapOabaIdEvent.put(EVT_DONE_REC_VAL, DONE_REC_VAL);
		mapOabaIdEvent.put(EVT_BLOCK_BY_ONE_COLUMN, BLOCK_BY_ONE_COLUMN);
		mapOabaIdEvent.put(EVT_DONE_BLOCK_BY_ONE_COLUMN,
				DONE_BLOCK_BY_ONE_COLUMN);
		mapOabaIdEvent.put(EVT_OVERSIZED_TRIMMING, OVERSIZED_TRIMMING);
		mapOabaIdEvent
				.put(EVT_DONE_OVERSIZED_TRIMMING, DONE_OVERSIZED_TRIMMING);
		mapOabaIdEvent.put(EVT_DEDUP_BLOCKS, DEDUP_BLOCKS);
		mapOabaIdEvent.put(EVT_DONE_DEDUP_BLOCKS, DONE_DEDUP_BLOCKS);
		mapOabaIdEvent.put(EVT_DEDUP_OVERSIZED_EXACT, DEDUP_OVERSIZED_EXACT);
		mapOabaIdEvent.put(EVT_DONE_DEDUP_OVERSIZED_EXACT,
				DONE_DEDUP_OVERSIZED_EXACT);
		mapOabaIdEvent.put(EVT_DEDUP_OVERSIZED, DEDUP_OVERSIZED);

		mapOabaIdEvent.put(EVT_DONE_DEDUP_OVERSIZED, DONE_DEDUP_OVERSIZED);

		mapOabaIdEvent.put(EVT_DONE_REVERSE_TRANSLATE_BLOCK,
				DONE_REVERSE_TRANSLATE_BLOCK);
		mapOabaIdEvent.put(EVT_DONE_REVERSE_TRANSLATE_OVERSIZED,
				DONE_REVERSE_TRANSLATE_OVERSIZED);

		mapOabaIdEvent.put(EVT_CREATE_CHUNK_IDS, CREATE_CHUNK_IDS);
		mapOabaIdEvent.put(EVT_CREATE_CHUNK_OVERSIZED_IDS,
				CREATE_CHUNK_OVERSIZED_IDS);
		mapOabaIdEvent.put(EVT_DONE_CREATE_CHUNK_IDS, DONE_CREATE_CHUNK_IDS);
		mapOabaIdEvent.put(EVT_DONE_CREATE_CHUNK_DATA, DONE_CREATE_CHUNK_DATA);

		mapOabaIdEvent.put(EVT_ALLOCATE_CHUNKS, ALLOCATE_CHUNKS);
		mapOabaIdEvent.put(EVT_DONE_ALLOCATE_CHUNKS, DONE_ALLOCATE_CHUNKS);
		mapOabaIdEvent.put(EVT_MATCHING_DATA, MATCHING_DATA);
		mapOabaIdEvent.put(EVT_DONE_MATCHING_DATA, DONE_MATCHING_DATA);

		mapOabaIdEvent.put(EVT_OUTPUT_DEDUP_MATCHES, OUTPUT_DEDUP_MATCHES);
		mapOabaIdEvent.put(EVT_MERGE_DEDUP_MATCHES, MERGE_DEDUP_MATCHES);
		mapOabaIdEvent.put(EVT_DONE_DEDUP_MATCHES, DONE_DEDUP_MATCHES);

		mapOabaIdEvent.put(EVT_DONE_OABA, DONE_OABA);

		// Check that there were no duplicate event ids
		assert mapOabaIdEvent.keySet().size() == values().length;
	}

	public static OabaEvent getOabaEvent(int evtId) {
		return mapOabaIdEvent.get(evtId);
	}

	private OabaEventUtil() {
	}

}
