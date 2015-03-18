package com.choicemaker.cm.transitivity.server.ejb;

import java.util.List;

import javax.ejb.Local;

import com.choicemaker.cm.args.TransitivityParameters;

@Local
public interface TransitivityParametersController {

	void delete(TransitivityParameters tp);

	void detach(TransitivityParameters tp);

	List<TransitivityParameters> findAllTransitivityParameters();

	TransitivityParameters findTransitivityParameters(long id);

	TransitivityParameters findTransitivityParametersByBatchJobId(long jobId);

	TransitivityParameters save(TransitivityParameters tp);

}
