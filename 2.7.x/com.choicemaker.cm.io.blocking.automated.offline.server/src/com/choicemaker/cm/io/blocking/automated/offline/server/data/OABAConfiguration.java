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
package com.choicemaker.cm.io.blocking.automated.offline.server.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.impl.BlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ChunkDataSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ChunkRecordIDSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonArrayGroupSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonArraySinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonTreeGroupSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonTreeSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.IDTreeSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.MatchRecord2CompositeSink;
import com.choicemaker.cm.io.blocking.automated.offline.impl.MatchRecord2CompositeSource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.MatchRecord2SinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.RecValSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.RecordIDSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.SuffixTreeSink;
import com.choicemaker.cm.io.blocking.automated.offline.impl.SuffixTreeSource;
import org.apache.log4j.Logger;

/**
 * This object configures factory objects for the OABA.
 * 
 * @author pcheung
 *
 */
public class OABAConfiguration implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = -6103842299302698576L;

	//maximum size of a file to 1.5 GB
	public static final long MAX_FILE_SIZE = 1500000000;
	
	private static final String STATUS_FILE_NAME = "cmt_status.txt"; 
	private static final String DATA_FILE_NAME = "cmt_data.dat"; 

	private String fileDir;
	private String modelName;
	private long jobID;
	
	private transient IProbabilityModel model;
	private static final Logger logger =
			Logger.getLogger(OABAConfiguration.class);

	public OABAConfiguration (String modelName, long jobID) {
		this.modelName = modelName;
		this.jobID = jobID;
	}
	
	
	private ImmutableProbabilityModel getModel () {
		if (model == null) model = PMManager.getModelInstance(modelName);
		return model;
	}
	
	/**
	 * Returns the directory on disk that will be used to write files used by the batch job.
	 * Note the expectation is that project.xml contains a fileDir entry that contains the final
	 * 'path separator'. 
	 * <br>
	 * <code>
	 * 	&lt;property name="fileDir" value="C:&#92;Dev&#92;urm-jobs&#92;"&gt;
	 * </code>
	 * <br>
	 * <b>Windows Example:</b><br>
	 * <code>
	 * 	c:&#92;Dev&#92;urm-jobs&#92;
	 * </code>
	 * <br>
	 * <b>Linux Example:</b><br>
	 * <code>/var/local/oc4j/</code>
	 * @return String - full path to a directory
	 */
	public String getFileDir () {
		if (fileDir == null) {
			fileDir = (String) getModel().properties().get("fileDir") + System.getProperty("file.separator")+
			"job" + Long.toString(jobID) + System.getProperty("file.separator");
			logger.info("Working directory for ChoiceMaker Jobs is '" + fileDir + "'");
			File f = new File (fileDir);
			if (!f.exists()) {
				f.mkdir();
			}
		} 
		return fileDir;
	}
	
	/** This gets the factory that is used to get translator id sink and source.
	 * 
	 * @return - translation ID factory
	 */
	public RecordIDSinkSourceFactory getTransIDFactory () { 
		return new RecordIDSinkSourceFactory (getFileDir(), "translator", "dat");
	}
	
	public RecordIDSinkSourceFactory getRecordIDFactory () { 
		return new RecordIDSinkSourceFactory (getFileDir(), "recordID", "txt");
	}
	
	public ChunkDataSinkSourceFactory getStageDataFactory () {
		return new ChunkDataSinkSourceFactory (getFileDir(), "chunkstagerow", model);
	}
	
	public ChunkDataSinkSourceFactory getMasterDataFactory () {
		return new ChunkDataSinkSourceFactory (getFileDir(), "chunkmasterrow", model);
	}
	
	public ComparisonArraySinkSourceFactory getCGFactory () {
		return new ComparisonArraySinkSourceFactory (getFileDir(), "compareGroup", "dat");
	}

	public RecValSinkSourceFactory getRecValFactory () {
		return new RecValSinkSourceFactory (getFileDir(), "btemp", "dat");
	}
	
	public BlockSinkSourceFactory getBlockGroupFactory () { 
		return new BlockSinkSourceFactory (getFileDir (), "blockGroup", "dat");
	}
	
	public BlockSinkSourceFactory getBlockFactory () { 
		return new BlockSinkSourceFactory (getFileDir(), "blocks", "dat");
	}
	
	public BlockSinkSourceFactory getOversizedFactory () { 
		return new BlockSinkSourceFactory (getFileDir(), "oversized", "dat");
	}
	
	public BlockSinkSourceFactory getOversizedGroupFactory () { 
		return new BlockSinkSourceFactory (getFileDir(), "oversizedGroup", "dat");
	}
	
	public BlockSinkSourceFactory getOversizedTempFactory () { 
		return new BlockSinkSourceFactory (getFileDir(), "oversizedTemp", "dat");
	}
	
	public SuffixTreeSink getSuffixTreeSink () {
		return new SuffixTreeSink (getFileDir() + "trees.txt");
	}
	
	public BlockSinkSourceFactory getBigBlocksSinkSourceFactory () {
		return new BlockSinkSourceFactory (getFileDir(), "bigBlocks", "dat");
	}

	public BlockSinkSourceFactory getTempBlocksSinkSourceFactory () {
		return new BlockSinkSourceFactory (getFileDir(), "tempBlocks", "dat");
	}

