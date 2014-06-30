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
package com.choicemaker.cm.io.xml.base;

import java.util.ArrayList;
import java.util.List;

import com.choicemaker.cm.core.base.Constants;
import com.choicemaker.cm.core.base.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.Sink;
import com.choicemaker.cm.core.base.SinkFactory;
import com.choicemaker.cm.core.base.Source;

/**
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 09:18:16 $
 */

public class XmlRecordSinkFactory implements SinkFactory {
    private String fileNameBase;
    private String xmlFileName;
    private String extension;
    private ImmutableProbabilityModel model;
    private int num;
    private List sources;

    public XmlRecordSinkFactory(String fileNameBase, String xmlFileName, String extension, ImmutableProbabilityModel model) {
        this.fileNameBase = fileNameBase;
        this.xmlFileName = xmlFileName;
        this.extension = extension;
        this.model = model;
        this.sources = new ArrayList();
    }

    public Sink getSink() {
        String tName = fileNameBase + num + "." + Constants.RS_EXTENSION;
        String tFileName = xmlFileName + num + extension;
        ++num;
        sources.add(new XmlRecordSource(tName, tFileName, model));
        return new XmlRecordSink(tName, tFileName, model);
    }

    public Source[] getSources() {
        return (Source[]) sources.toArray(new Source[sources.size()]);
    }
}
