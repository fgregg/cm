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
		new IntArrayList(new int[] { 10, 20, 21 }),
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
		new IntArrayList(new int[] { 10, 20, 21, 30, 31, 32 }),
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
		new IntArrayList(new int[] { 10, 20, 21, 30, 31, 32, 40, 41, 42, 43 })
	};

	protected static IndexedValues[] columns11 = new IndexedValues[] {
		new IndexedValues(0, new int[] { }),
		new IndexedValues(2, new int[] { 20, 21 }),
		new IndexedValues(3, new int[] { 30, 31, 32 }),
		new IndexedValues(4, new int[] { 40, 41, 42, 43 })
	};

	protected static IntArrayList[] expected11 = new IntArrayList[] {
		new IntArrayList(),
		null,
		new IntArrayList(new int[] { 20, 21 }),
		new IntArrayList(new int[] { 30, 31, 32 }),
		new IntArrayList(new int[] { 40, 41, 42, 43 })
	};

	protected static IndexedValues[] columns12 = new IndexedValues[] {
		new IndexedValues(0, new int[] { }),
		new IndexedValues(3, new int[] { 30, 31, 32 }),
		new IndexedValues(4, new int[] { 40, 41, 42, 43 })
	};

	protected static IntArrayList[] expected12 = new IntArrayList[] {
		new IntArrayList(),
		null, null,
		new IntArrayList(new int[] { 30, 31, 32 }),
		new IntArrayList(new int[] { 40, 41, 42, 43 })
	};

	protected static IndexedValues[] columns13 = new IndexedValues[] {
		new IndexedValues(1, new int[] { 10 }),
		new IndexedValues(2, new int[] { 20, 21 }),
		new IndexedValues(3, new int[] { 30, 31, 32 }),
		new IndexedValues(4, new int[] { 40, 41, 42, 43 })
	};

	protected static IntArrayList[] expected13 = new IntArrayList[] {
		null,
		new IntArrayList(new int[] { 10 }),
		new IntArrayList(new int[] { 20, 21 }),
		new IntArrayList(new int[] { 30, 31, 32 }),
		new IntArrayList(new int[] { 40, 41, 42, 43 })
	};

	protected static IndexedValues[] columns14 = new IndexedValues[] {
		new IndexedValues(2, new int[] { 20, 21 }),
		new IndexedValues(3, new int[] { 30, 31, 32 }),
		new IndexedValues(4, new int[] { 40, 41, 42, 43 })
	};

	protected static IntArrayList[] expected14 = new IntArrayList[] {
		null, null,
		new IntArrayList(new int[] { 20, 21 }),
		new IntArrayList(new int[] { 30, 31, 32 }),
		new IntArrayList(new int[] { 40, 41, 42, 43 })
	};

	protected static IndexedValues[] columns15 = new IndexedValues[] {
		new IndexedValues(0, new int[] { }),
		new IndexedValues(1, new int[] { 10, 10, 10 }),
		new IndexedValues(2, new int[] { 21, 20, 21 }),
		new IndexedValues(3, new int[] { 32, 31, 32, 30, 32, 31 }),
		new IndexedValues(4, new int[] { 40, 43, 42, 42, 42, 41, 41, 41 })
	};

	protected static IntArrayList[] expected15 = new IntArrayList[] {
		new IntArrayList(),
		new IntArrayList(new int[] { 10 }),
		new IntArrayList(new int[] { 20, 21 }),
		new IntArrayList(new int[] { 30, 31, 32 }),
		new IntArrayList(new int[] { 40, 41, 42, 43 })
	};

	protected static IndexedValues[] columns16 = new IndexedValues[] {
		new IndexedValues(0, new int[] { 10}),
		new IndexedValues(1, new int[] { 10, 10 }),
		new IndexedValues(2, new int[] { 10, 10, 10, 10 }),
		new IndexedValues(3, new int[] { 10, 10, 10, 10, 10, 10 }),
	};

	protected static IntArrayList[] expected16 = new IntArrayList[] {
		new IntArrayList(new int[] { 10 }),
		new IntArrayList(new int[] { 10 }),
		new IntArrayList(new int[] { 10 }),
		new IntArrayList(new int[] { 10 })
	};

	protected static IndexedValues[] columns17 = new IndexedValues[] {
		new IndexedValues(0, new int[] { 10, 10}),
		new IndexedValues(0, new int[] { 10, 10 }),
		new IndexedValues(2, new int[] { 20, 21, 20 }),
		new IndexedValues(3, new int[] { 32, 31, 30 }),
		new IndexedValues(4, new int[] { 40, 41, 42, 43 })
	};

	protected static IntArrayList[] expected17 = new IntArrayList[] {
		new IntArrayList(new int[] { 10 }),
		null,
		new IntArrayList(new int[] { 20, 21 }),
		new IntArrayList(new int[] { 30, 31, 32 }),
		new IntArrayList(new int[] { 40, 41, 42, 43 })
	};

	protected static IndexedValues[] columns18 = new IndexedValues[] {
		new IndexedValues(0, new int[] { }),
		new IndexedValues(1, new int[] { 10, 21, 20 }),
		new IndexedValues(1, new int[] { 20, 21 }),
		new IndexedValues(3, new int[] { 30, 31, 32 }),
		new IndexedValues(4, new int[] { 40, 41, 42, 43 })
	};

	protected static IntArrayList[] expected18 = new IntArrayList[] {
		new IntArrayList(),
		new IntArrayList(new int[] { 10, 20, 21 }),
		null,
		new IntArrayList(new int[] { 30, 31, 32 }),
		new IntArrayList(new int[] { 40, 41, 42, 43 })
	};

	protected static IndexedValues[] columns19 = new IndexedValues[] {
		new IndexedValues(0, new int[] { }),
		new IndexedValues(1, new int[] { 10, 21, 31 }),
		new IndexedValues(1, new int[] { 20, 21, 10 }),
		new IndexedValues(1, new int[] { 30, 31, 32, 21, 10 }),
		new IndexedValues(4, new int[] { 40, 41, 42, 43 })
	};

	protected static IntArrayList[] expected19 = new IntArrayList[] {
		new IntArrayList(),
		new IntArrayList(new int[] { 10, 20, 21, 30, 31, 32 }),
		null, null,
		new IntArrayList(new int[] { 40, 41, 42, 43 })
	};

	protected static IndexedValues[] columns20 = new IndexedValues[] {
		new IndexedValues(1, new int[] { 43, 32, 21, 10 }),
		new IndexedValues(1, new int[] { 42, 31, 20, 21 }),
		new IndexedValues(1, new int[] { 41, 30, 31, 32 }),
		new IndexedValues(1, new int[] { 42, 41, 40, 43 })
	};

	protected static IntArrayList[] expected20 = new IntArrayList[] {
		null,
		new IntArrayList(new int[] { 10, 20, 21, 30, 31, 32, 40, 41, 42, 43 })
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
		new Input_Output(columns10, expected10),
		new Input_Output(columns11, expected11),
		new Input_Output(columns12, expected12),
		new Input_Output(columns13, expected13),
		new Input_Output(columns14, expected14),
		new Input_Output(columns15, expected15),
		new Input_Output(columns16, expected16),
		new Input_Output(columns17, expected17),
		new Input_Output(columns18, expected18),
		new Input_Output(columns19, expected19),
		new Input_Output(columns20, expected20),
	};

}
