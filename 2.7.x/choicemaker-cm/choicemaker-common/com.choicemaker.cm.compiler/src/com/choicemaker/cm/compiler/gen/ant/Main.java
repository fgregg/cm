/*
 * Copyright (c) 2001, 2009 ChoiceMaker Technologies, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ChoiceMaker Technologies, Inc. - initial API and implementation
 */
package com.choicemaker.cm.compiler.gen.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.DemuxOutputStream;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Target;

import com.choicemaker.cm.core.compiler.CompilationArguments;

/**
 * Command line entry point into Ant. This class is entered via the
 * cannonical `public static void main` entry point and reads the
 * command line arguments. It then assembles and executes an Ant
 * project.
 * <p>
 * If you integrating Ant into some other tool, this is not the class
 * to use as an entry point. Please see the source code of this
 * class to see how it manipulates the Ant project classes.
 *
 * @author duncan@x180.com
 */
public class Main {

	/** The default build file name */
	public static final String DEFAULT_BUILD_FILENAME = "build.xml";

	/** Our current message output status. Follows Project.MSG_XXX */
	private int msgOutputLevel = Project.MSG_INFO;

	/** File that we are using for configuration */
	private File buildFile; /** null */

	/** Stream that we are using for logging */
	private PrintStream out = System.out;

	/** Stream that we are using for logging error messages */
	private PrintStream err = System.err;

	/** The build targets */
	private Vector targets = new Vector(5);

	/** Set of properties that can be used by tasks */
	private Properties definedProps = new Properties();

	/** Names of classes to add as listeners to project */
	private Vector listeners = new Vector(5);

	/**
	 * The Ant logger class. There may be only one logger. It will have the
	 * right to use the 'out' PrintStream. The class must implements the BuildLogger
	 * interface
	 */
	private String loggerClassname = null;

	/**
	 * Indicates whether output to the log is to be unadorned.
	 */
	private boolean emacsMode = false;

	/**
	 * Indicates if this ant should be run.
	 */
	private boolean readyToRun = false;

	/**
	 * Indicates we should only parse and display the project help information
	 */
	private boolean projectHelp = false;

	private static boolean success = true;

	/**
	 * Prints the message of the Throwable if it's not null.
	 */
	private static void printMessage(Throwable t) {
		String message = t.getMessage();
		if (message != null) {
			System.err.println(message);
		}
	}

	/**
	 * Entry point allowing for more options from other front ends
	 */
	public static boolean start(String[] args, Properties additionalUserProperties, ClassLoader coreLoader) {
		success = true;
		Main m = null;

		try {
			m = new Main(args);
		} catch (Throwable exc) {
			printMessage(exc);
			return false;
			// System.exit(1);
		}

		if (additionalUserProperties != null) {
			for (Enumeration e = additionalUserProperties.keys(); e.hasMoreElements();) {
				String key = (String) e.nextElement();
				String property = additionalUserProperties.getProperty(key);
				m.definedProps.put(key, property);
			}
		}

		try {
			m.runBuild(coreLoader);
			return success;
			// System.exit(0);
		} catch (BuildException be) {
			if (m.err != System.err) {
				printMessage(be);
			}
			return false;
			// System.exit(1);
		} catch (Throwable exc) {
			printMessage(exc);
			return false;
			// System.exit(1);
		}
	}

	/**
	 * Command line entry point. This method kicks off the building
	 * of a project object and executes a build using either a given
	 * target or the default target.
	 *
	 * @param args Command line args.
	 */
	public static void main(String[] args) {
		start(args, null, null);
	}

