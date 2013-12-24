/*
 * Copyright (c) 2001, 2009 ChoiceMaker Technologies, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ChoiceMaker Technologies, Inc. - initial API and implementation
 */
package com.choicemaker.cm.matching.cfg.tokentype;

import java.util.*;

import com.choicemaker.cm.matching.cfg.TokenType;
import com.choicemaker.cm.matching.gen.LongestCommonSubsequence;

/**
 * Comment
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:59 $
 */
public class LcsTokenType extends TokenType {

	protected HashMap members;
	protected double defaultProbability = 0;
	protected int minLength = 0;
	protected int minPrefixLength = 0;

	public LcsTokenType(String name) {
		this(name, new HashSet(), 0);
	}
	
	public LcsTokenType(String name, Set members) {
		this(name, members, 0);
	}

	public LcsTokenType(String name, Set members, double defaultProbability) {
		super(name);
		setMembers(members);
		setDefaultProbability(defaultProbability);
		setMinLength(1);
		setMinPrefixLength(2);
	}

	public void setMembers(Set m) {
		this.members = new HashMap();
		Iterator itMembers = m.iterator();
		while (itMembers.hasNext()) {
			String member = (String)itMembers.next();
			Character c = new Character(member.charAt(0));
			HashSet forC = (HashSet) members.get(c);
			if (forC == null) {
				forC = new HashSet();
				this.members.put(c, forC);
			}
			forC.add(member);
		}
		
		itMembers = members.keySet().iterator();
		while (itMembers.hasNext()) {
			Object key = itMembers.next();
			HashSet forKey = (HashSet) members.get(key);
			String[] s = new String[forKey.size()];
			int index = 0;
			Iterator itForKey = forKey.iterator();
			while (itForKey.hasNext()) {
				s[index++] = (String)itForKey.next();
			}
			members.put(key, s);
		}
	}

	public void setDefaultProbability(double p) {
		this.defaultProbability = p;
	}

	public void setMinLength(int len) {
		this.minLength = len;
	}
	
	public void setMinPrefixLength(int len) {
		if (len < 0) {
			this.minPrefixLength = 0;
		} else {
			this.minPrefixLength = len;
		} 
	}

	public boolean canHaveToken(String token) {
		if (token == null || token.length() < minLength) {
			return false;
		}

		int len = token.length();
		String pfx = token.substring(0, Math.min(len, minPrefixLength));
		
		String[] forFirstChar = (String[])members.get(new Character(token.charAt(0)));
		if (forFirstChar == null) {
			return false;
		} else {
			for (int i = 0; i < forFirstChar.length; i++) {
				if (len > forFirstChar[i].length()) {
					continue;
				} else if (forFirstChar[i].startsWith(pfx) &&
						   LongestCommonSubsequence.isLcsAbbrev(token, forFirstChar[i], 1)) {
					return true;
				}
			}
		}
		
		return false;
	}

	protected double getTokenProbability(String token) {
		if (canHaveToken(token)) {
			return defaultProbability;
		} else {
			return 0.0;
		}
	}

}
