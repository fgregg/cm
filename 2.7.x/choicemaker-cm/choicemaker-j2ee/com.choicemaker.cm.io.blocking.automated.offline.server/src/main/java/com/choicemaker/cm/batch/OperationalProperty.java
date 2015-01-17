package com.choicemaker.cm.batch;

import static com.choicemaker.cm.batch.impl.OperationalPropertyJPA.TABLE_NAME;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

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
@Entity
@Table(/* schema = "CHOICEMAKER", */name = TABLE_NAME)
public interface OperationalProperty extends Serializable {

	/** Default persistence key of an ephemeral operational property */
	long INVALID_ID = 0;

	/** Physical persistence key */
	long getId();

	/** The identifier of the job that owns this operational property */
	long getJobId();

	/** The property name */
	String getName();

	/** The property value */
	String getValue();

	void updateValue(String v);

}