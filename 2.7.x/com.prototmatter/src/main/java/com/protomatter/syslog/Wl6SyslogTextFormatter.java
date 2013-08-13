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

import java.util.Date;

/**
 *  A log formatter that mimics the log output from WebLogic Server 6.x.
 *  This logger is based on the
 *  {@link WlSyslogTextFormatter WlSyslogTextFormatter} which
 *  mimics output from WebLogic Server 4.x and 5.x.
 *
 *  @see com.protomatter.syslog.xml.Wl6SyslogTextFormatter_Helper XML configuration class
 */
public class Wl6SyslogTextFormatter
extends SimpleSyslogTextFormatter
{
  // intern some strings so they are shared all over the VM.
  private static char[] cr = System.getProperty("line.separator").toCharArray();
  private static char   rt = '>';
  private static char   sp = ' ';
  private static char[] lf = " <".toCharArray();
  private static char   lab = '<';

  private static char[] DEBUG         = "Debug".toCharArray();
  private static char[] INFO          = "Info".toCharArray();
  private static char[] WARNING       = "Warning".toCharArray();
  private static char[] ERROR         = "Error".toCharArray();
  private static char[] FATAL         = "Fatal".toCharArray();
  private static char[] UNKNOWN_LEVEL = "Unknown".toCharArray();

  public Wl6SyslogTextFormatter()
  {
    super();
    setDateFormat("MMM dd, yyyy HH:mm:ss a z");
  }

  public void formatLogEntry(StringBuffer b, SyslogMessage message)
  {
    b.append(lab);
    b.append(formatDate(message.time));
    b.append(rt);

    b.append(lf);
    b.append(getStringForLevel(message.level));
    b.append(rt);

    if (getShowChannel())
    {
      b.append(lf);
      if (message.channel.equals(Syslog.ALL_CHANNEL))
        b.append("ALL_CHANNEL");
      else if (message.channel.equals(Syslog.DEFAULT_CHANNEL))
        b.append("DEFAULT_CHANNEL");
      else
        b.append(message.channel);
      b.append(": ");
      //b.append(trimFromLastPeriod(message.loggerClassname));
      formatLoggerClassName(b, message);
      b.append(rt);
    }
    else
    {
      b.append(lf);
      b.append(trimFromLastPeriod(message.loggerClassname));
      b.append(rt);
    }

    if (getShowHostName())
    {
      b.append(lf);
      b.append(Syslog.getResourceString(MessageConstants.WLS_HOST_MESSAGE));
      b.append("=");
      b.append(getHostname(message.host));
      b.append(rt);
    }

    if (getShowThreadName())
    {
      b.append(lf);
      b.append(Syslog.getResourceString(MessageConstants.WLS_THREAD_MESSAGE));
      b.append("=");
      b.append(message.threadName);
      b.append(rt);
    }

    if (message.msg != null)
    {
      b.append(lf);
      b.append(message.msg);
      b.append(rt);
    }
    b.append(cr);
    if (message.detail != null)
    {
      b.append(lab);
      formatMessageDetail(b, message);
      b.append(rt);
      b.append(cr);
    }
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

}
