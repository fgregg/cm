package com.choicemaker.e2.mbd.adapter;

import com.choicemaker.e2.mbd.core.runtime.MultiStatus;
import com.choicemaker.eclipse2.core.runtime.CMMultiStatus;
import com.choicemaker.eclipse2.core.runtime.CMStatus;

public class MultiStatusAdapter {

	public static MultiStatus convert(CMMultiStatus o) {
		MultiStatus retVal = null;
		if (o != null) {
			retVal =
				new MultiStatus(o.getPlugin(), o.getCode(),
						StatusAdapter.convert(o.getChildren()), o.getMessage(),
						o.getException());
		}
		return retVal;
	}

	public static MultiStatus[] convert(CMMultiStatus[] o) {
		MultiStatus[] retVal = null;
		if (o != null) {
			retVal = new MultiStatus[o.length];
			for (int i = 0; i < o.length; i++) {
				retVal[i] = convert(o[i]);
			}
		}
		return retVal;
	}

	public static CMMultiStatus convert(MultiStatus o) {
		CMMultiStatus retVal = null;
		if (o != null) {
			retVal = new StdToCM(o);
		}
		return retVal;
	}

	public static CMMultiStatus[] convert(MultiStatus[] o) {
		CMMultiStatus[] retVal = null;
		if (o != null) {
			retVal = new CMMultiStatus[o.length];
			for (int i = 0; i < o.length; i++) {
				retVal[i] = convert(o[i]);
			}
		}
		return retVal;
	}

	protected static class StdToCM implements CMMultiStatus {

		private final MultiStatus delegate;

		public StdToCM(MultiStatus o) {
			if (o == null) {
				throw new IllegalArgumentException("null delegate");
			}
			this.delegate = o;
		}

		public int hashCode() {
			return delegate.hashCode();
		}

		public void add(CMStatus status) {
			delegate.add(StatusAdapter.convert(status));
		}

		public int getCode() {
			return delegate.getCode();
		}

		public Throwable getException() {
			return delegate.getException();
		}

		public void addAll(CMStatus status) {
			delegate.addAll(StatusAdapter.convert(status));
		}

		public String getMessage() {
			return delegate.getMessage();
		}

		public String getPlugin() {
			return delegate.getPlugin();
		}

		public int getSeverity() {
			return delegate.getSeverity();
		}

		public CMStatus[] getChildren() {
			return StatusAdapter.convert(delegate.getChildren());
		}

		public boolean isMultiStatus() {
			return delegate.isMultiStatus();
		}

		public boolean isOK() {
			return delegate.isOK();
		}

		public void merge(CMStatus status) {
			delegate.merge(StatusAdapter.convert(status));
		}

		public boolean matches(int severityMask) {
			return delegate.matches(severityMask);
		}

		public boolean equals(Object obj) {
			return delegate.equals(obj);
		}

		public String toString() {
			return delegate.toString();
		}

	}

}
