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
 *  A log formatter that mimics the log output from WebLogic.
 *  This class was contributed by Vik Ganora &lt;vganora@yahoo.com&gt;
 *
 *  @see com.protomatter.syslog.xml.WlSyslogTextFormatter_Helper XML configuration class
 */
public class WlSyslogTextFormatter
extends SimpleSyslogTextFormatter
{
  // intern some strings so they are shared all over the VM.
  private static char[] cr = System.getProperty("line.separator").toCharArray();
  private static char   rt = '>';
  private static char[] lf = " <".toCharArray();
  private static char   lab = '<';

  private static char[] DEBUG         = "D".toCharArray();
  private static char[] INFO          = "I".toCharArray();
  private static char[] WARNING       = "W".toCharArray();
  private static char[] ERROR         = "E".toCharArray();
  private static char[] FATAL         = "F".toCharArray();
  private static char[] UNKNOWN_LEVEL = "?".toCharArray();

  public WlSyslogTextFormatter()
  {
    super();
    setDateFormat("EEE MMM dd HH:mm:ss zzz yyyy:");
  }

  public void formatLogEntry(StringBuffer b, SyslogMessage message)
  {
    b.append(formatDate(message.time));
    b.append(lab);
    b.append(getStringForLevel(message.level));
    b.append(rt);

    b.append(lf);
    //b.append(trimFromLastPeriod(message.loggerClassname));
    formatLoggerClassName(b, message);
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
      b.append(rt);
    }

    if (getShowHostName())
    {
      b.append(lf);
      b.append(getHostname(message.host));
      b.append(rt);
    }

    if (getShowThreadName())
    {
      b.append(lf);
      b.append(message.threadName);
      b.append(rt);
    }

    if (message.msg != null)
    {
      b.append(" ");
      b.append(message.msg);
    }
    b.append(cr);
    if (message.detail != null)
    {
      formatMessageDetail(b, message);
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
