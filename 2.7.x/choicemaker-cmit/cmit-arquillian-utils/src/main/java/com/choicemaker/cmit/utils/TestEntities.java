package com.choicemaker.cmit.utils;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.EntityManager;

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

//	public int countAllBatchJobs() {
//		return batchJobs.size();
//	}
//
//	public int countPersistentBatchJobs() {
//		int count = 0;
//		for (BatchJob job : batchJobs) {
//			if (BatchJobBean.isPersistent(job)) {
//				++count;
//			}
//		}
//		return count;
//	}
//
//	public int countPersistentTransitivityJobs() {
//		int count = 0;
//		for (TransitivityJob job : transitivityJobs) {
//			if (TransitivityJobBean.isPersistent(job)) {
//				++count;
//			}
//		}
//		return count;
//	}
//
//	public int countPersistentBatchParameters() {
//		int count = 0;
//		for (BatchParameters job : batchParameters) {
//			if (BatchParametersBean.isPersistent(job)) {
//				++count;
//			}
//		}
//		return count;
//	}

	public void removePersistentObjects(EntityManager em) {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		for (BatchJob job : batchJobs) {
			if (BatchJobBean.isPersistent(job)) {
				BatchJobBean refresh = em.find(BatchJobBean.class, job.getId());
				if (refresh != null) {
					em.remove(refresh);
				}
			}
		}
		for (TransitivityJob job : transitivityJobs) {
			if (TransitivityJobBean.isPersistent(job)) {
				TransitivityJobBean refresh = em.find(TransitivityJobBean.class, job.getId());
				if (refresh != null) {
					em.remove(refresh);
				}
			}
		}
		for (BatchParameters params : batchParameters) {
			if (BatchParametersBean.isPersistent(params)) {
				BatchParametersBean refresh = em.find(BatchParametersBean.class, params.getId());
				if (refresh != null) {
					em.remove(refresh);
				}
			}
		}
		for (OabaBatchJobProcessing p : oabaProcessing) {
			OabaBatchJobProcessingBean refresh = em.find(OabaBatchJobProcessingBean.class, p.getId());
			if (refresh != null) {
				em.remove(refresh);
			}
		}
	}

}
