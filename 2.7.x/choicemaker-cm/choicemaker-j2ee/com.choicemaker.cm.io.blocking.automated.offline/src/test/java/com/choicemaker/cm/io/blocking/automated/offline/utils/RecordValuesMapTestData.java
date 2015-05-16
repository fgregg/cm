package com.choicemaker.cm.io.blocking.automated.offline.utils;

import com.choicemaker.cm.io.blocking.automated.offline.utils.RecordValuesMapTestTypes.IndexedValues;
import com.choicemaker.cm.io.blocking.automated.offline.utils.RecordValuesMapTestTypes.Input_Output;

import com.choicemaker.util.IntArrayList;

public class RecordValuesMapTestData {

	protected static IndexedValues[] columns0 = new IndexedValues[] {
		new IndexedValues(0, new int[] { }),
		new IndexedValues(1, new int[] { 10 }),
		new IndexedValues(2, new int[] { 20, 21 }),
		new IndexedValues(3, new int[] { 30, 31, 32 }),
		new IndexedValues(4, new int[] { 40, 41, 42, 43 })
	};

	protected static IntArrayList[] expected0 = new IntArrayList[] {
		new IntArrayList(),
		new IntArrayList(new int[] { 10 }),
		new IntArrayList(new int[] { 20, 21 }),
		new IntArrayList(new int[] { 30, 31, 32 }),
		new IntArrayList(new int[] { 40, 41, 42, 43 })
	};
	
	protected static IndexedValues[] columns1 = new IndexedValues[] {
		new IndexedValues(0, new int[] { }),
		new IndexedValues(2, new int[] { 20, 21 }),
		new IndexedValues(3, new int[] { 30, 31, 32 }),
		new IndexedValues(4, new int[] { 40, 41, 42, 43 })
	};

	protected static IntArrayList[] expected1 = new IntArrayList[] {
		new IntArrayList(),
		null,
		new IntArrayList(new int[] { 20, 21 }),
		new IntArrayList(new int[] { 30, 31, 32 }),
		new IntArrayList(new int[] { 40, 41, 42, 43 })
	};
	
	protected static IndexedValues[] columns2 = new IndexedValues[] {
		new IndexedValues(0, new int[] { }),
		new IndexedValues(3, new int[] { 30, 31, 32 }),
		new IndexedValues(4, new int[] { 40, 41, 42, 43 })
	};

	protected static IntArrayList[] expected2 = new IntArrayList[] {
		new IntArrayList(),
		null, null,
		new IntArrayList(new int[] { 30, 31, 32 }),
		new IntArrayList(new int[] { 40, 41, 42, 43 })
	};
	
	protected static IndexedValues[] columns3 = new IndexedValues[] {
		new IndexedValues(1, new int[] { 10 }),
		new IndexedValues(2, new int[] { 20, 21 }),
		new IndexedValues(3, new int[] { 30, 31, 32 }),
		new IndexedValues(4, new int[] { 40, 41, 42, 43 })
	};

	protected static IntArrayList[] expected3 = new IntArrayList[] {
		null,
		new IntArrayList(new int[] { 10 }),
		new IntArrayList(new int[] { 20, 21 }),
		new IntArrayList(new int[] { 30, 31, 32 }),
		new IntArrayList(new int[] { 40, 41, 42, 43 })
	};
	
	protected static IndexedValues[] columns4 = new IndexedValues[] {
		new IndexedValues(2, new int[] { 20, 21 }),
		new IndexedValues(3, new int[] { 30, 31, 32 }),
		new IndexedValues(4, new int[] { 40, 41, 42, 43 })
	};

	protected static IntArrayList[] expected4 = new IntArrayList[] {
		null, null,
		new IntArrayList(new int[] { 20, 21 }),
		new IntArrayList(new int[] { 30, 31, 32 }),
		new IntArrayList(new int[] { 40, 41, 42, 43 })
	};
	
	protected static IndexedValues[] columns5 = new IndexedValues[] {
		new IndexedValues(0, new int[] { }),
		new IndexedValues(1, new int[] { 10 }),
		new IndexedValues(2, new int[] { 20, 21 }),
		new IndexedValues(3, new int[] { 30, 31, 32 }),
	};

