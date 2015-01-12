package com.choicemaker.cm.batch;

import java.util.List;

import javax.ejb.Local;

import com.choicemaker.cm.batch.impl.OperationalPropertyEntity;

/**
 * Manages persistent, operational properties.
 * 
 * @author rphall
 */
@Local
public interface OperationalPropertyController {

	OperationalProperty save(OperationalProperty property);

	OperationalProperty update(OperationalProperty property);

	void remove(OperationalProperty property);

	void remove(OperationalPropertyEntity ope);

	OperationalProperty find(long propertyId);

	OperationalProperty find(BatchJob job, String name);

	List<OperationalProperty> findAllByJob(BatchJob job);

}
