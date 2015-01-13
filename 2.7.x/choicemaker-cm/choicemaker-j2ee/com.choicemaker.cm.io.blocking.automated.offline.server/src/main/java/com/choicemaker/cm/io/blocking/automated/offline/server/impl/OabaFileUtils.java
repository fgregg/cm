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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.DecimalFormat;
import java.util.Set;

import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJob;
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
import com.choicemaker.util.SystemPropertyUtils;

/**
 * This object configures factory objects for the OABA.
 * 
 * @author pcheung
 *
 */
@SuppressWarnings("rawtypes")
class OabaFileUtils implements Serializable {

	static final long serialVersionUID = 271;

	// maximum size of a file to 1.5 GB
	public static final long MAX_FILE_SIZE = 1500000000;

	/**
	 * Restricted permissions since the working directory may contain
	 * confidential or sensitive information.
	 */
	public static final String WORKING_DIR_POSIX_PERMISSIONS = "rwx------";

	public static final String TEMP_WORKING_DIR_PREFIX = "BatchJob_";

	public static final String DEFAULT_PREFIX = "job";

	public static final int ROLLOVER = 100000;

	public static final String FMT = "00000";
	
	protected static final String FILE_SEPARATOR = System.getProperty(SystemPropertyUtils.FILE_SEPARATOR);

	public static final String BINARY_SUFFIX = "dat";

	public static final String TEXT_SUFFIX = "txt";

	public static String formatJobId(long jobId) {
		long truncated = Math.abs(jobId) % ROLLOVER;
		String retVal = new DecimalFormat(FMT).format(truncated);
		return retVal;
	}

	public static String computeWorkingDirectoryName(BatchJob job) {
		return computeWorkingDirectoryName(DEFAULT_PREFIX, job);
	}

	public static String computeWorkingDirectoryName(String stem, BatchJob job) {
		if (job == null) {
			throw new IllegalArgumentException("null job");
		}
		if (stem == null || stem.trim().isEmpty()) {
			throw new IllegalArgumentException("null or blank prefix");
		}
		stem = stem.trim();
		String index = formatJobId(job.getId());
		return stem + index;
	}

	/**
	 * Returns the directory on disk that will be used to write files used by
	 * the batch job.
	 */
	public static File createWorkingDirectory(ServerConfiguration sc,
			BatchJob batchJob) {
		if (sc == null | batchJob == null) {
			throw new IllegalArgumentException("null argument");
		}

		final File parentDir = sc.getWorkingDirectoryLocation();
		if (parentDir == null) {
			String msg = "null location for working directory";
			throw new IllegalArgumentException(msg);
		}
		if (!parentDir.exists() || !parentDir.isDirectory()
				|| !parentDir.canWrite() || !parentDir.canRead()) {
			final String pd = parentDir.getAbsolutePath();
			String msg =
				"Location '" + pd + "'"
						+ " does not exist, or is not a directory"
						+ ", cannot be read or cannot be written";
			throw new IllegalArgumentException(msg);
		}

		Set<PosixFilePermission> permissions =
			PosixFilePermissions.fromString(WORKING_DIR_POSIX_PERMISSIONS);
		FileAttribute<Set<PosixFilePermission>> attrs =
			PosixFilePermissions.asFileAttribute(permissions);
		String wd = computeWorkingDirectoryName(batchJob);
		File d = new File(parentDir, wd);
		File retVal = null;
		try {
			Path p = d.toPath();
			retVal = Files.createDirectory(p, attrs).toFile();
		} catch (IOException e) {
			throw new IllegalStateException(e.toString());
		}
		assert retVal != null;
		assert retVal.isDirectory();
		assert retVal.canRead();
		assert retVal.canWrite();

		return retVal;
	}

