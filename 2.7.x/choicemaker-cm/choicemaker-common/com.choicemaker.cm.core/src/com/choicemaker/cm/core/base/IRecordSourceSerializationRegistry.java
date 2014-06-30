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
package com.choicemaker.cm.core.base;

import java.io.NotSerializableException;

import com.choicemaker.cm.core.util.Precondition;

/**
 * A collection of record source serializers.
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 17:00:57 $
 */
public interface IRecordSourceSerializationRegistry {
	
	/** The default <code>priority</code> value */
	public static final int SERIALIZABLE_RECORD_SOURCE_DEFAULT_PRIORITY = 0;

	class PrioritizedSerializer implements Comparable {
		final IRecordSourceSerializer serializer;
		final int priority;
		public PrioritizedSerializer(IRecordSourceSerializer irss) {
			this(irss,SERIALIZABLE_RECORD_SOURCE_DEFAULT_PRIORITY);
		}
		public PrioritizedSerializer(IRecordSourceSerializer irss, int p) {
			Precondition.assertNonNullArgument("null record source serializer", irss);
			this.serializer = irss;
			this.priority = p;
		}
		public int compareTo(Object o) {
			Precondition.assertNonNullArgument("null object",o);
			PrioritizedSerializer p2 = (PrioritizedSerializer) o;
			int retVal = this.priority - p2.priority;
			return retVal;
		}
	}
	
	/** Registers a serializer with the specified priority */
	void registerRecordSourceSerializer(IRecordSourceSerializer serializer, int priority);
	
	/**
	 * Returns all serializers, ordered first by priority and second by the order in which they
	 * were registered; i.e. if two serializers have the same priority, the serializer that was registered
	 * first appears first in the array.
	 */
	public PrioritizedSerializer[] getPrioritizedSerializers();
	
	/**
	 * Tests whether this registry has a serializer
	 * for the specified record source URI
	 */
	boolean hasSerializer(String url);

	/**
	 * Tests whether this registry has a serializer
	 * for the specified record source
	 */
	boolean hasSerializer(RecordSource rs);

	/**
	 * Returns record source serializer for the specified record source.
	 * @param rs
	 * @return a non-null serializer
	 * @throws NotSerializableException if this registry does not have
	 * a serializer for the specified record source
	 * @see #hasSerializer(RecordSource)
	 */
	IRecordSourceSerializer getRecordSourceSerializer(RecordSource rs)
		throws NotSerializableException;

	/**
	 * Returns record source serializer for the specified record source URI
	 * @param recordsourceURI 
	 * @return a non-null serializer
	 * @throws NotSerializableException if this registry does not have
	 * a serializer for the specified URI
	 * @see #hasSerializer(String)
	 */
	IRecordSourceSerializer getRecordSourceSerializer(String recordsourceURI)
		throws NotSerializableException;

}
