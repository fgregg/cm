package com.choicemaker.e2.mbd.adapter;

import com.choicemaker.e2.mbd.core.runtime.PluginVersionIdentifier;
import com.choicemaker.eclipse2.core.runtime.CMPluginVersionIdentifier;

public class PluginVersionIdentifierAdapter {

	public static PluginVersionIdentifier convert(CMPluginVersionIdentifier cmce) {
		PluginVersionIdentifier retVal = null;
		if (cmce != null) {
			retVal = new PluginVersionIdentifier(cmce.getMajorComponent(), cmce.getMinorComponent(), cmce.getServiceComponent(), cmce.getQualifierComponent());
		}
		return retVal;
	}

	public static PluginVersionIdentifier[] convert(CMPluginVersionIdentifier[] cmce) {
		PluginVersionIdentifier[] retVal = null;
		if (cmce != null) {
			retVal = new PluginVersionIdentifier[cmce.length];
			for (int i=0; i<cmce.length; i++) {
				retVal[i] = convert(cmce[i]);
			}
		}
		return retVal;
	}
	
	public static CMPluginVersionIdentifier convert(PluginVersionIdentifier ice) {
		CMPluginVersionIdentifier retVal = null;
		if (ice != null) {
			retVal = new StdToCM(ice);
		}
		return retVal;
	}

	public static CMPluginVersionIdentifier[] convert(PluginVersionIdentifier[] ice) {
		CMPluginVersionIdentifier[] retVal = null;
		if (ice != null) {
			retVal = new CMPluginVersionIdentifier[ice.length];
			for (int i=0; i<ice.length; i++) {
					retVal[i] = convert(ice[i]);
			}
		}
		return retVal;
	}
	
	protected static class StdToCM implements CMPluginVersionIdentifier {
		
		private final PluginVersionIdentifier delegate;

		public StdToCM(PluginVersionIdentifier o) {
			if (o == null) {
				throw new IllegalArgumentException("null delegate");
			}
			this.delegate = o;
		}

		public int getMajorComponent() {
			return delegate.getMajorComponent();
		}

		public int getMinorComponent() {
			return delegate.getMinorComponent();
		}

		public int getServiceComponent() {
			return delegate.getServiceComponent();
		}

		public String getQualifierComponent() {
			return delegate.getQualifierComponent();
		}

		public boolean isGreaterOrEqualTo(CMPluginVersionIdentifier id) {
			return delegate.isGreaterOrEqualTo(convert(id));
		}

		public boolean isCompatibleWith(CMPluginVersionIdentifier id) {
			return delegate.isCompatibleWith(convert(id));
		}

		public boolean isEquivalentTo(CMPluginVersionIdentifier id) {
			return delegate.isEquivalentTo(convert(id));
		}

		public boolean isPerfect(CMPluginVersionIdentifier id) {
			return delegate.isPerfect(convert(id));
		}

		public boolean isGreaterThan(CMPluginVersionIdentifier id) {
			return delegate.isGreaterThan(convert(id));
		}

	}

}
