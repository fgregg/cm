/*
 * Copyright (c) 2001, 2009 ChoiceMaker Technologies, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ChoiceMaker Technologies, Inc. - initial API and implementation
 */
package com.choicemaker.cm.urm.ejb;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.urm.base.IRecordCollection;
import com.choicemaker.cm.urm.base.JobStatus;
import com.choicemaker.cm.urm.base.LinkCriteria;
import com.choicemaker.cm.urm.base.RefRecordCollection;
import com.choicemaker.cm.urm.config.AnalysisResultFormat;
import com.choicemaker.cm.urm.exceptions.ArgumentException;
import com.choicemaker.cm.urm.exceptions.CmRuntimeException;
import com.choicemaker.cm.urm.exceptions.ConfigException;
import com.choicemaker.cm.urm.exceptions.ModelException;
import com.choicemaker.cm.urm.exceptions.RecordCollectionException;

/**
 * @author emoussikaev
 * @version Revision: 2.5  Date: Jul 15, 2005 3:41:42 PM
 * @see
 */
@SuppressWarnings({"rawtypes"})
public class BatchMatchAnalyzerBean extends BatchMatchBaseBean {

	private static final long serialVersionUID = 1L;
	public static final long BMA_STEPS_NUMB = 3;
	public static final int BATCH_MATCH_STEP_INDEX = 0; 
	public static final int TRANS_OABA_STEP_INDEX = 1;
	public static final int TRANS_SERIAL_STEP_INDEX = 2;
	static {
		log = Logger.getLogger(BatchMatchAnalyzerBean.class.getName());
	}

	// @PersistenceContext (unitName = "oaba")
	private EntityManager em;

	// @EJB
	private OabaService batchQuery;

	// @EJB
//	private OperationalPropertyController propController;

	public BatchMatchAnalyzerBean() {
		super();
	}

	public long 	startMatchAndAnalysis(
										IRecordCollection qRs, 
										RefRecordCollection mRs,
										String modelName, 
										float differThreshold, 
										float matchThreshold,
										int maxSingle,
										LinkCriteria c,
										AnalysisResultFormat s,
										String externalId) 
					throws
							RecordCollectionException,
							ArgumentException,
							ConfigException,
							ModelException,
							CmRuntimeException, 
							RemoteException, ServerConfigurationException
	{
		log.fine("<< startMatching...");
		UrmJob uj = Single.getInst().createUrmJob(externalId);
		uj.setCurStepIndex(new Long(0));
		uj.setGroupMatchType(c.getGraphPropType().toString());
		uj.setSerializationType(s.toString());
		uj.setLowerThreshold(new Float(differThreshold));
		uj.setUpperThreshold(new Float(matchThreshold));
		uj.setModelName(modelName);
		long id = startBatchQueryService(
						qRs,
						mRs,
						modelName,
						differThreshold,
						matchThreshold,
						maxSingle,
						externalId,
		
						uj);
		uj.setCurStepJobId(new Long(id));					
		log.fine (">> startMatching");
		return uj.getId().longValue();		
	}

										
	