	protected static IntArrayList[] expected5 = new IntArrayList[] {
		new IntArrayList(),
		new IntArrayList(new int[] { 10 }),
		new IntArrayList(new int[] { 20, 21 }),
		new IntArrayList(new int[] { 30, 31, 32 }),
	};
	
	protected static IndexedValues[] columns6 = new IndexedValues[] {
		new IndexedValues(0, new int[] { }),
		new IndexedValues(1, new int[] { 10 }),
		new IndexedValues(2, new int[] { 20, 21 }),
	};

	protected static IntArrayList[] expected6 = new IntArrayList[] {
		new IntArrayList(),
		new IntArrayList(new int[] { 10 }),
		new IntArrayList(new int[] { 20, 21 }),
	};
	
	protected static IndexedValues[] columns7 = new IndexedValues[] {
		new IndexedValues(0, new int[] { }),
		new IndexedValues(0, new int[] { 10 }),
		new IndexedValues(2, new int[] { 20, 21 }),
		new IndexedValues(3, new int[] { 30, 31, 32 }),
		new IndexedValues(4, new int[] { 40, 41, 42, 43 })
	};

	protected static IntArrayList[] expected7 = new IntArrayList[] {
		new IntArrayList(new int[] { 10 }),
		null,
		new IntArrayList(new int[] { 20, 21 }),
		new IntArrayList(new int[] { 30, 31, 32 }),
		new IntArrayList(new int[] { 40, 41, 42, 43 })
	};
	
	protected static IndexedValues[] columns8 = new IndexedValues[] {
		new IndexedValues(0, new int[] { }),
		new IndexedValues(1, new int[] { 10 }),
		new IndexedValues(1, new int[] { 20, 21 }),
		new IndexedValues(3, new int[] { 30, 31, 32 }),
		new IndexedValues(4, new int[] { 40, 41, 42, 43 })
	};

	protected static IntArrayList[] expected8 = new IntArrayList[] {
		new IntArrayList(),
		new IntArrayList(new int[] { 20, 21, 10 }),
		null,
		new IntArrayList(new int[] { 30, 31, 32 }),
		new IntArrayList(new int[] { 40, 41, 42, 43 })
	};
	
	protected static IndexedValues[] columns9 = new IndexedValues[] {
		new IndexedValues(0, new int[] { }),
		new IndexedValues(1, new int[] { 10 }),
		new IndexedValues(1, new int[] { 20, 21 }),
		new IndexedValues(1, new int[] { 30, 31, 32 }),
		new IndexedValues(4, new int[] { 40, 41, 42, 43 })
	};

	protected static IntArrayList[] expected9 = new IntArrayList[] {
		new IntArrayList(),
		new IntArrayList(new int[] { 30, 31, 32, 20, 21, 10 }),
		null, null,
		new IntArrayList(new int[] { 40, 41, 42, 43 })
	};
	
	protected static IndexedValues[] columns10 = new IndexedValues[] {
		new IndexedValues(0, new int[] { }),
		new IndexedValues(1, new int[] { 10 }),
		new IndexedValues(1, new int[] { 20, 21 }),
		new IndexedValues(1, new int[] { 30, 31, 32 }),
		new IndexedValues(1, new int[] { 40, 41, 42, 43 })
	};

	protected static IntArrayList[] expected10 = new IntArrayList[] {
		new IntArrayList(),
		new IntArrayList(new int[] { 40, 41, 42, 43, 30, 31, 32, 20, 21, 10, })
	};
	
	protected static Input_Output[] testData = new Input_Output[] {
		new Input_Output(columns0, expected0),
		new Input_Output(columns1, expected1),
		new Input_Output(columns2, expected2),
		new Input_Output(columns3, expected3),
		new Input_Output(columns4, expected4),
		new Input_Output(columns5, expected5),
		new Input_Output(columns6, expected6),
		new Input_Output(columns7, expected7),
		new Input_Output(columns8, expected8),
		new Input_Output(columns9, expected9),
		new Input_Output(columns10, expected10)
	};
	
}
