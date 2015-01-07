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

import static com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE.*;
import static com.choicemaker.cm.io.blocking.automated.offline.core.Constants.*;

import java.io.IOException;
import java.util.List;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.ComparisonArray;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.EXTERNAL_DATA_FORMAT;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySink;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;

/**
 * This is a file implementation of IComparisonArraySink.
 * 
 * @author pcheung
 *
 */
@SuppressWarnings({ "rawtypes" })
public class ComparisonArraySink extends BaseFileSink implements
		IComparisonArraySink {

	public ComparisonArraySink(String fileName, EXTERNAL_DATA_FORMAT type) {
		super(fileName, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonGroupSink
	 * #
	 * writeComparisonGroup(com.choicemaker.cm.io.blocking.automated.offline.core
	 * .ComparisonGroup)
	 */
	public void writeComparisonArray(ComparisonArray cg)
			throws BlockingException {
		try {
			if (cg.getStagingIDsType() == null)
				throw new BlockingException("stage id type not set");
			if ((cg.getMasterIDsType() == null)
					&& (cg.getMasterIDs().size() > 0))
				throw new BlockingException("master id type not set");

			if (type == EXTERNAL_DATA_FORMAT.BINARY) {
				// first write the id type of staging IDs
				dos.writeInt(cg.getStagingIDsType().getCharSymbol());

				// second write the stagingIDs list.
				writeArray(cg.getStagingIDs(), cg.getStagingIDsType());

				// third write the master id type
				dos.writeInt(cg.getMasterIDsType().getCharSymbol());

				// fourth write the master list
				writeArray(cg.getMasterIDs(), cg.getMasterIDsType());

			} else if (type == EXTERNAL_DATA_FORMAT.STRING) {
				// first write the id type of staging IDs
				fw.write(Integer.toString(cg.getStagingIDsType()
						.getCharSymbol()) + LINE_SEPARATOR);

				// second write the stagingIDs list.
				writeArray(cg.getStagingIDs(), cg.getStagingIDsType());

				// third write the master id type
				fw.write(Integer
						.toString(cg.getMasterIDsType().getCharSymbol())
						+ Constants.LINE_SEPARATOR);

				// fourth write the master list
				writeArray(cg.getMasterIDs(), cg.getMasterIDsType());

			}

			count++;

		} catch (IOException ex) {
			throw new BlockingException(ex.toString());
		}
	}

	private void writeArray(List list, RECORD_ID_TYPE dataType)
			throws IOException {

		if (type == EXTERNAL_DATA_FORMAT.BINARY) {
			// first write the size
			dos.writeInt(list.size());

			// then write each element
			for (int i = 0; i < list.size(); i++) {
				if (dataType == RECORD_ID_TYPE.TYPE_INTEGER) {
					dos.writeInt(((Integer) list.get(i)).intValue());

				} else if (dataType == RECORD_ID_TYPE.TYPE_LONG) {
					dos.writeLong(((Long) list.get(i)).longValue());

				} else if (dataType == RECORD_ID_TYPE.TYPE_STRING) {
					String s = (String) list.get(i);
					dos.writeInt(s.length());
					dos.writeChars(s);
				}
			}

		} else if (type == EXTERNAL_DATA_FORMAT.STRING) {
			// first write the size
			fw.write(Integer.toString(list.size()) + Constants.LINE_SEPARATOR);

			// then write each element
			for (int i = 0; i < list.size(); i++) {
				if (dataType == TYPE_INTEGER) {
					fw.write(((Integer) list.get(i)).toString()
							+ LINE_SEPARATOR);

				} else if (dataType == TYPE_LONG) {
					fw.write(((Long) list.get(i)).toString() + LINE_SEPARATOR);

				} else if (dataType == TYPE_STRING) {
					String s = (String) list.get(i);
					fw.write(s + Constants.LINE_SEPARATOR);
				}
			}

			/*
			 * for (int i=0; i<list.size(); i++) { if (dataType ==
			 * Constants.TYPE_INTEGER) { fw.write (((Integer)
			 * list.get(i)).toString() + " ");
			 * 
			 * } else if (dataType == Constants.TYPE_LONG) { fw.write ( ((Long)
			 * list.get(i)).toString() + " ");
			 * 
			 * } else if (dataType == Constants.TYPE_STRING) { String s =
			 * (String) list.get(i); fw.write ( s + " "); } } fw.write
			 * (Constants.LINE_SEPARATOR);
			 */

		} // end if type
	}

}
