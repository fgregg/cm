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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IControl;
import com.choicemaker.cm.io.blocking.automated.offline.utils.ControlChecker;

/**
 * This is a single instance object that contains the chunk data to be shared by
 * multiple matcher message beans.
 *
 * @author pcheung
 *
 */
public class ChunkDataStore {

	// Make this explicitly not serializable
	private transient Map<Object, Record> stageMap = null;
	private transient Map<Object, Record> masterMap = null;

	private static ChunkDataStore dataStore = null;

	private ChunkDataStore() {
	}

	/**
	 * This gets the single instance object.
	 *
	 * @return
	 */
	public static ChunkDataStore getInstance() {
		if (dataStore == null)
			dataStore = new ChunkDataStore();
		return dataStore;
	}

	/**
	 * This method loads the chunk data into this data store.
	 *
	 * @param stageSource
	 *            - staging record source
	 * @param stageModel
	 *            - staging accessProvider
	 * @param masterSource
	 *            - master record source
	 * @param masterModel
	 *            - master accessProvider
	 * @param maxChunkSize
	 *            - maximum chunk size
	 * @param control
	 *            - control object
	 * @throws BlockingException
	 */
	public void init(RecordSource stageSource,
			ImmutableProbabilityModel stageModel, RecordSource masterSource,
			ImmutableProbabilityModel masterModel, int maxChunkSize,
			IControl control) throws BlockingException {

		stageMap = readMap(stageSource, stageModel, maxChunkSize + 10, control);
		masterMap =
			readMap(masterSource, masterModel, maxChunkSize + 10, control);
	}

	private static Map<Object, Record> readMap(RecordSource rs,
			ImmutableProbabilityModel model, int maxSize, IControl control)
			throws BlockingException {

		Map<Object, Record> dataMap = new HashMap<>(maxSize, 1.0f);
		try {
			if (rs != null && model != null) {
				rs.setModel(model);
				rs.open();

				boolean stop = control.shouldStop();
				int c = 0;

				// put the whole chunk dataset into memory.
				while (rs.hasNext() && !stop) {
					Record r = rs.getNext();
					Object O = r.getId();
					dataMap.put(O, r);
					ControlChecker.checkStop(control, ++c);
				}

				rs.close();
			}
		} catch (IOException ex) {
			throw new BlockingException(ex.toString());
		}

		return dataMap;
	}

	/**
	 * This method frees up resources.
	 *
	 *
	 */
	public void cleanUp() {
		stageMap = null;
		masterMap = null;
	}

	/**
	 * This method returns the staging record with the given id.
	 *
	 * @param id
	 * @return
	 */
	public Object getStage(Object id) {
		return stageMap.get(id);
	}

	/**
	 * This method returns the master record with the given id.
	 *
	 * @param id
	 * @return
	 */
	public Object getMaster(Object id) {
		return masterMap.get(id);
	}

}
