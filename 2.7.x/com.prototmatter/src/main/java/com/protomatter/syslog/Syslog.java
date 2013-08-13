package com.protomatter.syslog;

/**
 *  {{{ The Protomatter Software License, Version 1.0
 *  derived from The Apache Software License, Version 1.1
 *
 *  Copyright (c) 1998-2002 Nate Sammons.  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *     if any, must include the following acknowledgment:
 *        "This product includes software developed for the
 *         Protomatter Software Project
 *         (http://protomatter.sourceforge.net/)."
 *     Alternately, this acknowledgment may appear in the software itself,
 *     if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Protomatter" and "Protomatter Software Project" must
 *     not be used to endorse or promote products derived from this
 *     software without prior written permission. For written
 *     permission, please contact support@protomatter.com.
 *
 *  5. Products derived from this software may not be called "Protomatter",
 *     nor may "Protomatter" appear in their name, without prior written
 *     permission of the Protomatter Software Project
 *     (support@protomatter.com).
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE PROTOMATTER SOFTWARE PROJECT OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.   }}}
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.reflect.*;
import java.text.MessageFormat;
import com.protomatter.util.*;
import com.protomatter.Protomatter;

/**
 *  This class implements a system-wide logging utility. Please read the <a
 *  href="syslog-whitepaper.html">Syslog Whitepaper</a> for more information. It
 *  allows a program to log messages and objects at different severity levels in
 *  a standardized way. The implementation of the log is specified by an object
 *  which implements the Syslogger interface. You can have multiple log
 *  implementations, and each can have it's own log mask or can inherit it's log
 *  mask from Syslog (this is the default behavior).<P>
 *
 *  There are 5 severity levels: DEBUG, INFO, WARNING, ERROR, FATAL. They
 *  correspond to the numbers 0, 4, 8, 12, and 16, respectively. You can enable
 *  logging for any combination of these levels. The default setting logs only
 *  WARNING and above.<P>
 *
 *
 *  <DL>
 *    <DT> <B>Quickie introduction and usage:</B> </DT>
 *    <DD>
 *    <TABLEBORDER=1 CELLPADDING=5 CELLSPACING=0>
 *
 *      <TR>
 *
 *        <TD>
 *          Anywhere you're doing this: <BLOCKQUOTE> <TT>
 *          System.out.println("Some message");</TT> </BLOCKQUOTE> <P>
 *
 *          Do this instead: <BLOCKQUOTE> <TT>Syslog.debug(this, "Some
 *          message");</TT> </BLOCKQUOTE> <P>
 *
 *          If you're in a <tt>static</tt> method, do this: <BLOCKQUOTE> <TT>
 *          Syslog.debug(MyClass.class, "Some message");</TT> </BLOCKQUOTE> <P>
 *
 *          If you like what this gets you, start playing around with different
 *          log levels (call "<TT>Syslog.info(...)</TT> " and others). If you
 *          like that, start playing around with channels and configuring the
 *          loggers and other things. Syslog has a <u>very</u> robust API and
 *          can be bent around to do all sorts of things, but it <u>does not
 *          have to be complicated to use</u> even though it <u>can</u> be
 *          configured in all sorts of complicated ways.
 *        </TD>
 *
 *      </TR>
 *
 *    </TABLE>
 *    </DD>
 *  </DL>
 *  <P>
 *
 *
 *  <DL>
 *    <DT> <B>More involved usage examples:</B> </DT>
 *    <DD>
 *    <TABLEBORDER=1 CELLPADDING=5 CELLSPACING=0>
 *
 *   <TR>
 *   <TD>
 *   <pre>
 *   // log simple message at DEBUG level
 *   // pass 'this' as the object performing the logging
 *   Syslog.log(this, "simple message", null, Syslog.DEBUG);<P>
 *
 *   // this performs toString() on object 'obj', and prints a message
 *   Syslog.log(this, "logging object", obj, Syslog.INFO);<P>
 *
 *   // logs 'exception' and its stack trace at ERROR level
 *   Syslog.log(this, exception);<P>
 *
 *   // here's how you log in a static method -- pass the class object
 *   Syslog.log(MyClass.class, "logging in MyClass");<P>
 *
 *   // shortcuts for each log level Syslog.debug(this, "foo");
 *   Syslog.debug(this, "foo", bar); Syslog.info(this, "foo");
 *   Syslog.info(this, "foo", bar);<P>
 *
 *   // log to multiple channels (the "foo" and "bar" channels)
 *   Syslog.infoToChannel(this, new String[]{"foo", "bar"}, "My
 *   message"); </pre>
 *   </TD>
 *   </TR>
 *   </TABLE>
 *   </DD>
 *  </DL>
 *  <P>
 *
 *  There are basically three types of methods on this class for issuing log
 *  messages. Whenever a channel name is not specified, the channel <tt>
 *  DEFAULT_CHANNEL</tt> is used.<P>
 *
 *  Whenever a channel is specified, it is an <tt>Object</tt> . This argument
 *  can either be a <tt>String</tt> or an array of <tt>String</tt> (in which
 *  case, the message will be sent out to multiple channels).<P>
 *
 *
 *  <ul>
 *    <dl>
 *      <dt> <tt>log(...);</tt> <br>
 *      </dt>
 *      <dd> They have the following forms:<P>
 *
 *      </dd>
 *      <dd> <tt> log(Object logger, Throwable t);<br>
 *      log(Object logger, Throwable t, int level);<br>
 *      log(Object logger, Object msg, Object detail, int level);<br>
 *      log(Object logger, Object chan, Object msg, Object detail, int level);
 *      <br>
 *      log(InetAddress host, Object logger, Throwable t);<br>
 *      log(InetAddress host, Object logger, Throwable t, int level);<br>
 *      log(InetAddress host, Object logger, Object msg, Object detail, int
 *      level);<br>
 *      log(InetAddress host, Object logger, Object chan, Object msg, Object
 *      detail, int level);<br>
 *      </tt> </dd> <P>
 *
 *
 *      <dt> <tt>xxx(...);</tt> <br>
 *      </dt>
 *      <dd> Where "<tt>xxx</tt> " is "<tt>debug</tt> ", "<tt>info</tt> ", "<tt>
 *      warning</tt> ", "<tt>error</tt> ", or "<tt>fatal</tt> " These are
 *      shortcuts to the <tt>log(...)</tt> method for each log level. They have
 *      the following forms:<P>
 *
 *      </dd>
 *      <dd> <tt> xxx(Object logger, Object msg);<br>
 *      xxx(Object logger, Object msg, Object detail);<br>
 *      xxx(InetAddress host, Object logger, Object msg);<br>
 *      xxx(InetAddress host, Object logger, Object msg, Object detail);<br>
 *      </tt> </dd> <P>
 *
 *
 *      <dt> <tt>xxxToChannel(...);</tt> <br>
 *      </dt>
 *      <dd> Where "<tt>xxx</tt> " is "<tt>debug</tt> ", "<tt>info</tt> ", "<tt>
 *      warning</tt> ", "<tt>error</tt> ", or "<tt>fatal</tt> " These are
 *      shortcuts to the <tt>log(...)</tt> method for each log level, and to a
 *      specific channel. They have the following forms:<P>
 *
 *      </dd>
 *      <dd> <tt> xxxToChannel(Object logger, Object chan, Object msg);<br>
 *      xxxToChannel(Object logger, Object chan, Object msg, Object detail);<br>
 *      xxxToChannel(InetAddress host, Object logger, Object chan, Object msg);
 *      <br>
 *      xxxToChannel(InetAddress host, Object logger, Object chan, Object msg,
 *      Object detail);<br>
 *      </tt> </dd> <P>
 *
 *
 *    </dl>
 *
 *  </ul><P>
 *
 *  You may want to look at the <tt><a href="Channel.html">Channel</a></tt>
 *  class.  Some people prefet its API.<P>
 *
 *  When the <TT>Syslog</TT> class is loaded, the following happens:<P>
 *
 *
 *  <ol>
 *    <li> If the <tt>Syslog.level</tt> system property is set, it is passed to
 *    <tt>Syslog.setLogMask(...)</tt> (otherwise it defaults to <tt>WARNING</tt>
 *    ).<P>
 *
 *
 *    <li> A <tt>PrintWriterLog</tt> named "<TT>PrintWriterLog.err</TT> " is
 *    added using <tt>Syslog.addLogger(...)</tt> and connected to the <tt>
 *    System.err</tt> PrintStream.<P>
 *
 *
 *    <li> If the <tt>Syslog.file</tt> system property is set, it is interpreted
 *    as a filename and another <tt>PrintWriterLog</tt> is added and connected
 *    to that file. The logger is named "<TT>PrintWriterLog.file</TT> ".
 *  </ol>
 *  <P>
 *
 *  If you want to change these defaults, you can use the <tt>getLoggers()</tt>
 *  , <tt>addLogger(...)</tt> , <tt>removeLogger(...)</tt> and <tt>
 *  removeAllLoggers()</tt> methods to change what loggers are registered with
 *  <tt>Syslog</tt> .<P>
 *
 *  Syslog provides background work queues for loggers that need to perform
 *  their operations asynchronously. For instance, the <tt>MailLog</tt> performs
 *  its policy check synchronously and then if it decides that a message will
 *  actually need to be sent, it asks syslog to perform the operation
 *  asynchronously. This is far more efficient than doing <i>everything</i> in
 *  the background because the amount of synchronization necessary to add work
 *  to a queue is usually larger than a policy check.<P>
 *
 *  <B>Please read the <a href="syslog-whitepaper.html">Syslog Whitepaper</a>
 *  for more information on Syslog.</B> <P>
 *
 *
 *
 *@author     nate
 *@created    May 3, 2002
 *@see        Syslogger
 *@see        SyslogChannelAware
 */
