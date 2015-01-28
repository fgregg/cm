package com.choicemaker.cm.batch;

import java.util.List;

import javax.ejb.Local;

/**
 * Manages persistent, operational properties.
 * 
 * @author rphall
 */
@Local
public interface OperationalPropertyController {

	void setJobProperty(BatchJob job, String pn, String pv);

	String getJobProperty(BatchJob job, String pn);

	OperationalProperty save(OperationalProperty property);

	void remove(OperationalProperty property);

	OperationalProperty find(long propertyId);

	OperationalProperty find(BatchJob job, String name);

	List<OperationalProperty> findAllByJob(BatchJob job);

	/** Returns a count of the number of events deleted */
	int deleteOperationalPropertiesByJobId(long jobId);

	List<OperationalProperty> findAllOperationalProperties();

}
