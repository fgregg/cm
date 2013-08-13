package com.protomatter.jdbc.pool;

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
import java.sql.*;
import java.util.*;

import com.protomatter.syslog.*;
import com.protomatter.syslog.xml.SyslogXML;

/**
 *  A standalone connection pool testing application. Here is an
 *  example config file that will hammer a MySQL database:<P>
 *
 *  <blockquote><pre>
 *  #####################################################
 *  ##
 *  ## Protomatter JDBC Connection Pool test
 *  ## harness config file.
 *  ##
 *
 *  ##
 *  ## The classname of the underlying JDBC driver that the
 *  ## pool driver will use.
 *  ##
 *  jdbc.driver         = org.gjt.mm.mysql.Driver
 *
 *  ##
 *  ## The URL used to connect the underlying driver to
 *  ## the database.
 *  ##
 *  jdbc.URL            = jdbc:mysql://hostname:3306/mysql
 *
 *  ##
 *  ## Properties for the underlying driver and it's
 *  ## connection.  The format is "name=val,name=val,..."
 *  ##
 *  jdbc.properties     = user=root,password=secret
 *
 *  ##
 *  ## A statement to execute.  It doesn't matter what it
 *  ## is, it should just not throw an error when executed
 *  ## with Statement.executeQuery(...)
 *  ##
 *  jdbc.statement      = select * from user
 *
 *  ##
 *  ## A statement that is guaranteed to return at least 1
 *  ## row from the database. (for Oracle, this is
 *  ## "select 1 from dual", for Sybase and MySQL it's
 *  ## "select 1")
 *  ##
 *  jdbc.validityCheckStatement = select 1
 *
 *  ##
 *  ## Number of seconds between pool refreshes by the
 *  ## low priority pool refresh thread.
 *  ##
 *  pool.refreshThreadCheckInterval = 10
 *
 *  ##
 *  ## Should the refresh operation be verbose?
 *  ##
 *  pool.verboseRefresh = true
 *
 *  ##
 *  ## Should the pre-checkout validate operation be verbose?
 *  ##
 *  pool.verboseValidate = true
 *
 *  ##
 *  ## The initial size of the pool
 *  ##
 *  pool.initialSize    = 1
 *
 *  ##
 *  ## The maximum size that the pool should grow to.
 *  ##
 *  pool.maxSize        = 5
 *
 *  ##
 *  ## The time (in milliseconds) to sleep between tries
 *  ## to create multiple connections to the database
 *  ## (some databases can be overwhelmed by trying to
 *  ## open a group of connections too fast)
 *  ##
 *  pool.createWaitTime = 2000
 *
 *  ##
 *  ## If the pool needs to be grown, this is the number of
 *  ## connections to allocate at a time.
 *  ##
 *  pool.growBlock      = 2
 *
 *  ##
 *  ## Should the pool validate connectione before handing them out?
 *  ##
 *  pool.validateOnCheckout = true
 *
 *  ##
 *  ## For the test, the min and max times (in milliseconds)
 *  ## to sleep between tries to access the database.  A random
 *  ## interval between these times is picked by each thread.
 *  ##
 *  test.minSleepTime   = 1000
 *  test.maxSleepTime   = 2000
 *
 *  ##
 *  ## The number of test threads to spawn.
 *  ##
 *  test.numThreads     = 10
 *
 *  ##
 *  ## Number of times each thread should checkout a
 *  ## connection and use it.  If this number is -1, the
 *  ## test will run forever.  If this is set to something
 *  ## other than -1, when the run is complete, each thread
 *  ## will display statistics about how long it had to
 *  ## wait for connections from the pool.
 *  ##
 *  test.numRuns        = -1
 *
 *  ##
 *  ## If this property is present, Syslog is initialized using
 *  ## the given XML file.  If not, a logger is created that writes
 *  ## to standard out.
 *  ##
 *  Syslog.xml          = syslog.xml
 *  </pre></blockquote><P>
 *
 *  Modify this config file or write your own, and then pass it to
 *  <tt>PoolTest</tt> as it's first commandline argument, like this:
 *
 *  <blockquote><pre>
 *  java com.protomatter.jdbc.pool.PoolTest <i>config.properties</i>
 *  </pre></blockquote>
 *
 */
public class PoolTest
{
  private Properties props = null;

  public static void main(String args[])
  {
    if (args.length != 1)
    {
      System.out.println("Usage: com.protomatter.jdbc.pool.PoolTest props");
      System.exit(0);
    }
    try
    {
      Properties props = new Properties();
      props.load(new FileInputStream(new File(args[0])));

      if (props.getProperty("Syslog.xml") != null)
      {
          File file = new File(props.getProperty("Syslog.xml"));
          SyslogXML.configure(file);
      }
      else
      {
          Syslog.removeAllLoggers();
          Syslog.setLogMask("DEBUG");
    
          BasicLogger l = new PrintWriterLog(new PrintWriter(System.out));
          SimpleSyslogTextFormatter f = new SimpleSyslogTextFormatter();
          f.setShowThreadName(true);
          l.setTextFormatter(f);
          Syslog.addLogger(l);
      }

      PoolTest test = new PoolTest(props);
      test.run();
    }
    catch (Throwable x)
    {
      x.printStackTrace();
    }
  }

