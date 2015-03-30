package com.choicemaker.cmit.utils;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import com.choicemaker.cm.core.Accessor;
import com.choicemaker.cm.core.ClueSet;
import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.MachineLearner;
import com.choicemaker.cm.core.base.Evaluator;
import com.choicemaker.cm.core.report.Report;
import com.choicemaker.cm.core.util.Signature;
import com.choicemaker.util.SystemPropertyUtils;

public class MutableProbabilityModelStub implements IProbabilityModel {

	private static final String CLASSNAME = MutableProbabilityModelStub.class
			.getName();

	private static final Logger log = Logger.getLogger(CLASSNAME);

	public final String modelName = MutableProbabilityModelStub.class
			.getSimpleName() + "_" + new Date().getTime();

	public final String evaluatorSignature = Signature
			.calculateSignature(modelName + "_Evaluator");

	public final String cluesetName = modelName + "_ClueSet";

	public final String cluesetSignature = Signature
			.calculateSignature(cluesetName);

	public final String schemaName = modelName + "_Schema";

	public final String schemaSignature = Signature
			.calculateSignature(schemaName);

	public final String modelSignature = Signature.calculateSignature(
			evaluatorSignature, cluesetSignature, schemaSignature);

	private String userName = System.getProperty(SystemPropertyUtils.USER_NAME);

	// Instance data that can be tweaked for testing
	public String accessorClassName;
	public String antCommand;
	public String blockingConfigurationName;
	public String clueFilePath;
	public String databaseAbstractionName;
	public String databaseAccessorName;
	public String databaseConfigurationName;
	public String modelFilePath;
	public String trainingSource;

	public MutableProbabilityModelStub() {
		log.exiting(CLASSNAME, "ctor");
	}

	@Override
	public Accessor getAccessor() {
		log.exiting(CLASSNAME, "getAccessor", null);
		return null;
	}

	@Override
	public String getAccessorClassName() {
		log.exiting(CLASSNAME, "getAccessorClassName", accessorClassName);
		return accessorClassName;
	}

	@Override
	public int activeSize() {
		log.exiting(CLASSNAME, "activeSize", 0);
		return 0;
	}

	@Override
	public int activeSize(Decision d) {
		log.entering(CLASSNAME, "activeSize", d);
		log.exiting(CLASSNAME, "activeSize", 0);
		return 0;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener l) {
		log.entering(CLASSNAME, "addPropertyChangeListener", l);
		log.exiting(CLASSNAME, "addPropertyChangeListener");

	}

	@Override
	public boolean canEvaluate() {
		log.exiting(CLASSNAME, "canEvaluate", false);
		return false;
	}

	@Override
	public void changedCluesToEvaluate() {
		log.exiting(CLASSNAME, "changedCluesToEvaluate");

	}

	@Override
	public String getAntCommand() {
		log.exiting(CLASSNAME, "getAntCommand", antCommand);
		return antCommand;
	}

	@Override
	public String getBlockingConfigurationName() {
		log.exiting(CLASSNAME, "getBlockingConfigurationName",
				blockingConfigurationName);
		return blockingConfigurationName;
	}

	@Override
	public String getClueFilePath() {
		log.exiting(CLASSNAME, "getClueFilePath", clueFilePath);
		return clueFilePath;
	}

	@Override
	public ClueSet getClueSet() {
		log.exiting(CLASSNAME, "getClueSet", null);
		return null;
	}

	@Override
	public String getClueSetName() {
		log.exiting(CLASSNAME, "getClueSetName", cluesetName);
		return cluesetName;
	}

	@Override
	public String getClueSetSignature() {
		log.exiting(CLASSNAME, "getClueSetSignature", cluesetSignature);
		return cluesetSignature;
	}

	@Override
	public boolean[] getCluesToEvaluate() {
		log.exiting(CLASSNAME, "getCluesToEvaluate", null);
		return null;
	}

