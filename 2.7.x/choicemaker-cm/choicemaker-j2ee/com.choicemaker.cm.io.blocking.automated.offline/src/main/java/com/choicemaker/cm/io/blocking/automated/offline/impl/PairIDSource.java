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

import java.io.EOFException;
import java.io.IOException;
import java.util.StringTokenizer;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.EXTERNAL_DATA_FORMAT;
import com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.PairID;

/**
 * @author pcheung
 *
 */
public class PairIDSource extends BaseFileSource<PairID> implements
		IPairIDSource {

	private PairID p;

	@Deprecated
	public PairIDSource(String fileName, int type) {
		super(fileName, EXTERNAL_DATA_FORMAT.fromSymbol(type));
	}

	public PairIDSource(String fileName, EXTERNAL_DATA_FORMAT type) {
		super(fileName, type);
	}

	@Override
	public PairID next() {
		return p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.ISource#hasNext()
	 */
	@Override
	public boolean hasNext() throws BlockingException {
		boolean next = false;

		try {
			if (getType() == EXTERNAL_DATA_FORMAT.BINARY) {
				long id1 = dis.readLong();
				long id2 = dis.readLong();
				p = new PairID(id1, id2);
				next = true;

			} else if (getType() == EXTERNAL_DATA_FORMAT.STRING) {
				String str;

				// read the pairs
				str = br.readLine();

				// if there is a blank line, return false
				if (str == null || str.equals(""))
					return false;

				StringTokenizer st = new StringTokenizer(str, " ");
				long id1 = 0;
				long id2 = 0;
				if (st.hasMoreTokens())
					id1 = Long.parseLong(st.nextToken());
				if (st.hasMoreTokens())
					id2 = Long.parseLong(st.nextToken());
				p = new PairID(id1, id2);
				next = true;

			}
		} catch (EOFException eofex) {
			// expecting an EOF
		} catch (IOException ioex) {
			new BlockingException(ioex.toString());
		}

		return next;
	}

}
