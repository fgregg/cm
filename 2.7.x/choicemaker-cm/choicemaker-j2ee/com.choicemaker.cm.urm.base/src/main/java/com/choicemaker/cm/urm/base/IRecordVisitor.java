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
package com.choicemaker.cm.urm.base;

import java.io.Serializable;

/**
 * A record visitor.   
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 1:30:27 PM
 * @see
 */
public interface IRecordVisitor extends Serializable {
	void visit(IRecordHolder rh);
	void visit(RecordRef rRef);
	void visit(GlobalRecordRef grRef);
	void visit(LinkedRecordSet lrs);
	void visit(ConnectedRecordSet crs);
}
