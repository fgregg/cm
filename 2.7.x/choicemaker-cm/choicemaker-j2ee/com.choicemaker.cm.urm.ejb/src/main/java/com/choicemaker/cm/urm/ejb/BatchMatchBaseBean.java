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
import java.sql.SQLException;
import java.util.Date;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.SerialRecordSource;
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

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static Logger log;
	protected static boolean initialized = false;
	protected transient SessionContext sessionContext;	
	/**
	 * 
	 */
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
			log.error(ex.toString (), ex);
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
									ArgumentException,
									ConfigException,
									ModelException,
									CmRuntimeException, 
									RemoteException
	{	//TODO: check input parameters
		log.debug("<< startBatchQueryService...");
		IProbabilityModel model = PMManager.getModelInstance(modelName);
		if (model == null) {
			log.error("Invalid probability accessProvider: " + modelName);
			throw new ModelException("Invalid probability accessProvider: " + modelName);
		}
		SerialRecordSourceBuilder	rcb = new SerialRecordSourceBuilder(model,true);
		qRs.accept(rcb);
		SerialRecordSource staging = rcb.getResultRecordSource();
		SerialRecordSource master = null;
		if(mRs != null){
			rcb.setCheckEmpty(false);
			mRs.accept(rcb);
			master =  rcb.getResultRecordSource();
		}
		int transactionId = -1;
		if(uj != null){		
			uj.setQueryRs(staging);
			uj.setMasterRs(master);
			transactionId = uj.getId().intValue();
			uj.markAsMatching();
		}
		long id;
		try {
			BatchQueryService qs = Single.getInst().getBatchQueryService();		
			id = qs.startOABA(
					externalId,
					transactionId,
					staging,
					master,
					differThreshold,
					matchThreshold,
					modelName,
					modelName,
					maxSingle,
					false);	
		} catch (NamingException e) {
			log.error(e);
			throw new ConfigException(e.toString());
		} catch (CreateException e) {
			log.error(e);
			throw new ConfigException(e.toString());
		} catch (JMSException e) {
			log.error(e);
			throw new ConfigException(e.toString());
		} catch (SQLException e) {
			log.error(e);
			throw new CmRuntimeException(e.toString());
		}
		log.debug (">> startBatchQueryService");
		return id;
	
	}


	/**
	 * Aborts the matching process with the given job ID.
	 * 
	 * @param   jobID		Job ID.
	 *  
	 * @return  Job ID.
	 * @throws  RemoteException
	 */	
	public boolean 					abortBatchJob(
									long jobId 
								)
								throws	ArgumentException,
										ConfigException,
										CmRuntimeException, 
										RemoteException

	{
		try {
			BatchQueryService qs = Single.getInst().getBatchQueryService();
			return qs.abortJob(jobId) == 0;
			} catch (NamingException e) {
				log.error(e);
				throw new ConfigException(e.toString());
			} catch (CreateException e) {
				log.error(e);
				throw new ConfigException(e.toString());
			} catch (JMSException e) {
				log.error(e);
				throw new ConfigException(e.toString());
			} catch (FinderException e) {
				log.error(e);
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
					log.debug("Host: " + url.getHost());
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
			log.error(e);
			throw new RecordCollectionException(e.toString());
		} catch (MalformedURLException e) {
			log.error(e);
			throw new RecordCollectionException(e.toString());
		} catch (IOException e) {
			log.error(e);
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
