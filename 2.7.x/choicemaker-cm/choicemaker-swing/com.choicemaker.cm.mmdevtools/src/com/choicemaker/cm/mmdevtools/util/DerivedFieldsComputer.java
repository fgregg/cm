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

import java.lang.reflect.Field;
import java.util.Date;

import com.choicemaker.cm.core.Descriptor;
import com.choicemaker.cm.core.DescriptorCollection;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSink;
import com.choicemaker.cm.core.RecordSource;

/**
 * @author Adam Winkel
 * 
 */
public class DerivedFieldsComputer {

	protected RecordSource source;
	protected IProbabilityModel sourceModel;
	protected RecordSink sink;
	protected IProbabilityModel sinkModel;
	protected String[][] fieldNames;
	
	protected Descriptor sourceDescriptor;
	protected Descriptor sinkDescriptor;
	protected Descriptor[] sourceDescriptors;
	protected Descriptor[] sinkDescriptors;
	protected int[][] sourceIndices;
	protected int[][] sinkIndices;
	protected Field[] childArrayFields;
	protected Object[] zeroArrays;

	public DerivedFieldsComputer(RecordSource source, IProbabilityModel sourceModel, RecordSink sink, IProbabilityModel sinkModel, String[][] fieldNames) {
		this.source = source;
		this.sourceModel = sourceModel;
		this.sink = sink;
		this.sinkModel = sinkModel;
		this.fieldNames = fieldNames;
		
		sourceDescriptor = sourceModel.getAccessor().getDescriptor();
		sinkDescriptor = sinkModel.getAccessor().getDescriptor();
	}

	public void compute() throws Exception {
		initFieldIndices();
		
		source.setModel(sourceModel);
		source.open();
		
		sink.setModel(sinkModel);
		sink.open();
		
		int num = 0;
		while (source.hasNext()) {
			Record r = source.getNext();
			Record rPrime = sourceToTarget(r);
			
			sink.put(rPrime);

			if (++num % 10000 == 0) {
				System.out.println(num + "  " + new Date());	
			}
		}
		
		sink.close();
		source.close();
	}

	protected void initFieldIndices() throws Exception {
		DescriptorCollection sourceDc = new DescriptorCollection(sourceDescriptor);
		sourceDescriptors = sourceDc.getDescriptors();
		sourceIndices = new int[fieldNames.length][];
		for (int i = 0; i < sourceIndices.length; i++) {
			sourceIndices[i] = new int[fieldNames[i].length];
			for (int j = 0; j < sourceIndices[i].length; j++) {
				sourceIndices[i][j] = sourceDescriptors[i].getColumnIndexByName(fieldNames[i][j]);
			}
		}

		DescriptorCollection sinkDc = new DescriptorCollection(sinkDescriptor);
		sinkDescriptors = sinkDc.getDescriptors();
		sinkIndices = new int[fieldNames.length][];
		for (int i = 0; i < sinkIndices.length; i++) {
			sinkIndices[i] = new int[fieldNames[i].length];
			for (int j = 0; j < sinkIndices[i].length; j++) {
				sinkIndices[i][j] = sinkDescriptors[i].getColumnIndexByName(fieldNames[i][j]);
			}
		}
		
		Class cls = sinkDescriptor.getHandledClass();
		childArrayFields = new Field[sinkDescriptors.length];
		zeroArrays = new Object[sinkDescriptors.length];
		for (int i = 1; i < sinkDescriptors.length; i++) {
			String name = sinkDescriptors[i].getName();
			childArrayFields[i] = cls.getField(name);
			zeroArrays[i] = sinkDescriptors[i].getHandledClass().getField("__zeroArray").get(null);
		}			
	}

	protected Record sourceToTarget(Record r) throws Exception {
		Record rPrime = createSinkRecord(r, sourceDescriptors[0], sourceIndices[0], sinkDescriptors[0], sinkIndices[0]);

		for (int i = 1; i < sourceDescriptors.length; i++) {
			Descriptor sourceD = sourceDescriptors[i];
			Descriptor sinkD = sinkDescriptors[i];
			
			int rows = sourceD.getRowCount(r);
			for (int j = 0; j < rows; j++) {
				try {
					if (j == 0) {
						sinkD.addRow(j, true, rPrime);
					} else {
						sinkD.addRow(j-1, false, rPrime);
					}
					fillInFields(r, rPrime, j, sourceD, sourceIndices[i], sinkD, sinkIndices[i]);
				} catch (Exception ex) {
					System.out.println(sinkD.getName());
					System.out.println(rows);
					System.out.println(j);
					ex.printStackTrace();
					throw ex;
				}
			}
		}
		
		return rPrime;
	}

	protected Record createSinkRecord(Record r, Descriptor sourceD, int[] sourceIndices, Descriptor sinkD, int[] sinkIndices)  throws Exception {
		Record rPrime = (Record) sinkD.getHandledClass().newInstance();
		
		// init the values in the nonstacked root record.			
		for (int index = 0; index < sourceIndices.length; index++) {
			String value = sourceD.getValueAsString(r, 0, sourceIndices[index]);
			sinkD.setValue(rPrime, 0, sinkIndices[index], value);
		}
		
		// init the zero arrays.
		for (int i = 1; i < childArrayFields.length; i++) {
			childArrayFields[i].set(rPrime, zeroArrays[i]);
		}
		
		return rPrime;
	}

	protected void fillInFields(Record r, Record rPrime, int row, Descriptor sourceD, int[] sourceIndices, Descriptor sinkD, int[] sinkIndices)  throws Exception {
		for (int index = 0; index < sourceIndices.length; index++) {
			String value = sourceD.getValueAsString(r, row, sourceIndices[index]);
			sinkD.setValue(rPrime, row, sinkIndices[index], value);
		}
	}

}
