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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.choicemaker.cm.core.util.DoubleValuedHashMap;
import com.choicemaker.cm.matching.cfg.TokenType;
import com.choicemaker.cm.matching.gen.Maps;
import com.choicemaker.cm.matching.gen.Sets;

/**
 * TokenType subclass for which the membership in the TokenType,
 * the implied rule probabilities, and the standard token forms
 * are defined by Sets and Maps.
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:00 $
 */
public class SetTokenType extends TokenType {

	/** A Collection of Strings listing the Tokens which can take on this TokenType */
	protected Collection members = new HashSet();

	/** Mapping from Token to implied rule probability */
	protected DoubleValuedHashMap probabilities;
	
	/** The default probability if a Token has no entry in the probabilities map */
	protected double defaultProbability;

	/** 
	 * A Map from Tokens to standard token forms, for example, &quot;AVENUE&quot; to
	 * &quot;AVE&quot;
	 */
	protected Map standards;

	/**
	 * Create a new SetTokenType with the specified name.
	 */
	public SetTokenType(String name) {
		super(name);
	}
	
	public SetTokenType(String name, Set members) {
		super(name);
		setMembers(members);
		setDefaultProbability(1.0 / members.size());
	}
	
	/**
	 * Creates a new SetTokenType with the specified name, whose membership is 
	 * defined by the Set (accessed through the Sets utility) named by <code>setName</code>.
	 * 
	 * @param name the name of this SetTokenType
	 * @param setName the name of the membership Set
	 * @throws IllegalArgumentException if the specified Set does not exist.
	 * @see com.choicemaker.cm.matching.gen.Sets
	 * @see #setMembers(String)
	 */
	public SetTokenType(String name, String setName) {
		this(name, setName, false);
	}

	/**
	 * Creates a new SetTokenType with the specified name, whose membership is defined by 
	 * the Set named by setName.  If createUniformProb is set to true, each token gets the
	 * same probability, namely 1 / # members.
	 */
	public SetTokenType(String name, String setName, boolean createUniformProb) {
		super(name);
		setMembers(setName);
		if (createUniformProb) {
			setDefaultProbability(1.0 / members.size());
		}
	}

	/**
	 * Creates a new SetTokenType with the specified name, whose membership is
	 * defined by the Set (accessed through the Sets utility) named by <code>setName</code>,
	 * whose occurrences count Map (accessed through the Maps utility) is named by 
	 * <code>countsName</code>, and whose default probability (should a Token that can
	 * take on this TokenType) is given by <code>defaultProb</code>
	 * 
	 * @throws IllegalArgumentException if the specified members Set or counts Map 
	 * does not exist.
	 * @see com.choicemaker.cm.matching.gen.Sets
	 * @see com.choicemaker.cm.matching.gen.Maps
	 * @see #setMembers(String)
	 * @see #setCounts(String)
	 */
	public SetTokenType(String name, String setName, String countsName, double defaultProb) {
		this(name, setName);
		setCounts(countsName);
		setDefaultProbability(defaultProb);
	}

	/**
	 * Creates a new SetTokenType with the specified name, whose membership is
	 * defined by the Set (accessed through the Sets utility) named by <code>setName</code>,
	 * whose occurrences count Map (accessed through the Maps utility) is named by 
	 * <code>countsName</code>, whose default probability (should a Token that can
	 * take on this TokenType) is given by <code>defaultProb</code>, and whose standard
	 * token forms are defined by the Map named by <code>standardsName</code>.
	 * 
	 * @throws IllegalArgumentException if the specified members or standards Sets 
	 * or counts Map does not exist.
	 * @see com.choicemaker.cm.matching.gen.Sets
	 * @see com.choicemaker.cm.matching.gen.Maps
	 * @see #setMembers(String)
	 * @see #setCounts(String)
	 * @see #setStandards(String)
	 */
	public SetTokenType(String name, String setName, String countsName, 
						double defaultProb, String standardsName) {
		this(name, setName, countsName, defaultProb);
		setStandards(standardsName);
	}
	
	/**
	 * If the members Set is not null, returns true iff the members set
	 * contains the specified token.  If the members Set is null, returns false.
	 * 
	 * It is expected that child classes may wish to override this behavior.  As
	 * an example of when a TokenType might want to do so, see WordTokenType.
	 * 
	 * @see WordTokenType#canHaveToken(String)
	 */
	public boolean canHaveToken(String token) {
		if (members != null)
			return members.contains(token);
			
		return false;
	}

	/**
	 * If <code>setCounts(Map)</code> has been called or a counts Map
	 * was passed to the constructor, and the probabilties map has an
	 * entry for the specified token, return as the probabilities Map says.
	 * 
	 * Otherwise, return the default probability.
	 */
	protected double getTokenProbability(String token) {
		if (probabilities != null) {
			double prob = probabilities.getDouble(token);
			if (prob > 0) {
				return prob;
			}
		}
		
		return defaultProbability;
	}
	
	/**
	 * Returns the standard token form for the specified token.
	 *
	 * If the standard tokens Map is not null and has an entry for
	 * <code>token</code>, return the value of that entry.  Otherwise,
	 * just return token.
	 * 
	 * It is expected that some subclasses may wish to override this
	 * behavior.
	 */
	protected String getStandardToken(String token) {
		if (standards != null) {
			Object standard = standards.get(token);
			if (standard != null) {
				return standard.toString();
			}
		}
			
		return token;
	}

