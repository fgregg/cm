package com.choicemaker.cmit.io.blocking.automated.offline.server;

import static com.choicemaker.cmit.io.blocking.automated.offline.server.BatchDeploymentUtils.DEPENDENCIES_POM;
import static com.choicemaker.cmit.io.blocking.automated.offline.server.BatchDeploymentUtils.EJB_MAVEN_COORDINATES;
import static com.choicemaker.cmit.utils.DeploymentUtils.PERSISTENCE_CONFIGURATION;
import static com.choicemaker.cmit.utils.DeploymentUtils.PROJECT_POM;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.io.blocking.automated.offline.server.CM_ModelBean;
import com.choicemaker.cmit.core.base.MutableProbabilityModelStub0;
import com.choicemaker.cmit.utils.DeploymentUtils;

@RunWith(Arquillian.class)
public class CM_ModelBean_0_Test {

	private static final Logger logger = Logger
			.getLogger(CM_ModelBean_0_Test.class.getName());

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		List<Class<?>> testClasses = new ArrayList<>();
		testClasses.add(MutableProbabilityModelStub0.class);
		testClasses.add(CM_ModelBean_0_Test.class);
		testClasses.add(CM_ModelController0.class);

		JavaArchive ejb =
			DeploymentUtils.createEjbJar(PROJECT_POM, EJB_MAVEN_COORDINATES,
					testClasses, PERSISTENCE_CONFIGURATION);

		File[] deps = DeploymentUtils.createTestDependencies(DEPENDENCIES_POM);

