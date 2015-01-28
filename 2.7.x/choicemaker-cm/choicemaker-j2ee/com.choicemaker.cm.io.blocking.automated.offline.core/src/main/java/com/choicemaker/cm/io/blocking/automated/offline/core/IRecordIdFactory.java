package com.choicemaker.cm.io.blocking.automated.offline.core;

import com.choicemaker.cm.core.BlockingException;

public interface IRecordIdFactory {

	<T extends Comparable<T>> ImmutableRecordIdTranslator<T> toImmutableTranslator(
			MutableRecordIdTranslator<T> mutableTranslator)
			throws BlockingException;

}