	protected Main(String[] args) throws BuildException {

		String searchForThis = null;

		// cycle through given args

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			if (arg.equals("-help")) {
				printUsage();
				return;
			} else if (arg.equals("-version")) {
				printVersion();
				return;
			} else if (arg.equals("-quiet") || arg.equals("-q")) {
				msgOutputLevel = Project.MSG_WARN;
			} else if (arg.equals(CompilationArguments.VERBOSE)
					|| arg.equals(CompilationArguments.VERBOSE)) {
				printVersion();
				msgOutputLevel = Project.MSG_VERBOSE;
			} else if (arg.equals(CompilationArguments.DEBUG)) {
				printVersion();
				msgOutputLevel = Project.MSG_DEBUG;
			} else if (arg.equals("-logfile") || arg.equals("-l")) {
				try {
					File logFile = new File(args[i + 1]).getAbsoluteFile();
					i++;
					out = new PrintStream(new FileOutputStream(logFile));
					err = out;
					System.setOut(out);
					System.setErr(out);
				} catch (IOException ioe) {
					String msg =
						"Cannot write on the specified log file. "
								+ "Make sure the path exists and you have write permissions.";
					System.out.println(msg);
					return;
				} catch (ArrayIndexOutOfBoundsException aioobe) {
					String msg =
						"You must specify a log file when "
								+ "using the -log argument";
					System.out.println(msg);
					return;
				}
			} else if (arg.equals("-buildfile") || arg.equals("-file")
					|| arg.equals("-f")) {
				try {
					buildFile = new File(args[i + 1]).getAbsoluteFile();
					i++;
				} catch (ArrayIndexOutOfBoundsException aioobe) {
					String msg =
						"You must specify a buildfile when "
								+ "using the -buildfile argument";
					System.out.println(msg);
					return;
				}
			} else if (arg.equals("-listener")) {
				try {
					listeners.addElement(args[i + 1]);
					i++;
				} catch (ArrayIndexOutOfBoundsException aioobe) {
					String msg =
						"You must specify a classname when "
								+ "using the -listener argument";
					System.out.println(msg);
					return;
				}
			} else if (arg.startsWith("-D")) {

				/*
				 * Interestingly enough, we get to here when a user uses
				 * -Dname=value. However, in some cases, the JDK goes ahead *
				 * and parses this out to args {"-Dname", "value"} so instead of
				 * parsing on "=", we just make the "-D" characters go away and
				 * skip one argument forward.
				 * 
				 * I don't know how to predict when the JDK is going to help or
				 * not, so we simply look for the equals sign.
				 */

				String name = arg.substring(2, arg.length());
				String value = null;
				int posEq = name.indexOf("=");
				if (posEq > 0) {
					value = name.substring(posEq + 1);
					name = name.substring(0, posEq);
				} else if (i < args.length - 1)
					value = args[++i];

				definedProps.put(name, value);
			} else if (arg.equals("-logger")) {
				if (loggerClassname != null) {
					System.out
							.println("Only one logger class may be specified.");
					return;
				}
				try {
					loggerClassname = args[++i];
				} catch (ArrayIndexOutOfBoundsException aioobe) {
					System.out.println("You must specify a classname when "
							+ "using the -logger argument");
					return;
				}
			} else if (arg.equals("-emacs")) {
				emacsMode = true;
			} else if (arg.equals("-projecthelp")) {
				// set the flag to display the targets and quit
				projectHelp = true;
			} else if (arg.equals("-find")) {
				// eat up next arg if present, default to build.xml
				if (i < args.length - 1) {
					searchForThis = args[++i];
				} else {
					searchForThis = DEFAULT_BUILD_FILENAME;
				}
			} else if (arg.startsWith("-")) {
				// we don't have any more args to recognize!
				String msg = "Unknown argument: " + arg;
				System.out.println(msg);
				printUsage();
				return;
			} else {
				// if it's no other arg, it may be the target
				targets.addElement(arg);
			}

		}

		// if buildFile was not specified on the command line,
		if (buildFile == null) {
			// but -find then search for it
			if (searchForThis != null) {
				buildFile = findBuildFile(System.getProperty("user.dir"), searchForThis);
			} else {
				buildFile = new File(DEFAULT_BUILD_FILENAME).getAbsoluteFile();
			}
		}

