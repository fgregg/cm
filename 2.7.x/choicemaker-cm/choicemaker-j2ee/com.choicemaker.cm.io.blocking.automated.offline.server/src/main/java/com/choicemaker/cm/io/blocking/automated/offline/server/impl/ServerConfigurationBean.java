package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationJPA.CN_CONFIGNAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationJPA.CN_FILE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationJPA.CN_HOSTNAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationJPA.CN_ID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationJPA.CN_MAXCHUNKCOUNT;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationJPA.CN_MAXCHUNKSIZE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationJPA.CN_MAXTHREADS;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationJPA.CN_UUID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationJPA.ID_GENERATOR_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationJPA.ID_GENERATOR_PK_COLUMN_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationJPA.ID_GENERATOR_PK_COLUMN_VALUE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationJPA.ID_GENERATOR_TABLE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationJPA.ID_GENERATOR_VALUE_COLUMN_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationJPA.JPQL_SERVERCONFIG_FIND_ALL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationJPA.JPQL_SERVERCONFIG_FIND_BY_HOSTNAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationJPA.JPQL_SERVERCONFIG_FIND_BY_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationJPA.QN_SERVERCONFIG_FIND_ALL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationJPA.QN_SERVERCONFIG_FIND_BY_HOSTNAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationJPA.QN_SERVERCONFIG_FIND_BY_NAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationJPA.TABLE_NAME;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.logging.Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.MutableServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfiguration;

@NamedQueries({
		@NamedQuery(name = QN_SERVERCONFIG_FIND_ALL,
				query = JPQL_SERVERCONFIG_FIND_ALL),
		@NamedQuery(name = QN_SERVERCONFIG_FIND_BY_HOSTNAME,
				query = JPQL_SERVERCONFIG_FIND_BY_HOSTNAME),
		@NamedQuery(name = QN_SERVERCONFIG_FIND_BY_NAME,
				query = JPQL_SERVERCONFIG_FIND_BY_NAME),
//		@NamedQuery(name = QN_SERVERCONFIG_FIND_ANY_HOST,
//				query = JPQL_SERVERCONFIG_FIND_ANY_HOST)
		})
@Entity
@Table(/* schema = "CHOICEMAKER", */name = TABLE_NAME)
public class ServerConfigurationBean implements MutableServerConfiguration {

	private static final Logger logger = Logger
			.getLogger(ServerConfigurationBean.class.getName());
	
	public static long NON_PERSISTENT_ID = 0;

	// -- Instance data

	@Id
	@Column(name = CN_ID)
	@TableGenerator(name = ID_GENERATOR_NAME, table = ID_GENERATOR_TABLE,
			pkColumnName = ID_GENERATOR_PK_COLUMN_NAME,
			valueColumnName = ID_GENERATOR_VALUE_COLUMN_NAME,
			pkColumnValue = ID_GENERATOR_PK_COLUMN_VALUE)
	@GeneratedValue(strategy = GenerationType.TABLE,
			generator = ID_GENERATOR_NAME)
	private long id;

	@Column(name = CN_CONFIGNAME)
	private String name;

	@Column(name = CN_UUID)
	private final String uuid;

	@Column(name = CN_HOSTNAME)
	private String hostName;

	@Column(name = CN_MAXTHREADS)
	private int maxThreads;

	@Column(name = CN_MAXCHUNKSIZE)
	private int maxChunkSize;

	@Column(name = CN_MAXCHUNKCOUNT)
	private int maxChunkCount;

	@Column(name = CN_FILE)
	private String fileURI;

	public ServerConfigurationBean() {
		this.uuid = UUID.randomUUID().toString();
	}

	public ServerConfigurationBean(ServerConfiguration sc) {
		this.uuid = UUID.randomUUID().toString();
		this.name =
			ServerConfigurationManagerBean.computeUniqueGenericName();
		File f = sc.getWorkingDirectoryLocation();
		if (f == null) {
			throw new IllegalArgumentException("null working directory");
		} else {
			this.fileURI = f.toURI().toString();
		}
		this.hostName = sc.getHostName();
		this.maxChunkCount = sc.getMaxOabaChunkFileCount();
		this.maxChunkSize = sc.getMaxOabaChunkFileRecords();
		this.maxThreads = sc.getMaxChoiceMakerThreads();
	}
	
	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getUUID() {
		return uuid;
	}

	@Override
	public String getHostName() {
		return hostName;
	}

	@Override
	public int getMaxChoiceMakerThreads() {
		return maxThreads;
	}

	@Override
	public int getMaxOabaChunkFileRecords() {
		return maxChunkSize;
	}

	@Override
	public int getMaxOabaChunkFileCount() {
		return maxChunkCount;
	}

	@Override
	public File getWorkingDirectoryLocation() {
		File retVal = null;
		try {
			retVal = new File(new URI(fileURI));
		} catch (URISyntaxException e) {
			String msg =
				"Invalid file location: " + fileURI + ": " + e.toString();
			logger.severe(msg);
			new IllegalStateException(msg);
		}
		assert retVal != null;
		return retVal;
	}

	public boolean equalsIgnoreIdUuid(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ServerConfigurationBean other = (ServerConfigurationBean) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (fileURI == null) {
			if (other.fileURI != null) {
				return false;
			}
		} else if (!fileURI.equals(other.fileURI)) {
			return false;
		}
		if (hostName == null) {
			if (other.hostName != null) {
				return false;
			}
		} else if (!hostName.equals(other.hostName)) {
			return false;
		}
		if (maxChunkCount != other.maxChunkCount) {
			return false;
		}
		if (maxChunkSize != other.maxChunkSize) {
			return false;
		}
		if (maxThreads != other.maxThreads) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ServerConfigurationBean [id=" + id + ", name="
				+ name + ", uuid=" + uuid + "]";
	}

	@Override
	public void setConfigurationName(String name) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("null or blank name");
		}
		this.name = name.trim();
	}

	@Override
	public void setHostName(String name) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("null or blank name");
		}
		this.hostName = name.trim();
	}

	@Override
	public void setMaxChoiceMakerThreads(int value) {
		if (value < 0) {
			throw new IllegalArgumentException("negative thread limit");
		}
		this.maxThreads = value;
	}

	@Override
	public void setMaxOabaChunkFileRecords(int value) {
		if (value < 0) {
			throw new IllegalArgumentException("negative chunk size");
		}
		this.maxChunkSize = value;
	}

	@Override
	public void setMaxOabaChunkFileCount(int value) {
		if (value < 0) {
			throw new IllegalArgumentException("negative chunk count");
		}
		this.maxChunkCount = value;
	}

	@Override
	public void setWorkingDirectoryLocation(File location) {
		if (location == null) {
			throw new IllegalArgumentException("null location");
		}
		if (!location.exists()) {
			throw new IllegalArgumentException("location does not exist: "
					+ location);
		}
		if (!location.isDirectory()) {
			throw new IllegalArgumentException("location is not a directory: "
					+ location);
		}
		if (!location.canWrite()) {
			throw new IllegalArgumentException("location can not be written: "
					+ location);
		}
		if (!location.canRead()) {
			throw new IllegalArgumentException("location can not be read: "
					+ location);
		}
		this.fileURI = location.toURI().toString();
	}

}
