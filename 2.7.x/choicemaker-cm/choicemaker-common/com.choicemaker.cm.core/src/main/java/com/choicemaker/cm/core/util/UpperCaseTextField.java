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
package com.choicemaker.cm.core.util;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1 $ $Date: 2010/01/20 15:05:03 $
 */
public class UpperCaseTextField extends JTextField {
 
	 private static final long serialVersionUID = 1L;

	public UpperCaseTextField(int cols) {
		 super(cols);
	 }
 
	 protected Document createDefaultModel() {
		  return new UpperCaseDocument();
	 }
 
	 static class UpperCaseDocument extends PlainDocument {
 
		 private static final long serialVersionUID = 1L;

		public void insertString(int offs, String str, AttributeSet a) 
			  throws BadLocationException {
 
			  if (str == null) {
			  	return;
			  }
			  char[] upper = str.toCharArray();
			  for (int i = 0; i < upper.length; i++) {
			  upper[i] = Character.toUpperCase(upper[i]);
			  }
			  super.insertString(offs, new String(upper), a);
		  }
	 }
	 
	static public class LimitDocument extends PlainDocument {
 
		private static final long serialVersionUID = 1L;
		int limit = -1;

		public LimitDocument(){
			super();
		}
 	
		public LimitDocument(int l)throws IllegalArgumentException { 
			super();
			if (l<0)
				throw new IllegalArgumentException("Negative limit for LimitDocument");
			limit = l;			
		}
 	
		public void insertString(int offs, String str, AttributeSet a) 
			 throws BadLocationException {
 
			 if (str == null) 
			   return;
			 if(limit!=-1 && getLength()+str.length()>limit)
			   return;
			  	
			 char[] upper = str.toCharArray();
			 for (int i = 0; i < upper.length; i++) {
			 	upper[i] = Character.toUpperCase(upper[i]);
			 }
			 super.insertString(offs, new String(upper), a);
		 }
	}

 }
 

 