	@Override
	public String getClueText(int clueNum) throws IOException {
		log.entering(CLASSNAME, "getClueText", clueNum);
		log.exiting(CLASSNAME, "getClueText", null);
		return null;
	}

	@Override
	public String getDatabaseAbstractionName() {
		log.exiting(CLASSNAME, "getDatabaseAbstractionName",
				databaseAbstractionName);
		return databaseAbstractionName;
	}

	@Override
	public String getDatabaseAccessorName() {
		log.exiting(CLASSNAME, "getDatabaseAccessorName",
				databaseAccessorName);
		return databaseAccessorName;
	}

	@Override
	public String getDatabaseConfigurationName() {
		log.exiting(CLASSNAME, "getDatabaseConfigurationName",
				databaseConfigurationName);
		return databaseConfigurationName;
	}

	@Override
	public int getDecisionDomainSize() {
		log.exiting(CLASSNAME, "getDecisionDomainSize", 0);
		return 0;
	}

	@Override
	public Evaluator getEvaluator() {
		log.exiting(CLASSNAME, "getEvaluator", null);
		return null;
	}

	@Override
	public String getEvaluatorSignature() {
		log.exiting(CLASSNAME, "getEvaluatorSignature", evaluatorSignature);
		return evaluatorSignature;
	}

	@Override
	public int getFiringThreshold() {
		log.exiting(CLASSNAME, "getFiringThreshold", 0);
		return 0;
	}

	@Override
	public Date getLastTrainingDate() {
		log.exiting(CLASSNAME, "getLastTrainingDate", null);
		return null;
	}

	@Override
	public MachineLearner getMachineLearner() {
		log.exiting(CLASSNAME, "getMachineLearner", null);
		return null;
	}

	@Override
	public String getModelFilePath() {
		log.exiting(CLASSNAME, "getModelFilePath", modelFilePath);
		return modelFilePath;
	}

	@Override
	public String getModelName() {
		log.exiting(CLASSNAME, "getModelName", modelName);
		return modelName;
	}

	@Override
	public String getModelSignature() {
		log.exiting(CLASSNAME, "getModelSignature", modelSignature);
		return modelSignature;
	}

	@Override
	public String getSchemaName() {
		log.exiting(CLASSNAME, "getSchemaName", schemaName);
		return schemaName;
	}

	@Override
	public String getSchemaSignature() {
		log.exiting(CLASSNAME, "getSchemaSignature", schemaSignature);
		return schemaSignature;
	}

	@Override
	public boolean[] getTrainCluesToEvaluate() {
		log.exiting(CLASSNAME, "getTrainCluesToEvaluate", null);
		return null;
	}

	@Override
	public String getTrainingSource() {
		log.exiting(CLASSNAME, "getTrainingSource", trainingSource);
		return trainingSource;
	}

	@Override
	public String getUserName() {
		log.exiting(CLASSNAME, "getUserName", userName);
		return userName;
	}

	@Override
	public boolean isEnableAllCluesBeforeTraining() {
		log.exiting(CLASSNAME, "isEnableAllCluesBeforeTraining", false);
		return false;
	}

	@Override
	public boolean isEnableAllRulesBeforeTraining() {
		log.exiting(CLASSNAME, "isEnableAllRulesBeforeTraining", false);
		return false;
	}

	@Override
	public boolean isTrainedWithHolds() {
		log.exiting(CLASSNAME, "isTrainedWithHolds", false);
		return false;
	}

	@Override
	public boolean isUseAnt() {
		log.exiting(CLASSNAME, "isUseAnt", false);
		return false;
	}

	@Override
	public void machineLearnerChanged(Object oldValue, Object newValue) {
		log.entering(CLASSNAME, "machineLearnerChanged", new Object[] {
				oldValue, newValue });
		log.exiting(CLASSNAME, "machineLearnerChanged");

	}

	@Override
	public boolean needsRecompilation() {
		log.exiting(CLASSNAME, "needsRecompilation", false);
		return false;
	}

