package com.choicemaker.cmit.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import com.choicemaker.cm.args.AbaSettings;
import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchJobController;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.batch.ProcessingController;
import com.choicemaker.cm.batch.impl.BatchJobEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.DefaultServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.DefaultSettings;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ImmutableRecordIdTranslatorLocal;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaBatchProcessingEvent;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbaSettingsEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbaSettingsJPA;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.DefaultSettingsEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.DefaultSettingsPK;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaSettingsEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaSettingsJPA;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJobController;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityParametersController;

/**
 * Lists of objects created during a test. Provides a convenient way of cleaning
 * up persistent objects after a test is finished. This class is not
 * thread-safe, so it should be used only within the scope of a method.
 * 
 * @author rphall
 */
public class TestEntityCounts {

	private static final Logger logger = Logger
			.getLogger(TestEntityCounts.class.getName());

	protected static final String INDENT = "  ";

	/**
	 * The name of a system property that can be set to "true" to enable
	 * retention of DB objects created during testing.
	 */
	public static final String PN_CMIT_RETAIN_TEST_OBJECTS =
		"CMIT_RetainTestObjects";

	// Don't use this directly; use isTestObjectRetentionRequested() instead
	private static Boolean _isTestObjectRetentionRequested = null;

	/**
	 * Checks the system property {@link #PN_SANITY_CHECK} and caches the result
	 */
	public static boolean isTestObjectRetentionRequested() {
		if (_isTestObjectRetentionRequested == null) {
			String pn = PN_CMIT_RETAIN_TEST_OBJECTS;
			String defaultValue = Boolean.FALSE.toString();
			String value = System.getProperty(pn, defaultValue);
			_isTestObjectRetentionRequested = Boolean.valueOf(value);
		}
		boolean retVal = _isTestObjectRetentionRequested.booleanValue();
		if (retVal) {
			logger.info("ProcessingStatus order-by debugging is enabled");
		}
		return retVal;
	}

	// The suffix 'IC' stands for 'InitialCount'

	private final int oabaJobIC;
	private final int oabaParamsIC;
	private final int transJobIC;
	private final int transParamsIC;
	private final int abaSettingsIC;
	private final int defaultAbaIC;
	private final int oabaSettingsIC;
	private final int defaultOabaIC;
	private final int serverConfIC;
	private final int defaultServerIC;
	private final int oabaEventIC;
	private final int opPropertyIC;
	private final int recordSourceIC;
	private final int recordIdIC;

	private Set<Object> objects = new LinkedHashSet<>();
	private Set<DefaultServerConfiguration> defaultServerConfigs =
		new LinkedHashSet<>();
	private Set<DefaultSettings> defaultSettings = new LinkedHashSet<>();
	private Set<ImmutableRecordIdTranslatorLocal<?>> recordIdTranslators =
		new LinkedHashSet<>();
	private Set<BatchJob> batchJobs = new LinkedHashSet<>();

	public TestEntityCounts(final Logger testLogger,
			final BatchJobController oabaJobController,
			final OabaParametersController oabaParamsController,
			final OabaSettingsController oabaSettingsController,
			final ServerConfigurationController serverController,
			final ProcessingController processingController,
			final OperationalPropertyController opPropController,
			final RecordSourceController rsController,
			final RecordIdController ridController) throws Exception {
		this(testLogger, oabaJobController, oabaParamsController,
				(TransitivityJobController) null,
				(TransitivityParametersController) null,
				oabaSettingsController, serverController, processingController,
				opPropController, rsController, ridController);
	}

