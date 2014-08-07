/*
 * Created on Dec 9, 2005
 *
 */
package com.choicemaker.cm.urm.ejb;

import java.io.NotSerializableException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.Accessor;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.IRecordSourceSerializationRegistry;
import com.choicemaker.cm.core.IRecordSourceSerializer;
import com.choicemaker.cm.core.ISerializableRecordSource;
import com.choicemaker.cm.core.base.DefaultRecordSourceSerializationRegistry;
import com.choicemaker.cm.core.base.SerializedRecordSourceDescriptor;
import com.choicemaker.cm.io.db.base.DbAccessor;
import com.choicemaker.cm.io.db.base.DbReaderSequential;
import com.choicemaker.cm.io.db.base.ISerializableDbRecordSource;
import com.choicemaker.cm.io.flatfile.base.FlatFileSerializableRecordSource;
import com.choicemaker.cm.io.xml.base.XMLSerializableRecordSource;
import com.choicemaker.cm.urm.base.DbRecordCollection;
import com.choicemaker.cm.urm.base.DelimitedTextFormat;
import com.choicemaker.cm.urm.base.FixedLengthTextFormat;
import com.choicemaker.cm.urm.base.IRecordCollectionVisitor;
import com.choicemaker.cm.urm.base.ITextFormat;
import com.choicemaker.cm.urm.base.ITextFormatVisitor;
import com.choicemaker.cm.urm.base.RefRecordCollection;
import com.choicemaker.cm.urm.base.SelfDescrRecordCollection;
import com.choicemaker.cm.urm.base.SubsetDbRecordCollection;
import com.choicemaker.cm.urm.base.TextRefRecordCollection;
import com.choicemaker.cm.urm.base.ValueRecordCollection;
import com.choicemaker.cm.urm.base.XmlTextFormat;
import com.choicemaker.cm.urm.exceptions.RecordCollectionException;

public class SerialRecordSourceBuilder implements IRecordCollectionVisitor, ITextFormatVisitor {

	protected static Logger log = Logger.getLogger(SerialRecordSourceBuilder.class);
	private ISerializableRecordSource resRs;
	private IProbabilityModel model;
	private boolean checkEmpty;
	private String fileName;
	 
	
	SerialRecordSourceBuilder(IProbabilityModel model, boolean checkEmpty){
		this.checkEmpty = checkEmpty;
		this.model = model;
	}
	
	protected String extractUrl(RefRecordCollection rc) throws RecordCollectionException{
		String urlString = rc.getUrl(); 
		log.debug("url:"+urlString);
		if(urlString == null || urlString.length() == 0 ){
		  if(checkEmpty)
			throw new RecordCollectionException("empty url");
		  else
			urlString =  null;	
		}
		return urlString;
	}
	
	public String extractDsJndiName(String urlString) throws RecordCollectionException {

		// Preconditions
		if (urlString == null) {
			throw new RecordCollectionException("null database URI");
		}
		urlString = urlString.trim();
	
		String retVal = urlString;

		// FIXME JBoss-specific; move this to a plugin
		//    "services:jndi://java:comp/env/jdbc/OABADS";
		final String JBOSS_JNDI_SERVICES = "services:jndi:";
		final int JBOSS_LENGTH = JBOSS_JNDI_SERVICES.length();
		if(urlString.startsWith(JBOSS_JNDI_SERVICES)){
			retVal = urlString.substring(JBOSS_LENGTH);
		}

		log.debug("Datasource URI: "+retVal);
		// Post-condition
		if (retVal.length() == 0) {
			throw new RecordCollectionException("blank database URI");
		}

		return retVal;	
	}
	
	
	/* (non-Javadoc)
	 * @see com.choicemaker.cm.urm.base.IRecordCollectionVisitor#visit(com.choicemaker.cm.urm.base.TextRefRecordCollection)
	 */
	public void visit(TextRefRecordCollection rc) throws RecordCollectionException{
		if(rc == null){
			resRs = null;
			return;
		}	
		String urlString = extractUrl(rc);
		try {
			URL url = new URL(urlString);
			fileName = url.getFile();
			ITextFormat f = ((TextRefRecordCollection)rc).getFormat();
			f.accept(this);
		} catch (MalformedURLException e) {
			log.error(e);
			throw new RecordCollectionException(e.toString());
		} 
	}

	public void visit(XmlTextFormat f){		
		resRs = new XMLSerializableRecordSource ( fileName, model.getModelName());
	}
	
	protected boolean isTaggedByDefault(){
		Accessor acc = model.getAccessor();
		return acc.getNumRecordTypes()!= 0; 
	} 

	public void visit(DelimitedTextFormat f){
		boolean tagged = isTaggedByDefault();	
		resRs = new FlatFileSerializableRecordSource ( fileName, false, f.getSeparator(),tagged ,!tagged, model.getModelName());
	}