	@Override
	public int numTrainCluesToEvaluate() {
		log.exiting(CLASSNAME, "numTrainCluesToEvaluate", 0);
		return 0;
	}

	@Override
	public Map<String, String> properties() {
		log.exiting(CLASSNAME, "properties", null);
		return null;
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener l) {
		log.entering(CLASSNAME, "removePropertyChangeListener", l);
		log.exiting(CLASSNAME, "removePropertyChangeListener");

	}

	@Override
	public void report(Report report) throws IOException {
		log.entering(CLASSNAME, "report", report);
		log.exiting(CLASSNAME, "report");

	}

	@Override
	public void beginMultiPropertyChange() {
		log.exiting(CLASSNAME, "beginMultiPropertyChange");

	}

	@Override
	public void endMultiPropertyChange() {
		log.exiting(CLASSNAME, "endMultiPropertyChange");

	}

	@Override
	public void setAccessor(Accessor newAcc) {
		log.entering(CLASSNAME, "setAccessor", newAcc);
		log.exiting(CLASSNAME, "setAccessor");

	}

	@Override
	public void setAntCommand(String v) {
		log.entering(CLASSNAME, "setAntCommand", v);
		log.exiting(CLASSNAME, "setAntCommand");

	}

	@Override
	public void setCluesToEvaluate(boolean[] cluesToEvaluate)
			throws IllegalArgumentException {
		log.entering(CLASSNAME, "setCluesToEvaluate", cluesToEvaluate);
		log.exiting(CLASSNAME, "setCluesToEvaluate");

	}

	@Override
	public void setEnableAllCluesBeforeTraining(boolean v) {
		log.entering(CLASSNAME, "setEnableAllCluesBeforeTraining", v);
		log.exiting(CLASSNAME, "setEnableAllCluesBeforeTraining");

	}

	@Override
	public void setEnableAllRulesBeforeTraining(boolean v) {
		log.entering(CLASSNAME, "setEnableAllRulesBeforeTraining", v);
		log.exiting(CLASSNAME, "setEnableAllRulesBeforeTraining");

	}

	@Override
	public void setModelFilePath(String fileName) {
		log.entering(CLASSNAME, "setModelFilePath", fileName);
		log.exiting(CLASSNAME, "setModelFilePath");

	}

	@Override
	public void setFiringThreshold(int v) {
		log.entering(CLASSNAME, "setFiringThreshold", v);
		log.exiting(CLASSNAME, "setFiringThreshold");

	}

	@Override
	public void setLastTrainingDate(Date v) {
		log.entering(CLASSNAME, "setLastTrainingDate", v);
		log.exiting(CLASSNAME, "setLastTrainingDate");

	}

	@Override
	public void setMachineLearner(MachineLearner ml) {
		log.entering(CLASSNAME, "setMachineLearner", ml);
		log.exiting(CLASSNAME, "setMachineLearner");

	}

	@Override
	public void setClueFilePath(String fn) {
		log.entering(CLASSNAME, "setClueFilePath", fn);
		log.exiting(CLASSNAME, "setClueFilePath");

	}

	@Override
	public void setTrainedWithHolds(boolean b) {
		log.entering(CLASSNAME, "setTrainedWithHolds", b);
		log.exiting(CLASSNAME, "setTrainedWithHolds");

	}

	@Override
	public void setTrainingSource(String v) {
		log.entering(CLASSNAME, "setTrainingSource", v);
		log.exiting(CLASSNAME, "setTrainingSource");

	}

	@Override
	public void setUseAnt(boolean v) {
		log.entering(CLASSNAME, "setUseAnt", v);
		log.exiting(CLASSNAME, "setUseAnt");

	}

	@Override
	public void setUserName(String v) {
		log.entering(CLASSNAME, "setUserName", v);
		this.userName = v;
		log.exiting(CLASSNAME, "setUserName");

	}

}
