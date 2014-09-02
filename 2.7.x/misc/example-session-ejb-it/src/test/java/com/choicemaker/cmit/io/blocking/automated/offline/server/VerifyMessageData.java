package com.choicemaker.cmit.io.blocking.automated.offline.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import com.choicemaker.cm.core.SerialRecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.StartData;

public class VerifyMessageData {

	final long jobID;
	final SerialRecordSource staging;
	final SerialRecordSource master;
	final String stageModelName;
	final String masterModelName;
	final float low;
	final float high;
	final int maxSingle;
	final boolean runTransitivity;

	VerifyMessageData(long jobID, String externalID,
			SerialRecordSource staging, SerialRecordSource master,
			float lowThreshold, float highThreshold, String stageModelName,
			String masterModelName, int maxSingle, boolean runTransitivity) {
		this.jobID = jobID;
		this.staging = staging;
		this.master = master;
		this.low = lowThreshold;
		this.high = highThreshold;
		this.stageModelName = stageModelName;
		this.masterModelName = masterModelName;
		this.maxSingle = maxSingle;
		this.runTransitivity = runTransitivity;
	}

	public void assertExpectedMessage(Message message) {
		try {
			assertTrue("Null message", message != null);
			assertTrue("Wrong message type: "
					+ message.getClass().getSimpleName(),
					message instanceof ObjectMessage);
			ObjectMessage omsg = (ObjectMessage) message;
			assertTrue("Null data type: ", omsg.getObject() != null);
			assertTrue("Wrong data type: "
					+ omsg.getObject().getClass().getSimpleName(),
					omsg.getObject() instanceof StartData);
			StartData data = (StartData) omsg.getObject();
			assertEquals("Wrong jobID: " + data.jobID, this.jobID,
					data.jobID);
			assertEquals("Wrong staging RecordSource", this.staging,
					data.staging);
			assertEquals("Wrong master RecordSource", this.master,
					data.master);
			assertEquals("Wrong differ threshold: " + data.low, this.low,
					data.low, 5 * Math.ulp(this.low));
			assertEquals("Wrong match threshold: " + data.high, this.high,
					data.high, 5 * Math.ulp(this.high));
			assertEquals("Wrong staging model: " + data.stageModelName,
					this.stageModelName, data.stageModelName);
			assertEquals("Wrong master model: " + data.masterModelName,
					this.masterModelName, data.masterModelName);
			assertEquals("Wrong maxSingle: " + data.maxCountSingle,
					this.maxSingle, data.maxCountSingle);
			assertEquals(
					"Wrong transitivity flag: " + data.runTransitivity,
					this.runTransitivity, data.runTransitivity);
		} catch (JMSException e) {
			fail(e.toString());
		}
	}

}