  private PoolTest(Properties props)
  {
    this.props = props;
  }

  private void run()
  {
    Syslog.info(this, "Test starting");

    Syslog.info(this, "Loading connection pool driver");
    try
    {
      Class.forName("com.protomatter.jdbc.pool.JdbcConnectionPoolDriver")
        .newInstance();
    }
    catch (Exception x)
    {
      Syslog.log(this, x);
      return;
    }

    Map args = new HashMap();

    String sql = props.getProperty("jdbc.statement");

    args.put("jdbc.driver", props.getProperty("jdbc.driver"));
    args.put("jdbc.URL",    props.getProperty("jdbc.URL"));
    args.put("jdbc.validityCheckStatement", props.getProperty("jdbc.validityCheckStatement"));

    Properties p = new Properties();
    StringTokenizer st
      = new StringTokenizer(props.getProperty("jdbc.properties"), ",");
    while (st.hasMoreTokens())
    {
      StringTokenizer st2 = new StringTokenizer(st.nextToken(), "=");
      p.put(st2.nextToken(), st2.nextToken());
    }
    args.put("jdbc.properties", p);

    args.put("pool.growBlock",
      new Integer(Integer.parseInt(props.getProperty("pool.growBlock"))));
    args.put("pool.initialSize",
      new Integer(Integer.parseInt(props.getProperty("pool.initialSize"))));
    args.put("pool.maxSize",
      new Integer(Integer.parseInt(props.getProperty("pool.maxSize"))));
    args.put("pool.createWaitTime",
      new Integer(Integer.parseInt(props.getProperty("pool.createWaitTime"))));
    args.put("pool.refreshThreadCheckInterval",
      new Integer(Integer.parseInt(props.getProperty("pool.refreshThreadCheckInterval"))));
    args.put("pool.validateOnCheckout",
      new Boolean(props.getProperty("pool.validateOnCheckout")));

    if (props.get("pool.verboseRefresh") != null)
    {
      args.put("pool.verboseRefresh",
        new Boolean(props.getProperty("pool.verboseRefresh")));
    }

    if (props.get("pool.verboseValidate") != null)
    {
      args.put("pool.verboseValidate",
        new Boolean(props.getProperty("pool.verboseValidate")));
    }

    boolean useMaidThread = false;
    if (props.get("pool.maxConnectionIdleTime") != null)
    {
      args.put("pool.maxConnectionIdleTime",
        new Integer(Integer.parseInt(
          props.getProperty("pool.maxConnectionIdleTime"))));
      useMaidThread = true;
    }
    if (props.get("pool.maidThreadCheckInterval") != null)
    {
      args.put("pool.maidThreadCheckInterval",
        new Integer(Integer.parseInt(
          props.getProperty("pool.maidThreadCheckInterval"))));
      useMaidThread = true;
    }


    int minSleepTime = Integer.parseInt(props.getProperty("test.minSleepTime"));
    int maxSleepTime = Integer.parseInt(props.getProperty("test.maxSleepTime"));
    int numThreads = Integer.parseInt(props.getProperty("test.numThreads"));

    int numRuns = Integer.parseInt(props.getProperty("test.numRuns"));

    JdbcConnectionPool pool = null;

    Syslog.info(this, "Connection pool properties:");
    Syslog.info(this, " driver             = " + props.getProperty("jdbc.driver"));
    Syslog.info(this, " URL                = " + props.getProperty("jdbc.URL"));
    Syslog.info(this, " properties         = " + args.get("jdbc.properties"));
    Syslog.info(this, " Query              = " + sql);
    Syslog.info(this, " initialSize        = " + args.get("pool.initialSize"));
    Syslog.info(this, " maxSize            = " + args.get("pool.maxSize"));
    Syslog.info(this, " growBlock          = " + args.get("pool.growBlock"));
    Syslog.info(this, " createWaitTime     = " + args.get("pool.createWaitTime"));
    Syslog.info(this, " validateOnCheckout = " + args.get("pool.validateOnCheckout"));
    Syslog.info(this, " validity stmt      = " + args.get("jdbc.validityCheckStatement"));
    Syslog.info(this, " check interval     = " + args.get("pool.refreshThreadCheckInterval"));

    if (useMaidThread)
    {
      Syslog.info(this, " Will use the maid thread.");
      Syslog.info(this, " Will NOT be closing connections ourself.");
      Syslog.info(this, " Maid thread check interval = " + args.get("pool.maidThreadCheckInterval") + " seconds");
      Syslog.info(this, " Max connection idle time   = " + args.get("pool.maxConnectionIdleTime") + " seconds");
    }
    else
    {
      Syslog.info(this, " Will NOT use the maid thread.");
    }

    try
    {
      Syslog.info(this, "Creating connection pool");
      pool = new JdbcConnectionPool("testPool", args);
    }
    catch (Exception x)
    {
      Syslog.log(this, x);
      return;
    }

    PoolMonitorThread pmt = new PoolMonitorThread(pool);
    pmt.start();

    Syslog.info(this, "Starting " + numThreads + " worker threads");
    for (int i=0; i<numThreads; i++)
    {
      PoolTestThread t = new PoolTestThread(i, sql, minSleepTime, maxSleepTime, numRuns, useMaidThread);
      t.start();
    }
  }
}