		// make sure buildfile exists
		if (!buildFile.exists()) {
			System.out.println("Buildfile: " + buildFile + " does not exist!");
			throw new BuildException("Build failed");
		}

		// make sure it's not a directory (this falls into the ultra
		// paranoid lets check everything catagory

		if (buildFile.isDirectory()) {
			System.out.println("What? Buildfile: " + buildFile + " is a dir!");
			throw new BuildException("Build failed");
		}

		readyToRun = true;
	}

	/**
	 * Helper to get the parent file for a given file.
	 *
	 * <P>Added to simulate File.getParentFile() from JDK 1.2.
	 *
	 * @param file   File
	 * @return       Parent file or null if none
	 */
	private File getParentFile(File file) {
		String filename = file.getAbsolutePath();
		file = new File(filename).getAbsoluteFile();
		filename = file.getParent();

		if (filename != null && msgOutputLevel >= Project.MSG_VERBOSE) {
			System.out.println("Searching in " + filename);
		}

		return (filename == null) ? null : new File(filename).getAbsoluteFile();
	}

	/**
	 * Search parent directories for the build file.
	 *
	 * <P>Takes the given target as a suffix to append to each
	 *    parent directory in seach of a build file.  Once the
	 *    root of the file-system has been reached an exception
	 *    is thrown.
	 *
	 * @param suffix    Suffix filename to look for in parents.
	 * @return          A handle to the build file
	 *
	 * @exception BuildException    Failed to locate a build file
	 */
	private File findBuildFile(String start, String suffix) throws BuildException {
		if (msgOutputLevel >= Project.MSG_INFO) {
			System.out.println("Searching for " + suffix + " ...");
		}

		File parent = new File(new File(start).getAbsolutePath());
		File file = new File(parent, suffix);

		// check if the target file exists in the current directory
		while (!file.exists()) {
			// change to parent directory
			parent = getParentFile(parent);

			// if parent is null, then we are at the root of the fs,
			// complain that we can't find the build file.
			if (parent == null) {
				throw new BuildException("Could not locate a build file!");
			}

			// refresh our file handle
			file = new File(parent, suffix).getAbsoluteFile();
		}

		return file;
	}

	/**
	 * Executes the build.
	 */
	private void runBuild(ClassLoader coreLoader) throws BuildException {

		if (!readyToRun) {
			return;
		}

		// track when we started

		if (msgOutputLevel >= Project.MSG_INFO) {
			System.out.println("Buildfile: " + buildFile);
		}

		final Project project = new Project();
		project.setCoreLoader(coreLoader);

		Throwable error = null;

		try {
			addBuildListeners(project);
			project.addBuildListener(new BuildListener() {
				public void buildStarted(BuildEvent event) {
				}
				public void buildFinished(BuildEvent event) {
					success = event.getException() == null;
				}
				public void targetStarted(BuildEvent event) {
				}
				public void targetFinished(BuildEvent event) {
				}
				public void taskStarted(BuildEvent event) {
				}
				public void taskFinished(BuildEvent event) {
				}
				public void messageLogged(BuildEvent event) {
				}
			});

			PrintStream err = System.err;
			PrintStream out = System.out;

			try {
				System.setOut(new PrintStream(new DemuxOutputStream(project, false)));
				System.setErr(new PrintStream(new DemuxOutputStream(project, true)));
				project.fireBuildStarted();
				project.init();
				project.setUserProperty("ant.version", getAntVersion());

				// set user-define properties
				Enumeration e = definedProps.keys();
				while (e.hasMoreElements()) {
					String arg = (String) e.nextElement();
					String value = (String) definedProps.get(arg);
					project.setUserProperty(arg, value);
				}

				project.setUserProperty("ant.file", buildFile.getAbsolutePath());

				// first use the ProjectHelper to create the project object
				// from the given build file.
				String noParserMessage =
					"No JAXP compliant XML parser found. Please visit http://xml.apache.org for a suitable parser";
				try {
					Class.forName("javax.xml.parsers.SAXParserFactory");
					ProjectHelper.configureProject(project, buildFile);
				} catch (NoClassDefFoundError ncdfe) {
					throw new BuildException(noParserMessage, ncdfe);
				} catch (ClassNotFoundException cnfe) {
					throw new BuildException(noParserMessage, cnfe);
				} catch (NullPointerException npe) {
					throw new BuildException(noParserMessage, npe);
				}

				// make sure that we have a target to execute
				if (targets.size() == 0) {
					targets.addElement(project.getDefaultTarget());
				}

				if (!projectHelp) {
					project.executeTargets(targets);
				}
			} finally {
				System.setOut(out);
				System.setErr(err);
			}
			if (projectHelp) {
				printDescription(project);
				printTargets(project);
			}
		} catch (RuntimeException exc) {
			error = exc;
			throw exc;
		} catch (Error err) {
			error = err;
			throw err;
		} finally {
			project.fireBuildFinished(error);
		}
	}

	protected void addBuildListeners(Project project) {

		// Add the default listener
		project.addBuildListener(createLogger());

		for (int i = 0; i < listeners.size(); i++) {
			String className = (String) listeners.elementAt(i);
			try {
				BuildListener listener = (BuildListener) Class.forName(className).newInstance();
				project.addBuildListener(listener);
			} catch (Throwable exc) {
				throw new BuildException("Unable to instantiate listener " + className, exc);
			}
		}
	}

	/**
	 *  Creates the default build logger for sending build events to the ant log.
	 */
	private BuildLogger createLogger() {
		BuildLogger logger = null;
		if (loggerClassname != null) {
			try {
				logger = (BuildLogger) (Class.forName(loggerClassname).newInstance());
			} catch (ClassCastException e) {
				System.err.println(
					"The specified logger class " + loggerClassname + " does not implement the BuildLogger interface");
				throw new RuntimeException();
			} catch (Exception e) {
				System.err.println(
					"Unable to instantiate specified logger class " + loggerClassname + " : " + e.getClass().getName());
				throw new RuntimeException();
			}
		} else {
			logger = new DefaultLogger();
		}

		logger.setMessageOutputLevel(msgOutputLevel);
		logger.setOutputPrintStream(out);
		logger.setErrorPrintStream(err);
		logger.setEmacsMode(emacsMode);

		return logger;
	}

	/**
	 * Prints the usage of how to use this class to System.out
	 */
	private static void printUsage() {
		String lSep = System.getProperty("line.separator");
		StringBuffer msg = new StringBuffer();
		msg.append("ant [options] [target [target2 [target3] ...]]" + lSep);
		msg.append("Options: " + lSep);
		msg.append("  -help                  print this message" + lSep);
		msg.append("  -projecthelp           print project help information" + lSep);
		msg.append("  -version               print the version information and exit" + lSep);
		msg.append("  -quiet                 be extra quiet" + lSep);
		msg.append("  -verbose               be extra verbose" + lSep);
		msg.append("  -debug                 print debugging information" + lSep);
		msg.append("  -emacs                 produce logging information without adornments" + lSep);
		msg.append("  -logfile <file>        use given file for log" + lSep);
		msg.append("  -logger <classname>    the class which is to perform logging" + lSep);
		msg.append("  -listener <classname>  add an instance of class as a project listener" + lSep);
		msg.append("  -buildfile <file>      use given buildfile" + lSep);
		msg.append("  -D<property>=<value>   use value for given property" + lSep);
		msg.append("  -find <file>           search for buildfile towards the root of the" + lSep);
		msg.append("                         filesystem and use it" + lSep);
		System.out.println(msg.toString());
	}

	private static void printVersion() throws BuildException {
		System.out.println(getAntVersion());
	}

	private static String antVersion = null;

	public synchronized static String getAntVersion() throws BuildException {
		if (antVersion == null) {
			try {
				Properties props = new Properties();
				InputStream in = Main.class.getResourceAsStream("/org/apache/tools/ant/version.txt");
				props.load(in);
				in.close();

				StringBuffer msg = new StringBuffer();
				msg.append("Ant version ");
				msg.append(props.getProperty("VERSION"));
				msg.append(" compiled on ");
				msg.append(props.getProperty("DATE"));
				antVersion = msg.toString();
			} catch (IOException ioe) {
				throw new BuildException("Could not load the version information:" + ioe.getMessage());
			} catch (NullPointerException npe) {
				throw new BuildException("Could not load the version information.");
			}
		}
		return antVersion;
	}

	/**
	 * Print the project description, if any
	 */
	private static void printDescription(Project project) {
		if (project.getDescription() != null) {
			System.out.println(project.getDescription());
		}
	}

	/**
	 * Print out a list of all targets in the current buildfile
	 */
	private static void printTargets(Project project) {
		// find the target with the longest name
		int maxLength = 0;
		Enumeration ptargets = project.getTargets().elements();
		String targetName;
		String targetDescription;
		Target currentTarget;
		// split the targets in top-level and sub-targets depending
		// on the presence of a description
		Vector topNames = new Vector();
		Vector topDescriptions = new Vector();
		Vector subNames = new Vector();

		while (ptargets.hasMoreElements()) {
			currentTarget = (Target) ptargets.nextElement();
			targetName = currentTarget.getName();
			targetDescription = currentTarget.getDescription();
			// maintain a sorted list of targets
			if (targetDescription == null) {
				int pos = findTargetPosition(subNames, targetName);
				subNames.insertElementAt(targetName, pos);
			} else {
				int pos = findTargetPosition(topNames, targetName);
				topNames.insertElementAt(targetName, pos);
				topDescriptions.insertElementAt(targetDescription, pos);
				if (targetName.length() > maxLength) {
					maxLength = targetName.length();
				}
			}
		}

		String defaultTarget = project.getDefaultTarget();
		if (defaultTarget != null && !"".equals(defaultTarget)) { // shouldn't need to check but...
			Vector defaultName = new Vector();
			Vector defaultDesc = null;
			defaultName.addElement(defaultTarget);

			int indexOfDefDesc = topNames.indexOf(defaultTarget);
			if (indexOfDefDesc >= 0) {
				defaultDesc = new Vector();
				defaultDesc.addElement(topDescriptions.elementAt(indexOfDefDesc));
			}
			printTargets(defaultName, defaultDesc, "Default target:", maxLength);

		}

		printTargets(topNames, topDescriptions, "Main targets:", maxLength);
		printTargets(subNames, null, "Subtargets:", 0);
	}

	/**
	 * Search for the insert position to keep names a sorted list of Strings
	 */
	private static int findTargetPosition(Vector names, String name) {
		int res = names.size();
		for (int i = 0; i < names.size() && res == names.size(); i++) {
			if (name.compareTo((String) names.elementAt(i)) < 0) {
				res = i;
			}
		}
		return res;
	}

	/**
	 * Output a formatted list of target names with an optional description
	 */
	private static void printTargets(Vector names, Vector descriptions, String heading, int maxlen) {
		// now, start printing the targets and their descriptions
		String lSep = System.getProperty("line.separator");
		// got a bit annoyed that I couldn't find a pad function
		String spaces = "    ";
		while (spaces.length() < maxlen) {
			spaces += spaces;
		}
		StringBuffer msg = new StringBuffer();
		msg.append(heading + lSep + lSep);
		for (int i = 0; i < names.size(); i++) {
			msg.append(" ");
			msg.append(names.elementAt(i));
			if (descriptions != null) {
				msg.append(spaces.substring(0, maxlen - ((String) names.elementAt(i)).length() + 2));
				msg.append(descriptions.elementAt(i));
			}
			msg.append(lSep);
		}
		System.out.println(msg.toString());
	}
}
