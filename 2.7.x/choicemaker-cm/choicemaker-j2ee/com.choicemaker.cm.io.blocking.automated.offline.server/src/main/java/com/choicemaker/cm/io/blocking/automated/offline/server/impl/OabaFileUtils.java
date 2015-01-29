/*
 * Copyright (c) 2001, 2009 ChoiceMaker Technologies, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ChoiceMaker Technologies, Inc. - initial API and implementation
 */
package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.batch.impl.BatchJobFileUtils.BINARY_SUFFIX;
import static com.choicemaker.cm.batch.impl.BatchJobFileUtils.FILE_SEPARATOR;
import static com.choicemaker.cm.batch.impl.BatchJobFileUtils.MAX_FILE_SIZE;
import static com.choicemaker.cm.batch.impl.BatchJobFileUtils.TEXT_SUFFIX;
import static com.choicemaker.cm.batch.impl.BatchJobFileUtils.formatJobId;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.impl.BatchJobFileUtils;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;
import com.choicemaker.cm.io.blocking.automated.offline.impl.BlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ChunkDataSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ChunkRecordIDSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonArrayGroupSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonArraySinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonTreeGroupSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonTreeSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.IDTreeSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.MatchRecord2CompositeSink;
import com.choicemaker.cm.io.blocking.automated.offline.impl.MatchRecord2CompositeSource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.MatchRecord2SinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.RecValSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.SuffixTreeSink;
import com.choicemaker.cm.io.blocking.automated.offline.impl.SuffixTreeSource;

/**
 * This object configures factory objects for Batch jobs.
 * 
 * @author pcheung
 *
 */
public class OabaFileUtils {

	public static final String BASENAME_BIG_BLOCK_STORE = "bigBlocks";

	public static final String BASENAME_BLOCK_STORE = "blocks";

	public static final String BASENAME_BLOCKGROUP_STORE = "blockGroup";

	public static final String BASENAME_CHUNKMASTER_ROW_STORE =
		"chunkmasterrow";

	public static final String BASENAME_CHUNKROW_STORE = "chunkrow";

	public static final String BASENAME_CHUNKSTAGE_ROW_STORE = "chunkstagerow";

	public static final String BASENAME_COMPARE_ARRAY_GROUP =
		"compareArrayGroup_O";

	public static final String BASENAME_COMPARE_ARRAY_STORE = "compareArray_O";

	public static final String BASENAME_COMPARE_TREE_GROUP_STORE =
		"compareTreeGroup";

	public static final String BASENAME_COMPARE_TREE_STORE = "compareTree";

	public static final String BASENAME_COMPAREGROUP_STORE = "compareGroup";

	public static final String BASENAME_MATCH_STORE_INDEXED = "match_";

	public static final String BASENAME_MATCH_TEMP_STORE = "matchtemp";

	public static final String BASENAME_MATCH_TEMP_STORE_INDEXED = "matchtemp_";

	public static final String BASENAME_MATCHCHUNK_STORE = "matchchunk";

	public static final String BASENAME_OVERSIZED_GROUP_STORE =
		"oversizedGroup";

	public static final String BASENAME_OVERSIZED_STORE = "oversized";

	public static final String BASENAME_OVERSIZED_TEMP_STORE = "oversizedTemp";

	public static final String BASENAME_RECVAL_STORE = "btemp";

	public static final String BASENAME_TEMP_BLOCK_STORE = "tempBlocks";

	public static final String BASENAME_TWOMATCH_STORE = "twomatch";

	public static final String FILENAME_TREE_STORE = "trees.txt";

	public static BlockSinkSourceFactory getBigBlocksSinkSourceFactory(
			BatchJob job) {
		String wd = BatchJobFileUtils.getWorkingDir(job);
		return new BlockSinkSourceFactory(wd, BASENAME_BIG_BLOCK_STORE,
				BINARY_SUFFIX);
	}

	public static BlockSinkSourceFactory getBlockFactory(BatchJob job) {
		String wd = BatchJobFileUtils.getWorkingDir(job);
		return new BlockSinkSourceFactory(wd, BASENAME_BLOCK_STORE,
				BINARY_SUFFIX);
	}

