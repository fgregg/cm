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

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

import javax.jms.*;
import javax.naming.*;

import com.protomatter.util.*;

/**
 *  A logger that writes messages to JMS.
 *  The JMS session used has no transaction attribute
 *  itself, so it will obey any JTS transaction context
 *  which is currently active.<P>
 *
 *  The {@link SyslogServer SyslogServer} class can be used as
 *  a standalone JMS message receiver.  Please see the JavaDoc
 *  for that class for more information.
 *
 *  @see com.protomatter.syslog.xml.JMSLog_Helper XML configuration class
 */
public class JMSLog
extends BasicLogger
implements JMSConstants
{
  private String topicName = null;
  private String factoryName = null;
  private String user = null;
  private String pass = null;

  private boolean persistent = false;
  private int priority = 5;
  private int ttl = 30 * 60 * 1000; // 30 minutes

  private TopicSession session = null;
  private TopicPublisher publisher = null;
  private boolean running = false;

  /**
   *  You will need to call the configure() method if
   *  you use this constructor.
   */
  public JMSLog()
  {
    super();
  }

  /**
   *  Get the message priority.
   */
  public int getPriority()
  {
    return this.priority;
  }
  /**
   *  Set the message priority.  Default is 5.
   */
  public void setPriority(int priority)
  {
    this.ttl = priority;
  }

  /**
   *  Get the message TTL.
   */
  public int getTTL()
  {
    return this.ttl;
  }
  /**
   *  Get the message TTL.  Default is 30 minutes.
   */
  public void setTTL(int ttl)
  {
    this.ttl = ttl;
  }

  /**
   *  Get the message persistence flag.
   */
  public boolean getPersistent()
  {
    return this.persistent;
  }
  /**
   *  Set the message persistence flag.  Default is false.
   */
  public void setPersistent(boolean persistent)
  {
    this.persistent = persistent;
  }

  /**
   *  Get the JNDI name of the JMS topic we'll publish to.
   */
  public String getTopicName()
  {
    return this.topicName;
  }
  /**
   *  Set the JNDI name of the JMS topic we'll publish to.
   */
  public void setTopicName(String topicName)
  {
    this.topicName = topicName;
  }

  /**
   *  Get the JNDI name of the JMS connection factory we'll use.
   */
  public String getFactoryName()
  {
    return this.factoryName;
  }
  /**
   *  Set the JNDI name of the JMS connection factory we'll use.
   */
  public void setFactoryName(String factoryName)
  {
    this.factoryName = factoryName;
  }

  /**
   *  Get the username for authenticating to the JMS connection.
   *  THIS IS NOT THE JNDI CREDENTIALS.
   */
  public String getUser()
  {
    return this.user;
  }
  /**
   *  Set the username for authenticating to the JMS connection.
   *  THIS IS NOT THE JNDI CREDENTIALS.
   */
  public void setUser(String user)
  {
    this.user = user;
  }

  /**
   *  Get the password for authenticating to the JMS connection.
   *  THIS IS NOT THE JNDI CREDENTIALS.
   */
  public String getPass()
  {
    return this.pass;
  }
  /**
   *  Set the password for authenticating to the JMS connection.
   *  THIS IS NOT THE JNDI CREDENTIALS.
   */
  public void setPass(String pass)
  {
    this.pass = pass;
  }

  /**
   *  Write a log message.  A <tt>javax.jms.TextMessage</tt>
   *  is created.  The text of the message is set to the
   *  detail formatting output from text formatter
   *  configured for this logger.<P>
   *
   *  Message properties are set as follows:<P>
   *
   *  <TABLE BORDER=1 CELLPADDING=2 CELLSPACING=0 WIDTH="90%">
   *
   *  <TR CLASS="TableHeadingColor">
   *  <TD><B>name</B></TD>
   *  <TD><B>type</B></TD>
   *  <TD><B>value</B></TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP>JMS_PROP_HOST ("<TT>host</TT>")</TD>
   *  <TD VALIGN=TOP>String</TD>
   *  <TD>Originating host IP address.
   *  </TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP>JMS_PROP_TIME ("<TT>time</TT>")</TD>
   *  <TD VALIGN=TOP>Long</TD>
   *  <TD>Time the message was sent.
   *  </TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP>JMS_PROP_CHANNEL ("<TT>channel</TT>")</TD>
   *  <TD VALIGN=TOP>String</TD>
   *  <TD>Channel name.
   *  </TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP>JMS_PROP_LOGGER ("<TT>logger</TT>")</TD>
   *  <TD VALIGN=TOP>String</TD>
   *  <TD>Full class name of the logger.
   *  </TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP>JMS_PROP_MESSAGE ("<TT>message</TT>")</TD>
   *  <TD VALIGN=TOP>String</TD>
   *  <TD>Short message.
   *  </TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP>JMS_PROP_LEVEL ("<TT>level</TT>")</TD>
   *  <TD VALIGN=TOP>Integer</TD>
   *  <TD>Severity level.
   *  </TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP>JMS_PROP_THREAD ("<TT>thread</TT>")</TD>
   *  <TD VALIGN=TOP>String</TD>
   *  <TD>Originating thread's <tt>getName()</tt> value.
   *  </TD>
   *  </TR>
   *
   *  </TABLE>
   */
  public final void log(SyslogMessage sm)
  {
    if (!running)
      return;

    try
    {
      String text = null;
      if (sm.detail == null)
      {
        text = "";
      }
      else
      {
        StringBuffer b = new StringBuffer(256);
        formatter.formatMessageDetail(b, sm);
        text = b.toString();
      }
      String ip = sm.host.getHostAddress();
      String msg = (sm.msg == null) ? "" : sm.msg.toString();
      String threadName = (sm.thread != null) ? sm.thread.getName() : "";

      // session and sender access is single threaded
      // according to the JMS spec.
      synchronized(session)
      {
        TextMessage m = session.createTextMessage();
        if (text != null)
          m.setText(text);

        m.setStringProperty(JMS_PROP_MSG_TYPE, JMS_PROP_MSG_TYPE_VALUE);

        m.setStringProperty(JMS_PROP_HOST, ip);
        m.setLongProperty(JMS_PROP_TIME, sm.time);
        m.setStringProperty(JMS_PROP_CHANNEL, sm.channel);
        m.setStringProperty(JMS_PROP_LOGGER, sm.loggerClassname);
        m.setStringProperty(JMS_PROP_MESSAGE, msg);
        m.setIntProperty(JMS_PROP_LEVEL, sm.level);
        m.setStringProperty(JMS_PROP_THREAD, threadName);

        publisher.publish(m);
      }
    }
    catch (JMSException x)
    {
      System.err.println("############################################################");
      System.err.println(MessageFormat.format(
        Syslog.getResourceString(MessageConstants.JMS_ATTEMPT_MESSAGE_1),
        new Object[] { "JMSLog" }));
      System.err.println(MessageFormat.format(
        Syslog.getResourceString(MessageConstants.JMS_ATTEMPT_MESSAGE_2),
        new Object[] { getName() } ));
      System.err.println("");
      System.err.println(Syslog.getResourceString(MessageConstants.JMS_ATTEMPT_CAUSE_MESSAGE));
      x.printStackTrace();
      System.err.println("");
      System.err.println(Syslog.getResourceString(MessageConstants.JMS_ATTEMPT_ORIGINAL_MESSAGE));
      StringBuffer b = new StringBuffer(256);
      (new SimpleSyslogTextFormatter()).formatLogEntry(b, sm);
      System.err.println(b);
      System.err.println("############################################################");
    }
  }

  /**
   *  Prepare for shutdown.
   */
  public void shutdown()
  {
    this.running = false;
  }

  public void start()
  {
    try
    {
      // hopefully we're in an appserver here ;-)
      Context ctx = new InitialContext();

      Topic topic = (Topic)ctx.lookup(topicName);

      TopicConnectionFactory tcf =
        (TopicConnectionFactory)ctx.lookup(factoryName);

      TopicConnection connection = null;
      if (user != null)
        connection = tcf.createTopicConnection(user, pass);
      else
        connection = tcf.createTopicConnection();

      session =
        connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

      connection.start();

      publisher = session.createPublisher(topic);
      publisher.setPriority(this.priority);
      publisher.setTimeToLive(this.ttl);
      publisher.setDeliveryMode(this.persistent
        ? DeliveryMode.PERSISTENT
        : DeliveryMode.NON_PERSISTENT);

      running = true;
    }
    catch (NamingException nx)
    {
      throw new ChainedRuntimeException(MessageFormat.format(
        Syslog.getResourceString(MessageConstants.JMS_NAMING_EXCEPITON_MESSAGE),
        new Object[] { "NamingException", "JMSLog" }), nx);
    }
    catch (JMSException jx)
    {
      throw new ChainedRuntimeException(MessageFormat.format(
        Syslog.getResourceString(MessageConstants.JMS_JMS_EXCEPITON_MESSAGE),
        new Object[] { "JMSException", "JMSLog" }), jx);
    }
  }

  public void flush()
  {
    // do nothing.
  }
}
