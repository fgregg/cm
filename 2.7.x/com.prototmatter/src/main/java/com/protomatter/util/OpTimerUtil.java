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
import java.net.*;
import java.sql.*;
import java.util.*;
import java.text.*;

import com.protomatter.util.*;

/**
 *  A utility class for parsing log files with timer traces.
 *  Run this class as follows for a usage display:<P>
 *
 *  <PRE>
 *  java com.protomatter.util.OpTimerUtil
 *  </PRE>
 *
 *  Example output looks like the following:<P>
 *
 *  <PRE><font size="-1"><B>
 *  Reading file "file.txt"
 *  Parsed 2843 lines.
 *  Found 1 unique timer names
 *  Found 2843 timer traces
 *
 *  Timing information:
 *  ---------------------------------------------------------------------------
 *  OpTimer: My timer
 *    count   = 2,843
 *    total   = 222,946 ms
 *    min     = 0 ms
 *    max     = 360 ms
 *    median  = 65 ms
 *    average = 78.42 ms
 *
 *    Histogram:
 *    Median: ------------v
 *   Average: --------------v
 *        132   o   o
 *            o ooo o   o
 *            ooooo o  ooo o
 *         88 oooooooo oooooo
 *            ooooooooooooooooooo
 *            ooooooooooooooooooooo
 *         44 ooooooooooooooooooooooooo  o
 *            ooooooooooooooooooooooooooooooo
 *            oooooooooooooooooooooooooooooooooooooo  o
 *    count 0 ooooooooooooooooooooooooooooooooooooooooooooooooooooo ooooooo oo    o
 *            |          |          |          |          |          |          |
 *     millis 0         58        116        175        233        291        349
 *
 *  </B></font></PRE><P>
 *
 *  The graph axes are automatically scaled for the data being parsed.  This
 *  can be very useful for gathering statistics from log files to determine
 *  system bottlenecks.<P>
 *
 *  Command line options include the ability to only display analytic data for
 *  a subset of the timers traced in a given log file.
 *
 *  @see OpTimer
 */
public class OpTimerUtil
{
  private static Map countMap = new HashMap();
  private static Map totalMap = new HashMap();
  private static Map minMap = new HashMap();
  private static Map maxMap = new HashMap();
  private static Map dataMap = new HashMap();
  private static Collator collator = null;
  private static Set nameSet = null;
  private static Map nameMap = new HashMap();
  private static Set displayNameSet = new HashSet();

  static
  {
    collator = Collator.getInstance(new Locale("en", "US", ""));
    nameSet = new TreeSet(collator);
  }

  /**
   *  Private constructor so people don't go around creating these.
   */
  private OpTimerUtil()
  {
  }

  private static void usage()
  {
    System.out.println("");
    System.out.println("Usage:");
    System.out.println("  java com.protomatter.util.OpTimer logfile.txt [timer_name_1 ... timer_name_N]");
    System.out.println("");
    System.out.println("This program examines the given logfile to find all timers and then displays");
    System.out.println("aggregate timing information.  Number of occurrences, min, max, and average");
    System.out.println("times are reported.");
    System.out.println("");
    System.out.println("If timers are named on the command line, then only those timers will be");
    System.out.println("analyzed.  If none are named on the command line, then all timers found");
    System.out.println("in the log file will be analyzed.");
    System.exit(0);
  }