	public static BlockSinkSourceFactory getBlockGroupFactory(BatchJob job) {
		String wd = BatchJobFileUtils.getWorkingDir(job);
		return new BlockSinkSourceFactory(wd, BASENAME_BLOCKGROUP_STORE,
				BINARY_SUFFIX);
	}

	public static ComparisonArraySinkSourceFactory getCGFactory(BatchJob job) {
		String wd = BatchJobFileUtils.getWorkingDir(job);
		return new ComparisonArraySinkSourceFactory(wd,
				BASENAME_COMPAREGROUP_STORE, BINARY_SUFFIX);
	}

	public static ChunkRecordIDSinkSourceFactory getChunkIDFactory(BatchJob job) {
		String wd = BatchJobFileUtils.getWorkingDir(job);
		return new ChunkRecordIDSinkSourceFactory(wd, BASENAME_CHUNKROW_STORE,
				BINARY_SUFFIX);
	}

	public static ComparisonArraySinkSourceFactory getComparisonArrayFactoryOS(
			BatchJob job) {
		String wd = BatchJobFileUtils.getWorkingDir(job);
		return new ComparisonArraySinkSourceFactory(wd,
				BASENAME_COMPARE_ARRAY_STORE, BINARY_SUFFIX);
	}

	/**
	 * This is used by the parallelization code. It creates many array files for
	 * each chunk.
	 */
	public static ComparisonArrayGroupSinkSourceFactory getComparisonArrayGroupFactoryOS(
			BatchJob job, int num) {
		String wd = BatchJobFileUtils.getWorkingDir(job);
		return new ComparisonArrayGroupSinkSourceFactory(wd,
				BASENAME_COMPARE_ARRAY_GROUP, BINARY_SUFFIX, num);
	}

	public static ComparisonTreeSinkSourceFactory getComparisonTreeFactory(
			BatchJob job, RECORD_ID_TYPE stageType) {
		String wd = BatchJobFileUtils.getWorkingDir(job);
		return new ComparisonTreeSinkSourceFactory(wd,
				BASENAME_COMPARE_TREE_STORE, TEXT_SUFFIX, stageType);
	}

	/**
	 * This is used by the parallelization code. It creates many tree files for
	 * each chunk.
	 */
	public static ComparisonTreeGroupSinkSourceFactory getComparisonTreeGroupFactory(
			BatchJob job, RECORD_ID_TYPE stageType, int num) {
		String wd = BatchJobFileUtils.getWorkingDir(job);
		return new ComparisonTreeGroupSinkSourceFactory(wd,
				BASENAME_COMPARE_TREE_GROUP_STORE, TEXT_SUFFIX, num, stageType);
	}

	protected static String getCompositeMatchFileName(BatchJob job) {
		String wd = BatchJobFileUtils.getWorkingDir(job);
		assert wd.endsWith(FILE_SEPARATOR);
		String id = formatJobId(job.getId());
		String retVal = wd + BASENAME_MATCH_STORE_INDEXED + id;
		return retVal;
	}

	/**
	 * This returns the final sink in which to store the result of the OABA.
	 * Since this file could be big, we limit the file size to MAX_FILE_SIZE.
	 * This is mainly for Windows 32 systems where the max file size if 2 GB.
	 * The file name is [file dir]/match_[job id]_*.txt.
	 * 
	 * @param id
	 *            - the job id of the OABA job
	 * @return IMatchRecord2Sink - the sink to store the OABA output.
	 */
	@SuppressWarnings("rawtypes")
	public static IMatchRecord2Sink getCompositeMatchSink(BatchJob job) {
		String fileName = getCompositeMatchFileName(job);
		return new MatchRecord2CompositeSink(fileName, "txt", MAX_FILE_SIZE);
	}

	/**
	 * This returns the source handle to the OABA result.
	 * 
	 * @param id
	 *            - the job id of the OABA job
	 */
	@SuppressWarnings("rawtypes")
	public static IMatchRecord2Source getCompositeMatchSource(BatchJob job) {
		String fileName = getCompositeMatchFileName(job);
		return new MatchRecord2CompositeSource(fileName, TEXT_SUFFIX);
	}

	public static ChunkDataSinkSourceFactory getMasterDataFactory(BatchJob job,
			ImmutableProbabilityModel model) {
		if (model == null) {
			throw new IllegalArgumentException("null modelId");
		}
		String wd = BatchJobFileUtils.getWorkingDir(job);
		return new ChunkDataSinkSourceFactory(wd,
				BASENAME_CHUNKMASTER_ROW_STORE, model);
	}

