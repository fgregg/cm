package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_ALLOCATE_CHUNKS;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_BLOCK_BY_ONE_COLUMN;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_CREATE_CHUNK_IDS;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_CREATE_CHUNK_OVERSIZED_IDS;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_CREATE_REC_VAL;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DEDUP_BLOCKS;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DEDUP_OVERSIZED;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DEDUP_OVERSIZED_EXACT;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_ALLOCATE_CHUNKS;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_BLOCK_BY_ONE_COLUMN;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_CREATE_CHUNK_DATA;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_CREATE_CHUNK_IDS;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_DEDUP_BLOCKS;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_DEDUP_MATCHES;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_DEDUP_OVERSIZED;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_DEDUP_OVERSIZED_EXACT;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_MATCHING_DATA;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_OABA;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_OVERSIZED_TRIMMING;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_REC_VAL;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_REVERSE_TRANSLATE_BLOCK;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_REVERSE_TRANSLATE_OVERSIZED;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_INIT;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_MATCHING_DATA;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_MERGE_DEDUP_MATCHES;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_OUTPUT_DEDUP_MATCHES;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_OVERSIZED_TRIMMING;

import java.util.HashMap;
import java.util.Map;

import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEvent;

class OabaProcessingUtil {

	private static Map<Integer, OabaEvent> mapOabaIdEvent = new HashMap<>();

	static {
		mapOabaIdEvent.put(EVT_INIT, OabaEvent.INIT);
		mapOabaIdEvent.put(EVT_CREATE_REC_VAL, OabaEvent.CREATE_REC_VAL);
		mapOabaIdEvent.put(EVT_DONE_REC_VAL, OabaEvent.DONE_REC_VAL);
		mapOabaIdEvent.put(EVT_BLOCK_BY_ONE_COLUMN,
				OabaEvent.BLOCK_BY_ONE_COLUMN);
		mapOabaIdEvent.put(EVT_DONE_BLOCK_BY_ONE_COLUMN,
				OabaEvent.DONE_BLOCK_BY_ONE_COLUMN);
		mapOabaIdEvent
				.put(EVT_OVERSIZED_TRIMMING, OabaEvent.OVERSIZED_TRIMMING);
		mapOabaIdEvent.put(EVT_DONE_OVERSIZED_TRIMMING,
				OabaEvent.DONE_OVERSIZED_TRIMMING);
		mapOabaIdEvent.put(EVT_DEDUP_BLOCKS, OabaEvent.DEDUP_BLOCKS);
		mapOabaIdEvent.put(EVT_DONE_DEDUP_BLOCKS, OabaEvent.DONE_DEDUP_BLOCKS);
		mapOabaIdEvent.put(EVT_DEDUP_OVERSIZED_EXACT,
				OabaEvent.DEDUP_OVERSIZED_EXACT);
		mapOabaIdEvent.put(EVT_DONE_DEDUP_OVERSIZED_EXACT,
				OabaEvent.DONE_DEDUP_OVERSIZED_EXACT);
		mapOabaIdEvent.put(EVT_DEDUP_OVERSIZED, OabaEvent.DEDUP_OVERSIZED);

		mapOabaIdEvent.put(EVT_DONE_DEDUP_OVERSIZED,
				OabaEvent.DONE_DEDUP_OVERSIZED);

		mapOabaIdEvent.put(EVT_DONE_REVERSE_TRANSLATE_BLOCK,
				OabaEvent.DONE_REVERSE_TRANSLATE_BLOCK);
		mapOabaIdEvent.put(EVT_DONE_REVERSE_TRANSLATE_OVERSIZED,
				OabaEvent.DONE_REVERSE_TRANSLATE_OVERSIZED);

		mapOabaIdEvent.put(EVT_CREATE_CHUNK_IDS, OabaEvent.CREATE_CHUNK_IDS);
		mapOabaIdEvent.put(EVT_CREATE_CHUNK_OVERSIZED_IDS,
				OabaEvent.CREATE_CHUNK_OVERSIZED_IDS);
		mapOabaIdEvent.put(EVT_DONE_CREATE_CHUNK_IDS,
				OabaEvent.DONE_CREATE_CHUNK_IDS);
		mapOabaIdEvent.put(EVT_DONE_CREATE_CHUNK_DATA,
				OabaEvent.DONE_CREATE_CHUNK_DATA);

		mapOabaIdEvent.put(EVT_ALLOCATE_CHUNKS, OabaEvent.ALLOCATE_CHUNKS);
		mapOabaIdEvent.put(EVT_DONE_ALLOCATE_CHUNKS,
				OabaEvent.DONE_ALLOCATE_CHUNKS);
		mapOabaIdEvent.put(EVT_MATCHING_DATA, OabaEvent.MATCHING_DATA);
		mapOabaIdEvent
				.put(EVT_DONE_MATCHING_DATA, OabaEvent.DONE_MATCHING_DATA);

		mapOabaIdEvent.put(EVT_OUTPUT_DEDUP_MATCHES,
				OabaEvent.OUTPUT_DEDUP_MATCHES);
		mapOabaIdEvent.put(EVT_MERGE_DEDUP_MATCHES,
				OabaEvent.MERGE_DEDUP_MATCHES);
		mapOabaIdEvent
				.put(EVT_DONE_DEDUP_MATCHES, OabaEvent.DONE_DEDUP_MATCHES);

		mapOabaIdEvent.put(EVT_DONE_OABA, OabaEvent.DONE_OABA);

		// Check that there were no duplicate event ids
		assert mapOabaIdEvent.keySet().size() == OabaEvent.values().length;
	}

	public static OabaEvent getOabaEvent(int evtId) {
		return mapOabaIdEvent.get(evtId);
	}

	private OabaProcessingUtil() {
	}

}