  public static void main(String args[])
  {
    if (args.length == 0)
      usage();

    for (int i=1; i<args.length; i++)
      displayNameSet.add(args[i]);

    try
    {
      System.out.println("Reading file \"" + args[0] + "\"");
      long startTime = System.currentTimeMillis();
      BufferedReader reader = new BufferedReader(
        new FileReader(new File(args[0])));

      String timerName = null;
      long elapsed = 0;
      String line = null;
      long numLines = 0;
      long numFound = 0;
      int index0;
      int index;
      int index2;
      int index3;
      int index4;
      String elapsedString = null;
      while ((line = reader.readLine()) != null)
      {
        numLines++;

        index = 0;
        while ((index0 = line.indexOf("OpTimer[", index)) != -1)
        {
          index = index0 + 8;
          index2 = line.indexOf(", thread=", index);
          if (index2 != -1)
          {
            timerName = line.substring(index, index2);
            index = index2 + 9;
            index3 = line.indexOf(", took ", index);
            if (index3 != -1)
            {
              index = index3 + 7;
              index4 = line.indexOf("ms]", index);
              if (index4 != -1)
              {
                elapsedString = line.substring(index, index4);
                index = index4 + 3;
                try
                {
                  elapsed = Long.parseLong(elapsedString);
                  ++numFound;
                  handleMatch(timerName, elapsed);
                }
                catch (Exception x) { ; }
              }
            }
            else
            {
              index = index2;
            }
          }
          else
          {
            index2 = line.indexOf(", took ", index);
            if (index2 != -1)
            {
              timerName = line.substring(index, index2);
              index = index2 + 7;
              index3 = line.indexOf("ms]", index);
              if (index3 != -1)
              {
                elapsedString = line.substring(index, index3);
                index = index3 + 3;
                try
                {
                  elapsed = Long.parseLong(elapsedString);
                  ++numFound;
                  handleMatch(timerName, elapsed);
                }
                catch (Exception x) { ; }
              }
            }
          }
        }
      }
      reader.close();
      startTime = System.currentTimeMillis() - startTime;
      double parseTime = ((double)startTime) / (double)1000;

      DecimalFormat numberFormat = new DecimalFormat("###,###,###,###,##0.00");
      DecimalFormat longFormat = new DecimalFormat("###,###,###,###,##0");

      System.out.println("Parse took " + numberFormat.format(parseTime) + " sec.");
      System.out.println("Parsed " + longFormat.format(numLines) + " lines.");
      System.out.println("Found " + longFormat.format(nameSet.size()) + " unique timer names");
      System.out.println("Found " + longFormat.format(numFound) + " timer traces");
      System.out.println("");
      System.out.println("Timing information:");

      Iterator i = null;
      if (displayNameSet.size() > 0)
        i = displayNameSet.iterator();
      else
        i = nameSet.iterator();

      int timerId = 0;
      while (i.hasNext())
      {
        timerName = (String)i.next();
        if (!nameSet.contains(timerName))
          continue;

        ++timerId;

        long min = ((Long)minMap.get(timerName)).longValue();
        long max = ((Long)maxMap.get(timerName)).longValue();
        long count = ((Long)countMap.get(timerName)).longValue();
        long total = ((Long)totalMap.get(timerName)).longValue();
        List data = (List)dataMap.get(timerName);

        Collections.sort(data,
          new Comparator()
          {
            public int compare(Object o1, Object o2)
            {
              long l1 = ((Long)o1).longValue();
              long l2 = ((Long)o2).longValue();
              if (l1 < l2) return -1;
              if (l1 == l2) return 0;
              return 1;
            }
            public boolean equals(Object o) { return false; }
          });

        long median = ((Long)data.get(data.size() /2)).longValue();

        double average = ((double)total)/((double)count);

        System.out.println("---------------------------------------------------------------------------");
        System.out.println("OpTimer: " + timerName);
        System.out.println("  count   = " + longFormat.format(count));
        System.out.println("  total   = " + longFormat.format(total) + " ms");
        System.out.println("  min     = " + longFormat.format(min) + " ms");
        System.out.println("  max     = " + longFormat.format(max) + " ms");
        System.out.println("  median  = " + longFormat.format(median) + " ms");
        System.out.println("  average = " + numberFormat.format(average) + " ms");
        System.out.println("");
        System.out.println("  Histogram:");
        histogram(timerName, 80, 10);
        System.out.println("");
        System.out.println("");
      }
    }
    catch (Exception x)
    {
      x.printStackTrace();
    }
  }

