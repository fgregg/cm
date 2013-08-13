package com.protomatter.util;

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
import java.text.DecimalFormat;

/**
 *  A utility class for parsing stack traces.<P>
 *
 *  Determining the stack information at runtime
 *  is a relatively expensive operation.  I've tested this
 *  on a 650MHz PIII Coppermine Sony Vaio
 *  laptop running RedHat Linux 7.2, kernel 2.4.9,
 *  I saw these results with single-threaded tests:<P>
 *
 *  <table border=1 cellpadding=4 cellspacing=0>
 *
 *  <tr>
 *  <td>Classic VM (build JDK-1.2.2_012, green threads, nojit)</td>
 *  <td>Average 0.73543ms</td>
 *  </tr>
 *
 *  <tr>
 *  <td>Classic VM (build 1.3.1, J2RE 1.3.1 IBM build cxia32131-20020410 (JIT enabled: jitc))</td>
 *  <td>Average 0.31817ms</td>
 *  </tr>
 *
 *  <tr>
 *  <td>Java(TM) 2 Runtime Environment, Standard Edition (build 1.3.1_03-b03)</td>
 *  <td>Average 0.18201ms</td>
 *  </tr>
 *
 *  <tr>
 *  <td>Java HotSpot(TM) Client VM (build 1.4.0-b92, mixed mode)</td>
 *  <td>Average 0.1024ms</td>
 *  </tr>
 *
 *  <tr>
 *  <td>JRockit Virtual Machine (build 3.1.4-dax.appeal.se-20020319-1000)<BR>
 *      Native Threads, Generational Concurrent Garbage Collector</td>
 *  <td valign=top>Average 0.0486ms</td>
 *  </tr>
 *
 *  </table><P>
 *
 *  If possible, this class loads an implementation
 *  of itself that uses new APIs available in JDK 1.4
 *  to improve performance.  Under JDK 1.2 and 1.3,
 *  this class parses a stack trace to determine
 *  call stack information.<P>
 *
 *  Also, under the IBM and JRockit JVMs, line numbers
 *  are usually not available.  They may or may
 *  not be availble under other JVMs because the JIT
 *  may or may not strip that information from
 *  stack traces.<P>
 *
 *  Sustained, rapid creation of <tt>java.lang.Throwable</tt>
 *  objects under the IBM JDK repeatedly caused JVM deadlocks.
 *  This should not be an issue in actual systems, but
 *  it is concerning.
 */
public class StackTraceUtil
{
    private static StackTraceUtil instance = null;

    static
    {
        // try and load the JDK 1.4 version of ourselves
        // and fail back on the "normal" version.
        // The JDK 1.4 version is much faster.
        try
        {
            instance = (StackTraceUtil)Class.forName("com.protomatter.util.JDK14StackTraceUtil").newInstance();
            instance.getInfo(0);
        }
        catch (Throwable t)
        {
            instance = new StackTraceUtil();
        }
    }

    /**
     *  Protected constructor so nobody instantiates this class.
     */
    protected StackTraceUtil()
    {
    }

    private static String LINE_SEP = System.getProperty("line.separator");

    private static char SPACE = ' ';
    private static char DOT = '.';
    private static char COLON = ':';
    private static char OPEN_P = '(';
    private static char CLOSE_P = ')';

    /**
     * Determine what class and method you are in.
     */
    public static StackTraceInfo whereAmI()
    {
        return instance.getInfo(1);
    }

    /**
     * Determine what class and method you are in.
     * The offset is how many levels above where
     * this method is called.
     */
    public static StackTraceInfo whereAmI(int stackOffset)
    {
        return instance.getInfo(++stackOffset);
    }