	public long 	startAnalysis(
										long  jobId,
										LinkCriteria c,
										AnalysisResultFormat s,
										String externalId) 
						throws
								ModelException, 	
								ConfigException,
								ArgumentException,
								CmRuntimeException, 
								RemoteException
	 {
		log.fine("<<startAnalysis");
		//TODO implement
		//TODO: check input parameters
//		try {
//			OabaService qs = Single.getInst().getBatchQueryService();			
//			BatchJobStatus batchJob = qs.getStatus(jobId);
//			if(!batchJob.getStatus().equals("COMPLETED"))
//				throw new 	ArgumentException("job doesn't have a complete status");
//			UrmJob uj = Single.getInst().createUrmJob(externalId, new Long(jobId));
//			uj.setGroupMatchType(c.getGraphPropType().toString());
//			uj.setSerializationType(s.toString());
//			uj.markAsMatching();
//					 	
//			TransitivityJobData data = new TransitivityJobData();
//			data.jobID = jobId;
//			data.stageModelName = modelName;
//			data.low = differThreshold;
//			data.high = matchThreshold;
//			data.runTransitivity = true;
//			sendToTransitivity (data);
//		} catch (RemoteException e) {
//			log.severe(e.toString());
//			throw new CmRuntimeException(e);
//		} catch (CreateException e) {
//			log.severe(e.toString());
//			throw new ConfigException(e);
//		} catch (NamingException e) {
//			log.severe(e.toString());
//			throw new ConfigException(e);
//		} catch (JMSException e) {
//			log.severe(e.toString());
//			throw new ConfigException(e);
//		} catch (FinderException e) {
//			log.severe(e.toString());
//			throw new ConfigException(e);
//		}
		log.fine (">>startAnalysis");
		return jobId;
	 }
	 
//	private void sendToTransitivity (TransitivityJobData d) throws ConfigException, CmRuntimeException  {
//		Queue queue = Single.getInst().getTransitivityMessageQueue();
//		Single.getInst().sendMessage(queue, d);
//		log.info ("send To StartTransitivityMDB ");
//	}

	/**
	 * Retrieves the status of the matching process with the give job ID.
	 * 
	 * @param jobID
	 *            Job ID.
	 * @return Job status.
	 * @throws RemoteException
	 */
	public JobStatus getJobStatus(long jobID) throws ArgumentException,
			ConfigException, CmRuntimeException, RemoteException {
		log.fine("<<getJobStatus");
		UrmJob urmJob = Single.getInst().findUrmJobById(jobID);
		JobStatus js = urmJob.getJobStatus();
//		FIXME STUBBED
//		if (!urmJob.isAbortRequested() && !urmJob.isStarted()) {
//			return js;
//		}
//		long stepJobId = urmJob.getCurStepJobId().longValue();
//		int stepIndex = (int) urmJob.getCurStepIndex().longValue();
//		String stepStatus = "";
//		if (stepJobId != JobStatus.UNDEFINED_ID) {
//			switch (stepIndex) {
//			case BATCH_MATCH_STEP_INDEX:
//				BatchJob bj =
//					Single.getInst().findBatchJobById(em, OabaJobEntity.class,
//							stepJobId);
//				stepStatus = bj.getStatus().name();
//				break;
//			case TRANS_OABA_STEP_INDEX:
//				BatchJob tj =
//					Single.getInst().findTransJobById(em, TransitivityJobEntity.class, stepJobId);
//				stepStatus = tj.getStatus().name();
//				break;
//			case TRANS_SERIAL_STEP_INDEX:
//				CmsJob cj = Single.getInst().findCmsJobById(stepJobId);
//				stepStatus = cj.getStatus();
//				break;
//			default:
//				log.info("unknown step index " + stepIndex);
//			}
//			js.setStepDescription(urmJob.getStepDescription() + " "
//					+ stepStatus);
//			if (urmJob.isAbortRequested()
//					&& stepStatus.equals(JobStatus.STATUS_ABORTED)) {
//				urmJob.markAsAborted();
//				urmJob.setStepDescription(js.getStepDescription());
//			}
//			if (urmJob.isStarted()
//					&& stepStatus.equals(JobStatus.STATUS_FAILED)) {
//				urmJob.markAsFailed();
//				urmJob.setStepDescription(js.getStepDescription());
//			}
//
//		}
//		log.fine(">>getJobStatus");
		return js;
	}

	/**
	 * Cleans serialized data related to the process with the give job ID (including the file with matching results).
	 * 
	 * @param   jobID		Job ID.
	 * @return  Job ID.
	 * @throws  RemoteException
	*/	
	public boolean					cleanJob (
										long urmJobId
									)
									throws	ArgumentException,
											CmRuntimeException,
											ConfigException, 
											RemoteException
		
