package com.choicemaker.e2.mbd;

import com.choicemaker.e2.CMAdaptable;
import com.choicemaker.e2.mbd.runtime.IAdaptable;

public class AdaptableAdaptor {

	public static IAdaptable convert(CMAdaptable cmce) {
		IAdaptable retVal = null;
		if (cmce != null) {
			retVal = new CMtoStd(cmce);
		}
		return retVal;
	}

	public static IAdaptable[] convert(CMAdaptable[] cmce) {
		IAdaptable[] retVal = null;
		if (cmce != null) {
			retVal = new IAdaptable[cmce.length];
			for (int i = 0; i < cmce.length; i++) {
				retVal[i] = convert(cmce[i]);
			}
		}
		return retVal;
	}

	public static CMAdaptable convert(IAdaptable ice) {
		CMAdaptable retVal = null;
		if (ice != null) {
			retVal = new StdToCM(ice);
		}
		return retVal;
	}

	public static CMAdaptable[] convert(IAdaptable[] ice) {
		CMAdaptable[] retVal = null;
		if (ice != null) {
			retVal = new CMAdaptable[ice.length];
			for (int i = 0; i < ice.length; i++) {
				retVal[i] = convert(ice[i]);
			}
		}
		return retVal;
	}

	protected static class StdToCM implements CMAdaptable {
		
		private final IAdaptable delegate;

		public StdToCM(IAdaptable o) {
			if (o == null) {
				throw new IllegalArgumentException("null delegate");
			}
			this.delegate = o;
		}

		public Object getAdapter(Class<?> adapter) {
			return delegate.getAdapter(adapter);
		}

	}

	protected static class CMtoStd implements IAdaptable {
		
		private final CMAdaptable delegate;

		public CMtoStd(CMAdaptable o) {
			if (o == null) {
				throw new IllegalArgumentException("null delegate");
			}
			this.delegate = o;
		}

		public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
			return delegate.getAdapter(adapter);
		}

	}

}