	public TestEntityCounts(Logger testLogger,
			BatchJobController oabaJobController,
			OabaParametersController oabaParamsController,
			TransitivityJobController transJobController,
			TransitivityParametersController transParamsController,
			OabaSettingsController oabaSettingsController,
			ServerConfigurationController serverController,
			ProcessingController processingController,
			OperationalPropertyController opPropController,
			RecordSourceController rsController,
			RecordIdController ridController) {
		String msg;

		if (oabaJobController != null) {
			oabaJobIC = oabaJobController.findAll().size();
		} else {
			oabaJobIC = 0;
			msg = "Null oabaJobController";
			testLogger.fine(msg);
		}
		msg = "Initial oabaJob count: " + oabaJobIC;
		testLogger.fine(msg);

		if (oabaParamsController != null) {
			oabaParamsIC = oabaParamsController.findAllOabaParameters().size();
		} else {
			oabaParamsIC = 0;
			msg = "Null oabaParamsController";
			testLogger.fine(msg);
		}
		msg = "Initial oabaParameters count: " + oabaParamsIC;
		testLogger.fine(msg);

		if (transJobController != null) {
			transJobIC = transJobController.findAllTransitivityJobs().size();
		} else {
			transJobIC = 0;
			msg = "Null transJobController";
			testLogger.fine(msg);
		}
		msg = "Initial transJob count: " + transJobIC;
		testLogger.fine(msg);

		if (transParamsController != null) {
			transParamsIC =
				transParamsController.findAllTransitivityParameters().size();
		} else {
			transParamsIC = 0;
			msg = "Null transParamsController";
			testLogger.fine(msg);
		}
		msg = "Initial transParameters count: " + transParamsIC;
		testLogger.fine(msg);

		if (oabaSettingsController != null) {
			abaSettingsIC = oabaSettingsController.findAllAbaSettings().size();
			defaultAbaIC =
				oabaSettingsController.findAllDefaultAbaSettings().size();
			oabaSettingsIC =
				oabaSettingsController.findAllOabaSettings().size();
			defaultOabaIC =
				oabaSettingsController.findAllDefaultOabaSettings().size();
		} else {
			abaSettingsIC = 0;
			defaultAbaIC = 0;
			oabaSettingsIC = 0;
			defaultOabaIC = 0;
			msg = "Null oabaSettingsController";
			testLogger.fine(msg);
		}
		msg = "Initial abaSettings count: " + abaSettingsIC;
		testLogger.fine(msg);
		msg = "Initial defaultAbaSettings count: " + defaultAbaIC;
		testLogger.fine(msg);
		msg = "Initial oabaSettings count: " + oabaSettingsIC;
		testLogger.fine(msg);
		msg = "Initial defaultOabaSettings count: " + defaultOabaIC;
		testLogger.fine(msg);

		if (serverController != null) {
			serverConfIC =
				serverController.findAllServerConfigurations().size();
			defaultServerIC =
				serverController.findAllDefaultServerConfigurations().size();
		} else {
			serverConfIC = 0;
			defaultServerIC = 0;
			msg = "Null serverController";
			testLogger.fine(msg);
		}
		msg = "Initial serverConfiguration count: " + serverConfIC;
		testLogger.fine(msg);
		msg = "defaultServerConfigurationCount: " + defaultServerIC;
		testLogger.fine(msg);

		if (processingController != null) {
			oabaEventIC =
					processingController.findAllProcessingEvents().size();
		} else {
			oabaEventIC = 0;
			msg = "Null processingController";
			testLogger.fine(msg);
		}
		msg = "Initial oabaEvent count: " + oabaEventIC;
		testLogger.fine(msg);

		if (opPropController != null) {
			opPropertyIC =
				opPropController.findAllOperationalProperties().size();
		} else {
			opPropertyIC = 0;
			msg = "Null opPropController";
			testLogger.fine(msg);
		}
		msg = "Initial operationalProperty count: " + opPropertyIC;
		testLogger.fine(msg);

		if (rsController != null) {
			recordSourceIC = rsController.findAll().size();
		} else {
			recordSourceIC = 0;
			msg = "Null rsController";
			testLogger.fine(msg);
		}
		msg = "Initial recordSource count: " + recordSourceIC;
		testLogger.fine(msg);

		if (ridController != null) {
			recordIdIC = ridController.findAllRecordIdTranslations().size();
		} else {
			recordIdIC = 0;
			msg = "Null ridController";
			testLogger.fine(msg);
		}
		msg = "Initial recordId count: " + recordIdIC;
		testLogger.fine(msg);
	}

