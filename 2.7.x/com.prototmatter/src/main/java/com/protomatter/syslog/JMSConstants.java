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
 *  Constants for JMS-related Syslog functions.
 */
public interface JMSConstants
{
  /**
   *  The message property declaring that the
   *  given JMS message is a syslog message.
   *  This is a String property, and its
   *  value is always "<TT>SyslogMessage</TT>".
   */
  public static final String JMS_PROP_MSG_TYPE = "messagetype";

  /**
   *  The value of the message property declaring that the
   *  given JMS message is a syslog message.
   *  The value is "<TT>SyslogMessage</TT>".
   */
  public static final String JMS_PROP_MSG_TYPE_VALUE = "SyslogMessage";

  /**
   *  The message property of the originating
   *  host's IP address.  The value of this
   *  property is "<tt>host</tt>".
   *  This is a String property.
   */
  public static final String JMS_PROP_HOST = "host";

  /**
   *  The message property of the message send time.
   *  The value of this property is "<tt>time</tt>".
   *  This is a long property.
   */
  public static final String JMS_PROP_TIME = "time";

  /**
   *  The message property of the message channel.
   *  The value of this property is "<tt>channel</tt>".
   *  This is a String property.
   */
  public static final String JMS_PROP_CHANNEL = "channel";

  /**
   *  The message property of the message logger's
   *  class name.  The value of this property is
   *  "<tt>logger</tt>".
   *  This is a String property.
   */
  public static final String JMS_PROP_LOGGER = "logger";

  /**
   *  The message property of the message's
   *  short text.  The value of this property is
   *  "<tt>message</tt>".
   *  This is a String property.
   */
  public static final String JMS_PROP_MESSAGE = "message";

  /**
   *  The message property of the message's
   *  severity level.  The value of this property is
   *  "<tt>level</tt>".
   *  This is an integer property.
   */
  public static final String JMS_PROP_LEVEL = "level";

  /**
   *  The message property of the originating thread's
   *  name.  The value of this property is "<tt>thread</tt>".
   *  This is a String property.
   */
  public static final String JMS_PROP_THREAD = "thread";
}
