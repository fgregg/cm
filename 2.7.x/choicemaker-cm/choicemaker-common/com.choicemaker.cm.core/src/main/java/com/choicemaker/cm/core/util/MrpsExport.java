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
package com.choicemaker.cm.core.util;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.choicemaker.cm.core.Accessor;
import com.choicemaker.cm.core.ClueDesc;
import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.ActiveClues;
import com.choicemaker.cm.core.base.BooleanActiveClues;
import com.choicemaker.cm.core.base.MutableMarkedRecordPair;

/**
 * Exports a list of marked record pairs to an output stream.
 * @author ajwinkel (initial version)
 * @author rphall (AC_CLUE_NAMES,AC_GROUPS)
 */
public class MrpsExport {

	/** Magic value indicating that clues and rules are not exported */
	public static final int AC_NONE = 901;

	/** Magic value indicating that clues and rules are exported as a bit vector*/
	public static final int AC_BIT_VECTOR = 902;

	/** Magic value indicating that clues and rules are exported by index */
	public static final int AC_CLUE_INDICES = 903;

	/** Magic value indicating that clues and rules are exported by name */
	public static final int AC_CLUE_NAMES = 904;

	/**
	 * Magic value indicating that clues (but not rules) are exported by name
	 * and grouped by type (match, hold, differ).  Groups are separated by
	 * a delimiter (the default value is {@link #DEFAULT_GROUP_DELIM}).  This
	 * format is similar to the "explicit" feature format of
	 * <a href="www.cs.utah.edu/~hal/megam">MEGAM</a>
	 * and the default feature format of
	 * <a href="http://www.fjoch.com/YASMET.html">YASMET</a>. 
	 * The purpose of this format to enable other Maximum Entropy packages to
	 * evaluate ChoiceMaker feature sets (i.e. clues).
	 */
	public static final int AC_GROUPS = 905;

	private static final String DEFAULT_GROUP_DELIM = "#";
	private static final String DEFAULT_GROUP_ELEMENT_DELIM = " ";

	/**
	 * Exports a list of marked record pairs to an output stream.
	 * @param model the model used to evaluate probabilities and decisions.
	 * 	May be null if <code>acPolicy</code> is <code>AC_NONE</code>.
	 * @param pairs the pairs to be written
	 * @param w the output stream/writer
	 * @param ids if true, pair ids are written
	 * @param prob if true, probabilities are written
	 * @param dec if true, calculated (CM) decisions are written
	 * @param mrk if true, marked (human) decisions are written
	 * @param acPolicy a flag that indicates whether and how active clues are written.
	 * @param delim a field separator such as the pipe symbol ("|")
	 * @throws IOException
	 */
	public static void exportProbabilities(
		ImmutableProbabilityModel model,
		List pairs,
		Writer w,
		boolean ids,
		boolean prob,
		boolean dec,
		boolean mrk,
		boolean details,
		int acPolicy,
		String delim)
		throws IOException {

		// Preconditions
		if (model == null && acPolicy != AC_NONE) {
			throw new IllegalArgumentException(
				"non-null model required for active clues");
		}
		if (pairs == null) {
			throw new IllegalArgumentException("null pairs");
		}
		if (w == null) {
			throw new IllegalArgumentException("null writer");
		}
		if (acPolicy != AC_NONE
			&& acPolicy != AC_BIT_VECTOR
			&& acPolicy != AC_CLUE_INDICES
			&& acPolicy != AC_CLUE_NAMES
			&& acPolicy != AC_GROUPS) {
			throw new IllegalArgumentException(
				"invalid acPolicy = '" + acPolicy + "'");
		}
		if (delim == null || delim.trim().length() == 0) {
			throw new IllegalArgumentException("null or blank field delimiter");
		}

		DecimalFormat df = new DecimalFormat("##0.0000");
		int numClues = -1;
		if (acPolicy == AC_BIT_VECTOR) {
			numClues =
				model.getAccessor().getClueSet().size();
		}
		for (int i = 0; i < pairs.size(); i++) {
			MutableMarkedRecordPair mrp = (MutableMarkedRecordPair) pairs.get(i);
			int count = 0;
			if (ids) {
				count = write(w, mrp.getQueryRecord().getId().toString(), delim, count);
				count = write(w, mrp.getMatchRecord().getId().toString(), delim, count);
			}
			if (prob) {
				count = write(w, df.format(mrp.getProbability()), delim, count);
			}
			if (dec) {
				count = write(w, "calc_" + mrp.getCmDecision().toString(), delim, count);
			}
			if (mrk) {
				count = write(w, "mark_" + mrp.getMarkedDecision().toString(), delim, count);
			}
			if (details) {
				ActiveClues ac = mrp.getActiveClues();
				String s = model.getEvaluator().getProbabilityDetails(ac);
				count = write(w,s,delim,count);
			}
			if (acPolicy == AC_BIT_VECTOR) {
				writeACBitVector(w, mrp.getActiveClues(), numClues, delim, count);
			} else if (acPolicy == AC_CLUE_INDICES) {
				writeACClueIndices(w, mrp.getActiveClues(), delim, count);
			} else if (acPolicy == AC_CLUE_NAMES) {
				writeACClueNames(w, model, mrp.getActiveClues(), delim, count);
			} else if (acPolicy == AC_GROUPS) {
				final String groupDelim = DEFAULT_GROUP_DELIM;
				final String clueDelim = DEFAULT_GROUP_ELEMENT_DELIM;
				writeACGroups(w, model, mrp.getActiveClues(), delim, groupDelim, clueDelim, count);
			}
			w.write(Constants.LINE_SEPARATOR);
		}
		w.flush(); // Needed for FileWriter on Linux (at least) (a JVM bug?)
		w.close();
	} // 	public void exportProbabilities(..)

