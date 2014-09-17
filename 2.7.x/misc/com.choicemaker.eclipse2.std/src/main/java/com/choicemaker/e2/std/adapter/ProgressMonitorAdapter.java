package com.choicemaker.e2.std.adapter;

import org.eclipse.core.runtime.IProgressMonitor;

import com.choicemaker.eclipse2.core.runtime.CMProgressMonitor;

public class ProgressMonitorAdapter {

	public static IProgressMonitor convert(CMProgressMonitor o) {
		IProgressMonitor retVal = null;
		if (o != null) {
			retVal = new CMtoStd(o);
		}
		return retVal;
	}

	public static IProgressMonitor[] convert(CMProgressMonitor[] o) {
		IProgressMonitor[] retVal = null;
		if (o != null) {
			retVal = new IProgressMonitor[o.length];
			for (int i=0; i<o.length; i++) {
				retVal[i] = convert(o[i]);
			}
		}
		return retVal;
	}
	
	public static CMProgressMonitor convert(IProgressMonitor o) {
		CMProgressMonitor retVal = null;
		if (o != null) {
			retVal = new StdToCM(o);
		}
		return retVal;
	}

	public static CMProgressMonitor[] convert(IProgressMonitor[] o) {
		CMProgressMonitor[] retVal = null;
		if (o != null) {
			retVal = new CMProgressMonitor[o.length];
			for (int i=0; i<o.length; i++) {
					retVal[i] = convert(o[i]);
			}
		}
		return retVal;
	}
	
	protected static class StdToCM implements CMProgressMonitor {
		
		private final IProgressMonitor delegate;

		public StdToCM(IProgressMonitor o) {
			if (o == null) {
				throw new IllegalArgumentException("null delegate");
			}
			this.delegate = o;
		}

		public void beginTask(String name, int totalWork) {
			delegate.beginTask(name, totalWork);
		}

		public void done() {
			delegate.done();
		}

		public void internalWorked(double work) {
			delegate.internalWorked(work);
		}

		public boolean isCanceled() {
			return delegate.isCanceled();
		}

		public void setCanceled(boolean value) {
			delegate.setCanceled(value);
		}

		public void setTaskName(String name) {
			delegate.setTaskName(name);
		}

		public void subTask(String name) {
			delegate.subTask(name);
		}

		public void worked(int work) {
			delegate.worked(work);
		}

	}

	protected static class CMtoStd implements IProgressMonitor {
		
		private final CMProgressMonitor delegate;

		public CMtoStd(CMProgressMonitor o) {
			if (o == null) {
				throw new IllegalArgumentException("null delegate");
			}
			this.delegate = o;
		}

		public void beginTask(String name, int totalWork) {
			delegate.beginTask(name, totalWork);
		}

		public void done() {
			delegate.done();
		}

		public void internalWorked(double work) {
			delegate.internalWorked(work);
		}

		public boolean isCanceled() {
			return delegate.isCanceled();
		}

		public void setCanceled(boolean value) {
			delegate.setCanceled(value);
		}

		public void setTaskName(String name) {
			delegate.setTaskName(name);
		}

		public void subTask(String name) {
			delegate.subTask(name);
		}

		public void worked(int work) {
			delegate.worked(work);
		}

	}

}
