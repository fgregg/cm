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
 *  A log entry formatter that produces HTML.
 *
 *  @see com.protomatter.sylog.xml.HTMLSyslogTextFormatter_Helper XML configuration class
 */
public class HTMLSyslogTextFormatter
extends SimpleSyslogTextFormatter
{
  private String styleSheet = "syslog-stylesheet.css";

  private static char[] DEBUG         = "DEBUG".toCharArray();
  private static char[] INFO          = "INFO".toCharArray();
  private static char[] WARNING       = "WARNING".toCharArray();
  private static char[] ERROR         = "ERROR".toCharArray();
  private static char[] FATAL         = "FATAL".toCharArray();
  private static char[] UNKNOWN_LEVEL = "UNKNOWN".toCharArray();

  /**
   *  Private constructor so nobody goes around creating these.
   */
  public HTMLSyslogTextFormatter()
  {
    super();
  }

  /**
   *  Set the stylesheet to use.
   */
  public void setStyleSheet(String styleSheet)
  {
    this.styleSheet = styleSheet;
  }

  /**
   *  Get the stylesheet being used.
   */
  public String getStyleSheet()
  {
    return this.styleSheet;
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

  /**
   *  Format a log entry.
   */
  public void formatLogEntry(StringBuffer b, SyslogMessage message)
  {
    int columns = 0;

    b.append("<TR>\n");

    b.append("<TD CLASS=\"SYSLOG-DATE\">");
    b.append(formatDate(message.time));
    b.append("</TD>\n");
    columns++;

    char[] level = getStringForLevel(message.level);
    b.append("<TD CLASS=\"SYSLOG-LEVEL-");
    b.append(level);
    b.append("\">");
    b.append(level);
    b.append("</TD>\n");
    columns++;

    if (getShowChannel())
    {
      b.append("<TD CLASS=\"SYSLOG-CHANNEL\">");
      if (message.channel.equals(Syslog.ALL_CHANNEL))
        b.append("ALL_CHANNEL");
      else if (message.channel.equals(Syslog.DEFAULT_CHANNEL))
        b.append("DEFAULT_CHANNEL");
      else
        b.append(message.channel);
      b.append("</TD>\n");
      columns++;
    }

    if (getShowHostName())
    {
      b.append("<TD CLASS=\"SYSLOG-HOSTNAME\">");
      b.append(getHostname(message.host));
      b.append("</TD>\n");
      columns++;
    }

    if (getShowThreadName())
    {
      b.append("<TD CLASS=\"SYSLOG-THREADNAME\">");
      b.append(message.threadName);
      b.append("</TD>\n");
      columns++;
    }

    b.append("<TD CLASS=\"SYSLOG-LOGGERCLASS\">");
    //b.append(trimFromLastPeriod(message.loggerClassname));
    formatLoggerClassName(b, message);
    b.append("</TD>\n");
    columns++;

    String msg = null;
    if (message.msg != null)
      msg = message.msg.toString();
    else
      msg = "&nbsp;";

    b.append("<TD CLASS=\"SYSLOG-MESSAGE\">");
    b.append(msg);
    b.append("</TD>\n");
    columns++;

    b.append("</TR>\n");

    if (message.detail != null)
    {
      b.append("<TR>\n");
      b.append("<TD>&nbsp;</TD>\n");
      b.append("<TD CLASS=\"SYSLOG-DETAIL\" COLSPAN=\""
        + (columns -1) + "\"><PRE>");
      int len = b.length();
      formatMessageDetail(b, message);
      if (b.length() == len)
        b.append("&nbsp;");
      b.append("</PRE></TD>\n</TR>\n");
    }
  }

  /**
   *  Get the log header.  This simply returns an empty string.
   */
  public String getLogHeader()
  {
    StringBuffer b = new StringBuffer(64);
    b.append("<HTML>\n");
    b.append("<HEAD>\n");
    b.append("<TITLE>");
    b.append(Syslog.getResourceString(MessageConstants.HTML_OUTPUT_MESSAGE));
    b.append("</TITLE>\n");
    b.append("<LINK REL=\"stylesheet\" TYPE=\"text/css\" HREF=\"");
    b.append(getStyleSheet());
    b.append("\" TITLE=\"Style\">\n");
    b.append("</HEAD>\n");
    b.append("<BODY BGCOLOR=\"#FFFFFF\">\n");
    b.append("<TABLE BORDER=\"1\" CELLPADDING=\"2\" CELLSPACING=\"0\">\n");
    return b.toString();
  }

  /**
   *  Get the log footer.  This simply returns an empty string.
   */
  public String getLogFooter()
  {
    StringBuffer b = new StringBuffer(64);
    b.append("</TABLE>\n");
    b.append("</BODY>\n");
    b.append("</HTML>\n");
    return b.toString();
  }
}