	{
		try {
			UrmStepJob batchStep = Single.getInst().findStepJobByUrmAndIndex(urmJobId,BatchMatchAnalyzerBean.BATCH_MATCH_STEP_INDEX);					
			long batchJobId  = batchStep.getStepJobId().longValue();		
			log.fine("batch job jd = "+batchJobId);
			boolean ret = batchQuery.removeDir(batchJobId);
			Single.getInst().removeUrmJob(em, urmJobId);
			return ret;
		} catch (NamingException e) {
			log.severe(e.toString());
			throw new ConfigException(e.toString());
		} catch (CreateException e) {
			log.severe(e.toString());
			throw new ConfigException(e.toString());
		} catch (JMSException e) {
			log.severe(e.toString());
			throw new ConfigException(e.toString());
		} catch (FinderException e) {
			log.severe(e.toString());
			throw new CmRuntimeException(e.toString());
		}		
	}

		
	/**
	 * Copies the matching result  
	 * 
	 * @param   jobID		Job ID.
	 * @param	resRc		Record collection to serialize matching result.
	 * @throws  RemoteException
	 */	
	public void 	copyResult (long jobID,
								RefRecordCollection resRc)
					throws
							ModelException, 	
							RecordCollectionException,
							ConfigException,
							ArgumentException,
							CmRuntimeException, 
							RemoteException			{
		
		log.fine("<<copyResult");						
		// FIXME STUBBED
//		if(! (resRc instanceof TextRefRecordCollection) )
//			throw new ArgumentException("this implementation supports only text record collection copying");							
//		try {
//			UrmJob urmJob = Single.getInst().findUrmJobById(jobID);
//			String serialType = urmJob.getSerializationType();//.toLowerCase();
//			if( !urmJob.getStatus().equals(JobStatus.STATUS_COMPLETED) )
//				throw new ArgumentException("job "+jobID+" is not completed");
//			
//			UrmStepJob stepJob = Single.getInst().findStepJobByUrmAndIndex(jobID,TRANS_OABA_STEP_INDEX);
//			TransitivityJob trJob = Single.getInst().findTransJobById(em, TransitivityJobEntity.class, stepJob.getStepJobId().longValue());
//
//			final String cachedPairsFileName =
//				propController.getJobProperty(trJob,
//						PN_TRANSITIVITY_CACHED_PAIRS_FILE);
//			log.info("Cached transitivity pairs file: " + cachedPairsFileName);
//
//			String fileName =
//				cachedPairsFileName.substring(0,
//						cachedPairsFileName.lastIndexOf("."));
//			fileName = fileName + "trans_analysis";
//			String dirName;
//			int slashInd = fileName.lastIndexOf("\\");
//			if(slashInd ==-1)
//				slashInd = fileName.lastIndexOf("/");
//			if(slashInd ==-1) {
//				dirName = ".";
//			} 
//			else {
//				dirName = fileName.substring(0,slashInd+1);
//				fileName = fileName.substring(slashInd+1);
//			}
//			log.fine("file name : "+fileName);	
//			if (!trJob.getStatus().equals(BatchJobStatus.COMPLETED)) {
//				throw new ArgumentException ("The job has not completed.");
//			} 
//			else {
//				copyResultFromFile(dirName,fileName, serialType, (TextRefRecordCollection)resRc);							
//			}
//			
//		} catch (IOException e) {
//			log.severe(e.toString());
//			throw new CmRuntimeException(e.toString());
//		}
//		log.fine(">>copyResult");	
	}
	
	/**
	 * Aborts the matching process with the given job ID.
	 * 
	 * @param   jobID		Job ID.
	 
	 * @return  Job ID.
	 * @throws  RemoteException
	 */	
	public boolean 					abortJob(
									long jobId
									)
								throws	ArgumentException,
										ConfigException,
										CmRuntimeException, 
										RemoteException

