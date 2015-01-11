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
package com.choicemaker.cm.transitivity.util;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.transitivity.core.CompositeEntity;
import com.choicemaker.cm.transitivity.core.ConnectedProperty;
import com.choicemaker.cm.transitivity.core.FullyConnectedProperty;
import com.choicemaker.cm.transitivity.core.INode;
import com.choicemaker.cm.transitivity.core.Link;
import com.choicemaker.cm.transitivity.core.MatchEdgeProperty;
import com.choicemaker.cm.transitivity.core.TransitivityResult;
import com.choicemaker.cm.transitivity.core.TransitivityResultSerializer;

/**
 * This object takes a TransitivityResult and a Writer and outputs an xml
 * representation of this TransitivityResult to the Writer.
 * 
 * @author pcheung
 *
 *         ChoiceMaker Technologies, Inc.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class XMLSerializer implements TransitivityResultSerializer {

	private static final long serialVersionUID = 271L;

	protected static final String NEW_LINE = System
			.getProperty("line.separator");

	// Here are the key tag names
	private static final String MATCH_RESULT = "MatchResult";
	private static final String MODEL = "model";
	private static final String DIFF_THRESHOLD = "differThreshold";
	private static final String MATCH_THRESHOLD = "matchThreshold";
	private static final String AGGREGATE = "Aggregate";
	private static final String AGGREGATES = "Aggregates";
	private static final String AGGREGATE_ID = "aggregateId";
	private static final String AGGRE_PROP = "AggregateProperties";
	private static final String CONNECT_COUNT = "ConnectionCount";
	private static final String LOW = "LowThreshold";
	private static final String HIGH = "HighThreshold";
	private static final String CONNECTIONS = "Connections";
	private static final String SIMPLE_CONN = "SimpleConnection";
	private static final String AGGREGATE_CONN = "AggregateConnection";
	private static final String ID = "id";
	private static final String RECORD = "Record";
	private static final String DECISION = "decision";
	private static final String PROB = "probability";

	public XMLSerializer() {
	}

	/**
	 * This method serializes the result to the writer.
	 * 
	 * @param result
	 *            a non-null result (possibly empty)
	 * @param writer
	 *            a non-null writer
	 * @throws IOException
	 */
	public void serialize(TransitivityResult result, Writer writer)
			throws IOException {
		if (result == null || writer == null) {
			throw new IllegalArgumentException("null argument");
		}

		writeHeader(result, writer);
		Iterator it = result.getNodes();
		while (it.hasNext()) {
			StringBuffer sb = new StringBuffer();
			CompositeEntity ce = (CompositeEntity) it.next();
			sb.append(writeCompositeEntity(ce, writer));
			sb.append(NEW_LINE);
			writer.write(sb.toString());
		}
		writeFooter(writer);

		writer.flush();
		writer.close();
	}

	protected void writeHeader(TransitivityResult result, Writer writer)
			throws IOException {
		assert result != null;
		assert writer != null;

		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append(NEW_LINE);
		sb.append('<');
		sb.append(MATCH_RESULT);
		sb.append(' ');
		sb.append(writeAttribute(MODEL, result.getModelName()));
		sb.append(' ');
		sb.append(writeAttribute(DIFF_THRESHOLD, result.getDifferThreshold()));
		sb.append(' ');
		sb.append(writeAttribute(MATCH_THRESHOLD, result.getMatchThreshold()));
		sb.append('>');
		sb.append(NEW_LINE);
		writer.write(sb.toString());
	}

	protected StringBuffer writeCompositeEntity(CompositeEntity ce,
			Writer writer) {
		assert ce != null;
		assert writer != null;

		StringBuffer sb = new StringBuffer();
		sb.append('<');
		sb.append(AGGREGATE);
		sb.append(' ');
		sb.append(writeAttribute(AGGREGATE_ID, ce.getNodeId().toString()));
		sb.append('>');
		sb.append(writeOpenTag(AGGRE_PROP));

		if (isFullyMatchConnected(ce))
			sb.append("<FullyMatchConnected/>");
		else if (isMatchConnected(ce))
			sb.append("<MatchConnected/>");
		else
			sb.append("<HoldConnected/>");

		sb.append(writeConnectionSummary(ce));
		sb.append(writeCloseTag(AGGRE_PROP));

		// add the sub graphs
		List children = ce.getChildren();
		ArrayList composites = new ArrayList(2);
		for (int i = 0; i < children.size(); i++) {
			INode child = (INode) children.get(i);
			if (child instanceof CompositeEntity)
				composites.add(child);
		}
		if (composites.size() > 0)
			sb.append(writeSubGraphs(composites, writer));

		sb.append(writeConnections(ce));
		sb.append(writeCloseTag(AGGREGATE));

		return sb;
	}

	private StringBuffer writeSubGraphs(List composites, Writer writer) {
		StringBuffer sb = new StringBuffer();
		sb.append(writeOpenTag(AGGREGATES));

		for (int i = 0; i < composites.size(); i++) {
			CompositeEntity subGraph = (CompositeEntity) composites.get(i);
			sb.append(writeCompositeEntity(subGraph, writer));
		}

		sb.append(writeCloseTag(AGGREGATES));
		return sb;
	}

	private StringBuffer writeConnections(CompositeEntity ce) {
		StringBuffer sb = new StringBuffer();
		sb.append(writeOpenTag(CONNECTIONS));

		List links = ce.getAllLinks();
		for (int i = 0; i < links.size(); i++) {
			Link link = (Link) links.get(i);
			INode node1 = link.getNode1();
			INode node2 = link.getNode2();
			if (node1 instanceof CompositeEntity
					|| node2 instanceof CompositeEntity) {
				sb.append(writeAggregateConnection(link));
			} else {
				List mrs = link.getLinkDefinition();
				if (mrs.size() > 1)
					throw new IllegalArgumentException(
							"Simple link size > 1.  Link between"
									+ link.getNode1().getNodeId() + " "
									+ link.getNode2().getNodeId());

				MatchRecord2 mr = (MatchRecord2) mrs.get(0);

				sb.append(writeSimpleConnection(mr));
			}
		}

		sb.append(writeCloseTag(CONNECTIONS));
		return sb;
	}

	private StringBuffer writeSimpleConnection(MatchRecord2 mr) {
		StringBuffer sb = new StringBuffer();
		sb.append('<');
		sb.append(SIMPLE_CONN);
		sb.append(' ');
		sb.append(writeAttribute(DECISION, writeDecision(mr)));
		sb.append(' ');
		sb.append(writeAttribute(PROB, mr.getProbability()));
		sb.append('>');

		sb.append(writeRecord(mr.getRecordID1()));
		sb.append(writeRecord(mr.getRecordID2()));

		sb.append(writeCloseTag(SIMPLE_CONN));

		return sb;
	}

	private StringBuffer writeRecord(Comparable c) {
		StringBuffer sb = new StringBuffer();
		sb.append('<');
		sb.append(RECORD);
		sb.append(' ');
		sb.append(writeAttribute(ID, c.toString()));
		sb.append("/>");

		return sb;
	}

	private StringBuffer writeCompositeNode(Comparable c) {
		StringBuffer sb = new StringBuffer();
		sb.append('<');
		sb.append(AGGREGATE);
		sb.append(' ');
		sb.append(writeAttribute(ID, c.toString()));
		sb.append("/>");

		return sb;
	}

	private String writeDecision(MatchRecord2 mr) {
		if (mr.getMatchType() == Decision.MATCH)
			return "match";
		else if (mr.getMatchType() == Decision.HOLD)
			return "hold";
		else
			return "error";
	}

	private StringBuffer writeAggregateConnection(Link link) {
		INode node1 = link.getNode1();
		INode node2 = link.getNode2();

		StringBuffer sb = new StringBuffer();
		sb.append(writeOpenTag(AGGREGATE_CONN));

		if (node1 instanceof CompositeEntity)
			sb.append(writeCompositeNode(node1.getNodeId()));
		else
			sb.append(writeRecord(node1.getNodeId()));

		if (node2 instanceof CompositeEntity)
			sb.append(writeCompositeNode(node2.getNodeId()));
		else
			sb.append(writeRecord(node2.getNodeId()));

		sb.append(writeOpenTag(CONNECTIONS));

		List mrs = link.getLinkDefinition();
		for (int i = 0; i < mrs.size(); i++) {
			MatchRecord2 mr = (MatchRecord2) mrs.get(i);
			sb.append(writeSimpleConnection(mr));
		}

		sb.append(writeCloseTag(CONNECTIONS));

		sb.append(writeCloseTag(AGGREGATE_CONN));
		return sb;
	}

	private String writeOpenTag(String s) {
		return "<" + s + ">";
	}

	private String writeCloseTag(String s) {
		return "</" + s + ">";
	}

	private String writeAttribute(String name, String value) {
		return name + "=\"" + value + "\"";
	}

	private String writeAttribute(String name, float value) {
		return name + "=\"" + value + "\"";
	}

	private StringBuffer writeConnectionSummary(CompositeEntity ce) {
		// now loop through the links
		List links = ce.getAllLinks();
		int count = links.size();
		float low = 2.0f;
		float high = -1.0f;
		for (int i = 0; i < count; i++) {
			Link link = (Link) links.get(i);
			List mrs = link.getLinkDefinition();
			for (int j = 0; j < mrs.size(); j++) {
				MatchRecord2 mr = (MatchRecord2) mrs.get(j);
				float f = mr.getProbability();
				if (f > high)
					high = f;
				if (f < low)
					low = f;
			}
		}

		StringBuffer sb = new StringBuffer();
		sb.append(writeOpenTag(CONNECT_COUNT));
		sb.append(count);
		sb.append(writeCloseTag(CONNECT_COUNT));
		sb.append(writeOpenTag(LOW));
		sb.append(low);
		sb.append(writeCloseTag(LOW));
		sb.append(writeOpenTag(HIGH));
		sb.append(high);
		sb.append(writeCloseTag(HIGH));

		return sb;
	}

	private boolean isFullyMatchConnected(CompositeEntity ce) {
		GraphFilter filter = GraphFilter.getInstance();
		MatchEdgeProperty matchProp = MatchEdgeProperty.getInstance();
		CompositeEntity out = filter.filter(ce, matchProp);

		if (out.getChildren().size() != ce.getChildren().size())
			return false;

		FullyConnectedProperty fully = new FullyConnectedProperty();
		return fully.hasProperty(out);
	}

	private boolean isMatchConnected(CompositeEntity ce) {
		GraphFilter filter = GraphFilter.getInstance();
		MatchEdgeProperty matchProp = MatchEdgeProperty.getInstance();
		CompositeEntity out = filter.filter(ce, matchProp);

		if (out.getChildren().size() != ce.getChildren().size())
			return false;

		ConnectedProperty connected = new ConnectedProperty();
		return connected.hasProperty(out);
	}

	protected void writeFooter(Writer writer) throws IOException {
		assert writer != null;

		StringBuffer sb = new StringBuffer();
		sb.append(writeCloseTag(MATCH_RESULT));
		sb.append(NEW_LINE);
		writer.write(sb.toString());
	}

}
