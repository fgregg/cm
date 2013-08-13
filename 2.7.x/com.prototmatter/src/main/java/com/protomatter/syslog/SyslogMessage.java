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

import java.util.*;
import java.net.*;

import com.protomatter.util.StackTraceInfo;

/**
 *  A utility class representing all the information needed
 *  to make a syslog call.
 */
public class SyslogMessage
{
   /**
    *  The address of the host making the call.
    */
   public InetAddress host = null;

   /**
    *  The time the call was made.
    */
   public long time = 0L;

   /**
    *  The channel the message is for.
    */
   public String channel = null;

   /**
    *  The object making the syslog call.
    */
   public Object logger = null;

   /**
    *  The classname of the logger.
    */
   public String loggerClassname = null;

   /**
    *  The name of the method being called when
    *  the Syslog method call was made.
    */
   public String callingMethodName = null;

   /**
    *  The line number in the calling object's
    *  source file, if known.
    */
   public int callingMethodLineNumber = StackTraceInfo.LINE_NUMBER_UNKNOWN;

   /**
    *  The message.
    */
   public Object msg = null;

   /**
    *  The detailed message.
    */
   public Object detail = null;

   /**
    *  The log level.
    */
   public int level = 0;

   /**
    *  The thread that made the log request.
    */
   public Thread thread = null;

   /**
    *  The output of <tt>toString()</tt> on the thread that made the log request.
    */
   public String threadName = null;

   /**
    *  Default constructor.
    */
   public SyslogMessage()
   {
     super();
   }

   /**
    *  A utility constructor.
    */
   public SyslogMessage(InetAddress host, long time, String channel,
        Object logger, String loggerClassname,
        Object msg, Object detail, int level,
        Thread thread, String threadName, String methodName,
        int lineNumber)
   {
     this();

     this.host = host;
     this.time = time;
     this.channel = channel;
     this.logger = logger;
     this.loggerClassname = loggerClassname;
     this.msg = msg;
     this.detail = detail;
     this.level = level;
     this.thread = thread;
     this.threadName = threadName;
     this.callingMethodName = methodName;
     this.callingMethodLineNumber = lineNumber;
   }
}
