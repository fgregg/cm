package com.choicemaker.e2.mbd;

import com.choicemaker.e2.CMStatus;
import com.choicemaker.e2.mbd.runtime.IStatus;

public class StatusAdapter {

	
	public static CMStatus convert(IStatus ice) {
		CMStatus retVal = null;
		if (ice != null) {
			retVal = new StdToCM(ice);
		}
		return retVal;
	}

	public static CMStatus[] convert(IStatus[] ice) {
		CMStatus[] retVal = null;
		if (ice != null) {
			retVal = new CMStatus[ice.length];
			for (int i=0; i<ice.length; i++) {
				retVal[i] = convert(ice[i]);
			}
		}
		return retVal;
	}

	public static IStatus convert(CMStatus ice) {
		IStatus retVal = null;
		if (ice != null) {
			retVal = new CMtoStd(ice);
		}
		return retVal;
	}

	public static IStatus[] convert(CMStatus[] ice) {
		IStatus[] retVal = null;
		if (ice != null) {
			retVal = new IStatus[ice.length];
			for (int i=0; i<ice.length; i++) {
				retVal[i] = convert(ice[i]);
			}
		}
		return retVal;
	}

	protected static class StdToCM implements CMStatus {
		
		private final IStatus delegate;

		public StdToCM(IStatus o) {
			if (o == null) {
				throw new IllegalArgumentException("null delegate");
			}
			this.delegate = o;
		}

		public CMStatus[] getChildren() {
			return convert(delegate.getChildren());
		}

		public int getCode() {
			return delegate.getCode();
		}

		public Throwable getException() {
			return delegate.getException();
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

		public boolean isMultiStatus() {
			return delegate.isMultiStatus();
		}

		public boolean isOK() {
			return delegate.isOK();
		}

		public boolean matches(int severityMask) {
			return delegate.matches(severityMask);
		}

	}

	protected static class CMtoStd implements IStatus {
		
		private final CMStatus delegate;

		public CMtoStd(CMStatus o) {
			if (o == null) {
				throw new IllegalArgumentException("null delegate");
			}
			this.delegate = o;
		}

		public IStatus[] getChildren() {
			return convert(delegate.getChildren());
		}

		public int getCode() {
			return delegate.getCode();
		}

		public Throwable getException() {
			return delegate.getException();
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

		public boolean isMultiStatus() {
			return delegate.isMultiStatus();
		}

		public boolean isOK() {
			return delegate.isOK();
		}

		public boolean matches(int severityMask) {
			return delegate.matches(severityMask);
		}

	}

}
