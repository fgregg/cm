package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.DefaultServerConfigurationJPA.CN_HOSTNAME;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.DefaultServerConfigurationJPA.CN_SERVERCONFIG;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.DefaultServerConfigurationJPA.TABLE_NAME;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfiguration;

@Entity
@Table(/* schema = "CHOICEMAKER", */name = TABLE_NAME)
public class DefaultServerConfigurationBean {

	@Id
	@Column(name = CN_HOSTNAME)
	private final String hostName;

	@Column(name = CN_SERVERCONFIG)
	private long serverConfigurationId;

	protected DefaultServerConfigurationBean() {
		this.hostName = null;
		this.serverConfigurationId = -1;
	}
	
	public DefaultServerConfigurationBean(String hostName, long serverConfigId) {
		if (hostName == null || hostName.trim().isEmpty()) {
			throw new IllegalArgumentException("null or blank host name");
		}
		if (serverConfigId == ServerConfigurationBean.NON_PERSISTENT_ID) {
			throw new IllegalArgumentException("non-persistent configuration");
		}
		this.hostName = hostName.trim();
		this.serverConfigurationId = serverConfigId;
	}

	public DefaultServerConfigurationBean(ServerConfiguration sc) {
		this(sc.getHostName(), sc.getId());
	}

	public String getHostName() {
		return hostName;
	}

	public long getServerConfigurationId() {
		return serverConfigurationId;
	}

}