public class Syslog
{
    public static final Debug debugging = Debug.forPackage(Syslog.class);

    /**
     *  A log generated during debugging of the software.
     */
    public final static int DEBUG = 1;

    /**
     *  An informational message that might come in handy later.
     */
    public final static int INFO = 2;

    /**
     *  A warning message that the system administrator might want to know
     *  about.
     */
    public final static int WARNING = 4;

    /**
     *  One of the software components caused an error or exception.
     */
    public final static int ERROR = 8;

    /**
     *  One of the software components is no longer functional.
     */
    public final static int FATAL = 16;

    /**
     *  Loggers can inherit Syslog's log mask by setting their log mask to this
     *  value.
     */
    public final static int INHERIT_MASK = 0;

    /**
     *  The name of the default log channel.
     */
    public final static String DEFAULT_CHANNEL = "DEFAULT_CHANNEL";

    /**
     *  The symbolic name for all channels.
     */
    public final static String ALL_CHANNEL = "*";

    /**
     *  The current system-wide log mask. This variable is exposed so that
     *  policies that inherit their mask from Syslog can get the value faster
     *  than by calling a method.
     */
    public static int currentLogMask = atOrAbove(WARNING);

    /**
     *  The name of a log channel for syslog configuration and system related
     *  messages.
     */
    public static String SYSLOG_CHANNEL_NAME = "SYSLOG";

    /**
     *  A log channel for syslog configuration and system related messages.
     */
    public static Channel channel = Channel.getChannel(SYSLOG_CHANNEL_NAME);

    // these loggers may or may not be synchronous.
    private static List loggers;
    private static Syslogger loggerArray[];

    private static Syslog instance = null;

    private static Map queueMap = new HashMap();

    private static InetAddress localHost = null;

    private static SyslogFlushThread flushThread = null;

    // turns off everything.
    private static boolean masterSwitch = true;

    private static String[] defaultChannelList = new String[]{DEFAULT_CHANNEL};

    private static String Q_MARK = "?";

    private static ResourceBundle resources = null;

    private static boolean computeCaller = false;

    private static boolean alwaysComputeCaller = false;

    /**
     *  Syslog classloader warning system property name.
     */
    public final static String WARNING_PROPERTY = "Syslog.classloader.warning";


    /**
     *  Protected constructor. Please don't make a subclass of <TT>Syslog</TT>
     *  unless you really know what you're doing.
     */
    protected Syslog()
    {
        super();
    }


    /**
     *  Returns the global Syslog instance. You really should not need to get
     *  ahold of it, as all the methods are static. You might want to get it so
     *  that you can keep an instance around to keep the class from being
     *  unloaded if you're writing application servers.
     *
     *@return    The instance value
     */
    public static Syslog getInstance()
    {
        return instance;
    }

    /**
     *  Set the flag for computing calling class and method
     *  names at runtime.  Default is <tt>false</tt>.
     */
    public static void setComputeCaller(boolean setting)
    {
        computeCaller = setting;
    }

    /**
     *  Get the flag for computing calling class and method
     *  names at runtime.  Default is <tt>false</tt>.
     */
    public static boolean getComputeCaller()
    {
        return computeCaller;
    }


    /**
     *  Set the flag for always computing calling class and method
     *  names at runtime.  Default is <tt>false</tt>.
     */
    public static void setAlwaysComputeCaller(boolean setting)
    {
        alwaysComputeCaller = setting;
    }

    /**
     *  Get the flag for always computing calling class and method
     *  names at runtime.  Default is <tt>false</tt>.  If this is
     *  set to <tt>true</tt> then the correct method name and caller
     *  are always computed so they are available to the
     *  log policies.  This can be overly expensive if you have
     *  lots of debug statements.  If this flag is set to <tt>false</tt>
     *  then the caller class and method are only computed if
     *  the log call passes the policy check, and if the
     *  <tt>getComputeCallingClassAndMethod()</tt> flag is also
     *  set to <tt>true</tt>.
     */
    public static boolean getAlwaysComputeCaller()
    {
        return alwaysComputeCaller;
    }


    /**
     *  Get a map of the background work queues. The key to the map is the name
     *  of the queue, and the value is a <tt>{@link
     *  com.protomatter.util.WorkQueue com.protomatter.util.WorkQueue}</tt>
     *  object. You should <i>never</i> play with the queues -- this method is
     *  provided so that external processes can monitor the size of each queue
     *  by calling the <tt>getObjectPoolSize()</tt> method on each queue. The
     *  map itself is read-only.
     *
     *@return    The workQueueMap value
     */
    public static Map getWorkQueueMap()
    {
        return Collections.unmodifiableMap(queueMap);
    }


    /**
     *  Static initializer. Performs the initialization steps described above
     *  when the class is loaded.
     */
    static
    {
        checkClassLoader();
        instance = new Syslog();
        loggers = new ArrayList();
        loggerArray = new Syslogger[0];
        init();
    }


    /**
     *  Check to see if we are being loaded by the system classloader or not.
     */
    private static void checkClassLoader()
    {
        ClassLoader loader = Syslog.class.getClassLoader();
        String loaderString = loader.toString();
        ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
        ClassLoader systemLoaderParent = systemLoader.getParent();
        boolean warningFlag = "off".equalsIgnoreCase(System.getProperty(WARNING_PROPERTY));

        if ((systemLoader != loader) && (systemLoaderParent != loader) && (!warningFlag))
        {
            System.err.println(" /***************************************************************************\\");
            System.err.println(" *                                                                          *");
            System.err.println(" *                 Protomatter Syslog Classloader Warning                   *");
            System.err.println(" *                 --------------------------------------                   *");
            System.err.println(" *                                                                          *");
            System.err.println(" *    Protomatter Syslog has been loaded by a classloader other than        *");
            System.err.println(" *    the system classloader.  This indicates that the Syslog libraries     *");
            System.err.println(" *    are not in the system CLASSPATH.  Usually this is because Syslog      *");
            System.err.println(" *    is being used in an application server and the Syslog libraries       *");
            System.err.println(" *    are in the \"lib\" directory for a WebApp or EAR, etc.                  *");
            System.err.println(" *                                                                          *");
            System.err.println(" *    ClassLoader: " + StringUtil.pad(loaderString, 56) + " *");
            System.err.println(" *                                                                          *");
            System.err.println(" *    Syslog may not work as you might expect if it is not loaded by        *");
            System.err.println(" *    the system classloader.  Under some circumstances, users may want     *");
            System.err.println(" *    to take advantage of features of this kind of configuration.  If      *");
            System.err.println(" *    this is the case, you can disable this warning message by setting     *");
            System.err.println(" *    the \"Syslog.classloader.warning\" system property to \"off\".  In        *");
            System.err.println(" *    most cases, though, you don't want to do that.                        *");
            System.err.println(" *                                                                          *");
            System.err.println(" *    For more information on this behavior and the logic behind it,        *");
            System.err.println(" *    please see the documentation at the following address:                *");
            System.err.println(" *                                                                          *");
            System.err.println(" *      http://protomatter.sourceforge.net/" +
                    StringUtil.pad(Protomatter.VERSION + "/javadoc/", 32) + " *");
            System.err.println(" *                   com/protomatter/syslog/classloader-warning.html        *");
            System.err.println(" *                                                                          *");
            System.err.println(" \\**************************************************************************/");
        }
    }


