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
import java.util.*;
import java.text.*;
import org.apache.oro.text.regex.*;

/**
 *  A simple hierarchical namespace utility.
 *  Maintains a static list of names and/or patterns
 *  to match names.  Dots (".") provide hierarchy markers.
 *  For instance, if you add the following patterns:<P>
 *
 *  <dl>
 *    <dd><tt>com.protomatter</tt></dd>
 *    <dd><tt>com.protomatter.syslog.*</tt></dd>
 *    <dd><tt>com.protomatter.util.*</tt></dd>
 *    <dd><tt>com.protomatter.foo.bar.*</tt></dd>
 *  </dl><P>
 *
 *  Then the following names would "match" the
 *  patterns if the "scanning" feature is turned on:<P>
 *
 *  <dl>
 *    <dd><tt>com.protomatter</tt></dd>
 *    <dd><tt>com.protomatter.syslog</tt></dd>
 *    <dd><tt>com.protomatter.syslog.xml</tt></dd>
 *    <dd><tt>com.protomatter.util.other.package</tt></dd>
 *  </dl><P>
 *
 *  If you have "scanning" turned off, then only the
 *  following names would match:<P>
 *
 *  <dl>
 *    <dd><tt>com.protomatter</tt></dd>
 *  </dl><P>
 *
 *  And the following names would not "match" the
 *  patterns:<P>
 *
 *  <dl>
 *    <dd><tt>com.protomatterfoo</tt></dd>
 *    <dd><tt>com.protomatter.foo</tt></dd>
 *    <dd><tt>com.protomatter.jdbc.pool</tt></dd>
 *  </dl><P>
 *
 *  This class is useful in debugging:<P>
 *
 *  <UL><TABLE BORDER=1 CELLPADDING=4 CELLSPACING=0>
 *  <TR><TD>
 *  <PRE><B>
 *
 *  import com.protomatter.util.Debug;
 *  import com.protomatter.util.StackTraceUtil;
 *
 *  import com.protomatter.syslog.Channel;
 *
 *  ...
 *
 *  private final static Debug debug = Debug.forPackage(ThisClass.class); &nbsp;
 *
 *  private final static Channel channel = Channel.forPackage(ThisClass.class); &nbsp;
 *
 *  ...
 *
 *  // three levels...  trace, debug and info
 *  if (debug.trace())
 *      channel.debug(this, "Really detailed tracing statements");
 *
 *  if (debug.debug())
 *      channel.debug(this, "Debugging statements:  I'm here -> " + StackTraceUtil.whereAm()); &nbsp;
 *
 *  if (debug.info())
 *      channel.info(this, "Information messages");
 *
 *  </B></PRE>
 *  </TD></TR></TABLE></UL><P>
 *
 *  This can prevent expensive operations involved in debugging
 *  to be quickly bypassed in production environments
 *  by changing a configuration file instead of re-compiling.<P>
 *
 *  All the work is done in the constructor, which calls
 *  the <tt><a href="#match(java.lang.String)">match()</a></tt> method.
 *  Calling the
 *  <tt><a href="#trace()">trace()</a></tt>,
 *  <tt><a href="#debug()">debug()</a></tt> or 
 *  <tt><a href="#info()">info()</a></tt>
 *  methods takes almost no time (return values are cached).<P>
 *
 *  I've tested this operation on a 650MHz PIII Coppermine Sony Vaio
 *  laptop running RedHat Linux 7.2, kernel 2.4.9,
 *  I saw these results for timing "<tt>Debug.forName(name)</tt>":<P>
 *
 *  <ul><table border=1 cellpadding=4 cellspacing=0>
 *
 *  <tr>
 *    <th>Virtual Machine</th>
 *    <th>Best&nbsp;Case</th>
 *    <th>Middle&nbsp;Case</th>
 *    <th>Worst&nbsp;Case</th>
 *  </tr>
 *
 *  <tr>
 *  <td>Classic VM<BR>
 *      (build JDK-1.2.2_012, green threads, nojit)</td>
 *  <td valign=top>0.018627ms</td>
 *  <td valign=top>0.076707ms</td>
 *  <td valign=top>0.625373ms</td>
 *  </tr>
 *
 *  <tr>
 *  <td>Java(TM) 2 Runtime Environment,<BR>
 *      Standard Edition (build 1.3.1_03-b03)</td>
 *  <td valign=top>0.001347ms</td>
 *  <td valign=top>0.008267ms</td>
 *  <td valign=top>0.08128ms</td>
 *  </tr>
 *
 *  <tr>
 *  <td>Java HotSpot(TM) Client VM<BR>
 *      (build 1.4.0-b92, mixed mode)</td>
 *  <td valign=top>0.00184ms</td>
 *  <td valign=top>0.008067ms</td>
 *  <td valign=top>0.07928ms</td>
 *  </tr>
 *
 *  <tr>
 *  <td>JRockit Virtual Machine<BR>
 *      (build 3.1.4-dax.appeal.se-20020319-1000)<BR>
 *      Native Threads, Generational Concurrent Garbage Collector</td>
 *  <td valign=top>0.00092ms</td>
 *  <td valign=top>0.006453ms</td>
 *  <td valign=top>0.065147ms</td>
 *  </tr>
 *
 *  <tr>
 *  <td>Classic VM<BR>
 *      (build 1.3.1, J2RE 1.3.1 IBM build cxia32131-20020410<BR>
 *      (JIT enabled: jitc))</td>
 *  <td valign=top>0.00204ms</td>
 *  <td valign=top>0.00488ms</td>
 *  <td valign=top>0.039253ms</td>
 *  </tr>
 *
 *  </table></ul><P>
 *
 *  "Best Case" above is a direct match, where the name
 *  being matched is the same as one of the patterns.
 *  "Middle Case" is a search only one or two checks
 *  away from success, like matching "<tt>foo.bar.baz</tt>"
 *  against "<tt>foo.bar.*</tt>".  "Worst Case" is a match
 *  that's 13 checks away. All cases had around 125 patterns
 *  in the search group.  Your mileage may vary.<P>
 *
 *  If you call <tt>setScan(false)</tt>, then all matches
 *  will be "Best Case" but only exact matches will
 *  return <tt>true</tt>.<P>
 *
 *  If <tt>disable()</tt> has been called, this method
 *  will return <tt>false</tt> immediately.  If there
 *  are no patterns set, it has the same effect.<P>
 *
 *  @see com.protomatter.syslog.Channel
 *  @see com.protomatter.syslog.Syslog
 *  @see com.protomatter.util.StackTraceUtil
 *  @see com.protomatter.syslog.xml.SyslogXML#configure(org.jdom.Element)
 */
