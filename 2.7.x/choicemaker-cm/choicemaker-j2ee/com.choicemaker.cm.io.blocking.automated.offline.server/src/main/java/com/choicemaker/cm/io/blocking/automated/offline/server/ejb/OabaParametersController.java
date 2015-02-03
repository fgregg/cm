package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import java.util.List;

import javax.ejb.Local;

import com.choicemaker.cm.args.OabaParameters;

@Local
public interface OabaParametersController {

	void delete(OabaParameters p);

	void detach(OabaParameters p);

	List<OabaParameters> findAllOabaParameters();

	OabaParameters findOabaParameters(long id);

	OabaParameters findOabaParametersByJobId(long jobId);

	OabaParameters save(OabaParameters p);

}