package com.choicemaker.util3;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.choicemaker.cm.core.util.MessageUtil;


public class DefaultFileContentListener implements FileContentListener {

	public static final int DEFAULT_MAX_DIFFERENCES = 3;
	
	private static final String RESOURCE_BUNDLE = "com.choicemaker.util3.Messages";
	private final int maxRecorded;
	private final boolean recordResultIfSame;
	private final MessageUtil m;
	private final Set<String> msgs = new HashSet<>();
	private int diffs;

	public DefaultFileContentListener() {
		this(DEFAULT_MAX_DIFFERENCES, false);
	}

	public DefaultFileContentListener(int maxRecorded, boolean recordSames) {
		if (maxRecorded < 0) {
			throw new IllegalArgumentException("negative limit: " + maxRecorded);
		}
		this.maxRecorded = maxRecorded;
		this.recordResultIfSame = recordSames;
		this.m = new MessageUtil(RESOURCE_BUNDLE);
	}

	@Override
	public void fileComparison(Path p1, Path p2, FileContentComparison0 result) {
		assert result != null;

		if (!FileContentComparison0.SAME_CONTENT.equals(result)) {
			++diffs;
		}
		if (msgs.size() < maxRecorded) {
			if (recordResultIfSame || !FileContentComparison0.SAME_CONTENT.equals(result)) {
				String msg = m.formatMessage(result.toString(), p1, p2);
				msgs.add(msg);
			}
		}
	}

	public void clear() {
		msgs.clear();
		diffs = 0;
	}
	
	public int getDifferenceCount() {
		return diffs;
	}
	
	public Set<String> getMessages() {
		Set<String> retVal = new LinkedHashSet<>();
		retVal.addAll(msgs);
		return retVal;
	}

}