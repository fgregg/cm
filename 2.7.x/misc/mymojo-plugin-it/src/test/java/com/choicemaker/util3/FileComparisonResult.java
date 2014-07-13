package com.choicemaker.util3;

import java.nio.file.Path;



final class FileComparisonResult {
	
	private final Path p1;
	private final Path p2;
	private final FILE_CONTENT_COMPARISON0 result;

	FileComparisonResult(Path p1, Path p2, FILE_CONTENT_COMPARISON0 result) {
		if (result == null) {
			throw new IllegalArgumentException("null result");
		}
		this.p1 = p1;
		this.p2 = p2;
		this.result = result;
	}
	
	public Path getPath1() {
		return p1;
	}

	public Path getPath2() {
		return p2;
	}

	public FILE_CONTENT_COMPARISON0 getResult() {
		return result;
	}

}