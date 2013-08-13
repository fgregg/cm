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
 *  A simple HTML log entry formatter for email.
 *
 *  @see com.protomatter.syslog.xml.SyslogHTMLMailFormatter_Helper XML configuration class
 */
public class SyslogHTMLMailFormatter
extends SimpleSyslogTextFormatter
{
  private static char[] DEBUG         = "DEBUG".toCharArray();
  private static char[] INFO          = "INFO".toCharArray();
  private static char[] WARNING       = "WARNING".toCharArray();
  private static char[] ERROR         = "ERROR".toCharArray();
  private static char[] FATAL         = "FATAL".toCharArray();
  private static char[] UNKNOWN_LEVEL = "????".toCharArray();

  /**
   *  Default constructor.
   */
  public SyslogHTMLMailFormatter()
  {
    super();
    setDateFormat("MM/dd/yyyy HH:mm:ss");
  }

  /**
   *  Format the given log entry.
   *
   *  A sample of the text created by this formatter is:<P>
   *
   *  <TABLE BORDER=1 CELLSPACING=0 CELLPADDING=5>
   *  <TR><TD>
   *
   *  <TABLE BORDER=0 CELLPADDING=2 CELLSPACING=0>
   *  <TR><TD>Date:</TD><TD><TT>10/31/2000 23:31:25</TT></TD></TR>
   *  <TR><TD>Severity:</TD><TD><TT><B>ERROR</B></TT></TD></TR>
   *  <TR><TD>Channel:</TD><TD><TT>DEFAULT_CHANNEL</TT></TD></TR>
   *  <TR><TD>Hostname:</TD><TD><TT>sassyrobot</TT></TD></TR>
   *  <TR><TD>Thread:</TD><TD><TT>main</TT></TD></TR>
   *  <TR><TD>Logger:</TD><TD><TT>LittleSyslogTest</TT></TD></TR>
   *  </TABLE>
   *  <DL>
   *  <DT>Message: <TT>java.lang.Exception: Happy halloween!</TT></DT>
   *
   *  <DD><PRE>
   *  java.lang.Exception: Happy halloween!
   *        at LittleSyslogTest.go(LittleSyslogTest.java:31)
   *        at LittleSyslogTest.main(LittleSyslogTest.java:13)
   *  </PRE></DD>
   *  </DL>
   *
   *  </TD></TR>
   *  </TABLE>
   */
  public String formatLogEntry(SyslogMessage message)
  {
    StringBuffer b = new StringBuffer(256);

    b.append("<TABLE BORDER=0 CELLPADDING=2 CELLSPACING=0>\n");

    b.append("<TR><TD>");
    b.append(Syslog.getResourceString(MessageConstants.HTML_DATE_MESSAGE));
    b.append("</TD><TD><TT>");
    b.append(formatDate(message.time));
    b.append("</TT></TD></TR>\n");

    b.append("<TR><TD>");
    b.append(Syslog.getResourceString(MessageConstants.HTML_SEVERITY_MESSAGE));
    b.append("</TD><TD><TT><B>");
    b.append(getStringForLevel(message.level));
    b.append("</B></TT></TD></TR>\n");

    if (getShowChannel())
    {
      b.append("<TR><TD>");
      b.append(Syslog.getResourceString(MessageConstants.HTML_CHANNEL_MESSAGE));
      b.append("</TD><TD><TT>");
      if (message.channel.equals(Syslog.ALL_CHANNEL))
        b.append("ALL_CHANNEL");
      else if (message.channel.equals(Syslog.DEFAULT_CHANNEL))
        b.append("DEFAULT_CHANNEL");
      else
        b.append(message.channel);
      b.append("</TT></TD></TR>\n");
    }

    if (getShowHostName())
    {
      b.append("<TR><TD>");
      b.append(Syslog.getResourceString(MessageConstants.HTML_HOSTNAME_MESSAGE));
      b.append("</TD><TD><TT>");
      b.append(getHostname(message.host));
      b.append("</TT></TD></TR>\n");
    }

    if (getShowThreadName())
    {
      b.append("<TR><TD>");
      b.append(Syslog.getResourceString(MessageConstants.HTML_THREAD_MESSAGE));
      b.append("</TD><TD><TT>");
      b.append(message.threadName);
      b.append("</TT></TD></TR>\n");
    }
    b.append("<TR><TD>");
    b.append(Syslog.getResourceString(MessageConstants.HTML_LOGGER_MESSAGE));
    b.append("</TD><TD><TT>");
    formatLoggerClassName(b, message);
    //b.append(message.loggerClassname);
    b.append("</TT></TD></TR>\n");
    b.append("</TABLE>\n");

    b.append("<DL>\n");
    if (message.msg != null)
    {
      b.append("<DT>");
      b.append(Syslog.getResourceString(MessageConstants.HTML_MESSAGE_MESSAGE));
      b.append("<TT> ");
      b.append(message.msg);
      b.append("</TT></DT>\n");
    }
    if (message.detail != null)
    {
      b.append("<DD><PRE>\n");
      formatMessageDetail(b, message);
      b.append("</PRE></DD>\n");
    }
    b.append("</DL>\n");
    return b.toString();
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
