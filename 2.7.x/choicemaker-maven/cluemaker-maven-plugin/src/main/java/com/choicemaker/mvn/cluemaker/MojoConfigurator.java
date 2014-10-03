package com.choicemaker.mvn.cluemaker;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.configure.ChoiceMakerConfiguration;
import com.choicemaker.cm.core.configure.ChoiceMakerConfigurator;

public class MojoConfigurator implements ChoiceMakerConfigurator {

	private final MavenProject project;
	private final File cluemakerDirectory;
	private final File generatedSourceDirectory;
	private final File compiledCodeDirectory;
	private final List<Artifact> artifacts;

	public MojoConfigurator(MavenProject p, File cluemakerDir, File generatedSrcDir, File compiledCodeDir,
			List<Artifact> a) {
		if (p == null) {
			throw new IllegalArgumentException("null maven project");
		}
		if (cluemakerDir == null) {
			throw new IllegalArgumentException("null source directory");
		}
		if (generatedSrcDir == null) {
			throw new IllegalArgumentException("null target directory");
		}
		this.project = p;
		this.cluemakerDirectory = cluemakerDir;
		this.generatedSourceDirectory = generatedSrcDir;
		this.compiledCodeDirectory = compiledCodeDir;
		this.artifacts = a;
	}

	public ChoiceMakerConfiguration init() throws XmlConfException {
		return new MojoConfiguration(project, cluemakerDirectory, generatedSourceDirectory, compiledCodeDirectory, artifacts);
	}

	/**
	 * All method parameters are ignored. Equivalent to invoking {@link #init()}
	 * without any parameters.
	 * @param fn may be null
	 */
	public ChoiceMakerConfiguration init(String fn, boolean reload,
			boolean initGui) throws XmlConfException {
		return init();
	}

	/**
	 * All method parameters are ignored. Equivalent to invoking {@link #init()}
	 * without any parameters.
	 * @param fn may be null
	 * @param logConfName may be null
	 */
	public ChoiceMakerConfiguration init(String fn, String logConfName,
			boolean reload, boolean initGui) throws XmlConfException {
		return init();
	}

}
