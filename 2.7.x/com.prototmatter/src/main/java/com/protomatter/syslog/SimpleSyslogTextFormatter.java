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

import java.io.PrintWriter;
import java.util.*;
import java.text.*;
import java.net.*;
import java.io.*;
import java.lang.reflect.*;

import com.protomatter.util.*;

/**
 *  A simple log entry formatter.  This class is used by several of
 *  the included <tt>Syslogger</tt> implementations to format their
 *  log entries.
 *
 *  @see com.protomatter.syslog.xml.SimpleSyslogTextFormatter_Helper XML configuration class
 */
public class SimpleSyslogTextFormatter
implements SyslogTextFormatter
{
  private static String crString = System.getProperty("line.separator");
  private static char[] cr = System.getProperty("line.separator").toCharArray();
  private static char[] rb = " [".toCharArray();
  private static char[] lb = "] ".toCharArray();
  private static char rb_ns = '[';
  private static char[] sp = "  ".toCharArray();
  private static char[] parens = "()".toCharArray();
  private static char sp_1 = ' ';
  private static char dot = '.';
  private static char colon = ':';

  private static char[] DEBUG         = "DBUG".toCharArray();
  private static char[] INFO          = "INFO".toCharArray();
  private static char[] WARNING       = "WARN".toCharArray();
  private static char[] ERROR         = "EROR".toCharArray();
  private static char[] FATAL         = "FTAL".toCharArray();
  private static char[] UNKNOWN_LEVEL = "????".toCharArray();

  private static String CH_ALL_CHANNEL = "ALL_CHANNEL";
  private static String CH_DEF_CHANNEL = "DEFAULT_CHANNEL";

  private DateFormat dateFormat = null;
  private TimeZone dateFormatTimeZone = TimeZone.getDefault();
  private String dateFormatString = null;
  private long lastDate = -1;
  private char[] lastDateString = null;
  private int dateFormatCacheTime = 1000;
  private boolean showCaller = true;
  private boolean showChannel = false;
  private boolean showThreadName = false;
  private boolean showHostName = false;

  private int classWidth = 20;
  private int hostWidth = 15;
  private int channelWidth = 15;
  private int threadWidth = 15;

  /**
   *  Default constructor.
   */
  public SimpleSyslogTextFormatter()
  {
    super();
    setDateFormat("MM/dd/yyyy HH:mm:ss");
  }

  /**
   *  Format the given log entry.
   */
  public void formatLogEntry(StringBuffer b, SyslogMessage message)
  {
    synchronized (b)
    {
      b.append(formatDate(message.time));
      b.append(rb);
      b.append(getStringForLevel(message.level));
      b.append(lb);

      if (showChannel)
      {
        if (message.channel.equals(Syslog.ALL_CHANNEL))
        {
          b.append(rb_ns);
          justify(b, CH_ALL_CHANNEL, getChannelWidth());
          b.append(lb);
        }
        else if (message.channel.equals(Syslog.DEFAULT_CHANNEL))
        {
          b.append(rb_ns);
          justify(b, CH_DEF_CHANNEL, getChannelWidth());
          b.append(lb);
        }
        else
        {
          b.append(rb_ns);
          justify(b, message.channel, getChannelWidth());
          b.append(lb);
        }
      }

      if (showHostName)
      {
        justify(b, getHostname(message.host), getHostWidth());
        b.append(sp_1);
      }

      if (showThreadName)
      {
        b.append(rb_ns);
        b.append("T:");
        justify(b, message.threadName, getThreadWidth());
        b.append(lb);
      }
      if (showCaller)
      {
        formatLoggerClassName(b, message);
      }

      if (message.msg != null)
      {
        b.append(sp);
        b.append(message.msg);
      }
      b.append(cr);
      if (message.detail != null)
      {
        formatMessageDetail(b, message);
      }
    }
  }

  public void formatLoggerClassName(StringBuffer b, SyslogMessage message)
  {
    trimFromLastPeriod(b, message.loggerClassname, message.callingMethodName,
        message.callingMethodLineNumber, getClassWidth());
  }

  public void formatMessageDetail(StringBuffer b, SyslogMessage message)
  {
    if (message.detail == null)
      return;

    String temp = null;

    // include stack traces in the output if it's a Throwable
    if (message.detail instanceof Throwable)
    {
      ByteArrayOutputStream bs = new ByteArrayOutputStream();
      PrintWriter pw = new PrintWriter(bs);
      Throwable e = (Throwable)message.detail;
      Throwable e2 = e;
      Object[] junk = null;
      String methodCalled = null;
      while (e2 != null)
      {
        if (methodCalled != null)
        {
          pw.print(methodCalled);
          pw.print("(): ");
        }
        e2.printStackTrace(pw);
        junk = getNextException(e2);
        e2 = (Throwable)junk[1];
        methodCalled = (String)junk[0];
      }
      pw.flush();
      temp = bs.toString();
      b.append(temp);
      if (!temp.endsWith(crString))
        b.append(cr);
    }
    else
    {
      temp = message.detail.toString();
      b.append(temp);
      if (!temp.endsWith(crString))
        b.append(cr);
    }
  }

  /**
   *  Get the "next" exception in this series.  This method
   *  will look for a no-arg method on the given object that
   *  returns a subclass of throwable.  It will skip the
   *  "fillInStackTrace" method.  The return type is
   *  a two-element object array.  The first element is
   *  the method name (or null) that was called to get
   *  the next exception, and the second element is the
   *  instance of the Throwable.
   */
  protected Object[] getNextException(Throwable t)
  {
    if (t == null)
      return null;
    Method methods[] = t.getClass().getMethods();
    Class pt[] = null;
    Class rt = null;
    boolean isFIST = false;
    String name = null;
    Object junk[] = new Object[2];

    for (int i=0; i<methods.length; i++)
    {
      rt = methods[i].getReturnType();
      pt = methods[i].getParameterTypes();
      name = methods[i].getName();
      isFIST = name.equals("fillInStackTrace"); // skip this guy
      if (!isFIST && pt.length == 0 && Throwable.class.isAssignableFrom(rt))
      {
        try
        {
          junk[0] = name;
          junk[1] = (Throwable)methods[i].invoke(t, new Object[0]);
          return junk;
        }
        catch (Throwable x)
        {
          // OK, give up.
          return junk;
        }
      }
    }
    return junk;
  }

  protected char[] getStringForLevel(int level)
  {
    switch (level)
    {
      case Syslog.DEBUG:   return DEBUG;
      case Syslog.INFO:    return INFO;
      case Syslog.WARNING: return WARNING;
      case Syslog.ERROR:   return ERROR;
      case Syslog.FATAL:   return FATAL;
      default: return UNKNOWN_LEVEL;
    }
  }

  public String getHostname(InetAddress host)
  {
    if (host == null)
      return "<null>";

    String ip = host.getHostAddress();
    String name = host.getHostName();
    if (ip.equals(name))
      return ip;

    int idx = name.indexOf(".");
    if (idx == -1)
      return name;
    return name.substring(0, idx);
  }

  /**
   *  Set the format for logging dates.
   */
  public void setDateFormat(String format)
  {
    this.dateFormatString = format;
    this.dateFormat = new SimpleDateFormat(format);
    setDateFormatTimezone(TimeZone.getDefault());
    resetDateFormat();
  }

  /**
   *  Get the format for logging dates.
   */
  public String getDateFormat()
  {
    return this.dateFormatString;
  }

  /**
   *  Set wether we should show the host name in the output.
   *  If this is set to <tt>true</tt> and the hostname
   *  has not been set on Syslog yet, the <tt>Syslog.setLocalHostName()</tt>
   *  method is called.
   */
  public void setShowHostName(boolean showHostName)
  {
    this.showHostName = showHostName;
    if (showHostName && (Syslog.getLocalHostName() == null))
    {
      Syslog.setLocalHostName();
    }
  }

  /**
   *  Get wether we should show the host name in the output.
   */
  public boolean getShowHostName()
  {
    return this.showHostName;
  }

  /**
   *  Set wether we should show the thread name in the output.
   */
  public void setShowThreadName(boolean showThreadName)
  {
    this.showThreadName = showThreadName;
  }

  /**
   *  Get wether we should show the thread name in the output.
   */
  public boolean getShowThreadName()
  {
    return this.showThreadName;
  }

  /**
   *  Set wether we should show the caller name in the output.
   */
  public void setShowCaller(boolean showCaller)
  {
    this.showCaller = showCaller;
  }

  /**
   *  Get wether we should show the caller name in the output.
   */
  public boolean getShowCaller()
  {
    return this.showCaller;
  }

  /**
   *  Set wether we should show the channel name in the output.
   */
  public void setShowChannel(boolean showChannel)
  {
    this.showChannel = showChannel;
  }

  /**
   *  Get wether we should show the channel name in the output.
   */
  public boolean getShowChannel()
  {
    return this.showChannel;
  }

  /**
   *  Set the number of milliseconds a date format should
   *  be cached.  This is also the resolution of the timestamp
   *  in log entries.  Default is 1000ms (1 second).
   */
  public void setDateFormatCacheTime(int cacheTime)
  {
    this.dateFormatCacheTime = cacheTime;
  }

  /**
   *  Get the number of milliseconds a date format should
   *  be cached.  This is also the resolution of the timestamp
   *  in log entries.  Default is 1000ms (1 second).
   */
  public int getDateFormatCacheTime()
  {
    return this.dateFormatCacheTime;
  }

  /**
   *  Set the timezone of the date format.  Default
   *  is <tt>TimeZone.getDefault()</tt>.
   */
  public void setDateFormatTimezone(TimeZone zone)
  {
    this.dateFormatTimeZone = zone;
    this.dateFormat.setTimeZone(zone);
  }

  /**
   *  Get the timezone of the date format.
   */
  public TimeZone getDateFormatTimezone()
  {
    return this.dateFormatTimeZone;
  }

  /**
   *  Format the given date with the dateformat that's been set.
   *  This will cache the date until it's been long enough
   *  for the date format cache time to have expired.
   *
   *  @see #setDateFormatCacheTime
   */
  protected char[] formatDate(long theDate)
  {
    // do the check outside a synchronized block so we
    // don't sync unless absolutely necessary.
    if (lastDate == -1 || theDate > lastDate+dateFormatCacheTime)
    {
      synchronized (dateFormat)
      {
        // check again now that we're synchronized.
        if (lastDate == -1 || theDate > lastDate+dateFormatCacheTime)
        {
          lastDateString = dateFormat.format(new Date(theDate)).toCharArray();
          lastDate = theDate;
        }
      }
    }
    return lastDateString;
  }

  /**
   *  Given something like "foo.bar.Baz" this will return "Baz".
   */
  protected void trimFromLastPeriod(StringBuffer b, String s, int width)
  {
    trimFromLastPeriod(b, s, null, StackTraceInfo.LINE_NUMBER_UNKNOWN, width);
  }

  /**
   *  Given something like "foo.bar.Baz" this will return "Baz".
   */
  protected void trimFromLastPeriod(StringBuffer b, String className, String method, int line, int width)
  {
    char data[] = (className == null) ? new char[0] : className.toCharArray();

    int i=data.length;
    for (; --i>=0 && data[i] != '.';);

    i++;
    int len = data.length - i;
    b.append(data, i, len);
    len = width - len;

    if (method != null)
    {
      b.append(dot);
      b.append(method);
      b.append(parens);
      len -= 3; // dot + parens
      len -= method.length();
      if (line != StackTraceInfo.LINE_NUMBER_UNKNOWN)
      {
        b.append(colon);
        String lineString = String.valueOf(line);
        b.append(lineString);
        len--;
        len -= lineString.length();
      }
    }

    for (;--len>=0;)
      b.append(sp_1);
  }

  /**
   *  Trim a string after the last "." (dot).
   */
  protected String trimFromLastPeriod(String s)
  {
    int pos = s.lastIndexOf('.');
    if (pos >= 0)
      return s.substring(pos+1);
    else
      return s;
  }

  /**
   *  Reset the <tt>formatDate(...)</tt> method so that it's
   *  guaranteed to not return a cached date string the
   *  next time it's called.
   */
  public void resetDateFormat()
  {
    this.lastDate = -1;
  }

  /**
   *  Get the log header.  This simply returns an empty string.
   */
  public String getLogHeader()
  {
    return "";
  }

  /**
   *  Get the log footer.  This simply returns an empty string.
   */
  public String getLogFooter()
  {
    return "";
  }

  /**
   *  Get the width of the caller class name field.
   */
  public int getClassWidth()
  {
    return this.classWidth;
  }

  /**
   *  Set the width of the caller class name field.
   */
  public void setClassWidth(int classWidth)
  {
    this.classWidth = classWidth;
  }

  /**
   *  Get the width of the hostname field.
   */
  public int getHostWidth()
  {
    return this.hostWidth;
  }

  /**
   *  Set the width of the hostname field.
   */
  public void setHostWidth(int hostWidth)
  {
    this.hostWidth = hostWidth;
  }

  /**
   *  Get the width of the channel field.
   */
  public int getChannelWidth()
  {
    return this.channelWidth;
  }

  /**
   *  Set the width of the channel field.
   */
  public void setChannelWidth(int channelWidth)
  {
    this.channelWidth = channelWidth;
  }

  /**
   *  Get the width of the thread field.
   */
  public int getThreadWidth()
  {
    return this.threadWidth;
  }

  /**
   *  Set the width of the thread field.
   */
  public void setThreadWidth(int threadWidth)
  {
    this.threadWidth = threadWidth;
  }

  /**
   *  Justify text to given width in the buffer.
   */
  protected void justify(StringBuffer out, StringBuffer in, int width)
  {
    justify(out, in.toString(), width);
  }

  /**
   *  Justify text to given width in the buffer.
   */
  protected void justify(StringBuffer out, String in, int width)
  {
    out.append(in);
    for (int i=in.length(); i<width; i++)
        out.append(" ");
  }
}
