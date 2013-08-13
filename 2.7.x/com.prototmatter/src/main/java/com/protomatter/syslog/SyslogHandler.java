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

import java.net.InetAddress;
import java.util.*;
import java.util.logging.*;
import java.io.File;
import java.text.MessageFormat;
import com.protomatter.syslog.xml.SyslogXML;
import com.protomatter.syslog.SyslogInitException;
import com.protomatter.util.ChainedRuntimeException;
import com.protomatter.util.StackTraceUtil;

/**
 *  A JDK 1.4 logging system handler to route messages
 *  into Syslog.  Logger names are converted to
 *  syslog channel names.<P>
 *
 *  The following log properties file assigns
 *  the <TT>SyslogHandler</TT> class as the only
 *  log handler.<P>
 *
 *  <UL><TABLE BORDER=1 CELLPADDING=4 CELLSPACING=0>
 *  <TR><TD>
 *  <PRE><B><FONT size="-1">
 *
 *  .level = ALL
 *
 *  handlers = com.protomatter.syslog.SyslogHandler
 *
 *  </FONT></B></PRE>
 *  </TD></TR></TABLE></UL><P>
 *
 *  Here's a usage example:<P>
 *
 *  <UL><TABLE BORDER=1 CELLPADDING=4 CELLSPACING=0>
 *  <TR><TD>
 *  <PRE><B><FONT size="-1">
 *  import java.util.logging.Logger;
 *
 *  ...
 *
 *  // Make a new logger
 *  Logger myLogger = Logger.getLogger("foo.bar");
 *
 *  // Write some log messages
 *  myLogger.info("Info message to my logger");
 *  myLogger.warning("Warning message to my logger");
 *  myLogger.severe("Severe message to my logger");
 *
 *  </FONT></B></PRE>
 *  </TD></TR></TABLE></UL><P>
 *
 *  If you have syslog configured to show the channel name and
 *  log all message levels, you'll see something like this:<P>
 *
 *  <UL><TABLE BORDER=1 CELLPADDING=4 CELLSPACING=0>
 *  <TR><TD>
 *  <PRE><B><FONT size="-1">
 *   11/01/2001 20:09:16 [INFO] [foo.bar        ] Test.main():23      Info message to my logger
 *   11/01/2001 20:09:16 [WARN] [foo.bar        ] Test.main():25      Warning message to my logger
 *   11/01/2001 20:09:16 [EROR] [foo.bar        ] Test.main():27      Severe message to my logger
 *  </FONT></B></PRE>
 *  </TD></TR></TABLE></UL><P>
 *
 *  If the "<TT>SyslogHandler.xml</TT>" system property is set,
 *  this class will call <TT>SyslogXML.configure()</TT> with
 *  that configuration file.<P>
 *
 *  So, to configure syslog and use the JDK 1.4 logging API to
 *  write to it, do the following:
 *
 *  <UL><TABLE BORDER=1 CELLPADDING=4 CELLSPACING=0>
 *  <TR><TD>
 *  <PRE><B><FONT size="-1">
 *
 *  java \
 *    ...
 *    -DSyslogHandler.xml=/path/to/syslog.xml                        \ 
 *    -Djava.util.logging.config.file=/path/to/logging.properties    \ 
 *    ...
 *    java-main-class
 *
 *  </FONT></B></PRE>
 *  </TD></TR></TABLE></UL><P>
 *
 * Note that if you use this class, and don't have Syslog configured to compute
 * the caller class and method, then the caller name in your logs will be
 * incorrect.  This is because the JDK 1.4 logging API doesn't have a mechanism
 * for directly getting a reference to the caller.<P>
 */
