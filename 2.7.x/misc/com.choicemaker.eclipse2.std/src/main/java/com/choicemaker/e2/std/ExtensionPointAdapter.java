package com.choicemaker.e2.std;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;

import com.choicemaker.e2.CMConfigurationElement;
import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.CMExtensionPoint;
import com.choicemaker.e2.CMPluginDescriptor;

public class ExtensionPointAdapter {

	public static IExtensionPoint convert(CMExtensionPoint o) {
		IExtensionPoint retVal = null;
		if (o != null) {
			retVal = new CMtoStd(o);
		}
		return retVal;
	}

	public static IExtensionPoint[] convert(CMExtensionPoint[] o) {
		IExtensionPoint[] retVal = null;
		if (o != null) {
			retVal = new IExtensionPoint[o.length];
			for (int i=0; i<o.length; i++) {
				retVal[i] = convert(o[i]);
			}
		}
		return retVal;
	}
	
	public static CMExtensionPoint convert(IExtensionPoint o) {
		CMExtensionPoint retVal = null;
		if (o != null) {
			retVal = new StdToCM(o);
		}
		return retVal;
	}

	public static CMExtensionPoint[] convert(IExtensionPoint[] o) {
		CMExtensionPoint[] retVal = null;
		if (o != null) {
			retVal = new CMExtensionPoint[o.length];
			for (int i=0; i<o.length; i++) {
					retVal[i] = convert(o[i]);
			}
		}
		return retVal;
	}
	
	protected static class StdToCM implements CMExtensionPoint {
		
		private final IExtensionPoint delegate;

		public StdToCM(IExtensionPoint o) {
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

		public CMExtension getExtension(String extensionId) {
			return ExtensionAdapter.convert(delegate.getExtension(extensionId));
		}

		public CMExtension[] getExtensions() {
			return ExtensionAdapter.convert(delegate.getExtensions());
		}

		public String getLabel() {
			return delegate.getLabel();
		}

		public String getSchemaReference() {
			return delegate.getSchemaReference();
		}

		public String getSimpleIdentifier() {
			return delegate.getSimpleIdentifier();
		}

		public String getUniqueIdentifier() {
			return delegate.getUniqueIdentifier();
		}

	}

	protected static class CMtoStd implements IExtensionPoint {
		
		private final CMExtensionPoint delegate;

		public CMtoStd(CMExtensionPoint o) {
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

		public IExtension getExtension(String extensionId) {
			return ExtensionAdapter.convert(delegate.getExtension(extensionId));
		}

		public IExtension[] getExtensions() {
			return ExtensionAdapter.convert(delegate.getExtensions());
		}

		public String getLabel() {
			return delegate.getLabel();
		}

		public String getSchemaReference() {
			return delegate.getSchemaReference();
		}

		public String getSimpleIdentifier() {
			return delegate.getSimpleIdentifier();
		}

		public String getUniqueIdentifier() {
			return delegate.getUniqueIdentifier();
		}

	}

}
