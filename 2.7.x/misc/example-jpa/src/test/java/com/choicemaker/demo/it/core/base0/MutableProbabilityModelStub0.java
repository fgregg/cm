package com.choicemaker.demo.it.core.base0;

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

public class MutableProbabilityModelStub0 implements IProbabilityModel {

	private static final String CLASSNAME = MutableProbabilityModelStub0.class
			.getName();

	private static final Logger log = Logger.getLogger(CLASSNAME);

	public final String modelName = MutableProbabilityModelStub0.class
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

	public MutableProbabilityModelStub0() {
		log.entering(CLASSNAME, "ctor");
		log.exiting(CLASSNAME, "ctor");
	}

	@Override
	public Accessor getAccessor() {
		log.entering(CLASSNAME, "getAccessor");
		log.exiting(CLASSNAME, "getAccessor", null);
		return null;
	}

	@Override
	public String getAccessorClassName() {
		log.entering(CLASSNAME, "getAccessorClassName");
		log.exiting(CLASSNAME, "getAccessorClassName", null);
		return null;
	}

	@Override
	public int activeSize() {
		log.entering(CLASSNAME, "activeSize");
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
		log.entering(CLASSNAME, "canEvaluate");
		log.exiting(CLASSNAME, "canEvaluate", false);
		return false;
	}

	@Override
	public void changedCluesToEvaluate() {
		log.entering(CLASSNAME, "changedCluesToEvaluate");
		log.exiting(CLASSNAME, "changedCluesToEvaluate");

	}

	@Override
	public String getAntCommand() {
		log.entering(CLASSNAME, "getAntCommand");
		log.exiting(CLASSNAME, "getAntCommand", null);
		return null;
	}

	@Override
	public String getBlockingConfigurationName() {
		log.entering(CLASSNAME, "getBlockingConfigurationName");
		log.exiting(CLASSNAME, "getBlockingConfigurationName", null);
		return null;
	}

	@Override
	public String getClueFilePath() {
		log.entering(CLASSNAME, "getClueFilePath");
		log.exiting(CLASSNAME, "getClueFilePath", null);
		return null;
	}

	@Override
	public ClueSet getClueSet() {
		log.entering(CLASSNAME, "getClueSet");
		log.exiting(CLASSNAME, "getClueSet", null);
		return null;
	}

	@Override
	public String getClueSetName() {
		log.entering(CLASSNAME, "getClueSetName");
		log.exiting(CLASSNAME, "getClueSetName", cluesetName);
		return cluesetName;
	}

	@Override
	public String getClueSetSignature() {
		log.entering(CLASSNAME, "getClueSetSignature");
		log.exiting(CLASSNAME, "getClueSetSignature", cluesetSignature);
		return cluesetSignature;
	}

	@Override
	public boolean[] getCluesToEvaluate() {
		log.entering(CLASSNAME, "getCluesToEvaluate");
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
	public String getDatabaseConfigurationName() {
		log.entering(CLASSNAME, "getDatabaseConfigurationName");
		log.exiting(CLASSNAME, "getDatabaseConfigurationName", null);
		return null;
	}

	@Override
	public int getDecisionDomainSize() {
		log.entering(CLASSNAME, "getDecisionDomainSize");
		log.exiting(CLASSNAME, "getDecisionDomainSize", 0);
		return 0;
	}

	@Override
	public Evaluator getEvaluator() {
		log.entering(CLASSNAME, "getEvaluator");
		log.exiting(CLASSNAME, "getEvaluator", null);
		return null;
	}

	@Override
	public String getEvaluatorSignature() {
		log.entering(CLASSNAME, "getEvaluatorSignature");
		log.exiting(CLASSNAME, "getEvaluatorSignature", evaluatorSignature);
		return evaluatorSignature;
	}

	@Override
	public int getFiringThreshold() {
		log.entering(CLASSNAME, "getFiringThreshold");
		log.exiting(CLASSNAME, "getFiringThreshold", 0);
		return 0;
	}

	@Override
	public Date getLastTrainingDate() {
		log.entering(CLASSNAME, "getLastTrainingDate");
		log.exiting(CLASSNAME, "getLastTrainingDate", null);
		return null;
	}

	@Override
	public MachineLearner getMachineLearner() {
		log.entering(CLASSNAME, "getMachineLearner");
		log.exiting(CLASSNAME, "getMachineLearner", null);
		return null;
	}

	@Override
	public String getModelFilePath() {
		log.entering(CLASSNAME, "getModelFilePath");
		log.exiting(CLASSNAME, "getModelFilePath", null);
		return null;
	}

	@Override
	public String getModelName() {
		log.entering(CLASSNAME, "getModelName");
		log.exiting(CLASSNAME, "getModelName", modelName);
		return modelName;
	}

	@Override
	public String getModelSignature() {
		log.entering(CLASSNAME, "getModelSignature");
		log.exiting(CLASSNAME, "getModelSignature", modelSignature);
		return modelSignature;
	}

	@Override
	public String getSchemaName() {
		log.entering(CLASSNAME, "getSchemaName");
		log.exiting(CLASSNAME, "getSchemaName", null);
		return null;
	}

	@Override
	public String getSchemaSignature() {
		log.entering(CLASSNAME, "getSchemaSignature");
		log.exiting(CLASSNAME, "getSchemaSignature", schemaSignature);
		return schemaSignature;
	}

	@Override
	public boolean[] getTrainCluesToEvaluate() {
		log.entering(CLASSNAME, "getTrainCluesToEvaluate");
		log.exiting(CLASSNAME, "getTrainCluesToEvaluate", null);
		return null;
	}

	@Override
	public String getTrainingSource() {
		log.entering(CLASSNAME, "getTrainingSource");
		log.exiting(CLASSNAME, "getTrainingSource", null);
		return null;
	}

	@Override
	public String getUserName() {
		log.entering(CLASSNAME, "getUserName");
		log.exiting(CLASSNAME, "getUserName", userName);
		return userName;
	}

	@Override
	public boolean isEnableAllCluesBeforeTraining() {
		log.entering(CLASSNAME, "isEnableAllCluesBeforeTraining");
		log.exiting(CLASSNAME, "isEnableAllCluesBeforeTraining", false);
		return false;
	}

	@Override
	public boolean isEnableAllRulesBeforeTraining() {
		log.entering(CLASSNAME, "isEnableAllRulesBeforeTraining");
		log.exiting(CLASSNAME, "isEnableAllRulesBeforeTraining", false);
		return false;
	}

	@Override
	public boolean isTrainedWithHolds() {
		log.entering(CLASSNAME, "isTrainedWithHolds");
		log.exiting(CLASSNAME, "isTrainedWithHolds", false);
		return false;
	}

	@Override
	public boolean isUseAnt() {
		log.entering(CLASSNAME, "isUseAnt");
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
		log.entering(CLASSNAME, "needsRecompilation");
		log.exiting(CLASSNAME, "needsRecompilation", false);
		return false;
	}

	@Override
	public int numTrainCluesToEvaluate() {
		log.entering(CLASSNAME, "numTrainCluesToEvaluate");
		log.exiting(CLASSNAME, "numTrainCluesToEvaluate", 0);
		return 0;
	}

	@Override
	public Map<String, String> properties() {
		log.entering(CLASSNAME, "properties");
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
		log.entering(CLASSNAME, "beginMultiPropertyChange");
		log.exiting(CLASSNAME, "beginMultiPropertyChange");

	}

	@Override
	public void endMultiPropertyChange() {
		log.entering(CLASSNAME, "endMultiPropertyChange");
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
