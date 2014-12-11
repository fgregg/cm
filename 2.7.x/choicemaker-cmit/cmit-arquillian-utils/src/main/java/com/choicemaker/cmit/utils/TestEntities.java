package com.choicemaker.cmit.utils;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import com.choicemaker.cm.args.AbaSettings;
import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbaSettingsEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.DefaultServerConfigurationEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.DefaultSettingsEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaSettingsEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationEntity;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJob;
import com.choicemaker.cm.transitivity.server.impl.TransitivityJobEntity;

/**
 * Lists of objects created during a test. Provides a convenient way of cleaning
 * up persistent objects after a test is finished. This class is not
 * thread-safe, so it should be used only within the scope of a method.
 * 
 * @author rphall
 */
public class TestEntities {

	private static final Logger logger = Logger.getLogger(TestEntities.class
			.getName());

	private Set<OabaJob> oabaJobs = new LinkedHashSet<>();
	private Set<TransitivityJob> transitivityJobs = new LinkedHashSet<>();
	private Set<OabaParameters> oabaParameters = new LinkedHashSet<>();
	private Set<OabaJobProcessing> oabaProcessing = new LinkedHashSet<>();
	private Set<ServerConfiguration> serverConfigs = new LinkedHashSet<>();
	private Set<DefaultServerConfigurationEntity> defaultConfigs =
		new LinkedHashSet<>();
	private Set<AbaSettings> abaSettings = new LinkedHashSet<>();
	private Set<OabaSettings> oabaSettings = new LinkedHashSet<>();
	private Set<DefaultSettingsEntity> defaultSettings = new LinkedHashSet<>();

	public void add(OabaJob job) {
		if (job != null) {
			oabaJobs.add(job);
		}
	}

	public void add(TransitivityJob job) {
		if (job != null) {
			transitivityJobs.add(job);
		}
	}

	public void add(OabaParameters params) {
		if (params != null) {
			oabaParameters.add(params);
		}
	}

	public void add(OabaJobProcessing p) {
		if (p != null) {
			oabaProcessing.add(p);
		}
	}

	public void add(ServerConfiguration sc) {
		if (sc != null) {
			serverConfigs.add(sc);
		}
	}

	public void add(DefaultServerConfigurationEntity dscb) {
		if (dscb != null) {
			defaultConfigs.add(dscb);
		}
	}

	public void add(AbaSettings aba) {
		if (aba != null) {
			abaSettings.add(aba);
		}
	}

	public void add(OabaSettings oaba) {
		if (oaba != null) {
			oabaSettings.add(oaba);
		}
	}

	public void add(DefaultSettingsEntity dsb) {
		if (dsb != null) {
			defaultSettings.add(dsb);
		}
	}

	public boolean contains(BatchJob job) {
		boolean retVal = false;
		if (job != null) {
			retVal = oabaJobs.contains(job);
		}
		return retVal;
	}

	public boolean contains(TransitivityJob job) {
		boolean retVal = false;
		if (job != null) {
			retVal = transitivityJobs.contains(job);
		}
		return retVal;
	}

	public boolean contains(OabaParameters params) {
		boolean retVal = false;
		if (params != null) {
			retVal = oabaParameters.contains(params);
		}
		return retVal;
	}

	public boolean contains(OabaJobProcessing p) {
		boolean retVal = false;
		if (p != null) {
			retVal = oabaParameters.contains(p);
		}
		return retVal;
	}

	public boolean contains(ServerConfiguration sc) {
		boolean retVal = false;
		if (sc != null) {
			retVal = serverConfigs.contains(sc);
		}
		return retVal;
	}

	public boolean contains(DefaultServerConfigurationEntity dscb) {
		boolean retVal = false;
		if (dscb != null) {
			retVal = oabaJobs.contains(dscb);
		}
		return retVal;
	}

	public boolean contains(AbaSettings aba) {
		boolean retVal = false;
		if (aba != null) {
			retVal = abaSettings.contains(aba);
		}
		return retVal;
	}

	public boolean contains(OabaSettings oaba) {
		boolean retVal = false;
		if (oaba != null) {
			retVal = oabaSettings.contains(oaba);
		}
		return retVal;
	}

	public boolean contains(DefaultSettingsEntity dsb) {
		boolean retVal = false;
		if (dsb != null) {
			retVal = defaultSettings.contains(dsb);
		}
		return retVal;
	}

	public void removePersistentObjects(EntityManager em) {
		try {
			removePersistentObjects(em, null);
		} catch (Exception e) {
			logger.severe(e.toString());
			throw new RuntimeException(e.toString());
		}
	}

