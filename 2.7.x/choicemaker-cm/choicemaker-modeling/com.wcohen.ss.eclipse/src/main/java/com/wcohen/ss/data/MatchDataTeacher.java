package com.wcohen.ss.data;

import java.util.Collections;

import com.wcohen.ss.BasicDistanceInstanceIterator;
import com.wcohen.ss.api.DistanceInstance;
import com.wcohen.ss.api.DistanceInstanceIterator;
import com.wcohen.ss.api.StringDistanceTeacher;
import com.wcohen.ss.api.StringWrapperIterator;

/**
 * Train a StringDistanceLearner using MatchData and a Blocker. 
 *
 */
public class MatchDataTeacher extends StringDistanceTeacher
{
	private static final long serialVersionUID = 1L;
	private Blocker blocker;
	private MatchData data;

	public MatchDataTeacher(MatchData data,Blocker blocker) {
		this.blocker = blocker;
		this.data = data;
	}

	public StringWrapperIterator stringWrapperIterator() 
	{
		return data.getIterator();
	}

	public DistanceInstanceIterator distanceInstancePool()
	{
		return new BasicDistanceInstanceIterator(Collections.EMPTY_SET.iterator() );
	}

	public DistanceInstanceIterator distanceExamplePool() 
	{
		blocker.block(data);
		return new DistanceInstanceIterator() {
				private static final long serialVersionUID = 1L;
				private int cursor=0;
				public boolean hasNext() { return cursor<blocker.size(); }
				public Object next() { return blocker.getPair( cursor++ ); }
				public void remove() { throw new UnsupportedOperationException(); }
				public DistanceInstance nextDistanceInstance() { return (DistanceInstance)next();}
			};
	}

	public DistanceInstance labelInstance(DistanceInstance distanceInstance) 
	{	
		return distanceInstance;
	}

	public boolean hasAnswers() 
	{ 
		return true; 
	}
}
