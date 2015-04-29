package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import java.util.List;

import javax.ejb.Local;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersEntity;

@Local
public interface OabaParametersController {

	void delete(OabaParameters p);

	void detach(OabaParameters p);

	/** Find only instances of OabaParametersEntity (but no subclasses) */
	List<OabaParameters> findAllOabaParameters();

	/** Find all instances of AbstractParametersEntity and subclasses */
	List<AbstractParametersEntity> findAllParameters();

	/** Find only an instance of OabaParametersEntity (but not any subclass) */
	OabaParameters findOabaParameters(long id);

	/** Find any instance of AbstractParametersEntity or its subclasses */
	AbstractParametersEntity findParameters(long id);

	OabaParameters findOabaParametersByBatchJobId(long jobId);

	OabaParameters save(OabaParameters p);

}