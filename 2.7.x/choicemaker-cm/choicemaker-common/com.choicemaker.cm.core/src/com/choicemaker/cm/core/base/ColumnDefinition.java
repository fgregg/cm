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
package com.choicemaker.cm.core.base;

import java.io.Serializable;

/**
 * Field (column) for display in the human review panel of ModelMaker. 
 *
 * @author   Martin Buechi
 * @author   S. Yoakum-Stover
 * @version  $Revision: 1.2 $ $Date: 2010/03/27 21:29:59 $
 */
public class ColumnDefinition implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = -8402115126099625953L;

	/** Name displayed in column header */
	private String name;
	/** Name of the field (variable) */
	private String fieldName;
	/** Width in pixels in default layout */
	private int width;
	/** Alignment SwingConstants.LEFT/CENTER/RIGHT */
	private int alignment;
	
	/**
	 * @deprecated ONLY USED TO MAKE THE CLASS SERIALIZABLE!
	 */
	public ColumnDefinition(){
	}

	/**
	 * Constructs a <code>ColumnDefinition</code> instance.
	 *
	 * @param  name  Name displayed in column header.
	 * @param  fieldName  Name of the field (variable).
	 * @param  width  Width in pixels in default layout.
	 * @param  alignment  Alignment SwingConstants.LEFT/CENTER/RIGHT
	 */
	public ColumnDefinition(String name, String fieldName, int width, int alignment) {
		this.name = name;
		this.fieldName = fieldName;
		this.width = width;
		this.alignment = alignment;
	}

	/**
	 * Constructs a <code>ColumnDefinition</code> instance.
	 *
	 * @param  name  Name displayed in column header.
	 * @param  width  Width in pixels in default layout.
	 * @param  alignment  The alignment (Swing constant).
	 */
	public ColumnDefinition(String name, int width, int alignment) {
		this(name, name, width, alignment);
	}

	/**
	 * Returns the name displayed in the column header.
	 *
	 * @return   The name displayed in the column header.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the name of the field.
	 *
	 * @return   The name of the field.
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * Returns the width in pixels in the default layout.
	 *
	 * @return  The width in pixels of the default layout.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Returns the alignment, e.g., SwingConstants.LEFT/CENTER/RIGHT.
	 *
	 * @return  The alignment, e.g., SwingConstants.LEFT/CENTER/RIGHT.
	 */
	public int getAlignment() {
		return alignment;
	}
}
