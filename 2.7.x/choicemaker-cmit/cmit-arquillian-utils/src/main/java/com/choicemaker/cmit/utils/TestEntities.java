package com.choicemaker.cmit.utils;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaBatchJobProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.TransitivityJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchJobBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchParametersBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaBatchJobProcessingBean;
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

	public void removePersistentObjects(EntityManager em) /* throws Exception */{
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
						logger.warning("BatchJob " + refresh.getId() + " is not managed");
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
						logger.warning("TransitivityJob " + refresh.getId() + " is not managed");
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
						logger.warning("BatchParameters " + refresh.getId() + " is not managed");
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
						logger.warning("OabaBatchJobProcessing " + refresh.getId() + " is not managed");
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
}
