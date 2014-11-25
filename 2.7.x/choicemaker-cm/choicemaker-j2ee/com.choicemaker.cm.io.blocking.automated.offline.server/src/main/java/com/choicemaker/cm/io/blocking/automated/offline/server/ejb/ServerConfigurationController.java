package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import java.util.List;

import com.choicemaker.cm.io.blocking.automated.offline.server.impl.DefaultServerConfigurationEntity;

/**
 * Manages a database of server configurations.
 * 
 * @author rphall
 *
 */
public interface ServerConfigurationController {

	ServerConfiguration find(long id);

	/**
	 * Finds a persistent server configuration by name.
	 * 
	 * <pre>
	 * findServerConfigurationsByHostName(false)
	 * </pre>
	 * 
	 * @return a non-null list, never empty
	 */
	ServerConfiguration findServerConfigurationByName(String configName);

	/**
	 * Finds all persistent server configurations.
	 * 
	 * @return a non-null list, never empty
	 */
	List<ServerConfiguration> findAllServerConfigurations();

	/**
	 * Finds all persistent server configurations for a given host name,
	 * including any marked for {@link ServerConfiguration#ANY_HOST}. Equivalent
	 * to
	 * 
	 * <pre>
	 * findServerConfigurationsByHostName(hostName, false)
	 * </pre>
	 * 
	 * @return a non-null list, never empty
	 */
	List<ServerConfiguration> findServerConfigurationsByHostName(String hostName);

	/**
	 * Finds all persistent server configurations for a given host name. If the
	 * <code>strict</code> is <code>true</code>, configurations specified for
	 * {@link ServerConfiguration#ANY_HOST} are excluded; otherwise they are
	 * included.
	 * 
	 * @param strict
	 *            if true, exclude configurations for
	 *            {@link ServerConfiguration#ANY_HOST}
	 * @return a non-null list, possibly empty
	 */
	List<ServerConfiguration> findServerConfigurationsByHostName(
			String hostName, boolean strict);

	// /**
	// * Finds all server configurations with fields matching the non-null or
	// * positive fields of the specified template. For example, the following
	// * usage is equivalent to
	// * <code>findServerConfigurationsByHostName("Fred", strict)</code>:
	// *
	// * <pre>
	// * ServerConfiguration template = new ServerConfiguration() {
	// * String getConfigurationName() { return null; }
	// * String getUUID() { return null; }
	// * String getHostName() { return "Fred"; }
	// * int getMaxChoiceMakerThreads() { return -1; }
	// * int getMaxOabaChunkFileRecords() { return -1; }
	// * int getMaxOabaChunkFileCount() { return -1; }
	// * File getWorkingDirectoryLocation() { return null; }
	// * }
	// * findServerConfigurationsByExample(template);
	// * </pre>
	// *
	// * @param strict
	// * @return
	// */
	// List<ServerConfiguration> findServerConfigurationsByExample(
	// ServerConfiguration template);

	/**
	 * Computes and returns a generic, non-persistent configuration which is
	 * probably suitable, but not optimal, for the current host. Each invocation
	 * will produce an instance with a distinct name and UUID.
	 */
	MutableServerConfiguration computeGenericConfiguration();

	/**
	 * Clones an existing ServerConfiguration as a mutable, non-persistent
	 * configuration and assigns it a random, unique name and a new UUID.
	 */
	MutableServerConfiguration clone(ServerConfiguration serverConfiguration);

	/**
	 * Saves a valid server configuration to the database.
	 * <ul>
	 * <li>If all the fields excluding the UUID match an existing configuration,
	 * the specified configuration is discarded and the existing configuration
	 * is returned.</li>
	 * <li>If the {@link ServerConfiguration#getName() name} of the specified
	 * configuration matches the name of an existing configuration, a
	 * DuplicateName exception is thrown.</li>
	 * <li>If the configuration is not valid -- that is, if any field is null or
	 * non-positive -- an IllegalArgument exception is thrown.</li>
	 * <li>Otherwise, the configuration is saved to the database and an
	 * immutable copy of its fields is returned.</li>
	 * </ul>
	 * Once a configuration is saved, it should be treated as immutable.
	 * 
	 * @throws ServerConfigurationException
	 *             if a server configuration that has the same configuration
	 *             name already exists in the database, and it differs on other
	 *             field values besides <code>id</code> and <code>uuid</code>
	 */
	ServerConfiguration save(ServerConfiguration configuration)
			throws ServerConfigurationException;

	/**
	 * Sets the default configuration for a particular host.
	 * 
	 * @return the previous value for the default configuration if a default was
	 *         explicitly set by a previous invocation of this method, or null
	 *         otherwise.
	 */
	ServerConfiguration setDefaultConfiguration(String host,
			ServerConfiguration configuration);

	/**
	 * Gets the default configuration for a particular host.
	 * <ol>
	 * <li>If default configuration for a host has been set previously, returns
	 * this configuration</li>
	 * <li>If only one configuration exists for a host, including any
	 * configuration marked {@link ServerConfiguration#ANY_HOST ANY_HOST}, this
	 * configuration is returned</li>
	 * <li>If neither of the previous conditions hold, then a generic
	 * configuration is computed and returned.
	 * </ol>
	 * Equivalent to
	 * 
	 * <pre>
	 * getDefaultConfiguration(hostName, true)
	 * </pre>
	 */
	ServerConfiguration getDefaultConfiguration(String hostName);

	/**
	 * Gets the default configuration for a particular host.
	 * <ol>
	 * <li>If default configuration for a host has been set previously, returns
	 * this configuration</li>
	 * <li>If only one configuration exists for a host, including any
	 * configuration marked {@link ServerConfiguration#ANY_HOST ANY_HOST}, this
	 * configuration is returned</li>
	 * <li>If neither of the previous conditions hold, and if the
	 * <code>computeFallback</code> flag is true, then a generic configuration
	 * is computed and returned.
	 * <li>Otherwise returns null.
	 * </ol>
	 */
	ServerConfiguration getDefaultConfiguration(String hostName,
			boolean computeFallback);

	List<DefaultServerConfigurationEntity> findAllDefaultServerConfigurations();

	ServerConfiguration findServerConfigurationByJobId(long jobId);

}
