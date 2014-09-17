/**
 * @(#)$RCSfile: BootLoader.java,v $  $Revision: 1.1 $ $Date: 2010/01/27 03:58:28 $
 * 
 * Copyright (c) 2003 ChoiceMaker Technologies, Inc. 
 * 71 W 23rd St, Ste 515, New York, NY 10010 
 * All rights reserved.
 * 
 * This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

package com.choicemaker.e2.mbd.core.boot;

import java.util.Locale;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.1 $ $Date: 2010/01/27 03:58:28 $
 */
public class BootLoader {
	/**
	 * Constant string (value "win32") indicating the platform is running on a
	 * Window 32-bit operating system (e.g., Windows 98, NT, 2000).
	 */
	public static final String OS_WIN32 = "win32";//$NON-NLS-1$

	/**
	 * Constant string (value "linux") indicating the platform is running on a
	 * Linux-based operating system.
	 */
	public static final String OS_LINUX = "linux";//$NON-NLS-1$

	/**
	 * Constant string (value "aix") indicating the platform is running on an
	 * AIX-based operating system.
	 */
	public static final String OS_AIX = "aix";//$NON-NLS-1$

	/**
	 * Constant string (value "solaris") indicating the platform is running on a
	 * Solaris-based operating system.
	 */
	public static final String OS_SOLARIS = "solaris";//$NON-NLS-1$

	/**
	 * Constant string (value "hpux") indicating the platform is running on an
	 * HP/UX-based operating system.
	 */
	public static final String OS_HPUX = "hpux";//$NON-NLS-1$

	/**
	 * Constant string (value "qnx") indicating the platform is running on a
	 * QNX-based operating system.
	 */
	public static final String OS_QNX = "qnx";//$NON-NLS-1$

	/**
	 * Constant string (value "macosx") indicating the platform is running on a
	 * Mac OS X operating system.
	 * 
	 * @since 2.0
	 */
	public static final String OS_MACOSX = "macosx";//$NON-NLS-1$

	/**
	 * Constant string (value "unknown") indicating the platform is running on a
	 * machine running an unknown operating system.
	 */
	public static final String OS_UNKNOWN = "unknown";//$NON-NLS-1$

	/**
	 * Constant string (value "x86") indicating the platform is running on an
	 * x86-based architecture.
	 */
	public static final String ARCH_X86 = "x86";//$NON-NLS-1$

	/**
	 * Constant string (value "PA_RISC") indicating the platform is running on an
	 * PA_RISC-based architecture.
	 */
	public static final String ARCH_PA_RISC = "PA_RISC";//$NON-NLS-1$

	/**
	 * Constant string (value "ppc") indicating the platform is running on an
	 * PowerPC-based architecture.
	 * 
	 * @since 2.0
	 */
	public static final String ARCH_PPC = "ppc";//$NON-NLS-1$

	/**
	 * Constant string (value "sparc") indicating the platform is running on an
	 * Sparc-based architecture.
	 * 
	 * @since 2.0
	 */
	public static final String ARCH_SPARC = "sparc";//$NON-NLS-1$

	/**
	 * Constant string (value "win32") indicating the platform is running on a
	 * machine using the Windows windowing system.
	 */
	public static final String WS_WIN32 = "win32";//$NON-NLS-1$

	/**
	 * Constant string (value "motif") indicating the platform is running on a
	 * machine using the Motif windowing system.
	 */
	public static final String WS_MOTIF = "motif";//$NON-NLS-1$

	/**
	 * Constant string (value "gtk") indicating the platform is running on a
	 * machine using the GTK windowing system.
	 */
	public static final String WS_GTK = "gtk";//$NON-NLS-1$

	/**
	 * Constant string (value "photon") indicating the platform is running on a
	 * machine using the Photon windowing system.
	 */
	public static final String WS_PHOTON = "photon";//$NON-NLS-1$

	/**
	 * Constant string (value "carbon") indicating the platform is running on a
	 * machine using the Carbon windowing system (Mac OS X).
	 * 
	 * @since 2.0
	 */
	public static final String WS_CARBON = "carbon";//$NON-NLS-1$

	/**
	 * Constant string (value "unknown") indicating the platform is running on a
	 * machine running an unknown windowing system.
	 */
	public static final String WS_UNKNOWN = "unknown";//$NON-NLS-1$
	
	// While we recognize the SunOS operating system, we change
	// this internally to be Solaris.
	private static final String INTERNAL_OS_SUNOS = "SunOS"; //$NON-NLS-1$
	// While we recognize the i386 architecture, we change
	// this internally to be x86.
	private static final String INTERNAL_ARCH_I386 = "i386"; //$NON-NLS-1$
	
	private static String ws;
	private static String os;
	private static String arch;
	private static String nl;
	
	private static void init() {
		if (os == null) {
			String name = System.getProperty("os.name");//$NON-NLS-1$
			String lowerCaseName = name.toLowerCase();
			if (name.regionMatches(true, 0, BootLoader.OS_WIN32, 0, 3)) {
				os = BootLoader.OS_WIN32;
			} else if(lowerCaseName.indexOf(OS_LINUX) != -1) {
				os = OS_LINUX;
			} else if(lowerCaseName.indexOf("mac") != -1) {
				os = OS_MACOSX;
			} else {
				os = name.equalsIgnoreCase(INTERNAL_OS_SUNOS) ? BootLoader.OS_SOLARIS : BootLoader.OS_UNKNOWN;
			}
		}

		// if the user didn't set the window system with a command line 
		// argument then use the default.
		if (ws == null) {
			// setup default values for known OSes if nothing was specified
			if (os.equals(BootLoader.OS_WIN32))
				ws = BootLoader.WS_WIN32;
			else if (os.equals(BootLoader.OS_LINUX))
				ws = BootLoader.WS_MOTIF;
			else if (os.equals(BootLoader.OS_MACOSX))
				ws = BootLoader.WS_CARBON;
			else if (os.equals(BootLoader.OS_HPUX))
				ws = BootLoader.WS_MOTIF;
			else if (os.equals(BootLoader.OS_AIX))
				ws = BootLoader.WS_MOTIF;
			else if (os.equals(BootLoader.OS_SOLARIS))
				ws = BootLoader.WS_MOTIF;
			else
				ws = BootLoader.WS_UNKNOWN;
		}

		// if the user didn't set the system architecture with a command line 
		// argument then use the default.
		if (arch == null) {
			String name = System.getProperty("os.arch");//$NON-NLS-1$
			// Map i386 architecture to x86
			arch = name.equalsIgnoreCase(INTERNAL_ARCH_I386) ? BootLoader.ARCH_X86 : name;
		}
		if(nl == null) {
			nl = Locale.getDefault().toString();
		}
	}

	public static String getNL() {
		init();
		return nl;
	}

	public static String getWS() {
		init();
		return ws;
	}

	public static String getOS() {
		init();
		return os;
	}

	public static String getOSArch() {
		init();
		return arch;
	}
	
	public static void setOs(String string) {
		os = string;
	}

	public static void setOsArch(String string) {
		arch = string;
	}

	public static void setWs(String string) {
		ws = string;
	}

	public static void setNl(String string) {
		nl = string;
	}
}
