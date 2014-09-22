package com.choicemaker.e2.mbd;

import java.net.URL;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.choicemaker.e2.E2Exception;
import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.CMExtensionPoint;
import com.choicemaker.e2.CMLibrary;
import com.choicemaker.e2.CMPath;
import com.choicemaker.e2.CMPlugin;
import com.choicemaker.e2.CMPluginDescriptor;
import com.choicemaker.e2.CMPluginPrerequisite;
import com.choicemaker.e2.CMPluginVersionIdentifier;
import com.choicemaker.e2.mbd.runtime.CoreException;
import com.choicemaker.e2.mbd.runtime.IExtension;
import com.choicemaker.e2.mbd.runtime.IExtensionPoint;
import com.choicemaker.e2.mbd.runtime.ILibrary;
import com.choicemaker.e2.mbd.runtime.IPath;
import com.choicemaker.e2.mbd.runtime.IPluginDescriptor;
import com.choicemaker.e2.mbd.runtime.IPluginPrerequisite;
import com.choicemaker.e2.mbd.runtime.Plugin;
import com.choicemaker.e2.mbd.runtime.PluginVersionIdentifier;

public class PluginDescriptorAdapter {

	public static IPluginDescriptor convert(CMPluginDescriptor o) {
		IPluginDescriptor retVal = null;
		if (o != null) {
			retVal = new CMtoStd(o);
		}
		return retVal;
	}

	public static IPluginDescriptor[] convert(CMPluginDescriptor[] o) {
		IPluginDescriptor[] retVal = null;
		if (o != null) {
			retVal = new IPluginDescriptor[o.length];
			for (int i=0; i<o.length; i++) {
				retVal[i] = convert(o[i]);
			}
		}
		return retVal;
	}
	
	public static CMPluginDescriptor convert(IPluginDescriptor o) {
		CMPluginDescriptor retVal = null;
		if (o != null) {
			retVal = new StdToCM(o);
		}
		return retVal;
	}

	public static CMPluginDescriptor[] convert(IPluginDescriptor[] o) {
		CMPluginDescriptor[] retVal = null;
		if (o != null) {
			retVal = new CMPluginDescriptor[o.length];
			for (int i=0; i<o.length; i++) {
					retVal[i] = convert(o[i]);
			}
		}
		return retVal;
	}
	
	protected static class StdToCM implements CMPluginDescriptor {
		
		private final IPluginDescriptor delegate;

		public StdToCM(IPluginDescriptor o) {
			if (o == null) {
				throw new IllegalArgumentException("null delegate");
			}
			this.delegate = o;
		}

		public CMExtension getExtension(String extensionName) {
			return ExtensionAdapter.convert(delegate.getExtension(extensionName));
		}

		public CMExtensionPoint getExtensionPoint(String extensionPointId) {
			return ExtensionPointAdapter.convert(delegate.getExtensionPoint(extensionPointId));
		}

		public CMExtensionPoint[] getExtensionPoints() {
			return ExtensionPointAdapter.convert(delegate.getExtensionPoints());
		}

		public CMExtension[] getExtensions() {
			return ExtensionAdapter.convert(delegate.getExtensions());
		}

		public URL getInstallURL() {
			return delegate.getInstallURL();
		}

		public String getLabel() {
			return delegate.getLabel();
		}

		public CMPlugin getPlugin() throws E2Exception {
			try {
				return PluginAdapter.convert(delegate.getPlugin());
			} catch (CoreException e) {
				E2Exception ce = CoreExceptionAdapter.convert(e);
				throw ce;
			}
		}

		public ClassLoader getPluginClassLoader() {
			return delegate.getPluginClassLoader();
		}

		public CMPluginPrerequisite[] getPluginPrerequisites() {
			return PluginPrerequisiteAdapter.convert(delegate.getPluginPrerequisites());
		}

		public String getProviderName() {
			return delegate.getProviderName();
		}

		public ResourceBundle getResourceBundle()
				throws MissingResourceException {
			return delegate.getResourceBundle();
		}

		public String getResourceString(String value) {
			return delegate.getResourceString(value);
		}

		public String getResourceString(String value, ResourceBundle bundle) {
			return delegate.getResourceString(value, bundle);
		}

		public CMLibrary[] getRuntimeLibraries() {
			throw new Error("not implemented");
		}

		public String getUniqueIdentifier() {
			return delegate.getUniqueIdentifier();
		}

		public CMPluginVersionIdentifier getVersionIdentifier() {
			return PluginVersionIdentifierAdapter.convert(delegate.getVersionIdentifier());
		}

		public boolean isPluginActivated() {
			return delegate.isPluginActivated();
		}

		public URL find(CMPath path) {
			throw new Error("not implemented");
		}

		public URL find(CMPath path, @SuppressWarnings("rawtypes") Map override) {
			throw new Error("not implemented");
		}

	}

	protected static class CMtoStd implements IPluginDescriptor {
		
		private final CMPluginDescriptor delegate;

		public CMtoStd(CMPluginDescriptor o) {
			if (o == null) {
				throw new IllegalArgumentException("null delegate");
			}
			this.delegate = o;
		}

		public IExtension getExtension(String extensionName) {
			return ExtensionAdapter.convert(delegate.getExtension(extensionName));
		}

		public IExtensionPoint getExtensionPoint(String extensionPointId) {
			return ExtensionPointAdapter.convert(delegate.getExtensionPoint(extensionPointId));
		}

		public IExtensionPoint[] getExtensionPoints() {
			return ExtensionPointAdapter.convert(delegate.getExtensionPoints());
		}

		public IExtension[] getExtensions() {
			return ExtensionAdapter.convert(delegate.getExtensions());
		}

		public URL getInstallURL() {
			return delegate.getInstallURL();
		}

		public String getLabel() {
			return delegate.getLabel();
		}

		public Plugin getPlugin() throws CoreException {
			try {
				return PluginAdapter.convert(delegate.getPlugin());
			} catch (E2Exception e) {
				CoreException ce = CoreExceptionAdapter.convert(e);
				throw ce;
			}
		}

		public ClassLoader getPluginClassLoader() {
			return delegate.getPluginClassLoader();
		}

		public IPluginPrerequisite[] getPluginPrerequisites() {
			return PluginPrerequisiteAdapter.convert(delegate.getPluginPrerequisites());
		}

		public String getProviderName() {
			return delegate.getProviderName();
		}

		public ResourceBundle getResourceBundle()
				throws MissingResourceException {
			return delegate.getResourceBundle();
		}

		public String getResourceString(String value) {
			return delegate.getResourceString(value);
		}

		public String getResourceString(String value, ResourceBundle bundle) {
			return delegate.getResourceString(value, bundle);
		}

		public ILibrary[] getRuntimeLibraries() {
			throw new Error("not implemented");
		}

		public String getUniqueIdentifier() {
			return delegate.getUniqueIdentifier();
		}

		public PluginVersionIdentifier getVersionIdentifier() {
			return PluginVersionIdentifierAdapter.convert(delegate.getVersionIdentifier());
		}

		public boolean isPluginActivated() {
			return delegate.isPluginActivated();
		}

		public URL find(IPath path) {
			throw new Error("not implemented");
		}

		public URL find(IPath path, @SuppressWarnings("rawtypes") Map override) {
			throw new Error("not implemented");
		}

	}

}
