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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.IControl;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.utils.ControlChecker;

/**
 * This is a single instance object that contains the chunk data to be shared by
 * multiple matcher message beans.
 *
 * @author pcheung
 *
 */
public class ChunkDataStore {

	private static final Logger logger = Logger.getLogger(ChunkDataStore.class
			.getName());

	// public static final int DEFAULT_LOOP_CONTROL = 10000;
	public static final int DEFAULT_LOOP_CONTROL = 100;

	// Make this explicitly not serializable
	private transient Map<Object, Record> stageMap = null;
	private transient Map<Object, Record> masterMap = null;

	private static ChunkDataStore dataStore = new ChunkDataStore();

	private ChunkDataStore() {
	}

	/**
	 * This gets the single instance object.
	 */
	public static ChunkDataStore getInstance() {
		return dataStore;
	}

	/**
	 * This method loads the chunk data into this data store.
	 *
	 * @param stageSource
	 *            - staging record source
	 * @param modelId
	 *            - staging accessProvider
	 * @param masterSource
	 *            - master record source
	 * @param masterModel
	 *            - master accessProvider
	 * @param maxChunkSize
	 *            - maximum chunk size
	 * @param control
	 *            - control object
	 */
	public synchronized void init(RecordSource stageSource,
			ImmutableProbabilityModel model, RecordSource masterSource,
			int maxChunkSize, IControl control) throws BlockingException {

		final String s = "Staging";
		stageMap = readMap(stageSource, model, maxChunkSize + 10, control, s);
		logger.info("Stage map size: " + stageMap.size());

		final String m = "Master";
		masterMap = readMap(masterSource, model, maxChunkSize + 10, control, m);
		logger.info("Master map size: " + masterMap.size());
	}

	private static Map<Object, Record> readMap(RecordSource rs,
			ImmutableProbabilityModel model, int maxSize, IControl control,
			String tag) throws BlockingException {

		Map<Object, Record> dataMap = new HashMap<>(maxSize, 1.0f);
		try {
			if (rs == null) {
				logger.info(tag + ": null record source -- skipping map");
			} else if (model == null) {
				logger.info(tag + ": null model -- skipping map");
			} else {
				logger.fine(tag + ": creating map");
				assert rs != null && model != null;
				rs.setModel(model);
				logger.fine(tag + ": opening record source");
				rs.open();

				logger.fine(tag + ": checking job control");
				boolean stop = control.shouldStop();

				// put the whole chunk data set into memory.
				int c = 0;
				while (rs.hasNext() && !stop) {
					Record r = rs.getNext();
					Object O = r.getId();
					dataMap.put(O, r);
					++c;
					if (c % DEFAULT_LOOP_CONTROL == 1) {
						logger.fine(tag + ": record count: " + c);
						logger.fine(tag + ": record id: " + O + " (class "
								+ (O == null ? null : O.getClass().getName())
								+ ")");
					}
					ControlChecker.checkStop(control, c);
				}
				logger.fine(tag + ": total record count: " + c);

				rs.close();
				logger.fine(tag + ": finished map");
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
	public synchronized void cleanUp() {
		stageMap = null;
		masterMap = null;
	}

	/**
	 * This method returns the staging record with the given id.
	 */
	public Object getStage(Object id) {
		if (stageMap == null) {
			String msg = "null stage map";
			logger.severe(msg);
			throw new IllegalStateException(msg);
		}
		if (logger.isLoggable(Level.FINE) && stageMap.size() == 0) {
			// Issue warnings only for fine-grained logging
			logger.warning("stageMap is empty");
		}
		Object retVal = stageMap.get(id);
		if (logger.isLoggable(Level.FINE) && retVal == null) {
			// Issue warnings only for fine-grained logging
			logger.warning("null staging record (id " + id + ", stageMap size "
					+ stageMap.size() + ", class "
					+ (id == null ? null : id.getClass().getName()) + ")");
		}
		return retVal;
	}

	/**
	 * This method returns the master record with the given id.
	 */
	public Object getMaster(Object id) {
		if (masterMap == null) {
			String msg = "null master map";
			logger.severe(msg);
			throw new IllegalStateException(msg);
		}
		if (logger.isLoggable(Level.FINE) && masterMap.size() == 0) {
			// Issue warnings only for fine-grained logging
			logger.warning("masterMap is empty");
		}
		Object retVal = masterMap.get(id);
		if (logger.isLoggable(Level.FINE) && retVal == null) {
			// Issue warnings only for fine-grained logging
			logger.warning("null master record (id " + id + ")");
			logger.warning("null master record (id " + id + ", masterMap size "
					+ masterMap.size() + ", class "
					+ (id == null ? null : id.getClass().getName()) + ")");
		}
		return retVal;
	}

}
