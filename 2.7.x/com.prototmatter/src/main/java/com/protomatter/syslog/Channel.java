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

import java.util.Map;
import java.util.HashMap;
import java.text.MessageFormat;
import com.protomatter.util.*;

/**
 *  A utility class for writing log messages to channels.
 *  Example usage of this class is as follows:<P>
 *
 *  <UL><TABLE BORDER=1 CELLPADDING=4 CELLSPACING=0 WIDTH="90%">
 *  <TR><TD><PRE>
 *
 *  // get the channel object
 *  Channel myChannel = Channel.getChannel("MY CHANNEL");
 *
 *  myChannel.info(this, "Hello there");
 *
 *  // get the default chanel object
 *  Channel default = Channel.getDefaultChannel();
 *
 *  default.info(this, "Hello there");
 *
 *
 *  // You can also write to multiple channels
 *  String channels[] = new String[] { "first-channel", "second-channel" };
 *  Channel lotsaChannels = Channel.getChannel(channels);
 *
 *  lotsChannels.info(this, "Hello there");
 *
 *
 *  // And finally, you can delegate the list of channels to
 *  // an object that implements the SyslogChannelAware interface
 *  SyslogChannelAware channelAware = new MyChannelAwareObject();
 *  Channel delegatedChannel = Channel.getChannel(channelAware);
 *
 *  delegatedChannel.info(this, "Hello there");
 *
 *  </PRE></TD></TR></TABLE></UL><P>
 *
 *  Basically, rather than using the <TT>infoToChannel(...)</TT>
 *  method and others on <TT>Syslog</TT>, you can simply get a
 *  handle to a channel and call methods on it.  Some people
 *  prefer this interface to the regular <tt>Syslog</tt> interface.
 */
public final class Channel
{
    /**
     *  A static utility field, which is the "all" channel.
     */
    public static final Channel ALL = Channel.getAllChannel();

    /**
     *  A static utility field, which is the "default" channel.
     */
    public static final Channel DEFAULT = Channel.getDefaultChannel();

    // one or the other of these two is null
    private String channels[] = null;
    private SyslogChannelAware channelAware = null;

    private Channel(String channels[])
    {
        this.channels = channels;
    }

    private Channel(SyslogChannelAware channelAware)
    {
        this.channelAware = channelAware;
    }

    private final Object getChannelForLogging()
    {
        return (channels != null) ? channels : channelAware.getSyslogChannel();
    }

    /**
     *  Get the list of channels that this <tt>Channel</tt> object writes to.
     *  This method will either return the channel name(s) passed
     *  into the <tt>getChannel()</tt> method that created this
     *  object, or it will ask the <tt>SyslogChannelAware</tt> object
     *  that this <tt>Channel</tt> is associated with what channels
     *  it thinks we should write to.
     */
    public String[] getChannelNames()
    {
        return Syslog.convertChannelObject(getChannelForLogging());
    }

    /**
     *  Get a Channel object for the given channel name.
     */
    public static Channel getChannel(String channelName)
    {
        return getChannel(new String[] { channelName });
    }

    /**
     *  Get a Channel object with the name of the given class.
     */
    public static Channel getChannel(Class channelClass)
    {
        return getChannel(new String[] { channelClass.getName() });
    }

    /**
     *  Get a Channel whose name is the package
     *  that the given class is in.  For instace,
     *  if you pass in <tt>com.yourcompany.foo.SomeClass</tt>
     *  the channel name will be <tt>com.yourcompany.foo</tt>.
     */
    public static Channel forPackage(Class someClass)
    {
        String packageName = someClass.getName();
        int pos = packageName.lastIndexOf('.');
        return getChannel(new String[] { packageName.substring(0, pos) });
    }

    /**
     *  Get a Channel object for the given set of channels.  Each
     *  logging call will send messages to each of these channels.
     */
    public static Channel getChannel(String channels[])
    {
        return new Channel(channels);
    }

    /**
     *  Get a Channel object which will delegate to the given
     *  channel aware object.  Each logging call will call
     *  the <tt>getSyslogChannel()</tt> method on the given
     *  <tt>SyslogChannelAware</tt> object to get the channel
     *  name (or list of channel names) to send messages to.
     *
     *  @see SyslogChannelAware
     */
    public static Channel getChannel(SyslogChannelAware channelAware)
    {
        return new Channel(channelAware);
    }

