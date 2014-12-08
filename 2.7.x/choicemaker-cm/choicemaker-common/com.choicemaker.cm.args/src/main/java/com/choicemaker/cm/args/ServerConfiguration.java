package com.choicemaker.cm.args;

import java.io.File;

public interface ServerConfiguration {

	/**
	 * A special value for the {@link #getHostName() hostName} field that
	 * indicates a configuration is not specific to a particular host machine.
	 */
	String ANY_HOST = "**ANY HOST**";

	long getId();

	/**
	 * A memorable name for a configuration. A configuration name must be unique
	 * within the database used to store configuration information.
	 */
	String getName();

	/**
	 * A universally, unique identifier for a configuration, automatically
	 * assigned.
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
	 * Checks whether the URI returned by
	 * {@link #getWorkingDirectoryLocationUriString()} represents an exiting and
	 * valid directory. Since this class is persistent, the URI may be valid in
	 * the context in which it was first saved, but be invalid in a subsequent
	 * context when it is retrieved (for example, on a completely different
	 * host).
	 */
	boolean isWorkingDirectoryLocationValid();

	/**
	 * Returns the parent directory in which job-specific working directories
	 * may be created. This method is preferred over
	 * {@link #getWorkingDirectoryLocationUriString()} when the location must
	 * exist for an application to work successfully.
	 * 
	 * @return never null
	 * @throws IllegalStateException
	 *             if the location doesn't exist or isn't valid
	 */
	File getWorkingDirectoryLocation() throws IllegalStateException;

	/**
	 * Returns a String represent the URI of the parent directory in which
	 * job-specific working directories may be created. This method should never
	 * throw an exception, and it is therefore preferred over
	 * {@link #getWorkingDirectoryLocation()} in certain applications where the
	 * existence or validity of the location is not critical, such as equality
	 * checks between two server configurations.
	 * 
	 * @return possibly null
	 */
	String getWorkingDirectoryLocationUriString();

}