	public void removePersistentObjects(EntityManager em, UserTransaction utx)
			throws Exception {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		for (BatchJob job : oabaJobs) {
			if (OabaJobEntity.isPersistent(job)) {
				boolean usingUtx = false;
				if (utx != null) {
					utx.begin();
					usingUtx = true;
				}
				OabaJobEntity refresh =
					em.find(OabaJobEntity.class, job.getId());
				if (refresh != null) {
					em.merge(refresh);
					boolean isManaged = em.contains(refresh);
					if (!isManaged) {
						logger.warning("OabaJob " + refresh.getId()
								+ " is not managed");
					} else {
						em.remove(refresh);
					}
				}
				if (usingUtx) {
					utx.commit();
				}
			}
		}
		for (TransitivityJob job : transitivityJobs) {
			if (TransitivityJobEntity.isPersistent(job)) {
				boolean usingUtx = false;
				if (utx != null) {
					utx.begin();
					usingUtx = true;
				}
				TransitivityJobEntity refresh =
					em.find(TransitivityJobEntity.class, job.getId());
				if (refresh != null) {
					em.merge(refresh);
					boolean isManaged = em.contains(refresh);
					if (!isManaged) {
						logger.warning("TransitivityJob " + refresh.getId()
								+ " is not managed");
					} else {
						em.remove(refresh);
					}
				}
				if (usingUtx) {
					utx.commit();
				}
			}
		}
		for (OabaParameters params : oabaParameters) {
			if (OabaParametersEntity.isPersistent(params)) {
				boolean usingUtx = false;
				if (utx != null) {
					utx.begin();
					usingUtx = true;
				}
				OabaParametersEntity refresh =
					em.find(OabaParametersEntity.class, params.getId());
				if (refresh != null) {
					em.merge(refresh);
					boolean isManaged = em.contains(refresh);
					if (!isManaged) {
						logger.warning("OabaParameters " + refresh.getId()
								+ " is not managed");
					} else {
						em.remove(refresh);
					}
				}
				if (usingUtx) {
					utx.commit();
				}
			}
		}
		for (OabaJobProcessing p : oabaProcessing) {
			if (OabaProcessingEntity.isPersistent(p)) {
				boolean usingUtx = false;
				if (utx != null) {
					utx.begin();
					usingUtx = true;
				}
				OabaProcessingEntity refresh =
					em.find(OabaProcessingEntity.class, p.getId());
				if (refresh != null) {
					em.merge(refresh);
					boolean isManaged = em.contains(refresh);
					if (!isManaged) {
						logger.warning("OabaJobProcessing " + refresh.getId()
								+ " is not managed");
					} else {
						em.remove(refresh);
					}
				}
				if (usingUtx) {
					utx.commit();
				}
			}
		}
		for (ServerConfiguration sc : serverConfigs) {
			if (ServerConfigurationEntity.isPersistent(sc)) {
				boolean usingUtx = false;
				if (utx != null) {
					utx.begin();
					usingUtx = true;
				}
				ServerConfigurationEntity refresh =
					em.find(ServerConfigurationEntity.class, sc.getId());
				if (refresh != null) {
					em.merge(refresh);
					boolean isManaged = em.contains(refresh);
					if (!isManaged) {
						logger.warning("ServerConfiguration " + refresh.getId()
								+ " is not managed");
					} else {
						em.remove(refresh);
					}
				}
				if (usingUtx) {
					utx.commit();
				}
			}
		}
		for (DefaultServerConfigurationEntity dscb : defaultConfigs) {
			boolean usingUtx = false;
			if (utx != null) {
				utx.begin();
				usingUtx = true;
			}
			DefaultServerConfigurationEntity refresh =
				em.find(DefaultServerConfigurationEntity.class,
						dscb.getHostName());
			if (refresh != null) {
				em.merge(refresh);
				boolean isManaged = em.contains(refresh);
				if (!isManaged) {
					logger.warning("Default server configuration "
							+ refresh.getHostName() + " is not managed");
				} else {
					em.remove(refresh);
				}
			}
			if (usingUtx) {
				utx.commit();
			}
		}
		for (AbaSettings aba : abaSettings) {
			if (AbaSettingsEntity.isPersistent(aba)) {
				boolean usingUtx = false;
				if (utx != null) {
					utx.begin();
					usingUtx = true;
				}
				AbaSettingsEntity refresh =
					em.find(AbaSettingsEntity.class, aba.getId());
				if (refresh != null) {
					em.merge(refresh);
					boolean isManaged = em.contains(refresh);
					if (!isManaged) {
						logger.warning("AbaSettings " + refresh.getId()
								+ " is not managed");
					} else {
						em.remove(refresh);
					}
				}
				if (usingUtx) {
					utx.commit();
				}
			}
		}
		for (OabaSettings oaba : oabaSettings) {
			if (OabaSettingsEntity.isPersistent(oaba)) {
				boolean usingUtx = false;
				if (utx != null) {
					utx.begin();
					usingUtx = true;
				}
				OabaSettingsEntity refresh =
					em.find(OabaSettingsEntity.class, oaba.getId());
				if (refresh != null) {
					em.merge(refresh);
					boolean isManaged = em.contains(refresh);
					if (!isManaged) {
						logger.warning("OabaSettings " + refresh.getId()
								+ " is not managed");
					} else {
						em.remove(refresh);
					}
				}
				if (usingUtx) {
					utx.commit();
				}
			}
		}
		for (DefaultSettingsEntity dscb : defaultSettings) {
			boolean usingUtx = false;
			if (utx != null) {
				utx.begin();
				usingUtx = true;
			}
			DefaultSettingsEntity refresh =
				em.find(DefaultSettingsEntity.class, dscb.getPrimaryKey());
			if (refresh != null) {
				em.merge(refresh);
				boolean isManaged = em.contains(refresh);
				if (!isManaged) {
					logger.warning("Default settings "
							+ refresh.getPrimaryKey() + " is not managed");
				} else {
					em.remove(refresh);
				}
			}
			if (usingUtx) {
				utx.commit();
			}
		}
	}
}
