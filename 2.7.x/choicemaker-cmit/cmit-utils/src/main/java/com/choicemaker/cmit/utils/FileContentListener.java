package com.choicemaker.cmit.utils;

import java.nio.file.Path;

public interface FileContentListener {

//	enum FILE_CONTENT_COMPARISON {
//		ONLY_IN_PATH1, ONLY_IN_PATH12, DIFFERENT_CONTENT, SAME_CONTENT,
//		UNREACHABLE_PATH1, UNREACHABLE_PATH2
//	}

	void fileComparison(Path p1, Path p2, FileContentComparison0 result);

}
