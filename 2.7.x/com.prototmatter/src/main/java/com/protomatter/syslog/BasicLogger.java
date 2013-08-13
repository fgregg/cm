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

import java.net.*;
import java.util.*;
import java.text.*;

/**
 *  A base class for Syslogger implementations.
 *  This class provides common functions for setting
 *  the date format for log entries and for formatting dates.
 *  The default policy used by this logger is the
 *  {@link SimpleLogPolicy SimpleLogPolicy} policy.  The
 *  default text formatter is the
 *  {@link SimpleSyslogTextFormatter SimpleSyslogTextFormatter}
 *  formatter.  If this class is used in JDK 1.4, the
 *  defauly text formatter is the
 *  {@link JDK14SyslogTextFormatter JDK14SyslogTextFormatter}.
 *
 *  @see com.protomatter.syslog.xml.BasicLogger_Helper XML configuration class
 */
public abstract class BasicLogger
implements Syslogger
{
  protected LogPolicy policy = new SimpleLogPolicy();
  protected LogPolicy realPolicy = null;
  protected SyslogTextFormatter formatter = null;
  private String name = null;

  /**
   *  The default constructor -- configure() will need to be called.
   */
  public BasicLogger()
  {
    super();

    formatter = new SimpleSyslogTextFormatter();
  }

  /**
   *  Suspend the operation of this logger.  After suspending
   *  a logger, it will no longer accept new messages.
   *  Any messages alread received an in a buffer may or
   *  may not be written.
   */
  public void suspend()
  {
      this.realPolicy = this.policy;
      this.policy = new FalseLogPolicy();
  }

  /**
   *  Resume operations with this logger.  Any new messages
   *  will be processed normally.
   */
  public void resume()
  {
      this.policy = this.realPolicy;
      this.realPolicy = null;
  }

  /**
   *  Determine if this logger is suspended or not.
   */
  public boolean isSuspended()
  {
      return (this.realPolicy != null);
  }

  /**
   *  Set the log policy used by this logger.
   */
  public void setPolicy(LogPolicy policy)
  {
    this.policy = policy;
  }

  /**
   *  Get the log policy used by this logger.
   */
  public LogPolicy getPolicy()
  {
    return this.policy;
  }

  /**
   *  Determine if it's likely that a message from the given
   *  logger at the given level on the given channel will be
   *  paid attention to.  This method delegates the decision
   *  to the <TT>LogPolicy</TT> that's being used.  The message
   *  generated to query the policy has everything except the
   *  message, detail and hostname filled in.
   *
   *  @see LogPolicy
   *  @deprecated
   */
  public boolean mightLog(Object logger, int level, String channel)
  {
    SyslogMessage m = new SyslogMessage();
    m.level = level;
    m.channel = channel;
    m.thread = Thread.currentThread();

    m.logger = logger;
    if (logger != null)
    {
      // get the name of the logger's class.
      if (logger instanceof Class)
        m.loggerClassname = ((Class)logger).getName();
      else
        m.loggerClassname = logger.getClass().getName();
    }
    else
    {
      m.loggerClassname = "?";
    }

    // current time.
    m.time = System.currentTimeMillis();

    // ask the policy what it thinks.
    return policy.shouldLog(m);
  }

  /**
   *  Set the log formatter object used by this logger.
   */
  public void setTextFormatter(SyslogTextFormatter formatter)
  {
    this.formatter = formatter;
  }

  /**
   *  Get the log formatter object used by this logger.
   */
  public SyslogTextFormatter getTextFormatter()
  {
    return this.formatter;
  }

  /**
   *  A utility method to see if the current log policy says we
   *  should pay attention to this message.
   */
  protected boolean shouldLog(SyslogMessage message)
  {
    return policy.shouldLog(message);
  }

  /**
   *  Format the log entry using the current log formatter.
   *
   *  @see SimpleSyslogTextFormatter
   */
  protected void formatLogEntry(StringBuffer b, SyslogMessage message)
  {
    formatter.formatLogEntry(b, message);
  }

  /**
   *  Reset the text formatter's date format.  This is
   *  basically a kludge for loggers that rotate
   *  their logs.
   *
   *  @see SyslogTextFormatter
   */
  protected void resetDateFormat()
  {
    formatter.resetDateFormat();
  }

  /**
   *  Set this logger's name.
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   *  Get this logger's name.
   */
  public String getName()
  {
    return this.name;
  }
}
