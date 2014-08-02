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
package com.choicemaker.cm.mmdevtools.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author    Adam Winkel
 * @version   
 */
public class Profile {

	private static Pattern letterPattern = Pattern.compile("[A-Za-z]");
	private static Pattern digitPattern = Pattern.compile("\\d");
	private static Pattern spacePattern = Pattern.compile("\\s{2,}");

	private static Pattern collapseLetterPattern = Pattern.compile("[A-Za-z]+");
	private static Pattern collapseDigitPattern = Pattern.compile("\\d+");

	public static String profile(String s) {
		Matcher m;
		m = letterPattern.matcher(s);
		s = m.replaceAll("A");
		m = digitPattern.matcher(s);
		s = m.replaceAll("9");
		m = spacePattern.matcher(s);
		s = m.replaceAll(" ");
		
		return s;
	}
	
	public static String collapsedProfile(String s) {
		Matcher m;
		m = collapseLetterPattern.matcher(s);
		s = m.replaceAll("A");
		m = collapseDigitPattern.matcher(s);
		s = m.replaceAll("9");
		m = spacePattern.matcher(s);
		s = m.replaceAll(" ");
		
		return s;
	}

}
