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

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

import com.protomatter.xml.*;
import com.protomatter.syslog.*;
import org.jdom.*;

/**
 *  XML configuration helper for <tt>UNIXSyslogLog</tt>.
 */
public class UNIXSyslogLog_Helper
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
     *  &lt;Logger class="com.protomatter.syslog.UNIXSyslogLog" &gt;
     *
     *    <font color="#888888">&lt;!--
     *
     *     Note that the <tt>hostname</tt> attribute of the
     *     <tt>&lt;Syslog&gt;</tt> tag must be specified.
     *
     *     Config params from {@link BasicLogger_Helper#configure(Object,Element) BasicLogger_Helper} can
     *     get inserted here.
     *
     *     <B>Note:</B> The <TT>&lt;Format/&gt;</TT> tag is completely ignored
     *     because the format is dictated by the UNIX
     *     syslog system.
     *    --&gt;</font>
     *
     *    &lt;logServer&gt;<i>hostname or IP address</i>&lt;/logServer&gt;
     *    &lt;port&gt;<i>port</i>&lt;/port&gt;
     *    &lt;facility&gt;<i>facility-id</i>&lt;/facility&gt;
     *    &lt;tag&gt;<i>tag-value</i>&lt;/tag&gt;
     *    &lt;showHostname&gt;<i>true|false</i>&lt;/showHostname&gt;
     *
     *    &lt;messageTemplate&gt;<i>message-body-template</i>&lt;/messageTemplate&gt;
     *
     *    &lt;SeverityMap&gt;
     *      &lt;MapEntry&gt;
     *        &lt;syslog&gt;DEBUG&lt;/syslog&gt;
     *        &lt;unix&gt;DEBUG&lt;/unix&gt;
     *      &lt;/MapEntry&gt;
     *      &lt;MapEntry&gt;
     *        &lt;syslog&gt;INFO&lt;/syslog&gt;
     *        &lt;unix&gt;INFO&lt;/unix&gt;
     *      &lt;/MapEntry&gt;
     *      &lt;MapEntry&gt;
     *        &lt;syslog&gt;WARNING&lt;/syslog&gt;
     *        &lt;unix&gt;WARNING&lt;/unix&gt;
     *      &lt;/MapEntry&gt;
     *      &lt;MapEntry&gt;
     *        &lt;syslog&gt;ERROR&lt;/syslog&gt;
     *        &lt;unix&gt;ERROR&lt;/unix&gt;
     *      &lt;/MapEntry&gt;
     *      &lt;MapEntry&gt;
     *        &lt;syslog&gt;FATAL&lt;/syslog&gt;
     *        &lt;unix&gt;EMERGENCY&lt;/unix&gt;
     *      &lt;/MapEntry&gt;
     *    &lt;/SeverityMap&gt;
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
     *  <TD VALIGN=TOP><TT>logServer</TT></TD>
     *  <TD>The hostname or IP address of the log server that
     *  packets should be sent to.
     *  </TD>
     *  <TD VALIGN=TOP>yes</TD>
     *  </TR>
     *
     *  <TR CLASS="TableRowColor">
     *  <TD VALIGN=TOP><TT>port</TT></TD>
     *  <TD>The port number to send packets to on the log server.
     *  </TD>
     *  <TD VALIGN=TOP>no (default is <TT>514</TT>)</TD>
     *  </TR>
     *
     *  <TR CLASS="TableRowColor">
     *  <TD VALIGN=TOP><TT>facility</TT></TD>
     *  <TD>The UNIX syslog facility ID that messages appear from.
     *  A list can be found <a href="../UNIXSyslogLog.html#setFacility(int)">here</a>.
     *  </TD>
     *  <TD VALIGN=TOP>no (default is <TT>16</TT>)</TD>
     *  </TR>
     *
     *  <TR CLASS="TableRowColor">
     *  <TD VALIGN=TOP><TT>tag</TT></TD>
     *  <TD>Text value of the packet "tag" -- the default
     *  is "<TT>ProtomatterSyslog</TT>" and is generally meant
     *  to be the application or "process" name.
     *  </TD>
     *  <TD VALIGN=TOP>no (default is "<tt>ProtomatterSyslog</tt>")</TD>
     *  </TR>
     *
     *  <TR CLASS="TableRowColor">
     *  <TD VALIGN=TOP><TT>showHostname</TT></TD>
     *  <TD><tt>true</tt> or <tt>false</tt> -- decide if we should
     *  show the hostname before the "tag".  The spec says that you
     *  should do this, but when I tested under Red Hat Linux 7.2,
     *  it just ended up repeating the data.  Your mileage may
     *  vary.
     *  </TD>
     *  <TD VALIGN=TOP>no (default is <tt>false</tt>)</TD>
     *  </TR>
     *
     *  <TR CLASS="TableRowColor">
     *  <TD VALIGN=TOP><TT>messageTemplate</TT></TD>
     *  <TD colspan="2">
     *  The template for formatting the body of the message.  The
     *  following tokens are replaced in this string:<P>
     *  <ul>
     *  <table border=0 cellpadding=3 cellspacing=0>
     *    <tr><td><tt>{CHANNEL}</tt></td><td>Channel name</td></tr>
     *    <tr><td><tt>{CALLER-FULLNAME}</tt></td><td>Full caller class name (with package)</td></tr>
     *    <tr><td><tt>{CALLER}</tt></td><td>Caller class name</td></tr>
     *    <tr><td><tt>{MESSAGE}</tt></td><td>Short message</td></tr>
     *    <tr><td><tt>{THREAD}</tt></td><td>Thread name</td></tr>
     *    <tr><td><tt>{SEVERITY}</tt></td><td>Severity ("<tt>DEBUG</tt>" ... "<tt>FATAL</tt>")</td></tr>
     *  </table>
     *  </ul><P>
     *
     *  default is <nobr>"<tt>[{SEVERITY}] {CALLER}: {MESSAGE}</tt>"</nobr></TD>
     *
     *  </TR>
     *
     *  <TR CLASS="TableRowColor">
     *  <TD VALIGN=TOP><tt>SeverityMap</tt></TD>
     *  <TD VALIGN=TOP>
     *  Contains a set of <TT>&lt;MapEntry&gt;</TT> elements,
     *  each containing a <TT>&lt;syslog&gt;</TT> and
     *  <TT>&lt;unix&gt;</TT> elements.  Valid values for
     *  the <TT>&lt;syslog&gt;</tt> element are <TT>DEBUG</TT>,
     *  <TT>INFO</TT>, <TT>WARNING</TT>, <TT>ERROR</TT> and
     *  <TT>FATAL</TT>.  You <i>must</i> specify map entries for
     *  <i>all</i> of the syslog severities.  Each must map to
     *  a UNIX syslog severity, whose legal values are:
     *  <TT>DEBUG</TT>, <TT>INFO</TT>, <TT>NOTICE</TT>,
     *  <TT>WARNING</TT>, <TT>ERROR</TT>, <TT>CRITICAL</TT>
     *  and <TT>EMERGENCY</TT>.
     *  </TD>
     *  <TD VALIGN=TOP>no (default is shown above)</TD>
     *  </TR>
     *
     *  </TABLE><P>
     *
     */
    public void configure(Object o, Element e)
    throws SyslogInitException
    {
        super.configure(o, e, false, true);

        UNIXSyslogLog log = (UNIXSyslogLog)o;

        String tmp = e.getChildTextTrim("logServer", e.getNamespace());
        if (tmp != null)
        {
            try
            {
                log.setLogServer(InetAddress.getByName(tmp));
            }
            catch (UnknownHostException x)
            {
                throw new IllegalArgumentException(
                    MessageFormat.format(Syslog.getResourceString(MessageConstants.UNIX_UNKNOWN_HOST),
                                         new Object[] { tmp } ));
            }
        }
        else
        {
            throw new IllegalArgumentException(
                MessageFormat.format(Syslog.getResourceString(MessageConstants.XML_MUST_SPECIFY_PARAM_MESSAGE),
                                     new Object[] { "logServer" } ));
        }

        tmp = e.getChildTextTrim("port", e.getNamespace());
        if (tmp != null)
        {
            try
            {
                log.setPort(Integer.parseInt(tmp));
            }
            catch (NumberFormatException x)
            {
                throw new IllegalArgumentException(
                    MessageFormat.format(Syslog.getResourceString(MessageConstants.UNIX_BAD_PORT),
                                         new Object[] { tmp } ));
            }
        }

        tmp = e.getChildTextTrim("tag", e.getNamespace());
        if (tmp != null)
        {
            log.setTag(tmp);
        }

        tmp = e.getChildTextTrim("facility", e.getNamespace());
        if (tmp != null)
        {
            try
            {
                log.setFacility(Integer.parseInt(tmp));
            }
            catch (NumberFormatException x)
            {
                throw new IllegalArgumentException(
                    MessageFormat.format(Syslog.getResourceString(MessageConstants.UNIX_BAD_FACILITY),
                                         new Object[] { tmp } ));
            }
        }

        tmp = e.getChildTextTrim("showHostname", e.getNamespace());
        if (tmp != null)
            log.setShowHostname("true".equalsIgnoreCase(tmp.trim()));

        tmp = e.getChildTextTrim("messageTemplate", e.getNamespace());
        if (tmp != null)
            log.setMessageTemplate(tmp.trim());

        Element element = e.getChild("SeverityMap", e.getNamespace());
        if (element != null)
        {
            Iterator params = element.getChildren("MapEntry", e.getNamespace()).iterator();
            Map severityMap = new HashMap();
            while (params.hasNext())
            {
                Element param = (Element)params.next();
                String syslog = param.getChildTextTrim("syslog", e.getNamespace());
                String unix = param.getChildTextTrim("unix", e.getNamespace());
                int syslogSeverity = 0;
                int unixSeverity = 0;
                if (syslog != null && unix != null)
                {
                    if ("DEBUG".equalsIgnoreCase(syslog))
                        syslogSeverity = Syslog.DEBUG;
                    else if ("INFO".equalsIgnoreCase(syslog))
                        syslogSeverity = Syslog.INFO;
                    else if ("WARNING".equalsIgnoreCase(syslog))
                        syslogSeverity = Syslog.WARNING;
                    else if ("ERROR".equalsIgnoreCase(syslog))
                        syslogSeverity = Syslog.ERROR;
                    else if ("FATAL".equalsIgnoreCase(syslog))
                        syslogSeverity = Syslog.FATAL;
                    else
                        throw new SyslogInitException(MessageFormat.format(
                          Syslog.getResourceString(MessageConstants.UNIX_BAD_SYSLOG_SEVERITY),
                          new Object[] { syslog } ));

                    if ("DEBUG".equalsIgnoreCase(unix))
                        unixSeverity = UNIXSyslogLog.UNIX_DEBUG;
                    else if ("INFO".equalsIgnoreCase(unix))
                        unixSeverity = UNIXSyslogLog.UNIX_INFO;
                    else if ("NOTICE".equalsIgnoreCase(unix))
                        unixSeverity = UNIXSyslogLog.UNIX_NOTICE;
                    else if ("WARNING".equalsIgnoreCase(unix))
                        unixSeverity = UNIXSyslogLog.UNIX_WARNING;
                    else if ("ERROR".equalsIgnoreCase(unix))
                        unixSeverity = UNIXSyslogLog.UNIX_ERROR;
                    else if ("CRITICAL".equalsIgnoreCase(unix))
                        unixSeverity = UNIXSyslogLog.UNIX_CRITICAL;
                    else if ("ALERT".equalsIgnoreCase(unix))
                        unixSeverity = UNIXSyslogLog.UNIX_ALERT;
                    else if ("EMERGENCY".equalsIgnoreCase(unix))
                        unixSeverity = UNIXSyslogLog.UNIX_EMERGENCY;
                    else
                        throw new SyslogInitException(MessageFormat.format(
                          Syslog.getResourceString(MessageConstants.UNIX_BAD_UNIX_SEVERITY),
                          new Object[] { unix } ));

                    severityMap.put(new Integer(syslogSeverity), new Integer(unixSeverity));
                }
            }
            log.setSeverityMap(severityMap);
        }
        else
        {
            log.setSeverityMap(UNIXSyslogLog.DEFAULT_SEVERITY_MAP);
        }

    }

    public Element getConfiguration(Object o, Element element)
    {
        Element e = super.getConfiguration(o, element, false, true);

        UNIXSyslogLog log = (UNIXSyslogLog)o;

        Element logServer = new Element("logServer");
        logServer.setText(log.getLogServer().getHostName());
        e.getChildren().add(logServer);

        Element port = new Element("port");
        port.setText(String.valueOf(log.getPort()));
        e.getChildren().add(port);

        Element facility = new Element("facility");
        facility.setText(String.valueOf(log.getFacility()));
        e.getChildren().add(facility);

        Element tag = new Element("tag");
        tag.setText(log.getTag());
        e.getChildren().add(tag);

        Element showHostname = new Element("showHostname");
        showHostname.setText(String.valueOf(log.getShowHostname()));
        e.getChildren().add(showHostname);

        Element messageTemplate = new Element("messageTemplate");
        messageTemplate.setText(log.getMessageTemplate());
        e.getChildren().add(messageTemplate);

        Element param = new Element("SeverityMap");

        Map severityMap = log.getSeverityMap();

        Element mapEntry = new Element("MapEntry");
        mapEntry.getChildren().add(new Element("syslog").setText("DEBUG"));
        mapEntry.getChildren().add(new Element("unix").setText(getUNIXSeverityName(severityMap, Syslog.DEBUG)));
        param.getChildren().add(mapEntry);

        mapEntry = new Element("MapEntry");
        mapEntry.getChildren().add(new Element("syslog").setText("INFO"));
        mapEntry.getChildren().add(new Element("unix").setText(getUNIXSeverityName(severityMap, Syslog.INFO)));
        param.getChildren().add(mapEntry);

        mapEntry = new Element("MapEntry");
        mapEntry.getChildren().add(new Element("syslog").setText("WARNING"));
        mapEntry.getChildren().add(new Element("unix").setText(getUNIXSeverityName(severityMap, Syslog.WARNING)));
        param.getChildren().add(mapEntry);

        mapEntry = new Element("MapEntry");
        mapEntry.getChildren().add(new Element("syslog").setText("ERROR"));
        mapEntry.getChildren().add(new Element("unix").setText(getUNIXSeverityName(severityMap, Syslog.ERROR)));
        param.getChildren().add(mapEntry);

        mapEntry = new Element("MapEntry");
        mapEntry.getChildren().add(new Element("syslog").setText("FATAL"));
        mapEntry.getChildren().add(new Element("unix").setText(getUNIXSeverityName(severityMap, Syslog.FATAL)));
        param.getChildren().add(mapEntry);


        e.getChildren().add(param);

        return e;
    }

    private static String getUNIXSeverityName(Map severityMap, int syslogSeverity)
    {
        int severity = ((Integer)severityMap.get(new Integer(syslogSeverity))).intValue();
        switch (severity)
        {
            case UNIXSyslogLog.UNIX_DEBUG:
                return "DEBUG";
            case UNIXSyslogLog.UNIX_INFO:
                return "INFO";
            case UNIXSyslogLog.UNIX_NOTICE:
                return "NOTICE";
            case UNIXSyslogLog.UNIX_WARNING:
                return "WARNING";
            case UNIXSyslogLog.UNIX_ERROR:
                return "ERROR";
            case UNIXSyslogLog.UNIX_CRITICAL:
                return "CRITICAL";
            case UNIXSyslogLog.UNIX_ALERT:
                return "ALERT";
            case UNIXSyslogLog.UNIX_EMERGENCY:
                return "EMERGENCY";
        }
        return "GARBAGE";
    }
}
