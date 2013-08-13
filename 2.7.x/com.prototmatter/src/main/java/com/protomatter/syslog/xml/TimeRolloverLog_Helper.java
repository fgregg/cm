package com.protomatter.syslog.xml;

/**
 *  The Protomatter Software License, Version 1.0
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
 *  SUCH DAMAGE.
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

import com.protomatter.xml.*;
import com.protomatter.syslog.*;
import org.jdom.*;

/**
 *  XML configuration helper for <tt>TimeRolloverLog</tt>.
 */
public class TimeRolloverLog_Helper
extends BasicLogger_Helper
{
  /**
   *  Configure this logger given the XML element.
   *  The <tt>&lt;Logger&gt;</tt> element should look like this:<P>
   *
   *  <TABLE BORDER=1 CELLPADDING=4 CELLSPACING=0 WIDTH="90%">
   *  <TR><TD>
   *  <PRE><B>
   *
   *  &lt;Logger class="com.protomatter.syslog.TimeRolloverLog" &gt;
   *
   *    <font color="#888888">&lt;!--
   *     Config params from {@link BasicLogger_Helper#configure(Object,Element) BasicLogger_Helper} can
   *     get inserted here.
   *    --&gt;</font>
   *
   *    &lt;baseName&gt;<i>BaseFileName</i>&lt;/baseName&gt;
   *    &lt;extension&gt;<i>FilenameExtension</i>&lt;/extension&gt;
   *    &lt;nameFormat&gt;<i>FileDateFormat</i>&lt;/nameFormat&gt;
   *    &lt;append&gt;<i>true|false</i>&lt;/append&gt;
   *    &lt;autoFlush&gt;<i>true|false</i>&lt;/autoFlush&gt;
   *    &lt;roll&gt;<i>RollConstant</i>&lt;/roll&gt;
   *    &lt;weeklyRollDay&gt;<i>RollDayConstant</i>&lt;/weeklyRollDay&gt;
   *
   *  &lt;/Logger&gt;
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
   *  <TD VALIGN=TOP><TT>baseName</TT></TD>
   *  <TD VALIGN=TOP>The base name of the file to write to.
   *  </TD>
   *  <TD VALIGN=TOP>yes</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>extension</TT></TD>
   *  <TD VALIGN=TOP>The filename extension to use.
   *  </TD>
   *  <TD VALIGN=TOP>no</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>nameFormat</TT></TD>
   *  <TD VALIGN=TOP>A date format that can be understood by the
   *  <tt>java.text.SimpleDateFormat</tt> formatting class.
   *  </TD>
   *  <TD VALIGN=TOP>no (default is "<TT>yyyy.MM.dd-HH.mm.ss</TT>")</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>roll</TT></TD>
   *  <TD VALIGN=TOP>The roll interval.  Must be <TT>ROLL_MINUTELY</TT>,
   *  <TT>ROLL_HOURLY</TT>, <TT>ROLL_DAILY</TT>, <TT>ROLL_WEEKLY</TT>
   *  or <TT>ROLL_MONTHLY</TT>
   *  </TD>
   *  <TD VALIGN=TOP>yes</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>weeklyRollDay</TT></TD>
   *  <TD VALIGN=TOP>The day of week to roll if the <tt>roll</tt>
   *  setting is <tt>ROLL_WEEKLY</tt>.  Must be <TT>MONDAY</TT>,
   *  <TT>TUESDAY</TT>, <TT>WEDNESDAY</TT>, <tt>THURSDAY</tt>,
   *  <TT>FRIDAY</TT>, <TT>SATURDAY</TT> or <TT>SUNDAY</TT>.
   *  </TD>
   *  <TD VALIGN=TOP>no (default is "<tt>MONDAY</tt>"</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>append</TT></TD>
   *  <TD><tt>true</tt> or <tt>false</tt> -- decide if the file
   *  should be appended to (or overwritten).
   *  </TD>
   *  <TD VALIGN=TOP>no (default is <tt>true</tt>)</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>autoFlush</TT></TD>
   *  <TD><tt>true</tt> or <tt>false</tt> -- decide if we
   *  should always flush the stream every time it's written or not.
   *  Setting this to <tt>false</tt> (the default) will make things
   *  fast, and setting it to <tt>true</tt> will make the log
   *  always up-to-date.
   *  </TD>
   *  <TD VALIGN=TOP>no (default is <tt>false</tt>)</TD>
   *  </TR>
   *
   *  </TABLE><P>
   *
   */
  public void configure(Object o, Element e)
  throws SyslogInitException
  {
    super.configure(o, e);

    TimeRolloverLog log = (TimeRolloverLog)o;

    String tmp = e.getChildTextTrim("baseName", e.getNamespace());
    if (tmp != null)
    {
      log.setBaseFilename(tmp);
    }
    else
    {
      throw new IllegalArgumentException(MessageFormat.format(
        Syslog.getResourceString(MessageConstants.XML_MUST_SPECIFY_PARAM_MESSAGE),
        new Object[] { "baseName" } ));
    }

    tmp = e.getChildTextTrim("append", e.getNamespace());
    log.setAppend("true".equalsIgnoreCase(tmp));

    tmp = e.getChildTextTrim("autoFlush", e.getNamespace());
    log.setAutoFlush("true".equalsIgnoreCase(tmp));

    log.setFileExtension(e.getChildTextTrim("extension", e.getNamespace()));

    tmp = e.getChildTextTrim("nameFormat", e.getNamespace());
    if (tmp != null)
      log.setNameFormat(tmp);
    else
      log.setNameFormat("yyyy.MM.dd-HH.mm.ss");

    tmp = e.getChildTextTrim("roll", e.getNamespace());
    if (tmp != null)
    {
      try
      {
        int r = Integer.parseInt(tmp);
        log.setRollType(r);
      }
      catch (NumberFormatException x)
      {
        if (tmp.equals("ROLL_MINUTELY"))
        {
          log.setRollType(TimeRolloverLog.ROLL_MINUTELY);
        }
        else if (tmp.equals("ROLL_HOURLY"))
        {
          log.setRollType(TimeRolloverLog.ROLL_HOURLY);
        }
        else if (tmp.equals("ROLL_DAILY"))
        {
          log.setRollType(TimeRolloverLog.ROLL_DAILY);
        }
        else if (tmp.equals("ROLL_MONTHLY"))
        {
          log.setRollType(TimeRolloverLog.ROLL_MONTHLY);
        }
        else if (tmp.equals("ROLL_WEEKLY"))
        {
          log.setRollType(TimeRolloverLog.ROLL_WEEKLY);
          String rollDay = e.getChildTextTrim("weeklyRollDay", e.getNamespace());
          if (rollDay != null)
          {
              if ("MONDAY".equals(rollDay))
                  log.setWeeklyRollDay(Calendar.MONDAY);
              else if ("TUESDAY".equals(rollDay))
                  log.setWeeklyRollDay(Calendar.TUESDAY);
              else if ("WEDNESDAY".equals(rollDay))
                  log.setWeeklyRollDay(Calendar.WEDNESDAY);
              else if ("THURSDAY".equals(rollDay))
                  log.setWeeklyRollDay(Calendar.THURSDAY);
              else if ("FRIDAY".equals(rollDay))
                  log.setWeeklyRollDay(Calendar.FRIDAY);
              else if ("SATURDAY".equals(rollDay))
                  log.setWeeklyRollDay(Calendar.SATURDAY);
              else if ("SUNDAY".equals(rollDay))
                  log.setWeeklyRollDay(Calendar.SUNDAY);
              else
                  throw new IllegalArgumentException(MessageFormat.format(
                    Syslog.getResourceString(MessageConstants.TIMEROLLOVERLOG_BAD_ROLL_DAY_VALUE_MESSAGE),
                    new Object[] { "MONDAY", "SUNDAY" }));
          }
        }
        else
        {
          throw new IllegalArgumentException(MessageFormat.format(
            Syslog.getResourceString(MessageConstants.TIMEROLLOVER_ILLEGAL_ROLL_VALUE_MESSAGE),
            new Object[] { "roll", "ROLL_MINUTELY", "ROLL_HOURLY", "ROLL_DAILY", "ROLL_MONTHLY", "ROLL_WEEKLY" }));
        }
      }
    }
    else
    {
      throw new IllegalArgumentException(MessageFormat.format(
        Syslog.getResourceString(MessageConstants.XML_MUST_SPECIFY_PARAM_MESSAGE),
        new Object[] { "roll" } ));
    }

    log.rollover(new Date());
  }

  public Element getConfiguration(Object o, Element element)
  {
    Element e = super.getConfiguration(o, element);

    TimeRolloverLog log = (TimeRolloverLog)o;

    Element file = new Element("baseName");
    file.setText(log.getBaseFilename());
    e.getChildren().add(file);

    if (log.getFileExtension() != null)
    {
      Element ext = new Element("extension");
      ext.setText(log.getFileExtension());
      e.getChildren().add(ext);
    }

    Element append = new Element("append");
    append.setText(String.valueOf(log.getAppend()));
    e.getChildren().add(append);

    Element af = new Element("autoFlush");
    af.setText(String.valueOf(log.getAutoFlush()));
    e.getChildren().add(af);

    Element roll = new Element("roll");
    if (log.getRollType() == TimeRolloverLog.ROLL_MINUTELY)
      roll.setText("ROLL_MINUTELY");
    if (log.getRollType() == TimeRolloverLog.ROLL_HOURLY)
      roll.setText("ROLL_HOURLY");
    if (log.getRollType() == TimeRolloverLog.ROLL_DAILY)
      roll.setText("ROLL_DAILY");
    if (log.getRollType() == TimeRolloverLog.ROLL_MONTHLY)
      roll.setText("ROLL_MONTHLY");
    if (log.getRollType() == TimeRolloverLog.ROLL_WEEKLY)
      roll.setText("ROLL_WEEKLY");
    e.getChildren().add(roll);

    if (log.getRollType() == TimeRolloverLog.ROLL_WEEKLY)
    {
        Element rollDay = new Element("weeklyRollDay");
        switch (log.getWeeklyRollDay())
        {
            case Calendar.MONDAY:
                rollDay.setText("MONDAY");
                break;
            case Calendar.TUESDAY:
                rollDay.setText("TUESDAY");
                break;
            case Calendar.WEDNESDAY:
                rollDay.setText("WEDNESDAY");
                break;
            case Calendar.THURSDAY:
                rollDay.setText("THURSDAY");
                break;
            case Calendar.FRIDAY:
                rollDay.setText("FRIDAY");
                break;
            case Calendar.SATURDAY:
                rollDay.setText("SATURDAY");
                break;
            case Calendar.SUNDAY:
                rollDay.setText("SUNDAY");
                break;
        }
        e.getChildren().add(rollDay);
    }

    return e;
  }
}
