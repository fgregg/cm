package com.choicemaker.cm.transitivity.server.impl;

import java.util.List;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersEntity;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityParametersController;

public class CombinedParametersController implements
		OabaParametersController {

	private final OabaParametersController o;
	private final TransitivityParametersController t;

	CombinedParametersController(OabaParametersController o,
			TransitivityParametersController t) {
		if (o == null || t == null) {
			throw new IllegalArgumentException("null argument");
		}
		this.o = o;
		this.t = t;
	}

	@Override
	public void delete(OabaParameters p) {
		if (p instanceof TransitivityParameters) {
			t.delete((TransitivityParameters) p);
		} else {
			this.o.delete(p);
		}
	}

	@Override
	public void detach(OabaParameters p) {
		if (p instanceof TransitivityParameters) {
			t.detach((TransitivityParameters) p);
		} else {
			this.o.detach(p);
		}
	}

	@Override
	public List<OabaParameters> findAllOabaParameters() {
		List<TransitivityParameters> tps =
			t.findAllTransitivityParameters();
		List<OabaParameters> ops = o.findAllOabaParameters();
		ops.addAll(tps);
		return ops;
	}

	@Override
	public OabaParameters findOabaParameters(long id) {
		OabaParameters retVal = o.findOabaParameters(id);
		if (retVal == null) {
			retVal = t.findTransitivityParameters(id);
		} else {
			assert t.findTransitivityParameters(id) == null;
		}
		return retVal;
	}

	@Override
	public OabaParameters findOabaParametersByBatchJobId(long jobId) {
		OabaParameters retVal = o.findOabaParametersByBatchJobId(jobId);
		if (retVal == null) {
			retVal = t.findTransitivityParametersByBatchJobId(jobId);
		} else {
			assert t.findTransitivityParametersByBatchJobId(jobId) == null;
		}
		return retVal;
	}

	@Override
	public OabaParameters save(OabaParameters p) {
		OabaParameters retVal;
		if (p instanceof TransitivityParameters) {
			retVal = t.save((TransitivityParameters) p);
		} else {
			retVal = this.o.save(p);
		}
		return retVal;
	}

	@Override
	public List<AbstractParametersEntity> findAllParameters() {
		return o.findAllParameters();
	}

	@Override
	public AbstractParametersEntity findParameters(long id) {
		return o.findParameters(id);
	}

	@Override
	public String getQueryDatabaseConfiguration(OabaParameters oabaParams) {
		return o.getQueryDatabaseConfiguration(oabaParams);
	}

	@Override
	public String getQueryDatabaseAccessor(OabaParameters oabaParams) {
		return o.getQueryDatabaseAccessor(oabaParams);
	}

	@Override
	public String getReferenceDatabaseConfiguration(OabaParameters oabaParams) {
		return o.getReferenceDatabaseConfiguration(oabaParams);
	}

	@Override
	public String getReferenceDatabaseAccessor(OabaParameters oabaParams) {
		return o.getReferenceDatabaseAccessor(oabaParams);
	}

}