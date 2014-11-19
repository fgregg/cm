package com.choicemaker.cmit.utils;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import com.choicemaker.cm.io.blocking.automated.AbaSettings;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaBatchJobProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettings;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.TransitivityJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbaSettingsBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchJobBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchParametersBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.DefaultServerConfigurationBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.DefaultSettingsBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaBatchJobProcessingBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaSettingsBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.TransitivityJobBean;

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

	private Set<BatchJob> batchJobs = new LinkedHashSet<>();
	private Set<TransitivityJob> transitivityJobs = new LinkedHashSet<>();
	private Set<BatchParameters> batchParameters = new LinkedHashSet<>();
	private Set<OabaBatchJobProcessing> oabaProcessing = new LinkedHashSet<>();
	private Set<ServerConfiguration> serverConfigs = new LinkedHashSet<>();
	private Set<DefaultServerConfigurationBean> defaultConfigs =
		new LinkedHashSet<>();
	private Set<AbaSettings> abaSettings = new LinkedHashSet<>();
	private Set<OabaSettings> oabaSettings = new LinkedHashSet<>();
	private Set<DefaultSettingsBean> defaultSettings = new LinkedHashSet<>();

	public void add(BatchJob job) {
		if (job != null) {
			batchJobs.add(job);
		}
	}

	public void add(TransitivityJob job) {
		if (job != null) {
			transitivityJobs.add(job);
		}
	}

	public void add(BatchParameters params) {
		if (params != null) {
			batchParameters.add(params);
		}
	}

	public void add(OabaBatchJobProcessing p) {
		if (p != null) {
			oabaProcessing.add(p);
		}
	}

	public void add(ServerConfiguration sc) {
		if (sc != null) {
			serverConfigs.add(sc);
		}
	}

	public void add(DefaultServerConfigurationBean dscb) {
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

	public void add(DefaultSettingsBean dsb) {
		if (dsb != null) {
			defaultSettings.add(dsb);
		}
	}

	public boolean contains(BatchJob job) {
		boolean retVal = false;
		if (job != null) {
			retVal = batchJobs.contains(job);
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

	public boolean contains(BatchParameters params) {
		boolean retVal = false;
		if (params != null) {
			retVal = batchParameters.contains(params);
		}
		return retVal;
	}

	public boolean contains(OabaBatchJobProcessing p) {
		boolean retVal = false;
		if (p != null) {
			retVal = batchParameters.contains(p);
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

	public boolean contains(DefaultServerConfigurationBean dscb) {
		boolean retVal = false;
		if (dscb != null) {
			retVal = batchJobs.contains(dscb);
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

	public boolean contains(DefaultSettingsBean dsb) {
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
		for (BatchJob job : batchJobs) {
			if (BatchJobBean.isPersistent(job)) {
				boolean usingUtx = false;
				if (utx != null) {
					utx.begin();
					usingUtx = true;
				}
				BatchJobBean refresh = em.find(BatchJobBean.class, job.getId());
				if (refresh != null) {
					em.merge(refresh);
					boolean isManaged = em.contains(refresh);
					if (!isManaged) {
						logger.warning("BatchJob " + refresh.getId()
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
			if (TransitivityJobBean.isPersistent(job)) {
				boolean usingUtx = false;
				if (utx != null) {
					utx.begin();
					usingUtx = true;
				}
				TransitivityJobBean refresh =
					em.find(TransitivityJobBean.class, job.getId());
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
		for (BatchParameters params : batchParameters) {
			if (BatchParametersBean.isPersistent(params)) {
				boolean usingUtx = false;
				if (utx != null) {
					utx.begin();
					usingUtx = true;
				}
				BatchParametersBean refresh =
					em.find(BatchParametersBean.class, params.getId());
				if (refresh != null) {
					em.merge(refresh);
					boolean isManaged = em.contains(refresh);
					if (!isManaged) {
						logger.warning("BatchParameters " + refresh.getId()
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
		for (OabaBatchJobProcessing p : oabaProcessing) {
			if (OabaBatchJobProcessingBean.isPersistent(p)) {
				boolean usingUtx = false;
				if (utx != null) {
					utx.begin();
					usingUtx = true;
				}
				OabaBatchJobProcessingBean refresh =
					em.find(OabaBatchJobProcessingBean.class, p.getId());
				if (refresh != null) {
					em.merge(refresh);
					boolean isManaged = em.contains(refresh);
					if (!isManaged) {
						logger.warning("OabaBatchJobProcessing "
								+ refresh.getId() + " is not managed");
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
			if (ServerConfigurationBean.isPersistent(sc)) {
				boolean usingUtx = false;
				if (utx != null) {
					utx.begin();
					usingUtx = true;
				}
				ServerConfigurationBean refresh =
					em.find(ServerConfigurationBean.class, sc.getId());
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
		for (DefaultServerConfigurationBean dscb : defaultConfigs) {
			boolean usingUtx = false;
			if (utx != null) {
				utx.begin();
				usingUtx = true;
			}
			DefaultServerConfigurationBean refresh =
				em.find(DefaultServerConfigurationBean.class,
						dscb.getHostName());
			if (refresh != null) {
				em.merge(refresh);
				boolean isManaged = em.contains(refresh);
				if (!isManaged) {
					logger.warning("Default server configuration " + refresh.getHostName()
							+ " is not managed");
				} else {
					em.remove(refresh);
				}
			}
			if (usingUtx) {
				utx.commit();
			}
		}
		for (AbaSettings aba : abaSettings) {
			if (AbaSettingsBean.isPersistent(aba)) {
				boolean usingUtx = false;
				if (utx != null) {
					utx.begin();
					usingUtx = true;
				}
				AbaSettingsBean refresh =
					em.find(AbaSettingsBean.class, aba.getId());
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
			if (OabaSettingsBean.isPersistent(oaba)) {
				boolean usingUtx = false;
				if (utx != null) {
					utx.begin();
					usingUtx = true;
				}
				OabaSettingsBean refresh =
					em.find(OabaSettingsBean.class, oaba.getId());
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
		for (DefaultSettingsBean dscb : defaultSettings) {
			boolean usingUtx = false;
			if (utx != null) {
				utx.begin();
				usingUtx = true;
			}
			DefaultSettingsBean refresh =
				em.find(DefaultSettingsBean.class,
						dscb.getPrimaryKey());
			if (refresh != null) {
				em.merge(refresh);
				boolean isManaged = em.contains(refresh);
				if (!isManaged) {
					logger.warning("Default settings " + refresh.getPrimaryKey()
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
}
