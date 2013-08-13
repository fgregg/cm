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

import com.protomatter.util.*;
import com.protomatter.pool.*;

/**
 *  A logger that sends email.
 *
 *  @see com.protomatter.syslog.xml.MailLog_Helper XML configuration class
 */
public class MailLog
extends BasicLogger
{
   private SMTPMailTransport transport = null;
   private String smtpServer = null;
   private int port = 25;
   private String fromName = "Syslog";
   private String fromAddress = null;
   private Vector to = new Vector();
   private Vector cc = new Vector();
   private Vector bcc = new Vector();
   private boolean html = false;
   private String workQueue = null;

   private SyslogMailSubjectFormatter
     subjectFormat = new SimpleSyslogMailSubjectFormatter();

   /**
    *  Create a new mail log that communicates with the given SMTP
    *  on port 25.
    */
   public MailLog(String workQueueName, String smtpServer)
   {
     this(workQueueName, smtpServer, 25);
   }

   /**
    *  Create a new mail log that communicates with the given SMTP
    *  on the given port.
    */
   public MailLog(String workQueueName, String smtpServer, int port)
   {
     this();
     transport = new SMTPMailTransport(smtpServer, port);
     setWorkQueue(workQueueName);
   }

   /**
    *  You will need to call the configure() method if you use this constructor.
    */
   public MailLog()
   {
     super();
   }

   /**
    *  Set the transport agent name.
    */
   public void setTransportAgent(String agentName)
   {
     transport.setTransportAgentName(agentName);
   }
   /**
    *  Get the transport agent name.
    */
   public String getTransportAgent()
   {
     return transport.getTransportAgentName();
   }

   /**
    *  Set the human-readable name that the message will appear to be from.
    */
   public void setFromName(String fromName)
   {
     this.fromName = fromName;
   }
   /**
    *  Get the human-readable name that the message will appear to be from.
    */
   public String getFromName()
   {
     return this.fromName;
   }

   /**
    *  Set the email address the message will appear to come from.
    */
   public void setFromAddress(String fromAddress)
   {
     this.fromAddress = fromAddress;
   }
   /**
    *  Get the email address the message will appear to come from.
    */
   public String getFromAddress()
   {
     return this.fromAddress;
   }

   /**
    *  Set the hostname of the mail server to use.
    */
   public void setMailServer(String mailServer)
   {
     this.smtpServer = mailServer;
   }
   /**
    *  Get the hostname of the mail server to use.
    */
   public String getMailServer()
   {
     return this.smtpServer;
   }

   /**
    *  Set the port to use on the mail server.  Default is 25.
    */
   public void setMailPort(int port)
   {
     this.port = port;
   }
   /**
    *  Get the port to use on the mail server.
    */
   public int getMailPort()
   {
     return this.port;
   }

   /**
    *  Set the html-email flag.  Default is false.
    */
   public void setHTML(boolean html)
   {
     this.html = html;
   }
   /**
    *  Get the html-email flag.
    */
   public boolean getHTML()
   {
     return this.html;
   }

   /**
    *  Set the list of email addresses which appear in the "To:" header.
    */
   public void setTo(Vector list)
   {
     this.to = list;
   }
   /**
    *  Get the list of email addresses which appear in the "To:" header.
    */
   public Vector getTo()
   {
     return this.to;
   }

   /**
    *  Set the list of email addresses which appear in the "Cc:" header.
    */
   public void setCC(Vector list)
   {
     this.cc = list;
   }
   /**
    *  Get the list of email addresses which appear in the "Cc:" header.
    */
   public Vector getCC()
   {
     return this.cc;
   }

   /**
    *  Set the list of email addresses which appear in the "Bcc:" header.
    */
   public void setBCC(Vector list)
   {
     this.bcc = list;
   }
   /**
    *  Get the list of email addresses which appear in the "Bcc:" header.
    */
   public Vector getBCC()
   {
     return this.bcc;
   }

   /**
    *  Set the name of the work queue that this logger
    *  will actually do work in.  If it is null,
    *  everything will be done synchronously.
    */
   public void setWorkQueue(String workQueue)
   {
     this.workQueue = workQueue;
   }

   /**
    *  Get the name of the work queue that this logger
    *  will actually do work in.  If this return null,
    *  everything will be done synchronously.
    */
   public String getWorkQueue()
   {
     return this.workQueue;
   }

   /**
    *  Set the message subject formatter.
    */
   public void setSubjectFormatter(SyslogMailSubjectFormatter subjectFormat)
   {
     this.subjectFormat = subjectFormat;
   }

   /**
    *  Get the message subject formatter.
    */
   public SyslogMailSubjectFormatter getSubjectFormatter()
   {
     return this.subjectFormat;
   }

   /**
    *  Log a message.
    */
   public final void log(SyslogMessage message)
   {
     if (transport == null)
       return;

     if (workQueue == null)
     {
       sendMail(message);
     }
     else
     {
       // ask syslog to send the message out in the background.
       Syslog.addWork(workQueue, new MailLogRunnable(this, message));
     }
   }

   private class MailLogRunnable
   implements Runnable, ObjectPoolObject
   {
     private MailLog logger = null;
     private SyslogMessage message = null;

     public MailLogRunnable(MailLog logger, SyslogMessage message)
     {
       this.logger = logger;
       this.message = message;
     }

     public void run()
     {
       logger.sendMail(message);
     }

     public void deleteObjectPoolObject()
     {
     }

     public boolean isObjectPoolObjectValid()
     {
       return true;
     }

     public void beforeObjectPoolObjectCheckout()
     {
     }

     public void afterObjectPoolObjectCheckin()
     {
     }
   }

   void sendMail(SyslogMessage message)
   {
     StringBuffer b = new StringBuffer(256);
     formatLogEntry(b, message);

     try
     {
       MailMessage m = new MailMessage();
       m.setTo(to);
       m.setFromAddress(fromAddress);
       m.setFromName(fromName);
       m.setCC(cc);
       m.setBCC(bcc);
       m.setSubject(subjectFormat.formatMessageSubject(message));

       if (html)
       {
         MIMEMessage container = new MIMEMessage();
         MIMEAttachment a
           = new MIMEAttachment("text/html", "message body", b.toString());
         container.addAttachment(a);
         m.setBody(container);
       }
       else
       {
         m.setBody(b.toString());
       }
       transport.sendMessage(m);
     }
     catch (IOException ix)
     {
       System.err.println(MessageFormat.format(
            Syslog.getResourceString(MessageConstants.MAILLOG_CANNOT_WRITE_SMTP_MESSAGE),
            new Object[] { ix.toString() }));
       ix.printStackTrace();
     }
     catch (MailException mx)
     {
       System.err.println(MessageFormat.format(
            Syslog.getResourceString(MessageConstants.MAILLOG_CANNOT_WRITE_SMTP_MESSAGE),
            new Object[] { mx.toString() }));
       mx.printStackTrace();
     }
   }

   /**
    *  Closes down the file and prepares for shutdown.
    */
   public synchronized void shutdown()
   {
     transport = null;
   }

   public void init()
   {
     transport = new SMTPMailTransport(smtpServer, port);
   }

   public void flush()
   {
     // do nothing.
   }
}
