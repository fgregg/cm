/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.internal.plugins;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.model.*;

public class PluginPrerequisite extends PluginPrerequisiteModel implements IPluginPrerequisite {
/**
 * @see CMPluginPrerequisite
 */
public PluginVersionIdentifier getResolvedVersionIdentifier() {
	String version = getResolvedVersion();
	return version == null ? null : new PluginVersionIdentifier(version);
}
/**
 * @see CMPluginPrerequisite
 */
public String getUniqueIdentifier() {
	return getPlugin();
}
/**
 * @see CMPluginPrerequisite
 */
public PluginVersionIdentifier getVersionIdentifier() {
	String version = getVersion();
	return version == null ? null : new PluginVersionIdentifier(version);
}
/**
 * @see CMPluginPrerequisite
 */
public boolean isExported() {
	return getExport();
}
/**
 * @see CMPluginPrerequisite
 */
public boolean isMatchedAsGreaterOrEqual() {
	return getMatchByte() == PREREQ_MATCH_GREATER_OR_EQUAL;
}
/**
 * @see CMPluginPrerequisite
 */
public boolean isMatchedAsCompatible() {
	return (getMatchByte() == PREREQ_MATCH_COMPATIBLE) ||
	        ((getVersionIdentifier() != null) && (getMatchByte() == PREREQ_MATCH_UNSPECIFIED));
}
/**
 * @see CMPluginPrerequisite
 */
public boolean isMatchedAsEquivalent() {
	return getMatchByte() == PREREQ_MATCH_EQUIVALENT;
}
/**
 * @see CMPluginPrerequisite
 */
public boolean isMatchedAsPerfect() {
	return getMatchByte() == PREREQ_MATCH_PERFECT;
}
/**
 * @see CMPluginPrerequisite
 */
public boolean isMatchedAsExact() {
	return isMatchedAsEquivalent();
}
/**
 * @see CMPluginPrerequisite
 */
public boolean isOptional() {
	return getOptional();
}
}
