package com.choicemaker.cm.batch;

import java.io.Serializable;

import com.choicemaker.cm.args.PersistentObject;

/**
 * An operational property is some property computed during a batch job that
 * needs to be retained temporarily.<br/>
 * <br/>
 * After a job is successfully completed, operational properties can be
 * discarded without loss of information needed to repeat the job. However, if a
 * job fails or is aborted, operational properties may be needed to resume the
 * job at the point where it stopped.
 *
 * @author rphall
 */
public interface OperationalProperty extends PersistentObject, Serializable {

	/** The identifier of the job that owns this operational property */
	long getJobId();

	/** The property name */
	String getName();

	/** The property value */
	String getValue();

	void updateValue(String v);

}