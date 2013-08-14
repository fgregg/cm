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
package com.choicemaker.cm.matching.cfg.map;

/**
 * @author ajwinkel
 *
 */
public final class CharacterCache {

	private static final int LEN = 256;
	private static Character[] chars = new Character[LEN];

	/**
	 * Returns a character object corresponding to the
	 * input char.
	 * 
	 * This method may cache Character objects between
	 * invocations with the same char, but user code 
	 * should not depend on this.  That is, do not use
	 * reference equality with the returned Character objects!
	 */
	public static Character getCharacter(char c) {
		if (c < LEN) {
			Character cached = chars[c];
			if (cached == null) {
				cached = new Character(c);
				chars[c] = cached;
			}
		
			return cached;
		} else {
			return new Character(c);
		}
	}
	
	private CharacterCache() { }

}
