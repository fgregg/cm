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

/**
 * Exception chaining for JDK 1.2 and 1.3. To be replaced by
 * exception chaining in JDK 1.4.
 *
 * @@author    Martin Buechi
 * @@version   $Revision: 1.1 $ $Date: 2010/01/20 15:05:03 $
 */
public class ChainedException extends Exception {
        private Throwable cause = null;

        public ChainedException() {
                super();
        }

        public ChainedException(String message) {
                super(message);
        }

        public ChainedException(String message, Throwable cause) {
                super(message);
                this.cause = cause;
        }

        public Throwable getCause() {
                return cause;
        }

        public void printStackTrace() {
                super.printStackTrace();
                if (cause != null) {
                        System.err.println("Caused by:");
                        cause.printStackTrace();
                }
        }

        public void printStackTrace(java.io.PrintStream ps) {
                super.printStackTrace(ps);
                if (cause != null) {
                        ps.println("Caused by:");
                        cause.printStackTrace(ps);
                }
        }

        public void printStackTrace(java.io.PrintWriter pw) {
                super.printStackTrace(pw);
                if (cause != null) {
                        pw.println("Caused by:");
                        cause.printStackTrace(pw);
                }
        }
}