    /**
     *  Get a Channel object for the "all" channel.
     */
    public static Channel getAllChannel()
    {
        return getChannel(Syslog.ALL_CHANNEL);
    }

    /**
     *  Get a Channel object for the default channel.
     */
    public static Channel getDefaultChannel()
    {
        return getChannel(Syslog.DEFAULT_CHANNEL);
    }

    /**
     *  Log a debug message.
     */
    public void debug(Object caller, Object message)
    {
        Syslog.log(Syslog.getLocalHostName(), caller, getChannelForLogging(), message, null, Syslog.DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *  Log a debug message.
     */
    public void debug(Object caller, Object message, Object detail)
    {
        Syslog.log(Syslog.getLocalHostName(), caller, getChannelForLogging(), message, detail, Syslog.DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *  Log an info message.
     */
    public void info(Object caller, Object message)
    {
        Syslog.log(Syslog.getLocalHostName(), caller, getChannelForLogging(), message, null, Syslog.INFO,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *  Log an info message.
     */
    public void info(Object caller, Object message, Object detail)
    {
        Syslog.log(Syslog.getLocalHostName(), caller, getChannelForLogging(), message, detail, Syslog.INFO,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *  Log a warning message.
     */
    public void warning(Object caller, Object message)
    {
        Syslog.log(Syslog.getLocalHostName(), caller, getChannelForLogging(), message, null, Syslog.WARNING,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *  Log a warning message.
     */
    public void warning(Object caller, Object message, Object detail)
    {
        Syslog.log(Syslog.getLocalHostName(), caller, getChannelForLogging(), message, detail, Syslog.WARNING,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *  Log an error message.
     */
    public void error(Object caller, Object message)
    {
        Syslog.log(Syslog.getLocalHostName(), caller, getChannelForLogging(), message, null, Syslog.ERROR,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *  Log an error message.
     */
    public void error(Object caller, Object message, Object detail)
    {
        Syslog.log(Syslog.getLocalHostName(), caller, getChannelForLogging(), message, detail, Syslog.ERROR,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *  Log a fatal message.
     */
    public void fatal(Object caller, Object message)
    {
        Syslog.log(Syslog.getLocalHostName(), caller, getChannelForLogging(), message, null, Syslog.FATAL,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *  Log a fatal message.
     */
    public void fatal(Object caller, Object message, Object detail)
    {
        Syslog.log(Syslog.getLocalHostName(), caller, getChannelForLogging(), message, detail, Syslog.FATAL,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *  Log a message about the given exception.  The
     *  <TT>toString()</TT> method on the exception is used as
     *  the log message.  A stack trace from the exception is used
     *  as the message detail, and the message is logged at
     *  the <TT>ERROR</TT> level.
     */
    public void log(Object caller, Throwable exception)
    {
        Syslog.log(Syslog.getLocalHostName(), caller, getChannelForLogging(), exception.toString(), exception, Syslog.ERROR,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *  Log a message.
     */
    public void log(Object caller, Object message, Object detail, int level)
    {
        Syslog.log(Syslog.getLocalHostName(), caller, getChannelForLogging(), message, detail, level,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *  Log a breadcrumb at the debug level.  The breadcrumb
     *  includes information from a <tt><a href="../util/StackTraceInfo.html">StackTraceInfo</a></tt>
     *  object.
     */
    public void crumb(Object caller)
    {
        Syslog.log(Syslog.getLocalHostName(), caller, getChannelForLogging(), 
            MessageFormat.format(Syslog.getResourceString(MessageConstants.CRUMB_MESSAGE),
            new Object[] { Syslog.whereAmI(1) }),
            null, Syslog.DEBUG, Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *  Log a breadcrumb at the debug level.  The breadcrumb
     *  includes information from a <tt><a href="../util/StackTraceInfo.html">StackTraceInfo</a></tt>
     *  object.
     */
    public void crumb()
    {
        StackTraceInfo trace = StackTraceUtil.whereAmI(1);
        Syslog.log(Syslog.getLocalHostName(), trace.className, getChannelForLogging(), 
            MessageFormat.format(Syslog.getResourceString(MessageConstants.CRUMB_MESSAGE),
            new Object[] { trace }),
            null, Syslog.DEBUG, Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

}
