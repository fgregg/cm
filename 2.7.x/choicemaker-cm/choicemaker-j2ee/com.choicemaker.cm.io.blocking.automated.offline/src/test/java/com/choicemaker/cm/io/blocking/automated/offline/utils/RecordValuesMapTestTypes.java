package com.choicemaker.cm.io.blocking.automated.offline.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSource;
import com.choicemaker.util.IntArrayList;

public class RecordValuesMapTestTypes {

	protected static class IndexedValues {
		public final int index;
		public final int[] values;

		public IndexedValues(int idx, int[] values) {
			this.index = idx;
			this.values = values;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + index;
			result = prime * result + Arrays.hashCode(values);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			IndexedValues other = (IndexedValues) obj;
			if (index != other.index) {
				return false;
			}
			if (!Arrays.equals(values, other.values)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "IndexedValues [index=" + index + ", values="
					+ Arrays.toString(values) + "]";
		}

	}

	public static class Input_Output {
		public final IRecValSource in;
		public final List<IntArrayList> out;

		public Input_Output(IndexedValues[] ce, IntArrayList[] iala) {
			this.in = createRecValSource(ce);
			this.out = new ArrayList<>();
			for (IntArrayList ial : iala) {
				this.out.add(ial);
			}
		}

		@Override
		public String toString() {
			return "Input_Output [in=" + in + ", out=" + out + "]";
		}

	}

	public static IRecValSource createRecValSource(
			final IndexedValues[] indexedValues) {
		return new IRecValSource() {

			private int current = 0;

			@Override
			public void open() throws BlockingException {
			}

			@Override
			public boolean hasNext() throws BlockingException {
				return current < indexedValues.length;
			}

			@Override
			public Long next() throws BlockingException {
				return Long.valueOf(current);
			}

			@Override
			public void close() throws BlockingException {
			}

			@Override
			public String getInfo() {
				return null;
			}

			@Override
			public void delete() throws BlockingException {
				throw new Error("not implemented");
			}

			@Override
			public long getNextRecID() throws BlockingException {
				return indexedValues[current].index;
			}

			@Override
			public IntArrayList getNextValues() throws BlockingException {
				int[] values = indexedValues[current].values;
				IntArrayList retVal = new IntArrayList(values);
				++current;
				return retVal;
			}

			@Override
			public boolean exists() {
				return true;
			}

			@Override
			public String toString() {
				return "IRecValSource [current =" + current
						+ ", indexedValues=" + Arrays.toString(indexedValues)
						+ "]";

			}

		};
	}

}
