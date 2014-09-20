package com.choicemaker.e2.std;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginDescriptor;

import com.choicemaker.e2.CMConfigurationElement;
import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.CMPluginDescriptor;

public class ExtensionAdapter {

	public static IExtension convert(CMExtension o) {
		IExtension retVal = null;
		if (o != null) {
			retVal = new CMtoStd(o);
		}
		return retVal;
	}

	public static IExtension[] convert(CMExtension[] o) {
		IExtension[] retVal = null;
		if (o != null) {
			retVal = new IExtension[o.length];
			for (int i=0; i<o.length; i++) {
				retVal[i] = convert(o[i]);
			}
		}
		return retVal;
	}
	
	public static CMExtension convert(IExtension o) {
		CMExtension retVal = null;
		if (o != null) {
			retVal = new StdToCM(o);
		}
		return retVal;
	}

	public static CMExtension[] convert(IExtension[] o) {
		CMExtension[] retVal = null;
		if (o != null) {
			retVal = new CMExtension[o.length];
			for (int i=0; i<o.length; i++) {
					retVal[i] = convert(o[i]);
			}
		}
		return retVal;
	}
	
	protected static class StdToCM implements CMExtension {
		
		private final IExtension delegate;

		public StdToCM(IExtension o) {
			if (o == null) {
				throw new IllegalArgumentException("null delegate");
			}
			this.delegate = o;
		}

		public CMConfigurationElement[] getConfigurationElements() {
			return ConfigurationElementAdapter.convert(delegate.getConfigurationElements());
		}

		public CMPluginDescriptor getDeclaringPluginDescriptor() {
			return PluginDescriptorAdapter.convert(delegate.getDeclaringPluginDescriptor());
		}

		public String getExtensionPointUniqueIdentifier() {
			return delegate.getExtensionPointUniqueIdentifier();
		}

		public String getLabel() {
			return delegate.getLabel();
		}

		public String getSimpleIdentifier() {
			return delegate.getSimpleIdentifier();
		}

		public String getUniqueIdentifier() {
			return delegate.getUniqueIdentifier();
		}

	}

	protected static class CMtoStd implements IExtension {
		
		private final CMExtension delegate;

		public CMtoStd(CMExtension o) {
			if (o == null) {
				throw new IllegalArgumentException("null delegate");
			}
			this.delegate = o;
		}

		public IConfigurationElement[] getConfigurationElements() {
			return ConfigurationElementAdapter.convert(delegate.getConfigurationElements());
		}

		public IPluginDescriptor getDeclaringPluginDescriptor() {
			return PluginDescriptorAdapter.convert(delegate.getDeclaringPluginDescriptor());
		}

		public String getExtensionPointUniqueIdentifier() {
			return delegate.getExtensionPointUniqueIdentifier();
		}

		public String getLabel() {
			return delegate.getLabel();
		}

		public String getSimpleIdentifier() {
			return delegate.getSimpleIdentifier();
		}

		public String getUniqueIdentifier() {
			return delegate.getUniqueIdentifier();
		}

	}

}
