package com.choicemaker.cm.compiler.app;

import java.io.File;
// import java.net.URISyntaxException;
import java.net.URL;

import com.choicemaker.cm.core.util.MrpsExport;

/**
 * Configuration constants used by compiler tests based on the MCI project
 * @author rphall
 */
public class CompilerConfig {

	public final static boolean RELOAD = false;

	public final static boolean INIT_GUI = false;

	public final static float DIFF_THRESHOLD = 0.20f;

	public final static float MATCH_THRESHOLD = 0.80f;

	public final static boolean EXPORT_IDS = true;

	public final static boolean EXPORT_PROBABILITIES = true;

	public final static boolean EXPORT_DECISIONS = true;

	public final static boolean EXPORT_MARKED = false;

	public final static boolean EXPORT_DETAILS = false;

	public final static int EXPORT_ACTIVE_CLUES = MrpsExport.AC_CLUE_INDICES;

	public final static String EXPORT_DELIMITER = "|";

	public static String[] getCompilerArguments(File clueFile) {
		// Preconditions
//		if (clueFile == null || !clueFile.exists()) {
//			throw new IllegalArgumentException("invalid clue file");
//		}
		String[] retVal = new String[] { clueFile.getAbsolutePath(), "-debug" };
		return retVal;
	} // getCompilerArguments(File)

	public static File resourceToFile(String name) throws RuntimeException {

		// Preconditions
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException(
				"invalid file name + '" + name + "'");
		}

		URL url = null;
		File retVal = null;
		url = CompilerConfig.class.getClassLoader().getResource(name);
		if (url == null) {
			throw new RuntimeException("not found + '" + name + "'");
		}
		retVal = new File(url.getFile());
		return retVal;
	} // nameToFile(String)

} // CompilerConfig
