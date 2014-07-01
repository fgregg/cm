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
package com.choicemaker.cm.validation.eclipse.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

import com.choicemaker.cm.matching.gen.Sets;
import com.choicemaker.cm.validation.AbstractSetBasedValidator;
import com.choicemaker.cm.validation.IPatternMatcher;
import com.choicemaker.util.StringUtils;

/**
 * Validates a non-null name by checking whether it matches
 * any pattern in some set of patterns. Null names are considered invalid.
* @ see StringPatternInvalidator
 *
 * @author rphall
 * @version $Revision: 1.2 $ $Date: 2010/03/29 14:44:29 $
 */
public class StringPatternValidator
	extends AbstractSetBasedValidator
	implements IPatternMatcher {

	/** The hash character (#) */
	public static final String COMMENT_FLAG = "#";

	protected static Logger logger =
		Logger.getLogger(StringPatternValidator.class);
	protected Pattern[] patterns;

	protected String setName;

	/**
	 * Partially constructs a validator. The
	 * {@link #setNamedSet(String)} method must
	 * be called to finish construction.
	 */
	public StringPatternValidator() {
	}

	/**
	 * Constructs a validitor from a named set of patterns.
	 * @param setName a named set in the collection
	 * com.choicemaker.cm.matching.gen.Sets;
	 */
	public StringPatternValidator(String setName) {
		initializeSetNameAndPatterns(setName);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.ISetBasedValidator#getNamedSet()
	 */
	public String getNamedSet() {
		if (this.setName == null) {
			throw new IllegalStateException("set name not initialized");
		}
		return this.setName;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.ISetBasedValidator#setNamedSet(String)
	 */
	public void setNamedSet(String setName) {
		initializeSetNameAndPatterns(setName);
	}

	private void initializeSetNameAndPatterns(String setName) {
		// Precondition
		if (!StringUtils.nonEmptyString(setName)) {
			throw new IllegalArgumentException("null or blank set name");
		}

		this.setName = setName;
		List patternList = new ArrayList();
		Collection c = Sets.getCollection(this.setName);
		for (Iterator i = c.iterator(); i.hasNext();) {
			String s = (String) i.next();
			boolean nonEmptyString = StringUtils.nonEmptyString(s);
			if (nonEmptyString && !s.startsWith(COMMENT_FLAG)) {
				logger.debug(setName + ": adding '" + s + "'");
				Pattern p = null;
				try {
					p = Pattern.compile(s);
				} catch (PatternSyntaxException x) {
					String msg = "invalid pattern '" + s + "'";
					logger.error(msg, x);
					throw x;
				}
				patternList.add(p);
			} else if (nonEmptyString) {
				logger.debug(setName + ": skipping '" + s + "'");
			} else {
				logger.debug(setName + ": skipping blank line");
			}
		}
		this.patterns = (Pattern[]) patternList.toArray(new Pattern[0]);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.IPatternMatcher#getPatterns()
	 */
	public Pattern[] getPatterns() {
		if (this.patterns == null) {
			throw new IllegalStateException("patterns not initialized");
		}

		Pattern[] retVal = new Pattern[this.patterns.length];
		for (int i = 0; i < this.patterns.length; i++) {
			String pattern = this.patterns[i].pattern();
			retVal[i] = Pattern.compile(pattern);
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.ISetBasedValidator#getSetContents()
	 */
	public Set getSetContents() {
		if (this.patterns == null) {
			throw new IllegalStateException("patterns not initialized");
		}

		Set retVal = new HashSet();
		for (int i = 0; i < this.patterns.length; i++) {
			retVal.add(this.patterns[i].pattern());
		}
		return retVal;
	}

	public Class[] getValidationTypes() {
		return new Class[] { String.class };
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.eclipse.IValidator#isValid(java.lang.String)
	 */
	public boolean isValid(Object object) {
		boolean retVal = false;
		if (object != null && object instanceof String) {
			retVal = isMatch((String) object);
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.IPatternMatcher#isMatch(java.lang.String)
	 */
	public boolean isMatch(String s) {
		boolean retVal = false;
		Matcher[] matches = getAllMatches(s);
		if (matches.length > 0) {
			retVal = true;
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.IPatternMatcher#getFirstMatch(java.lang.String)
	 */
	public Matcher getFirstMatch(String s) {
		Matcher retVal = null;
		Matcher[] matches = getAllMatches(s,true);
		if (matches.length > 0) {
			retVal = matches[0];
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.validation.IPatternMatcher#getAllMatches(java.lang.String)
	 */
	public Matcher[] getAllMatches(String s) {
		return getAllMatches(s,false);
	}

	protected Matcher[] getAllMatches(String s, boolean firstMatchOnly) {
		// Preconditions
		if (this.patterns == null) {
			throw new IllegalStateException("patterns not initialized");
		}

		List matches = new ArrayList();
		for (int i = 0; i < this.patterns.length; i++) {
			Matcher matcher = this.patterns[i].matcher(s);
			if (matcher.matches()) {
				matches.add(matcher);
				if (firstMatchOnly) {
					break;
				} 
			}
		}
		Matcher[] retVal = (Matcher[]) matches.toArray(new Matcher[matches.size()]);
		return retVal;
	}

}

