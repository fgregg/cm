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
package com.choicemaker.e2;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * A checked exception representing a failure.
 * <p>
 * Core exceptions contain a status object describing the cause of the
 * exception.
 * </p>
 *
 * @see CMStatus
 */
public class E2Exception extends Exception {

	private static final long serialVersionUID = 271L;
	
	public static class Status implements CMStatus {

		public static final int SEVERITY_ERROR = CMStatus.ERROR;
		public static final String UNKNOWN_PLUGINID = "UNKNOWN";
		public static final int UNKNOWN_CODE = Integer.MIN_VALUE;
		public static final String UNKNOWN_MESSAGE = "no diagnostic available";
		public static final Throwable UNKNOWN_CAUSE = null;
		
		private final int severity;
		private final String pluginId;
		private final int code;
		private final String message;
		private final Throwable exception;

		public Status() {
			this(UNKNOWN_MESSAGE, UNKNOWN_CAUSE);
		}

		public Status(String message) {
			this(message, UNKNOWN_CAUSE);
		}

		public Status(Throwable x) {
			this(x == null ? UNKNOWN_MESSAGE : x.toString(), x);
		}

		public Status(String message, Throwable exception) {
			this.severity = SEVERITY_ERROR;
			this.pluginId = UNKNOWN_PLUGINID;
			this.code = UNKNOWN_CODE;
			this.message = message;
			this.exception = exception;
		}

		public Status(String pluginId, int code,
				String message, Throwable exception) {
			this.severity = SEVERITY_ERROR;
			this.pluginId = pluginId;
			this.code = code;
			this.message = message;
			this.exception = exception;
		}

		@Override
		public CMStatus[] getChildren() {
			return new CMStatus[0];
		}

		@Override
		public int getCode() {
			return code;
		}

		@Override
		public Throwable getException() {
			return exception;
		}

		@Override
		public String getMessage() {
			return message;
		}

		@Override
		public String getPlugin() {
			return pluginId;
		}

		@Override
		public int getSeverity() {
			return severity;
		}

		@Override
		public boolean isMultiStatus() {
			return false;
		}

		@Override
		public boolean isOK() {
			return false;
		}

		@Override
		public boolean matches(int severityMask) {
			return (severity & severityMask) != 0;
		}
		
	}

	/** Status object. */
	private CMStatus status;

	public E2Exception() {
	}

	public E2Exception(String message) {
	}

	public E2Exception(Throwable cause) {
	}

	public E2Exception(String message, Throwable cause) {
	}

	/**
	 * Creates a new exception with the given status object. The message of the
	 * given status is used as the exception message.
	 *
	 * @param status
	 *            the status object to be associated with this exception
	 */
	public E2Exception(CMStatus status) {
		this(status.getMessage());
		this.status = status;
	}

	/**
	 * Returns the status object for this exception.
	 *
	 * @return a status object
	 */
	public final CMStatus getStatus() {
		return status;
	}

	/**
	 * Prints a stack trace out for the exception, and any nested exception that
	 * it may have embedded in its Status object.
	 */
	public void printStackTrace() {
		printStackTrace(System.err);
	}

	/**
	 * Prints a stack trace out for the exception, and any nested exception that
	 * it may have embedded in its Status object.
	 */
	public void printStackTrace(PrintStream output) {
		synchronized (output) {
			if (status.getException() != null) {
				output.print(getClass().getName()
						+ "[" + status.getCode() + "]: "); //$NON-NLS-1$ //$NON-NLS-2$
				status.getException().printStackTrace(output);
			} else
				super.printStackTrace(output);
		}
	}

	/**
	 * Prints a stack trace out for the exception, and any nested exception that
	 * it may have embedded in its Status object.
	 */
	public void printStackTrace(PrintWriter output) {
		synchronized (output) {
			if (status.getException() != null) {
				output.print(getClass().getName()
						+ "[" + status.getCode() + "]: "); //$NON-NLS-1$ //$NON-NLS-2$
				status.getException().printStackTrace(output);
			} else
				super.printStackTrace(output);
		}
	}

}
