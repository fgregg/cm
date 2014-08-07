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
package com.choicemaker.cm.urm.ejb;

import com.choicemaker.cm.urm.base.EvalRecordFormat;
import com.choicemaker.cm.urm.base.RecordType;
import com.choicemaker.cm.urm.base.ScoreType;
import com.choicemaker.cm.urm.base.SubsetDbRecordCollection;
import com.choicemaker.cm.urm.exceptions.RecordCollectionException;

/**
 * @author emoussikaev
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class test {

	public static void main(String[] args) {
		try {
			EvalRecordFormat erf = new EvalRecordFormat(ScoreType.NO_NOTE,RecordType.REF);
			SubsetDbRecordCollection qRs = new SubsetDbRecordCollection("","",10,""); 
			SerialRecordSourceBuilder	rcb = new SerialRecordSourceBuilder(null,true);
			qRs.accept(rcb);
		} catch (RecordCollectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}
}
