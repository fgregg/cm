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
import java.text.*;
import java.util.*;

import org.jdom.*;
import org.jdom.input.*;
import com.protomatter.syslog.xml.*;

/**
 *  A performance testing rig for Syslog.  Run this program with no
 *  arguments for usage information and examples.
 */
public class PerformanceTest
{
  static Syslogger logger = null;
  static LogPolicy policy = null;
  static SyslogTextFormatter format = null;
  static SyslogMessage message = new SyslogMessage();

  static int POLICY_TEST = 0;
  static int FORMAT_TEST = 1;
  static int DIRECT_TEST = 2;
  static int SYSLOG_TEST = 3;

  static int runs[] = { 0, 0, 0, 0 };
  static boolean tests[] = { false, false, false, false };
  static String testNames[] =
    { "LogPolicy.shouldLog()", "Format.formatLogEntry()",
      "Direct Logger.log()", "Syslog.log()" };

  public static void main(String args[])
  {
    if (args.length != 2)
    {
      System.out.println("");
      System.out.println("Usage: PerformanceTest syslog-config.xml test-config.xml");
      System.out.println("");
      System.out.println("Example test-config.xml file:");
      System.out.println("-----------------------------------------------------------------");
      System.out.println("");
      System.out.println("<PerformanceTest>");
      System.out.println("");
      System.out.println("  <Message>");
      System.out.println("    <loggerClassname>com.protomatter.syslog.PerformanceTest</loggerClassname>");
      System.out.println("    <level>ERROR</level>");
      System.out.println("    <channel>DEFAULT_CHANNEL</channel>");
      System.out.println("    <message>This is the short message text, it's right here.</message>");
      System.out.println("    <detail></detail>");
      System.out.println("  </Message>");
      System.out.println("");
      System.out.println("  <PolicyTest>true</PolicyTest>");
      System.out.println("  <FormatTest>true</FormatTest>");
      System.out.println("  <DirectTest>true</DirectTest>");
      System.out.println("  <SyslogTest>true</SyslogTest>");
      System.out.println("");
      System.out.println("  <NumThreads>5</NumThreads>");
      System.out.println("");
      System.out.println("  <PolicyIterations>10000000</PolicyIterations>");
      System.out.println("  <FormatIterations>1000000</FormatIterations>");
      System.out.println("  <LogIterations>100000</LogIterations>");
      System.out.println("");
      System.out.println("</PerformanceTest>");
      System.out.println("");
      System.out.println("-----------------------------------------------------------------");
      System.out.println("");
      System.exit(0);
    }
    try
    {
      DecimalFormat msFormat = new DecimalFormat("###,###,###,###,##0");
      DecimalFormat timeFormat = new DecimalFormat("###,###,###,###,##0.0");
      DecimalFormat psFormat = new DecimalFormat("###,###,###,###,##0.0");
      DecimalFormat idFormat = new DecimalFormat("00");

      System.out.println("Syslog performance test utility");
      System.out.println("");
      System.out.println("JVM Information:");
      System.out.println("  VM Name:          " + System.getProperty("java.vm.name"));
      System.out.println("  VM Version:       " + System.getProperty("java.vm.version"));
      System.out.println("  Runtime name:     " + System.getProperty("java.runtime.name"));
      System.out.println("  Runtime version:  " + System.getProperty("java.runtime.version"));
      System.out.println("");
      System.out.println("OS Information:");
      System.out.println("  " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
      System.out.println("");
      System.out.println("Configuring Syslog from \"" + args[0] + "\"");
      long time = System.currentTimeMillis();
      SyslogXML.configure(new File(args[0]));
      time = System.currentTimeMillis() - time;
      System.out.println("Configuration completed in " + msFormat.format(time) + " ms");

      System.out.println("");
      System.out.println("Configuring test from \"" + args[1] + "\"");
      time = System.currentTimeMillis();
      Document d = (new SAXBuilder(false)).build(new File(args[1]));
      Element root = d.getRootElement();

      runs[POLICY_TEST] = Integer.parseInt(root.getChild("PolicyIterations").getText());
      runs[FORMAT_TEST] = Integer.parseInt(root.getChild("FormatIterations").getText());
      runs[DIRECT_TEST] = Integer.parseInt(root.getChild("LogIterations").getText());
      runs[SYSLOG_TEST] = runs[DIRECT_TEST];

      int numThreads = Integer.parseInt(root.getChild("NumThreads").getText());

      Element mElement = root.getChild("Message");
      String tmp = mElement.getChild("level").getText();
      if (tmp.equalsIgnoreCase("debug"))
        message.level = Syslog.DEBUG;
      else if (tmp.equalsIgnoreCase("info"))
        message.level = Syslog.INFO;
      else if (tmp.equalsIgnoreCase("warning"))
        message.level = Syslog.WARNING;
      else if (tmp.equalsIgnoreCase("error"))
        message.level = Syslog.ERROR;
      else if (tmp.equalsIgnoreCase("fatal"))
        message.level = Syslog.FATAL;

      message.logger = PerformanceTest.class;
      message.loggerClassname = mElement.getChild("loggerClassname").getText();
      message.channel = mElement.getChild("channel").getText();
      message.msg = mElement.getChild("message").getText();
      message.detail = mElement.getChild("detail").getText();
      if (message.detail != null && message.detail.toString().equals(""))
        message.detail = null;
      time = System.currentTimeMillis() - time;

      boolean theTests[] = { false, false, false, false };
      theTests[POLICY_TEST] = "true".equalsIgnoreCase(root.getChild("PolicyTest").getText());
      theTests[FORMAT_TEST] = "true".equalsIgnoreCase(root.getChild("FormatTest").getText());
      theTests[DIRECT_TEST] = "true".equalsIgnoreCase(root.getChild("DirectTest").getText());
      theTests[SYSLOG_TEST] = "true".equalsIgnoreCase(root.getChild("SyslogTest").getText());

      System.out.println("Configuration completed in " + msFormat.format(time) + " ms");

      System.out.println("");
      System.out.println("Policy/Format/Direct test message:");
      System.out.println("  logger:    " + message.loggerClassname);
      System.out.println("  channel:   \"" + message.channel + "\"");
      System.out.println("  level:     " + mElement.getChild("level").getText().toUpperCase());
      System.out.println("  message:   \"" + message.msg + "\"");
      System.out.println("  detail:    \"" + message.detail + "\"");
      System.out.println("");
      System.out.println("Number of test threads: " + numThreads);
      System.out.println("");

      logger = (Syslogger)Syslog.getLoggers().next();
      policy = logger.getPolicy();
      format = logger.getTextFormatter();

      TestThread threads[] = new TestThread[numThreads];

      for (int i=0; i<tests.length; i++)
      {
        if (theTests[i])
        {
          for(int j=tests.length; --j>=0;)
            tests[j] = false;
          tests[i] = true;

          System.out.println("Running " + testNames[i]
            + " test (" + msFormat.format(runs[i]) + " iterations per thread):");
          for (int j=0; j<threads.length; j++)
            threads[j] = new TestThread();

          time = System.currentTimeMillis();
          for(int j=threads.length; --j>=0;)
            threads[j].start();
          for(int j=threads.length; --j>=0;)
            threads[j].join();
          time = System.currentTimeMillis() - time;
          System.out.println("Test complete in " + timeFormat.format((double)time/(double)1000) + " s");

          System.out.println("");
          System.out.println("Timings per thread:");
          System.out.println("");
          System.out.println("  Note:  \"ms\" denotes milli-seconds (1/1,000 sec)");
          System.out.println("         \"us\" denotes micro-seconds (1/1,000,000 sec)");
          System.out.println("");

          for (int j=0; j<threads.length; j++)
          {
            long total = threads[j].total;
            double uAverage = ((double)total*1000)/((double)(runs[i]));
            double perSecond = ((double)1000000)/(uAverage);

            System.out.println("  " + idFormat.format(j) + ":");
            System.out.println("    Total time: " + msFormat.format(total) + " ms");
            System.out.println("    Each call:  " + timeFormat.format(uAverage) + " us");
            System.out.println("    Per second: " + psFormat.format(perSecond));
          }
          double uAverage = ((double)time*1000)/((double)(runs[i]*numThreads));
          double perSecond = ((double)1000000)/(uAverage);
          System.out.println("");
          System.out.println("  Aggregate timings:");
          System.out.println("    Total time:     " + msFormat.format(time) + " ms");
          System.out.println("    Average time:   " + timeFormat.format(uAverage) + " us");
          System.out.println("    Per second:     " + psFormat.format(perSecond));
          System.out.println("");
          System.out.println("");
        }
      }

      System.out.println("Syslog.shutdown()");
      Syslog.shutdown();
      System.out.println("  done.");
    }
    catch (Exception x)
    {
      x.printStackTrace();
    }
  }

  static class TestThread
  extends Thread
  {
    double uAverage = 0;
    long total = 0;

    public TestThread()
    {
      super();
      setDaemon(true);
    }

    public void run()
    {
      long time = 0;
      if (tests[POLICY_TEST])
      {
        boolean retval = false;
        time = System.currentTimeMillis();
        for (int i=runs[POLICY_TEST]; --i>=0;)
        {
          retval = policy.shouldLog(message);
        }
        total = System.currentTimeMillis() - time;
        uAverage = ((double)total*1000)/((double)(runs[POLICY_TEST]));
      }

      if (tests[FORMAT_TEST])
      {
        time = System.currentTimeMillis();
        for (int i=runs[FORMAT_TEST]; --i>=0;)
        {
          StringBuffer b = new StringBuffer(128);
          format.formatLogEntry(b, message);
        }
        total = System.currentTimeMillis() - time;
        uAverage = ((double)total*1000)/((double)(runs[FORMAT_TEST]));
      }

      if (tests[DIRECT_TEST])
      {
        time = System.currentTimeMillis();
        for (int i=runs[DIRECT_TEST]; --i>=0;)
        {
          logger.log(message);
        }
        total = System.currentTimeMillis() - time;
        uAverage = ((double)total*1000)/((double)(runs[DIRECT_TEST]));
      }

      if (tests[SYSLOG_TEST])
      {
        time = System.currentTimeMillis();
        for (int i=runs[SYSLOG_TEST]; --i>=0;)
        {
          Syslog.log(PerformanceTest.class, message.msg, message.detail, message.level);
        }
        total = System.currentTimeMillis() - time;
        uAverage = ((double)total*1000)/((double)(runs[SYSLOG_TEST]));
      }
    }
  }
}
