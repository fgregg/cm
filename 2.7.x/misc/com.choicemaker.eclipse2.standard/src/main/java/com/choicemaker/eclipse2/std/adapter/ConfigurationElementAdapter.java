package com.choicemaker.eclipse2.std.adapter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;

import com.choicemaker.eclipse2.core.runtime.CMConfigurationElement;
import com.choicemaker.eclipse2.core.runtime.CMCoreException;
import com.choicemaker.eclipse2.core.runtime.CMExtension;

public class ConfigurationElementAdapter {

	public static IConfigurationElement convert(CMConfigurationElement cmce) {
		IConfigurationElement retVal = null;
		if (cmce != null) {
			retVal = new CMtoStd(cmce);
		}
		return retVal;
	}

	public static IConfigurationElement[] convert(CMConfigurationElement[] cmce) {
		IConfigurationElement[] retVal = null;
		if (cmce != null) {
			retVal = new IConfigurationElement[cmce.length];
			for (int i = 0; i < cmce.length; i++) {
				retVal[i] = convert(cmce[i]);
			}
		}
		return retVal;
	}

	public static CMConfigurationElement convert(IConfigurationElement ice) {
		CMConfigurationElement retVal = null;
		if (ice != null) {
			retVal = new StdToCM(ice);
		}
		return retVal;
	}

	public static CMConfigurationElement[] convert(IConfigurationElement[] ice) {
		CMConfigurationElement[] retVal = null;
		if (ice != null) {
			retVal = new CMConfigurationElement[ice.length];
			for (int i = 0; i < ice.length; i++) {
				retVal[i] = convert(ice[i]);
			}
		}
		return retVal;
	}

	protected static class StdToCM implements CMConfigurationElement {

		private final IConfigurationElement delegate;

		public StdToCM(IConfigurationElement o) {
			if (o == null) {
				throw new IllegalArgumentException("null delegate");
			}
			this.delegate = o;
		}

		public Object createExecutableExtension(String propertyName)
				throws CMCoreException {
			try {
				return delegate.createExecutableExtension(propertyName);
			} catch (CoreException e) {
				CMCoreException cmce = CoreExceptionAdapter.convert(e);
				throw cmce;
			}
		}

		public String getAttribute(String name) {
			return delegate.getAttribute(name);
		}

		public String getAttributeAsIs(String name) {
			return delegate.getAttributeAsIs(name);
		}

		public String[] getAttributeNames() {
			return delegate.getAttributeNames();
		}

		public CMConfigurationElement[] getChildren() {
			return convert(delegate.getChildren());
		}

		public CMConfigurationElement[] getChildren(String name) {
			return convert(delegate.getChildren(name));
		}

		public CMExtension getDeclaringExtension() {
			return ExtensionAdapter.convert(delegate.getDeclaringExtension());
		}

		public String getName() {
			return delegate.getName();
		}

		public String getValue() {
			return delegate.getValue();
		}

		public String getValueAsIs() {
			return delegate.getValueAsIs();
		}

	}

	protected static class CMtoStd implements IConfigurationElement {

		private final CMConfigurationElement delegate;

		public CMtoStd(CMConfigurationElement o) {
			if (o == null) {
				throw new IllegalArgumentException("null delegate");
			}
			this.delegate = o;
		}

		public Object createExecutableExtension(String propertyName)
				throws CoreException {
			try {
				return delegate.createExecutableExtension(propertyName);
			} catch (CMCoreException e) {
				CoreException ce = CoreExceptionAdapter.convert(e);
				throw ce;
			}
		}

		public String getAttribute(String name) {
			return delegate.getAttribute(name);
		}

		public String getAttributeAsIs(String name) {
			return delegate.getAttributeAsIs(name);
		}

		public String[] getAttributeNames() {
			return delegate.getAttributeNames();
		}

		public IConfigurationElement[] getChildren() {
			return convert(delegate.getChildren());
		}

		public IConfigurationElement[] getChildren(String name) {
			return convert(delegate.getChildren(name));
		}

		public IExtension getDeclaringExtension() {
			return ExtensionAdapter.convert(delegate.getDeclaringExtension());
		}

		public String getName() {
			return delegate.getName();
		}

		public String getValue() {
			return delegate.getValue();
		}

		public String getValueAsIs() {
			return delegate.getValueAsIs();
		}

	}

	private ConfigurationElementAdapter() {
	}

}
