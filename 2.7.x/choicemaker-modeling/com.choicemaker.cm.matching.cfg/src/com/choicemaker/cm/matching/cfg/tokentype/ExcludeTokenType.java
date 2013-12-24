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
package com.choicemaker.cm.matching.cfg.tokentype;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author    Adam Winkel
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:59 $
 */
public class ExcludeTokenType extends WordTokenType {

	protected Set exclude;

	public ExcludeTokenType(String name) {
		this(name, null);
	}

	public ExcludeTokenType(String name, Set exclude) {
		super(name);
	}

	public void setExcludes(Set e) {
		if (e == null || e.size() == 0) {
			this.exclude = null;
		} else {
			this.exclude = new HashSet(e);
		}
	}
	
	public void addExcludes(Set e) {
		if (e == null || e.size() == 0) {
			return;
		} 
		
		if (this.exclude == null) {
			this.exclude = new HashSet(e);
		} else {
			this.exclude.addAll(e);
		}
	}
	
	public boolean canHaveToken(String token) {
		if (exclude != null) {
			return super.canHaveToken(token) && !exclude.contains(token);
		}
		
		return true;
	}
	
}
