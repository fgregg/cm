package com.choicemaker.cm.transitivity.server.impl;

import static com.choicemaker.cm.args.OabaLinkageType.MASTER_TO_MASTER_LINKAGE;
import static com.choicemaker.cm.args.OabaLinkageType.STAGING_DEDUPLICATION;
import static com.choicemaker.cm.args.OabaLinkageType.STAGING_TO_MASTER_LINKAGE;
import static com.choicemaker.cm.args.OabaLinkageType.TRANSITIVITY_ANALYSIS;
import static com.choicemaker.cm.transitivity.server.impl.TransitivityParametersJPA.DV_TRANS;
import static com.choicemaker.cm.transitivity.server.impl.TransitivityParametersJPA.JPQL_TRANSPARAMETERS_FIND_ALL;
import static com.choicemaker.cm.transitivity.server.impl.TransitivityParametersJPA.QN_TRANSPARAMETERS_FIND_ALL;

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
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractParametersEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;

@NamedQuery(name = QN_TRANSPARAMETERS_FIND_ALL,
		query = JPQL_TRANSPARAMETERS_FIND_ALL)
@Entity
@DiscriminatorValue(value = DV_TRANS)
public class TransitivityParametersEntity extends AbstractParametersEntity
		implements TransitivityParameters {

	private static final long serialVersionUID = 271L;

	public static final String OABA_ONLY_DUMP_TAG = "TP/BP PRECEDESSOR";

	public static final String COMMON_DUMP_TAG = "TP/BP";

	public static final String TRANS_ONLY_DUMP_TAG = "TP";

	/**
	 * Dumps the transitivity parameters and the parameters of an associated
	 * OABA job.
	 * 
	 * @param tp
	 *            must be non-null
	 * @param batchJob
	 * @param predecessorParams
	 *            (may be null)
	 */
	public static String dump(TransitivityParameters tp, BatchJob batchJob,
			OabaParameters predecessorParams) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		if (tp == null) {
			pw.println("null transitivity parameters");
		} else {
			pw.println("Transitivity parameters (TP)");
			pw.println("TP: Result format: " + tp.getAnalysisResultFormat());
			pw.println("TP: Graph property: " + tp.getGraphProperty());
			Long predecessorId = batchJob == null ? null : batchJob.getId();
			pw.println("TP: OABA predecessor: " + predecessorId);
			dumpDifferences(pw, tp, predecessorParams);
		}
		String retVal = sw.toString();
		return retVal;
	}

	protected static void dumpDifferences(PrintWriter pw,
			TransitivityParameters tp, OabaParameters predecessorParams) {
		assert pw != null;
		assert tp != null;
		if (predecessorParams != null) {
			if (predecessorParams.equals(tp)) {
				String s = dump(COMMON_DUMP_TAG, tp);
				pw.println(s);
			} else {
				String s = dump(OABA_ONLY_DUMP_TAG, predecessorParams);
				pw.println(s);
				dump(TRANS_ONLY_DUMP_TAG, tp);
				pw.println(s);
			}
		} else {
			pw.println(TRANS_ONLY_DUMP_TAG + ": null OABA precedessor params");
			String s = dump(TRANS_ONLY_DUMP_TAG, tp);
			pw.println(s);
		}
	}

	public static String dump(String tag, OabaParameters p) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		if (p == null) {
			OabaParametersEntity.dump(tag, p);
		} else {
			final OabaLinkageType task = p.getOabaLinkageType();
			if (task == STAGING_DEDUPLICATION) {
				OabaParametersEntity.dump(tag, p);
			} else if (task == STAGING_TO_MASTER_LINKAGE) {
				OabaParametersEntity.dump(tag, p);
			} else if (task == MASTER_TO_MASTER_LINKAGE) {
				OabaParametersEntity.dump(tag, p);
			} else if (task == TRANSITIVITY_ANALYSIS) {
				pw.println("Transitivity parameters (" + tag + ")");
				pw.println(tag + ": DIFFER threshold: " + p.getLowThreshold());
				pw.println(tag + ": MATCH threshold: " + p.getHighThreshold());
				pw.println(tag + ": Model configuration name: "
						+ p.getModelConfigurationName());
				pw.print(tag + ": Linkage task: " + task);
				pw.println(" (deduplicating a single record source)");
				pw.println(tag + ": Staging record source: " + p.getQueryRsId());
				pw.println(tag + ": Staging record source type: "
						+ p.getQueryRsType());
				pw.println(tag + ": Master record source: "
						+ p.getReferenceRsId());
				pw.println(tag + ": Master record source type: "
						+ p.getReferenceRsType());
			} else {
				throw new IllegalArgumentException("unexpected task type: "
						+ task);
			}
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
				DEFAULT_QUERY_RS_IS_DEDUPLICATED, master,
				DEFAULT_RESULT_FORMAT, DEFAULT_GRAPH_PROPERTY_NAME);
	}

	public TransitivityParametersEntity(String modelConfigurationName,
			float differThreshold, float matchThreshold,
			PersistableRecordSource stage, boolean isQueryRsDeduped, PersistableRecordSource master,
			AnalysisResultFormat format, String graphPropertyName) {
		super(DV_TRANS, modelConfigurationName, differThreshold,
				matchThreshold, stage.getId(), stage.getType(),isQueryRsDeduped,
				master == null ? null : master.getId(), master == null ? null
						: master.getType(),
				TRANSITIVITY_ANALYSIS, format == null ? null
						: format.name(), graphPropertyName);
		if (format == null) {
			throw new IllegalArgumentException("null analysis-result format");
		}
		if (graphPropertyName == null) {
			throw new IllegalArgumentException("null graph-property name");
		}
	}

	public TransitivityParametersEntity(TransitivityParameters tp) {
		super(DV_TRANS, tp.getModelConfigurationName(), tp.getLowThreshold(),
				tp.getHighThreshold(), tp.getQueryRsId(), tp.getQueryRsType(),
				tp.isQueryRsDeduplicated(), tp.getReferenceRsId(), tp
						.getReferenceRsType(),
				TRANSITIVITY_ANALYSIS, tp
						.getAnalysisResultFormat().name(), tp
						.getGraphProperty().getName());
		if (tp.getGraphProperty() == null) {
			throw new IllegalArgumentException("null graph-property");
		}
	}

	public TransitivityParametersEntity(OabaParameters p,
			AnalysisResultFormat format, String graphPropertyName) {
		super(DV_TRANS, p.getModelConfigurationName(), p.getLowThreshold(), p
				.getHighThreshold(), p.getQueryRsId(), p.getQueryRsType(), p
				.isQueryRsDeduplicated(), p.getReferenceRsId(), p
				.getReferenceRsType(), TRANSITIVITY_ANALYSIS, format.name(),
				graphPropertyName);
		if (graphPropertyName == null) {
			throw new IllegalArgumentException("null graph-property");
		}
	}

	@Override
	public String getModelConfigurationName() {
		return this.modelConfigName;
	}

	@Override
	public long getQueryRsId() {
		return this.queryRsId;
	}

	@Override
	public String getQueryRsType() {
		return this.queryRsType;
	}

	@Override
	public boolean isQueryRsDeduplicated() {
		return queryRsIsDeduplicated;
	}

	@Override
	public Long getReferenceRsId() {
		return this.referenceRsId;
	}

	@Override
	public String getReferenceRsType() {
		return this.referenceRsType;
	}

	@Override
	public AnalysisResultFormat getAnalysisResultFormat() {
		return AnalysisResultFormat.valueOf(format);
	}

	@Override
	public IGraphProperty getGraphProperty() {
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

	@Override
	public String toString() {
		return "TransitivityParametersEntity [id=" + id + ", uuid=" + getUUID()
				+ ", modelId=" + modelConfigName + ", lowThreshold="
				+ lowThreshold + ", highThreshold=" + highThreshold
				+ ", graph=" + graph + "]";
	}

}
