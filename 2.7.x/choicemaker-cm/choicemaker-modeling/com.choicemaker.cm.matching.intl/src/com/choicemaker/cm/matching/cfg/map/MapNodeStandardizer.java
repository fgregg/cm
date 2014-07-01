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
package com.choicemaker.cm.matching.cfg.map;

import java.util.List;
import java.util.Map;

import com.choicemaker.cm.matching.cfg.ParseTreeNode;
import com.choicemaker.cm.matching.cfg.ParsedData;
import com.choicemaker.cm.matching.cfg.standardizer.DefaultStandardizer;
import com.choicemaker.util.StringUtils;

/**
 * @author ajwinkel
 *
 */
public class MapNodeStandardizer extends DefaultStandardizer {

	private MapStandardizer standardizer;

	public MapNodeStandardizer(String fieldName, Map standards) {
		super(fieldName);
		standardizer = new MapStandardizer(standards);
	}

	public void standardize(ParseTreeNode node, ParsedData holder) {
		List tokens = getTokens(node);
		String[] words = (String[])tokens.toArray(new String[tokens.size()]);
		String joined = StringUtils.join(words);
		
		String ret = standardizer.getStandard(joined);
		if (ret == null) {
			ret = joined;
		}
		
		holder.append(fieldName, ret);
	}
	
	
	
}
