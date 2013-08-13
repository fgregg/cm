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
import java.util.*;
import java.text.*;

/**
 *  A Writer that is attached to Syslog.  When data is
 *  flushed, any information built up is sent off to Syslog.
 *  Generally, this object will be wrapped with a <tt>PrintWriter</tt>
 *  object for easier use, like this:<P>
 *
 *  <BLOCKQUOTE><TT>
 *    SyslogWriter sw = new SyslogWriter(someOtherObject, Syslog.INFO);<BR>
 *    PrintWriter pw = new PrintWriter(sw, true);<BR>
 *    ...<BR>
 *    pw.println("Blah blah blah.. this will go to Syslog");
 *  </TT></BLOCKQUOTE>
 *
 *  The <tt>pw.println(...)</tt> call is equivalent to calling:<P>
 *
 *  <BLOCKQUOTE><TT>
 *    Syslog.info(someOtherObject, "Blah blah blah.. this will go to Syslog");
 *  </TT></BLOCKQUOTE>
 *
 *  @see Syslog
 */
public class SyslogWriter
extends Writer
{
  private Object logger = null;
  private Object channel = null;
  private int level = Syslog.INFO;

  private int maxLength = 60;

  private boolean closed = false;

  private StringBuffer data = null;
  private String lineSep = null;
  private int lineSepLength = 0;

  private final void checkClosed()
  {
    if (closed)
      throw new IllegalStateException(MessageFormat.format(
        Syslog.getResourceString(MessageConstants.SYSLOGWRITER_IS_CLOSED_MESSAGE),
        new Object[] { "SyslogWriter" }));
  }

  /**
   *  Create a new SyslogWriter.  Messages will be written to
   *  Syslog as if they came from the given logger, and will be
   *  logged at the given level on the default channel.
   *
   *  @param logger The object the log messages should appear to come from.
   *  @param level The message severity.
   */
  public SyslogWriter(Object logger, int level)
  {
    this(logger, null, level);
  }

  /**
   *  Create a new SyslogWriter.  Messages will be written to
   *  Syslog as if they came from the given logger, and will be
   *  logged at the given level on given channel (or set of
   *  channels).
   *
   *  @param logger The object the log messages should appear to come from.
   *  @param channel The channel to write messages to (a <tt>String</tt> or <tt>String[]</tt>).
   *  @param level The message severity.
   */
  public SyslogWriter(Object logger, Object channel, int level)
  {
    this.logger = logger;
    this.channel = channel;
    this.level = level;
    this.lineSep = System.getProperty("line.separator");
    this.lineSepLength = this.lineSep.length();
  }

  /**
   *  Close the writer.
   */
  public void close()
  {
    this.closed = true;
  }

  /**
   *  Flush unwritten data to Syslog.  If the data is longer
   *  than 60 characters, then the first 60 characters (up
   *  to the first line separator, with "..." appended) are
   *  written as the "message" in Syslog, and the entire piece
   *  of data is written as the "detail".
   */
  public void flush()
  {
    checkClosed();
    synchronized (lock)
    {
      if (data != null)
      {
        if (data.length() > maxLength)
        {
          StringBuffer msg = new StringBuffer(maxLength + 3);
          String s = data.toString();

          // this is done so that if the data is long enough
          // to need to be broken up, but there's a line ending
          // before maxLength, the output looks "correct"
          int index = s.indexOf(lineSep);
          if ((index != -1) && (index < maxLength))
            msg.append(s.substring(0, index));
          else
            msg.append(s.substring(0, maxLength));
          msg.append("...");

          if (channel == null)
            Syslog.log(logger, msg, s, level);
          else
            Syslog.log(logger, channel, msg, s, level);
        }
        else
        {
          String msg = data.toString();
          if (msg.endsWith(lineSep))
            msg = msg.substring(0, msg.length() - lineSepLength);
          else if (channel == null)
            Syslog.log(logger, msg, null, level);
          else
            Syslog.log(logger, channel, msg, null, level);
        }
        this.data = null;
      }
    }
  }

  /**
   *  Write the given data to the writer.
   */
  public void write(char buf[], int offset, int length)
  {
    checkClosed();
    synchronized (lock)
    {
      if (data == null)
        data = new StringBuffer();
      data.append(buf, offset, length);
    }
  }
}
