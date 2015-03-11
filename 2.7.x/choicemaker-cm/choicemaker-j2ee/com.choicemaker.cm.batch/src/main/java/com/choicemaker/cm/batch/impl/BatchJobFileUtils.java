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
package com.choicemaker.cm.batch.impl;

import java.io.File;
import java.io.IOException;
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
import com.choicemaker.util.SystemPropertyUtils;

public class BatchJobFileUtils {

	public static final String BINARY_SUFFIX = "dat";

	public static final String DEFAULT_PREFIX = "job";

	public static final String FILE_SEPARATOR = System
			.getProperty(SystemPropertyUtils.FILE_SEPARATOR);

	public static final String FMT = "00000";

	public static final long MAX_FILE_SIZE = 1500000000;

	public static final int ROLLOVER = 100000;

	public static final String TEMP_WORKING_DIR_PREFIX = "BatchJob_";

	public static final String TEXT_SUFFIX = "txt";

	/**
	 * Restricted permissions since the working directory may contain
	 * confidential or sensitive information.
	 */
	public static final String WORKING_DIR_POSIX_PERMISSIONS = "rwx------";

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

	public static String formatJobId(long jobId) {
		long truncated = Math.abs(jobId) % ROLLOVER;
		String retVal = new DecimalFormat(FMT).format(truncated);
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

	protected BatchJobFileUtils() {
		super();
	}

}