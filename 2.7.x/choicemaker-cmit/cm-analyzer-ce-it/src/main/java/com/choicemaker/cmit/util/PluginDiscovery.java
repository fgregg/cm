package com.choicemaker.cmit.util;

import java.util.Iterator;

public final class PluginDiscovery<S> implements Iterable<S> {

//	private static final String PREFIX = "META-INF/services/";

	public Iterator<S> iterator() {
		// ..
		return null;
	}

	public static <S> PluginDiscovery<S> load(Class<S> service,
			ClassLoader loader) {
		// ..
		return null;
	}

	public static <S> PluginDiscovery<S> load(Class<S> service) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		return PluginDiscovery.load(service, cl);
	}

}
