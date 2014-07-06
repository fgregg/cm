package org.apache.maven.plugin.my;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.MachineLearner;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.compiler.ICompiler;
import com.choicemaker.cm.core.configure.ChoiceMakerConfiguration;
import com.choicemaker.cm.core.configure.MachineLearnerPersistence;
import com.choicemaker.cm.core.configure.ProbabilityModelPersistence;

public class MojoConfiguration implements ChoiceMakerConfiguration {

	// private final MavenProject project;
	private final File sourceDirectory;
	private final File targetDirectory;
	private final List<Artifact> artifacts;
	
	private String classpath;

	public MojoConfiguration(MavenProject p, File srcDir, File outDir, List<Artifact> a) {
//		if (p == null) {
//			throw new IllegalArgumentException("null maven project");
//		}
		if (srcDir == null) {
			throw new IllegalArgumentException("null source directory");
		}
		if (outDir == null) {
			throw new IllegalArgumentException("null target directory");
		}
		if (a == null) {
			throw new IllegalArgumentException("null artifact list");
		}
//		this.project = p;
		this.sourceDirectory = srcDir;
		this.targetDirectory = outDir;
		this.artifacts = a;
	}

	public void deleteGeneratedCode() {
		throw new Error("not yet implemented");
	}

	public ICompiler getChoiceMakerCompiler() {
		throw new Error("not yet implemented");
	}

	public ClassLoader getClassLoader() {
		throw new Error("not yet implemented");
	}

	public String getClassPath() {
		if (classpath == null) {
			classpath = MojoConfigurationUtils.computeClasspath(artifacts);
		}
		assert classpath != null;
		return classpath;
	}

	public String getFileName() {
		throw new Error("not yet implemented");
	}

	public String getJavaDocClasspath() {
		throw new Error("not yet implemented");
	}

	public MachineLearnerPersistence getMachineLearnerPersistence(
			MachineLearner model) {
		throw new Error("not yet implemented");
	}

	public ProbabilityModelPersistence getModelPersistence(
			ImmutableProbabilityModel model) {
		throw new Error("not yet implemented");
	}

	@SuppressWarnings("rawtypes")
	public List getProbabilityModelConfigurations() {
		throw new Error("not yet implemented");
	}

	public String getReloadClassPath() {
		throw new Error("not yet implemented");
	}

	public ClassLoader getRmiClassLoader() {
		throw new Error("not yet implemented");
	}

	public File getWorkingDirectory() {
		throw new Error("not yet implemented");
	}

	public void reloadClasses() throws XmlConfException {
		throw new Error("not yet implemented");
	}

	public String toXml() {
		throw new Error("not yet implemented");
	}

	public String getSourceCodeRoot() {
		return this.sourceDirectory.getAbsolutePath();
	}

	public String getCompiledCodeRoot() {
		return this.targetDirectory.getAbsolutePath();
	}

	public String getPackagedCodeRoot() {
		throw new Error("not yet implemented");
	}

}
