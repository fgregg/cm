/*
 * RecordType.java       Revision: 2.5  Date: Sep 9, 2005 3:53:13 PM 
 *
 * Copyright (c) 2001 ChoiceMaker Technologies, Inc.
 * 48 Wall Street, 11th Floor, New York, NY 10005
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * ChoiceMaker Technologies Inc. ("Confidential Information").
 */
package com.choicemaker.cm.args;


/**
 * A type of the record object
 * <ul>
 * <li>NONE no data</li>
 * <li>HOLDER record holder object</li>
 * <li>REF record reference object</li>
 * <li>GLOBAL_REF record global reference object</li>
 * </ul>
 * <p>
 *
 * @author emoussikaev
 * @version Revision: 2.5 Date: Nov 1, 2005 2:09:30 PM
 */
public enum RecordType {
	NONE, HOLDER, REF, GLOBAL_REF
}
