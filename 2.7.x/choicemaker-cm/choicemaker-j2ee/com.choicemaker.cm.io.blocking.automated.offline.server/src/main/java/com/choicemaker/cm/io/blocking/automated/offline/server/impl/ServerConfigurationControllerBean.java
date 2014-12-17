package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.MutableServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.util.SystemPropertyUtils;

@Stateless
public class ServerConfigurationControllerBean implements
		ServerConfigurationController {

	private static final Logger logger = Logger
			.getLogger(ServerConfigurationEntity.class.getName());

	protected static final String GENERIC_NAME_PREFIX = "GENERIC_";

	protected static final String UNKNOWN_HOSTNAME = "UKNOWN";
	
	public static final long INVALID_ID = 0;

	public static final int DEFAULT_MAX_CHUNK_SIZE = 1000000;

	public static final int DEFAULT_MAX_CHUNK_COUNT = 2000;

	public static int computeAvailableProcessors() {
		int retVal = Runtime.getRuntime().availableProcessors();
		return retVal;
	}

	public static String computeHostName() {
		// A hack to an unsolvable problem. See StackOverflow,
		// "How do I get the local hostname if unresolvable through DNS?"
		// http://links.rph.cx/1szjiIc
		String retVal = null;
		if (System.getProperty("os.name").startsWith("Windows")) {
			// Windows will always set the 'COMPUTERNAME' variable
			retVal = System.getenv("COMPUTERNAME");
		}
		if (retVal == null) {
			retVal = System.getenv("HOSTNAME");
		}
		if (retVal == null) {
			try {
				InetAddress localhost = java.net.InetAddress.getLocalHost();
				retVal = localhost.getHostName();
			} catch (UnknownHostException e) {
				assert retVal == null;
				logger.warning(e.toString());
			}
		}
		if (retVal == null) {
			retVal = "UNKNOWN_HOSTNAME";
		}
		assert retVal != null;
		return retVal;
	}

	public static String computeUniqueGenericName() {
		String retVal = GENERIC_NAME_PREFIX + UUID.randomUUID().toString();
		return retVal;
	}
	
	public static File computeGenericLocation() {
		String home = System.getProperty(SystemPropertyUtils.USER_HOME);
		File retVal = new File(home);
		return retVal;
	}

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@EJB
	private OabaJobControllerBean jobController;

	@Override
	public ServerConfiguration find(long id) {
		ServerConfigurationEntity retVal =
			em.find(ServerConfigurationEntity.class, id);
		return retVal;
	}

	@Override
	public ServerConfiguration findServerConfigurationByName(String configName) {
		Query query =
			em.createNamedQuery(ServerConfigurationJPA.QN_SERVERCONFIG_FIND_BY_NAME);
		query.setParameter(
				ServerConfigurationJPA.PN_SERVERCONFIG_FIND_BY_NAME_P1,
				configName);
		@SuppressWarnings("unchecked")
		List<ServerConfigurationEntity> beans = query.getResultList();

		ServerConfiguration retVal = null;
		if (beans.size() > 1) {
			throw new IllegalStateException("non-unique configuration name: "
					+ configName);
		} else if (beans.size() == 1) {
			retVal = beans.get(0);
		} else {
			assert beans.size() == 0;
			assert retVal == null;
		}

		return retVal;
	}

	@Override
	public List<ServerConfiguration> findAllServerConfigurations() {
		Query query =
			em.createNamedQuery(ServerConfigurationJPA.QN_SERVERCONFIG_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<ServerConfigurationEntity> beans = query.getResultList();
		List<ServerConfiguration> retVal = new LinkedList<>();
		if (beans != null) {
			retVal.addAll(beans);
		}
		return retVal;
	}

	@Override
	public List<ServerConfiguration> findServerConfigurationsByHostName(
			String hostName) {
		return findServerConfigurationsByHostName(hostName, false);
	}

	@Override
	public List<ServerConfiguration> findServerConfigurationsByHostName(
			String hostName, boolean strict) {
		List<ServerConfiguration> retVal =
			findServerConfigurationsByHostNameStrict(hostName);
		if (strict == false) {
			retVal.addAll(findServerConfigurationsForAnyHost());
		}
		return retVal;
	}

	protected List<ServerConfiguration> findServerConfigurationsByHostNameStrict(
			String hostName) {
		Query query =
			em.createNamedQuery(ServerConfigurationJPA.QN_SERVERCONFIG_FIND_BY_HOSTNAME);
		query.setParameter(
				ServerConfigurationJPA.PN_SERVERCONFIG_FIND_BY_HOSTNAME_P1,
				hostName);
		@SuppressWarnings("unchecked")
		List<ServerConfigurationEntity> beans = query.getResultList();
		List<ServerConfiguration> retVal = new LinkedList<>();
		if (beans != null) {
			retVal.addAll(beans);
		}
		return retVal;
	}

	protected List<ServerConfiguration> findServerConfigurationsForAnyHost() {
		return findServerConfigurationsByHostNameStrict(ServerConfiguration.ANY_HOST);
	}

	@Override
	public ServerConfiguration findServerConfigurationByJobId(long jobId) {
		ServerConfiguration retVal = null;
		OabaJob oabaJob = jobController.findOabaJob(jobId);
		if (oabaJob != null) {
			long serverId = oabaJob.getServerId();
			retVal = find(serverId);
		}
		return retVal;
	}

	@Override
	public MutableServerConfiguration computeGenericConfiguration() {
		MutableServerConfiguration retVal = new ServerConfigurationEntity();
		retVal.setConfigurationName(computeUniqueGenericName());
		retVal.setHostName(computeHostName());
		retVal.setMaxChoiceMakerThreads(computeAvailableProcessors());
		retVal.setMaxOabaChunkFileCount(DEFAULT_MAX_CHUNK_COUNT);
		retVal.setMaxOabaChunkFileRecords(DEFAULT_MAX_CHUNK_SIZE);
		retVal.setWorkingDirectoryLocation(computeGenericLocation());
		return retVal;
	}

	@Override
	public MutableServerConfiguration clone(ServerConfiguration sc) {
		MutableServerConfiguration retVal = new ServerConfigurationEntity(sc);
		return retVal;
	}

	@Override
	public ServerConfiguration save(ServerConfiguration sc)
			throws ServerConfigurationException {
		if (sc == null) {
			throw new IllegalArgumentException("null configuration");
		}

		ServerConfiguration retVal = null;

		ServerConfigurationEntity scb = null;
		if (!(sc instanceof ServerConfigurationEntity)) {
			scb = new ServerConfigurationEntity(sc);
		} else {
			scb = (ServerConfigurationEntity) sc;
		}
		assert scb != null;

		final String name = sc.getName();
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"null or blank configuration name");
		}
		ServerConfiguration duplicate = findServerConfigurationByName(name);
		if (duplicate != null) {
			assert duplicate instanceof ServerConfigurationEntity;
			if (scb.equalsIgnoreIdUuid(duplicate)) {
				retVal = duplicate;
			} else {
				// Beans have the same name but other fields are different
				assert sc.getName().equals(duplicate.getName());
				String msg = "Duplicate server name: " + sc.getName();
				throw new ServerConfigurationException(msg);
			}
		}

		if (retVal == null) {
			em.persist(scb);
			retVal = scb;
		}

		assert retVal != null;
		return retVal;
	}

	@Override
	public ServerConfiguration setDefaultConfiguration(String host,
			ServerConfiguration sc) {
		if (host == null) {
			throw new IllegalArgumentException("null host name");
		}
		if (sc == null) {
			throw new IllegalArgumentException("null configuration");
		}
		if (!host.equals(sc.getHostName())
				&& !ServerConfiguration.ANY_HOST.equals(sc.getHostName())) {
			String msg =
				"Host name '"
						+ host
						+ "' is inconsistent with the configuration host name '"
						+ sc.getHostName() + "'";
			throw new IllegalArgumentException(msg);
		}

		ServerConfiguration retVal = null;
		DefaultServerConfigurationEntity old =
			em.find(DefaultServerConfigurationEntity.class, host);
		if (old != null) {
			long id = old.getServerConfigurationId();
			retVal = em.find(ServerConfigurationEntity.class, id);
			if (retVal == null) {
				throw new IllegalStateException(
						"missing server configuration: " + id);
			}
		}

		DefaultServerConfigurationEntity dsc =
			new DefaultServerConfigurationEntity(host, sc.getId());
		em.persist(dsc);

		return retVal;
	}

	@Override
	public ServerConfiguration getDefaultConfiguration(String hostName) {
		return getDefaultConfiguration(hostName, true);
	}

	@Override
	public ServerConfiguration getDefaultConfiguration(String host,
			boolean computeFallback) {
		if (host == null) {
			throw new IllegalArgumentException("null host name");
		}
		host = host.trim();

		ServerConfiguration retVal = null;
		DefaultServerConfigurationEntity dscb =
			em.find(DefaultServerConfigurationEntity.class, host);
		if (dscb != null) {
			long id = dscb.getServerConfigurationId();
			retVal = em.find(ServerConfigurationEntity.class, id);
			if (retVal == null) {
				throw new IllegalStateException(
						"missing server configuration: " + id);
			}
		}
		if (retVal == null) {
			List<ServerConfiguration> configs =
				findServerConfigurationsByHostName(host);
			if (configs.size() == 1) {
				retVal = configs.get(0);
			}
		}
		if (retVal == null && computeFallback) {
			MutableServerConfiguration mutable = computeGenericConfiguration();
			mutable.setHostName(host);
			try {
				retVal = save(mutable);
			} catch (ServerConfigurationException e) {
				// The mutable instance is created with a unique name,
				// so a duplicate name exception should never occur
				new IllegalStateException(e.getMessage());
			}
			assert retVal.getId() != ServerConfigurationEntity.NON_PERSISTENT_ID;
			setDefaultConfiguration(host,mutable);
		}
		
		if (computeFallback) {
			assert retVal != null;
		}
		return retVal;
	}

	@Override
	public List<DefaultServerConfigurationEntity> findAllDefaultServerConfigurations() {
		Query query =
			em.createNamedQuery(DefaultServerConfigurationJPA.QN_DSC_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<DefaultServerConfigurationEntity> retVal = query.getResultList();
		return retVal;
	}

}
