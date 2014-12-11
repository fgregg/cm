package com.choicemaker.cmit.oaba.failed_experiments;

import static com.choicemaker.cmit.utils.JmsUtils.SHORT_TIMEOUT_MILLIS;
import static com.choicemaker.cmit.utils.JmsUtils.VERY_LONG_TIMEOUT_MILLIS;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;

import javax.jms.JMSConsumer;

/**
 * A Utility class for receiving a message from a queue after a long delay.
 * 
 * @author rphall
 *
 */
class CallableMessage<T> implements Callable<T> {

	private final Class<T> cls;
	private final JMSConsumer consumer;
	private final long shortTimeOut;
	private final long longTimeOut;

	public CallableMessage(Class<T> cls, JMSConsumer c) {
		this(cls, c, SHORT_TIMEOUT_MILLIS, VERY_LONG_TIMEOUT_MILLIS);
	}

	public CallableMessage(Class<T> cls, JMSConsumer c, long lto) {
		this(cls, c, SHORT_TIMEOUT_MILLIS, lto);
	}

	public CallableMessage(Class<T> cls, JMSConsumer c, long sto, long lto) {
		if (cls == null) {
			throw new IllegalArgumentException("null class");
		}
		if (c == null) {
			throw new IllegalArgumentException("null consumer");
		}
		if (lto < 0) {
			throw new IllegalArgumentException("negative long timeout: " + lto);
		}
		if (sto < 0) {
			throw new IllegalArgumentException("non-postive short timeout: "
					+ lto);
		}
		this.cls = cls;
		this.consumer = c;
		this.shortTimeOut = sto;
		this.longTimeOut = lto;
	}

	public T call() {
		T retVal = null;
		long count = longTimeOut / shortTimeOut;
		try {
			retVal = null;
			do {
				retVal = consumer.receiveBody(cls, longTimeOut);
				--count;
			} while (retVal == null && count > 0);
		} catch (Exception x) {
			fail(x.toString());
		}
		return retVal;
	}

}
