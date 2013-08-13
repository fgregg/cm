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

import com.protomatter.syslog.xml.*;
import com.protomatter.util.*;

/**
 *  A standalone log processing server which either
 *  reads messages from a JMS topic, or through RMI.
 *  Reads configuration information from a properties
 *  file, given as the first command-line argument.
 *  System properties will override ones given in the
 *  properties file.<P>
 *
 *  Basic properties are:<P>
 *
 *  <UL><TABLE BORDER=1 CELLPADDING=2 CELLSPACING=0 WIDTH="90%">
 *
 *  <TR CLASS="TableHeadingColor">
 *  <TD><B>name</B></TD>
 *  <TD><B>value</B></TD>
 *  </TR>
 *
 *  <TR CLASS="TableRowColor">
 *  <TD VALIGN=TOP><TT>Syslog.config.xml</TT></TD>
 *  <TD>Path to a syslog configuration XML file.
 *      Messages are pulled off of JMS and sent into
 *      the "local" Syslog instance for processing
 *      according to this configuration file.
 *  </TD>
 *  </TR>
 *
 *  </TABLE></UL><P>
 *
 *  If the server will listen to a JMS topic for messages,
 *  the following properties are used:<P>
 *
 *  <UL><TABLE BORDER=1 CELLPADDING=2 CELLSPACING=0 WIDTH="90%">
 *
 *  <TR CLASS="TableHeadingColor">
 *  <TD><B>name</B></TD>
 *  <TD><B>value</B></TD>
 *  </TR>
 *
 *  <TR CLASS="TableRowColor">
 *  <TD VALIGN=TOP><TT>jms.topic</TT></TD>
 *  <TD>The JNDI name of the JMS topic to listen to.
 *  </TD>
 *  </TR>
 *
 *  <TR CLASS="TableRowColor">
 *  <TD VALIGN=TOP><TT>jms.connection.user</TT></TD>
 *  <TD>A location in JNDI where an instance of
 *      <tt>javax.jms.TopicConnectionFactory</tt> can
 *      be found.
 *  </TD>
 *  </TR>
 *
 *  <TR CLASS="TableRowColor">
 *  <TD VALIGN=TOP><TT>jms.connection.pass</TT></TD>
 *  <TD>Optional.  The password to use when creating the
 *      JMS connection.  This is <i>not</i> the JNDI
 *      credentials.
 *  </TD>
 *  </TR>
 *
 *  <TR CLASS="TableRowColor">
 *  <TD VALIGN=TOP><TT>jms.connection.factory</TT></TD>
 *  <TD>The JNDI location of a JMS connection factory to use.
 *  </TD>
 *  </TR>
 *
 *  <TR CLASS="TableRowColor">
 *  <TD VALIGN=TOP><TT>jms.message.selector</TT></TD>
 *  <TD>Optional.  A JMS message selector for choosing
 *      messages that this server will receive.
 *  </TD>
 *  </TR>
 *
 *  </TABLE></UL><P>
 *
 *  If the server will bind a log receiver
 *  callback object into JNDI on an application server,
 *  the following properties are used:<P>
 *
 *  <UL><TABLE BORDER=1 CELLPADDING=2 CELLSPACING=0 WIDTH="90%">
 *
 *  <TR CLASS="TableHeadingColor">
 *  <TD><B>name</B></TD>
 *  <TD><B>value</B></TD>
 *  </TR>
 *
 *  <TR CLASS="TableRowColor">
 *  <TD VALIGN=TOP><TT>jndi.name</TT></TD>
 *  <TD> The location in JNDI to bind the
 *       receiver object at.  The string
 *       "<tt>com.protomatter.syslog.remote.</tt>"
 *       is prepended to this value.  The
 *       value should not contain
 *       the "<TT>.</TT>" character.
 *  </TD>
 *  </TR>
 *
 *  <TR CLASS="TableRowColor">
 *  <TD VALIGN=TOP><TT>receiver.class</TT></TD>
 *  <TD>Optional.  The full classname of an
 *      object that implements the
 *      <TT>{@link RemoteLogReceiver RemoteLogReceiver}</TT> interface.
 *      The object must have a default (no-argument)
 *      constructor.  The default value for this
 *      property is
 *      <TT>{@link RemoteLogReceiverImpl com.protomatter.syslog.RemoteLogReceiverImpl}</TT>.
 *  </TD>
 *  </TR>
 *
 *  </TABLE></UL><P>
 *
 *  The properties file is also used when creating the
 *  JNDI context (it is passed to the constructor for
 *  <tt>javax.naming.InitialContext</tt>), so the following
 *  properties are also needed, and are specific to the
 *  JNDI provider you're using (WebLogic, etc):<P>
 *
 *  <UL><TABLE BORDER=1 CELLPADDING=2 CELLSPACING=0 WIDTH="90%">
 *
 *  <TR CLASS="TableHeadingColor">
 *  <TD><B>name</B></TD>
 *  <TD><B>value</B></TD>
 *  </TR>
 *
 *  <TR CLASS="TableRowColor">
 *  <TD VALIGN=TOP><TT>java.naming.factory.initial</TT></TD>
 *  <TD>The JNDI provider factory to use.  For instance,
 *  with BEA WebLogic, this should be set to
 *  "<tt>weblogic.jndi.WLInitialContextFactory</tt>".
 *  </TD>
 *  </TR>
 *
 *  <TR CLASS="TableRowColor">
 *  <TD VALIGN=TOP><TT>java.naming.provider.url</TT></TD>
 *  <TD>The JNDI provider URL.  With BEA WebLogic, this
 *  should be set to something like
 *  "<tt>t3://<i>hostname</i>:<i>port</i></tt>".
 *  </TD>
 *  </TR>
 *
 *  </TABLE></UL><P>
 *
 *  See the
 *  JavaDoc for <tt>javax.naming.Context</tt> for more
 *  information on JNDI connection properties.<P>
 *
 *  Here's an example that works with BEA WebLogic Server.  First,
 *  configure a JMS connection factory and a topic in WLS.  Add the
 *  following to your <tt>weblogic.properties</tt> file:<P>
 *
 *  <UL><TABLE BORDER=1 CELLPADDING=4 CELLSPACING=0 WIDTH="90%">
 *  <TR><TD>
 *  <PRE><B>
 *  ## The JMS topic
 *  weblogic.jms.topic.syslogTopic=\
 *    javax.jms.topic.Syslog
 *
 *  ## The JMS connection factory
 *  weblogic.jms.connectionFactoryName.syslogTopicFactory=\ 
 *    javax.jms.connection.syslog
 *
 *
 *  ## The Syslog startup class for WLS
 *  weblogic.system.startupClass.syslog=\
 *    com.protomatter.syslog.SyslogT3Startup
 *
 *  ## The configuration file for Syslog.
 *  java.system.property.Syslog.config.xml=\
 *    /opt/weblogic/weblogic-syslog.xml
 *  </B></PRE>
 *  </TD></TR></TABLE></UL><P>
 *
 *  Now, configure the syslog instance that runs in WLS.  This file is
 *  referenced above in the <tt>weblogic.properties</tt> file as
 *  <tt>/opt/weblogic/weblogic-syslog.xml</tt>.  This configuration
 *  includes a JMS logger and a logger that writes to <tt>System.out</tt>.
 *  <P>
 *
 *  <UL><TABLE BORDER=1 CELLPADDING=4 CELLSPACING=0 WIDTH="90%">
 *  <TR><TD>
 *  <PRE><B>
 *
 *  &lt;Syslog defaultMask="DEBUG"&gt;
 *    &lt;Logger name="out" class="com.protomatter.syslog.PrintWriterLog"&gt;
 *      &lt;stream&gt;System.out&lt;/stream&gt;
 *    &lt;/Logger&gt;
 *
 *    &lt;Logger name="jms" class="com.protomatter.syslog.JMSLog"&gt;
 *      &lt;factoryName&gt;javax.jms.connection.syslog&lt;/factoryName&gt;
 *      &lt;topicName&gt;javax.jms.topic.Syslog&lt;/topicName&gt;
 *    &lt;/Logger&gt;
 *  &lt;/Syslog&gt;
 *  </B></PRE>
 *  </TD></TR></TABLE></UL><P>
 *
 *  Next, configure the Syslog instance that will run inside the
 *  remote log server.  This is a simple configuration that just prints
 *  things out to <tt>System.out</tt>.  We'll refer to this file
 *  as <tt>/home/nate/syslog.xml</tt>.<P>
 *
 *  <UL><TABLE BORDER=1 CELLPADDING=4 CELLSPACING=0 WIDTH="90%">
 *  <TR><TD>
 *  <PRE><B>
 *
 *  &lt;Syslog defaultMask="DEBUG"&gt;
 *    &lt;Logger name="out" class="com.protomatter.syslog.PrintWriterLog"&gt;
 *      &lt;stream&gt;System.out&lt;/stream&gt;
 *    &lt;/Logger&gt;
 *  &lt;/Syslog&gt;
 *  </B></PRE>
 *  </TD></TR></TABLE></UL><P>
 *
 *  Next, we need a configuration file for the remote log server.
 *  <P>
 *
 *  <UL><TABLE BORDER=1 CELLPADDING=4 CELLSPACING=0 WIDTH="90%">
 *  <TR><TD>
 *  <PRE><B>
 *  Syslog.config.xml=/home/nate/syslog.xml
 *
 *  ## The JNDI connection factory to use
 *  java.naming.factory.initial=\
 *    weblogic.jndi.WLInitialContextFactory
 *
 *  ## The JNDI URL of the server to connect to
 *  java.naming.provider.url=\
 *    t3://hostname:port
 *
 *  ## The JMS factory location in JNDI
 *  jms.connection.factory=\
 *    javax.jms.connection.syslog
 *
 *  ## The JMS topic name in JNDI
 *  jms.topic=javax.jms.topic.Syslog
 *  </B></PRE>
 *  </TD></TR></TABLE></UL><P>
 *
 *  Now, pass this properties file to the <tt>SyslogServer</tt> program as its
 *  only command-line argument.  It should come up, connect to the server
 *  and start sucking messages off of JMS.
 *
 *  @see JMSLog
 *  @see RemoteLog
 */
