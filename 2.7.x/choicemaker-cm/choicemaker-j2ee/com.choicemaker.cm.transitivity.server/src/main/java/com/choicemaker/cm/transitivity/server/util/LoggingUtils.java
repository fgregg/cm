package com.choicemaker.cm.transitivity.server.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.batch.BatchJob;

public class LoggingUtils {

	private LoggingUtils() {
	}

	public static String buildDiagnostic(String msg, BatchJob batchJob,
			TransitivityParameters oabaParams, OabaSettings oabaSettings,
			ServerConfiguration serverConfig) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		if (msg != null && !msg.isEmpty()) {
			pw.println(msg);
		}
		pw.println("BatchJob: " + batchJob);
		pw.println("OabaParameters: " + oabaParams);
		pw.println("OabaSettings: " + oabaSettings);
		pw.println("ServerConfiguration: " + serverConfig);
		String retVal = sw.toString();
		return retVal;
	}

}
