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
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.text.*;

import com.protomatter.util.*;

/**
 *  An implementation of an object that will log things
 *  using the Syslog facility.
 *
 *  @see com.protomatter.syslog.xml.PrintWriterLog_Helper XML configuration class
 */
public class PrintWriterLog
extends BasicLogger
{
   private PrintWriter out = null;
   private boolean writerChanged = true;
   private String streamName = null;
   private boolean canDumpConfig = false;

   /**
    *  Construct a new PrintWriterLog attached to the given
    *  PrintWriter.  By default, it will respond to log entries
    *  coming along on all channels.
    */
   public PrintWriterLog(PrintWriter writer)
   {
      this();
      out = writer;
   }

   /**
    *  Construct a new PrintWriterLog attached to the given
    *  stream.  The stream name must be either
    *  "<tt>System.out</tt>" or "<tt>System.err</tt>".
    */
   public PrintWriterLog(String streamName)
   {
      this();
      if ("System.err".equals(streamName))
      {
        out = new PrintWriter(System.err);
      }
      else if ("System.out".equals(streamName))
      {
        out = new PrintWriter(System.out);
      }
      else
      {
         throw new IllegalArgumentException(MessageFormat.format(
           Syslog.getResourceString(MessageConstants.PRINTWRITER_BAD_STREAM_NAME_MESSAGE),
           new Object[] { "stream", "System.out", "System.err" }));
      }
      this.streamName = streamName;
      this.canDumpConfig = true;
   }

   /**
    *  Get the name of the stream.
    */
   public String getStreamName()
   {
     return this.streamName;
   }

   /**
    *  Set the name of the stream.
    */
   public void setStreamName(String streamName)
   {
     this.streamName = streamName;
   }

   /**
    *  Construct a new PrintWriterLog -- you must call
    *  configure() after using this constructor.
    */
   public PrintWriterLog()
   {
      super();
   }

   /**
    *  Set the writer that we're writing to.
    */
   public void setWriter(PrintWriter writer)
   {
      cleanup();
      out = writer;
      writerChanged = true;
      this.canDumpConfig = false;
   }

   /**
    *  Write a log message.
    */
   public void log(SyslogMessage message)
   {
      StringBuffer b = new StringBuffer();
      formatLogEntry(b, message);
      if (out != null)
      {
         if (writerChanged)
         {
           out.print(formatter.getLogHeader());
           writerChanged = false;
         }
         out.print(b);
         out.flush();
      }
   }

   private void cleanup()
   {
     if (out != null)
     {
       out.print(formatter.getLogFooter());
       out.flush();
       out.close();
       out = null;
     }
   }

   /**
    *  Clean up and prepare for shutdown.
    */
   public synchronized void shutdown()
   {
     cleanup();
   }

   public void flush()
   {
     if (out != null)
       out.flush();
   }
}
