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
import com.protomatter.syslog.Syslog;
import java.util.*;
import java.io.*;
import java.sql.*;
import java.text.MessageFormat;
import javax.sql.*;

/**
 *  An implementation of the <tt>javax.sql.DataSource</tt> and
 *  <tt>javax.sql.ConnectionPoolDataSource</tt> interfaces.
 *
 *  @see javax.sql.DataSource
 *  @see javax.sql.ConnectionPoolDataSource
 *  @see JdbcConnectionPool
 *  @see JdbcConnectionPoolDriver
 *  @see JdbcConnectionPoolConnection
 */
public class JdbcConnectionPoolDataSource
implements DataSource, ConnectionPoolDataSource
{
  private String poolName = null;
  private PrintWriter logWriter = null;
  private int loginTimeout = 0;
  private JdbcConnectionPool pool = null;

  /**
   *  Create a new <tt>DataSource</tt> attached to the given JDBC
   *  connection pool.  The pool must already exist.
   *
   *  @see JdbcConnectionPool
   *  @throw SQLException If the given pool does not exist.
   */
  public JdbcConnectionPoolDataSource(String poolName)
  throws SQLException
  {
    pool = JdbcConnectionPoolDriver.getPool(poolName);
    if (pool == null)
      throw new SQLException(MessageFormat.format(
        PoolResources.getResourceString(MessageConstants.UNKNOWN_POOL_MESSAGE),
        new Object[] { poolName }));
    this.poolName = poolName;
  }

  /**
   *  Get a connection from this <tt>DataSource</tt>.  If the login timeout
   *  has been set to zero, this method will block indefinately.  If the timeout
   *  has been set to something other than zero, this method is guaranteed to
   *  return (or throw a <tt>SQLException</tt>) within the specified time period.
   *
   *  @see javax.sql.DataSource
   */
  public Connection getConnection()
  throws SQLException
  {
    Connection c = null;

    // limit the time we wait for a connection.
    if (loginTimeout > 0)
    {
      // use a background thread to get a connection so we can
      // implement this login timeout stuff.
      GetConnectionSlaveThread t = new GetConnectionSlaveThread(pool);
      t.start();

      // when should we stop?
      int timeout = loginTimeout;
      long end = System.currentTimeMillis();
      end += (long)((long)loginTimeout * (long)1000);

      // just in case we can get a connection immediately,
      // first sleep for as little time as we can.
      try { Thread.sleep(1); } catch (InterruptedException x) { ; }

      // now, wait 10 milliseconds between checks to see if
      // we could get a connection or not.
      while ((t.getConnection() == null) && (t.isAlive()) && (System.currentTimeMillis() < end))
      {
        try { Thread.sleep(10); } catch (InterruptedException x) { ; }
        Thread.yield(); // tight loops are bad.
      }

      // remove the thread from the list of waiters that
      // are in line to get a connection.
      pool.removeWaiter(t);

      // see if we were able to get a connection.
      c = t.getConnection();
      if (c == null) // thread hasn't got a connection yet
      {
        if (t.isAlive()) // still running?
          t.interrupt(); // not anymore

        if (t.getException() != null) // throw the exception that the thread ran into.
          throw t.getException();
        else // just give up.
          throw new SQLException(MessageFormat.format(
            PoolResources.getResourceString(MessageConstants.CANNOT_GET_CONNECTION_TIMEOUT_MESSAGE),
            new Object[] { String.valueOf(timeout) }));
      }
    }
    else
    {
      // don't limit the time we take to get a connection.
      c = DriverManager.getConnection(JdbcConnectionPoolDriver.URL_PREFIX + poolName);
    }

    // success... finally.
    return c;
  }

  /**
   *  Get a connection from this <tt>DataSource</tt>.  The <tt>user</tt>
   *  and <tt>password</tt> are ignored.
   *  If the login timeout
   *  has been set to zero, this method will block indefinately.  If the timeout
   *  has been set to something other than zero, this method is guaranteed to
   *  return (or throw a <tt>SQLException</tt>) within the specified time period.
   *
   *  @see javax.sql.DataSource
   */
  public Connection getConnection(String user, String password)
  throws SQLException
  {
    return getConnection();
  }

  /**
   *  Get the log writer.
   *
   *  @see javax.sql.DataSource
   */
  public PrintWriter getLogWriter()
  throws SQLException
  {
    return this.logWriter;
  }

  /**
   *  Set the log writer.
   *
   *  @see javax.sql.DataSource
   */
  public void setLogWriter(PrintWriter logWriter)
  throws SQLException
  {
    this.logWriter = logWriter;
  }

  /**
   *  Get the login timeout.  The value specified is in seconds.
   *
   *  @see javax.sql.DataSource
   */
  public int getLoginTimeout()
  throws SQLException
  {
    return this.loginTimeout;
  }

  /**
   *  Set the login timeout (in seconds).  If this value is set to
   *  something other than zero, any method that gets a connection
   *  is guaranteed to return (or throw a <tt>SQLException</tt>) within the
   *  specified amount of time.  If this value is set to zero,
   *  any method that gets a connection will block indefinately
   *  until a connection is available.  The default value is zero.
   *
   *  @see javax.sql.DataSource
   */
  public void setLoginTimeout(int loginTimeout)
  throws SQLException
  {
    this.loginTimeout = loginTimeout;
  }

  /**
   *  Get a <tt>PooledConnection</tt> from this <tt>ConnectionPoolDataSource</tt>.
   *  The <tt>user</tt> and <tt>password</tt> are currently ignored.
   *  If the login timeout
   *  has been set to zero, this method will block indefinately.  If the timeout
   *  has been set to something other than zero, this method is guaranteed to
   *  return (or throw a <tt>SQLException</tt>) within the specified time period.
   *
   *  @see javax.sql.ConnectionPoolDataSource
   */
  public PooledConnection getPooledConnection(String user, String password)
  throws SQLException
  {
    return getPooledConnection();
  }

  /**
   *  Get a <tt>PooledConnection</tt> from this <tt>ConnectionPoolDataSource</tt>.
   *  If the login timeout
   *  has been set to zero, this method will block indefinately.  If the timeout
   *  has been set to something other than zero, this method is guaranteed to
   *  return (or throw a <tt>SQLException</tt>) within the specified time period.
   *
   *  @see javax.sql.ConnectionPoolDataSource
   */
  public PooledConnection getPooledConnection()
  throws SQLException
  {
    return new JdbcConnectionPoolPooledConnection((JdbcConnectionPoolConnection)getConnection());
  }

  /**
   *  This is a support class for getting connections in the background.
   */
  private class GetConnectionSlaveThread
  extends Thread
  {
    private JdbcConnectionPool pool = null;
    private Connection connection = null;
    private SQLException exception = null;

    public GetConnectionSlaveThread(JdbcConnectionPool pool)
    {
      super();
      setDaemon(true);
      this.pool = pool;
    }

    public void run()
    {
      try
      {
        connection = DriverManager.getConnection(JdbcConnectionPoolDriver.URL_PREFIX + pool.getName());
      }
      catch (SQLException sqlx)
      {
        pool.removeWaiter(this);
        exception = sqlx;
        close();
      }
      catch (Exception x) // most likely we were interrupted.
      {
        pool.removeWaiter(this);
        close();
      }
    }

    private void close()
    {
      if (connection != null)
      {
        try
        {
          connection.close();
        }
        catch (Exception x)
        {
          if (pool.useSyslog())
          {
            Syslog.error(this, MessageFormat.format(
              PoolResources.getResourceString(MessageConstants.CANNOT_CLOSE_OPEN_CONNECTION_MESSAGE),
              new Object[] { pool.getName() }), x);
          }
          else
          {
            PrintWriter pw = DriverManager.getLogWriter();
            if (pw != null)
            {
              pw.println("JdbcConnectionPoolDataSource: " +
                MessageFormat.format(PoolResources.getResourceString(MessageConstants.CANNOT_CLOSE_OPEN_CONNECTION_MESSAGE),
                new Object[] { pool.getName() }));
              x.printStackTrace(pw);
            }
          }
        }
      }
    }

    public Connection getConnection()
    {
      return this.connection;
    }

    public SQLException getException()
    {
      return this.exception;
    }
  }

  public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
    throw new SQLFeatureNotSupportedException();
  }

  public Object unwrap(Class c) throws SQLException {
    throw new SQLException("not implemented");
  }

  public boolean isWrapperFor(java.lang.Class c) throws SQLException {
    return false;
  }

}
