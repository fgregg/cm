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

/**
 *  An interface for objects that will log things using the Syslog facility.
 */
public interface Syslogger
{
  /**
   *  Log an entry to the log.
   */
  public void log(SyslogMessage message);

  /**
   *  Set the name of this logger.
   */
  public void setName(String name);

  /**
   *  Get the name of this logger.
   */
  public String getName();

  /**
   *  Shutdown this logger.  The implementation should clean up
   *  any resources it has allocated, etc.  After this method
   *  is called, no more log messages will be sent to this logger.
   *  However, if the logger receives messages after the
   *  <tt>shutdown()</tt> method is called, it can assume that
   *  logging has resumed.  This method is called after the
   *  "master switch" inside Syslog has been flipped to "off"
   *  in the <tt>Syslog.shutdown()</tt> method.
   */
  public void shutdown();

  /**
   *  Determine if it's likely that a message at the
   *  given level on the given channel(s) will be logged.
   *  This covers about 99% of the cases where people
   *  need to use the <TT>Syslog.canXXX()</TT> methods.
   *  It's still possible that this method would return
   *  true even though the logger would ignore the message
   *  for some other reason.  You can't please all the
   *  people all the time.
   */
  public boolean mightLog(Object logger, int level, String channel);

  /**
   *  Set the log policy used by this logger.
   */
  public void setPolicy(LogPolicy policy);

  /**
   *  Get the log policy used by this logger.
   */
  public LogPolicy getPolicy();

  /**
   *  Set the log formatter object used by this logger.
   */
  public void setTextFormatter(SyslogTextFormatter formatter);

  /**
   *  Get the log formatter object used by this logger.
   */
  public SyslogTextFormatter getTextFormatter();

  /**
   *  Flush the given logger's output.
   */
  public void flush();
}
