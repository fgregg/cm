package com.choicemaker.cm.io.blocking.automated.offline.data;

import static com.choicemaker.cm.core.Decision.HOLD;
import static com.choicemaker.cm.core.Decision.MATCH;
import static com.choicemaker.cm.io.blocking.automated.offline.core.Constants.EXPORT_NOTE_SEPARATOR;
import static com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_SOURCE_ROLE.MASTER;
import static com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_SOURCE_ROLE.STAGING;

import java.util.SortedSet;
import java.util.TreeSet;

import com.choicemaker.cm.core.ClueSet;
import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.base.ActiveClues;
import com.choicemaker.cm.core.base.Evaluator;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_SOURCE_ROLE;

public class MatchRecordUtils {

	/**
	 * This method compares two records and returns a MatchRecord2 object.
	 * 
	 * @param q
	 *            - first record
	 * @param m
	 *            - second record
	 * @param isStage
	 *            - indicates if the second record is staging or master
	 */
	public static <T extends Comparable<T>> MatchRecord2<T> compareRecords(
			ClueSet clueSet, boolean[] enabledClues,
			ImmutableProbabilityModel model, Record q, Record m,
			boolean isStage, float low, float high) {

		MatchRecord2<T> retVal = null;
		if ((q != null) && (m != null)) {
			final Evaluator e = model.getEvaluator();
			final ActiveClues ac = clueSet.getActiveClues(q, m, enabledClues);
			float p = e.getProbability(ac);
			final Decision d = e.getDecision(ac, p, low, high);
			if (d == MATCH || d == HOLD) {
				final String notes =
					MatchRecordUtils.getNotesAsDelimitedString(ac, model);
				final RECORD_SOURCE_ROLE role = isStage ? STAGING : MASTER;
				// If both id's are staging, the smaller one should be first
				@SuppressWarnings("unchecked")
				final T qid = (T) q.getId();
				@SuppressWarnings("unchecked")
				final T mid = (T) m.getId();
				final T i1;
				final T i2;
				if (isStage && mid.compareTo(qid) < 0) {
					i1 = mid;
					i2 = qid;
				} else {
					i1 = qid;
					i2 = mid;
				}
				retVal = new MatchRecord2<T>(i1, i2, role, p, d, notes);
			}
		}

		return retVal;
	}

	/** Inclusive minimum match probability */
	public static final double MIN_PROBABILITY = 0.0;
	/** Inclusive maximum match probability */
	public static final double MAX_PROBABILITY = 1.0;

	private MatchRecordUtils() {
	}

	/**
	 * This method concatenates the notes into a single string delimited by
	 * Constants.EXPORT_NOTE_SEPARATOR.
	 * 
	 * @param notes
	 * @return
	 */
	public static String getNotesAsDelimitedString(ActiveClues ac,
			ImmutableProbabilityModel model) {

		String[] notes = ac.getNotes(model);
		String retVal = getNotesAsDelimitedString(notes);
		return retVal;
	}

	/**
	 * This method concatenates the notes into a single string delimited by
	 * Constants.EXPORT_NOTE_SEPARATOR.
	 * 
	 * @param notes
	 * @return
	 */
	public static String getNotesAsDelimitedString(String[] notes) {
		String retVal = null;
		if (notes != null && notes.length > 0) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < notes.length; i++) {
				sb.append(notes[i]);
				sb.append(Constants.EXPORT_NOTE_SEPARATOR);
			}
			sb.deleteCharAt(sb.length() - 1);
			retVal = sb.toString();
		}
		return retVal;
	}

	public static boolean isValidProbability(double probability) {
		return probability >= MIN_PROBABILITY && probability <= MAX_PROBABILITY;
	}

	public static String[] notesFromDelimitedString(String s) {
		String[] retVal = null;
		if (s != null) {
			s = s.trim();
			if (!s.isEmpty()) {
				String regex = "\\\\" + EXPORT_NOTE_SEPARATOR;
				String[] raw = s.split(regex);
				SortedSet<String> sorted = notesToSortedSet(raw);
				if (sorted.size() > 0) {
					retVal = sorted.toArray(new String[sorted.size()]);
				} else {
					assert retVal == null;
				}
			} else {
				assert retVal == null;
			}
		}
		return retVal;
	}

	public static SortedSet<String> notesToSortedSet(String[] notes) {
		SortedSet<String> retVal = new TreeSet<>();
		if (notes != null) {
			for (String note : notes) {
				if (note == null) {
					continue;
				}
				note = note.trim();
				if (note.isEmpty()) {
					continue;
				}
				retVal.add(note);
			}
		}
		return retVal;
	}

	public static String notesToString(SortedSet<String> sorted) {
		String retVal = null;
		if (sorted != null && sorted.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (String note : sorted) {
				assert note != null;
				assert !note.isEmpty();
				sb.append(note);
				sb.append(EXPORT_NOTE_SEPARATOR);
			}
			retVal = sb.toString();
			final int lastIndex = retVal.length() - 1;
			assert retVal.charAt(lastIndex) == EXPORT_NOTE_SEPARATOR;
			retVal = retVal.substring(0, lastIndex);
			if (retVal.length() == 0) {
				retVal = null;
			} else {
				assert retVal.charAt(retVal.length() - 1) != EXPORT_NOTE_SEPARATOR;
			}
		} else {
			assert retVal == null;
		}
		return retVal;
	}

	public static String notesToString(String[] notes) {
		String retVal = null;
		if (notes != null) {
			SortedSet<String> sorted = notesToSortedSet(notes);
			retVal = notesToString(sorted);
		}
		return retVal;
	}

}