	/**
	 * This gets the match result sink for each chunk of the Matcher Bean.
	 * 
	 */
	public static MatchRecord2SinkSourceFactory getMatchChunkFactory(
			BatchJob job) {
		String wd = BatchJobFileUtils.getWorkingDir(job);
		return new MatchRecord2SinkSourceFactory(wd, BASENAME_MATCHCHUNK_STORE,
				"txt");
	}

	public static MatchRecord2SinkSourceFactory getMatchTempFactory(BatchJob job) {
		String wd = BatchJobFileUtils.getWorkingDir(job);
		return new MatchRecord2SinkSourceFactory(wd, BASENAME_MATCH_TEMP_STORE,
				"txt");
	}

	public static MatchRecord2SinkSourceFactory getMatchTempFactory(
			BatchJob job, int i) {
		String wd = BatchJobFileUtils.getWorkingDir(job);
		String str = Integer.toString(i);
		String fileNameIndexed = BASENAME_MATCH_TEMP_STORE_INDEXED + str + "_";
		return new MatchRecord2SinkSourceFactory(wd, fileNameIndexed, "txt");
	}

	public static BlockSinkSourceFactory getOversizedFactory(BatchJob job) {
		String wd = BatchJobFileUtils.getWorkingDir(job);
		return new BlockSinkSourceFactory(wd, BASENAME_OVERSIZED_STORE,
				BINARY_SUFFIX);
	}

	public static BlockSinkSourceFactory getOversizedGroupFactory(BatchJob job) {
		String wd = BatchJobFileUtils.getWorkingDir(job);
		return new BlockSinkSourceFactory(wd, BASENAME_OVERSIZED_GROUP_STORE,
				BINARY_SUFFIX);
	}

	public static BlockSinkSourceFactory getOversizedTempFactory(BatchJob job) {
		String wd = BatchJobFileUtils.getWorkingDir(job);
		return new BlockSinkSourceFactory(wd, BASENAME_OVERSIZED_TEMP_STORE,
				BINARY_SUFFIX);
	}

	public static RecValSinkSourceFactory getRecValFactory(BatchJob job) {
		String wd = BatchJobFileUtils.getWorkingDir(job);
		return new RecValSinkSourceFactory(wd, BASENAME_RECVAL_STORE,
				BINARY_SUFFIX);
	}

	public static MatchRecord2SinkSourceFactory getSet2MatchFactory(BatchJob job) {
		String wd = BatchJobFileUtils.getWorkingDir(job);
		return new MatchRecord2SinkSourceFactory(wd, BASENAME_TWOMATCH_STORE,
				TEXT_SUFFIX);
	}

	public static ChunkDataSinkSourceFactory getStageDataFactory(BatchJob job,
			ImmutableProbabilityModel model) {
		if (model == null) {
			throw new IllegalArgumentException("null modelId");
		}
		String wd = BatchJobFileUtils.getWorkingDir(job);
		return new ChunkDataSinkSourceFactory(wd,
				BASENAME_CHUNKSTAGE_ROW_STORE, model);
	}

	public static SuffixTreeSink getSuffixTreeSink(BatchJob job) {
		String path = getTreeFilePath(job);
		return new SuffixTreeSink(path);
	}

	public static BlockSinkSourceFactory getTempBlocksSinkSourceFactory(
			BatchJob job) {
		String wd = BatchJobFileUtils.getWorkingDir(job);
		return new BlockSinkSourceFactory(wd, BASENAME_TEMP_BLOCK_STORE,
				BINARY_SUFFIX);
	}

	protected static String getTreeFilePath(BatchJob job) {
		String wd = BatchJobFileUtils.getWorkingDir(job);
		assert wd.endsWith(FILE_SEPARATOR);
		String retVal = wd + FILENAME_TREE_STORE;
		return retVal;
	}

	public static IDTreeSetSource getTreeSetSource(BatchJob job) {
		String path = getTreeFilePath(job);
		SuffixTreeSource sSource = new SuffixTreeSource(path);
		IDTreeSetSource source = new IDTreeSetSource(sSource);
		return source;
	}

	protected OabaFileUtils() {
	}

}
