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
package com.choicemaker.cm.matching.en.us;

import com.choicemaker.cm.matching.gen.Sets;

/**
 * Utilities for dealing with US State, Possession, and Military State codes.
 * 
 * @author	Adam Winkel
 */
public final class StateUtils {

	/**
	 * Returns true iff stateCode is a code for one of the fifty states or is 'DC'.
	 * 
	 * @param stateCode the input state code
	 * @return true iff stateCode is a valid state code
	 */
	public static boolean isStateCode(String stateCode) {
		return Sets.includes("stateCodes", stateCode);
	}

	/**
	 * Returns true iff possessionCode is a valid US possession code.
	 * 
	 * @param possessionCode the input possession code
	 * @return true iff possessionCode is a valid possession code
	 */
	public static boolean isPossessionCode(String possessionCode) {
		return Sets.includes("possessionCodes", possessionCode);
	}
	
	/**
	 * Returns true iff militaryCode is a valid US military state code.
	 * 
	 * @param militaryCode the input military state code
	 * @return true iff militaryCode is a valid military code
	 */
	public static boolean isMilitaryCode(String militaryCode) {
		return Sets.includes("militaryStateCodes", militaryCode);
	}
	
	/**
	 * Returns true iff code is a valid state code, possession code, or military code.
	 * 
	 * @param code the input code
	 * @return true iff state code, possession code, or military code
	 */
	public static boolean isValidCode(String code) {
		return isStateCode(code) || isPossessionCode(code) || isMilitaryCode(code);	
	}
	
	private StateUtils() { }

}
