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
package com.choicemaker.cm.matching.wfst.eclipse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import com.choicemaker.cm.matching.wfst.AmbiguousParser;
import com.choicemaker.cm.matching.wfst.*;

/**
 * @author rphall
 */
class LazyWfstParser implements AmbiguousParser {

	private String name;
	private URL filterUrl, grammarUrl;

	private AmbiguousParser parser;

	public LazyWfstParser(String name, URL filterUrl, URL grammarUrl) {
		this.name = name;
		this.filterUrl = filterUrl;
		this.grammarUrl = grammarUrl;
		this.parser = null;
	}

	protected synchronized void init() {
		if (parser == null) {
			InputStream ifs = null;
			InputStream igs = null;
			try {
				ifs = filterUrl.openStream();
				igs = grammarUrl.openStream();
				parser = WfstParser.readWfstParser(ifs, igs);
				WfstParsers.addWfstParser(name, parser);
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				if (ifs != null) {
					try {
						ifs.close();
						ifs = null;
					} catch (IOException x) {
						// TODO better error handling
						x.printStackTrace();
					}
				}
				if (igs != null) {
						try {
							igs.close();
							igs = null;
						} catch (IOException x) {
							// TODO better error handling
							x.printStackTrace();
						}
					}
			} // finally
		} // if parser
		
		return;
	} // init()

	public boolean isWeighted() {
		init();
		return parser.isWeighted();
	}

	public List parse(String text) {
		init();
		return parser.parse(text);
	}

} // LazyWfstParser

