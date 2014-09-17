package com.choicemaker.e2.std.adapter;

import java.io.File;

import org.eclipse.core.runtime.IPath;

import com.choicemaker.eclipse2.core.runtime.CMPath;

public class PathAdapter {

	public static IPath convert(CMPath o) {
		IPath retVal = null;
		if (o != null) {
			retVal = new CMtoStd(o);
		}
		return retVal;
	}

	public static IPath[] convert(CMPath[] o) {
		IPath[] retVal = null;
		if (o != null) {
			retVal = new IPath[o.length];
			for (int i=0; i<o.length; i++) {
				retVal[i] = convert(o[i]);
			}
		}
		return retVal;
	}
	
	public static CMPath convert(IPath o) {
		CMPath retVal = null;
		if (o != null) {
			retVal = new StdToCM(o);
		}
		return retVal;
	}

	public static CMPath[] convert(IPath[] o) {
		CMPath[] retVal = null;
		if (o != null) {
			retVal = new CMPath[o.length];
			for (int i=0; i<o.length; i++) {
					retVal[i] = convert(o[i]);
			}
		}
		return retVal;
	}
	
	protected static class StdToCM implements CMPath {
		
		private final IPath delegate;

		public StdToCM(IPath o) {
			if (o == null) {
				throw new IllegalArgumentException("null delegate");
			}
			this.delegate = o;
		}

		public CMPath addFileExtension(String extension) {
			return PathAdapter.convert(delegate.addFileExtension(extension));
		}

		public CMPath addTrailingSeparator() {
			return PathAdapter.convert(delegate.addTrailingSeparator());
		}

		public CMPath append(String path) {
			return PathAdapter.convert(delegate.append(path));
		}

		public CMPath append(CMPath path) {
			return PathAdapter.convert(delegate.append(PathAdapter.convert(path)));
		}

		public Object clone() {
			return delegate.clone();
		}

		public boolean equals(Object obj) {
			return delegate.equals(obj);
		}

		public String getDevice() {
			return delegate.getDevice();
		}

		public String getFileExtension() {
			return delegate.getFileExtension();
		}

		public boolean hasTrailingSeparator() {
			return delegate.hasTrailingSeparator();
		}

		public boolean isAbsolute() {
			return delegate.isAbsolute();
		}

		public boolean isEmpty() {
			return delegate.isEmpty();
		}

		public boolean isPrefixOf(CMPath anotherPath) {
			return delegate.isPrefixOf(PathAdapter.convert(anotherPath));
		}

		public boolean isRoot() {
			return delegate.isRoot();
		}

		public boolean isUNC() {
			return delegate.isUNC();
		}

		public boolean isValidPath(String path) {
			return delegate.isValidPath(path);
		}

		public boolean isValidSegment(String segment) {
			return delegate.isValidSegment(segment);
		}

		public String lastSegment() {
			return delegate.lastSegment();
		}

		public CMPath makeAbsolute() {
			return PathAdapter.convert(delegate.makeAbsolute());
		}

		public CMPath makeRelative() {
			return PathAdapter.convert(delegate.makeRelative());
		}

		public CMPath makeUNC(boolean toUNC) {
			return PathAdapter.convert(delegate.makeUNC(toUNC));
		}

		public int matchingFirstSegments(CMPath anotherPath) {
			return delegate.matchingFirstSegments(PathAdapter.convert(anotherPath));
		}

		public CMPath removeFileExtension() {
			return PathAdapter.convert(delegate.removeFileExtension());
		}

		public CMPath removeFirstSegments(int count) {
			return PathAdapter.convert(delegate.removeFirstSegments(count));
		}

		public CMPath removeLastSegments(int count) {
			return PathAdapter.convert(delegate.removeLastSegments(count));
		}

		public CMPath removeTrailingSeparator() {
			return PathAdapter.convert(delegate.removeTrailingSeparator());
		}