	public void visit(FixedLengthTextFormat f){
		boolean tagged = isTaggedByDefault();		
		resRs = new FlatFileSerializableRecordSource ( fileName,true, '\0', tagged, !tagged,model.getModelName());
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.urm.base.IRecordCollectionVisitor#visit(com.choicemaker.cm.urm.base.DbRecordCollection)
	 */
	public void visit(DbRecordCollection rc) throws RecordCollectionException {
		resRs = null;
	  if(rc != null){
		// Configure the query determined by the model configuration
		final String modelName = model.getModelName();								
		DbAccessor accessor = (DbAccessor) model.getAccessor();
		DbReaderSequential dbr = accessor.getDbReaderSequential(rc.getName());
		String schemaName = dbr.getName();
		schemaName = schemaName.substring(0,schemaName.indexOf(':')); 
		String rootViewName = "vw_cmt_"+schemaName+"_r_"+rc.getName()+"0";
		final String query = "select "+dbr.getMasterId()+" as id from "+rootViewName;

		createSerializableRecordSource(rc, query);
	  }	
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.urm.base.IRecordCollectionVisitor#visit(com.choicemaker.cm.urm.base.SubsetDbRecordCollection)
	 */
	public void visit(SubsetDbRecordCollection rc) throws RecordCollectionException{
		resRs = null;
	  if(rc != null){
		final String 	query = ((SubsetDbRecordCollection)rc).getIdsQuery();
		createSerializableRecordSource(rc, query);
	  }	
	}

	private void createSerializableRecordSource(DbRecordCollection rc, String query)  throws RecordCollectionException {
		// assert rc != null ;
		// assert query != null && query.trim().length() > 0 ;
		try {
			// Get database connection information
			final String urlString = extractUrl(rc);
			String dsJndiName = extractDsJndiName(urlString);
			DataSource ds = Single.getInst().getDataSource (dsJndiName);
			Connection conn = ds.getConnection();
			String connUrl  = conn.getMetaData().getURL();
			log.debug("DB connection URL: "+connUrl);
			conn.close();
		
			// Configure the record source buffer size, dbConfig name and model name
			Integer bufferSize = rc.getBufferSize();
			final int nonnullBufferSize = (bufferSize != null)? bufferSize.intValue():
												DbRecordCollection.DEFAULT_REC_COLLETION_BUFFER_SIZE;
			final String safeBufferSize = Integer.toString(nonnullBufferSize);
			final String dbConfig = rc.getName();
			final String modelName = model.getModelName();								
		
			// Create a serializable record source
			IRecordSourceSerializationRegistry registry2 = DefaultRecordSourceSerializationRegistry.getInstance();
			IRecordSourceSerializer serializer = registry2.getRecordSourceSerializer(connUrl);
			Properties properties = new Properties();
			properties.setProperty(ISerializableDbRecordSource.PN_DATASOURCE_JNDI_NAME, urlString);
			properties.setProperty(ISerializableDbRecordSource.PN_MODEL_NAME,modelName);
			properties.setProperty(ISerializableDbRecordSource.PN_DATABASE_CONFIG,dbConfig);
			properties.setProperty(ISerializableDbRecordSource.PN_SQL_QUERY,query);
			properties.setProperty(ISerializableDbRecordSource.PN_BUFFER_SIZE,safeBufferSize);
			resRs = serializer.getSerializableRecordSource(properties);
		} catch (NotSerializableException e) {
			String errMsg = "Unable to create serializable record source";
			log.error(errMsg,e);
			throw new RecordCollectionException(errMsg + ": " + e.toString());
		} catch (NamingException e) {
			log.error(e);
			throw new RecordCollectionException(e.toString()); 
		} catch (SQLException e) {
			log.error(e);
			throw new RecordCollectionException(e.toString()); 
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.urm.base.IRecordCollectionVisitor#visit(com.choicemaker.cm.urm.base.ValueRecordCollection)
	 */
	public void visit(ValueRecordCollection rc) throws RecordCollectionException{
		if(rc == null){
			resRs = null;
			return;
		}
		throw new RuntimeException("not yet implemented");
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.urm.base.IRecordCollectionVisitor#visit(com.choicemaker.cm.urm.base.SelfDescrRecordCollection)
	 */
	public void visit(SelfDescrRecordCollection rc) throws RecordCollectionException{
		if(rc == null){
			resRs = null;
			return;
		}
		String urlString = extractUrl(rc);
		try {
			URL url = new URL(urlString);
			resRs = new SerializedRecordSourceDescriptor ( url.getFile(), model.getModelName());
		} catch (MalformedURLException e) {
			log.error(e);
			throw new RecordCollectionException(e.toString());
		} 

	}

	/**
	 * @return
	 */
	public ISerializableRecordSource getResultRecordSource() {
		return this.resRs;
	}

	/**
	 * @return
	 */
	public boolean isCheckEmpty() {
		return checkEmpty;
	}

	/**
	 * @param b
	 */
	public void setCheckEmpty(boolean b) {
		checkEmpty = b;
	}

}
