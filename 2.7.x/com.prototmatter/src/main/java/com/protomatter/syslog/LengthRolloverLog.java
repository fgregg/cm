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
import java.util.zip.*;
import java.text.*;

import com.protomatter.util.*;

/**
  * An implementation of an object that will log things using the Syslog
  * facility, and roll it's log files after a certain number of bytes
  * have been written to them.
  *
  *  @see com.protomatter.syslog.xml.LengthRolloverLog_Helper XML configuration class
  */
public class LengthRolloverLog
extends BasicLogger
{
  private Writer out = null;
  private Object syncObject = new Object();
  private int rolllength = 1048576; // default roll to 1Mbyte
  private int written;
  private String basename;
  private String extension;
  private boolean append = true;
  private boolean autoFlush = true;
  private File currentFile = null;
  private File previousFile = null;

  /**
   *  Write log information to the given log, roll when specified.
   *  The file written to will actually be "basename.number" where
   *  number is from 1..N (1 being older log entries than N)  When
   *  starting, if logfiles basename.1 through basename.N exist,
   *  it will begin writing to basename.N+1.  By default, all channels
   *  are listened to.
   */
  public LengthRolloverLog(String basename, String extension, int roll, boolean append, boolean autoFlush)
  {
    this();
    this.basename = basename;
    this.extension = extension;
    this.rolllength = roll;
    this.append = append;
    this.autoFlush = autoFlush;
    rollover();
  }

  /**
   *  You will need to call the configure() method to configure this
   *  logger if you use this constructor.
   */
  public LengthRolloverLog()
  {
    super();
  }

  /**
   *  Set the max file size (in bytes).  Default is 1MB.
   */
  public void setRollLength(int rolllength)
  {
    this.rolllength = rolllength;
  }
  /**
   *  Get the max file size (in bytes).
   */
  public int getRollLength()
  {
    return this.rolllength;
  }

  /**
   *  Set the base file name.
   */
  public void setBasename(String basename)
  {
    this.basename = basename;
  }
  /**
   *  Get the base file name.
   */
  public String getBasename()
  {
    return this.basename;
  }

  /**
   *  Set the file extension.
   */
  public void setExtension(String extension)
  {
    this.extension = extension;
  }
  /**
   *  Get the file extension.
   */
  public String getExtension()
  {
    return this.extension;
  }

  /**
   *  Set the append-to-file flag.  Default is true.
   */
  public void setAppend(boolean append)
  {
    this.append = append;
  }
  /**
   *  Get the append-to-file flag.
   */
  public boolean getAppend()
  {
    return this.append;
  }

  /**
   *  Set the auto-flush flag.  Default is false.
   */
  public void setAutoFlush(boolean autoFlush)
  {
    this.autoFlush = autoFlush;
  }
  /**
   *  Get the auto-flush flag.
   */
  public boolean getAutoFlush()
  {
    return this.autoFlush;
  }

  /**
   *  Get the file currently being written to.
   */
  public File getCurrentFile()
  {
    return this.currentFile;
  }

  /**
   *  Get the file that was being written to before we rolled.
   */
  public File getPreviousFile()
  {
    return this.previousFile;
  }

  /**
   *  Roll the logs now.
   */
  public void rollover()
  {
    synchronized(syncObject)
    {
      if (out != null)
      {
        try
        {
          out.write(formatter.getLogFooter());
        }
        catch (IOException x)
        {
          x.printStackTrace();
        }
      }
      resetDateFormat();
      Writer old = out;
      try
      {
        int i;
        File f = null;
        for (i=1; (new File(basename + i + extension)).exists(); i++);
        f = new File(basename + i + extension);
        out = new BufferedWriter(
          new FileWriter(f.getCanonicalPath(), append));
        out.write(formatter.getLogHeader());
        written = 0;

        if (old != null)
        {
          old.flush();
          old.close();
        }
        this.previousFile = this.currentFile;
        this.currentFile = f;
      }
      catch (IOException x)
      {
        System.err.println(MessageFormat.format(
          Syslog.getResourceString(MessageConstants.LENGTHRO_CANNOT_ROLL_MESSAGE),
          new Object[] { "IOException" } ));
        x.printStackTrace();
        out = old;
      }
    }
  }

  public final void log(SyslogMessage message)
  {
    StringBuffer b = new StringBuffer(128);
    formatLogEntry(b, message);
    int length = b.length();
    synchronized(syncObject)
    {
      if ((length + written) > rolllength)
        rollover();
      try
      {
        out.write(b.toString());
        if (autoFlush)
          out.flush();
        written += length;
      }
      catch (IOException x)
      {
        x.printStackTrace();
      }
    }
  }

  public void flush()
  {
    try
    {
      if (out != null)
        out.flush();
    }
    catch (IOException x)
    {
      x.printStackTrace();
    }
  }

  public void shutdown()
  {
    if (out != null)
    {
      try
      {
        out.write(formatter.getLogFooter());
        out.flush();
        out.close();
        out = null;
      }
      catch (IOException x)
      {
        x.printStackTrace();
      }
    }
  }
}
