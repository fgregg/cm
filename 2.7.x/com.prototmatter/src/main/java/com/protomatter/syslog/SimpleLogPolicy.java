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

import java.io.PrintWriter;
import java.util.*;
import java.text.*;
import java.io.*;
import java.net.*;

import com.protomatter.util.*;

/**
 *  The default LogPolicy that knows about log levels and channels.
 *
 *  @see LogPolicy
 *  @see com.protomatter.syslog.xml.SimpleLogPolicy_Helper XML configuration class
 */
public class SimpleLogPolicy
implements LogPolicy
{
  private String name = null;
  private int logMask = Syslog.INHERIT_MASK;
  private HashMap channels = null;
  private boolean allChannels = false;
  private boolean initialized = false;
  private Object VALUE = new Object();

  /**
   *  All channels are listened to by default, and the
   *  default log mask is inherited from Syslog itself.
   */
  public SimpleLogPolicy()
  {
    super();
    Set channels = new HashSet();
    channels.add(Syslog.ALL_CHANNEL);
    setChannels(channels);
  }

  /**
   *  Get the name of this policy.
   */
  public String getName()
  {
    return this.name;
  }

  /**
   *  Set the name of this policy.
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   *  Set the list of channels to use.  Channel names
   *  are values in the vector.
   */
  public synchronized void setChannels(List channels)
  {
    Set set = new HashSet();
    Iterator i = channels.iterator();
    while (i.hasNext())
    {
      set.add(i.next());
    }
    setChannels(set);
  }

  /**
   *  Set the list of channels to use.  Channel names are
   *  looked for as keys in the hashtable.
   */
  public synchronized void setChannels(Set channelSet)
  {
    this.channels = new HashMap();
    Iterator i = channelSet.iterator();
    while (i.hasNext())
    {
      channels.put(i.next(), VALUE);
    }
    this.allChannels = channelSet.contains(Syslog.ALL_CHANNEL);
  }

  /**
   *  Add the given channel to the list of channels we
   *  are listening to.
   */
  public synchronized void addChannel(String channel)
  {
    channels.put(channel, VALUE);
    this.allChannels = channels.containsKey(Syslog.ALL_CHANNEL);
  }

  /**
   *  Remove the given channel from the list of channels
   *  we are listening to.
   */
  public synchronized void removeChannel(String channel)
  {
    channels.remove(channel);
    this.allChannels = channels.containsKey(Syslog.ALL_CHANNEL);
  }

  /**
   *  Remove all channels from the list of channels
   *  we are listening to.
   */
  public synchronized void removeAllChannels()
  {
    channels = new HashMap();
    this.allChannels = false;
  }

  /**
   *  Get the list of channels this policy listens to.
   */
  public Iterator getChannels()
  {
    return channels.keySet().iterator();
  }

  /**
   *  Determine if a log message should be logged given the information.
   *  Only checks to see if the log level is in the mask.
   */
  public boolean shouldLog(SyslogMessage message)
  {
    boolean inMask = false;
    if (this.logMask == Syslog.INHERIT_MASK)
      inMask = ((message.level & Syslog.currentLogMask) > 0);
    else
      inMask = ((message.level & this.logMask) > 0);

    // not in our mask... not gonna log it.
    if (!inMask)
      return false;

    // we check channels.
    if (!allChannels)
    {
      // it's on the ALL_CHANNEL -- we pay attention to these
      // no matter what.
      if (Syslog.ALL_CHANNEL.equals(message.channel))
        return true;

      // is the channel in our list?
      return channels.containsKey(message.channel);
    }

    // we're listening to the ALL_CHANNEL channel,
    // meaning we listen to every channel.
    return true;
  }

  /**
   *  Check if the given level is covered by the given mask.
   */
  protected final boolean inMask(int level, int mask)
  {
    return ((level & mask) > 0);
  }

  /**
   *  Set the log mask.
   *
   *  @see Syslog#parseLogMask
   */
  public final void setLogMask(String mask)
  {
    setLogMask(Syslog.parseLogMask(mask));
  }

  /**
    * Set the mask for logging of messages.
    * For example, to log all messages of type ERROR or greater,
    * you would call:
    *
    *   setLogMask(Syslog.atOrAbove(Syslog.ERROR));
    */
  public final void setLogMask(int mask)
  {
    this.logMask = mask;
  }

  /**
   *  Get the mask for logging of messages.
   */
  public final int getLogMask()
  {
    return this.logMask;
  }
}
