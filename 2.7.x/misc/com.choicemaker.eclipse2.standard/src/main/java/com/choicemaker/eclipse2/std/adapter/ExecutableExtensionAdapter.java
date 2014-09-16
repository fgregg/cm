package com.choicemaker.eclipse2.std.adapter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;

import com.choicemaker.eclipse2.core.runtime.CMConfigurationElement;
import com.choicemaker.eclipse2.core.runtime.CMCoreException;
import com.choicemaker.eclipse2.core.runtime.CMExecutableExtension;

public class ExecutableExtensionAdapter {
	
	public static IExecutableExtension convert(CMExecutableExtension cmce) {
		IExecutableExtension retVal = null;
		if (cmce != null) {
			retVal = new CMtoStd(cmce);
		}
		return retVal;
	}

	public static IExecutableExtension[] convert(CMExecutableExtension[] cmce) {
		IExecutableExtension[] retVal = null;
		if (cmce != null) {
			retVal = new IExecutableExtension[cmce.length];
			for (int i=0; i<cmce.length; i++) {
				retVal[i] = convert(cmce[i]);
			}
		}
		return retVal;
	}
	
	public static CMExecutableExtension convert(IExecutableExtension ice) {
		CMExecutableExtension retVal = null;
		if (ice != null) {
			retVal = new StdToCM(ice);
		}
		return retVal;
	}

	public static CMExecutableExtension[] convert(IExecutableExtension[] ice) {
		CMExecutableExtension[] retVal = null;
		if (ice != null) {
			retVal = new CMExecutableExtension[ice.length];
			for (int i=0; i<ice.length; i++) {
					retVal[i] = convert(ice[i]);
			}
		}
		return retVal;
	}
	
	protected static class StdToCM implements CMExecutableExtension {
		
		private final IExecutableExtension delegate;

		
		public StdToCM(IExecutableExtension o) {
			if (o == null) {
				throw new IllegalArgumentException("null delegate");
			}
			this.delegate = o;
		}

		public void setInitializationData(CMConfigurationElement config,
				String propertyName, Object data) throws CMCoreException {
			try {
				delegate.setInitializationData(ConfigurationElementAdapter.convert(config), propertyName, data);
			} catch (CoreException e) {
				CMCoreException cmce = CoreExceptionAdapter.convert(e);
				throw cmce;
			}
		}

	}
	
	protected static class CMtoStd implements IExecutableExtension {
		
		private final CMExecutableExtension delegate;

		
		public CMtoStd(CMExecutableExtension o) {
			if (o == null) {
				throw new IllegalArgumentException("null delegate");
			}
			this.delegate = o;
		}

		public void setInitializationData(IConfigurationElement config,
				String propertyName, Object data) throws CoreException {
			try {
				delegate.setInitializationData(ConfigurationElementAdapter.convert(config), propertyName, data);
			} catch (CMCoreException e) {
				CoreException cmce = CoreExceptionAdapter.convert(e);
				throw cmce;
			}
		}

	}

}