public class SyslogHandler
extends Handler
{
  private boolean open = true;
  private Map levelMap = null;

  private static String CONFIG_FILE_PROPERTY = "SyslogHandler.xml";

  static
  {
    String xmlFile = System.getProperty(CONFIG_FILE_PROPERTY);
    if (xmlFile != null)
    {
      try
      {
        SyslogXML.configure(new File(xmlFile));
      }
      catch (SyslogInitException x)
      {
        throw new ChainedRuntimeException(Syslog.getResourceString(MessageConstants.CANNOT_CONFIGURE_MESSAGE), x);
      }
    }
  }

  /**
   *  Create a new handler for routing messages to Syslog.
   */
  public SyslogHandler()
  {
    super();

    resetLevelMap();
  }

  /**
   *  Reset the level conversion map to the default.
   *  The default map is:<P>
   *
   *  <table border=1 cellpadding=4 cellspacing=0>
   *  <tr>
   *    <th>java.util.logging.Level</th>
   *    <th>Syslog Level</th>
   *  </tr>
   *  <tr>
   *    <td><TT>Level.SEVERE</TT></td>
   *    <td><TT>Syslog.ERROR</TT></td>
   *  </tr>
   *  <tr>
   *    <td><TT>Level.WARNING</TT></td>
   *    <td><TT>Syslog.WARNING</TT></td>
   *  </tr>
   *  <tr>
   *    <td><TT>Level.INFO</TT></td>
   *    <td><TT>Syslog.INFO</TT></td>
   *  </tr>
   *  <tr>
   *    <td><TT>Level.CONFIG</TT></td>
   *    <td><TT>Syslog.DEBUG</TT></td>
   *  </tr>
   *  <tr>
   *    <td><TT>Level.FINE</TT></td>
   *    <td><TT>Syslog.DEBUG</TT></td>
   *  </tr>
   *  <tr>
   *    <td><TT>Level.FINER</TT></td>
   *    <td><TT>Syslog.DEBUG</TT></td>
   *  </tr>
   *  <tr>
   *    <td><TT>Level.FINEST</TT></td>
   *    <td><TT>Syslog.DEBUG</TT></td>
   *  </tr>
   *  </table>
   */
  public void resetLevelMap()
  {
    Map levelMap = new HashMap();
    levelMap.put(Level.SEVERE,  new Integer(Syslog.ERROR));
    levelMap.put(Level.WARNING, new Integer(Syslog.WARNING));
    levelMap.put(Level.INFO,    new Integer(Syslog.INFO));
    levelMap.put(Level.CONFIG,  new Integer(Syslog.DEBUG));
    levelMap.put(Level.FINE,    new Integer(Syslog.DEBUG));
    levelMap.put(Level.FINER,   new Integer(Syslog.DEBUG));
    levelMap.put(Level.FINEST,  new Integer(Syslog.DEBUG));
    setLevelMap(levelMap);
  }

  /**
   *  Close the handler.
   */
  public void close()
  {
    open = false;
  }

  /**
   *  Flush this handler.  This method simply
   *  calls <TT>Syslog.flush()</TT>.
   *
   *  @see Syslog#flush()
   */
  public void flush()
  {
    Syslog.flush();
  }

  /**
   *  Route the given log record into Syslog.
   *
   *  If this handler has been closed, the record is ignored.
   *  If a filter has been set, then it is applied to record
   *  to see if it should be passed on to Syslog.<P>
   *
   *  The formatter is ignored.  Arguments passed as
   *  message parameters are parsed and replaced
   *  in the message using the <tt>java.text.MessageFormat</tt>
   *  class.<P>
   *
   *  If a throwable has been set, it is used as the message
   *  detail when the record is sent to Syslog.<P>
   *
   *  The log level is converted from a <TT>java.util.logging.Level</TT>
   *  according to the level map for this handler.<P>
   *
   *  The name of the <TT>java.util.logging.Logger</TT> is used
   *  as the channel name.<P>
   *
   *  Once these conversions have been made, the information is
   *  passed onto Syslog for further processing.
   */
  public void publish(java.util.logging.LogRecord record)
  {
    if (!open)
      return;

    // check with a filter if there is one.
    Filter filter = getFilter();
    if (filter != null && !filter.isLoggable(record))
      return;

    // format the message
    String message = record.getMessage();
    Object params[] = record.getParameters();
    if (params != null && (params.length > 0))
    {
      message = MessageFormat.format(message, params);
    }

    // convert JDK 1.4 level to Syslog level
    int syslogLevel = getSyslogLevel(record.getLevel());

    // get the local thread and its name
    Thread currentThread = Thread.currentThread();
    String threadName = currentThread.getName();

    // get message send time
    long messageTime = record.getMillis();

    // get initiating class name
    Object loggerClass = record.getSourceClassName();
    if (loggerClass == null)
      loggerClass = this;

    // get the detail.  This is the exception being logged, if any
    Object detail = record.getThrown();

    // treat the logger name as the channel.
    String channel = record.getLoggerName();

    // determine call stack depth.  The
    // entering() and exiting() methods are
    // one layer deeper.  This is so we can
    // accurately determine class, method
    // and line number at runtime.
    int depth = 4;
    if ("java.util.logging.Logger".equals(StackTraceUtil.whereAmI(depth).className))
        depth++;

    // send the message off to Syslog.
    Syslog.log(
        Syslog.getLocalHostName(),
        loggerClass,
        channel,
        message,
        detail,
        syslogLevel,
        currentThread,
        threadName,
        messageTime,
        depth
        );
  }

  /**
   *  Convert a JDK 1.4 log level to a Syslog level.
   */
  private final int getSyslogLevel(java.util.logging.Level level)
  {
    return ((Integer)this.levelMap.get(level)).intValue();
  }

  /**
   *  Get the conversion map for levels.  The key in the
   *  map is an instance of <TT>java.util.logging.Level</TT>
   *  and the value is a <TT>java.lang.Integer</TT> which is
   *  a wrapper for <TT>Syslog.DEBUG</TT>, <TT>Syslog.INFO</TT>, etc.
   */
  public Map getLevelMap()
  {
    return this.levelMap;
  }

  /**
   *  Set the conversion map for levels.  The key in the
   *  map is an instance of <TT>java.util.logging.Level</TT>
   *  and the value is a <TT>java.lang.Integer</TT> which is
   *  a wrapper for <TT>Syslog.DEBUG</TT>, <TT>Syslog.INFO</TT>, etc.
   */
  public void setLevelMap(Map levelMap)
  {
    this.levelMap = levelMap;
  }
}
