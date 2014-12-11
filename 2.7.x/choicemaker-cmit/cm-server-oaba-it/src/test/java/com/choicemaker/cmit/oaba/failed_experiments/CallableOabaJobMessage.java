package com.choicemaker.cmit.oaba.failed_experiments;

import javax.jms.JMSConsumer;

import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;

/**
 * A Utility class for receiving an OABA job message from a queue after a long
 * delay. Usage:
 * 
 * <pre>
 * JMSConsumer = jmsContext.createConsumer(blockQueue);
 * CallableMessage csd = new CallableMessage(consumer, longTimeOut);
 * Future&lt;OabaJobMessage&gt; fsd = executor.submit(csd);
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
public class CallableOabaJobMessage extends CallableMessage<OabaJobMessage> {

	public CallableOabaJobMessage(JMSConsumer c) {
		super(OabaJobMessage.class, c);
	}

	public CallableOabaJobMessage(JMSConsumer c, long lto) {
		super(OabaJobMessage.class, c);
	}

	public CallableOabaJobMessage(JMSConsumer c, long sto, long lto) {
		super(OabaJobMessage.class, c, sto, lto);
	}

}
