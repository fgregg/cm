package com.choicemaker.cm.transitivity.server.impl;

import com.choicemaker.cm.args.AnalysisResultFormat;
import com.choicemaker.cm.args.IGraphProperty;
import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;

public class TransitivityParametersEntity extends OabaParametersEntity
		implements TransitivityParameters {

	private static final long serialVersionUID = 271L;

	public static boolean isPersistent(TransitivityParameters params) {
		throw new Error("not yet implemented");
	}

	protected TransitivityParametersEntity() {
		super();
	}

	public TransitivityParametersEntity(String modelConfigurationName,
			float differThreshold, float matchThreshold,
			PersistableRecordSource stage, PersistableRecordSource master) {
		this(modelConfigurationName, differThreshold, matchThreshold, stage,
				master, DEFAULT_RESULT_FORMAT, DEFAULT_GRAPH_PROPERTY_NAME);
	}

	public TransitivityParametersEntity(String modelConfigurationName,
			float differThreshold, float matchThreshold,
			PersistableRecordSource stage, PersistableRecordSource master,
			AnalysisResultFormat format, String graphPropertyName) {
		super(modelConfigurationName, differThreshold, matchThreshold, stage
				.getId(), stage.getType(), master == null ? null : master.getId(),
				master == null ? null : master.getType(),
						OabaLinkageType.TRANSITIVITY_ANALYSIS, format == null ? null : format.name(), graphPropertyName);
		if (format == null) {
			throw new IllegalArgumentException("null analysis-result format");
		}
		if (graphPropertyName == null) {
			throw new IllegalArgumentException("null graph-property name");
		}
	}

	public TransitivityParametersEntity(TransitivityParameters tp) {
		super(tp.getModelConfigurationName(), tp.getLowThreshold(), tp
				.getHighThreshold(), tp.getStageRsId(), tp.getStageRsType(), tp
				.getMasterRsId(), tp.getMasterRsType(), OabaLinkageType.TRANSITIVITY_ANALYSIS, tp
				.getAnalysisResultFormat() == null ? null : tp
						.getAnalysisResultFormat().name(), tp.getGraphProperty()
				.getName());
		if (tp.getAnalysisResultFormat() == null) {
			throw new IllegalArgumentException("null analysis-result format");
		}
		if (tp.getGraphProperty() == null) {
			throw new IllegalArgumentException("null graph-property");
		}
	}

	@Override
	public AnalysisResultFormat getAnalysisResultFormat() {
		return AnalysisResultFormat.valueOf(format);
	}

	@Override
	public IGraphProperty getGraphProperty() {
		// TODO Auto-generated method stub
		throw new Error("not yet implemented");
	}

}