	protected void add(Object o) {
		if (o != null) {
			objects.add(o);
		}
	}

	public <T extends Comparable<T>> void add(
			ImmutableRecordIdTranslatorLocal<T> irit) {
		if (irit != null) {
			recordIdTranslators.add(irit);
		}
	}

	public void add(BatchJob job) {
		if (job != null) {
			batchJobs.add(job);
		}
	}

	public void add(OabaParameters params) {
		add((Object) params);
	}

	public void add(PersistableRecordSource prs) {
		add((Object) prs);
	}

	public void add(TransitivityParameters tp) {
		add((Object) tp);
	}

	public void add(OabaBatchProcessingEvent p) {
		add((Object) p);
	}

	public void add(ServerConfiguration sc) {
		add((Object) sc);
	}

	public void add(DefaultServerConfiguration dscb) {
		if (dscb != null) {
			defaultServerConfigs.add(dscb);
		}
	}

	public void add(AbaSettings aba) {
		add((Object) aba);
	}

	public void add(OabaSettings oaba) {
		add((Object) oaba);
	}

	public void add(DefaultSettings dse) {
		if (dse != null) {
			defaultSettings.add(dse);
		}
		add((Object) dse);
	}

	public boolean contains(DefaultServerConfiguration o) {
		boolean retVal = false;
		if (o != null) {
			retVal = defaultServerConfigs.contains(o);
		}
		return retVal;
	}

	public boolean contains(DefaultSettings o) {
		boolean retVal = false;
		if (o != null) {
			retVal = defaultSettings.contains(o);
		}
		return retVal;
	}

	public boolean contains(BatchJob o) {
		boolean retVal = false;
		if (o != null) {
			retVal = batchJobs.contains(o);
		}
		return retVal;
	}

	public boolean contains(Object o) {
		boolean retVal = false;
		if (o != null) {
			retVal = objects.contains(o);
		}
		return retVal;
	}

	public void checkCounts(Logger testLogger, final EntityManager em,
			final UserTransaction utx,
			final BatchJobController oabaJobController,
			final OabaParametersController oabaParamsController,
			final OabaSettingsController settingsController,
			final ServerConfigurationController serverController,
			final ProcessingController processingController,
			final OperationalPropertyController opPropController,
			final RecordSourceController rsController,
			final RecordIdController ridController) throws AssertionError {
		checkCounts(testLogger, em, utx, oabaJobController,
				oabaParamsController, (TransitivityJobController) null,
				(TransitivityParametersController) null, settingsController,
				serverController, processingController, opPropController,
				rsController, ridController);
	}

	public void checkCounts(Logger testLogger, EntityManager em,
			UserTransaction utx, BatchJobController oabaJobController,
			OabaParametersController oabaParamsController,
			TransitivityJobController transJobController,
			TransitivityParametersController transParamsController,
			OabaSettingsController settingsController,
			ServerConfigurationController serverController,
			ProcessingController processingController,
			OperationalPropertyController opPropController,
			RecordSourceController rsController,
			RecordIdController ridController) throws AssertionError {

		final boolean doRetention = isTestObjectRetentionRequested();
		if (!doRetention) {
			try {
				testLogger.info("Removing test entities");
				removeTestEntities(em, utx, serverController,
						settingsController, processingController,
						opPropController, ridController);
			} catch (Exception x) {
				throw new AssertionError("Unable to remove test entities: "
						+ x.toString());
			}
		}

		final boolean doAssert = !doRetention;
		logMaybeAssert(doAssert, testLogger, oabaJobController,
				oabaParamsController, transJobController,
				transParamsController, settingsController, serverController,
				processingController, opPropController, rsController,
				ridController);
	}

