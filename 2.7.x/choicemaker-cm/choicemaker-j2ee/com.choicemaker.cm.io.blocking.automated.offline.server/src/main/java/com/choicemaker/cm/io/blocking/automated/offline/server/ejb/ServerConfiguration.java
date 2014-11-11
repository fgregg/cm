package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import java.io.File;

public interface ServerConfiguration {

	/**
	 * A special value for the {@link #getHostName() hostName} field that
	 * indicates a configuration is not specific to a particular host machine.
	 */
	String ANY_HOST = "**ANY HOST**";

	long getId();
	
	/**
	 * A memorable name for a configuration. A configuration name must be
	 * unique within the database used to store configuration information.
	 */
	String getName();
	
	/**
	 * A universally, unique identifier for a configuration, automatically assigned.
	 */
	String getUUID();

	/** The host machine or logical domain to which a configuration applies */
	String getHostName();

	/** The maximum number of ChoiceMaker tasks that should be run in parallel */
	int getMaxChoiceMakerThreads();

	/** The maximum number of records in an OABA chunk file */
	int getMaxOabaChunkFileRecords();

	/** The maximum number of OABA chunk files */
	int getMaxOabaChunkFileCount();

	/**
	 * The parent directory in which job-specific working directories may be
	 * created
	 */
	File getWorkingDirectoryLocation();

}
