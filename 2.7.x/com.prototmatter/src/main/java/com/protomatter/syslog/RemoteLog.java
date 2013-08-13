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
import java.rmi.*;
import java.util.*;
import java.text.*;

import javax.rmi.*;
import javax.naming.*;

import com.protomatter.util.*;

/**
 *  A logger that sends messages to remote receivers
 *  bound in JNDI.
 *
 *  Object bound directly under the
 *  "<tt>com.protomatter.syslog.remote</tt>" location
 *  in JNDI will receive the log message if they
 *  implement the {@link RemoteLogReceiver RemoteLogReceiver}
 *  interface.<P>
 *
 *  When calling methods on the remote objects, they
 *  are first run through <tt>PortableRemoteObject.narrow()</tt>
 *  to ensure everything is OK.  This should facilitate
 *  the use of RMI over IIOP and other transports, and the
 *  use of non-Java log receivers via CORBA or another
 *  cross-language transport mechanisms.<P>
 *
 *  The {@link SyslogServer SyslogServer} class can be used as
 *  a standalone remote message receiver.  Please see the JavaDoc
 *  for that class for more information.
 *
 *  @see com.protomatter.syslog.xml.RemoteLog_Helper XML configuration class
 */
public class RemoteLog
extends BasicLogger
{
  private Context context = null;
  private Context subContext = null;
  private Context subSubContext = null;

  /**
   *  You will need to call the configure() method if
   *  you use this constructor.
   */
  public RemoteLog()
  {
    super();

    try
    {
      context = new InitialContext();
      subContext = getSubContext(context, "com.protomatter.syslog");
      subSubContext = getSubContext(context, "com.protomatter.syslog.remote");
    }
    catch (NamingException x)
    {
      throw new ChainedRuntimeException(Syslog.getResourceString(MessageConstants.REMOTELOG_JNDI_INIT_EXCEPTION_MESSAGE), x);
    }
  }

  /**
   *  getSubContext() -- creates intermediate contexts as needed.
   */
  private static Context getSubContext(Context ctx, String name)
  throws NamingException
  {
    try
    {
      return (Context)ctx.lookup(name);
    }
    catch (NamingException x)
    {
      Context context = ctx;

      // create the necessary subcontexts
      Name resolvedName = x.getResolvedName();
      Name remainingName = x.getRemainingName();

      if (!remainingName.isEmpty())
      {
        // create subcontext
        String nextName = remainingName.get(0);
        resolvedName.add(nextName);
        Context subCtx = context.createSubcontext(resolvedName);
        // try lookup again
        return getSubContext(context, name);
      }
      else
      {
        // can't help here...
        throw x;
      }
    }
  }

  /**
   *  Log the given message to all bound listeners.  If a
   *  <tt>RemoteException</tt> is thrown while calling the
   *  logging callback method on the receiver, then
   *  the receiver is unbound from JNDI to prevent further
   *  problems with that receiver.
   */
  public final void log(SyslogMessage sm)
  {
    if (context == null)
      return;

    String ip = sm.host.getHostAddress();
    String loggerClass = sm.loggerClassname;
    String channel = sm.channel;
    String message = (sm.msg == null) ? "" : sm.msg.toString();
    String detail = null;
    if (detail == null)
    {
      detail = "";
    }
    else
    {
      StringBuffer b = new StringBuffer(256);
      formatter.formatMessageDetail(b, sm);
      detail = b.toString();
    }
    int level = sm.level;
    String threadName = (sm.thread != null) ? sm.thread.getName() : "";
    long time = sm.time;

    try
    {
      NamingEnumeration e = subContext.list("remote");
      while (e.hasMore())
      {
        NameClassPair pair = (NameClassPair)e.next();
        String name = pair.getName();
        Object thing = subSubContext.lookup(name);
        if (thing instanceof RemoteLogReceiver)
        {
          try
          {
            RemoteLogReceiver receiver = (RemoteLogReceiver)thing;
            receiver = (RemoteLogReceiver)PortableRemoteObject.narrow(receiver, RemoteLogReceiver.class);
            receiver.log(ip, loggerClass, channel, message, detail, level, threadName, time);
          }
          catch (RemoteException rx)
          {
            System.err.println(MessageFormat.format(
              Syslog.getResourceString(MessageConstants.REMOTELOG_CANNOT_WRITE_REMOTE_MESSAGE),
              new Object[] { "RemoteException", name }));
            rx.printStackTrace();
            System.err.println(Syslog.getResourceString(MessageConstants.REMOTELOG_REMOVE_RECEIVER_MESSAGE));
            try
            {
              subSubContext.unbind(name);
            }
            catch (NamingException nx) { ; }
          }
        }
      }
    }
    catch (Exception x)
    {
      // TODO: don't know what to do!
      x.printStackTrace();
    }
  }

  /**
   *  Prepare for shutdown.
   */
  public void shutdown()
  {
    this.context = null;
  }

  public void flush()
  {
    // do nothing.
  }
}
