package com.choicemaker.e2.std;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Plugin;

import com.choicemaker.e2.E2Exception;
import com.choicemaker.e2.CMPlugin;
import com.choicemaker.e2.CMPluginDescriptor;

public class PluginAdapter {

	public static Plugin convert(CMPlugin o) {
		Plugin retVal = null;
		if (o != null) {
			retVal = new CMtoStd(o);
		}
		return retVal;
	}

	public static Plugin[] convert(CMPlugin[] o) {
		Plugin[] retVal = null;
		if (o != null) {
			retVal = new Plugin[o.length];
			for (int i=0; i<o.length; i++) {
				retVal[i] = convert(o[i]);
			}
		}
		return retVal;
	}
	
	public static CMPlugin convert(Plugin o) {
		CMPlugin retVal = null;
		if (o != null) {
			retVal = new StdToCM(o);
		}
		return retVal;
	}

	public static CMPlugin[] convert(Plugin[] o) {
		CMPlugin[] retVal = null;
		if (o != null) {
			retVal = new CMPlugin[o.length];
			for (int i=0; i<o.length; i++) {
					retVal[i] = convert(o[i]);
			}
		}
		return retVal;
	}
	
	protected static class StdToCM implements CMPlugin {
		
		private final Plugin delegate;

		public StdToCM(Plugin o) {
			if (o == null) {
				throw new IllegalArgumentException("null delegate");
			}
			this.delegate = o;
		}

		public CMPluginDescriptor getDescriptor() {
			return PluginDescriptorAdapter.convert(delegate.getDescriptor());
		}

		public boolean isDebugging() {
			return delegate.isDebugging();
		}

		public void setDebugging(boolean value) {
			delegate.setDebugging(value);
		}

		public void shutdown() throws E2Exception {
			try {
				delegate.shutdown();
			} catch (CoreException e) {
				E2Exception cmce = CoreExceptionAdapter.convert(e);
				throw cmce;
			}
		}

		public void startup() throws E2Exception {
			try {
				delegate.startup();
			} catch (CoreException e) {
				E2Exception cmce = CoreExceptionAdapter.convert(e);
				throw cmce;
			}
		}

	}

	protected static class CMtoStd extends Plugin {
		
		public CMtoStd(CMPlugin o) {
			super(PluginDescriptorAdapter.convert(o.getDescriptor()));
		}

	}
	
}
