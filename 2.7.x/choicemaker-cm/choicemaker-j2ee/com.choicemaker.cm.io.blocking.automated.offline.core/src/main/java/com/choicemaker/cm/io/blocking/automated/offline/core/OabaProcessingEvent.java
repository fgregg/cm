package com.choicemaker.cm.io.blocking.automated.offline.core;

import com.choicemaker.cm.args.BatchProcessingEvent;
import com.choicemaker.cm.args.ProcessingEvent;

public class OabaProcessingEvent extends BatchProcessingEvent implements
		ProcessingEvent, OabaProcessing {

	public OabaProcessingEvent(String name, int id, float estimate) {
		super(name, id, estimate);
	}

	public OabaProcessingEvent(OabaEvent event) {
		super(event.name(), event.getEventId(), event.getPercentComplete());
	}

	public static final OabaProcessingEvent CREATE_REC_VAL =
		new OabaProcessingEvent(OabaEvent.CREATE_REC_VAL);

	public static final OabaProcessingEvent DONE_REC_VAL =
		new OabaProcessingEvent(OabaEvent.DONE_REC_VAL);

	public static final OabaProcessingEvent BLOCK_BY_ONE_COLUMN =
		new OabaProcessingEvent(OabaEvent.BLOCK_BY_ONE_COLUMN);

	public static final OabaProcessingEvent DONE_BLOCK_BY_ONE_COLUMN =
		new OabaProcessingEvent(OabaEvent.DONE_BLOCK_BY_ONE_COLUMN);

	public static final OabaProcessingEvent OVERSIZED_TRIMMING =
		new OabaProcessingEvent(OabaEvent.OVERSIZED_TRIMMING);

	public static final OabaProcessingEvent DONE_OVERSIZED_TRIMMING =
		new OabaProcessingEvent(OabaEvent.DONE_OVERSIZED_TRIMMING);

	public static final OabaProcessingEvent DEDUP_BLOCKS =
		new OabaProcessingEvent(OabaEvent.DEDUP_BLOCKS);

	public static final OabaProcessingEvent DONE_DEDUP_BLOCKS =
		new OabaProcessingEvent(OabaEvent.DONE_DEDUP_BLOCKS);

	public static final OabaProcessingEvent DEDUP_OVERSIZED_EXACT =
		new OabaProcessingEvent(OabaEvent.DEDUP_OVERSIZED_EXACT);

	public static final OabaProcessingEvent DONE_DEDUP_OVERSIZED_EXACT =
		new OabaProcessingEvent(OabaEvent.DONE_DEDUP_OVERSIZED_EXACT);

	public static final OabaProcessingEvent DEDUP_OVERSIZED =
		new OabaProcessingEvent(OabaEvent.DEDUP_OVERSIZED);

	public static final OabaProcessingEvent DONE_DEDUP_OVERSIZED =
		new OabaProcessingEvent(OabaEvent.DONE_DEDUP_OVERSIZED);

	public static final OabaProcessingEvent DONE_REVERSE_TRANSLATE_BLOCK =
		new OabaProcessingEvent(OabaEvent.DONE_REVERSE_TRANSLATE_BLOCK);

	public static final OabaProcessingEvent DONE_REVERSE_TRANSLATE_OVERSIZED =
		new OabaProcessingEvent(OabaEvent.DONE_REVERSE_TRANSLATE_OVERSIZED);

	public static final OabaProcessingEvent CREATE_CHUNK_IDS =
		new OabaProcessingEvent(OabaEvent.CREATE_CHUNK_IDS);

	public static final OabaProcessingEvent CREATE_CHUNK_OVERSIZED_IDS =
		new OabaProcessingEvent(OabaEvent.CREATE_CHUNK_OVERSIZED_IDS);

	public static final OabaProcessingEvent DONE_CREATE_CHUNK_IDS =
		new OabaProcessingEvent(OabaEvent.DONE_CREATE_CHUNK_IDS);

	public static final OabaProcessingEvent DONE_CREATE_CHUNK_DATA =
		new OabaProcessingEvent(OabaEvent.DONE_CREATE_CHUNK_DATA);

	public static final OabaProcessingEvent ALLOCATE_CHUNKS =
		new OabaProcessingEvent(OabaEvent.ALLOCATE_CHUNKS);

	public static final OabaProcessingEvent DONE_ALLOCATE_CHUNKS =
		new OabaProcessingEvent(OabaEvent.DONE_ALLOCATE_CHUNKS);

	public static final OabaProcessingEvent MATCHING_DATA =
		new OabaProcessingEvent(OabaEvent.MATCHING_DATA);

	public static final OabaProcessingEvent DONE_MATCHING_DATA =
		new OabaProcessingEvent(OabaEvent.DONE_MATCHING_DATA);

	public static final OabaProcessingEvent OUTPUT_DEDUP_MATCHES =
		new OabaProcessingEvent(OabaEvent.OUTPUT_DEDUP_MATCHES);

	public static final OabaProcessingEvent MERGE_DEDUP_MATCHES =
		new OabaProcessingEvent(OabaEvent.MERGE_DEDUP_MATCHES);

	public static final OabaProcessingEvent DONE_DEDUP_MATCHES =
		new OabaProcessingEvent(OabaEvent.DONE_DEDUP_MATCHES);

}
