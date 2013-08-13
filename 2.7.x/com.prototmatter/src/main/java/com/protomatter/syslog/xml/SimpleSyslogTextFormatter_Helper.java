package com.protomatter.syslog.xml;

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

import org.jdom.*;

import com.protomatter.xml.*;
import com.protomatter.syslog.*;

/**
 *  XML configuration helper for <tt>SimpleSyslogTextFormatter</tt>.
 */
public class SimpleSyslogTextFormatter_Helper
implements XMLConfigHelper
{
  /**
   *  Configure this text formatter given the XML element.
   *  The <tt>&lt;Format&gt;</tt> element should look like this:<P>
   *
   *  <TABLE BORDER=1 CELLPADDING=4 CELLSPACING=0 WIDTH="90%">
   *  <TR><TD>
   *  <PRE><B>
   *
   *  &lt;Format class="com.protomatter.syslog.SimpleSyslogTextFormatter" &gt;
   *
   *    &lt;showCaller&gt;<i>true|false</i>&lt;/showCaller&gt;
   *    &lt;showChannel&gt;<i>true|false</i>&lt;/showChannel&gt;
   *    &lt;showThreadName&gt;<i>true|false</i>&lt;/showThreadName&gt;
   *    &lt;showHostName&gt;<i>true|false</i>&lt;/showHostName&gt;
   *    &lt;dateFormat&gt;<i>DateFormat</i>&lt;/dateFormat&gt;
   *    &lt;dateFormatTimeZone&gt;<i>TimeZoneName</i>&lt;/dateFormatTimeZone&gt;
   *    &lt;dateFormatCacheTime&gt;<i>CacheTimeout</i>&lt;/dateFormatCacheTime&gt;
   *
   *    &lt;classWidth&gt;<i>field-width</i>&lt;/classWidth&gt;
   *    &lt;channelWidth&gt;<i>field-width</i>&lt;/channelWidth&gt;
   *    &lt;threadWidth&gt;<i>field-width</i>&lt;/threadWidth&gt;
   *    &lt;hostWidth&gt;<i>field-width</i>&lt;/hostWidth&gt;
   *
   *  &lt;/Format&gt;
   *  </B></PRE>
   *  </TD></TR></TABLE><P>
   *
   *  <TABLE BORDER=1 CELLPADDING=2 CELLSPACING=0 WIDTH="90%">
   *  <TR CLASS="TableHeadingColor">
   *  <TD COLSPAN=3><B>Element</B></TD>
   *  </TR>
   *  <TR CLASS="TableHeadingColor">
   *  <TD><B>name</B></TD>
   *  <TD><B>value</B></TD>
   *  <TD><B>required</B></TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>showCaller</TT></TD>
   *  <TD><tt>true</tt> or <tt>false</tt> -- decide if the caller
   *  name should appear in the log.
   *  </TD>
   *  <TD VALIGN=TOP>no (default is <tt>true</tt>)</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>showChannel</TT></TD>
   *  <TD><tt>true</tt> or <tt>false</tt> -- decide if the channel
   *  name should appear in the log.
   *  </TD>
   *  <TD VALIGN=TOP>no (default is <tt>false</tt>)</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>showThreadName</TT></TD>
   *  <TD><tt>true</tt> or <tt>false</tt> -- decide if the thread
   *  name should appear in the log.
   *  </TD>
   *  <TD VALIGN=TOP>no (default is <tt>false</tt>)</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>showHostName</TT></TD>
   *  <TD><tt>true</tt> or <tt>false</tt> -- decide if the host name
   *  name should appear in the log.
   *  </TD>
   *  <TD VALIGN=TOP>no (default is <tt>false</tt>)</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>dateFormat</TT></TD>
   *  <TD>A date format that can be understood by the
   *  <tt>java.text.SimpleDateFormat</tt> format class.
   *  </TD>
   *  <TD VALIGN=TOP>no (default is "<tt>HH:mm:ss MM/dd</tt>")</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>dateFormatTimeZone</TT></TD>
   *  <TD>The name of the timezone to use for formatting timestamps.
   *  </TD>
   *  <TD VALIGN=TOP>no (default is the current time zone)</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>dateFormatCacheTime</TT></TD>
   *  <TD>How long (in milliseconds) to cache a created timestamp formatting object
   *  </TD>
   *  <TD VALIGN=TOP>no (default is <tt>1000</tt>)</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>classWidth</TT></TD>
   *  <TD>Field width in characters for the class section.
   *  </TD>
   *  <TD VALIGN=TOP>no (default is <tt>20</tt>)</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>channelWidth</TT></TD>
   *  <TD>Field width in characters for the channel section.
   *  </TD>
   *  <TD VALIGN=TOP>no (default is <tt>15</tt>)</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>hostWidth</TT></TD>
   *  <TD>Field width in characters for the host name section.
   *  </TD>
   *  <TD VALIGN=TOP>no (default is <tt>15</tt>)</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>threadWidth</TT></TD>
   *  <TD>Field width in characters for the thread name section.
   *  </TD>
   *  <TD VALIGN=TOP>no (default is <tt>15</tt>)</TD>
   *  </TR>
   *
   *  </TABLE><P>
   */
  public void configure(Object o, Element e)
  throws SyslogInitException
  {
    SimpleSyslogTextFormatter format = (SimpleSyslogTextFormatter)o;

    String tmp = e.getChildTextTrim("showChannel", e.getNamespace());
    if (tmp != null)
        format.setShowChannel("true".equalsIgnoreCase(tmp));

    tmp = e.getChildTextTrim("showCaller", e.getNamespace());
    if (tmp != null)
        format.setShowCaller("true".equalsIgnoreCase(tmp));

    tmp = e.getChildTextTrim("showThreadName", e.getNamespace());
    if (tmp != null)
        format.setShowThreadName("true".equalsIgnoreCase(tmp));

    tmp = e.getChildTextTrim("showHostName", e.getNamespace());
    if (tmp != null)
        format.setShowHostName("true".equalsIgnoreCase(tmp));

    tmp = e.getChildTextTrim("dateFormat", e.getNamespace());
    if (tmp != null)
      format.setDateFormat(tmp);

    tmp = e.getChildTextTrim("dateFormatTimeZone", e.getNamespace());
    if (tmp != null)
      format.setDateFormatTimezone(TimeZone.getTimeZone(tmp));

    tmp = e.getChildTextTrim("dateFormatCacheTime", e.getNamespace());
    if (tmp != null)
      format.setDateFormatCacheTime(Integer.parseInt(tmp));

    tmp = e.getChildTextTrim("classWidth", e.getNamespace());
    if (tmp != null)
      format.setClassWidth(Integer.parseInt(tmp));

    tmp = e.getChildTextTrim("channelWidth", e.getNamespace());
    if (tmp != null)
      format.setChannelWidth(Integer.parseInt(tmp));

    tmp = e.getChildTextTrim("hostWidth", e.getNamespace());
    if (tmp != null)
      format.setHostWidth(Integer.parseInt(tmp));

    tmp = e.getChildTextTrim("threadWidth", e.getNamespace());
    if (tmp != null)
      format.setThreadWidth(Integer.parseInt(tmp));

  }

  public Element getConfiguration(Object o, Element element)
  {
    SimpleSyslogTextFormatter format = (SimpleSyslogTextFormatter)o;

    Element sc = new Element("showChannel");
    sc.setText(String.valueOf(format.getShowChannel()));
    element.getChildren().add(sc);

    Element st = new Element("showThreadName");
    st.setText(String.valueOf(format.getShowThreadName()));
    element.getChildren().add(st);

    Element sh = new Element("showHostName");
    sh.setText(String.valueOf(format.getShowHostName()));
    element.getChildren().add(sh);

    Element df = new Element("dateFormat");
    df.setText(String.valueOf(format.getDateFormat()));
    element.getChildren().add(df);

    Element dfc = new Element("dateFormatCacheTime");
    dfc.setText(String.valueOf(format.getDateFormatCacheTime()));
    element.getChildren().add(dfc);

    Element tz = new Element("dateFormatTimeZone");
    tz.setText(String.valueOf(format.getDateFormatTimezone().getID()));
    element.getChildren().add(tz);

    Element cw = new Element("classWidth");
    cw.setText(String.valueOf(format.getClassWidth()));
    element.getChildren().add(cw);

    Element chw = new Element("channelWidth");
    chw.setText(String.valueOf(format.getChannelWidth()));
    element.getChildren().add(chw);

    Element tw = new Element("threadWidth");
    tw.setText(String.valueOf(format.getThreadWidth()));
    element.getChildren().add(tw);

    Element hw = new Element("hostWidth");
    hw.setText(String.valueOf(format.getHostWidth()));
    element.getChildren().add(hw);

    return element;
  }
}
