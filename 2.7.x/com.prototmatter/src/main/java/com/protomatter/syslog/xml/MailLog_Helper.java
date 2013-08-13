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
 *  XML configuration helper for <tt>MailLog</tt>.
 */
public class MailLog_Helper
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
   *  &lt;Logger class="com.protomatter.syslog.MailLog" &gt;
   *
   *    <font color="#888888">&lt;!--
   *     Config params from {@link BasicLogger_Helper#configure(Object,Element) BasicLogger_Helper} can
   *     get inserted here.
   *    --&gt;</font>
   *
   *    &lt;SubjectFormat class="<i>MessageSubjectFormatClass</i>" &gt;
   *      <font color="#888888">&lt;!-- message subject formatter elements --&gt;</font>
   *    &lt;/SubjectFormat&gt;
   *
   *    &lt;workQueue&gt;<i>queueName</i>&lt;/workQueue&gt;
   *    &lt;server&gt;<i>hostname</i>&lt;/server&gt;
   *    &lt;port&gt;<i>port</i>&lt;/port&gt;
   *    &lt;html&gt;<i>true|false</i>&lt;/html&gt;
   *    &lt;to&gt;<i>to-address-list</i>&lt;/to&gt;
   *    &lt;cc&gt;<i>cc-address-list</i>&lt;/cc&gt;
   *    &lt;bcc&gt;<i>bcc-address-list</i>&lt;/bcc&gt;
   *    &lt;from&gt;<i>from-address</i>&lt;/from&gt;
   *    &lt;fromName&gt;<i>from-name</i>&lt;/fromName&gt;
   *    &lt;transportAgentName&gt;<i>ta-name</i>&lt;/transportAgentName&gt;
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
   *  <TD VALIGN=TOP><TT>workQueue</TT></TD>
   *  <TD VALIGN=TOP>The symbolic name of a work queue
   *  to use for sending messages.
   *  </TD>
   *  <TD VALIGN=TOP>yes</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>server</TT></TD>
   *  <TD VALIGN=TOP>The SMTP server hostname or IP address.
   *  </TD>
   *  <TD VALIGN=TOP>yes</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>port</TT></TD>
   *  <TD VALIGN=TOP>The port number on the SMTP server to connect to.
   *  </TD>
   *  <TD VALIGN=TOP>no (default is <tt>25</tt>)</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>html</TT></TD>
   *  <TD VALIGN=TOP>Should the message be sent as a MIME message?
   *      <tt>true</tt> or <tt>false</tt>.  If this is
   *      set to <tt>true</tt>, it is expected that the
   *      message formatter will actually produce HTML.
   *  </TD>
   *  <TD VALIGN=TOP>no (default is <tt>false</tt>)</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>to</TT></TD>
   *  <TD VALIGN=TOP>A comma-separated list of email addresses to send
   *      messages to.
   *  </TD>
   *  <TD VALIGN=TOP>yes</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>cc</TT></TD>
   *  <TD VALIGN=TOP>A comma-separated list of email addresses to
   *      include in the CC (carbon-copy) list.
   *  </TD>
   *  <TD VALIGN=TOP>no</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>bcc</TT></TD>
   *  <TD VALIGN=TOP>A comma-separated list of email addresses to
   *      include in the BCC (blind carbon-copy) list.
   *  </TD>
   *  <TD VALIGN=TOP>no</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>from</TT></TD>
   *  <TD VALIGN=TOP>The email address that the mail should appear
   *      to be from.
   *  </TD>
   *  <TD VALIGN=TOP>yes</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>fromName</TT></TD>
   *  <TD VALIGN=TOP>The name (not address) that the mail should
   *      appear to be from.
   *  </TD>
   *  <TD VALIGN=TOP>no</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>transportAgentName</TT></TD>
   *  <TD VALIGN=TOP>The transport agent name that this
   *   SMTP client identifies itself as.  The default is
   *   "<TT>com.protomatter.syslog.SMTPMailTransport</TT>".
   *  </TD>
   *  <TD VALIGN=TOP>no</TD>
   *  </TR>
   *
   *  </TABLE><P>
   */
  public void configure(Object o, Element e)
  throws SyslogInitException
  {
    super.configure(o, e);

    MailLog log = (MailLog)o;

    String smtpServer = null;
    int port = 25;

    String fromName = "Syslog";
    String fromAddress = null;
    Vector to = new Vector();
    Vector cc = new Vector();
    Vector bcc = new Vector();
    boolean html = false;

    String sf = "SubjectFormat";
    String classString = "class";

    Element formatElement = e.getChild(sf, e.getNamespace());
    if (formatElement != null)
    {
      String formatClass = formatElement.getAttributeValue(classString);
      if (formatClass == null)
      {
        throw new IllegalArgumentException(
          MessageFormat.format(
            Syslog.getResourceString(MessageConstants.XML_MUST_SPECIFY_ATTRIBUTE_MESSAGE),
            new Object[] { classString, sf } ));
      }
      try
      {
        SyslogMailSubjectFormatter subjectFormat =
          (SyslogMailSubjectFormatter)Class.forName(formatClass).newInstance();
        XMLConfigHelper helper = XMLConfigUtil.getConfigHelper(subjectFormat);
        try
        {
          helper.configure(subjectFormat, formatElement);
        }
        catch (Exception x)
        {
          x.printStackTrace();
          throw new IllegalArgumentException(MessageFormat.format(
            Syslog.getResourceString(MessageConstants.MAILLOG_CANNOT_CONFIGURE_SUBJECT_FORMAT_MESSAGE),
            new Object[] { formatClass }));
        }
      }
      catch (Exception x)
      {
        x.printStackTrace();
        throw new IllegalArgumentException(MessageFormat.format(
          Syslog.getResourceString(MessageConstants.MAILLOG_CANNOT_CONFIGURE_SUBJECT_FORMAT_MESSAGE),
          new Object[] { formatClass }));
      }
    }

    String tmp = e.getChildTextTrim("server", e.getNamespace());
    if (tmp != null)
    {
      log.setMailServer(tmp);
    }
    else
    {
      throw new IllegalArgumentException(
        MessageFormat.format(
          Syslog.getResourceString(MessageConstants.XML_MUST_SPECIFY_PARAM_MESSAGE),
          new Object[] { "server" } ));
    }

    tmp = e.getChildTextTrim("workQueue", e.getNamespace());
    if (tmp != null)
    {
      log.setWorkQueue(tmp);
    }
    else
    {
      throw new IllegalArgumentException(
        MessageFormat.format(
          Syslog.getResourceString(MessageConstants.XML_MUST_SPECIFY_PARAM_MESSAGE),
          new Object[] { "workQueue" } ));
    }

    tmp = e.getChildTextTrim("port", e.getNamespace());
    if (tmp != null)
      log.setMailPort(Integer.parseInt(tmp));

    tmp = e.getChildTextTrim("html", e.getNamespace());
    log.setHTML("true".equalsIgnoreCase(tmp));

    tmp = e.getChildTextTrim("from", e.getNamespace());
    if (tmp != null)
    {
      log.setFromAddress(tmp);
    }
    else
    {
      throw new IllegalArgumentException(
        MessageFormat.format(
          Syslog.getResourceString(MessageConstants.XML_MUST_SPECIFY_PARAM_MESSAGE),
          new Object[] { "from" } ));
    }

    log.setFromName(e.getChildTextTrim("fromName", e.getNamespace()));

    tmp = e.getChildTextTrim("to", e.getNamespace());
    if (tmp != null)
    {
      StringTokenizer st = new StringTokenizer(tmp, ", ");
      while (st.hasMoreTokens())
        to.add(st.nextToken());
      log.setTo(to);
    }

    tmp = e.getChildTextTrim("cc", e.getNamespace());
    if (tmp != null)
    {
      StringTokenizer st = new StringTokenizer(tmp, ", ");
      while (st.hasMoreTokens())
        cc.add(st.nextToken());
      log.setCC(cc);
    }

    tmp = e.getChildTextTrim("bcc", e.getNamespace());
    if (tmp != null)
    {
      StringTokenizer st = new StringTokenizer(tmp, ", ");
      while (st.hasMoreTokens())
        bcc.add(st.nextToken());
      log.setBCC(bcc);
    }

    if (to.size() == 0)
    {
      throw new IllegalArgumentException(
        MessageFormat.format(
          Syslog.getResourceString(MessageConstants.XML_MUST_SPECIFY_PARAM_MESSAGE),
          new Object[] { "to" } ));
    }

    tmp = e.getChildTextTrim("transportAgentName", e.getNamespace());
    if (tmp != null)
    {
      log.setTransportAgent(tmp);
    }

    log.init();
  }

  public Element getConfiguration(Object o, Element element)
  {
    Element e = super.getConfiguration(o, element);

    MailLog log = (MailLog)o;

    Vector to = log.getTo();
    Vector cc = log.getCC();
    Vector bcc = log.getBCC();

    e.getChildren().add((new Element("workQueue"))
      .setText(log.getWorkQueue()));
    e.getChildren().add((new Element("server"))
      .setText(log.getMailServer()));
    e.getChildren().add((new Element("port"))
      .setText(String.valueOf(log.getMailPort())));
    e.getChildren().add((new Element("html"))
      .setText(String.valueOf(log.getHTML())));
    e.getChildren().add((new Element("from"))
      .setText(log.getFromAddress()));

    if (log.getFromName() != null)
      e.getChildren().add((new Element("fromName")).setText(log.getFromName()));

    e.getChildren().add((new Element("to")).setText(assembleAddressList(to)));

    if (cc.size() > 0)
      e.getChildren().add((new Element("cc")).setText(assembleAddressList(cc)));
    if (bcc.size() > 0)
      e.getChildren().add((new Element("bcc")).setText(assembleAddressList(bcc)));

    Object formatClass = log.getSubjectFormatter();
    Element formatElement = new Element("SubjectFormat");
    formatElement.setAttribute("class", formatClass.getClass().getName());

    try
    {
      XMLConfigHelper helper = XMLConfigUtil.getConfigHelper(formatClass);
      formatElement = helper.getConfiguration(formatClass, formatElement);
      e.getChildren().add(formatElement);
    }
    catch (Exception x)
    {
      x.printStackTrace();
    }

    e.getChildren().add((new Element("transportAgentName")).setText(log.getTransportAgent()));

    return e;
  }

  private String assembleAddressList(Vector list)
  {
    StringBuffer b = new StringBuffer();
    Enumeration e = list.elements();
    while (e.hasMoreElements())
    {
      b.append(e.nextElement());
      if (e.hasMoreElements())
        b.append(", ");
    }
    return b.toString();
  }
}