	//
	// Modifying the Sets/Maps.
	//

	/**
	 * Set the Members Collection to the one named by <code>membersName</code>
	 * from the Sets utilty.
	 * 
	 * @throws IllegalArgumentException if the named Set does not exist.
	 * @see com.choicemaker.cm.matching.gen.Sets
	 */
	public void setMembers(String membersName) {
		Collection m = Sets.getCollection(membersName);
		if (m == null) {
			throw new IllegalArgumentException(
				"The specified set (" + membersName + ") doesn't exist.");
		}
		
		setMembers(m);		
	}

	/**
	 * Sets the members Collection.
	 */
	public void setMembers(Collection m) {
		this.members = new HashSet(m);	
	}
	
	public void setMembers(Set s) {
		this.members = new HashSet(s);
	}
	
	public void addMembers(Set m) {
		if (members == null) {
			members = new HashSet(m);
		} else {
			members.addAll(m);	
		}
	}

	/**
	 * Recalculates the probabilities for this TokenType's implied rules
	 * from the map named by <code>countsName</code> in the Maps utility.
	 * See <code>setCounts(Map, int)</code> for more information.
	 * 
	 * @throws IllegalArgumentException if the named Map does not exist.
	 * @throws ClassCastException if counts is not a map from Strings to Integers.
	 * @see com.choicemaker.cm.matching.gen.Maps
	 * @see #setCounts(Map, int)
	 */	
	public void setCounts(String countsName) {
		Map counts = Maps.getMap(countsName);
		if (counts == null) {
			throw new IllegalArgumentException(
				"The specified map (" + countsName + ") doesn't exist.");
		}
		
		setCounts(counts, 0);			
	}
	
	/**
	 * Recalculates the probabilities for this TokenType's implied rules
	 * from <code>counts</code> with the number of unincluded tokens as 0.
	 * 
	 * @throws ClassCastException if counts is not a map from Strings to Integers.
	 * @see SetTokenType#setCounts(Map, int)
	 */
	public void setCounts(Map counts) {
		setCounts(counts, 0);	
	}
	
	/**
	 * Recalculates the probabilities for this TokenType's implied rules
	 * from <code>counts</code>, allowing for <code>notIncluded</code> 
	 * unseen tokens.  <code>counts</code> is taken to be a mapping from
	 * Strings to Integer objects, where each key represents a token for
	 * this TokenType, and the corresponding value is the number of times
	 * it was seen in a particular training corpus.  <code>notIncluded</code>
	 * is the number of members of this TokenType that were not seen.  Each of
	 * them is (by default) assigned a count of 1.
	 * 
	 * The procedure works as follows:
	 * 
	 * <ol>
	 * <li>
	 * The total number of occurrences of this TokenType, <it>total</it>, is computed
	 * as the sum of the values of the counts Map plus <code>notIncluded</code>, 
	 * to approximate the effect of unseen Tokens.
	 * <li>
	 * The probability of each implied rules whose Token has an entry in <code>counts</code>
	 * is computed as that Token's value divided by <it>total</it>.
	 * <li>
	 * Since we assign a default count of 1 to each unseen Token, the defualt probability
	 * is set to 1 / <it>total</it>.
	 * </ol>
	 * 
	 * @throws ClassCaseException if counts is not a mapping from Strings to Integers
	 */
	public void setCounts(Map counts, int notIncluded) {
		
		double sum = notIncluded;
		if (sum <= 0) {
			sum = 1E-50;	
		}

		// count the total number of occurrences.
		Iterator itValues = counts.values().iterator();
		while (itValues.hasNext()) {
			sum += ((Integer)itValues.next()).intValue();
		}
		
		// compute the probabilities for the tokens in the counts
		// map...
		probabilities = new DoubleValuedHashMap();
		Iterator itKeys = counts.keySet().iterator();
		while (itKeys.hasNext()) {
			String key = (String)itKeys.next();
			Integer value = (Integer)counts.get(key);
			probabilities.putDouble(key, value.intValue() / sum);
		}
		
		// set the default probability for those that aren't.
		if (sum >= 1) {
			setDefaultProbability( 1.0 / sum );
		} else {
			setDefaultProbability(0);
		}
	}
	
	/**
	 * Sets the standard token forms Map to the map--held in the Maps utility--
	 * named by <code>standardsName</code>.
	 * 
	 * @throws IllegalArgumentException if the named Map doesn't exist
	 */
	public void setStandards(String standardsName) {
		Map s = Maps.getMap(standardsName);
		if (s == null) {
			throw new IllegalArgumentException(
				"The specified map (" + standardsName + ") doesn't exist.");
		}

		setStandards(s);	
	}
	
	/**
	 * Sets the standard token forms Map to the specified Map.
	 */
	public void setStandards(Map standards) {
		this.standards = standards;
	}

	/**
	 * Sets the default probability for this TokenType's implied rules.
	 * 
	 */
	public void setDefaultProbability(double defProb) {
		this.defaultProbability = defProb;	
	}

}
