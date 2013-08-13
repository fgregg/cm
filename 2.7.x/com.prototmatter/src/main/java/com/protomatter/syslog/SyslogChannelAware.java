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
 *  An interface for objects that are aware of channels in
 *  syslog.  <P>
 *
 *  Why is this a good thing?  The idea here is that in
 *  some baseclass for things that do logging, you implement
 *  this interface and then everytime you pass <tt>this</tt>
 *  into a syslog method, you magically get messages sent to
 *  the "right" channels for the circumstances.  For instance,
 *  you might have multiple projects being hosted on the same
 *  VM, and you don't want to have to hardcode in channel
 *  names everywhere.  You can just have the baseclass read
 *  a configuration file or something, and everyone is happy.
 *  Later, when you decide to have things go to other channels,
 *  or want certain object to write to your own channels, you just
 *  override the implementation of <tt>getSyslogChannel()</tt>
 *  and everyone is still happy.
 */
public interface SyslogChannelAware
{
  /**
   *  This method should return the channel that messages
   *  coming from this object should be logged to if the
   *  call to Syslog didn't include a channel (or the channel
   *  specified was <tt>null</tt>).  If an
   *  object implementing this interface is passed into
   *  a Syslog method as the logger (the first argument to
   *  any of the log methods) and a channel was not specified
   *  as part of the call to that log method, this method
   *  will be called to determine what channel(s) the message
   *  should be sent to.  This method <b>must</b> return
   *  either an instance of <tt>java.lang.String</tt> or
   *  an array of <tt>java.lang.String</tt> if the message
   *  should be sent to multiple channels.  If there is an
   *  error calling this method or something goes wrong,
   *  the channel will be set to <tt>Syslog.DEFAULT_CHANNEL</tt>.
   */
  public Object getSyslogChannel();
}