	{
		log.fine("<<abortJob");	
			UrmJob	job = Single.getInst().findUrmJobById(jobId);
			if(  job.isAbortRequested() ) {
				job.getStatus();
				log.fine(">>abortJob - false");
				return false;
			}
			boolean isAcualyRequested = job.markAsAbortRequested();
			if(!isAcualyRequested)
				return isAcualyRequested;
			long stepJobId  = job.getCurStepJobId().longValue();
			int stepIndex = (int)job.getCurStepIndex().longValue();	
			if(stepJobId != JobStatus.UNDEFINED_ID){
				switch(stepIndex){
					case BATCH_MATCH_STEP_INDEX :
						abortBatchJob(stepJobId);
					break; 
					case TRANS_OABA_STEP_INDEX:{
						log.info("no action on step index "+stepIndex);
					}
					break;
					case TRANS_SERIAL_STEP_INDEX:{
						TransSerializer ts = Single.getInst().getTransSerializer();
						ts.abortJob(stepJobId);
					}
					break;
					default:
						log.info("unknown step index "+stepIndex);
				}
			}
			log.fine(">>abortJob - true");
			return true;
	}

	/**
	 * Suspends the matching process with the given job ID.
	 * 
	 * @param   jobID		Job ID.
	 
	 * @return  Job ID.
	 * @throws  RemoteException
	 */	
	public boolean 		suspendJob(
							long jobID 
						)
						throws	ArgumentException,
								ConfigException,
								CmRuntimeException, 
								RemoteException

	{
		log.fine("<<suspendJob");
		//TODO: implement  
		//log.fine(">>suspendJob");
		throw new CmRuntimeException("method is not implemented");
		//return false;
	}

	/**
	 * Resumess the matching process with the given job ID.
	 * 
	 * @param   jobID		Job ID.
	 
	 * @return  Job ID.
	 * @throws  RemoteException
	 */	
	public boolean 		resumeJob(
							long jobID 
						)
						throws	ArgumentException,
								ConfigException,
								CmRuntimeException, 
								RemoteException

	{
		log.fine("<<resumeJob");
		//TODO: implement 
		//log.fine(">>resumeJob");
		//return res;
		throw new CmRuntimeException("method is not implemented");
	}

	public long[] 		getJobList()
						throws	ArgumentException,
								ConfigException,
								CmRuntimeException, 
								RemoteException

	{
		long[] res;
		log.fine("<<getJobList");
		Collection jobColl = Single.getInst().getUrmJobList();
		res = new long[jobColl.size()];
		Iterator jobIter = jobColl.iterator();
		int ind = 0;
		UrmJob urmJob;
		while (jobIter.hasNext()) {
			urmJob = (UrmJob) jobIter.next();
			res[ind++] = urmJob.getId().longValue(); 
		}
		log.fine(">>getJobList");
		return res;
	}

	public Iterator	getResultIterator(
							RefRecordCollection rc,
							AnalysisResultFormat s)
						throws
							RecordCollectionException,
							ArgumentException,
							CmRuntimeException, 
							RemoteException{
		log.fine("<<getResultIterator rc");
		//log.fine(">>getResultIterator rc");						
		//return null;//TODO:implememnt
		throw new CmRuntimeException("method is not implemented");							
	}

	public Iterator	getResultIterator(
							long jobId,
							AnalysisResultFormat s)
						throws
							RecordCollectionException,
							ArgumentException,
							CmRuntimeException, 
							RemoteException{
		log.fine("<<getResultIterator job");
		//log.fine(">>getResultIterator job");						
		//return null;//TODO:implememnt
		throw new CmRuntimeException("method is not implemented");							
	}

	public String getVersion(Object context)
						throws  RemoteException {
		return Single.getInst().getVersion();					
	}											

}

/*
 * 
 * 
 * static public void execCmd(String[] cmdParam) throws Exception {
          StringBuffer cmd = new StringBuffer();
          cmd.append(cmdParam[0]).append(" ");

          for(int i = 1; i<cmdParam.length; i++) {
               cmd.append(cmdParam[i]).append(" ");
          }

          Process pr = Runtime.getRuntime().exec(cmd.toString());
          pr.waitFor();
          Log.logMsg("Exit Value for :" + cmd + " is " + pr.exitValue());
              }

call this method lie execCmd(" cmd.exe /c copy source destinaltion")
 */
	