	/**
	 * Creates a directory in the user's home directory, or if that fails,
	 * creates a directory in the default location for temporary files. If a
	 * directory cannot be created in either location, throws an
	 * IllegalStateException.
	 * 
	 * @return a non-null directory with permissions specified by
	 *         {@link WORKING_DIR_POSIX_PERMISSIONS}
	 * @throws IllegalStateException
	 *             if a directory cannot be created.
	 */
	public static File createDefaultWorkingDir() {
		File retVal = null;
		Set<PosixFilePermission> permissions =
			PosixFilePermissions.fromString(WORKING_DIR_POSIX_PERMISSIONS);
		FileAttribute<Set<PosixFilePermission>> attrs =
			PosixFilePermissions.asFileAttribute(permissions);
		try {
			String userHome = System.getProperty(SystemPropertyUtils.USER_HOME);
			Path userPath = Paths.get(userHome);
			retVal =
				Files.createTempDirectory(userPath, TEMP_WORKING_DIR_PREFIX,
						attrs).toFile();
		} catch (IOException e) {
			assert retVal == null;
		}
		if (retVal == null) {
			try {
				retVal =
					Files.createTempDirectory(TEMP_WORKING_DIR_PREFIX, attrs)
							.toFile();
			} catch (IOException e) {
				throw new IllegalStateException(e.toString());
			}
		}
		assert retVal != null;
		assert retVal.isDirectory();
		assert retVal.canRead();
		assert retVal.canWrite();
		return retVal;
	}

	public static String getWorkingDir(BatchJob job) {
		if (job == null) {
			throw new IllegalArgumentException("null job");
		}
		File d = job.getWorkingDirectory();
		String retVal = d.getAbsolutePath();
		if (!retVal.endsWith(FILE_SEPARATOR)) {
			retVal += FILE_SEPARATOR;
		}
		return retVal;
	}

	public static final String BASENAME_CHUNKSTAGE_ROW_STORE = "chunkstagerow";

	public static ChunkDataSinkSourceFactory getStageDataFactory(BatchJob job,
			ImmutableProbabilityModel model) {
		if (model == null) {
			throw new IllegalArgumentException("null modelId");
		}
		String wd = getWorkingDir(job);
		return new ChunkDataSinkSourceFactory(wd, BASENAME_CHUNKSTAGE_ROW_STORE, model);
	}

	public static final String BASENAME_CHUNKMASTER_ROW_STORE = "chunkmasterrow";

	public static ChunkDataSinkSourceFactory getMasterDataFactory(BatchJob job,
			ImmutableProbabilityModel model) {
		if (model == null) {
			throw new IllegalArgumentException("null modelId");
		}
		String wd = getWorkingDir(job);
		return new ChunkDataSinkSourceFactory(wd, BASENAME_CHUNKMASTER_ROW_STORE, model);
	}

	public static final String BASENAME_COMPAREGROUP_STORE = "compareGroup";

	public static ComparisonArraySinkSourceFactory getCGFactory(BatchJob job) {
		String wd = getWorkingDir(job);
		return new ComparisonArraySinkSourceFactory(wd, BASENAME_COMPAREGROUP_STORE, BINARY_SUFFIX);
	}

	public static final String BASENAME_RECVAL_STORE = "btemp";

	public static RecValSinkSourceFactory getRecValFactory(BatchJob job) {
		String wd = getWorkingDir(job);
		return new RecValSinkSourceFactory(wd, BASENAME_RECVAL_STORE, BINARY_SUFFIX);
	}

	public static final String BASENAME_BLOCKGROUP_STORE = "blockGroup";

	public static BlockSinkSourceFactory getBlockGroupFactory(BatchJob job) {
		String wd = getWorkingDir(job);
		return new BlockSinkSourceFactory(wd, BASENAME_BLOCKGROUP_STORE, BINARY_SUFFIX);
	}

	public static final String BASENAME_BLOCK_STORE = "blocks";
	
	public static BlockSinkSourceFactory getBlockFactory(BatchJob job) {
		String wd = getWorkingDir(job);
		return new BlockSinkSourceFactory(wd, BASENAME_BLOCK_STORE, BINARY_SUFFIX);
	}

	public static final String BASENAME_OVERSIZED_STORE = "oversized";
	
	public static BlockSinkSourceFactory getOversizedFactory(BatchJob job) {
		String wd = getWorkingDir(job);
		return new BlockSinkSourceFactory(wd, BASENAME_OVERSIZED_STORE, BINARY_SUFFIX);
	}

	public static final String BASENAME_OVERSIZED_GROUP_STORE = "oversizedGroup";
	