    /**
     *  Get the local hostname.
     *
     *@return    The localHostName value
     */
    public static InetAddress getLocalHostName()
    {
        return localHost;
    }


    /**
     *  Set the number of milliseconds a flush thread should wait before
     *  flushing output on each logger. If the interval is 0 (or negative) the
     *  thread will be stopped. The default is not to have a flush thread
     *  running. You cannot set it to less than 1000ms, except for setting it to
     *  0.
     *
     *@param  interval  The new flushThreadInterval value
     */
    public static void setFlushThreadInterval(long interval)
    {
        synchronized (instance)
        {
            if (flushThread != null)
            {
                flushThread.stopRunning();
                flushThread = null;
            }

            if (interval > 0)
            {
                if (interval < 1000)
                {
                    throw new IllegalArgumentException(
                            MessageFormat.format(getResourceString(MessageConstants.CANNOT_SET_FLUSH_INTERVAL_MESSAGE),
                            new Object[]{"1000", "0"}));
                }

                flushThread = new SyslogFlushThread(interval);
                flushThread.start();
            }
        }
    }


    /**
     *  Get the sleep interval of the logger flush thread. This method will
     *  either return the number of milliseconds between flush calls, or zero if
     *  there is no flush thread running.
     *
     *@return    The flushThreadInterval value
     */
    public static long getFlushThreadInterval()
    {
        synchronized (instance)
        {
            if (flushThread != null)
            {
                return flushThread.getSleepInterval();
            }
            return 0;
        }
    }


    /**
     *  Set the local hostname automatically. This sets the hostname to whatever
     *  <tt>InetAddress.getLocalHost()</tt> returns. This is called by <tt>
     *  SimpleSyslogTextFormatter</tt> when it is configured if it is supposed
     *  to log the host name.
     */
    public static void setLocalHostName()
    {
        try
        {
            setLocalHostName(InetAddress.getLocalHost());
        }
        catch (UnknownHostException x)
        {
            throw new IllegalStateException(getResourceString(MessageConstants.CANNOT_DETERMINE_HOSTNAME_MESSAGE));
        }
    }


    /**
     *  Set the local hostname.
     *
     *@param  host  The new localHostName value
     */
    public static void setLocalHostName(InetAddress host)
    {
        localHost = host;
    }


    /**
     *  Set the value of the master switch. If this switch is set to <TT>false
     *  </TT>, then all log requests will be ignored.
     *
     *@param  value  The new masterSwitch value
     */
    public static void setMasterSwitch(boolean value)
    {
        masterSwitch = value;
    }


    /**
     *  Add work to the given background queue. Queues are created (but not
     *  destroyed) dynamically.
     *
     *@param  queueName  The feature to be added to the Work attribute
     *@param  r          The feature to be added to the Work attribute
     */
    public static void addWork(String queueName, Runnable r)
    {
        WorkQueue queue = (WorkQueue)queueMap.get(queueName);
        if (queue == null)
        {
            synchronized (queueMap)
            {
                // re-check once in here to avoid race conditions.
                queue = (WorkQueue)queueMap.get(queueName);
                if (queue == null)
                {
                    queue = new WorkQueue("Syslog: " + queueName, 1);
                    queueMap.put(queueName, queue);
                }
            }
        }
        queue.addWork(r);
    }


    /**
     *  Initializes the logging facility. This gets called upon initialization,
     *  and can be called multiple times during a program. It checks the system
     *  properties file for "syslog.level" property and sets the default log
     *  mask to that value. Also, if "syslog.file" is defined, will log to that
     *  file.<P>
     *
     *  If the "<tt>Syslog.config.xml</tt>" system property is set,
     *  then the XML configuration helper is loaded and Syslog
     *  is configured.  See the <tt><a href="xml/SyslogXML.html#configure(org.jdom.Element)">SylogXML</a></tt>
     *  class for more information.
     */
    private final static synchronized void init()
    {
        resources = ResourceBundle.getBundle("com.protomatter.syslog.Syslog");

        String loglevelstr = System.getProperty("Syslog.level");
        if (loglevelstr != null)
        {
            // either give the value as the number or "DEBUG", etc...
            try
            {
                int loglevel = Integer.parseInt(loglevelstr);
                setLogMask(atOrAbove(loglevel));
            }
            catch (NumberFormatException x)
            {
                setLogMask(loglevelstr);
            }
        }

        // add the logger that writes to Stderr.
        Syslogger l = new PrintWriterLog("System.err");
        l.setName("PrintWriterLog.err");
        addLogger(l);

        // add a logger that writes to a file.
        String logfilestr = System.getProperty("Syslog.file");
        if (logfilestr != null)
        {
            FileLog fileLogger = new FileLog(new File(logfilestr));
            fileLogger.setName("FileLog.file");
            addLogger(fileLogger);
        }


        if (System.getProperty("Syslog.config.xml") != null)
        {
            // load the class manually so that we don't get this class
            // depending on the XML things.
            try
            {
                Class config = Class.forName("com.protomatter.syslog.xml.SyslogXML");
                Method configure = config.getMethod("configure", new Class[] { File.class, String.class, Boolean.TYPE });
                configure.invoke(null,
                    new Object[] { new File(System.getProperty("Syslog.config.xml")),
                        System.getProperty("Syslog.xml.parser"), new Boolean(true) });
            }
            catch (Exception x)
            {
                System.err.println(Syslog.getResourceString(MessageConstants.CANNOT_CONFIGURE_MESSAGE));
                x.printStackTrace();
            }
        }
    }


    /**
     *  Registers a new Syslogger object with Syslog.
     *
     *@param  log  The feature to be added to the Logger attribute
     */
    public final static synchronized void addLogger(Syslogger log)
    {
        loggers.add(log);
        loggerArray = (Syslogger[])loggers.toArray(new Syslogger[0]);
    }


    /**
     *  Deregisters a Syslogger object from Syslog.
     *
     *@param  log  The logger to remove
     *@return      true if the logger was removed
     */
    public final static synchronized boolean removeLogger(Syslogger log)
    {
        if (loggers.contains(log))
        {
            loggers.remove(loggers.indexOf(log));
            loggerArray = (Syslogger[])loggers.toArray(new Syslogger[0]);
            return true;
        }
        return false;
    }


    /**
     *  Deregisters all Syslogger objects.
     */
    public final static synchronized void removeAllLoggers()
    {
        loggers = new ArrayList();
        loggerArray = new Syslogger[0];
    }


    /**
     *  Returns an Enumeration of all Syslogger objects registered with Syslog.
     *
     *@return    The loggers value
     */
    public final static Iterator getLoggers()
    {
        return loggers.iterator();
    }


    /**
     *  Remove all the loggers and shut them down. Waits for all background
     *  queues to finish processing work. Any new messages received immediately
     *  after this method is called (before it completes) will be ignored.
     */
    public static synchronized void shutdown()
    {
        // turn everything off.
        masterSwitch = false;

        // remove loggers just in case.
        List list = loggers;
        removeAllLoggers();

        // wait for all background queues to shut down.
        Iterator i = queueMap.keySet().iterator();
        while (i.hasNext())
        {
            String queueName = (String)i.next();
            WorkQueue queue = (WorkQueue)queueMap.get(queueName);
            while ((queue.getQueueLength() > 0) || (queue.getNumWorkers() > 0))
            {
                try
                {
                    Thread.yield();
                    Thread.sleep(100);
                }
                catch (InterruptedException x)
                {
                    ;  // who cares.
                }
            }
        }

        // shut 'em down.  Loggers should flush and close files, etc.
        i = list.iterator();
        while (i.hasNext())
        {
            Syslogger l = (Syslogger)i.next();
            l.shutdown();
        }
    }


