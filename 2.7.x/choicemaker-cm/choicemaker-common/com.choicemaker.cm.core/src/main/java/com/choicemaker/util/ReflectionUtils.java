package com.choicemaker.util;

import java.lang.reflect.Method;
import java.util.Random;
import java.util.UUID;

public class ReflectionUtils {

	private static final Random random = new Random();

	public static final String GET = "get";
	public static final String IS = "is";
	public static final String SET = "set";

	private ReflectionUtils() {
	}

	/**
	 * Copied from org.junit.Assert to avoid JUnit dependence in production code
	 */
	static void assertTrue(boolean condition) {
		assertTrue(null, condition);
	}

	/**
	 * Copied from org.junit.Assert to avoid JUnit dependence in production code
	 */
	static void assertTrue(String message, boolean condition) {
		if (!condition) {
			fail(message);
		}
	}

	/**
	 * Copied from org.junit.Assert to avoid JUnit dependence in production code
	 */
	static void fail(String message) {
		if (message == null) {
			throw new AssertionError();
		}
		throw new AssertionError(message);
	}

	static void fail(String context, Class c, Class p, String pn, Exception x) {
		StringBuilder sb = new StringBuilder();
		sb.append("Context: ").append(context);
		sb.append(", tClass: ").append(c == null ? "null" : c.toString());
		sb.append(", pClass: ").append(p == null ? "null" : p.getName());
		sb.append(", pName: ").append(pn == null ? "null" : pn);
		sb.append(", Exception: ").append(x == null ? "null" : x.toString());
		String msg = sb.toString();
		fail(msg);
	}

	static void fail(String context, Object nce, Class p, String pn, Exception x) {
		StringBuilder sb = new StringBuilder();
		sb.append("Context: ").append(context);
		sb.append(", target: ").append(nce == null ? "null" : nce.toString());
		sb.append(", pClass: ").append(p == null ? "null" : p.getName());
		sb.append(", pName: ").append(pn == null ? "null" : pn);
		sb.append(", Exception: ").append(x == null ? "null" : x.toString());
		String msg = sb.toString();
		fail(msg);
	}

	static void fail(String context, Object nce, Class p, String pn, Object pv,
			Exception x) {
		StringBuilder sb = new StringBuilder();
		sb.append("Context: ").append(context);
		sb.append(", target: ").append(nce == null ? "null" : nce.toString());
		sb.append(", pClass: ").append(p == null ? "null" : p.getName());
		sb.append(", pName: ").append(pn == null ? "null" : pn);
		sb.append(", pValue: ").append(pv == null ? "null" : pv.toString());
		sb.append(", Exception: ").append(x == null ? "null" : x.toString());
		String msg = sb.toString();
		fail(msg);
	}

	public static String standardizePropertyName(String pn) {
		assertTrue("Property name must be non-null", pn != null);
		assertTrue("Property name must not be blank", !pn.isEmpty());
		assertTrue("Property name must be trimmed", pn.trim().equals(pn));
		char c = pn.charAt(0);
		String s;
		if (pn.length() > 1) {
			s = pn.substring(1);
		} else {
			s = "";
		}
		char c2 = Character.toUpperCase(c);
		String retVal = c2 + s;
		return retVal;
	}

	public static int randomInt() {
		int retVal = random.nextInt();
		return retVal;
	}

	public static int randomInt(int excludedValue) {
		int retVal = randomInt();
		while (retVal == excludedValue) {
			retVal = randomInt();
		}
		assertTrue(retVal != excludedValue);
		return retVal;
	}

	public static boolean randomBoolean() {
		boolean retVal = random.nextBoolean();
		return retVal;
	}

	public static String randomString() {
		String retVal = UUID.randomUUID().toString();
		return retVal;
	}

	public static float randomFloat() {
		float retVal = random.nextFloat();
		return retVal;
	}

	public static Method getAccessor(final Class c, final Class p,
			final String _pn) {
		final String METHOD = "getAccessor";
		assertTrue("Target class must be non-null", c != null);
		assertTrue("Property value class must be non-null", p != null);
		final String pn = standardizePropertyName(_pn);

		Method retVal = null;
		try {
			final String accessorName;
			if (Boolean.class.isAssignableFrom(p)
					|| boolean.class.isAssignableFrom(p)) {
				accessorName = IS + pn;
			} else {
				accessorName = GET + pn;
			}
			retVal = c.getDeclaredMethod(accessorName, (Class[]) null);
		} catch (Exception e) {
			fail(METHOD, c, p, _pn, e);
		}
		assertTrue(retVal != null);

		return retVal;
	}

	public static Method getManipulator(final Class c, final Class p,
			final String _pn) {
		final String METHOD = "getManipulator";
		assertTrue("Target class must be non-null", c != null);
		assertTrue("Property value class must be non-null", p != null);
		final String pn = standardizePropertyName(_pn);

		Method retVal = null;
		try {
			final String manipulatorName = SET + pn;
			retVal = c.getDeclaredMethod(manipulatorName, new Class[] { p });
		} catch (Exception e) {
			fail(METHOD, c, p, _pn, e);
		}
		assertTrue(retVal != null);

		return retVal;
	}

	public static Object getProperty(final Object nce, final Class p,
			final String pn) {
		final String METHOD = "getProperty";
		assertTrue("Object must be non-null", nce != null);
		assertTrue("Property value class must be non-null", p != null);

		Object retVal = null;
		try {
			final Class c = nce.getClass();
			final Method accessor = getAccessor(c, p, pn);
			retVal = (Object) accessor.invoke(nce, (Object[]) null);
		} catch (Exception e) {
			fail(METHOD, nce, p, pn, e);
		}
		return retVal;
	}

	public static void setProperty(final Object nce, final Class p,
			final String pn, final Object pv) {
		setProperty(nce, p, pn, pv, false);
	}

	public static void setProperty(final Object nce, final Class p,
			final String pn, final Object pv, boolean requireChange) {
		final String METHOD = "setProperty";
		assertTrue("Object must be non-null", nce != null);
		assertTrue("Property value class must be non-null", p != null);
		assertTrue("Property value must not be null", pv != null);

		try {
			final Class c = nce.getClass();
			final Method accessor = getAccessor(c, p, pn);
			final Method manipulator = getManipulator(c, p, pn);

			// Confirm the existing value is different from the new value
			// @SuppressWarnings("unchecked")
			if (requireChange) {
				final Object existingValue =
					(Object) accessor.invoke(nce, (Object[]) null);
				assertTrue(!pv.equals(existingValue));
			}

			// Set the new property value in the configuration
			manipulator.invoke(nce, new Object[] { pv });
		} catch (Exception e) {
			fail(METHOD, nce, p, pn, pv, e);
		}
	}

	public static void testProperty(final Object nce, final Class p,
			final String pn, final Object pv) {
		testProperty(nce, p, pn, pv, false);
	}

	public static void testProperty(final Object nce, final Class p,
			final String pn, final Object pv, boolean requireChange) {
		final String METHOD = "testProperty";
		assertTrue("Object must be non-null", nce != null);
		try {
			// Set the property value
			setProperty(nce, p, pn, pv, requireChange);

			// Check the property value
			final Class c = nce.getClass();
			final Method accessor = getAccessor(c, p, pn);
			// @SuppressWarnings("unchecked")
			Object value = (Object) accessor.invoke(nce, (Object[]) null);
			assertTrue(pv.equals(value));
		} catch (Exception e) {
			fail(METHOD, nce, p, pn, pv, e);
		}
	}

}