public class Debug
implements java.io.Serializable
{
    /**
     *  The set of patterns and/or names to match with for the "trace" severity.
     */
    private static Set tracePatternSet = new HashSet();

    /**
     *  The set of patterns and/or names to match with for the "debug" severity.
     */
    private static Set debugPatternSet = new HashSet();

    /**
     *  The set of patterns and/or names to match with for the "info" severity.
     */
    private static Set infoPatternSet = new HashSet();

    /**
     *  The "." character.
     */
    private final static char DOT = '.';

    /**
     *  The String ".*".
     */
    private final static String DOT_STAR = ".*";

    /**
     *  Disables everything.  <tt>true</tt> = disabled,
     *  <tt>false</tt> = enabled (default).
     */
    private static boolean allOff = false;

    /**
     *  Scan "up" the naming hierarchy for
     *  matches.  <tt>true</tt> = yes,
     *  <tt>false</tt> = no (default).
     */
    private static boolean scan = false;

    /**
     *  Is this instance "on" for "trace"?.
     */
    private boolean traceOn = false;

    /**
     *  Is this instance "on" for "debug"?.
     */
    private boolean debugOn = false;

    /**
     *  Is this instance "on" for "info"?.
     */
    private boolean infoOn = false;

    /**
     *  The name of this instance.
     */
    private String name = null;
    
    /**
     *  The last time configuration info was updated.
     */
    private static long configLastUpdateTime = 0;
    
    /**
     *  The last time this instance was configured.  This
     *  is used to determine if we should call init() again
     *  before returning from the debug() info() or trace()
     *  methods.
     */
    private long lastUpdateTime = 0;

    /**
     *  The name of the "trace" severity, for the <tt>match()</tt> method.
     */
    private static final int TRACE = 0;

    /**
     *  The name of the "debug" severity, for the <tt>match()</tt> method.
     */
    private static final int DEBUG = 1;

    /**
     *  The name of the "info" severity, for the <tt>match()</tt> method.
     */
    private static final int INFO = 2;

    /**
     *  Protected constructor.  Calls <tt>init()</tt>.
     */
    private Debug(String name)
    {
      super();

      this.name = name;
      init();
    }

    /**
     *  Initialize this <tt>Debug</tt> instance.  Each time
     *  this method is called, the name of this instance
     *  is matched against the various patterns for
     *  each severity level.  This method is called
     *  by the constructor.
     */
    public final void init()
    {
      this.traceOn = match(name, TRACE);
      this.debugOn = match(name, DEBUG);
      this.infoOn = match(name, INFO);
      this.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     *  Get a debug object with name of the
     *  given class.
     *  Pass in the class <tt>com.protomatter.syslog.Channel</tt>
     *  would use the name "<tt>com.protomatter.syslog.Channel</tt>".
     *  A new instance of this class
     *  is created (and initialized) each time this method is called.
     */
    public final static Debug forClass(Class someClass)
    {
        return new Debug(someClass.getName());
    }

    /**
     *  Get a debug object with name of the
     *  given class's package.
     *  Pass in the class <tt>com.protomatter.syslog.Channel</tt>
     *  would use the name "<tt>com.protomatter.syslog</tt>".
     *  If the class has no package, the name of the
     *  class is used.  A new instance of this class
     *  is created (and initialized) each time this method is called.
     */
    public final static Debug forPackage(Class someClass)
    {
        String packageName = someClass.getName();
        int pos = packageName.lastIndexOf(DOT);
        if (pos < 0)
            return new Debug(packageName);
        return new Debug(packageName.substring(0, pos));
    }

    /**
     *  Get a debug object with the given name.  A new instance of this class
     *  is created (and initialized) each time this method is called.
     */
    public final static Debug forName(String name)
    {
        return new Debug(name);
    }

    /**
     *  Disable all checks.  This effectively means
     *  that the <tt>on()</tt> and <tt>match()</tt> methods
     *  on all instances of this class will return
     *  <tt>false</tt> until the <tt>enable()</tt> 
     *  method is called.
     */
    public final static void disable()
    {
        allOff = true;
    }

    /**
     *  Enable checks.  This is the default state.
     */
    public final static void enable()
    {
        allOff = false;
    }

    /**
     *  Determine if checks are enabled or disabled.
     */
    public final static boolean isEnabled()
    {
        return (!allOff);
    }

    /**
     *  Does our name match anything in the list of names and patterns.
     *  This value is cached when this object is instantiated, but is
     *  updated if the configuration is changed after this instance
     *  is created.
     */
    public final boolean trace()
    {
        if ((this.lastUpdateTime < Debug.configLastUpdateTime) && (!allOff))
            init();
        return (allOff) ? false : this.traceOn;
    }

    /**
     *  Does our name match anything in the list of names and patterns.
     *  This value is cached when this object is instantiated, but is
     *  updated if the configuration is changed after this instance
     *  is created.
     */
    public final boolean debug()
    {
        if ((this.lastUpdateTime < Debug.configLastUpdateTime) && (!allOff))
            init();
        return (allOff) ? false : this.debugOn;
    }

    /**
     *  Does our name match anything in the list of names and patterns.
     *  This value is cached when this object is instantiated, but is
     *  updated if the configuration is changed after this instance
     *  is created.
     */
    public final boolean info()
    {
        if ((this.lastUpdateTime < Debug.configLastUpdateTime) && (!allOff))
            init();
        return (allOff) ? false : this.infoOn;
    }

    /**
     *  Does the given name match anything in the list of names and patterns.<P>
     *
     *  @see #disable()
     *  @see #enable()
     *  @see #setScan(boolean)
     */
    private final static boolean match(String name, int severity)
    {
      if (name == null)
        return false;

      // is everything turned off?
      if (allOff)
        return false;

      Set patternSet = null;
      if (TRACE == severity)
        patternSet = tracePatternSet;
      else if (DEBUG == severity)
        patternSet = debugPatternSet;
      else if (INFO == severity)
        patternSet = infoPatternSet;

      // no patterns so no matches
      if (patternSet.isEmpty())
        return false;

      // scan up the namespace for matches?
      if (scan)
      {
        if (patternSet.contains(name))
          return true;

        int index = 1;
        while (index > 0)
        {
          if (patternSet.contains(name + DOT_STAR))
          {
              return true;
          }
          index = name.lastIndexOf(DOT);
          if (index < 0)
              return false;
          name = name.substring(0, index);
        }
        return false;
      }

      // find an exact match
      return patternSet.contains(name);
    }

    /**
     *  Set if we scan for matches.  Default is false.
     *  If set, we scan "up" the name hierarchy,
     *  which can be a little time consuming.  It
     *  may make sense to enable, and then
     *  only allow for exact matches on names.
     */
    public final static void setScan(boolean setting)
    {
      scan = setting;
      configLastUpdateTime = System.currentTimeMillis();
    }

    /**
     *  Should we scan for matches?  Default is false.
     *  If set, we scan "up" the name hierarchy,
     *  which can be a little time consuming.  It
     *  may make sense to enable, and then
     *  only allow for exact matches on names.
     */
    public final static boolean getScan()
    {
      return scan;
    }

    /**
     *  Get the names and/or patterns to match at the "trace" severity.
     */
    public final static Iterator getTraceNames()
    {
        return tracePatternSet.iterator();
    }

    /**
     *  Get the names and/or patterns to match at the "debug" severity.
     */
    public final static Iterator getDebugNames()
    {
        return debugPatternSet.iterator();
    }

    /**
     *  Get the names and/or patterns to match at the "info" severity.
     */
    public final static Iterator getInfoNames()
    {
        return infoPatternSet.iterator();
    }

    /**
     *  Add a name or pattern to the matching set at the "trace" level.
     */
    public final static void addTraceName(String name)
    {
        tracePatternSet.add(name);
        configLastUpdateTime = System.currentTimeMillis();
    }

    /**
     *  Add a name or pattern to the matching set at the "debug" level.
     */
    public final static void addDebugName(String name)
    {
        debugPatternSet.add(name);
        configLastUpdateTime = System.currentTimeMillis();
    }

    /**
     *  Add a name or pattern to the matching set at the "info" level.
     */
    public final static void addInfoName(String name)
    {
        infoPatternSet.add(name);
        configLastUpdateTime = System.currentTimeMillis();
    }

    /**
     *  Clear the sets of patterns and names.
     */
    public final static void clear()
    {
        tracePatternSet = new HashSet();
        debugPatternSet = new HashSet();
        infoPatternSet = new HashSet();
        configLastUpdateTime = System.currentTimeMillis();
    }

    /**
     *  Get the name this instance uses.
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     *  Return a human-readable representation of this object.
     */
    public final String toString()
    {
        if (this.lastUpdateTime < Debug.configLastUpdateTime)
            init();
        StringBuffer b = new StringBuffer();
        b.append("Debug[");
        b.append(name);
        b.append(" ");
        if (!traceOn && !debugOn && !infoOn)
        {
            b.append("OFF");
        }
        else if (traceOn && debugOn && infoOn)
        {
            b.append("ON");
        }
        else
        {
            b.append("trace");
            b.append((this.traceOn ? "=ON " : "=OFF "));

            b.append("debug");
            b.append((this.debugOn ? "=ON " : "=OFF "));

            b.append("info");
            b.append((this.infoOn ? "=ON" : "=OFF"));
        }

        b.append("]");

        return b.toString();
    }


    /**
     *  Performance testing rig.
     */
    public static void main(String args[])
    {
        try
        {
            if (args.length != 4)
            {
                System.out.println("Usage: java com.protomatter.util.Debug \\");
                System.out.println("         scan? numCalls patterns.txt tests.txt");
                System.out.println("");
                System.out.println(" scan:   true/false:    passed to Debug.setScan()");
                System.out.println("         numCalls:      number of Debug.forName() calls to make");
                System.out.println("         patterns.txt:  a text file containing strings");
                System.out.println("                        to pass to Debug.addInfoName()");
                System.out.println("         tests.txt:     a text file containing strings");
                System.out.println("                        to pass to Debug.forName() repeatedly.");
                System.out.println("");
                System.exit(0);
            }

            Debug.setScan("true".equalsIgnoreCase(args[0]));
            int numCalls = Integer.parseInt(args[1]);

            BufferedReader names = new BufferedReader(new FileReader(new File(args[2])));
            BufferedReader tests = new BufferedReader(new FileReader(new File(args[3])));


            System.out.println("Debug.forName() and Debug.info() performance test");
            System.out.println("");

            Debug.setScan(true);
            System.out.println("JVM Information:");
            System.out.println("  VM Name:          " + System.getProperty("java.vm.name"));
            System.out.println("  VM Version:       " + System.getProperty("java.vm.version"));
            System.out.println("  Runtime name:     " + System.getProperty("java.runtime.name"));
            System.out.println("  Runtime version:  " + System.getProperty("java.runtime.version"));
            System.out.println("");
            System.out.println("OS Information:");
            System.out.println("  " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
            System.out.println("");
            System.out.println("Scan:   " + Debug.getScan());


            System.out.println("Adding names:");
            String line = null;
            int num = 0;
            while ((line = names.readLine()) != null)
            {
                //System.out.println("   " + line.trim());
                Debug.addInfoName(line.trim());
                ++num;
            }
            System.out.println("  added " + num + " names/patterns");

            System.out.println("");
            System.out.println("Testing names:");
            DecimalFormat tf = new DecimalFormat("###,###,###,###");
            DecimalFormat format = new DecimalFormat("###.#######");
            Debug debug = null;
            Debug debug1 = null;
            while ((line = tests.readLine()) != null)
            {
                line = line.trim();
                System.out.println("   instance = Debug.forName(\"" + line + "\");");
                long time = System.currentTimeMillis();
                for (int i=0; i<numCalls; i++)
                {
                    debug = Debug.forName(line);
                }
                if (debug1 == null)
                    debug1 = debug;
                time = System.currentTimeMillis() - time;
                System.out.println("   " + debug); 
                System.out.println("   " + tf.format(numCalls) + " calls in " + tf.format(time) + "ms");
                double average = ((double)time / (double)numCalls);
                double persecond = ((double)1 / average) * 1000;
                System.out.println("   Average      = " + format.format(average) + "ms");
                System.out.println("   Per-second   = " + tf.format(persecond));
                System.out.println("");
            }
            System.out.println("");
            System.out.println("");
            numCalls = numCalls * 1000;
            System.out.println(tf.format(numCalls) + " instance.info() calls");
            long time = System.currentTimeMillis();
            boolean on = false;
            for (int i=0; i<numCalls; i++)
            {
                on = debug1.info();
            }
            time = System.currentTimeMillis() - time;
            System.out.println("   " + debug1); 
            System.out.println("   " + tf.format(numCalls) + " calls in " + tf.format(time) + "ms");
            double average = ((double)time / (double)numCalls);
            double persecond = ((double)1 / average) * 1000;
            System.out.println("   Average     = " + format.format(average) + "ms");
            System.out.println("   Per-second  = " + tf.format(persecond));
            System.out.println("");
        }
        catch (Exception x)
        {
            x.printStackTrace();
        }
    }
}
