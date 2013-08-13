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

import com.protomatter.util.*;

/**
 *  A simple mail subject formatter.  This class is used by
 *  default by the <tt>MailLog</tt> logger.
 *
 *  @see MailLog
 *  @see com.protomatter.syslog.xml.SimpleSyslogMailSubjectFormatter_Helper XML configuration class
 */
public class SimpleSyslogMailSubjectFormatter
implements SyslogMailSubjectFormatter
{
  private static String DEBUG         = "DEBUG".intern();
  private static String INFO          = "INFO".intern();
  private static String WARNING       = "WARNING".intern();
  private static String ERROR         = "ERROR".intern();
  private static String FATAL         = "FATAL".intern();
  private static String UNKNOWN_LEVEL = "????".intern();

  private boolean showCaller = true;
  private boolean showChannel = false;
  private boolean showThreadName = false;
  private boolean showHostName = false;

  /**
   *  Default constructor.
   */
  public SimpleSyslogMailSubjectFormatter()
  {
    super();
  }

  /**
   *  Format the given log entry.
   */
  public String formatMessageSubject(SyslogMessage message)
  {
    StringBuffer b = new StringBuffer(64);
    b.append(getStringForLevel(message.level));
    b.append(": ");

    if (showChannel)
    {
      b.append("[");
      if (message.channel.equals(Syslog.ALL_CHANNEL))
        b.append("ALL_CHANNEL");
      else if (message.channel.equals(Syslog.DEFAULT_CHANNEL))
        b.append("DEFAULT_CHANNEL");
      else
        b.append(message.channel);
      b.append("] ");
    }

    if (showHostName)
    {
      b.append("[");
      b.append(StringUtil.pad(getHostname(message.host), 10));
      b.append("] ");
    }

    if (showThreadName)
    {
      b.append("[");
      b.append(message.threadName);
      b.append("] ");
    }
    if (showCaller)
    {
      b.append(trimFromLastPeriod(message.loggerClassname));
    }

    if (message.msg != null)
    {
      b.append(": ");
      b.append(message.msg);
    }
    return b.toString();
  }

  protected String getStringForLevel(int level)
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

  protected String getHostname(InetAddress host)
  {
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
   *  Set wether we should show the host name in the output.
   */
  public void setShowHostName(boolean showHostName)
  {
    this.showHostName = showHostName;
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
   *  Given something like "foo.bar.Baz" this will return "Baz".
   */
  protected String trimFromLastPeriod(String s)
  {
    int pos = s.lastIndexOf('.');
    if (pos >= 0)
      return s.substring(pos+1);
    else
      return s;
  }
}
