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
package com.choicemaker.cm.io.xml.base;

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.base.Match;
import com.choicemaker.cm.core.base.MatchCandidate;
import com.choicemaker.cm.core.base.MatchCandidateFactory;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.2 $ $Date: 2010/03/28 09:18:16 $
 */
public class XmlMatchCandidateFactory extends MatchCandidateFactory {

	public MatchCandidate createMatchCandidate(Match match, IProbabilityModel model) {
		return new XmlMatchCandidate(match, model);
	}
}