	private static int write(Writer w, String value, String delim, int count)
		throws IOException {
		if (count++ > 0) {
			w.write(delim);
		}
		w.write(value);
		return count;
	}

	private static void writeACBitVector(
		Writer w,
		ActiveClues ac,
		int numClues,
		String delim,
		int count)
		throws IOException {
		BooleanActiveClues bac = (BooleanActiveClues) ac;
		if (bac.size() > 0 || bac.sizeRules() > 0) {
			for (int i = 0; i < numClues; i++) {
				if (count > 0) {
					w.write(delim);
				}
				if (bac.containsClueOrRule(i)) {
					w.write("1");
				} else {
					w.write("0");
				}
			}
		}
	}

	private static void writeACClueIndices(
		Writer w,
		ActiveClues ac,
		String delim,
		int count)
		throws IOException {
		BooleanActiveClues bac = (BooleanActiveClues) ac;
		int[] clues = bac.getCluesAndRules();
		Arrays.sort(clues);
		for (int i = 0; i < clues.length; i++) {
			if (count > 0) {
				w.write(delim);
			}
			w.write(String.valueOf(clues[i]));
		}
	}

	private static void writeACClueNames(
		Writer w,
		ImmutableProbabilityModel model,
		ActiveClues ac,
		String delim,
		int count)
		throws IOException {
			
		Accessor acc = model.getAccessor();
		ClueDesc[] clueDesc = acc.getClueSet().getClueDesc();
			
		BooleanActiveClues bac = (BooleanActiveClues) ac;
		int[] clues = bac.getCluesAndRules();
		Arrays.sort(clues);
		for (int i = 0; i < clues.length; i++) {
			if (count > 0) {
				w.write(delim);
			}
			int clueIdx = clues[i];
			String clueName = clueDesc[clueIdx].getName();
			w.write(clueName);
		}
	}

	/**
	 * Writes a structured field consisting of clues, grouped by
	 * type, to the specified output. The names of match clues
	 * are written first; then the names of hold clues; and finally
	 * the names of differ clues.  The start of a groups is indicated
	 * by <code>groupDelim</code>.  Within a group, clues are
	 * separated by <code>clueDelim</code>.
	 * 
	 * @param w Output writer
	 * @param model Matching model
	 * @param ac Active clues (and rules)
	 * @param fieldDelim The general separator between this field
	 * (i.e. groups of clues) and other fields (e.g. query and match
	 * indices, probability score, and calculated or marked decision).
	 * @param groupDelim The separator between types of clues
	 * (i.e. matches, holds and differs)
	 * @param clueDelim The separator between clues within a group
	 * @param count Indicates whether this field is the first on a line
	 * (i.e. count == 0) or not (i.e. count > 0)
	 * @throws IOException
	 */
	private static void writeACGroups(
		Writer w,
		ImmutableProbabilityModel model,
		ActiveClues ac,
		String fieldDelim,
		String groupDelim,
		String clueDelim,
		int count)
		throws IOException {
			
		Accessor acc = model.getAccessor();
		ClueDesc[] clueDesc = acc.getClueSet().getClueDesc();
		BooleanActiveClues bac = (BooleanActiveClues) ac;
		int[] clues = bac.getCluesAndRules();
		Arrays.sort(clues);

		List matches = new ArrayList(clues.length);
		List holds = new ArrayList(clues.length);
		List differs = new ArrayList(clues.length);
		for (int i = 0; i < clues.length; i++) {
			int clueIdx = clues[i];
			String clueName = clueDesc[clueIdx].getName();
			Decision clueDecision = clueDesc[clueIdx].getDecision();
			boolean isClue = !clueDesc[clueIdx].rule;
			if (isClue) {
				if (Decision.MATCH.toSingleCharString().equals(clueDecision.toSingleCharString())) {
					matches.add(clueName);
				} else if (Decision.HOLD.toSingleCharString().equals(clueDecision.toSingleCharString())) {
					holds.add(clueName);
			    } else if (Decision.DIFFER.toSingleCharString().equals(clueDecision.toSingleCharString())) {
			    	differs.add(clueName);
		        } else {
		        	throw new IllegalStateException("Unexpected clue/decision: '" + clueName + "/" + clueDecision.toSingleCharString() + "'");
				}
			}
		}
		List[] groups = new List[] {
			matches,
			holds,
			differs
		};
		
		if (count > 0) {
			w.write(fieldDelim);
		}
		for (int ig = 0; ig < groups.length; ig++) {
			// Terminate the preceding group and clue name
			if (ig > 0) {
				w.write(clueDelim);
			}
			w.write(groupDelim);
			List group = groups[ig];
			for (int ic=0; ic < group.size(); ic++) {
				w.write(clueDelim);
				String clueName = (String) group.get(ic);
				w.write(clueName);
			}
		}

	}

} // MrpsExport

