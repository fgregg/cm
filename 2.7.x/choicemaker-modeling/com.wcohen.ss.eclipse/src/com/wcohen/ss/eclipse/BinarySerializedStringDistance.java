/*
 * @(#)$RCSfile: BinarySerializedStringDistance.java,v $        $Revision: 1.1 $ $Date: 2010/03/27 19:41:45 $
 *
 * Copyright (c) 2005 ChoiceMaker Technologies, Inc.
 * 48 Wall Street, 11th floor, New York, NY 10005
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * ChoiceMaker Technologies Inc. ("Confidential Information").
 */
package com.wcohen.ss.eclipse;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;

import com.wcohen.ss.api.StringDistance;
import com.wcohen.ss.api.StringWrapper;

/**
 * @author rphall
 */
class BinarySerializedStringDistance implements StringDistance {

	private String name;
	private URL fileUrl;

	private StringDistance distance;

	public BinarySerializedStringDistance(String name, URL fileUrl) {
		this.name = name;
		this.fileUrl = fileUrl;
		this.distance = null;
		init();
	}

	protected synchronized void init() {
		if (distance == null) {
			InputStream ifs = null;
			try {
				ifs = fileUrl.openStream();
				ObjectInputStream ois = new ObjectInputStream(ifs);
				distance = (StringDistance) ois.readObject();
				ois.close();
				StringDistances.addStringDistance(name, distance);
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				if (ifs != null) {
					try {
						ifs.close();
						ifs = null;
					} catch (IOException x) {
						// TODO better error handling
						x.printStackTrace();
					}
				}
			} // finally
		} // if distance
		
		return;
	} // init()

	public double score(StringWrapper s,StringWrapper t) {
		return this.distance.score(s,t);
	}
	
	public double score(String s, String t) {
		return this.distance.score(s,t);
	}
	
	public StringWrapper prepare(String s) {
		return this.distance.prepare(s);
	}
	
	public String explainScore(StringWrapper s, StringWrapper t) {
		return this.distance.explainScore(s,t);
	}
	
	public String explainScore(String s, String t) {
		return this.explainScore(s,t);
	}

} // BinarySerializedStringDistance

