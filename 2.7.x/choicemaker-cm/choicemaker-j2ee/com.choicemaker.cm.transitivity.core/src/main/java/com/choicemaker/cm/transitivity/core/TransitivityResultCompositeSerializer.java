package com.choicemaker.cm.transitivity.core;

import java.io.IOException;

public interface TransitivityResultCompositeSerializer extends
		TransitivityResultSerializer {

	/**
	 * Serializes a transitivity result to a file or files
	 * 
	 * @param result
	 *            a non-null transitivity result
	 * @param fileBase
	 *            The name stem of an output file, excluding any index,
	 *            extension or qualifying path
	 * @param maxFileSize
	 *            the approximate maximum number of records in an output file
	 * @throws IOException
	 */
	void serialize(TransitivityResult result, String fileBase, int maxFileSize)
			throws IOException;

}