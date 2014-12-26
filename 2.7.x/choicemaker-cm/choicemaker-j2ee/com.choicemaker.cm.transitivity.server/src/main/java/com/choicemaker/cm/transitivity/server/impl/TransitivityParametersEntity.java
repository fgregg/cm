package com.choicemaker.cm.transitivity.server.impl;

import static com.choicemaker.cm.transitivity.server.impl.TransitivityParametersJPA.DISCRIMINATOR_VALUE;
import static com.choicemaker.cm.transitivity.server.impl.TransitivityParametersJPA.JPQL_TRANSPARAMS_FIND_ALL;
import static com.choicemaker.cm.transitivity.server.impl.TransitivityParametersJPA.QN_TRANSPARAMS_FIND_ALL;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;

import com.choicemaker.cm.args.AnalysisResultFormat;
import com.choicemaker.cm.args.IGraphProperty;
import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;

@NamedQuery(name = QN_TRANSPARAMS_FIND_ALL, query = JPQL_TRANSPARAMS_FIND_ALL)
@Entity
@DiscriminatorValue(value = DISCRIMINATOR_VALUE)
public class TransitivityParametersEntity extends OabaParametersEntity
		implements TransitivityParameters {

	private static final long serialVersionUID = 271L;

	public static boolean isPersistent(TransitivityParameters params) {
		throw new Error("not yet implemented");
	}

	public static String dump(TransitivityParameters tp) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		
		if (tp == null) {
			pw.println("null batch parameters");
		} else {
			final OabaLinkageType task = tp.getOabaLinkageType();
			pw.println("Linkage task: " + task);
			if (task == OabaLinkageType.STAGING_DEDUPLICATION) {
				pw.println("Deduplicating a single record source");
				pw.println("Staging record source: " + tp.getStageRsId());
			} else if (task == OabaLinkageType.STAGING_TO_MASTER_LINKAGE) {
				pw.println("Linking a staging source to a master source");
				pw.println("Staging record source: " + tp.getStageRsId());
				pw.println("Master record source: " + tp.getMasterRsId());
			} else if (task == OabaLinkageType.MASTER_TO_MASTER_LINKAGE) {
				pw.println("Linking a master source to a master source");
				pw.println("Master record source: " + tp.getStageRsId());
				pw.println("Master record source: " + tp.getMasterRsId());
			} else if (task == OabaLinkageType.TRANSITIVITY_ANALYSIS) {
				pw.println("Transitivity analysis");
				pw.println("Staging record source: " + tp.getStageRsId());
				pw.println("Master record source: " + tp.getMasterRsId());
			} else {
				throw new IllegalArgumentException("unexpected task type: " + task);
			}
			pw.println("DIFFER threshold: " + tp.getLowThreshold());
			pw.println("MATCH threshold: " + tp.getHighThreshold());
			pw.println("Model configuration id: "
					+ tp.getModelConfigurationName());
			pw.println("Result format: " + tp.getAnalysisResultFormat());
			pw.println("Graph property: " + tp.getGraphProperty());
		}
		String retVal = sw.toString();
		return retVal;
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
		super(DISCRIMINATOR_VALUE, modelConfigurationName, differThreshold,
				matchThreshold, stage.getId(), stage.getType(),
				master == null ? null : master.getId(), master == null ? null
						: master.getType(),
				OabaLinkageType.TRANSITIVITY_ANALYSIS, format == null ? null
						: format.name(), graphPropertyName);
		if (format == null) {
			throw new IllegalArgumentException("null analysis-result format");
		}
		if (graphPropertyName == null) {
			throw new IllegalArgumentException("null graph-property name");
		}
	}

	public TransitivityParametersEntity(TransitivityParameters tp) {
		super(DISCRIMINATOR_VALUE, tp.getModelConfigurationName(), tp
				.getLowThreshold(), tp.getHighThreshold(), tp.getStageRsId(),
				tp.getStageRsType(), tp.getMasterRsId(), tp.getMasterRsType(),
				OabaLinkageType.TRANSITIVITY_ANALYSIS, tp
						.getAnalysisResultFormat().name(), tp
						.getGraphProperty().getName());
		if (tp.getGraphProperty() == null) {
			throw new IllegalArgumentException("null graph-property");
		}
	}

	public TransitivityParametersEntity(OabaParameters tp, AnalysisResultFormat format, String graphPropertyName) {
		super(DISCRIMINATOR_VALUE, tp.getModelConfigurationName(), tp
				.getLowThreshold(), tp.getHighThreshold(), tp.getStageRsId(),
				tp.getStageRsType(), tp.getMasterRsId(), tp.getMasterRsType(),
				OabaLinkageType.TRANSITIVITY_ANALYSIS, format.name(), graphPropertyName);
		if (graphPropertyName == null) {
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
		// FIXME not yet implemented
		return new IGraphProperty() {

			private static final long serialVersionUID = 271L;

			@Override
			public String getName() {
				return graph;
			}
			
			@Override
			public String toString() {
				return getName();
			}
			
		};
	}

}
