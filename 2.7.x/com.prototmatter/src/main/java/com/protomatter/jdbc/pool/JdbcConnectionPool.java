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

import com.protomatter.util.*;
import com.protomatter.pool.*;
import com.protomatter.syslog.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.io.*;

/**
 *  Provides a pool of pre-opened JDBC connections.
 *  The configuration hashtable can contain the following:<P>
 *
 *  <ul><dl>
 *  <dt><tt>pool.initialSize</tt> (Integer)</dt>
 *  <dd>The initial pool size (default is 0).</dd><P>
 *
 *  <dt><tt>pool.maxSize</tt> (Integer)</dt>
 *  <dd>The max pool size (default is -1).  If the max
 *  pool size is -1, the pool grows infinitely.</dd><P>
 *
 *  <dt><tt>pool.growBlock</tt> (Integer)</dt>
 *  <dd>The grow size (default is 1).  When a new
 *  object is needed, this many are created.</dd><P>
 *
 *  <dt><tt>pool.createWaitTime</tt> (Integer)</dt>
 *  <dd>The time (in ms) to sleep between pool object creates
 *  (default is 0). This is useful for database connection
 *  pools where it's possible to overload the database by
 *  trying to make too many connections too quickly.</dd><P>
 *
 *  <dt><tt>jdbc.driver</tt> (String)</dt>
 *  <dd>The name of the JDBC driver class to use</dd><P>
 *
 *  <dt><tt>jdbc.URL</tt> (String)</dt>
 *  <dd>The URL to use for the underlying. JDBC connections.</dd><P>
 *
 *  <dt><tt>jdbc.properties</tt> (java.util.Properties)</dt>
 *  <dd>Properties for the connection.  Should include at
 *  least "user" and "password" -- see
 *  <tt>DriverManager.getConnection(String, Properties)</tt>
 *  for what the properties should include.  Each underlying
 *  JDBC driver has it's own properties that it pays attention
 *  to, so you may need to consult the driver's documentation.</dd><P>
 *
 *  <dt><tt>jdbc.validityCheckStatement</tt> (String)</dt>
 *  <dd>A SQL statement that is guaranteed to return at
 *  least 1 row.  For Oracle, this is "<tt>select 1
 *  from dual</tt>" and for Sybase it is "<tt>select 1</tt>".
 *  This statement is used as a means of checking that a
 *  connection is indeed working.</dd><P>
 *
 *  <dt><tt>pool.useSyslog</tt> (Boolean)</dt>
 *  <dd>When writing warnings, errors, etc, should the pool
 *  use Syslog, or the <tt>PrintWriter</TT> returned from
 *  <tt>DriverManager.getLogWriter()</tt>?  Default is
 *  <tt>true</tt>.
 *  </dd><P>
 *
 *  <dt><tt>pool.syslogChannelList</tt> (String)</dt>
 *  <dd>A comma-separated list of channel names to
 *  log messages to.  The symbolic names
 *  <TT>Syslog.DEFAULT_CHANNEL</TT> and <TT>Syslog.ALL_CHANNEL</TT>
 *  are understood.  This is ignored if
 *  <tt>pool.useSyslog</tt> is <tt>false</tt>.
 *  </dd><P>
 *
 *  <dt><tt>pool.validateOnCheckout</tt> (Boolean)</dt>
 *  <dd>Determine if we should only hand out validated connections
 *  or not.  If set to true, each connection is tested with the
 *  <TT>pool.validityCheckStatement</TT> prior to being returned.
 *  Default is false.  If this option is used, you <b>must</b> specify
 *  the <tt>jdbc.validityCheckStatement</tt> option also.
 *  </dd><P>
 *
 *  <dt><tt>pool.maxCheckoutRefreshAttempts</tt> (Integer)</dt>
 *  <dd>Determines the number of times to attempt to refresh
 *  a connection on checkout before giving up.  Default is 5,
 *  and setting it to -1 will cause the pool to keep trying
 *  forever.  This setting only comes into play if
 *  <tt>pool.validateOnCheckout</tt> is set to <tt>true</tt>.
 *  </dd><P>
 *
 *  <dt><tt>pool.checkoutRefreshWaitTime</tt> (Integer)</dt>
 *  <dd>The number of milliseconds to wait between attempts
 *  to refresh a connection when checking it out from the pool.
 *  The default is 500ms.  Setting this to 0 will cause no
 *  delay.  This setting only comes into play if
 *  <tt>pool.validateOnCheckout</tt> is set to <tt>true</tt>.
 *  </dd><P>
 *
 *  <dt><tt>pool.refreshThreadCheckInterval</tt> (Integer)</dt>
 *  <dd>If present and &gt; 0, this is the number of seconds for
 *  a low-priority thread to sleep between calls to refreshConnections()
 *  on this pool.  If this option is used, you <b>must</b> specify
 *  the <tt>jdbc.validityCheckStatement</tt> option also.
 *  </dd><P>
 *
 *  <dt><tt>pool.verboseRefresh</tt> (Boolean)</dt>
 *  <dd>If present and true, the refresh thread will write
 *  messages when it refreshes connections.
 *  </dd><P>
 *
 *  <dt><tt>pool.verboseValidate</tt> (Boolean)</dt>
 *  <dd>If present and true, operations while validating
 *  connections before checkout will be logged.
 *  </dd><P>
 *
 *  <dt><tt>pool.maxConnectionIdleTime</tt> (Integer)</dt>
 *  <dd>If this property is present, and the <tt>pool.maidThreadCheckInterval</tt>
 *  property is also present, then a thread will be created that
 *  looks for connections that have been idle for more than
 *  <tt>pool.maxConnectionIdleTime</tt> seconds.  When this thread
 *  finds them, it closed the connection and logs a warning
 *  with a stack trace of when the connection
 *  was checked out of the pool.  This is primarily here as a debugging
 *  aid for finding places where connections are not getting close, and
 *  <i><b>should not</b></i> be used in a production environment.
 *  </dd><P>
 *
 *  <dt><tt>pool.maidThreadCheckInterval</tt> (Integer)</dt>
 *  <dd>This is the number of seconds between attempts by the
 *  maid thread (if present) to find idle connections.
 *  </dd><P>
 *
 *  </dl></ul>
 *
 *  @see java.sql.DriverManager
 *  @see JdbcConnectionPoolDriver
 *  @see JdbcConnectionPoolConnection
 */
