package com.choicemaker.cm.transitivity.util;

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
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_DEDUP_OVERSIZED_EXACT;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_MATCHING_DATA;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_OVERSIZED_TRIMMING;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_REC_VAL;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_REVERSE_TRANSLATE_BLOCK;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_REVERSE_TRANSLATE_OVERSIZED;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_TRANSANALYSIS;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_TRANS_DEDUP_OVERSIZED;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_INIT;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_MATCHING_DATA;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_MERGE_DEDUP_MATCHES;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_OUTPUT_DEDUP_MATCHES;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_OVERSIZED_TRIMMING;

import java.util.HashMap;
import java.util.Map;

import com.choicemaker.cm.transitivity.core.TransitivityProcessing.TransitivityEvent;

class TransitivityProcessingUtil {

	private static Map<Integer, TransitivityEvent> mapTransIdEvent = new HashMap<>();

	static {
		mapTransIdEvent.put(EVT_INIT, TransitivityEvent.INIT);
		mapTransIdEvent.put(EVT_CREATE_REC_VAL, TransitivityEvent.CREATE_REC_VAL);
		mapTransIdEvent.put(EVT_DONE_REC_VAL, TransitivityEvent.DONE_REC_VAL);
		mapTransIdEvent.put(EVT_BLOCK_BY_ONE_COLUMN,
				TransitivityEvent.BLOCK_BY_ONE_COLUMN);
		mapTransIdEvent.put(EVT_DONE_BLOCK_BY_ONE_COLUMN,
				TransitivityEvent.DONE_BLOCK_BY_ONE_COLUMN);
		mapTransIdEvent.put(EVT_OVERSIZED_TRIMMING,
				TransitivityEvent.OVERSIZED_TRIMMING);
		mapTransIdEvent.put(EVT_DONE_OVERSIZED_TRIMMING,
				TransitivityEvent.DONE_OVERSIZED_TRIMMING);
		mapTransIdEvent.put(EVT_DEDUP_BLOCKS, TransitivityEvent.DEDUP_BLOCKS);
		mapTransIdEvent
				.put(EVT_DONE_DEDUP_BLOCKS, TransitivityEvent.DONE_DEDUP_BLOCKS);
		mapTransIdEvent.put(EVT_DEDUP_OVERSIZED_EXACT,
				TransitivityEvent.DEDUP_OVERSIZED_EXACT);
		mapTransIdEvent.put(EVT_DONE_DEDUP_OVERSIZED_EXACT,
				TransitivityEvent.DONE_DEDUP_OVERSIZED_EXACT);
		mapTransIdEvent.put(EVT_DEDUP_OVERSIZED, TransitivityEvent.DEDUP_OVERSIZED);
		mapTransIdEvent.put(EVT_DONE_TRANS_DEDUP_OVERSIZED,
				TransitivityEvent.DONE_TRANS_DEDUP_OVERSIZED);
		mapTransIdEvent.put(EVT_DONE_REVERSE_TRANSLATE_BLOCK,
				TransitivityEvent.DONE_REVERSE_TRANSLATE_BLOCK);
		mapTransIdEvent.put(EVT_DONE_REVERSE_TRANSLATE_OVERSIZED,
				TransitivityEvent.DONE_REVERSE_TRANSLATE_OVERSIZED);

		mapTransIdEvent.put(EVT_CREATE_CHUNK_IDS, TransitivityEvent.CREATE_CHUNK_IDS);
		mapTransIdEvent.put(EVT_CREATE_CHUNK_OVERSIZED_IDS,
				TransitivityEvent.CREATE_CHUNK_OVERSIZED_IDS);
		mapTransIdEvent.put(EVT_DONE_CREATE_CHUNK_IDS,
				TransitivityEvent.DONE_CREATE_CHUNK_IDS);
		mapTransIdEvent.put(EVT_DONE_CREATE_CHUNK_DATA,
				TransitivityEvent.DONE_CREATE_CHUNK_DATA);

		mapTransIdEvent.put(EVT_ALLOCATE_CHUNKS, TransitivityEvent.ALLOCATE_CHUNKS);
		mapTransIdEvent.put(EVT_DONE_ALLOCATE_CHUNKS,
				TransitivityEvent.DONE_ALLOCATE_CHUNKS);
		mapTransIdEvent.put(EVT_MATCHING_DATA, TransitivityEvent.MATCHING_DATA);
		mapTransIdEvent.put(EVT_DONE_MATCHING_DATA,
				TransitivityEvent.DONE_MATCHING_DATA);
		mapTransIdEvent.put(EVT_OUTPUT_DEDUP_MATCHES,
				TransitivityEvent.OUTPUT_DEDUP_MATCHES);
		mapTransIdEvent.put(EVT_MERGE_DEDUP_MATCHES,
				TransitivityEvent.MERGE_DEDUP_MATCHES);
		mapTransIdEvent.put(EVT_DONE_DEDUP_MATCHES,
				TransitivityEvent.DONE_DEDUP_MATCHES);
		mapTransIdEvent.put(EVT_DONE_TRANSANALYSIS,
				TransitivityEvent.DONE_TRANSANALYSIS);

		// Check that there were no duplicate event ids
		assert mapTransIdEvent.keySet().size() == TransitivityEvent.values().length;
	}

	public static TransitivityEvent getTransEvent(int evtId) {
		return mapTransIdEvent.get(evtId);
	}

	private TransitivityProcessingUtil() {
	}

}