    /**
     *  Get a logger by name. If there are multiple loggers with the same name,
     *  the first one registered with that name will be returned. If there is no
     *  logger by that name, <tt>null</tt> is returned.
     *
     *@param  name  The name of the logger to get.
     *@return       The logger
     */
    public final static Syslogger getLogger(String name)
    {
        Iterator i = getLoggers();
        Syslogger l = null;
        while (i.hasNext())
        {
            l = (Syslogger)i.next();
            if (l.getName() == null && name == null)
            {
                return l;
            }
            if (l.getName() != null && name != null && l.getName().equals(name))
            {
                return l;
            }
        }
        return null;
    }


    /**
     *  Set the default mask for logging of messages. For example, to log all
     *  messages of type ERROR or greater, you would:
     *  Syslog.setLogMask(Syslog.atOrAbove(ERROR));
     *
     *@param  mask  The new logMask value
     */
    public final static void setLogMask(int mask)
    {
        if (mask == INHERIT_MASK)
        {
            throw new IllegalArgumentException(MessageFormat.format(
                    getResourceString(MessageConstants.CANNOT_SET_MASK_MESSAGE),
                    new Object[]{"INHERIT_MASK"}));
        }
        currentLogMask = mask;
    }


    /**
     *  Set the mask.
     *
     *@param  mask  The new logMask value
     *@see          #parseLogMask
     */
    public final static void setLogMask(String mask)
    {
        setLogMask(parseLogMask(mask));
    }


    /**
     *  Parse the mask. If the value passed in is the name of a level, like "
     *  <TT>INFO</TT> " or "<TT>WARNING</TT> " the mask will be set to <I>at or
     *  above</I> the listed level. If the string passed in is "<TT>INHERIT_MASK
     *  </TT>" then this policy will inherit the mask set in Syslog itself.<P>
     *
     *  You can also pass in a list of levels, separated by commas and with an "
     *  <TT>=</TT> " before each level to select non-contiguous groups of
     *  levels. For instance, passing in "<TT>=INFO,=ERROR</TT> " will catch
     *  messages only at the <TT>INFO</TT> and <TT>ERROR</TT> levels, and <I>
     *  NOTHING ELSE</I> .
     *
     *@param  mask  String representation of the mask
     *@return       Computed log mask
     */
    public final static int parseLogMask(String mask)
    {
        if (mask.equals("DEBUG"))
        {
            return Syslog.atOrAbove(Syslog.DEBUG);
        }
        else if (mask.equals("INFO"))
        {
            return Syslog.atOrAbove(Syslog.INFO);
        }
        else if (mask.equals("WARNING"))
        {
            return Syslog.atOrAbove(Syslog.WARNING);
        }
        else if (mask.equals("ERROR"))
        {
            return Syslog.atOrAbove(Syslog.ERROR);
        }
        else if (mask.equals("FATAL"))
        {
            return Syslog.atOrAbove(Syslog.FATAL);
        }
        else if (mask.equals("INHERIT_MASK"))
        {
            return Syslog.INHERIT_MASK;
        }
        else if (mask.startsWith("="))
        {
            int newMask = 0;
            StringTokenizer st = new StringTokenizer(mask, ", ");
            while (st.hasMoreTokens())
            {
                String elt = st.nextToken().substring(1);
                if (elt.equals("DEBUG"))
                {
                    newMask |= Syslog.DEBUG;
                }
                else if (elt.equals("INFO"))
                {
                    newMask |= Syslog.INFO;
                }
                else if (elt.equals("WARNING"))
                {
                    newMask |= Syslog.WARNING;
                }
                else if (elt.equals("ERROR"))
                {
                    newMask |= Syslog.ERROR;
                }
                else if (elt.equals("FATAL"))
                {
                    newMask |= Syslog.FATAL;
                }
            }
            return newMask;
        }
        else
        {
            throw new IllegalArgumentException(getResourceString(MessageConstants.INVALID_MASK_MESSAGE));
        }
    }


    /**
     *  Get a string representation of the current master log mask.
     *
     *@return    The logMaskAsString value
     */
    public static String getLogMaskAsString()
    {
        return getLogMaskAsString(currentLogMask);
    }


    /**
     *  Get a string representation of the given log mask.
     *
     *@param  logMask  The parsed log mask
     *@return          The log mask rendered as a string
     */
    public static String getLogMaskAsString(int logMask)
    {
        if (logMask == Syslog.INHERIT_MASK)
        {
            return "INHERIT_MASK";
        }
        else if (logMask == Syslog.atOrAbove(Syslog.DEBUG))
        {
            return "DEBUG";
        }
        else if (logMask == Syslog.atOrAbove(Syslog.INFO))
        {
            return "INFO";
        }
        else if (logMask == Syslog.atOrAbove(Syslog.WARNING))
        {
            return "WARNING";
        }
        else if (logMask == Syslog.atOrAbove(Syslog.ERROR))
        {
            return "ERROR";
        }
        else if (logMask == Syslog.atOrAbove(Syslog.FATAL))
        {
            return "FATAL";
        }

        // bah... list of levels to pay attention to.
        Vector list = new Vector(5);
        if (inMask(Syslog.DEBUG, logMask))
        {
            list.addElement("=DEBUG");
        }
        if (inMask(Syslog.INFO, logMask))
        {
            list.addElement("=INFO");
        }
        if (inMask(Syslog.WARNING, logMask))
        {
            list.addElement("=WARNING");
        }
        if (inMask(Syslog.ERROR, logMask))
        {
            list.addElement("=ERROR");
        }
        if (inMask(Syslog.FATAL, logMask))
        {
            list.addElement("=FATAL");
        }
        StringBuffer b = new StringBuffer(32);
        Enumeration e = list.elements();
        while (e.hasMoreElements())
        {
            b.append(e.nextElement());
            if (e.hasMoreElements())
            {
                b.append(",");
            }
        }
        return b.toString();
    }


    /**
     *  Check if the given level is covered by the given mask.
     *
     *@param  level  Log message severity
     *@param  mask   Log mask to compare it to
     *@return        true if the given level is "in" the mask.
     */
    public final static boolean inMask(int level, int mask)
    {
        return ((level & mask) > 0);
    }


    /**
     *  Get the default mask for logging of messages.
     *
     *@return    The logMask value
     */
    public final static int getLogMask()
    {
        return currentLogMask;
    }


    /**
     *@param  level  The minimum severity of messages
     *@return        the log mask for "all logs at or above this level"
     */
    public final static int atOrAbove(int level)
    {
        if (level == DEBUG)
        {
            return DEBUG | INFO | WARNING | ERROR | FATAL;
        }
        if (level == INFO)
        {
            return INFO | WARNING | ERROR | FATAL;
        }
        if (level == WARNING)
        {
            return WARNING | ERROR | FATAL;
        }
        if (level == ERROR)
        {
            return ERROR | FATAL;
        }
        if (level == FATAL)
        {
            return FATAL;
        }
        throw new IllegalArgumentException(
                MessageFormat.format(getResourceString(MessageConstants.ILLEGAL_ARGUMENT_MESSAGE),
                new Object[]{String.valueOf(level)}));
    }


