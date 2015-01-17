package com.choicemaker.cm.io.blocking.automated.offline.data;

/**
 * A MatchRecord has a pair of id's and a match probability on this pair.
 * 
 * @author pcheung
 * @deprecated
 */
@Deprecated
public interface IMatchRecord extends Comparable<IMatchRecord> {

	public static final char MATCH = 'M';
	public static final char DIFFER = 'D';
	public static final char HOLD = 'H';

	long getRecordID1();

	long getRecordID2();

	float getProbability();

	char getMatchType();

	char getRecord2Source();

}