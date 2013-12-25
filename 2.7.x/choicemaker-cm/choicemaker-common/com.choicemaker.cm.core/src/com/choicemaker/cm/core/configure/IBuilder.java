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
package com.choicemaker.cm.core.configure;

import java.io.IOException;
import java.io.Reader;

/**
 * Based on the public methods of org.jdom.input.SAXBuilder
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 18:04:31 $
 */
public interface IBuilder {

	/**
	 * <p>
	 * This builds a document from the supplied
	 *   Reader.  It's the programmer's responsibility to make sure
	 *   the reader matches the encoding of the file.  It's always safer
	 *   to use an InputStream rather than a Reader, if it's available.
	 * </p>
	 *
	 * @param characterStream <code>Reader</code> to read from.
	 * @return <code>IDocument</code> - resultant IDocument object.
	 * @throws XmlSpecificationException when errors occur in parsing.
	 * @throws IOException when an I/O error prevents a document
	 *         from being fully parsed.
	 */
	public IDocument build(Reader characterStream) 
		throws XmlSpecificationException, IOException; 

}

