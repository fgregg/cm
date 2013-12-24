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

//import com.choicemaker.cm.core.Accessor;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;

import com.choicemaker.cm.urm.base.ConnectedRecordSet;
import com.choicemaker.cm.urm.base.GlobalRecordRef;
import com.choicemaker.cm.urm.base.IRecordHolder;
import com.choicemaker.cm.urm.base.IRecordVisitor;
import com.choicemaker.cm.urm.base.LinkedRecordSet;
import com.choicemaker.cm.urm.base.RecordRef;

/**
 * @author emoussikaev
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class InternalRecordBuilder implements IRecordVisitor {

	Record		resRec;
	ImmutableProbabilityModel model;
	public InternalRecordBuilder(ImmutableProbabilityModel model){
		this.model = model;
	}
	/* (non-Javadoc)
	 * @see com.choicemaker.cm.urm.base.IRecordVisitor#visit(com.choicemaker.cm.urm.base.IRecordHolder)
	 */
	public void visit(IRecordHolder rh) {
		if(rh == null){
			resRec = null;
			return;
		}
		resRec = model.getAccessor().toImpl(rh);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.urm.base.IRecordVisitor#visit(com.choicemaker.cm.urm.base.RecordRef)
	 */
	public void visit(RecordRef rRef) {
		if(rRef == null){
			resRec = null;
			return;
		}
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.urm.base.IRecordVisitor#visit(com.choicemaker.cm.urm.base.GlobalRecordRef)
	 */
	public void visit(GlobalRecordRef grRef) {
		if(grRef == null){
			resRec = null;
			return;
		}
		// TODO retrive record from DB

	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.urm.base.IRecordVisitor#visit(com.choicemaker.cm.urm.base.LinkedRecordSet)
	 */
	public void visit(LinkedRecordSet lrs) {
		if(lrs == null){
			resRec = null;
			return;
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.urm.base.IRecordVisitor#visit(com.choicemaker.cm.urm.base.ConnectedRecordSet)
	 */
	public void visit(ConnectedRecordSet crs) {
		if(crs == null){
			resRec = null;
		return;
}
	}

	/**
	 * @return
	 */
	public Record getResultRecord() {
		return resRec;
	}

}