    protected StackTraceInfo getInfo(int stackOffset)
    {
        Throwable t = new Throwable();
        StringWriter sw = new StringWriter(256);
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        String stack = sw.toString();

        stackOffset++;

        int start;
        int end;
        int dot;

        String className = null;
        String methodName = null;
        int lineNumber = StackTraceInfo.LINE_NUMBER_UNKNOWN;

        try
        {
            start = stack.indexOf(LINE_SEP) +1;
            for (dot=0; dot<stackOffset; dot++)
            {
                start = stack.indexOf(LINE_SEP, ++start);
            }
            start = stack.indexOf(SPACE, start);
            end = stack.indexOf(OPEN_P, start);
            dot = stack.lastIndexOf(DOT, end);

            className = stack.substring(++start, dot);
            methodName = stack.substring(++dot, end);

            // see if we can get the line number
            dot = stack.indexOf(COLON, end);
            if (dot > 0)
            {
              end = stack.indexOf(CLOSE_P, dot);
              lineNumber = Integer.parseInt(stack.substring(++dot, end));
            }
        }
        catch (Exception x)
        {
            ; // just return as much as is ready
        }

        return new StackTraceInfo(className, methodName, lineNumber);
    }

    private static class TestThread
    extends Thread
    {
        private int numRuns = 0;
        private long time = 0;
        private StackTraceInfo info = null;

        public TestThread(int runs)
        {
            super();
            this.numRuns = runs;
        }

        public void run()
        {
            time = System.currentTimeMillis();
            for (int i=0; i<numRuns; i++)
            {
                info = StackTraceUtil.whereAmI();
            }
            time = System.currentTimeMillis() - time;
        }

        public void info()
        {
            DecimalFormat format = new DecimalFormat("####.######");
            DecimalFormat tf = new DecimalFormat("###,###,##0");
            System.out.println("   " + tf.format(numRuns) + " runs in " + tf.format(time) + "ms");
            double average = ((double)time / (double)numRuns);
            System.out.println("   Average is " + format.format(average) + "ms");
            System.out.println("   Stack trace info = " + info);
            System.out.println("");
        }
    }

    /**
     *  Performance test rig.  Optional command-line
     *  arguments are the number of threads (default is 5),
     *  and number of calls per thread (default is 10,000).
     */
    public static void main(String args[])
    {
        try
        {
            int numThreads = 5;
            int numTries = 10000;

            if (args.length == 2)
            {
                try
                {
                    numThreads = Integer.parseInt(args[0]);
                    numTries = Integer.parseInt(args[1]);
                }
                catch (NumberFormatException x)
                {
                    System.out.println("Usage: java com.protomatter.util.StackTraceUtil num-threads num-calls");
                    System.exit(0);
                }
            }
            else if ((args.length == 1) || (args.length > 2))
            {
                System.out.println("Usage: java com.protomatter.util.StackTraceUtil num-threads num-calls");
                System.exit(0);
            }

            DecimalFormat tf = new DecimalFormat("###,###,##0");
            System.out.println("StackTraceUtil.whereAmI() test:");
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
            System.out.println("Creating " + numThreads + " test threads (" + tf.format(numTries) + " calls each)...");
            TestThread threads[] = new TestThread[numThreads];
            for (int i=0; i<numThreads; i++)
            {
                threads[i] = new TestThread(numTries);
            }

            System.out.println("Running test threads...");
            long time = System.currentTimeMillis();
            for (int i=0; i<numThreads; i++)
            {
                threads[i].start();
            }
            for (int i=0; i<numThreads; i++)
            {
                threads[i].join();
            }
            time = System.currentTimeMillis() - time;

            System.out.println("Per-thread results:");
            for (int i=0; i<numThreads; i++)
            {
                threads[i].info();
            }

            System.out.println("");
            System.out.println("Overall:");
            DecimalFormat format = new DecimalFormat("####.######");
            System.out.println("   " + tf.format((numThreads * numTries)) + " runs in " + tf.format(time) + "ms");
            double average = ((double)time / (double)(numThreads * numTries));
            System.out.println("   Average is " + format.format(average) + "ms");
            System.out.println("");
        }
        catch (Exception x)
        {
            x.printStackTrace();
        }
    }
}
