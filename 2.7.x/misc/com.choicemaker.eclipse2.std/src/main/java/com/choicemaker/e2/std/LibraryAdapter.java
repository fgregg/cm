package com.choicemaker.e2.std;

import org.eclipse.core.runtime.ILibrary;
import org.eclipse.core.runtime.IPath;

import com.choicemaker.e2.CMLibrary;
import com.choicemaker.e2.CMPath;

public class LibraryAdapter {

	public static ILibrary convert(CMLibrary o) {
		ILibrary retVal = null;
		if (o != null) {
			retVal = new CMtoStd(o);
		}
		return retVal;
	}

	public static ILibrary[] convert(CMLibrary[] o) {
		ILibrary[] retVal = null;
		if (o != null) {
			retVal = new ILibrary[o.length];
			for (int i=0; i<o.length; i++) {
				retVal[i] = convert(o[i]);
			}
		}
		return retVal;
	}
	
	public static CMLibrary convert(ILibrary o) {
		CMLibrary retVal = null;
		if (o != null) {
			retVal = new StdToCM(o);
		}
		return retVal;
	}

	public static CMLibrary[] convert(ILibrary[] o) {
		CMLibrary[] retVal = null;
		if (o != null) {
			retVal = new CMLibrary[o.length];
			for (int i=0; i<o.length; i++) {
					retVal[i] = convert(o[i]);
			}
		}
		return retVal;
	}
	
	protected static class StdToCM implements CMLibrary {
		
		private final ILibrary delegate;

		public StdToCM(ILibrary o) {
			if (o == null) {
				throw new IllegalArgumentException("null delegate");
			}
			this.delegate = o;
		}

		public String[] getContentFilters() {
			return delegate.getContentFilters();
		}

		public CMPath getPath() {
			return PathAdapter.convert(delegate.getPath());
		}

		public String getType() {
			return delegate.getType();
		}

		public boolean isExported() {
			return delegate.isExported();
		}

		public boolean isFullyExported() {
			return delegate.isFullyExported();
		}

		public String[] getPackagePrefixes() {
			return delegate.getPackagePrefixes();
		}

	}

	protected static class CMtoStd implements ILibrary {
		
		private final CMLibrary delegate;

		public CMtoStd(CMLibrary o) {
			if (o == null) {
				throw new IllegalArgumentException("null delegate");
			}
			this.delegate = o;
		}

		public String[] getContentFilters() {
			return delegate.getContentFilters();
		}

		public IPath getPath() {
			return PathAdapter.convert(delegate.getPath());
		}

		public String getType() {
			return delegate.getType();
		}

		public boolean isExported() {
			return delegate.isExported();
		}

		public boolean isFullyExported() {
			return delegate.isFullyExported();
		}

		public String[] getPackagePrefixes() {
			return delegate.getPackagePrefixes();
		}

	}

}
