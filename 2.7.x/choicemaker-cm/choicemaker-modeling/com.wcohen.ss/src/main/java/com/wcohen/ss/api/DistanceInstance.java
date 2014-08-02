package com.wcohen.ss.api;

import java.io.Serializable;
import java.util.Comparator;

/**
 * An 'instance' for a StringDistance, analogous to an 'instance' for
 * a classification learner.  Consists of a pair of StringWrappers,
 * a distance, and some labeling information.
 */
public interface DistanceInstance extends Serializable {

	public StringWrapper getA();
	public StringWrapper getB();
	public boolean isCorrect();
	public double getDistance();
	public void setDistance(double distance);

	public static final Comparator INCREASING_DISTANCE = new Comparator() {
			public int compare(Object aObj, Object bObj) {
				if (aObj instanceof DistanceInstance
					&& bObj instanceof DistanceInstance) {
					DistanceInstance a = (DistanceInstance) aObj;
					DistanceInstance b = (DistanceInstance) bObj;
					if (a.getDistance() > b.getDistance()) return -1;
					else if (a.getDistance() < b.getDistance()) return +1;
					else return 0;
			} else {
				throw new ClassCastException("incompatible types");
			}
		}
	};
}
