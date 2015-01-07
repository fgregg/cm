package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

public class OabaPairResultInteger extends AbstractPairResultEntity<Integer> {

	private static final long serialVersionUID = 271L;
	
	public static Integer computeIdFromString(String s) {
		Integer retVal = null;
		if (s != null && !s.trim().isEmpty()) {
			retVal = Integer.valueOf(s);
		}
		return retVal;
	}
	
	public static String exportIdToString(Integer id) {
		String retVal = null;
		if (id != null) {
			retVal = id.toString();
		}
		return retVal;
	}

	@Override
	protected Integer idFromString(String s) {
		return computeIdFromString(s);
	}

	@Override
	protected String idToString(Integer id) {
		return exportIdToString(id);
	}

}
