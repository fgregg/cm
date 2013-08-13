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
 *  A logger that simply writes to a file.
 *
 *  @see com.protomatter.syslog.xml.FileLog_Helper XML configuration class
 */
public class FileLog
extends BasicLogger
{
   private boolean append = true;

   private File file;
   private Writer out = null;
   private boolean autoFlush = true;

   /**
    *  Create a new file log attached to the given file.
    */
   public FileLog(File f)
   {
     this(f, true, false);
   }

   /**
    *  Create a new file log attached to the given file.
    */
   public FileLog(File f, boolean append, boolean autoFlush)
   {
     this();
     setFile(f);
     setAppend(append);
     setAutoFlush(autoFlush);
   }

   /**
    *  You will need to call the configure() method if you use this constructor.
    */
   public FileLog()
   {
     super();
   }

   /**
    *  Set the file we're writing to.
    */
   public synchronized void setFile(File f)
   {
     cleanup();
     file = f;
   }

   /**
    *  Get the file we're writing to.
    */
   public File getFile()
   {
     return this.file;
   }

   /**
    *  Set the file we're writing to.
    */
   public void setAppend(boolean append)
   {
     this.append = append;
   }

   /**
    *  Get the file we're writing to.
    */
   public boolean getAppend()
   {
     return this.append;
   }

   /**
    *  Should we auto-flush the buffer all the time?
    */
   public void setAutoFlush(boolean flush)
   {
     this.autoFlush = flush;
   }

   /**
    *  Determine if we should we auto-flush the buffer all the time.
    */
   public boolean getAutoFlush()
   {
     return this.autoFlush;
   }

   /**
    *  Log a message.
    */
   public final void log(SyslogMessage message)
   {
      StringBuffer b = null;
      if (message.detail == null)
        b = new StringBuffer(128);
      else
        b = new StringBuffer(256);
      formatLogEntry(b, message);

      try
      {
         if (out == null)
         {
           synchronized(file)
           {
             if (out == null)
             {
               out = new BufferedWriter(
                 new FileWriter(file.getCanonicalPath(), append));
               out.write(formatter.getLogHeader());
             }
           }
         }
         out.write(b.toString());
         if (autoFlush)
           out.flush();
      }
      catch (IOException x)
      {
        System.err.println(MessageFormat.format(
          Syslog.getResourceString(MessageConstants.FILELOG_CANNOT_WRITE_MESSAGE),
          new Object[] { x.toString() }));
        x.printStackTrace();
      }
   }

   private void cleanup()
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

   /**
    *  Closes down the file and prepares for shutdown.
    */
   public synchronized void shutdown()
   {
     cleanup();
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
}
