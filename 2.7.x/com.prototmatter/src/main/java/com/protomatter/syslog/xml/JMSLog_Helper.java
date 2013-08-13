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
 *  XML configuration helper for <tt>JMSLog</tt>.
 */
public class JMSLog_Helper
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
  *  &lt;Logger class="com.protomatter.syslog.JMSLog" &gt;
  *
  *    <font color="#888888">&lt;!--
  *     Config params from {@link BasicLogger_Helper#configure(Object,Element) BasicLogger_Helper}
  *     can get inserted here.
  *    --&gt;</font>
  *
  *    &lt;factoryName&gt;<i>JNDI-name</i>&lt;/factoryName&gt;
  *    &lt;topicName&gt;<i>JNDI-name</i>&lt;/topicName&gt;
  *
  *    &lt;connectionUser&gt;<i>UserName</i>&lt;/connectionUser&gt;
  *    &lt;connectionPass&gt;<i>Password</i>&lt;/connectionPass&gt;
  *
  *    &lt;persistent&gt;<i>true|false</i>&lt;/persistent&gt;
  *    &lt;ttl&gt;<i>TimeToLive</i>&lt;/ttl&gt;
  *    &lt;priority&gt;<i>Priority</i>&lt;/priority&gt;
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
  *  <TD VALIGN=TOP><TT>topicName</TT></TD>
  *  <TD>The JNDI location of a JMS Topic to send messages to.
  *  </TD>
  *  <TD VALIGN=TOP>yes</TD>
  *  </TR>
  *
  *  <TR CLASS="TableRowColor">
  *  <TD VALIGN=TOP><TT>factoryName</TT></TD>
  *  <TD>The JNDI location of a JMS TopicConnectionFactory
  *      to use to create the JMS session and publisher.
  *  </TD>
  *  <TD VALIGN=TOP>yes</TD>
  *  </TR>
  *
  *  <TR CLASS="TableRowColor">
  *  <TD VALIGN=TOP><TT>connectionUser</TT></TD>
  *  <TD>If this property is present, it is used as
  *      the username to use in creating the JMS connection.
  *  </TD>
  *  <TD VALIGN=TOP>no</TD>
  *  </TR>
  *
  *  <TR CLASS="TableRowColor">
  *  <TD VALIGN=TOP><TT>connectionPass</TT></TD>
  *  <TD>If this property is present, it is used as
  *      the password to use in creating the JMS connection.
  *  </TD>
  *  <TD VALIGN=TOP>no</TD>
  *  </TR>
  *
  *  <TR CLASS="TableRowColor">
  *  <TD VALIGN=TOP><TT>ttl</TT></TD>
  *  <TD>Message time to live in milliseconds.  Default is
  *      30 minutes.
  *  </TD>
  *  <TD VALIGN=TOP>no</TD>
  *  </TR>
  *
  *  <TR CLASS="TableRowColor">
  *  <TD VALIGN=TOP><TT>priority</TT></TD>
  *  <TD>Message priority.  Default is 5.
  *  </TD>
  *  <TD VALIGN=TOP>no</TD>
  *  </TR>
  *
  *  <TR CLASS="TableRowColor">
  *  <TD VALIGN=TOP><TT>persistent</TT></TD>
  *  <TD>Should persistent JMS be used?  Default is false.
  *  </TD>
  *  <TD VALIGN=TOP>no</TD>
  *  </TR>
  *
  *  </TABLE><P>
  *
  *  For example:  If you are using BEA WebLogic, you can add the
  *  following lines to your <tt>weblogic.properties</tt> file
  *  to configure a JMS topic and connection factory:<P>
  *
  *  <TABLE BORDER=1 CELLPADDING=4 CELLSPACING=0 WIDTH="90%">
  *  <TR><TD>
  *  <PRE><B>
  *  weblogic.jms.topic.syslog=jms.topic.syslog
  *  weblogic.jms.connectionFactoryName.syslog=jms.connection.syslogFactory
  *  </B></PRE>
  *  </TD></TR></TABLE><P>
  *
  *  Then set "<TT>topicName</tt>" to "<TT>jms.topic.syslog</tt>"
  *  and "<TT>factoryName</tt>" to "<TT>jms.connection.syslogFactory</tt>".
  *  You will be able to route syslog messages through that JMS topic to a
  *  remote machine that is running the
  *  {@link com.protomatter.syslog.SyslogServer SyslogServer} program.
  */
  public void configure(Object o, Element e)
  throws SyslogInitException
  {
    super.configure(o, e);
    JMSLog log = (JMSLog)o;

    if (Syslog.getLocalHostName() == null)
      Syslog.setLocalHostName();

    String tmp = null;

    tmp = e.getChildTextTrim("topicName", e.getNamespace());
    if (tmp != null)
      log.setTopicName(tmp);
    else
      throw new IllegalArgumentException(MessageFormat.format(
        Syslog.getResourceString(MessageConstants.XML_MUST_SPECIFY_PARAM_MESSAGE),
        new Object[] { "topicName" } ));

    tmp = e.getChildTextTrim("factoryName", e.getNamespace());
    if (tmp != null)
      log.setFactoryName(tmp);
    else
      throw new IllegalArgumentException(MessageFormat.format(
        Syslog.getResourceString(MessageConstants.XML_MUST_SPECIFY_PARAM_MESSAGE),
        new Object[] { "factoryName" } ));

    tmp = e.getChildTextTrim("connectionUser", e.getNamespace());
    if (tmp != null)
      log.setUser(tmp);

    tmp = e.getChildTextTrim("connectionPass", e.getNamespace());
    if (tmp != null)
      log.setPass(tmp);

    tmp = e.getChildTextTrim("persistent");
    if (tmp != null)
      log.setPersistent(tmp.equalsIgnoreCase("true"));

    tmp = e.getChildTextTrim("ttl");
    if (tmp != null)
      log.setTTL(Integer.parseInt(tmp));

    tmp = e.getChildTextTrim("priority");
    if (tmp != null)
      log.setPriority(Integer.parseInt(tmp));

    log.start();
  }

  public Element getConfiguration(Object o, Element element)
  {
    Element e = super.getConfiguration(o, element);
    JMSLog log = (JMSLog)o;

    e.getChildren().add(
      (new Element("topicName")).setText(log.getTopicName()));
    e.getChildren().add(
      (new Element("factoryName")).setText(log.getFactoryName()));

    if (log.getUser() != null)
    {
      e.getChildren().add(
        (new Element("connectionUser")).setText(log.getUser()));
      e.getChildren().add(
        (new Element("connectionPass")).setText(log.getPass()));
    }

    e.getChildren().add((new Element("persistent"))
      .setText(String.valueOf(log.getPersistent())));
    e.getChildren().add((new Element("priority"))
      .setText(String.valueOf(log.getPriority())));
    e.getChildren().add((new Element("ttl"))
      .setText(String.valueOf(log.getTTL())));
    return e;
  }
}
