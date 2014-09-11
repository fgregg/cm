package com.choicemaker.cm.persist0;

import com.choicemaker.cm.core.Decision;

public enum CMP_DefaultFeatureType {
	CLUE_MATCH(true, "match"), CLUE_HOLD(true, "hold"), CLUE_DIFFER(true,
			"differ"), RULE_NONE(false, "none"), RULE_MATCH(false, "match"),
	RULE_HOLD(false, "hold"), RULE_DIFFER(false, "differ"), RULE_NOMATCH(false,
			"nomatch"), RULE_NOHOLD(false, "nohold"), RULE_NODIFFER(false,
			"nodiffer");
	static String CLUE = "clue";
	static String RULE = "rule";
	private final boolean isClue;
	private final String nickname;

	CMP_DefaultFeatureType(boolean isClue, String nick) {
		this.isClue = isClue;
		this.nickname = nick;
		assert nickname.equals(nick.toLowerCase());
	}

	public boolean isClue() {
		return isClue;
	}

	public boolean isRule() {
		return !isClue;
	}

	public String getNickName() {
		return nickname;
	}

	public String toString() {
		String type = isClue ? CLUE : RULE;
		String retVal = type + nickname;
		assert retVal.equals(retVal.toLowerCase());
		return retVal;
	}

	private static String toName(boolean isClue, String s1) {
		String s0 = isClue ? CLUE : RULE;
		return toName(s0, s1);
	}

	private static String toName(String s0, String s1) {
		return s0.toUpperCase() + "_" + s1.toUpperCase();
	}

	static CMP_DefaultFeatureType fromFlaggedNickname(boolean isClue, String s) {
		String enumName = toName(isClue, s);
		CMP_DefaultFeatureType retVal =
			CMP_DefaultFeatureType.valueOf(enumName);
		return retVal;
	}

	static CMP_DefaultFeatureType fromString(String s) {
		if (s == null) {
			throw new IllegalArgumentException("null argument");
		}
		s = s.trim().toLowerCase();
		if (s.isEmpty()) {
			throw new IllegalArgumentException("blank argument");
		}
		String[] components = s.split(":");
		if (components.length != 2) {
			throw new IllegalArgumentException("invalid form: '" + s + "'");
		}
		String enumName = toName(components[0], components[1]);
		CMP_DefaultFeatureType retVal =
			CMP_DefaultFeatureType.valueOf(enumName);
		return retVal;
	}

	static CMP_DefaultFeatureType fromDecision(boolean isClue, Decision d) {
		if (d == null) {
			throw new IllegalArgumentException("null argument");
		}
		CMP_DefaultFeatureType retVal =
			fromFlaggedNickname(isClue, d.getName());
		return retVal;
	}
}