	protected void removeTestEntities(EntityManager em, UserTransaction utx,
			ServerConfigurationController scc, OabaSettingsController osc,
			final ProcessingController processingController,
			final OperationalPropertyController propertyController,
			RecordIdController ridController) throws Exception {
		if (em == null || scc == null) {
			throw new IllegalArgumentException("null argument");
		}
		final boolean usingUtx;
		if (utx != null) {
			usingUtx = true;
		} else {
			usingUtx = false;
		}

		if (isTestObjectRetentionRequested()) {
			String msg = "Retaining objects created during testing";
			logger.warning(msg);
		} else {
			removeOtherObjects(objects, em, utx, usingUtx);
			removeDefaultServerConfigurations(defaultServerConfigs, em, utx,
					scc, usingUtx);
			removeDefaultSettings(defaultSettings, em, utx, usingUtx);
			removeJobEventsProperties(batchJobs, processingController,
					propertyController);
			removeTranslations(recordIdTranslators, em, utx, ridController,
					usingUtx);
			removeTranslations(batchJobs, ridController);
			removeJobs(batchJobs, em, utx, usingUtx);
		}
	}

	/**
	 * An internal implementation method that logs and conditionally asserts the
	 * current counts of test entities.
	 */
	protected void logMaybeAssert(boolean doAssert, Logger testLogger,
			final BatchJobController oabaJobController,
			final OabaParametersController oabaParamsController,
			final TransitivityJobController transJobController,
			final TransitivityParametersController transParamsController,
			final OabaSettingsController oabaSettingsController,
			final ServerConfigurationController serverController,
			final ProcessingController processingController,
			final OperationalPropertyController opPropController,
			final RecordSourceController rsController,
			final RecordIdController ridController) {
		testLogger.info("Checking final object counts");

		// Note the suffix CC stands for 'CurrentCount'

		String msg;
		List<String> warnings = new LinkedList<>();

		final int oabaJobCC;
		if (oabaJobController != null) {
			oabaJobCC = oabaJobController.findAll().size();
		} else {
			oabaJobCC = 0;
			msg = "Null oabaJobController";
			testLogger.fine(msg);
		}
		String w = warnOrLog(testLogger, "oabaJob", oabaJobIC, oabaJobCC);
		if (w != null) {
			warnings.add(w);
		}

		final int oabaParamsCC;
		if (oabaParamsController != null) {
			oabaParamsCC =
					oabaParamsController.findAllOabaParameters().size();
		} else {
			oabaParamsCC = 0;
			msg = "Null oabaParamsController";
			testLogger.fine(msg);
		}
		w = warnOrLog(testLogger, "oabaParameters", oabaParamsIC, oabaParamsCC);
		if (w != null) {
			warnings.add(w);
		}

		final int transJobCC;
		if (transJobController != null) {
			transJobCC =
					transJobController.findAllTransitivityJobs().size();
		} else {
			transJobCC = 0;
			msg = "Null transJobController";
			testLogger.fine(msg);
		}
		w = warnOrLog(testLogger, "transitivityJob", transJobIC, transJobCC);
		if (w != null) {
			warnings.add(w);
		}

		final int transParamsCC;
		if (transParamsController != null) {
			transParamsCC =
					transParamsController.findAllTransitivityParameters().size();
		} else {
			transParamsCC = 0;
			msg = "Null transParamsController";
			testLogger.fine(msg);
		}
		w = warnOrLog(testLogger, "transParameters", transParamsIC, transParamsCC);
		if (w != null) {
			warnings.add(w);
		}

		final int abaSettingsCC;
		final int defaultAbaCC;
		final int oabaSettingsCC;
		final int defaultOabaCC;
		if (oabaSettingsController != null) {
			abaSettingsCC =
					oabaSettingsController.findAllAbaSettings().size();
			defaultAbaCC =
					oabaSettingsController.findAllDefaultAbaSettings().size();
			oabaSettingsCC =
					oabaSettingsController.findAllOabaSettings().size();
			defaultOabaCC =
					oabaSettingsController.findAllDefaultOabaSettings().size();
		} else {
			abaSettingsCC = 0;
			defaultAbaCC = 0;
			oabaSettingsCC = 0;
			defaultOabaCC = 0;
			msg = "Null oabaSettingsController";
			testLogger.fine(msg);
		}
		w = warnOrLog(testLogger, "abaSettings", abaSettingsIC, abaSettingsCC);
		if (w != null) {
			warnings.add(w);
		}
		w = warnOrLog(testLogger, "defaultAbaSettings", defaultAbaIC, defaultAbaCC);
		if (w != null) {
			warnings.add(w);
		}
		w = warnOrLog(testLogger, "oabaSettings", oabaSettingsIC, oabaSettingsCC);
		if (w != null) {
			warnings.add(w);
		}
		w = warnOrLog(testLogger, "defaultOabaSettings", defaultOabaIC, defaultOabaCC);
		if (w != null) {
			warnings.add(w);
		}

		final int serverConfCC;
		final int defServerCC;
		if (serverController != null) {
			serverConfCC =
					serverController.findAllServerConfigurations().size();
			defServerCC =
					serverController.findAllDefaultServerConfigurations().size();
		} else {
			serverConfCC = 0;
			defServerCC = 0;
			msg = "Null serverController";
			testLogger.fine(msg);
		}
		w = warnOrLog(testLogger, "serverConfiguration", serverConfIC, serverConfCC);
		if (w != null) {
			warnings.add(w);
		}
		w = warnOrLog(testLogger, "defaultServerConf", defaultServerIC, defServerCC);
		if (w != null) {
			warnings.add(w);
		}

		final int oabaEventCC;
		if (processingController != null) {
			oabaEventCC =
					processingController.findAllProcessingEvents().size();
		} else {
			oabaEventCC = 0;
			msg = "Null processingController";
			testLogger.fine(msg);
		}
		w = warnOrLog(testLogger, "oabaEvent", oabaEventIC, oabaEventCC);
		if (w != null) {
			warnings.add(w);
		}

		final int opPropertyCC;
		if (opPropController != null) {
			opPropertyCC =
					opPropController.findAllOperationalProperties().size();
		} else {
			opPropertyCC = 0;
			msg = "Null opPropController";
			testLogger.fine(msg);
		}
		w = warnOrLog(testLogger, "operationalProperty", opPropertyIC, opPropertyCC);
		if (w != null) {
			warnings.add(w);
		}

		final int recordSourceCC;
		if (rsController != null) {
			recordSourceCC = rsController.findAll().size();
		} else {
			recordSourceCC = 0;
			msg = "Null rsController";
			testLogger.fine(msg);
		}
		w = warnOrLog(testLogger, "recordSource", recordSourceIC, recordSourceCC);
		if (w != null) {
			warnings.add(w);
		}

		final int recordIdCC;
		if (ridController != null) {
			recordIdCC =
					ridController.findAllRecordIdTranslations().size();
		} else {
			recordIdCC = 0;
			msg = "Null ridController";
			testLogger.fine(msg);
		}
		w = warnOrLog(testLogger, "recordId", recordIdIC, recordIdCC);
		if (w != null) {
			warnings.add(w);
		}

		if (!warnings.isEmpty()) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			pw.println("Some test entities were not deleted");
			for (String w2 : warnings) {
				pw.println(INDENT + w2);
			}
			String warning = sw.toString();
			if (doAssert) {
				throw new AssertionError(warning);
			} else {
				testLogger.warning(warning);
			}
		}
	}

	protected static Long getId(Object o) {
		Long retVal = null;
		try {
			Class<?> c = o.getClass();
			Method getId = c.getMethod("getId", (Class[]) null);
			retVal = (long) getId.invoke(o, (Object[]) null);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			logger.fine("unable to find method 'getId': " + e.toString());
		}
		return retVal;
	}

	protected static String warnOrLog(Logger testLogger, String countName,
			int initialCount, int currentCount) {
		String retVal = null;
		if (initialCount != currentCount) {
			retVal =
				countName + ": initial count (" + initialCount
						+ ") != current count (" + currentCount + ")";
		} else {
			String msg = countName + " count: " + currentCount;
			testLogger.fine(msg);
		}
		return retVal;
	}

	protected static void removeDefaultServerConfigurations(
			Set<DefaultServerConfiguration> defaultServerConfigs,
			EntityManager em, UserTransaction utx,
			ServerConfigurationController scc, boolean usingUtx)
			throws Exception {
		String msg =
			"Removing default server configurations created during testing";
		logger.fine(msg);
		int count = 0;
		for (DefaultServerConfiguration dscb : defaultServerConfigs) {
			final DefaultServerConfiguration refresh =
				scc.findDefaultServerConfiguration(dscb.getHostName());
			if (refresh != null) {
				final long id = refresh.getServerConfigurationId();
				final ServerConfiguration sc = scc.findServerConfiguration(id);
				if (usingUtx) {
					utx.begin();
				}
				logger.fine("Deleting default configuration (" + count + "): "
						+ refresh);
				DefaultServerConfiguration dsc = em.merge(refresh);
				em.remove(dsc);
				if (sc != null) {
					logger.fine("Deleting configuration (" + count + "): " + sc);
					ServerConfiguration sc2 = em.merge(sc);
					em.remove(sc2);
				}
				++count;
				if (usingUtx) {
					utx.commit();
				}
			}
		}
		logger.info("Deleted default server configurations: " + count);
	}

	protected static void removeDefaultSettings(
			Set<DefaultSettings> defaultSettings, EntityManager em,
			UserTransaction utx, boolean usingUtx) throws Exception {
		String msg = "Removing default settings created during testing";
		logger.fine(msg);
		int count = 0;
		for (DefaultSettings ds : defaultSettings) {
			DefaultSettingsPK pk = ds.getPrimaryKey();
			final DefaultSettings refresh =
				em.find(DefaultSettingsEntity.class, pk);
			if (refresh != null) {
				final String type = pk.getType();
				final Object settings;
				final Class<?> c;
				if (AbaSettingsJPA.DISCRIMINATOR_VALUE.equals(type)) {
					c = AbaSettingsEntity.class;
					settings = em.find(c, refresh.getSettingsId());
				} else if (OabaSettingsJPA.DISCRIMINATOR_VALUE.equals(type)) {
					c = OabaSettingsEntity.class;
					settings = em.find(c, refresh.getSettingsId());
				} else {
					c = null;
					settings = null;
					throw new AssertionError("unknown type: " + type);
				}
				if (usingUtx) {
					utx.begin();
				}
				logger.fine("Deleting default settings (" + count + "): "
						+ refresh);
				DefaultSettings r0 = em.merge(refresh);
				em.remove(r0);
				if (settings != null) {
					logger.fine("Deleting settings(" + count + "): " + settings);
					Object o = em.merge(settings);
					em.remove(o);
				}
				++count;
				if (usingUtx) {
					utx.commit();
				}
			}
		}
		logger.info("Deleted default settings: " + count);
	}

	protected static void removeJobEventsProperties(Set<BatchJob> batchJobs,
			ProcessingController processingController,
			OperationalPropertyController propertyController) throws Exception {
		String msg = "Removing events and properties created during testing";
		logger.fine(msg);
		int eCount = 0;
		int pCount = 0;
		int jCount = 0;
		for (BatchJob job : batchJobs) {
			++jCount;
			long jobId = job.getId();
			if (job.isPersistent()) {
				eCount +=
					processingController
							.deleteProcessingEventsByJobId(jobId);
				pCount +=
					propertyController
							.deleteOperationalPropertiesByJobId(jobId);
			}
		}
		logger.info("Deleted events for " + jCount + " jobs: " + eCount);
		logger.info("Deleted properties for " + jCount + " jobs: " + pCount);
	}

	protected static void removeOtherObjects(Set<Object> objects,
			EntityManager em, UserTransaction utx, boolean usingUtx)
			throws Exception {
		String msg;
		int count;

		msg = "Removing objects created during testing";
		logger.fine(msg);
		count = 0;
		for (Object o : objects) {
			final Long oId = getId(o);
			if (oId != null) {
				final Class<?> c = o.getClass();
				if (oId != 0) {
					if (usingUtx) {
						utx.begin();
					}
					Object refresh = em.find(c, oId);
					if (refresh != null) {
						em.merge(refresh);
						boolean isManaged = em.contains(refresh);
						if (!isManaged) {
							String typeName = o.getClass().getSimpleName();
							logger.warning(typeName + " " + oId
									+ " is not managed");
						} else {
							em.remove(refresh);
							++count;
						}
					}
					if (usingUtx) {
						utx.commit();
					}
				}
			} else {
				try {
					// utx.begin();
					em.merge(o);
					em.remove(o);
					++count;
					// utx.commit();
				} catch (Exception x) {
					logger.warning(x.toString());
				}
			}
		}
		logger.info("Deleted objects: " + count);
	}

	protected static void removeTranslations(
			Set<ImmutableRecordIdTranslatorLocal<?>> recordIdTranslators,
			EntityManager em, UserTransaction utx, RecordIdController ridc,
			boolean usingUtx) throws Exception {
		String msg = "Removing record-id translations created during testing";
		logger.fine(msg);
		for (ImmutableRecordIdTranslatorLocal<?> translator : recordIdTranslators) {
			BatchJob job = translator.getBatchJob();
			long jobId = job.getId();
			msg = "Removing translations for job " + jobId;
			logger.fine(msg);
			if (job.isPersistent()) {
				int deletionCount = ridc.deleteTranslationsByJob(job);
				logger.info("Deleted record-id translations for job " + jobId
						+ ": " + deletionCount);
			}
		}
	}

	protected static void removeTranslations(Set<BatchJob> batchJobs,
			RecordIdController ridc) throws Exception {
		String msg = "Removing record-id translations created by test jobs";
		logger.fine(msg);
		int tCount = 0;
		int jCount = 0;
		for (BatchJob job : batchJobs) {
			++jCount;
			if (job.isPersistent()) {
				tCount += ridc.deleteTranslationsByJob(job);
			}
		}
		logger.info("Deleted translations for " + jCount + " jobs: " + tCount);
	}

	protected static void removeJobs(Set<BatchJob> batchJobs, EntityManager em,
			UserTransaction utx, boolean usingUtx) throws Exception {
		String msg = "Removing batch jobs created during testing";
		logger.fine(msg);
		int count = 0;
		for (BatchJob job : batchJobs) {
			if (usingUtx) {
				utx.begin();
			}
			final Class<?> c = job.getClass();
			final long jobId = job.getId();
			if (jobId != BatchJobEntity.NONPERSISTENT_ID) {
				BatchJob refresh = (BatchJob) em.find(c, jobId);
				if (refresh != null) {
					em.merge(refresh);
					boolean isManaged = em.contains(refresh);
					if (!isManaged) {
						String typeName = job.getClass().getSimpleName();
						logger.warning(typeName + " " + jobId
								+ " is not managed");
					} else {
						em.remove(refresh);
						++count;
					}
				}
			}
			if (usingUtx) {
				utx.commit();
			}
		}
		logger.info("Deleted batch jobs: " + count);
	}

}