  private static void histogram(String timerName, int size, int height)
  {
    size -= 12;
    long min = ((Long)minMap.get(timerName)).longValue();
    long max = ((Long)maxMap.get(timerName)).longValue();
    long count = ((Long)countMap.get(timerName)).longValue();
    long total = ((Long)totalMap.get(timerName)).longValue();
    double average = ((double)total)/((double)count);
    List data = (List)dataMap.get(timerName);
    double width = ((double)max) - ((double)min);
    double bucket = width / (double)size;
    double point;

    size++;
    int graph[] = new int[size];
    int middle = data.size() /2;
    double middlePoint = (double)((Long)data.get(middle)).longValue();

    for (int i=0; i<data.size(); i++)
    {
      point = (double)(((Long)data.get(i)).longValue() - min);
      int position = (int)(point / bucket);
      graph[position]++;
    }

    int top = 0;
    for (int i=0; i<size; i++)
    {
      if (graph[i] > top)
        top = graph[i];
    }

    DecimalFormat yFormat = new DecimalFormat("#########");
    DecimalFormat xFormat = new DecimalFormat("#######");
    double heightBucket = ((double)top)/((double)height);

    System.out.print(right("Median: ", 10));
    boolean mid = false;
    for (int x=0; x<=size && !mid; x++)
    {
      double val = (bucket * (double)x) + (double)min;
      double nextVal = (bucket * (double)(x +1)) + (double)min;
      if ((val <= middlePoint) && (middlePoint < nextVal))
      {
        System.out.println("v");
        mid = true;
      }
      else if (val < average)
      {
        System.out.print("-");
      }
    }

    System.out.print(right("Average: ", 10));
    boolean avg = false;
    for (int x=0; x<=size && !avg; x++)
    {
      double val = (bucket * (double)x) + (double)min;
      double nextVal = (bucket * (double)(x +1)) + (double)min;
      if ((val <= average) && (average < nextVal))
      {
        System.out.println("v");
        avg = true;
      }
      else if (val < average)
      {
        System.out.print("-");
      }
    }

    for (int y=(height-1); y>=0; y--)
    {
      if ((y % 3) == 0)
      {
        double localMin = (heightBucket * (double)y) + (double)min;
        if (y == 0)
        {
          System.out.print(right("count 0", 9));
        }
        else
        {
          System.out.print(right(yFormat.format(localMin), 9));
        }
      }
      else
      {
        System.out.print(right("", 9));
      }

      System.out.print(" ");
      for (int x=0; x<size; x++)
      {
        if (graph[x] > (int)(heightBucket * (double)y))
          System.out.print("o");
        else
          System.out.print(" ");
      }
      System.out.println("");
    }

    int xTick = 11;
    System.out.print(left("", 10));
    for (int x=0; x<=size; x++)
    {
      if ((x%xTick) == 0)
      {
        if (x == 0)
        {
         System.out.print("|");
        }
        else
        {
          System.out.print(right("|", xTick));
        }
      }
    }
    System.out.println("");

    System.out.print(right("millis ", 10));
    for (int x=0; x<=size; x++)
    {
      if ((x%xTick) == 0)
      {
        if (x == 0)
        {
          System.out.print("0");
        }
        else
        {
          System.out.print(right(xFormat.format((bucket * x) + min), xTick));
        }
      }
    }
    System.out.println("");
  }

  private static String left(String text, int width)
  {
    StringBuffer b = new StringBuffer(width);
    b.append(text);
    int size = width - text.length();
    for (int i=0; i<size; i++)
      b.append(" ");
    return b.toString();
  }

  private static String right(String text, int width)
  {
    StringBuffer b = new StringBuffer(width);
    int size = width - text.length();
    for (int i=0; i<size; i++)
      b.append(" ");
    b.append(text);
    return b.toString();
  }

  private static void handleMatch(String timerName, long elapsed)
  {
    if ((displayNameSet.size() > 0) && (!displayNameSet.contains(timerName)))
      return;

    nameSet.add(timerName);

    List data = (List)dataMap.get(timerName);
    if (data == null)
    {
      data = new ArrayList();
      dataMap.put(timerName, data);
    }
    data.add(new Long(elapsed));
    Long longVal = (Long)totalMap.get(timerName);
    if (longVal == null)
    {
      longVal = new Long(elapsed);
    }
    else
    {
      longVal = new Long(longVal.longValue() + elapsed);
    }
    totalMap.put(timerName, longVal);

    longVal = (Long)countMap.get(timerName);
    if (longVal == null)
    {
      longVal = new Long(1);
    }
    else
    {
    longVal = new Long(longVal.longValue() + 1);
    }
    countMap.put(timerName, longVal);

    longVal = (Long)minMap.get(timerName);
    if (longVal == null)
    {
      longVal = new Long(elapsed);
    }
    else
    {
      if (longVal.longValue() > elapsed)
        longVal = new Long(elapsed);
    }
    minMap.put(timerName, longVal);

    longVal = (Long)maxMap.get(timerName);
    if (longVal == null)
    {
      longVal = new Long(elapsed);
    }
    else
    {
      if (longVal.longValue() < elapsed)
        longVal = new Long(elapsed);
    }
    maxMap.put(timerName, longVal);
  }
}
