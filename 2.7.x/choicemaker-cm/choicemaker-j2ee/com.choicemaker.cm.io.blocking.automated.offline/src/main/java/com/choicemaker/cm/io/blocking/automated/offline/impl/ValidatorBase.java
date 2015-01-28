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
package com.choicemaker.cm.io.blocking.automated.offline.impl;

import static com.choicemaker.cm.io.blocking.automated.offline.core.ImmutableRecordIdTranslator.NOT_SPLIT;

import java.io.Serializable;
import java.util.logging.Logger;

import com.choicemaker.cm.io.blocking.automated.offline.core.BlockSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IValidatorBase;
import com.choicemaker.cm.io.blocking.automated.offline.core.ImmutableRecordIdTranslator;
import com.choicemaker.util.LongArrayList;

/**
 * @author pcheung
 */
public class ValidatorBase implements IValidatorBase, Serializable {

	private static final long serialVersionUID = 271;

	private static final Logger logger = Logger.getLogger(ValidatorBase.class
			.getName());

	// this indicates where the staging rows are.
	// true if the stage records are before the split Index
	private final boolean isBefore;
	private final boolean isSplit;
	private final int splitIndex; // the point at which record sources change

	/**
	 * This constructor takes these two parameters:
	 * 
	 * @param isBefore
	 *            - true if the stage records are before the split Index
	 * @param ImmutableRecordIdTranslator
	 *            - the record ID to internal id translator
	 */
	public ValidatorBase(boolean isBefore,
			ImmutableRecordIdTranslator<?> translator) {
		if (translator == null) {
			throw new IllegalArgumentException("null translator");
		}
		logger.fine("isBefore: " + isBefore);
		logger.fine("Translator: " + translator);
		this.isBefore = isBefore;
		this.isSplit = translator.isSplit();
		this.splitIndex = translator.getSplitIndex();
	}

	/**
	 * This method checks to see if all the id in the blocking set is from the
	 * "master" source. If so, return false. A valid blocking set need to be
	 * consisted of at least 1 staging record.
	 */
	@Override
	public boolean validBlockSet(BlockSet bs) {
		long min = Long.MAX_VALUE;
		long max = Long.MIN_VALUE;
		boolean ret = false;

		if (!isSplit) {
			assert splitIndex == NOT_SPLIT;
			ret = true;

		} else {
			LongArrayList list = bs.getRecordIDs();
			int s = list.size();
			for (int i = 0; i < s; i++) {
				long l = list.get(i);
				if (l > max)
					max = l;
				if (l < min)
					min = l;
			}

			if (isBefore) {
				if (min < splitIndex)
					ret = true;
				else
					ret = false;
			} else {
				if (max >= splitIndex)
					ret = true;
				else
					ret = false;
			}
		}

		// Log the result
		String msg =
			(ret ? "Valid" : "Invalid") + " blocking set: " + bs.toString();
		if (ret) {
			logger.fine(msg);
		} else {
			logger.warning(msg);
		}

		return ret;
	}

}
