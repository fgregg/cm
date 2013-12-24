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
package com.choicemaker.cm.core.configure;

/**
 * A registry for xml-configurable objects.
 * @author rphall
 * @version 1.0.0
 * @since 2.5.206
 */
public interface XmlConfigurablesRegistry {
	
	/**
	 * Retrieves a configurable object
	 * @throws NotFoundException if no configurable was registered under
	 * the specified id
	 */
	XmlConfigurable get(String uniqueId)
		throws NotFoundException;

	/**
	 * Registers a configurable object under an id that must be unique within
	 * the registry.
	 * @throws NotUniqueException if a different configurable is already
	 * registered under the specified id
	 */
	void register(String uniqueId, XmlConfigurable configurable)
		throws NotUniqueException;

	/**
	 * Updates an existing entry in the registry with the specified
	 * configurable object or, if there is not already an existing entry,
	 * registers a new entry .
	 */
	void update(String uniqueId, XmlConfigurable configurable);

	/** Removes a configurable object from the registry */
	void remove(String uniqueId);

}

