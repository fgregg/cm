package org.apache.maven.plugin.my;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.configure.ChoiceMakerConfiguration;
import com.choicemaker.cm.core.configure.ChoiceMakerConfigurator;

public class MojoConfigurator implements ChoiceMakerConfigurator {

	private final MavenProject project;
	private final File sourceDirectory;
	private final File targetDirectory;
	private final List<Artifact> artifacts;

	public MojoConfigurator(MavenProject p, File srcDir, File outDir,
			List<Artifact> a) {
		if (p == null) {
			throw new IllegalArgumentException("null maven project");
		}
		if (srcDir == null) {
			throw new IllegalArgumentException("null source directory");
		}
		if (outDir == null) {
			throw new IllegalArgumentException("null target directory");
		}
		this.project = p;
		this.sourceDirectory = srcDir;
		this.targetDirectory = outDir;
		this.artifacts = a;
	}

	public ChoiceMakerConfiguration init() throws XmlConfException {
		return new MojoConfiguration(project, sourceDirectory, targetDirectory, artifacts);
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
	 * @param log4jConfName may be null
	 */
	public ChoiceMakerConfiguration init(String fn, String log4jConfName,
			boolean reload, boolean initGui) throws XmlConfException {
		return init();
	}

}
