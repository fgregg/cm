package com.choicemaker.fake;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.Artifact;

import com.choicemaker.util.SystemPropertyUtils;

class MojoConfigurationUtils {

	private static final String PATH_SEPARATOR = System
			.getProperty(SystemPropertyUtils.PATH_SEPARATOR);

	private MojoConfigurationUtils() {
	}

	static String computeClasspath(List<Artifact> artifacts)
			throws IllegalStateException {
		assert artifacts != null;
		int count = artifacts.size();
		StringBuilder sb = new StringBuilder();
		for (Artifact a : artifacts) {
			File f = a.getFile();
			assert f != null;
			String fileName = f.getAbsolutePath();
			sb.append(fileName);
			--count;
			if (count > 1) {
				sb.append(PATH_SEPARATOR);
			}
		}
		final String retVal = sb.toString();
		assert retVal != null;
		return retVal;
	}

}
