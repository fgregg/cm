/*
 * GroupMatchType.java       Revision: 2.5  Date: Sep 9, 2005 2:53:25 PM
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
 * The type of an OABA job:<ul>
 * <li>Deduplication of a single record source (the staging source)</li>
 * <li>Linkage of a staging source to a master source</li>
 * <li>Linkage of a master source to another master source</li>
 * <li>Transitivity analysis (<em>TA</em>) of the above OABA job types</li>
 * </ul>
 * <p><strong>Staging deduplication</strong></p>
 * <p>
 * In this type of a job, a single record source is matched against itself.
 * The OABA service finds duplicate records within the source.
 * </p>
 * <p><strong>Staging to master linkage</strong></p>
 * <p>
 * In this type of a job, a staging record source is linked against a master
 * record source. The staging source is assumed to contain duplicates within
 * itself, whereas the master source is assumed to be already de-duplicated.
 * The OABA service finds duplicate records within the staging source and between
 * the staging source and the master source. The OABA service does not look for
 * duplicates within the master source.
 * </p>
 * <p><strong>Master to master linkage</strong></p>
 * <p>
 * In this type of a job, both record sources are assumed to be already
 * de-duplicated. The OABA service finds duplicate records between, but not
 * within, the sources.
 * </p>
 */
public enum OabaLinkageType {

	STAGING_DEDUPLICATION, STAGING_TO_MASTER_LINKAGE, MASTER_TO_MASTER_LINKAGE,
	TA_STAGING_DEDUPLICATION, TA_STAGING_TO_MASTER_LINKAGE,
	TA_MASTER_TO_MASTER_LINKAGE;

	public static OabaLinkageType transitivityAnalysis(OabaLinkageType oaba) {
		if (oaba == null) {
			throw new IllegalArgumentException("null linkage type");
		}

		OabaLinkageType retVal = null;
		switch(oaba) {
		case STAGING_DEDUPLICATION:
			retVal = TA_STAGING_DEDUPLICATION;
			break;
		case STAGING_TO_MASTER_LINKAGE:
			retVal = TA_STAGING_TO_MASTER_LINKAGE;
			break;
		case MASTER_TO_MASTER_LINKAGE:
			retVal = TA_MASTER_TO_MASTER_LINKAGE;
			break;
		default:
			retVal = oaba;
			break;
		}
		assert retVal != null;

		return retVal;
	}

}