/*	
	public BlockSinkSourceFactory getCompareBlockFactory () { 
		return new BlockSinkSourceFactory (getFileDir(), "compareBlock", "dat");
	}
*/
	
	public ChunkRecordIDSinkSourceFactory getChunkIDFactory () { 
		return new ChunkRecordIDSinkSourceFactory (getFileDir(), "chunkrow", "dat");
	}
	
	public IDTreeSetSource getTreeSetSource () {
		SuffixTreeSource sSource = new SuffixTreeSource (getFileDir() + "trees.txt");
		IDTreeSetSource source = new IDTreeSetSource (sSource);
		return source;
	}
	
	public ComparisonTreeSinkSourceFactory getComparisonTreeFactory (int stageType) {
		return new ComparisonTreeSinkSourceFactory
			(getFileDir(), "compareTree", "txt", stageType);
	}
	
	
	/** This is used by the parallelization code.  It creates many tree files for each chunk.
	 * 
	 * @param stageType
	 * @return
	 */
	public ComparisonTreeGroupSinkSourceFactory getComparisonTreeGroupFactory (int stageType, int num) {
		return new ComparisonTreeGroupSinkSourceFactory
			(getFileDir(), "compareTreeGroup", "txt", num, stageType);
	}
	
	
	public ComparisonArraySinkSourceFactory getComparisonArrayFactoryOS () {
		return new ComparisonArraySinkSourceFactory
			(getFileDir(), "compareArrayO", "dat");
	}

	/** This is used by the parallelization code.  It creates many array files for each chunk.
	 * 
	 * @param stageType
	 * @return
	 */
	public ComparisonArrayGroupSinkSourceFactory getComparisonArrayGroupFactoryOS (int num) {
		return new ComparisonArrayGroupSinkSourceFactory
			(getFileDir(), "compareArrayGroupO", "dat", num);
	}

/*	
	public ChunkDataSinkSourceFactory getChunkDataFactory () {
		return new ChunkDataSinkSourceFactory (getFileDir(), "chunkrow", accessProvider);
	}
*/	
	
	/** This gets the match result sink for each chunk of the Matcher Bean.
	 * 
	 */
	public MatchRecord2SinkSourceFactory getMatchChunkFactory () {
		return new MatchRecord2SinkSourceFactory (getFileDir(), "matchchunk", "txt");
	}
	
	public MatchRecord2SinkSourceFactory getMatchTempFactory () {
		return new MatchRecord2SinkSourceFactory (getFileDir(), "matchtemp", "txt");
	}
	
	public MatchRecord2SinkSourceFactory getMatchTempFactory (int i) {
		String str = Integer.toString(i);
		return new MatchRecord2SinkSourceFactory (getFileDir(), "matchtemp" + str + "_", "txt");
	}
	
	//public MatchRecord2SinkSourceFactory getMatchFactory () {
	//	return new MatchRecord2SinkSourceFactory (getFileDir(), "match", "txt");
	//}
	
	/** This returns the final sink in which to store the result of the OABA.
	 * Since this fle could be big, we limit the file size to MAX_FILE_SIZE.  This is mainly
	 * for Windows system where the max file size if 2 GB.
	 * The file name is [file dir]/match_[job id]_*.txt.
	 * 
	 * @param id - the job id of the OABA job
	 * @return IMatchRecord2Sink - the sink to store the OABA output.
	 */
	public IMatchRecord2Sink getCompositeMatchSink (long id) {
		String fileName = getFileDir () + "match_" + Long.toString(id);
		return new MatchRecord2CompositeSink (fileName,"txt",MAX_FILE_SIZE);
	}
	
	/**
	 * This returns the source handle to the OABA result.
	 * 
	 * @param id - the job id of the OABA job
	 * @return
	 */
	public IMatchRecord2Source getCompositeMatchSource (long id) {
		String fileName = getFileDir () + "match_" + Long.toString(id);
		return new MatchRecord2CompositeSource (fileName,"txt");
	}
	
	
	public MatchRecord2SinkSourceFactory getSet2MatchFactory () {
		return new MatchRecord2SinkSourceFactory (getFileDir(), "twomatch", "txt");
	}
	
	//public MatchRecord2SinkSourceFactory getTransMatchFactory () {
	//	return new MatchRecord2SinkSourceFactory (getFileDir(), "transMatch", "txt");
	//}


	public IMatchRecord2Sink getCompositeTransMatchSink (long id) {
		String fileName = getFileDir () + "transMatch_" + Long.toString(id);
		return new MatchRecord2CompositeSink (fileName,"txt",MAX_FILE_SIZE);
	}


	public IMatchRecord2Source getCompositeTransMatchSource (long id) {
		String fileName = getFileDir () + "transMatch_" + Long.toString(id);
		return new MatchRecord2CompositeSource (fileName,"txt");
	}

	
	public String getStatusFile () {
		return getFileDir () + STATUS_FILE_NAME;
	}
	
	
	public void saveStartData (StartData data) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream (new FileOutputStream (getFileDir() + DATA_FILE_NAME));
		oos.writeObject(data);
		oos.close();
	}

	public StartData getStartData () throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream (new FileInputStream (getFileDir() + DATA_FILE_NAME));
		StartData data = (StartData) ois.readObject ();
		ois.close();
		return data;
	}
	
	
	/** This method removes the temporary storage directory used by this job id.
	 * 
	 * @return boolean - true means it was a success
	 */
	public boolean removeTempDir () {
		File f = new File (getFileDir());
		File [] subs = f.listFiles();
		for (int i=0; i<subs.length; i++) {
			subs[i].delete();
		}
		return f.delete();
	}


	/** Block factory for transitivity
	 * 
	 * @return
	 */
	public BlockSinkSourceFactory getTransitivityBlockFactory () { 
		return new BlockSinkSourceFactory (getFileDir(), "transBlocks", "dat");
	}

}