/**
 *  A slave thread for the PoolTest application.
 */
class PoolTestThread
extends Thread
{
  private String sql = null;
  private int id = 0;
  private int minSleepTime;
  private int maxSleepTime;
  private Random r = new Random();
  private int numRuns = 0;
  private int runs;
  private boolean useMaidThread = false;

  private long totalWaitTime = 0L;

  public PoolTestThread(int id, String sql, int minSleepTime, int maxSleepTime, int numRuns, boolean useMaidThread)
  {
    super();
    this.id = id;
    this.sql = sql;
    this.minSleepTime = minSleepTime;
    this.maxSleepTime = maxSleepTime;
    this.numRuns = numRuns;
    this.useMaidThread = useMaidThread;
  }

  private int getSleepTime()
  {
    float rand = r.nextFloat();
    int span = (maxSleepTime - minSleepTime);
    int time = minSleepTime + (int)(rand * span);
    return time;
  }

  public void run()
  {
    boolean done = false;
    while (!done)
    {
      Connection c = null;
      Statement s = null;
      ResultSet r = null;
      try
      {
        long time = System.currentTimeMillis();
        c = DriverManager.getConnection("jdbc:protomatter:pool:testPool");
        time = System.currentTimeMillis() - time;
        totalWaitTime += time;
        Syslog.info(this, id + ": Waited " + time + "ms for a connection");
        s = c.createStatement();
        r = s.executeQuery(sql);
        if (!r.next())
        {
          Syslog.info(this, id + ": ResultSet returned < 1 row");
        }
        else
        {
          Syslog.info(this, id + ": ResultSet returned >= 1 row");
        }
      }
      catch (Exception x)
      {
        Syslog.log(this, id + ": " + x, x, Syslog.ERROR);
      }
      finally
      {
        if (r != null)
        {
          try
          {
            r.close();
          }
          catch (Exception x)
          {
            Syslog.log(this, id + ": " + x, x, Syslog.ERROR);
          }
        }
        if (s != null)
        {
          try
          {
            s.close();
          }
          catch (Exception x)
          {
            Syslog.log(this, id + ": " + x, x, Syslog.ERROR);
          }
        }
        if (!useMaidThread)
        {
          if (c != null)
          {
            try
            {
              c.close();
            }
            catch (Exception x)
            {
              Syslog.log(this, id + ": " + x, x, Syslog.ERROR);
            }
          }
        }
      }
      try
      {
        Thread.sleep(getSleepTime());
      }
      catch (InterruptedException x)
      {
        Syslog.log(this, id + ": " + x, x, Syslog.ERROR);
      }

      ++runs;
      if (runs == numRuns)
      {
        done = true;
        float averageWaitTime = ((float)totalWaitTime/(float)runs);
        synchronized(Syslog.class)
        {
          Syslog.info(this, id + ": Run results:");
          Syslog.info(this, id + ":   total runs         = " + runs);
          Syslog.info(this, id + ":   total wait time    = " + totalWaitTime + "ms");
          Syslog.info(this, id + ":   average wait time  = " + averageWaitTime + "ms");
        }
      }
    }
  }
}

/**
 *  A slave thread for the PoolTest application.
 */
class PoolMonitorThread
extends Thread
{
  private JdbcConnectionPool pool = null;
  public PoolMonitorThread(JdbcConnectionPool pool)
  {
    super();
    this.setDaemon(true);
    this.pool = pool;
  }

  public void run()
  {
    while (true)
    {
      try
      {
        sleep(1000);
      }
      catch (InterruptedException x)
      {
        ; // ignore
      }
      Syslog.info(this, "############  Connection pool size: inUse=" + pool.getObjectsInUse() + " max=" + pool.getMaxObjectPoolSize() + " avail=" + pool.getObjectPoolSize());
    }
  }
}
