package com.choicemaker.cmit.oaba;

import static org.junit.Assert.fail;

import java.util.concurrent.Callable;

import javax.jms.JMSConsumer;

import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;

/**
 * A Utility class, currently unused, for sending a message to a queue. Useage:
 * 
 * <pre>
 * JMSConsumer = jmsContext.createConsumer(blockQueue);
 * CallableStartData csd = new CallableStartData(consumer, timeOut);
 * Future&lt;StartData&gt; fsd = executor.submit(csd);
 * try {
 * 	retVal = fsd.get();
 * } catch (InterruptedException | ExecutionException x) {
 * 	fail(x.toString());
 * }
 * </pre>
 * 
 * @author rphall
 *
 */
class CallableStartData implements Callable<StartData> {
	private final JMSConsumer consumer;
	private final long timeOut;

	public CallableStartData(JMSConsumer c, long to) {
		if (c == null) {
			throw new IllegalArgumentException("null consumer");
		}
		if (to < 0) {
			throw new IllegalArgumentException("negative timeout: " + to);
		}
		this.consumer = c;
		this.timeOut = to;
	}

	public StartData call() {
		StartData retVal = null;
		try {
			retVal = consumer.receiveBody(StartData.class, timeOut);
		} catch (Exception x) {
			fail(x.toString());
		}
		return retVal;
	}

}