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

import com.protomatter.util.*;

/**
 *  A logger that opens the file for each log entry and closes it
 *  after it's done writing.
 *
 *  @see com.protomatter.syslog.xml.OpenFileLog_Helper XML configuration class
 */
public class OpenFileLog
extends BasicLogger
{
   private boolean fileChanged = true;

   private File file;

   /**
    *  Create an OpenFileLog attached to the given file.
    */
   public OpenFileLog(File f)
   {
      this();
      this.file = f;
   }

   /**
    *  You will need to call the configure() method if you use this constructor.
    */
   public OpenFileLog()
   {
     super();
   }

   /**
    *  Set the file we're writing to.
    */
   public void setFile(File f)
   {
      cleanup();
      file = f;
      fileChanged = true;
   }

   /**
    *  Get the file we're writing to.
    */
   public File getFile()
   {
     return this.file;
   }

   /**
    *  Write a log message.
    */
   public final void log(SyslogMessage message)
   {
      StringBuffer b = new StringBuffer(256);
      formatLogEntry(b, message);

      try
      {
         PrintWriter out = new PrintWriter(
           new FileWriter(file.getCanonicalPath(), true));
         if (fileChanged)
         {
           out.print(formatter.getLogHeader());
           fileChanged = false;
         }
         out.print(b);
         out.flush();
         out.close();
      }
      catch (IOException x)
      {
        System.err.println(MessageFormat.format(
          Syslog.getResourceString(MessageConstants.OPENFLOG_CANNOT_OPEN_MESSAGE),
          new Object[] { x.toString() }));
      }
   }

   private void cleanup()
   {
     try
     {
       if (file != null)
       {
         PrintWriter out = new PrintWriter(
           new FileWriter(file.getCanonicalPath(), true));
         out.print(formatter.getLogFooter());
         out.flush();
         out.close();
       }
     }
     catch (IOException x)
     {
       System.err.println(MessageFormat.format(
         Syslog.getResourceString(MessageConstants.OPENFLOG_CANNOT_OPEN_MESSAGE),
         new Object[] { x.toString() }));
     }
   }

   /**
    *  Cleanup our file and prepare for shutdown.
    */
   public void shutdown()
   {
     cleanup();
   }

   public void flush()
   {
     // do nothing.
   }
}