		public String segment(int index) {
			return delegate.segment(index);
		}

		public int segmentCount() {
			return delegate.segmentCount();
		}

		public String[] segments() {
			return delegate.segments();
		}

		public CMPath setDevice(String device) {
			return PathAdapter.convert(delegate.setDevice(device));
		}

		public File toFile() {
			return delegate.toFile();
		}

		public String toOSString() {
			return delegate.toOSString();
		}

		public String toString() {
			return delegate.toString();
		}

		public CMPath uptoSegment(int count) {
			return PathAdapter.convert(delegate.uptoSegment(count));
		}

	}

	protected static class CMtoStd implements IPath {
		
		private final CMPath delegate;

		public CMtoStd(CMPath o) {
			if (o == null) {
				throw new IllegalArgumentException("null delegate");
			}
			this.delegate = o;
		}

		public IPath addFileExtension(String extension) {
			return PathAdapter.convert(delegate.addFileExtension(extension));
		}

		public IPath addTrailingSeparator() {
			return PathAdapter.convert(delegate.addTrailingSeparator());
		}

		public IPath append(String path) {
			return PathAdapter.convert(delegate.append(path));
		}

		public IPath append(IPath path) {
			return PathAdapter.convert(delegate.append(PathAdapter.convert(path)));
		}

		public Object clone() {
			return delegate.clone();
		}

		public boolean equals(Object obj) {
			return delegate.equals(obj);
		}

		public String getDevice() {
			return delegate.getDevice();
		}

		public String getFileExtension() {
			return delegate.getFileExtension();
		}

		public boolean hasTrailingSeparator() {
			return delegate.hasTrailingSeparator();
		}

		public boolean isAbsolute() {
			return delegate.isAbsolute();
		}

		public boolean isEmpty() {
			return delegate.isEmpty();
		}

		public boolean isPrefixOf(IPath anotherPath) {
			return delegate.isPrefixOf(PathAdapter.convert(anotherPath));
		}

		public boolean isRoot() {
			return delegate.isRoot();
		}

		public boolean isUNC() {
			return delegate.isUNC();
		}

		public boolean isValidPath(String path) {
			return delegate.isValidPath(path);
		}

		public boolean isValidSegment(String segment) {
			return delegate.isValidSegment(segment);
		}

		public String lastSegment() {
			return delegate.lastSegment();
		}

		public IPath makeAbsolute() {
			return PathAdapter.convert(delegate.makeAbsolute());
		}

		public IPath makeRelative() {
			return PathAdapter.convert(delegate.makeRelative());
		}

		public IPath makeUNC(boolean toUNC) {
			return PathAdapter.convert(delegate.makeUNC(toUNC));
		}

		public int matchingFirstSegments(IPath anotherPath) {
			return delegate.matchingFirstSegments(PathAdapter.convert(anotherPath));
		}

		public IPath removeFileExtension() {
			return PathAdapter.convert(delegate.removeFileExtension());
		}

		public IPath removeFirstSegments(int count) {
			return PathAdapter.convert(delegate.removeFirstSegments(count));
		}

		public IPath removeLastSegments(int count) {
			return PathAdapter.convert(delegate.removeLastSegments(count));
		}

		public IPath removeTrailingSeparator() {
			return PathAdapter.convert(delegate.removeTrailingSeparator());
		}

		public String segment(int index) {
			return delegate.segment(index);
		}

		public int segmentCount() {
			return delegate.segmentCount();
		}

		public String[] segments() {
			return delegate.segments();
		}

		public IPath setDevice(String device) {
			return PathAdapter.convert(delegate.setDevice(device));
		}

		public File toFile() {
			return delegate.toFile();
		}

		public String toOSString() {
			return delegate.toOSString();
		}

		public String toString() {
			return delegate.toString();
		}

		public IPath uptoSegment(int count) {
			return PathAdapter.convert(delegate.uptoSegment(count));
		}

	}

}