public class JdbcConnectionPool
extends GrowingObjectPool
implements SyslogChannelAware
{
  private String url = null;
  private String driverName = null;
  private Properties props = null;
  private String poolName = null;
  private String checkStatement = null;
  private boolean initialized = false;
  private boolean validateOnCheckout = false;
  private boolean verboseValidate = false;

  private int maxCheckoutRefreshAttempts = 5;
  private int checkoutRefreshWaitTime = 500;

  private RefreshThread refreshThread = null;
  private MaidThread maidThread = null;
  private int maxConnectionIdleTime = 0;
  private Driver driver = null;

  private Object syslogChannel = null;

  private boolean useSyslog = true;

  protected static Debug DEBUG = Debug.forPackage(JdbcConnectionPool.class);
  protected static Channel log = Channel.forPackage(JdbcConnectionPool.class);
  
  int getMaxCheckoutRefreshAttempts()
  {
    return this.maxCheckoutRefreshAttempts;
  }
  int getCheckoutRefreshWaitTime()
  {
    return this.checkoutRefreshWaitTime;
  }

  /**
   *  Create a new JDBC connection pool with the given
   *  name, and the given initialization parameters.
   *
   *  @param name The name of the pool.
   *  @param args Initialization arguments.
   *  @see #init
   *  @exception Exception If there is a problem initializing the pool.
   */
  public JdbcConnectionPool(String name, Map args)
  throws Exception
  {
    this(name);
    init(args);
  }

  /**
   *  Create a connection pool from properties.
   *  Looks for properties in the following form:<P>
   *
   *  <blockquote><pre>
   *  ##
   *  ## Pool initialization parameters.  These each
   *  ## correspond to parameters passed into the
   *  ## init(ht) method -- the values are each
   *  ## converted to the appropriate type.  The
   *  ## "jdbc.properties" option is not specified
   *  ## here.
   *  ##
   *  com.protomatter.jdbc.pool.<i>POOLNAME</i>=\
   *    jdbc.driver=<i>driverclass</i>,\
   *    jdbc.URL=<i>connection_URL</i>,\
   *    jdbc.validityCheckStatement=<i>driverclass</i>,\
   *    pool.maxCheckoutRefreshAttempts=<i>num_attempts</i>,\
   *    pool.checkoutRefreshWaitTime=<i>num_milliseconds</i>,\
   *    pool.refreshThreadCheckInterval=<i>num_seconds</i>,\
   *    pool.verboseRefresh=<i>true or false</i>,\
   *    pool.verboseValidate=<i>true or false</i>,\
   *    pool.initialSize=<i>initial_pool_size</i>,\
   *    pool.maxSize=<i>max_pool_size</i>,\
   *    pool.growBlock=<i>pool_grow_block_size</i>,\
   *    pool.createWaitTime=<i>pool_create_wait_time</i>,\
   *    pool.maidThreadCheckInterval=<i>num_seconds</i>,\
   *    pool.maxConnectionIdleTime=<i>num_seconds</i>,\
   *    pool.validateOnCheckout=<i>true or false</i>,\
   *    pool.syslogChannelList=<i>list-of-channel-names</i>
   *
   *  ##
   *  ## Connection properties for the underlying driver.
   *  ## These correspond to the properties that are
   *  ## placed in the configuraton hashtable with the
   *  ## key "jdbc.properties".  These will be passed as
   *  ## the second argument to DriverManager.getConnection().
   *  ## Usually it just contains a usename and a password, but
   *  ## many drivers allow you to specify other options here.
   *  ##
   *  com.protomatter.jdbc.pool.<i>POOLNAME</i>.jdbcProperties=\
   *    user=<i>username</i>,\
   *    password=<i>password</i>,\
   *    key=<i>val</i>
   *  </pre></blockquote><P>
   *
   *  Each value specified in the properties is converted to
   *  the correct type (for instance, the "<tt>pool.initialSize</tt>"
   *  property's value is converted to an Integer).  The
   *  "<tt><i>POOLNAME</i></tt>" that appears above must match
   *  the "<tt>name</tt>" parameter passed to this method -- this
   *  allows to the same properties object to be passed into
   *  this constructor multiple times (each with a different
   *  "<tt>name</tt>" parameter) to create multiple pools.
   *
   *  @see #init
   *  @param name The name of the pool.
   *  @param props Properties describing pools.
   */
  public JdbcConnectionPool(String name, Properties props)
  throws Exception
  {
    this(name);

    // read system properties that look like this:
    // com.protomatter.jdbc.pool.POOLNAME = key=value,key=value,...
    // com.protomatter.jdbc.pool.POOLNAME.jdbcProperties = key=value,key=value,...

    String prefix = "com.protomatter.jdbc.pool.";
    int prefixLen = prefix.length();
    String suffix = ".jdbcProperties";

    Enumeration e = props.keys();
    while (e.hasMoreElements())
    {
      String key = (String)e.nextElement();
      if (key.startsWith(prefix) && !key.endsWith(suffix))
      {
        String pName = key.substring(prefixLen);
        if (pName.equals(name))
        {
          String value = props.getProperty(key);
          Properties poolProps = getProperties(value);
          Properties jdbcProps = getProperties(props.getProperty(prefix + pName + suffix));
          createPoolFromProps(poolProps, jdbcProps);
        }
      }
    }
  }

  /**
   *  Get the channel(s) to log messages to.
   *
   *  @see SyslogChannelAware
   */
  public Object getSyslogChannel()
  {
    return syslogChannel;
  }

  /**
   *  Set the channel that messages will be logged to.
   *
   *  @see SyslogChannelAware
   */
  public void setSyslogChannel(String channelName)
  {
    this.syslogChannel = channelName;
  }

  /**
   *  Set the list of channels that messages will be logged to.
   *
   *  @see SyslogChannelAware
   */
  public void setSyslogChannelList(List channelList)
  {
    this.syslogChannel = (Object)channelList.toArray();
  }

  /**
   *  Use Syslog to log messages?
   */
  boolean useSyslog()
  {
    return this.useSyslog;
  }

  /**
   *  Create pools from a properties object.  This method looks for
   *  all the properties matching the constraints in the
   *  {@link #JdbcConnectionPool(String, Properties) JdbcConnectionPool(String, Properties) }
   *  constructor, and creates a connection pool for each.  The
   *  resulting pools are placed in a List and returned.
   *
   *  @see #JdbcConnectionPool(String, Properties)
   */
  public static List createPools(Properties props)
  throws Exception
  {
    String prefix = "com.protomatter.jdbc.pool.";
    int prefixLen = prefix.length();
    String suffix = ".jdbcProperties";

    List list = new ArrayList();
    Enumeration e = props.keys();
    while (e.hasMoreElements())
    {
      String key = (String)e.nextElement();
      if (key.startsWith(prefix) && !key.endsWith(suffix))
      {
        String pName = key.substring(prefixLen);
        list.add(new JdbcConnectionPool(pName, props));
      }
    }
    return list;
  }

  private Properties getProperties(String s)
  {
    String tok;
    StringTokenizer st;
    Properties props = new Properties();
    int i;

    if (s == null)
        return props;

    st = new StringTokenizer(s, ",");

    while (st.hasMoreElements()) {
        tok= st.nextToken();

        if (-1 == (i= tok.indexOf('=')))
            throw new IllegalArgumentException ("Invalid property: " + tok);

        props.put(tok.substring(0,i), tok.substring(i+1));
    }

    return props;
  }

  private void createPoolFromProps(Properties props, Properties jdbcProps)
  throws Exception
  {
    // take a properties object that contains pool options and
    // change it into a hashtable with correct values and initialize
    // this pool with it.
    Map ht = new HashMap();

    if (props.get("pool.useSyslog") != null)
    {
      ht.put("pool.useSyslog", Boolean.valueOf(props.getProperty("pool.useSyslog")));
    }

    if (props.get("jdbc.driver") != null)
    {
      ht.put("jdbc.driver", props.getProperty("jdbc.driver"));
    }
    else
    {
      throw new PoolException(MessageFormat.format(
        PoolResources.getResourceString(MessageConstants.MUST_SPECIFY_PROP_MESSAGE),
        new Object[] { "jdbc.driver" }));
    }

    if (props.get("jdbc.URL") != null)
    {
      ht.put("jdbc.URL", props.getProperty("jdbc.URL"));
    }
    else
    {
      throw new PoolException(MessageFormat.format(
        PoolResources.getResourceString(MessageConstants.MUST_SPECIFY_PROP_MESSAGE),
        new Object[] { "jdbc.URL" }));
    }

    if (props.get("pool.maxCheckoutRefreshAttempts") != null)
    {
      try
      {
        ht.put("pool.maxCheckoutRefreshAttempts",
          new Integer(
            Integer.parseInt(
              props.getProperty("pool.maxCheckoutRefreshAttempts"))));
      }
      catch (NumberFormatException x)
      {
        throw new PoolException(MessageFormat.format(
          PoolResources.getResourceString(MessageConstants.MUST_SPECIFY_INT_PROP_MESSAGE),
          new Object[] { "pool.maxCheckoutRefreshAttempts" }));
      }
    }

    if (props.get("pool.checkoutRefreshWaitTime") != null)
    {
      try
      {
        ht.put("pool.checkoutRefreshWaitTime",
          new Integer(
            Integer.parseInt(
              props.getProperty("pool.checkoutRefreshWaitTime"))));
      }
      catch (NumberFormatException x)
      {
        throw new PoolException(MessageFormat.format(
          PoolResources.getResourceString(MessageConstants.MUST_SPECIFY_INT_PROP_MESSAGE),
          new Object[] { "pool.checkoutRefreshWaitTime" }));
      }
    }

    ht.put("jdbc.properties", jdbcProps);

    if (props.get("jdbc.validityCheckStatement") != null)
    {
      ht.put("jdbc.validityCheckStatement",
        props.getProperty("jdbc.validityCheckStatement"));
    }

    if (props.get("pool.syslogChannelList") != null)
    {
      ht.put("pool.syslogChannelList",
        props.getProperty("pool.syslogChannelList"));
    }

    String tmp = props.getProperty("pool.verboseValidate");
    boolean verboseValidate = false;
    if (tmp != null && tmp.equalsIgnoreCase("true"))
      verboseValidate = true;
    ht.put("pool.verboseValidate", new Boolean(verboseValidate));

    if (props.get("pool.refreshThreadCheckInterval") != null)
    {
      int i = Integer.parseInt(props.getProperty("pool.refreshThreadCheckInterval"));
      String verboseString = props.getProperty("pool.verboseRefresh");
      boolean verboseRefresh = false;
      if (verboseString != null && verboseString.equalsIgnoreCase("true"))
        verboseRefresh = true;
      ht.put("pool.verboseRefresh", new Boolean(verboseRefresh));
      if (i > 0)
      {
        if (props.get("jdbc.validityCheckStatement") == null)
        {
          throw new PoolException(MessageFormat.format(
            PoolResources.getResourceString(MessageConstants.MUST_SPECIFY_IF_PROP_MESSAGE),
            new Object[] { "jdbc.validityCheckStatement", "pool.refreshThreadCheckInterval" }));
        }
      }
      ht.put("pool.refreshThreadCheckInterval", new Integer(i));
    }

    if (props.get("pool.initialSize") != null)
    {
      ht.put("pool.initialSize",
        new Integer(props.getProperty("pool.initialSize")));
    }

    if (props.get("pool.maxSize") != null)
    {
      ht.put("pool.maxSize",
        new Integer(props.getProperty("pool.maxSize")));
    }

    if (props.get("pool.growBlock") != null)
    {
      ht.put("pool.growBlock",
        new Integer(props.getProperty("pool.growBlock")));
    }

    if (props.get("pool.createWaitTime") != null)
    {
      ht.put("pool.createWaitTime",
        new Integer(props.getProperty("pool.createWaitTime")));
    }

    if (props.get("pool.maidThreadCheckInterval") != null)
    {
      ht.put("pool.maidThreadCheckInterval",
        new Integer(props.getProperty("pool.maidThreadCheckInterval")));
    }

    if (props.get("pool.maxConnectionIdleTime") != null)
    {
      ht.put("pool.maxConnectionIdleTime",
        new Integer(props.getProperty("pool.maxConnectionIdleTime")));
    }

    if (props.get("pool.validateOnCheckout") != null)
    {
      ht.put("pool.validateOnCheckout",
        new Boolean(props.getProperty("pool.validateOnCheckout")));
    }

    // create the pool
    init(ht);
  }


  /**
   *  Create a new JDBC connection pool with the given name. You
   *  must call init(...) before using it.
   *
   *  @param name The name of the pool.
   *  @see #init
   */
  public JdbcConnectionPool(String name)
  {
    super();
    this.poolName = name;
    JdbcConnectionPoolDriver.registerPool(this);
  }

  /**
   *  Remove this pool from the JdbcConnectionPoolDriver's
   *  list of known pools.
   */
  public void unRegisterPool()
  {
    JdbcConnectionPoolDriver.unRegisterPool(this);
  }

  /**
   *  Get the name of this pool.
   */
  public String getName()
  {
    return this.poolName;
  }

  /**
   *  Shutdown all the connections that this pool has open
   *  and checked in.  This should only be done as part
   *  of a system shutdown of some kind.
   */
  public void closeAllConnections()
  {
    if (DEBUG.debug())
        log.debug(this, "Closing connections in pool " + poolName);
    List pool = getPool();
    synchronized (getSyncObject())
    {
      Iterator i = pool.iterator();
      while (i.hasNext())
      {
        ((JdbcConnectionPoolConnection)i.next()).reallyClose();
      }
    }
  }

  /**
   *  Refresh the connections.  This will call <tt>refresh()</tt> on all
   *  the connections that are currently checked-in.  If there are SQLExceptions
   *  thrown while refreshing connections, the last one is thrown back.  Any of
   *  the connections that fail the refresh are removed from the pool.
   *  If <tt>verbose</TT> is true, messages are written to Syslog
   *  about the refresh operation.
   *
   *  @see JdbcConnectionPoolConnection#refresh
   *  @exception SQLException If there is a problem refreshing connections.
   */
  public void refreshConnections(boolean verbose)
  throws SQLException
  {
    if (DEBUG.debug())
        log.debug(this, "Refreshing connections in pool " + poolName);
    SQLException x = null;
    synchronized (getSyncObject())
    {
      List pool = getPool();
      // put everything from the pool into another vector.
      List newPool = new ArrayList();
      Iterator i = pool.iterator();
      while (i.hasNext())
        newPool.add(i.next());

      // remove everything from the pool and add them back as we
      // refresh them.
      pool.clear();
      i = newPool.iterator();
      while (i.hasNext())
      {
        JdbcConnectionPoolConnection c = (JdbcConnectionPoolConnection)i.next();
        if (DEBUG.debug())
            log.debug(this, "Refreshing connection: " + c);
        boolean done = false;
        try
        {
          c.refresh(verbose);
          pool.add(c);
          done = true;
        }
        catch (SQLException sx)
        {
          if (DEBUG.debug())
            log.debug(this, "Caught SQLException refreshing connection: " + sx.toString());
          x = sx;
          c.deleteObjectPoolObject();
        }
      }
      getSyncObject().notifyAll();
    }
    if (x != null)
    {
      throw x;
    }
  }

  /**
   *  Performs a non-verbose refresh of the connections.
   *
   *  @see #refreshConnections(boolean)
   */
  public void refreshConnections()
  throws SQLException
  {
    refreshConnections(false);
  }

  /**
   *  Initialize the pool.
   *  Reads the following from the Map:<p>
   *  <dl>
   *
   *  <dt><tt>jdbc.driver</tt> (String)</dt>
   *  <dd>The name of the JDBC driver class to use</dd>
   *
   *  <dt><tt>jdbc.URL</tt> (String)</dt>
   *  <dd>The URL to use for the underlying. JDBC connections.</dd>
   *
   *  <dt><tt>jdbc.properties</tt> (java.util.Properties)</dt>
   *  <dd>Properties for the connection.  Should include at
   *  least "user" and "password" -- see
   *  <tt>DriverManager.getConnection(String, Properties)</tt>
   *  for what the properties should include.</dd>
   *
   *  <dt><tt>pool.syslogChannelList</tt> (String)</dt>
   *  <dd>A comma-separated list of channel names to
   *  log messages to.  The symbolic names
   *  <TT>Syslog.DEFAULT_CHANNEL</TT> and <TT>Syslog.ALL_CHANNEL</TT>
   *  are understood.
   *  </dd>
   *
   *  <dt><tt>jdbc.validityCheckStatement</tt> (String)</dt>
   *  <dd>A SQL statement that is guaranteed to return at
   *  least 1 row.  For Oracle, this is "<tt>select 1
   *  from dual</tt>" and for Sybase it is "<tt>select 1</tt>".
   *  This statement is used as a means of checking that a
   *  connection is indeed working.</dd>
   *
   *  <dt><tt>pool.refreshThreadCheckInterval</tt> (Integer)</dt>
   *  <dd>If present and &gt; 0, this is the number of seconds for
   *  a low-priority thread to sleep between calls to refreshConnections()
   *  on this pool.  If this option is used, you <b>must</b> specify
   *  the <tt>jdbc.validityCheckStatement</tt> option also.
   *  </dd>
   *
   *  <dt><tt>pool.verboseRefresh</tt> (Boolean)</dt>
   *  <dd>If present and true, the refresh thread will write
   *  messages using Syslog when it refreshes connections.
   *  </dd>
   *
   *  <dt><tt>pool.verboseValidate</tt> (Boolean)</dt>
   *  <dd>If present and true, operations while validating
   *  connections before checkout will be logged to Syslog.
   *  </dd>
   *
   *  <dt><tt>pool.maxConnectionIdleTime</tt> (Integer)</dt>
   *  <dd>If this property is present, and the <tt>pool.maidThreadCheckInterval</tt>
   *  property is also present, then a thread will be created that
   *  looks for connections that have been idle for more than
   *  <tt>pool.maxConnectionIdleTime</tt> seconds.  When this thread
   *  finds them, it closed the connection and logs a warning using
   *  the <tt>Syslog</tt> service with a stack trace of when the connection
   *  was checked out of the pool.  This is primarily here as a debugging
   *  aid for finding places where connections are not getting close, and
   *  <i><b>should not</b></i> be used in a production environment.
   *  </dd>
   *
   *  <dt><tt>pool.maidThreadCheckInterval</tt> (Integer)</dt>
   *  <dd>This is the number of seconds between attempts by the
   *  maid thread (if present) to find idle connections.
   *  </dd>
   *
   *  <dt><tt>pool.validateOnCheckout</tt> (Boolean)</dt>
   *  <dd>Determine if we should only hand out validated connections
   *  or not.  If set to true, each connection is tested with the
   *  <TT>pool.validityCheckStatement</TT> prior to being returned.
   *  Default is false.
   *  </dd>
   *
   *  <dt><tt>pool.checkoutRefreshWaitTime</tt> (Integer)</dt>
   *  <dd>The number of milliseconds to wait between attempts
   *  to refresh a connection when checking it out from the pool.
   *  The default is 500ms.  Setting this to 0 will cause no
   *  delay.  This setting only comes into play if
   *  <tt>pool.validateOnCheckout</tt> is set to <tt>true</tt>.
   *  </dd>
   *
   *  <dt><tt>pool.refreshThreadCheckInterval</tt> (Integer)</dt>
   *  <dd>If present and &gt; 0, this is the number of seconds for
   *  a low-priority thread to sleep between calls to refreshConnections()
   *  on this pool.  If this option is used, you <b>must</b> specify
   *  the <tt>jdbc.validityCheckStatement</tt> option also.
   *  </dd>
   *
   *  </dl><P>
   *
   *  The other options listed above are read by this class's superclass,
   *  <tt>{@link com.protomatter.pool.GrowingObjectPool com.protomatter.pool.GrowingObjectPool}</tt>.<P>
   *
   *  This method is called by the constructor that takes a String and
   *  a Map.  Calling this method multiple times will have no
   *  effect on the pool since all but the first call are ignored.<P>
   *
   *  @see com.protomatter.pool.GrowingObjectPool#init
   *  @exception Exception If there is a problem initializing the pool.
   */
  public void init(Map args)
  throws Exception
  {
    // don't initialize twice
    if (this.initialized == true)
      return;
    this.initialized = true;

    if (DEBUG.debug())
    {
        log.debug(this, "Initializing new JDBC connection pool:");
        Iterator it = args.keySet().iterator();
        while (it.hasNext())
        {
            Object key = it.next();
            log.debug(this, "  " + key + " = " + args.get(key));
        }
    }
    
    if (args.get("pool.useSyslog") != null)
      this.useSyslog = ((Boolean)args.get("pool.useSyslog")).booleanValue();

    // the max number of connections is a licensed property
    int maxSize = -1;
    if (args.get("pool.maxSize") != null)
      maxSize = ((Integer)args.get("pool.maxSize")).intValue();

    this.driverName = (String)args.get("jdbc.driver");
    try
    {
      // load the driver
      DatabaseUtil.registerDriver(driverName);
      driver = (Driver)Class.forName(driverName).newInstance();
    }
    catch (Exception x)
    {
      if (useSyslog)
        Syslog.log(this, x);
      throw new SQLException(MessageFormat.format(
        PoolResources.getResourceString(MessageConstants.CANNOT_LOAD_DRIVER_MESSAGE),
        new Object[] { driverName, x.toString() }));
    }
    this.url = (String)args.get("jdbc.URL");
    this.checkStatement = (String)args.get("jdbc.validityCheckStatement");
    this.props = (Properties)args.get("jdbc.properties");

    if (args.get("pool.maxCheckoutRefreshAttempts") != null)
    {
      this.maxCheckoutRefreshAttempts = ((Integer)args.get("pool.maxCheckoutRefreshAttempts")).intValue();
    }
    if (args.get("pool.checkoutRefreshWaitTime") != null)
    {
      this.checkoutRefreshWaitTime = ((Integer)args.get("pool.checkoutRefreshWaitTime")).intValue();
    }

    super.init(args);

    Boolean verboseValidate = (Boolean)args.get("pool.verboseValidate");
    if (verboseValidate != null)
      this.verboseValidate = verboseValidate.booleanValue();

    if (args.get("pool.refreshThreadCheckInterval") != null)
    {
      Boolean verboseFlag = (Boolean)args.get("pool.verboseRefresh");
      boolean verbose = false;
      if (verboseFlag != null)
        verbose = verboseFlag.booleanValue();

      if (this.checkStatement == null)
        throw new IllegalArgumentException(MessageFormat.format(
          PoolResources.getResourceString(MessageConstants.MUST_SPECIFY_IF_PROP_MESSAGE),
          new Object[] { "jdbc.validityCheckStatement", "pool.refreshThreadCheckInterval" }));
      int sleepTime = ((Integer)args.get("pool.refreshThreadCheckInterval")).intValue() * 1000;
      if (sleepTime > 0)
      {
        refreshThread = new RefreshThread(this, sleepTime, verbose);
        refreshThread.start();
      }
    }

    if (args.get("pool.maxConnectionIdleTime") != null)
    {
      maxConnectionIdleTime = ((Integer)args.get("pool.maxConnectionIdleTime")).intValue();
    }

    if (args.get("pool.syslogChannelList") != null)
    {
      String tmp = (String)args.get("pool.syslogChannelList");
      StringTokenizer st = new StringTokenizer(tmp, ", ");
      Vector list = new Vector();
      while (st.hasMoreTokens())
        list.add(st.nextToken());
      setSyslogChannelList(list);
    }

    if (args.get("pool.validateOnCheckout") != null)
    {
      validateOnCheckout = ((Boolean)args.get("pool.validateOnCheckout")).booleanValue();
    }

    if (args.get("pool.maidThreadCheckInterval") != null)
    {
      if (this.maxConnectionIdleTime == 0)
        throw new IllegalArgumentException(MessageFormat.format(
          PoolResources.getResourceString(MessageConstants.MUST_SPECIFY_LESS_THAN_IF_PROP_MESSAGE),
          new Object[] { "pool.maxConnectionIdleTime", "0", "pool.maidThreadCheckInterval" }));
      int sleepTime = ((Integer)args.get("pool.maidThreadCheckInterval")).intValue() * 1000;
      if (sleepTime > 0)
      {
        maidThread = new MaidThread(this, sleepTime);
        maidThread.start();
      }
    }
  }

  /**
   *  Look for idle connections that are checked out and have
   *  not been used in a while.
   */
  void performMaidCheck()
  {
    synchronized (getSyncObject())
    {
        try
        {
          if (DEBUG.debug())
          {
            log.debug(this, MessageFormat.format(
               PoolResources.getResourceString(MessageConstants.LOOKING_FOR_IDLE_CONNECTIONS_MESSAGE),
               new Object[] { getName() }));
          }
          else
          {
            PrintWriter writer = DriverManager.getLogWriter();
            if (writer != null)
            {
              writer.println("JdbcConnectionPool: " +
                MessageFormat.format(
                  PoolResources.getResourceString(MessageConstants.LOOKING_FOR_IDLE_CONNECTIONS_MESSAGE),
                  new Object[] { getName() }));
            }
          }

          List checkedOutObjects = getCheckedOutObjects();
          Iterator i = checkedOutObjects.iterator();
          while (i.hasNext())
          {
            JdbcConnectionPoolConnection c = (JdbcConnectionPoolConnection)i.next();
            long now = System.currentTimeMillis();
            long idleTime = (int)((now - c.getLastTimeUsed())/1000);
            if (idleTime > maxConnectionIdleTime)
            {
              // connection has been idle too long.
              if (useSyslog)
              {
                Syslog.warning(this, MessageFormat.format(
                  PoolResources.getResourceString(MessageConstants.CLOSING_IDLE_CONNECTION_MESSAGE),
                  new Object[] { String.valueOf(idleTime), c.toString() }));
                Syslog.warning(this, PoolResources.getResourceString(MessageConstants.CONNECTION_CHECKOUT_MESSAGE), c.getCheckoutStackTrace());
              }
              else
              {
                PrintWriter pw = DriverManager.getLogWriter();
                if (pw != null)
                {
                  pw.println("JdbcConnectionPool: " + MessageFormat.format(
                    PoolResources.getResourceString(MessageConstants.CLOSING_IDLE_CONNECTION_MESSAGE),
                    new Object[] { String.valueOf(idleTime), c.toString() }));
                  pw.println("JdbcConnectionPool: "
                    + PoolResources.getResourceString(MessageConstants.CONNECTION_CHECKOUT_MESSAGE));
                  c.getCheckoutStackTrace().printStackTrace(pw);
                }
              }

              try
              {
                c.close();
              }
              catch (Exception x)
              {
                if (useSyslog)
                {
                  Syslog.error(this, PoolResources.getResourceString(MessageConstants.EXCEPTION_CLOSE_CONNECTION_MESSAGE), x);
                }
                else
                {
                  PrintWriter pw = DriverManager.getLogWriter();
                  if (pw != null)
                  {
                    pw.println("JdbcConnectionPool: "
                      + PoolResources.getResourceString(MessageConstants.EXCEPTION_CLOSE_CONNECTION_MESSAGE));
                    x.printStackTrace(pw);
                  }
                }
              }
            }
          }
        }
        catch (Exception x)
        {
          if (useSyslog)
          {
            Syslog.error(this, MessageFormat.format(
               PoolResources.getResourceString(MessageConstants.MAID_EXCEPTION_MESSAGE),
               new Object[] { getName() }), x);
          }
          else
          {
            PrintWriter pw = DriverManager.getLogWriter();
            if (pw != null)
            {
              pw.println("JdbcConnectionPool: " + MessageFormat.format(
                 PoolResources.getResourceString(MessageConstants.MAID_EXCEPTION_MESSAGE),
                 new Object[] { getName() }));
              x.printStackTrace(pw);
            }
          }
        }
    }
  }

  /**
   *  Get the underlying JDBC driver instance.
   */
  Driver getDriver()
  {
    return this.driver;
  }

  /**
   *  Used internally for growing the pool.
   *
   *  @exception SQLException If there is a problem creating a new connection.
   */
  protected ObjectPoolObject createObjectPoolObject()
  throws SQLException
  {
    return new JdbcConnectionPoolConnection(this, url, props);
  }

  /**
   *  Get the statement set as the validity check statement.
   *
   *  @see #init
   */
  public String getValidityCheckStatement()
  {
    return this.checkStatement;
  }

  /**
   *  Determine if connections should be validated on checkout.
   */
  boolean getValidateOnCheckout()
  {
    return this.validateOnCheckout;
  }

  /**
   *  Determine if connection validation should be verbose.
   */
  boolean getVerboseValidate()
  {
    return this.verboseValidate;
  }

  /**
   *  Destroy this connection pool.
   */
  public void destroy()
  {
    unRegisterPool();

    if (refreshThread != null)
    {
      if (refreshThread.isAlive())
      {
        refreshThread.stopRunning();
        refreshThread = null;
      }
    }

    if (maidThread != null)
    {
      if (maidThread.isAlive())
      {
        maidThread.stopRunning();
        maidThread = null;
      }
    }

    closeAllConnections();
  }

  /**
   *  A thread that sits in the background and refreshes connections.
   */
  class RefreshThread
  extends Thread
  implements SyslogChannelAware
  {
    private JdbcConnectionPool pool = null;
    private int sleepTime;
    private boolean verbose = false;

    public Object getSyslogChannel()
    {
        if (pool != null)
            return pool.getSyslogChannel();
        return null;
    }

    public void stopRunning()
    {
        this.pool = null;
    }

    public RefreshThread(JdbcConnectionPool pool, int sleepTime, boolean verbose)
    {
      super("JdbcConnectionPoolRefreshThread[poolname=" + pool.getName() + "]");
      setPriority(Thread.MIN_PRIORITY);
      setDaemon(true); // VM should not wait for this thread to die.
      this.pool = pool;
      this.sleepTime = sleepTime;
      this.verbose = verbose;
    }

    public void run()
    {
      while (pool != null)
      {
        try
        {
          sleep(sleepTime);
        }
        catch (InterruptedException x)
        {
          ;
        }
        try
        {
          if (verbose && pool != null)
          {
            if (useSyslog)
            {
              Syslog.info(this, MessageFormat.format(
                 PoolResources.getResourceString(MessageConstants.REFRESHING_CONNECTIONS_MESSAGE),
                 new Object[] { pool.getName() }));
            }
            else
            {
              PrintWriter writer = DriverManager.getLogWriter();
              if (writer != null)
              {
                writer.println(MessageFormat.format(
                  PoolResources.getResourceString(MessageConstants.REFRESHING_CONNECTIONS_MESSAGE),
                  new Object[] { pool.getName() }));
              }
            }
          }
          if (pool != null)
            pool.refreshConnections(verbose);
        }
        catch (Exception x)
        {
          //Syslog.log(this, "Exception refreshing pool \"" + pool.getName() + "\"", x, Syslog.ERROR);
        }
      }
    }
  }

  /**
   *  Looks for connections that have been idle for a long time
   *  closes them.  Also logs a stack trace of where the connection
   *  was checked out.  This is primarily for debugging, and should
   *  probably not be used in production.
   */
  class MaidThread
  extends Thread
  implements SyslogChannelAware
  {
    private JdbcConnectionPool pool = null;
    private int sleepTime;

    public MaidThread(JdbcConnectionPool pool, int sleepTime)
    {
      super("JdbcConnectionPoolMaidThread[poolname=" + pool.getName() + "]");
      setPriority(Thread.MIN_PRIORITY);
      setDaemon(true); // VM should not wait for this thread to die.
      this.pool = pool;
      this.sleepTime = sleepTime;
    }

    public Object getSyslogChannel()
    {
        if (pool != null)
            return pool.getSyslogChannel();
        return null;
    }

    public void stopRunning()
    {
      this.pool = null;
    }

    public void run()
    {
      while (pool != null)
      {
        try
        {
          sleep(sleepTime);
        }
        catch (InterruptedException x)
        {
          ;
        }
        if (pool != null)
            pool.performMaidCheck();
      }
    }
  }
}