public class SyslogServer
{
  /**
   *  Private constructor so people don't go around creating these.
   */
  private SyslogServer()
  {
    super();
  }

  /**
   *  Start the syslog log server.  The first command-line argument
   *  must be the path to a properties file.
   */
  public static final void main(String args[])
  {
    if (args.length != 1)
    {
      System.out.println(Syslog.getResourceString(MessageConstants.SERVER_USAGE_MESSAGE) + ": SyslogServer config-file");
      System.exit(0);
    }

    try
    {
      System.out.println(MessageFormat.format(
        Syslog.getResourceString(MessageConstants.SERVER_LOADING_MESSAGE),
        new Object[] { args[0] }));
      Properties props = new Properties();
      props.load(new FileInputStream(new File(args[0])));
      Properties systemProps = System.getProperties();
      Enumeration e = systemProps.keys();
      while (e.hasMoreElements())
      {
        String key = (String)e.nextElement();
        props.put(key, systemProps.getProperty(key));
      }

      String syslogConfig = props.getProperty("Syslog.config.xml");
      if (syslogConfig == null)
      {
        System.out.println(MessageFormat.format(
          Syslog.getResourceString(MessageConstants.SERVER_CONFIG_PROP_ERROR_MESSAGE),
          new Object[] { "Syslog.config.xml" }));
        System.exit(0);
      }
      System.out.println(Syslog.getResourceString(MessageConstants.CONFIGURING_SYSLOG_MESSAGE));
      SyslogXML.configure(new File(syslogConfig));

      // the properties file needs to have all the connection info.
      System.out.println(Syslog.getResourceString(MessageConstants.SERVER_INIT_JNDI_MESSAGE));
      Context ctx = new InitialContext(props);

      String topicName = props.getProperty("jms.topic");
      if (topicName != null)
      {
        System.out.println(Syslog.getResourceString(MessageConstants.SERVER_LISTEN_JMS_TOPIC_MESSAGE));
        String factoryName = props.getProperty("jms.connection.factory");

        String user = (String)props.getProperty("jms.connection.user");
        String pass = (String)props.getProperty("jms.connection.pass");

        Topic topic = (Topic)ctx.lookup(topicName);

        TopicConnectionFactory tcf = (TopicConnectionFactory)ctx.lookup(factoryName);
        TopicConnection connection = null;
        if (user != null)
          connection = tcf.createTopicConnection(user, pass);
        else
          connection = tcf.createTopicConnection();
        TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

        String selector = props.getProperty("jms.message.selector");

        TopicSubscriber subscriber = null;
        if (selector != null)
          subscriber = session.createSubscriber(topic, selector, false);
        else
          subscriber = session.createSubscriber(topic);
        subscriber.setMessageListener(new InternalMessageListener());

        System.out.println(Syslog.getResourceString(MessageConstants.SERVER_STARTING_JMS_MESSAGE));
        connection.start();
      }
      else
      {
        String jndiName = props.getProperty("jndi.name");
        jndiName = "com.protomatter.syslog.remote." + jndiName;
        String receiverClass = props.getProperty("receiver.class");
        if (receiverClass == null)
          receiverClass = "com.protomatter.syslog.RemoteLogReceiverImpl";
        System.out.println(MessageFormat.format(
          Syslog.getResourceString(MessageConstants.SERVER_BINDING_MESSAGE),
          new Object[] { jndiName }));
        Class theClass = Class.forName(receiverClass);
        RemoteLogReceiver receiver = (RemoteLogReceiver)theClass.newInstance();

        ctx.rebind(jndiName, receiver);
      }

      System.out.println(Syslog.getResourceString(MessageConstants.SERVER_SUSPEND_MESSAGE));
      Object sync = new Object();
      synchronized(sync)
      {
        sync.wait();
      }
    }
    catch (Exception x)
    {
      x.printStackTrace();
    }
  }

  private static class InternalMessageListener
  implements MessageListener, JMSConstants
  {
    public void onMessage(Message msg)
    {
      try
      {
        if (!(msg instanceof TextMessage))
          return;
        TextMessage m = (TextMessage)msg;
        if (!JMS_PROP_MSG_TYPE_VALUE.equals(m.getStringProperty(JMS_PROP_MSG_TYPE)))
          return;

        long time = m.getLongProperty(JMS_PROP_TIME);
        String host = m.getStringProperty(JMS_PROP_HOST);
        String channel = m.getStringProperty(JMS_PROP_CHANNEL);
        String logger = m.getStringProperty(JMS_PROP_LOGGER);
        String message = m.getStringProperty(JMS_PROP_MESSAGE);
        int level = m.getIntProperty(JMS_PROP_LEVEL);
        String threadName = m.getStringProperty(JMS_PROP_THREAD);
        String detail = m.getText();

        // drop the message into Syslog locally.
        Syslog.log(InetAddress.getByName(host), logger, channel, message, detail, level, null, threadName, time);
      }
      catch (JMSException x)
      {
        x.printStackTrace();
      }
      catch (Exception xx)
      {
        xx.printStackTrace();
      }
    }
  }
}