    /**
     *  Log a message. Logs to the <tt>DEFAULT_CHANNEL</tt> channel.
     *
     *@param  host    The machine the log entry originated on.
     *@param  logger  The object which is perfoming the log. It's class name
     *      will be extracted and added to the log.
     *@param  msg     A short message describing the log entry.
     *@param  detail  A longer message with more information (can be null).
     *@param  level   The level of the log entry.
     */
    public final static void log(InetAddress host, Object logger, Object msg, Object detail, int level)
    {
        log(host, logger, null, msg, detail, level,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Log a message. Logs to the <tt>DEFAULT_CHANNEL</tt> channel.
     *
     *@param  logger  The object which is perfoming the log. It's class name
     *      will be extracted and added to the log.
     *@param  msg     A short message describing the log entry.
     *@param  detail  A longer message with more information (can be null).
     *@param  level   The level of the log entry.
     */
    public final static void log(Object logger, Object msg, Object detail, int level)
    {
        log(localHost, logger, null, msg, detail, level,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Log a message. If <tt>detail</tt> is a subclass of <tt>Throwable</tt>
     *  it's stack trace will be included in the output. If you want to log to a
     *  channel other than <tt>DEFAULT_CHANNEL</tt> you will have to use this
     *  method or one of the <tt><i>xxx</i> ToChannel(...)</tt> methods.
     *
     *@param  logger   The object which is perfoming the log. It's class name
     *      will be extracted and added to the log.
     *@param  channel  The log channel to write to.
     *@param  msg      A short message describing the log entry.
     *@param  detail   A longer message with more information (can be null).
     *@param  level    The level of the log entry.
     */
    public final static void log(Object logger, Object channel,
        Object msg, Object detail, int level)
    {
        log(localHost, logger, channel, msg, detail, level,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Log a message. If <tt>detail</tt> is a subclass of <tt>Throwable</tt>
     *  it's stack trace will be included in the output. If you want to log to a
     *  channel other than <tt>DEFAULT_CHANNEL</tt> you will have to use this
     *  method or one of the <tt><i>xxx</i> ToChannel(...)</tt> methods.
     *
     *@param  host     The host the log message originated on.
     *@param  logger   The object which is perfoming the log. It's class name
     *      will be extracted and added to the log.
     *@param  channel  The log channel to write to.
     *@param  msg      A short message describing the log entry.
     *@param  detail   A longer message with more information (can be null).
     *@param  level    The level of the log entry.
     */
    public final static void log(InetAddress host, Object logger, Object channel,
        Object msg, Object detail, int level)
    {
        log(host, logger, channel, msg, detail, level,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    final static String[] convertChannelObject(Object channel)
    {
        if (channel == null)
        {
            return defaultChannelList;
        }

        if (channel instanceof String)
        {
            return new String[]{(String)channel};
        }
        else if (channel instanceof String[])
        {
            return (String[])channel;
        }
        else if (channel instanceof Object[])
        {
            Object array[] = (Object[])channel;
            String channels[] = new String[array.length];
            for (int i = 0; i < array.length; i++)
            {
                channels[i] = array[i].toString();
            }
            return channels;
        }

        return new String[]{channel.toString()};
    }


    /**
     *  Log a message. <I>This method should not be called unless it's by a
     *  remote log adapter.</I>
     *
     *@param  host             The host the log message originated on.
     *@param  logger           The object which is perfoming the log. It's class
     *      name will be extracted and added to the log.
     *@param  msg              A short message describing the log entry.
     *@param  detail           A longer message with more information (can be
     *      null).
     *@param  level            The level of the log entry.
     *@param  thread           The thread making the log call (null if remote)
     *@param  threadName       The name of the thread making the log call.
     *@param  messageSendTime  The time the message was sent.
     *@param  incomingChannel  Message channel
     */
    public final static void log(InetAddress host, Object logger,
        Object incomingChannel, Object msg, Object detail, int level,
        Thread thread, String threadName, long messageSendTime)
    {
        log(host, logger, incomingChannel, msg, detail, level,
            thread, threadName, messageSendTime, 1);
    }


    /**
     *  Log a message. <I>This method should not be called unless it's by a
     *  remote log adapter.</I>
     *
     *@param  host             The host the log message originated on.
     *@param  logger           The object which is perfoming the log. It's class
     *      name will be extracted and added to the log.
     *@param  msg              A short message describing the log entry.
     *@param  detail           A longer message with more information (can be
     *      null).
     *@param  level            The level of the log entry.
     *@param  thread           The thread making the log call (null if remote)
     *@param  threadName       The name of the thread making the log call.
     *@param  messageSendTime  The time the message was sent.
     *@param  incomingChannel  Channel
     *@param  traceDepth       Caller's depth in call stack, for computing
     *                         caller and method names at runtime.
     */
    public final static void log(InetAddress host, Object logger,
        Object incomingChannel, Object msg, Object detail, int level,
        Thread thread, String threadName, long messageSendTime,
        int traceDepth)
    {
        if (!masterSwitch)
        {
            return;
        }

        String methodName = null;
        int lineNumber = StackTraceInfo.LINE_NUMBER_UNKNOWN;
        traceDepth++;

        Class loggerClass = (logger == null) ? null : logger.getClass();

        // get logger name
        String classname = null;
        if (!alwaysComputeCaller && !computeCaller)
        {
            if (logger == null)
            {
                classname = Q_MARK;
            }
            else
            {
                if (loggerClass == Class.class)
                {
                    classname = ((Class)logger).getName();
                }
                else if (loggerClass == String.class)
                {
                    classname = (String)logger;
                }
                else
                {
                    classname = logger.getClass().getName();
                }
            }
        }

        // determine what channel(s) we should
        // send this log message to
        String channels[] = null;
        if (incomingChannel != null)
        {  // specified channel in the call to log(...)

            channels = convertChannelObject(incomingChannel);
        }
        else
        {  // no channel(s) specified

            // check if the logger is channel aware.  If so,
            // ask it what channels it prefers.
            if (loggerClass == SyslogChannelAware.class)
            {
                channels = convertChannelObject(((SyslogChannelAware)logger).getSyslogChannel());
            }
            else
            {
                channels = defaultChannelList;
            }
        }

        // then log to all objects in loggers
        int loggerIndex = 0;
        int channelIndex = channels.length;
        Syslogger[] localLoggerArray = loggerArray;
        int size = localLoggerArray.length;
        Syslogger log = null;
        SyslogMessage sm = null;

        for (; --channelIndex >= 0; )
        {
            // outer loop is through the channel array
            sm = new SyslogMessage(host, messageSendTime,
                channels[channelIndex], logger, 
                classname, msg, detail, level, thread, threadName,
                methodName, lineNumber);
            for (loggerIndex = size; --loggerIndex >= 0; )
            {
                // inner loop is through the loggers
                log = localLoggerArray[loggerIndex];
                try
                {
                    // should we compute the calling method
                    if (alwaysComputeCaller && (sm.callingMethodName == null))
                    {
                        StackTraceInfo info = StackTraceUtil.whereAmI(traceDepth);
                        if (info != null)
                        {
                            sm.loggerClassname = info.className;
                            sm.callingMethodName = info.methodName;
                            sm.callingMethodLineNumber = info.lineNumber;
                        }
                    }

                    if (log.getPolicy().shouldLog(sm))
                    {
                        // should we compute the calling method
                        if (computeCaller && (sm.callingMethodName == null))
                        {
                            StackTraceInfo info = StackTraceUtil.whereAmI(traceDepth);
                            if (info != null)
                            {
                                sm.loggerClassname = info.className;
                                sm.callingMethodName = info.methodName;
                                sm.callingMethodLineNumber = info.lineNumber;
                            }
                        }

                        log.log(sm);
                    }
                }
                catch (Throwable t)
                {
                    System.err.println(MessageFormat.format(
                            getResourceString(MessageConstants.WRITE_PROBLEM),
                            new Object[]{((log.getName() == null) ? log.toString() : log.getName()),
                            t.toString()}));
                    t.printStackTrace();
                }
            }
        }
    }


    /**
     *  Logs an exception for the given object. The log level will be ERROR.
     *
     *@param  host    hostname
     *@param  logger  Calling object
     *@param  e       exception to log
     */
    public final static void log(InetAddress host, Object logger, Throwable e)
    {
        log(host, logger, null, e.toString(), e, ERROR,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs an exception for the given object at a given level.
     *
     *@param  host    hostname
     *@param  logger  Calling object
     *@param  e       exception to log
     *@param  level   Severity to log the message at
     */
    public final static void log(InetAddress host, Object logger, Throwable e, int level)
    {
        log(host, logger, null, e.toString(), e, level,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs an exception for the given object. The log level will be ERROR.
     *
     *@param  logger  Calling object
     *@param  e       Exception to log
     */
    public final static void log(Object logger, Throwable e)
    {
        log(localHost, logger, null, e.toString(), e, ERROR,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs an exception for the given object at a given level.
     *
     *@param  logger  Calling object
     *@param  e       Exception to log
     *@param  level   Severity to log the message at
     */
    public final static void log(Object logger, Throwable e, int level)
    {
        log(localHost, logger, null, e.toString(), e, level,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a debug message, which will be converted through toString().
     *
     *@param  host     hostname
     *@param  logger  Calling object
     *@param  message  Message to log
     */
    public final static void debug(InetAddress host, Object logger, Object message)
    {
        log(host, logger, null, message, null, DEBUG);
        log(host, logger, null, message, null, DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a debug message with a detail object, both of which will be
     *  converted through toString().
     *
     *@param  host     hostname
     *@param  logger  Calling object
     *@param  message  Message to log
     *@param  detail   Extended message or exception
     */
    public final static void debug(InetAddress host, Object logger, Object message, Object detail)
    {
        log(host, logger, null, message, detail, DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a debug message to the given channel.
     *
     *@param  host     hostname
     *@param  logger  Calling object
     *@param  message  Message to log
     */
    public final static void debugToChannel(InetAddress host, Object logger, Object channel, Object message)
    {
        log(host, logger, channel, message, null, DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a debug message with a detail object to the given channel.
     *
     *@param  host     hostname
     *@param  logger  Calling object
     *@param  channel Channel to log message on
     *@param  message  Message to log
     *@param  detail   Extended message or exception
     */
    public final static void debugToChannel(InetAddress host, Object logger, Object channel, Object message, Object detail)
    {
        log(host, logger, channel, message, detail, DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a debug message, which will be converted through toString().
     *
     *@param  logger  Calling object
     *@param  message  Message to log
     */
    public final static void debug(Object logger, Object message)
    {
        log(localHost, logger, null, message, null, DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a debug message with a detail object, both of which will be
     *  converted through toString().
     *
     *@param  logger  Calling object
     *@param  message  Message to log
     *@param  detail   Extended message or exception
     */
    public final static void debug(Object logger, Object message, Object detail)
    {
        log(localHost, logger, null, message, detail, DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a debug message to the given channel.
     *
     *@param  logger  Calling object
     *@param  message  Message to log
     *@param  channel   Channel to log message on
     */
    public final static void debugToChannel(Object logger, Object channel, Object message)
    {
        log(localHost, logger, channel, message, null, DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a debug message with a detail object to the given channel.
     *
     *@param  logger  Calling object
     *@param  channel   Channel to log message on
     *@param  message  Message to log
     *@param  detail   Extended message or exception
     */
    public final static void debugToChannel(Object logger, Object channel, Object message, Object detail)
    {
        log(localHost, logger, channel, message, detail, DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }



    /**
     *  Logs a info message, which will be converted through toString().
     *
     *@param  host  Hostname
     *@param  logger  Calling object
     *@param  message  Message to log
     */
    public final static void info(InetAddress host, Object logger, Object message)
    {
        log(host, logger, null, message, null, INFO,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a info message with a detail object, both of which will be
     *  converted through toString().
     *
     *@param  host  Hostname
     *@param  logger  Calling object
     *@param  message  Message to log
     *@param  detail   Extended message or exception
     */
    public final static void info(InetAddress host, Object logger, Object message, Object detail)
    {
        log(host, logger, null, message, detail, INFO,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs an info message to the given channel.
     *
     *@param  host  Hostname
     *@param  logger  Calling object
     *@param  channel  Channel to log message on
     *@param  message  Message to log
     */
    public final static void infoToChannel(InetAddress host, Object logger, Object channel, Object message)
    {
        log(host, logger, channel, message, null, INFO,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs an info message with a detail object to the given channel.
     *
     *@param  host  Hostname
     *@param  logger  Calling object
     *@param  channel  Channel to log message on
     *@param  message  Message to log
     *@param  detail   Extended message or exception
     */
    public final static void infoToChannel(InetAddress host, Object logger, Object channel, Object message, Object detail)
    {
        log(host, logger, channel, message, detail, INFO,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a info message, which will be converted through toString().
     *
     *@param  logger  Calling object
     *@param  message  Message to log
     */
    public final static void info(Object logger, Object message)
    {
        log(localHost, logger, null, message, null, INFO,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a info message with a detail object, both of which will be
     *  converted through toString().
     *
     *@param  logger  Calling object
     *@param  message  Message to log
     *@param  detail   Extended message or exception
     */
    public final static void info(Object logger, Object message, Object detail)
    {
        log(localHost, logger, null, message, detail, INFO,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs an info message to the given channel.
     *
     *@param  logger  Calling object
     *@param  channel  Channel to log message on
     *@param  message  Message to log
     */
    public final static void infoToChannel(Object logger, Object channel, Object message)
    {
        log(localHost, logger, channel, message, null, INFO,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs an info message with a detail object to the given channel.
     *
     *@param  logger  Calling object
     *@param  channel  Channel to log message on
     *@param  message  Message to log
     *@param  detail   Extended message or exception
     */
    public final static void infoToChannel(Object logger, Object channel, Object message, Object detail)
    {
        log(localHost, logger, channel, message, detail, INFO,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a warning message, which will be converted through toString().
     *
     *@param  host  Hostname
     *@param  logger  Calling object
     *@param  message  Message to log
     */
    public final static void warning(InetAddress host, Object logger, Object message)
    {
        log(host, logger, null, message, null, WARNING,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a warning message with a detail object, both of which will be
     *  converted through toString().
     *
     *@param  host  Hostname
     *@param  logger  Calling object
     *@param  message  Message to log
     *@param  detail   Extended message or exception
     */
    public final static void warning(InetAddress host, Object logger, Object message, Object detail)
    {
        log(host, logger, null, message, detail, WARNING,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a warning message to the given channel.
     *
     *@param  host  Hostname
     *@param  logger  Calling object
     *@param  channel  Channel to log message on
     *@param  message  Message to log
     */
    public final static void warningToChannel(InetAddress host, Object logger, Object channel, Object message)
    {
        log(host, logger, channel, message, null, WARNING,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a warning message with a detail object to the given channel.
     *
     *@param  host  Hostname
     *@param  logger  Calling object
     *@param  channel  Channel to log message on
     *@param  message  Message to log
     *@param  detail   Extended message or exception
     */
    public final static void warningToChannel(InetAddress host, Object logger, Object channel, Object message, Object detail)
    {
        log(host, logger, channel, message, detail, WARNING,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a warning message, which will be converted through toString().
     *
     *@param  logger  Calling object
     *@param  message  Message to log
     */
    public final static void warning(Object logger, Object message)
    {
        log(localHost, logger, null, message, null, WARNING,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a warning message with a detail object, both of which will be
     *  converted through toString().
     *
     *@param  logger  Calling object
     *@param  message  Message to log
     *@param  detail   Extended message or exception
     */
    public final static void warning(Object logger, Object message, Object detail)
    {
        log(localHost, logger, null, message, detail, WARNING,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a warning message to the given channel.
     *
     *@param  logger  Calling object
     *@param  channel  Channel to log message on
     *@param  message  Message to log
     */
    public final static void warningToChannel(Object logger, Object channel, Object message)
    {
        log(localHost, logger, channel, message, null, WARNING,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a warning message with a detail object to the given channel.
     *
     *@param  logger  Calling object
     *@param  channel  Channel to log message on
     *@param  message  Message to log
     *@param  detail   Extended message or exception
     */
    public final static void warningToChannel(Object logger, Object channel, Object message, Object detail)
    {
        log(localHost, logger, channel, message, detail, WARNING,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }



    /**
     *  Logs a error message, which will be converted through toString().
     *
     *@param  host  Hostname
     *@param  logger  Calling object
     *@param  message  Message to log
     */
    public final static void error(InetAddress host, Object logger, Object message)
    {
        log(host, logger, null, message, null, ERROR,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a error message with a detail object, both of which will be
     *  converted through toString().
     *
     *@param  host  Hostname
     *@param  logger  Calling object
     *@param  message  Message to log
     *@param  detail   Extended message or exception
     */
    public final static void error(InetAddress host, Object logger, Object message, Object detail)
    {
        log(host, logger, null, message, detail, ERROR,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs an error message to the given channel.
     *
     *@param  host  Hostname
     *@param  logger  Calling object
     *@param  channel  Channel to log message on
     *@param  message  Message to log
     */
    public final static void errorToChannel(InetAddress host, Object logger, Object channel, Object message)
    {
        log(host, logger, channel, message, null, ERROR,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs an error message with a detail object to the given channel.
     *
     *@param  host  Hostname
     *@param  logger  Calling object
     *@param  channel  Channel to log message on
     *@param  message  Message to log
     *@param  detail   Extended message or exception
     */
    public final static void errorToChannel(InetAddress host, Object logger, Object channel, Object message, Object detail)
    {
        log(host, logger, channel, message, detail, ERROR,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a error message, which will be converted through toString().
     *
     *@param  logger  Calling object
     *@param  message  Message to log
     */
    public final static void error(Object logger, Object message)
    {
        log(localHost, logger, null, message, null, ERROR,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a error message with a detail object, both of which will be
     *  converted through toString().
     *
     *@param  logger  Calling object
     *@param  message  Message to log
     *@param  detail   Extended message or exception
     */
    public final static void error(Object logger, Object message, Object detail)
    {
        log(localHost, logger, null, message, detail, ERROR,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs an error message to the given channel.
     *
     *@param  logger  Calling object
     *@param  channel  Channel to log message on
     *@param  message  Message to log
     */
    public final static void errorToChannel(Object logger, Object channel, Object message)
    {
        log(localHost, logger, channel, message, null, ERROR,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs an error message with a detail object to the given channel.
     *
     *@param  logger  Calling object
     *@param  channel  Channel to log message on
     *@param  message  Message to log
     *@param  detail   Extended message or exception
     */
    public final static void errorToChannel(Object logger, Object channel, Object message, Object detail)
    {
        log(localHost, logger, channel, message, detail, ERROR,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }



    /**
     *  Logs a fatal message, which will be converted through toString().
     *
     *@param  host  Hostname
     *@param  logger  Calling object
     *@param  message  Message to log
     */
    public final static void fatal(InetAddress host, Object logger, Object message)
    {
        log(host, logger, null, message, null, FATAL,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a fatal message with a detail object, both of which will be
     *  converted through toString().
     *
     *@param  host  Hostname
     *@param  logger  Calling object
     *@param  message  Message to log
     *@param  detail   Extended message or exception
     */
    public final static void fatal(InetAddress host, Object logger, Object message, Object detail)
    {
        log(host, logger, null, message, detail, FATAL,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a fatal message to the given channel.
     *
     *@param  host  Hostname
     *@param  logger  Calling object
     *@param  channel  Channel to log message on
     *@param  message  Message to log
     */
    public final static void fatalToChannel(InetAddress host, Object logger, Object channel, Object message)
    {
        log(host, logger, channel, message, null, FATAL,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a fatal message with a detail object to the given channel.
     *
     *@param  host  Hostname
     *@param  logger  Calling object
     *@param  channel  Channel to log message on
     *@param  message  Message to log
     *@param  detail   Extended message or exception
     */
    public final static void fatalToChannel(InetAddress host, Object logger, Object channel, Object message, Object detail)
    {
        log(host, logger, channel, message, detail, FATAL,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a fatal message, which will be converted through toString().
     *
     *@param  logger  Calling object
     *@param  message  Message to log
     */
    public final static void fatal(Object logger, Object message)
    {
        log(localHost, logger, null, message, null, FATAL,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a fatal message with a detail object, both of which will be
     *  converted through toString().
     *
     *@param  logger  Calling object
     *@param  message  Message to log
     *@param  detail   Extended message or exception
     */
    public final static void fatal(Object logger, Object message, Object detail)
    {
        log(localHost, logger, null, message, detail, FATAL,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a fatal message to the given channel.
     *
     *@param  logger  Calling object
     *@param  channel  Channel to log message on
     *@param  message  Message to log
     */
    public final static void fatalToChannel(Object logger, Object channel, Object message)
    {
        log(localHost, logger, channel, message, null, FATAL,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a fatal message with a detail object to the given channel.
     *
     *@param  logger  Calling object
     *@param  channel  Channel to log message on
     *@param  message  Message to log
     *@param  detail   Extended message or exception
     */
    public final static void fatalToChannel(Object logger, Object channel, Object message, Object detail)
    {
        log(localHost, logger, channel, message, detail, FATAL,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }


    /**
     *  Logs a breadcrumb at the debug level.  The breadcrumb
     *  includes information from a <tt><a href="../util/StackTraceInfo.html">StackTraceInfo</a></tt>
     *  object.
     */
    public final static void crumb()
    {
        StackTraceInfo trace = StackTraceUtil.whereAmI(1);
        log(localHost, trace.className, null, 
            MessageFormat.format(getResourceString(MessageConstants.CRUMB_MESSAGE),
            new Object[] { trace }),
            (Object)null, DEBUG, Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *  Logs a breadcrumb at the debug level.  The breadcrumb
     *  includes information from a <tt><a href="../util/StackTraceInfo.html">StackTraceInfo</a></tt>
     *  object.
     */
    public final static void crumb(Object logger)
    {
        log(localHost, logger, (Object)null, 
            MessageFormat.format(getResourceString(MessageConstants.CRUMB_MESSAGE),
            new Object[] { Syslog.whereAmI(1) }),
            (Object)null, DEBUG, Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *  Logs a breadcrumb at the debug level.
     */
    public final static void crumb(Object logger, Object channel)
    {
        log(localHost, logger, channel, 
            MessageFormat.format(getResourceString(MessageConstants.CRUMB_MESSAGE),
            new Object[] { Syslog.whereAmI(1) }),
            null, DEBUG, Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    static String whereAmI(int depth)
    {
        StackTraceInfo trace = StackTraceUtil.whereAmI(depth +1);
        return trace.toString();
    }

    /**
     *  Determine if the current default syslog mask would allow the given level
     *  of message to be logged. This is not an absolute method of determining
     *  if a message would be logged by some registered logger. If there is a
     *  registered logger who is not inheriting the mask from <tt>Syslog</tt>
     *  itself, it may (or may not) have logged the message.
     *
     *@param  level  Message severity to test
     *@return        True if it is likely to be logged
     *@deprecated
     */
    public final static boolean canLog(int level)
    {
        return inMask(level, currentLogMask);
    }


    /**
     *  Determine if the current syslog mask would allow a message at the <tt>
     *  DEBUG</tt> level to be logged.
     *
     *@return        True if it is likely to be logged
     *@deprecated
     */
    public final static boolean canDebug()
    {
        return canLog(DEBUG);
    }


    /**
     *  Determine if the current syslog mask would allow a message at the <tt>
     *  INFO</tt> level to be logged.
     *
     *@return        True if it is likely to be logged
     *@deprecated
     */
    public final static boolean canInfo()
    {
        return canLog(INFO);
    }


    /**
     *  Determine if the current syslog mask would allow a message at the <tt>
     *  WARNING</tt> level to be logged.
     *
     *@return        True if it is likely to be logged
     *@deprecated
     */
    public final static boolean canWarning()
    {
        return canLog(WARNING);
    }


    /**
     *  Determine if the current syslog mask would allow a message at the <tt>
     *  ERROR</tt> level to be logged.
     *
     *@return        True if it is likely to be logged
     *@see       #canLog(int)
     *@deprecated
     */
    public final static boolean canError()
    {
        return canLog(ERROR);
    }


    /**
     *  Determine if the current syslog mask would allow a message at the <tt>
     *  FATAL</tt> level to be logged.
     *
     *@return        True if it is likely to be logged
     *@see       #canLog(int)
     *@deprecated
     */
    public final static boolean canFatal()
    {
        return canLog(FATAL);
    }


    /**
     *  Determine if it's likely that someone will listen to a message from the
     *  given logger at the given level.
     *
     *@param  logger  Calling object
     *@return        True if it is likely to be logged
     *@see            #mightLog(Object, int, Object)
     *@deprecated
     */
    public final static boolean mightLog(Object logger, int level)
    {
        return mightLog(logger, level, null);
    }


    /**
     *  Determine if it's likely that someone will listen to a debug message
     *  from the given logger.
     *
     *@param  logger  Calling object
     *@return        True if it is likely to be logged
     *@see            #mightLog(Object, int, Object)
     *@deprecated
     */
    public final static boolean mightDebug(Object logger)
    {
        return mightLog(logger, DEBUG, null);
    }


    /**
     *  Determine if it's likely that someone will listen to an info message
     *  from the given logger.
     *
     *@param  logger  Calling object
     *@return        True if it is likely to be logged
     *@see            #mightLog(Object, int, Object)
     *@deprecated
     */
    public final static boolean mightInfo(Object logger)
    {
        return mightLog(logger, INFO, null);
    }


    /**
     *  Determine if it's likely that someone will listen to a warning message
     *  from the given logger.
     *
     *@param  logger  Calling object
     *@return        True if it is likely to be logged
     *@see            #mightLog(Object, int, Object)
     *@deprecated
     */
    public final static boolean mightWarning(Object logger)
    {
        return mightLog(logger, WARNING, null);
    }


    /**
     *  Determine if it's likely that someone will listen to an error message
     *  from the given logger.
     *
     *@param  logger  Calling object
     *@return        True if it is likely to be logged
     *@see            #mightLog(Object, int, Object)
     *@deprecated
     */
    public final static boolean mightError(Object logger)
    {
        return mightLog(logger, ERROR, null);
    }


    /**
     *  Determine if it's likely that someone will listen to a fatal message
     *  from the given logger.
     *
     *@param  logger  Calling object
     *@return        True if it is likely to be logged
     *@see            #mightLog(Object, int, Object)
     *@deprecated
     */
    public final static boolean mightFatal(Object logger)
    {
        return mightLog(logger, FATAL, null);
    }


    /**
     *  Determine if it's likely that someone will listen to a debug message
     *  from the given logger on the given channel(s).
     *
     *@param  logger  Calling object
     *@param  channel  Channel name or list
     *@return        True if it is likely to be logged
     *@see             #mightLog(Object, int, Object)
     *@deprecated
     */
    public final static boolean mightDebug(Object logger, Object channel)
    {
        return mightLog(logger, DEBUG, channel);
    }


    /**
     *  Determine if it's likely that someone will listen to an info message
     *  from the given logger on the given channel(s).
     *
     *@param  logger  Calling object
     *@param  channel  Channel name or list
     *@return        True if it is likely to be logged
     *@see             #mightLog(Object, int, Object)
     *@deprecated
     */
    public final static boolean mightInfo(Object logger, Object channel)
    {
        return mightLog(logger, INFO, channel);
    }


    /**
     *  Determine if it's likely that someone will listen to a warning message
     *  from the given logger on the given channel(s).
     *
     *@param  logger  Calling object
     *@param  channel  Channel name or list
     *@return        True if it is likely to be logged
     *@see             #mightLog(Object, int, Object)
     *@deprecated
     */
    public final static boolean mightWarning(Object logger, Object channel)
    {
        return mightLog(logger, WARNING, channel);
    }


    /**
     *  Determine if it's likely that someone will listen to an error message
     *  from the given logger on the given channel(s).
     *
     *@param  logger  Calling object
     *@param  channel  Channel name or list
     *@return        True if it is likely to be logged
     *@see             #mightLog(Object, int, Object)
     *@deprecated
     */
    public final static boolean mightError(Object logger, Object channel)
    {
        return mightLog(logger, ERROR, channel);
    }


    /**
     *  Determine if it's likely that someone will listen to a fatal message
     *  from the given logger on the given channel(s).
     *
     *@param  logger  Calling object
     *@param  channel  Channel name or list
     *@return        True if it is likely to be logged
     *@see             #mightLog(Object, int, Object)
     *@deprecated
     */
    public final static boolean mightFatal(Object logger, Object channel)
    {
        return mightLog(logger, FATAL, channel);
    }


    /**
     *  This method has been deprecated.  You should use the
     *  <tt><a href="../util/Debug.html">com.protomatter.util.Debug</a></tt>
     *  class instead.
     *
     *@param  logger  Calling object
     *@param  level    Message severity
     *@param  channel  Channel to log message on
     *@return        True if it is likely to be logged
     *@deprecated
     */
    public final static boolean mightLog(Object logger, int level, Object channel)
    {
        int theLevel = level;
        Object theLogger = logger;

        // determine channel list.
        if (channel == null)
        {
            if (logger instanceof SyslogChannelAware)
            {
                channel = ((SyslogChannelAware)logger).getSyslogChannel();
            }
            else
            {
                channel = new Object[]{DEFAULT_CHANNEL};
            }
        }

        // make up a nice list of channels.
        String[] list = null;
        if (channel instanceof String)
        {
            list = new String[]{(String)channel};
        }
        else if (channel instanceof String[])
        {
            list = (String[])channel;
        }

        // ask each logger if they care.
        int i = loggers.size();
        int j = 0;
        Syslogger l = null;
        for (; --i >= 0; )
        {
            l = (Syslogger)loggers.get(i);
            for (j = list.length; --j >= 0; )
            {
                if (l.mightLog(theLogger, level, list[j]))
                {
                    return true;
                }  // somebody cares
            }
        }

        // nobody wants it.
        return false;
    }


    /**
     *  Pass-through to <TT>mightLog(Object logger, int level)</TT> .
     *
     *@param  logger  Calling object
     *@param  level   Message severity
     *@return        True if it is likely to be logged
     *@see            #mightLog(Object, int)
     *@deprecated
     */
    public final static boolean canLog(Object logger, int level)
    {
        return mightLog(logger, level);
    }


    /**
     *  Pass-through to <TT>mightDebug(Object logger)</TT> .
     *
     *@param  logger  Calling object
     *@return        True if it is likely to be logged
     *@deprecated
     *@see            #mightDebug(Object)
     */
    public final static boolean canDebug(Object logger)
    {
        return canLog(logger, DEBUG);
    }


    /**
     *  Pass-through to <TT>mightInfo(Object logger)</TT> .
     *
     *@param  logger  Calling object
     *@return        True if it is likely to be logged
     *@deprecated
     *@see            #mightInfo(Object)
     */
    public final static boolean canInfo(Object logger)
    {
        return canLog(logger, INFO);
    }


    /**
     *  Pass-through to <TT>mightWarning(Object logger)</TT> .
     *
     *@param  logger  Calling object
     *@return        True if it is likely to be logged
     *@deprecated
     *@see            #mightWarning(Object)
     */
    public final static boolean canWarning(Object logger)
    {
        return canLog(logger, WARNING);
    }


    /**
     *  Pass-through to <TT>mightError(Object logger)</TT> .
     *
     *@param  logger  Calling object
     *@return        True if it is likely to be logged
     *@deprecated
     *@see            #mightError(Object)
     */
    public final static boolean canError(Object logger)
    {
        return canLog(logger, ERROR);
    }


    /**
     *  Pass-through to <TT>mightFatal(Object logger)</TT> .
     *
     *@param  logger  Calling object
     *@return        True if it is likely to be logged
     *@deprecated
     *@see            #mightFatal(Object)
     */
    public final static boolean canFatal(Object logger)
    {
        return canLog(logger, FATAL);
    }


    /**
     *  Flush output to all loggers.
     */
    public static void flush()
    {
        // run through the loggers and have them each
        // flush their output.
        Iterator i = Syslog.getLoggers();
        while (i.hasNext())
        {
            Syslogger logger = (Syslogger)i.next();
            try
            {
                logger.flush();
            }
            catch (Throwable t)
            {
                System.err.println(MessageFormat.format(
                        getResourceString(MessageConstants.FLUSH_PROBLEM_MESSAGE),
                        new Object[]{logger.getName(), t.toString()}));
                t.printStackTrace();
            }
        }
    }


    /**
     *  Get the resource bundle associated with Syslog.
     *
     *@return    The resources value
     */
    public final static ResourceBundle getResources()
    {
        return resources;
    }


    /**
     *  Get the value of the given resource name associated with Syslog.
     *
     *@param  name  Resource name to get
     *@return       The resourceString value
     */
    public final static String getResourceString(String name)
    {
        return getResources().getString(name);
    }


    private static class SyslogFlushThread
             extends Thread
    {
        private long sleepInterval;
        private boolean stop = false;


        public SyslogFlushThread(long sleepInterval)
        {
            super(Syslog.getResourceString(MessageConstants.FLUSH_THREAD_NAME_MESSAGE));
            setDaemon(true);
            this.sleepInterval = sleepInterval;
        }


        public long getSleepInterval()
        {
            return this.sleepInterval;
        }


        public void stopRunning()
        {
            this.stop = true;
        }


        public void run()
        {
            while (true && !stop)
            {
                Syslog.flush();
                try
                {
                    Thread.sleep(sleepInterval);
                }
                catch (InterruptedException x)
                {
                    ;
                }
            }
        }
    }
}