		EnterpriseArchive retVal = DeploymentUtils.createEarArchive(ejb, deps);
		return retVal;
	}

	public static final int MAX_TEST_ITERATIONS = 10;

	public static final int BRIEF_DELAY_MILLIS = 2;

	/**
	 * The method compares two String lists for equality. It works with
	 * unmodifiable lists, which the List.equals(List) method doesn't seem to.
	 * 
	 * @param c1
	 *            first list
	 * @param c2
	 *            second list
	 * @return true if the lists are equal
	 */
	public static <T> boolean equal(Collection<T> c1, Collection<T> c2) {
		boolean retVal = c1 != null && c2 != null && c1.size() == c2.size() &&
				c1.containsAll(c2);
		return retVal;
	}

	public static boolean equalNoteContent(CM_ModelBean model,
			Collection<String> expectedNotes) {
		Collection<String> c = model.getNotes().values();
		return equal(c,expectedNotes);
	}

	public static void assertSameValuesExcludingNotes(CM_ModelBean model,
			ImmutableProbabilityModel ipm) {
		assertTrue(model.getCluesetName().equals(ipm.getClueSetName()));
		assertTrue(model.getCluesetSignature()
				.equals(ipm.getClueSetSignature()));
		assertTrue(model.getEvaluatorSignature().equals(
				ipm.getEvaluatorSignature()));
		assertTrue(model.getModelName().equals(ipm.getModelName()));
		assertTrue(model.getModelSignature().equals(ipm.getModelSignature()));
	}

	public static List<String> createExpectedNotes(ImmutableProbabilityModel ipm) {
		final String expectedNote = CM_ModelBean.createDefaultNote(ipm);
		final List<String> retVal = new ArrayList<>();
		retVal.add(expectedNote);
		return retVal;
	}

	@EJB
	protected CM_ModelController0 controller;

	@Test
	public void testModelController() {
		assertTrue(controller != null);
	}

	@Test
	public void testConstruction() {
		final ImmutableProbabilityModel ipm =
			new MutableProbabilityModelStub0();
		final List<String> expectedNotes = createExpectedNotes(ipm);

		CM_ModelBean model = new CM_ModelBean(ipm);
		assertTrue(0 == model.getId());
		assertSameValuesExcludingNotes(model, ipm);
		assertTrue(equalNoteContent(model,expectedNotes));
	}

	@Test
	public void testPersistFindRemove() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		// Create a model
		final ImmutableProbabilityModel ipm =
			new MutableProbabilityModelStub0();
		final List<String> expectedNotes = createExpectedNotes(ipm);

		CM_ModelBean model = new CM_ModelBean(ipm);
		assertTrue(model.getId() == 0);
		assertSameValuesExcludingNotes(model, ipm);
		assertTrue(equalNoteContent(model,expectedNotes));

		// Save the model
		controller.save(model);
		assertTrue(model.getId() != 0);
		assertSameValuesExcludingNotes(model, ipm);
		assertTrue(equalNoteContent(model,expectedNotes));

		// Find the model
		CM_ModelBean model2 = controller.find(model.getId());
		assertTrue(model.getId() == model2.getId());
		assertSameValuesExcludingNotes(model2, ipm);
		assertTrue(equalNoteContent(model,expectedNotes));

		// Delete the model
		controller.delete(model2);
		CM_ModelBean model3 = controller.find(model.getId());
		assertTrue(model3 == null);

		// Check that the number of existing jobs equals the initial count
		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testMerge() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		final ImmutableProbabilityModel ipm =
			new MutableProbabilityModelStub0();
		final List<String> expectedNotes = createExpectedNotes(ipm);

		CM_ModelBean model = new CM_ModelBean(ipm);
		assertTrue(model.getId() == 0);
		assertSameValuesExcludingNotes(model, ipm);
		assertTrue(equalNoteContent(model,expectedNotes));

		controller.save(model);
		assertTrue(model.getId() != 0);
		final long id = model.getId();
		controller.detach(model);

		assertTrue(equalNoteContent(model,expectedNotes));
		final String expectedNote2 = "new note";
		model.addNote(expectedNote2);
		assertTrue(model.getNotes().size() == 2);
		assertTrue(model.getNotes().values().containsAll(expectedNotes));
		assertTrue(model.getNotes().values().contains(expectedNote2));
		controller.save(model);

		model = null;
		CM_ModelBean model2 = controller.find(id);
		assertTrue(id == model2.getId());
		assertTrue(model2.getNotes().size() == 2);
		assertTrue(model2.getNotes().values().containsAll(expectedNotes));
		assertTrue(model2.getNotes().values().contains(expectedNote2));
		controller.delete(model2);

		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testFindAll() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		List<Long> jobIds = new ArrayList<>();
		List<ImmutableProbabilityModel> models = new ArrayList<>();
		for (int i = 0; i < MAX_TEST_ITERATIONS; i++) {
			// Create and save a model
			try {
				Thread.sleep(BRIEF_DELAY_MILLIS);
			} catch (InterruptedException e) {
				logger.fine(e.toString());
			}
			ImmutableProbabilityModel ipm = new MutableProbabilityModelStub0();
			CM_ModelBean model = new CM_ModelBean(ipm);
			assertTrue(model.getId() == 0);
			controller.save(model);
			final long id = model.getId();
			assertTrue(id != 0);
			jobIds.add(id);
			models.add(ipm);
		}
		assertTrue(jobIds.size() == models.size());

		// Verify the number of jobs has increased
		List<CM_ModelBean> jobs = controller.findAll();
		assertTrue(jobs != null);
		assertTrue(initialCount + MAX_TEST_ITERATIONS == jobs.size());

		// Find the jobs
		boolean isFound = false;
		Iterator<ImmutableProbabilityModel> itModel = models.iterator();
		for (long jobId : jobIds) {
			ImmutableProbabilityModel ipm = itModel.next();
			for (CM_ModelBean model : jobs) {
				if (jobId == model.getId()) {
					assertSameValuesExcludingNotes(model, ipm);
					isFound = true;
					break;
				}
			}
			assertTrue(isFound);
		}

		// Remove the model
		for (long id : jobIds) {
			CM_ModelBean model = controller.find(id);
			controller.delete(model);
		}

		jobs = controller.findAll();
		assertTrue(jobs != null);
		assertTrue(initialCount == jobs.size());
	}

	@Test
	public void testEqualsHashCode() {
		// Create two generic jobs and verify equality
		final ImmutableProbabilityModel ipm =
			new MutableProbabilityModelStub0();
		CM_ModelBean m1 = new CM_ModelBean(ipm);
		CM_ModelBean m2 = new CM_ModelBean(ipm);
		assertTrue(m1.equals(m2));
		assertTrue(m1.hashCode() == m2.hashCode());

		// Different notes do not affect equality or hash code of models that
		// have not been persisted.
		assertTrue(equal(m1.getNotes().values(), m1.getNotes().values()));
		m1.addNote(new Date().toString());
		assertTrue(!equal(m1.getNotes().values(), m2.getNotes().values()));
		assertTrue(m1.equals(m2));
		assertTrue(m1.hashCode() == m2.hashCode());

		// Verify a non-persistent model is not equal to a persistent model
		m1 = controller.save(m1);
		assertTrue(!m1.equals(m2));
		assertTrue(m1.hashCode() != m2.hashCode());

		// Different notes do not affect equality or hash code models that have
		// been persisted; i.e. different notes never affect equality or hash
		// code
		controller.detach(m1);
		m2 = controller.find(m1.getId());
		controller.detach(m2);
		assertTrue(m1.getNotes().equals(m2.getNotes()));
		m1.addNote("nonsense");
		assertTrue(!equal(m1.getNotes().values(), m2.getNotes().values()));
		assertTrue(m1.equals(m2));
		assertTrue(m1.hashCode() == m2.hashCode());

		// Different models are not equal and have different hashCodes
		try {
			Thread.sleep(BRIEF_DELAY_MILLIS);
		} catch (InterruptedException e) {
			logger.fine(e.toString());
		}
		final ImmutableProbabilityModel ipm2 =
			new MutableProbabilityModelStub0();
		assertTrue(!ipm2.getModelName().equals(ipm.getModelName()));
		m2 = new CM_ModelBean(ipm);
		assertTrue(!m1.equals(m2));
		assertTrue(m1.hashCode() != m2.hashCode());
	}

}