	public static BlockSinkSourceFactory getOversizedGroupFactory(BatchJob job) {
		String wd = getWorkingDir(job);
		return new BlockSinkSourceFactory(wd, BASENAME_OVERSIZED_GROUP_STORE, BINARY_SUFFIX);
	}

	public static final String BASENAME_OVERSIZED_TEMP_STORE = "oversizedTemp";
	
	public static BlockSinkSourceFactory getOversizedTempFactory(BatchJob job) {
		String wd = getWorkingDir(job);
		return new BlockSinkSourceFactory(wd, BASENAME_OVERSIZED_TEMP_STORE, BINARY_SUFFIX);
	}

	public static SuffixTreeSink getSuffixTreeSink(BatchJob job) {
		String path = getTreeFilePath(job);
		return new SuffixTreeSink(path);
	}

	public static final String BASENAME_BIG_BLOCK_STORE = "bigBlocks";

	public static BlockSinkSourceFactory getBigBlocksSinkSourceFactory(
			BatchJob job) {
		String wd = getWorkingDir(job);
		return new BlockSinkSourceFactory(wd, BASENAME_BIG_BLOCK_STORE, BINARY_SUFFIX);
	}

	public static final String BASENAME_TEMP_BLOCK_STORE = "tempBlocks";

	public static BlockSinkSourceFactory getTempBlocksSinkSourceFactory(
			BatchJob job) {
		String wd = getWorkingDir(job);
		return new BlockSinkSourceFactory(wd, BASENAME_TEMP_BLOCK_STORE, BINARY_SUFFIX);
	}

	public static final String BASENAME_CHUNKROW_STORE = "chunkrow";

	public static ChunkRecordIDSinkSourceFactory getChunkIDFactory(BatchJob job) {
		String wd = getWorkingDir(job);
		return new ChunkRecordIDSinkSourceFactory(wd, BASENAME_CHUNKROW_STORE, BINARY_SUFFIX);
	}

	public static IDTreeSetSource getTreeSetSource(BatchJob job) {
		String path = getTreeFilePath(job);
		SuffixTreeSource sSource = new SuffixTreeSource(path);
		IDTreeSetSource source = new IDTreeSetSource(sSource);
		return source;
	}
	
	public static final String FILENAME_TREE_STORE = "trees.txt";

	protected static String getTreeFilePath(BatchJob job) {
		String wd = getWorkingDir(job);
		assert wd.endsWith(FILE_SEPARATOR);
		String retVal = wd + FILENAME_TREE_STORE;
		return retVal;
	}

	public static final String BASENAME_COMPARE_TREE_STORE = "compareTree";

	public static ComparisonTreeSinkSourceFactory getComparisonTreeFactory(
			BatchJob job, RECORD_ID_TYPE stageType) {
		String wd = getWorkingDir(job);
		return new ComparisonTreeSinkSourceFactory(wd,
				BASENAME_COMPARE_TREE_STORE, TEXT_SUFFIX, stageType);
	}

	public static final String BASENAME_COMPARE_TREE_GROUP_STORE = "compareTreeGroup";

	/**
	 * This is used by the parallelization code. It creates many tree files for
	 * each chunk.
	 */
	public static ComparisonTreeGroupSinkSourceFactory getComparisonTreeGroupFactory(
			BatchJob job, RECORD_ID_TYPE stageType, int num) {
		String wd = getWorkingDir(job);
		return new ComparisonTreeGroupSinkSourceFactory(wd,
				BASENAME_COMPARE_TREE_GROUP_STORE, TEXT_SUFFIX, num, stageType);
	}

	public static final String BASENAME_COMPARE_ARRAY_STORE = "compareArray_O";

	public static ComparisonArraySinkSourceFactory getComparisonArrayFactoryOS(
			BatchJob job) {
		String wd = getWorkingDir(job);
		return new ComparisonArraySinkSourceFactory(wd, BASENAME_COMPARE_ARRAY_STORE, BINARY_SUFFIX);
	}

	public static final String BASENAME_COMPARE_ARRAY_GROUP = "compareArrayGroup_O";

