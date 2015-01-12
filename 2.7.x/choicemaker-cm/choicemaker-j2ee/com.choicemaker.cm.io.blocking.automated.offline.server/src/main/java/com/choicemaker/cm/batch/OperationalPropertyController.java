package com.choicemaker.cm.batch;

import java.util.List;

import javax.ejb.Local;

import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobEntity;

/**
 * Manages persistent, operational properties.
 * 
 * @author rphall
 */
@Local
public interface OperationalPropertyController {

	void setJobProperty(OabaJobEntity job, String pn, String pv);

	String getJobProperty(OabaJobEntity job, String pn);

	OperationalProperty save(OperationalProperty property);

	OperationalProperty update(OperationalProperty property);

	void remove(OperationalProperty property);

	OperationalProperty find(long propertyId);

	OperationalProperty find(BatchJob job, String name);

	List<OperationalProperty> findAllByJob(BatchJob job);

}
