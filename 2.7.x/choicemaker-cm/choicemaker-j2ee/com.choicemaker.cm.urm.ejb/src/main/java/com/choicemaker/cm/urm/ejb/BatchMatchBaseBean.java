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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.logging.Logger;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.SerializableRecordSource;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.core.xmlconf.EmbeddedXmlConfigurator;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchQueryService;
import com.choicemaker.cm.urm.base.IRecordCollection;
import com.choicemaker.cm.urm.base.RefRecordCollection;
import com.choicemaker.cm.urm.base.TextRefRecordCollection;
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
public class BatchMatchBaseBean implements SessionBean {

	private static final long serialVersionUID = 1L;
	protected static Logger log;
	protected static boolean initialized = false;
	protected transient SessionContext sessionContext;	

//	@EJB
	private BatchQueryService batchQuery;

	public BatchMatchBaseBean() {
		super();
	}

	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException {

	}

	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException, RemoteException {
	}
	
	public void ejbCreate() throws CreateException, RemoteException {
		try {
			if (!initialized) {
				EmbeddedXmlConfigurator.getInstance().embeddedInit(null);
				initialized = true;
			}
		} catch (Exception ex) {
			log.severe(ex.toString());
			throw new CreateException(ex.toString());
		}

	} // ejbCreate()

	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#ejbRemove()
	 */
	public void ejbRemove() throws EJBException, RemoteException {
	}

	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext sessionContext) throws EJBException, RemoteException {
			this.sessionContext = sessionContext;
	}

	protected long 	startBatchQueryService(
											IRecordCollection qRs, 
											RefRecordCollection mRs,
											String modelName, 
											float differThreshold, 
											float matchThreshold,
											int maxSingle,
											String externalId,
											UrmJob uj) 
							throws
									RecordCollectionException,
//									ArgumentException,
									ConfigException,
									ModelException,
									CmRuntimeException, 
									RemoteException
	{	//TODO: check input parameters
		log.fine("<< startBatchQueryService...");
		IProbabilityModel model = PMManager.getModelInstance(modelName);
		if (model == null) {
			log.severe("Invalid probability accessProvider: " + modelName);
			throw new ModelException("Invalid probability accessProvider: " + modelName);
		}
		SerialRecordSourceBuilder	rcb = new SerialRecordSourceBuilder(model,true);
		qRs.accept(rcb);
		SerializableRecordSource staging = rcb.getResultRecordSource();
		SerializableRecordSource master = null;
		if(mRs != null){
			rcb.setCheckEmpty(false);
			mRs.accept(rcb);
			master =  rcb.getResultRecordSource();
		}
		
//		int transactionId = -1;
		if(uj != null){		
			uj.setQueryRs(staging);
			uj.setMasterRs(master);
//			transactionId = uj.getId().intValue();
			uj.markAsMatching();
		}

		final long retVal =
			batchQuery.startOABA(externalId, staging, master, differThreshold,
					matchThreshold, modelName, maxSingle, false);
		log.fine(">> startBatchQueryService");
		return retVal;
	}

	/**
	 * Aborts the matching process with the given job ID.
	 * 
	 * @param   jobID		Job ID.
	 *  
	 * @return  Job ID.
	 * @throws  RemoteException
	 */	
	public boolean abortBatchJob(long jobId) {
		return batchQuery.abortJob(jobId) == 0;
	}

	/**
	 * Copies the matching result  
	 * 
	 * @param   jobID		Job ID.
	 * @param	resRc		Record collection to serialize matching result.
	 * @throws  RemoteException
	 */
	class PrefixFileFilter implements FilenameFilter{
		private String prefix;
		private String ext;
		public PrefixFileFilter(String prefix,String ext) {
			this.prefix = prefix;
			this.ext = ext;
		}
		public boolean accept(File dir, String name) {
			//String  lkName = name.toLowerCase();
			boolean ret =  name.startsWith(prefix)&&name.endsWith("."+ext);
			return ret;
		}
	}
	
	protected void 	copyResultFromFile (
									String sourceDirName,
									String sourceFileNameBegining, 
									String sourceExt,
									TextRefRecordCollection resRc)
								throws
										ModelException, 	
										RecordCollectionException,
										ConfigException,
										ArgumentException,
										CmRuntimeException, 
										RemoteException		
	{
		try {
				//source files
				//--->sourceFileNameBegining = sourceFileNameBegining.toLowerCase();
				//sourceExt = sourceExt.toLowerCase();
				File   sourceDir = new File(sourceDirName);
				//String baseSourceFileName = sourceFile.getName();
				PrefixFileFilter filter = new PrefixFileFilter(sourceFileNameBegining,sourceExt);
				String[] fileNames = sourceDir.list(filter);
				
				// target files or urls
				String urlBeginingPart = null;
				String urlEndingPart = null;

				String 	urlString = resRc.getUrl();
				int		lastPeriod = urlString.lastIndexOf(".");
				int		lastSlash = urlString.lastIndexOf("/");
				int		lastBkSlash = urlString.lastIndexOf("\\");
				if(lastPeriod == -1 || lastPeriod<lastSlash || lastPeriod <lastBkSlash){
					urlBeginingPart = urlString;
					urlEndingPart = "";
				} 
				else	{
					urlBeginingPart = urlString.substring(0,lastPeriod);
					urlEndingPart = urlString.substring(lastPeriod);
				}
				
				for (int i = 0; i < fileNames.length; i++) {
					String fileName = fileNames[i];//.toLowerCase();
					FileInputStream is  = new FileInputStream(sourceDir.getAbsolutePath()+"/"+fileName);
					String nameCopiedPart = fileName.substring(sourceFileNameBegining.length(),fileName.lastIndexOf("."+sourceExt));
					URL url = new URL(urlBeginingPart+nameCopiedPart+urlEndingPart);
					log.info("URL is created: " + url.toString());
					log.fine("Host: " + url.getHost());
					OutputStream os;
					if(  url.getProtocol().equals("file") && (url.getHost()== null || url.getHost().length() == 0)){ 
						os  = new FileOutputStream(url.getFile());					
					}
					else {
						URLConnection urlC = url.openConnection();
						log.info("Url connection is opened: " + urlC);
						urlC.setDoOutput(true);
						urlC.setDoInput(false);
						urlC.setAllowUserInteraction(false);						
						os = urlC.getOutputStream();
						Date date=new Date(urlC.getLastModified());
						log.info("Copying result into " + urlC.getContentType()+", modified on: " + date+ ".");
					}
					int count = 0;
					byte[] buf = new byte[4096];
					int bytesread = 0;
					while((bytesread=is.read(buf))!=-1) {
						os.write(buf, 0, bytesread);
						count += bytesread;
					}
					is.close();
					os.close();
					log.info(count + " byte(s) copied");								
				}			
		} catch (FileNotFoundException e) {
			log.severe(e.toString());
			throw new RecordCollectionException(e.toString());
		} catch (MalformedURLException e) {
			log.severe(e.toString());
			throw new RecordCollectionException(e.toString());
		} catch (IOException e) {
			log.severe(e.toString());
			throw new CmRuntimeException(e.toString());
		}
	}
						
}

//byte[] buf = new byte[1024];
//int bytesread = 0;
//while((bytesread=fis.read(buf))!=-1) {
//fos.write(buf, 0, bytesread);s
//}				

//int oneChar;
//while ((oneChar=is.read()) != -1){
// os.write(oneChar);
// count++;
//}