	/**
	 * This is used by the parallelization code. It creates many array files for
	 * each chunk.
	 */
	public static ComparisonArrayGroupSinkSourceFactory getComparisonArrayGroupFactoryOS(
			BatchJob job, int num) {
		String wd = getWorkingDir(job);
		return new ComparisonArrayGroupSinkSourceFactory(wd, BASENAME_COMPARE_ARRAY_GROUP, BINARY_SUFFIX,
				num);
	}

	public static final String BASENAME_MATCHCHUNK_STORE = "matchchunk";

	/**
	 * This gets the match result sink for each chunk of the Matcher Bean.
	 * 
	 */
	public static MatchRecord2SinkSourceFactory getMatchChunkFactory(
			BatchJob job) {
		String wd = getWorkingDir(job);
		return new MatchRecord2SinkSourceFactory(wd, BASENAME_MATCHCHUNK_STORE, "txt");
	}

	public static final String BASENAME_MATCH_TEMP_STORE = "matchtemp";

	public static MatchRecord2SinkSourceFactory getMatchTempFactory(BatchJob job) {
		String wd = getWorkingDir(job);
		return new MatchRecord2SinkSourceFactory(wd, BASENAME_MATCH_TEMP_STORE, "txt");
	}

	public static final String BASENAME_MATCH_TEMP_STORE_INDEXED = "matchtemp_";

	public static MatchRecord2SinkSourceFactory getMatchTempFactory(
			BatchJob job, int i) {
		String wd = getWorkingDir(job);
		String str = Integer.toString(i);
		String fileNameIndexed = BASENAME_MATCH_TEMP_STORE_INDEXED + str  + "_";
		return new MatchRecord2SinkSourceFactory(wd, fileNameIndexed, "txt");
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
	public static IMatchRecord2Source getCompositeMatchSource(BatchJob job) {
		String fileName = getCompositeMatchFileName(job);
		return new MatchRecord2CompositeSource(fileName, TEXT_SUFFIX);
	}
	
	public static final String BASENAME_MATCH_STORE_INDEXED = "match_";

	protected static String getCompositeMatchFileName(BatchJob job) {
		String wd = getWorkingDir(job);
		assert wd.endsWith(FILE_SEPARATOR);
		String id = formatJobId(job.getId());
		String retVal = wd + BASENAME_MATCH_STORE_INDEXED + id;
		return retVal;
	}

	public static final String BASENAME_TWOMATCH_STORE = "twomatch";

	public static MatchRecord2SinkSourceFactory getSet2MatchFactory(BatchJob job) {
		String wd = getWorkingDir(job);
		return new MatchRecord2SinkSourceFactory(wd, BASENAME_TWOMATCH_STORE, TEXT_SUFFIX);
	}

	public static IMatchRecord2Sink getCompositeTransMatchSink(BatchJob job,
			long id) {
		String fileName = getCompositeTransMatchFileName(job);
		return new MatchRecord2CompositeSink(fileName, TEXT_SUFFIX, MAX_FILE_SIZE);
	}

	public static IMatchRecord2Source getCompositeTransMatchSource(BatchJob job) {
		String fileName = getCompositeTransMatchFileName(job);
		return new MatchRecord2CompositeSource(fileName, TEXT_SUFFIX);
	}

	public static final String BASENAME_TRANSMATCH_STORE_INDEXED = "transMatch_";

	protected static String getCompositeTransMatchFileName(BatchJob job) {
		String wd = getWorkingDir(job);
		assert wd.endsWith(FILE_SEPARATOR);
		String id = formatJobId(job.getId());
		String retVal = wd + BASENAME_TRANSMATCH_STORE_INDEXED + id;
		return retVal;
	}

	/**
	 * This method removes the temporary storage directory used by this job id.
	 * 
	 * @return boolean - true means it was a success
	 */
	public static boolean removeTempDir(BatchJob job) {
		File f = job.getWorkingDirectory();
		File[] subs = f.listFiles();
		for (int i = 0; i < subs.length; i++) {
			subs[i].delete();
		}
		return f.delete();
	}

	/**
	 * Block factory for transitivity
	 */
	public static BlockSinkSourceFactory getTransitivityBlockFactory(
			BatchJob job) {
		String wd = getWorkingDir(job);
		return new BlockSinkSourceFactory(wd, "transBlocks", "dat");
	}

	private OabaFileUtils() {
	}

}
