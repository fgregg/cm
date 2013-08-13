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
import java.text.MessageFormat;
import com.protomatter.util.*;
import com.protomatter.pool.*;
import com.protomatter.syslog.*;

/**
 *  A java.sql.Connection that's part of a pool of conections.  When this
 *  connection is closed, it is checked back into the pool, and it's options
 *  like transaction isolation level, auto-commit, type map, etc are all reset to
 *  defaults.
 *
 *  @see java.sql.Connection
 *  @see JdbcConnectionPoolDriver
 *  @see JdbcConnectionPool
 */
public class JdbcConnectionPoolConnection
implements ObjectPoolObject, Connection, SyslogChannelAware
{
  /**
   *  The pool that this connection is associated with.
   */
  protected JdbcConnectionPool pool = null;

  /**
   *  The actual connection to the database.
   */
  protected Connection connection = null;

  private boolean isDeleted = false;
  private boolean isClosed = false;
  private boolean isValid = true;

  private String url = null;
  private Properties props = null;

  private String originalCatalog = null;
  private Map originalTypeMap = null;
  private int originalTransactionIsolation = -1;
  private boolean originalAutoCommit = false;
  private boolean originalReadOnly = false;
  private boolean canCheckAutoCommit = true;

  // when was this thing last used?
  private long lastTimeUsed = 0;
  private JdbcCheckoutExceptionTrace checkoutStackException = null;

  // did they set any of these?
  private boolean setCatalog;
  private boolean setTransactionIsolation;
  private boolean setAutoCommit;
  private boolean setReadOnly;
  private boolean setTypeMap;

  JdbcConnectionPoolConnection(JdbcConnectionPool pool, String url, Properties props)
  throws SQLException
  {
    this.isDeleted = false;
    this.isClosed = false;
    this.pool = pool;
    this.url = url;
    this.props = props;
    init(false);
  }

  /**
   *  Get the pool that this connection is associated with.
   */
  public JdbcConnectionPool getConnectionPool()
  {
    return this.pool;
  }

  /**
   *  Get the connection that this object wraps.  This method
   *  should only be called if you <i>really</i> know what
   *  you're doing.  This method exposes the connection
   *  that is actually connected to the database, but you should
   *  not really have any need to get ahold of it.  This method
   *  will return <tt>null</tt> if you have called the
   *  <tt>close()</tt> method already.
   */
  public Connection getConnection()
  {
    return this.connection;
  }

  /**
   *  Returns the channel information from the pool
   *  this connection is associated with.
   *
   *  @see com.protomatter.syslog.SyslogChannelAware
   */
  public Object getSyslogChannel()
  {
    return pool.getSyslogChannel();
  }

  /**
   *  Get the last time that someone used this connection.
   */
  long getLastTimeUsed()
  {
    return this.lastTimeUsed;
  }

  /**
   *  Reset the last time that someone used this connection.
   */
  void resetLastTimeUsed()
  {
    this.lastTimeUsed = System.currentTimeMillis();
  }

  /**
   *  Set the stack trace of when this connection was checked out.
   */
  void setCheckoutStackTrace(JdbcCheckoutExceptionTrace x)
  {
    this.checkoutStackException = x;
  }

  /**
   *  Get the stack trace of when this connection was checked out.
   */
  JdbcCheckoutExceptionTrace getCheckoutStackTrace()
  {
    return this.checkoutStackException;
  }

  private void init(boolean verbose)
  throws SQLException
  {
    try
    {
      if (verbose)
      {
        if (pool.useSyslog())
        {
          Syslog.info(this, MessageFormat.format(
            PoolResources.getResourceString(MessageConstants.GETTING_CONNECTION_MESSAGE),
            new Object[] { pool.getName(), url }));
        }
        else
        {
          PrintWriter writer = DriverManager.getLogWriter();
          if (writer != null)
          {
            writer.println("JdbcConnectionPool: " +
              MessageFormat.format(PoolResources.getResourceString(MessageConstants.GETTING_CONNECTION_MESSAGE),
              new Object[] { pool.getName(), url }));
          }
        }
      }

      // ask the driver directly for a connection.  Don't bother
      // with the DriverManager.
      this.connection = pool.getDriver().connect(url, props);

      try
      {
        originalCatalog = this.connection.getCatalog();
      }
      catch (SQLException cx)
      {
        // don't care... some drivers (Sybase) will throw a SQLException
        // if certain metdata tables aren't installed.
        originalCatalog = null;
      }

      try
      {
        originalTypeMap = this.connection.getTypeMap();
      }
      catch (AbstractMethodError error) // Driver is not JDBC 2.0 compliant
      {
        originalTypeMap = null;
      }
      catch (Exception xx) // Driver is not JDBC 2.0 compliant
      {
        originalTypeMap = null;
      }

      // Some drivers freak out if you even *ask* them about
      // this stuff.  Informix?  I'm looking at you...
      try
      {
        originalTransactionIsolation = this.connection.getTransactionIsolation();
      }
      catch (SQLException x)
      {
        if (verbose)
        {
          if (pool.useSyslog())
          {
            Syslog.info(this, MessageFormat.format(PoolResources.getResourceString(MessageConstants.CANNOT_ASK_ISOLATION_MESSAGE),
              new Object[] { pool.getName(), x.toString() } ));
          }
          else
          {
            PrintWriter writer = DriverManager.getLogWriter();
            if (writer != null)
            {
              writer.println("JdbcConnectionPool: " +
                MessageFormat.format(PoolResources.getResourceString(MessageConstants.CANNOT_ASK_ISOLATION_MESSAGE),
                new Object[] { pool.getName(), x.toString() } ));
            }
          }
        }
      }

      try
      {
        originalAutoCommit = this.connection.getAutoCommit();
        this.canCheckAutoCommit = true;
      }
      catch (SQLException x)
      {
        this.canCheckAutoCommit = false;
        if (verbose)
        {
          if (pool.useSyslog())
          {
            Syslog.info(this, MessageFormat.format(
              PoolResources.getResourceString(MessageConstants.CANNOT_ASK_AUTOCOMMIT_MESSAGE),
              new Object[] { pool.getName(), x.toString() } ));
          }
          else
          {
            PrintWriter writer = DriverManager.getLogWriter();
            if (writer != null)
            {
              writer.println("JdbcConnectionPool: " +
                MessageFormat.format(
                PoolResources.getResourceString(MessageConstants.CANNOT_ASK_AUTOCOMMIT_MESSAGE),
                new Object[] { pool.getName(), x.toString() } ));
            }
          }
        }
      }

      try
      {
        originalReadOnly = this.connection.isReadOnly();
      }
      catch (SQLException x)
      {
        if (verbose)
        {
          if (pool.useSyslog())
          {
            Syslog.info(this, MessageFormat.format(
              PoolResources.getResourceString(MessageConstants.CANNOT_ASK_READONLY_MESSAGE),
              new Object[] { pool.getName(), x.toString() } ));
          }
          else
          {
            PrintWriter writer = DriverManager.getLogWriter();
            if (writer != null)
            {
              writer.println("JdbcConnectionPool: " +
                MessageFormat.format(PoolResources.getResourceString(
                MessageConstants.CANNOT_ASK_READONLY_MESSAGE),
                new Object[] { pool.getName(), x.toString() } ));
            }
          }
        }
      }

      this.isValid = true;
    }
    catch (SQLException x)
    {
      if (verbose)
      {
        if (pool.useSyslog())
        {
          Syslog.info(this, MessageFormat.format(
            PoolResources.getResourceString(MessageConstants.EXCEPTION_RECREATING_CONNECTION_MESSAGE),
            new Object[] { pool.getName() } ), x);
        }
        else
        {
          PrintWriter pw = DriverManager.getLogWriter();
          if (pw != null)
          {
            pw.println("JdbcConnectionPool: " +
              MessageFormat.format(
              PoolResources.getResourceString(MessageConstants.EXCEPTION_RECREATING_CONNECTION_MESSAGE),
              new Object[] { pool.getName() } ));
            x.printStackTrace(pw);
          }
        }
      }
      this.isValid = false;
      throw x;
    }
  }

  /**
   *  Used by the connection pool.
   *
   *  @see com.protomatter.pool.ObjectPoolObject
   */
  public void deleteObjectPoolObject()
  {
    this.isDeleted = true;
    this.isValid = false;
    try
    {
      this.connection.close();
    }
    catch (SQLException x)
    {
      // we don't actuall care what happens here...
      ;
    }
  }

  void reallyClose()
  {
    // this is called when we're going to close down everything anyway.
    try
    {
      this.connection.close();
      this.isValid = false;
      this.isClosed = true;
    }
    catch (SQLException x)
    {
      ;
    }
  }

  /**
   *  Will check that this connection is working and refresh it if not.
   *  This executes the validity check statement that was set when
   *  the connection pool was created.  If that statement was not set,
   *  the connection is refreshed no matter what.  If the statement was
   *  set, it is executed and if an exception is generated, the connection
   *  is refreshed.  If there's a problem refreshing the connection, this
   *  connection wrapper is invalidated -- you will have to either keep
   *  calling refresh() until it doesn't throw a SQLException, or call
   *  close() and open another connection using the JdbcConnectionPoolDriver.
   *  If <tt>verbose</TT> is true, messages are written to Syslog
   *  during the refresh operation.
   *
   *  @exception SQLException If the connection needs refreshing and there is
   *                          a problem re-opening the connection.
   */
  public synchronized void refresh(boolean verbose)
  throws SQLException
  {
    if (verbose)
    {
      if (pool.useSyslog())
      {
        Syslog.info(this,
          MessageFormat.format(PoolResources.getResourceString(MessageConstants.REFRESHING_CONNECTION_MESSAGE),
          new Object[] { pool.getName() }));
      }
      else
      {
        PrintWriter writer = DriverManager.getLogWriter();
        if (writer != null)
        {
          writer.println("JdbcConnectionPool: " +
            MessageFormat.format(PoolResources.getResourceString(MessageConstants.REFRESHING_CONNECTION_MESSAGE),
            new Object[] { pool.getName() }));
        }
      }
    }

    boolean needsRefresh = !validate(verbose);

    // try and re-nitialize the connection.  If there's an exception
    // opening it, init() sets the isValid flag to false.
    if (needsRefresh)
    {
      if (verbose)
      {
        if (pool.useSyslog())
        {
          Syslog.info(this,
            MessageFormat.format(PoolResources.getResourceString(MessageConstants.CLOSING_CONNECTION_MESSAGE),
            new Object[] { pool.getName() }));
        }
        else
        {
          PrintWriter writer = DriverManager.getLogWriter();
          if (writer != null)
          {
            writer.println("JdbcConnectionPool: " +
              MessageFormat.format(PoolResources.getResourceString(MessageConstants.CLOSING_CONNECTION_MESSAGE),
              new Object[] { pool.getName() }));
          }
        }
      }

      try { this.connection.close(); } catch (SQLException x) { ; }

      init(verbose);

      // now that we have a new connection, validate it again.
      if (!validate(verbose))
      {
        if (verbose)
        {
          if (pool.useSyslog())
          {
            Syslog.info(this,
              MessageFormat.format(PoolResources.getResourceString(MessageConstants.STILL_NOT_OK_MESSAGE),
              new Object[] { pool.getName() }));
          }
          else
          {
            PrintWriter writer = DriverManager.getLogWriter();
            if (writer != null)
            {
              writer.println("JdbcConnectionPool: " +
                MessageFormat.format(PoolResources.getResourceString(MessageConstants.STILL_NOT_OK_MESSAGE),
                new Object[] { pool.getName() }));
            }
          }
        }
        throw new SQLException(
          MessageFormat.format(PoolResources.getResourceString(MessageConstants.CONNECTION_INVALID),
          new Object[] { pool.getName() }));
      }
    }
    else
    {
      if (verbose)
      {
        if (pool.useSyslog())
        {
          Syslog.info(this,
            MessageFormat.format(PoolResources.getResourceString(MessageConstants.CONNECTION_OK),
            new Object[] { pool.getName() }));
        }
        else
        {
          PrintWriter writer = DriverManager.getLogWriter();
          if (writer != null)
          {
            writer.println("JdbcConnectionPool: " +
              MessageFormat.format(PoolResources.getResourceString(MessageConstants.CONNECTION_OK),
              new Object[] { pool.getName() }));
          }
        }
      }
    }
  }

  /**
   *  Performs a non-verbose refresh.
   *
   *  @see #refresh(boolean)
   */
  public synchronized void refresh()
  throws SQLException
  {
    refresh(false);
  }

  /**
   *  Validate the connection.
   *
   *  @return true If the connection is working properly.
   *  @return false If the connection needs to be refreshed.
   */
  private boolean validate(boolean verbose)
  throws SQLException
  {
    String statement = pool.getValidityCheckStatement();
    if (statement != null)
    {
      Statement s = null;
      ResultSet r = null;
      try
      {
        s = createStatement();
        r = s.executeQuery(statement);
        if (!r.next()) // read just the first row.
          return false;
        return true;
      }
      catch (Exception x)
      {
        return false;
      }
      finally
      {
        if (r != null) try { r.close(); } catch (Exception x) { ; }
        if (s != null) try { s.close(); } catch (Exception x) { ; }
      }
    }
    // if there's no validity check statement, assume the worst.
    return false;
  }

  /**
   *  Used by the connection pool.
   *
   *  @see com.protomatter.pool.ObjectPoolObject
   */
  public boolean isObjectPoolObjectValid()
  {
    return isValid;
  }

  /**
   *  Invalidates this connection manually. When this connection
   *  is closed, the pool will discard it.
   */
  public void invalidate()
  {
    this.isValid = false;
  }

  /**
   *  Used by the connection pool.
   *
   *  @see com.protomatter.pool.ObjectPoolObject
   */
  public void beforeObjectPoolObjectCheckout()
  {
    if (pool.getValidateOnCheckout())
    {
      int numTries = pool.getMaxCheckoutRefreshAttempts();
      long sleep = (long)pool.getCheckoutRefreshWaitTime();

      for (int i=0; i<numTries || numTries < 0; i++)
      {
        try
        {
          refresh(pool.getVerboseValidate());
          return;
        }
        catch (SQLException x)
        {
          ; // safe to ignore here.
        }
        Thread.yield();
        if (sleep > 0)
        {
            // sleep a bit... don't care about interruptions.
            try { Thread.sleep(sleep); }
            catch (Exception x) { ; }
        }
      }
      // tried a few times to refresh... no luck
      invalidate();
    }
  }

  /**
   *  Used by the connection pool.
   *
   *  @see com.protomatter.pool.ObjectPoolObject
   */
  public void afterObjectPoolObjectCheckin()
  {
    // clean things up and get ready for being checked out again.
    this.isClosed = false;
    try
    {
      clearWarnings();

      if (setCatalog && (originalCatalog != null))
        setCatalog(originalCatalog);
      setCatalog = false;

      if (setTypeMap)
        setTypeMap(originalTypeMap);
      setTypeMap = false;

      if (setTransactionIsolation)
        setTransactionIsolation(originalTransactionIsolation);
      setTransactionIsolation = false;

      if (setAutoCommit)
        setAutoCommit(originalAutoCommit);
      setAutoCommit = false;

      if (setReadOnly)
        setReadOnly(originalReadOnly);
      setReadOnly = false;
    }
    catch (SQLException x)
    {
      if (pool.useSyslog())
      {
        Syslog.warning(this,
          MessageFormat.format(PoolResources.getResourceString(MessageConstants.CANNOT_RESET_CONNECTION),
          new Object[] { pool.getName() }), x);
      }
      else
      {
        PrintWriter pw = DriverManager.getLogWriter();
        pw.println("JdbcConnectionPool: " +
          MessageFormat.format(PoolResources.getResourceString(MessageConstants.CANNOT_RESET_CONNECTION),
          new Object[] { pool.getName() }));
        x.printStackTrace(pw);
      }

      this.isValid = false;
      try
      {
        this.connection.close();
      }
      catch (SQLException sx)
      {
    ; // don't really care since we're closing anyway.
      }
    }
  }

  private final void checkValid()
  throws SQLException
  {
    if (!this.isValid)
      throw new SQLException(PoolResources.getResourceString(MessageConstants.CONNECTION_INVALID_REFRESH));
    lastTimeUsed = System.currentTimeMillis();
  }

  /**
   *  See <tt>java.sql.Connection</tt>.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public Statement createStatement()
  throws SQLException
  {
    checkValid();
    return this.connection.createStatement();
  }

  /**
   *  See <tt>java.sql.Connection</tt>.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public PreparedStatement prepareStatement(String sql)
  throws SQLException
  {
    checkValid();
    return this.connection.prepareStatement(sql);
  }

  /**
   *  See <tt>java.sql.Connection</tt>.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public CallableStatement prepareCall(String sql)
  throws SQLException
  {
    checkValid();
    return this.connection.prepareCall(sql);
  }

  /**
   *  See <tt>java.sql.Connection</tt>.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public String nativeSQL(String sql)
  throws SQLException
  {
    checkValid();
    return this.connection.nativeSQL(sql);
  }

  /**
   *  See <tt>java.sql.Connection</tt>.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public void setAutoCommit(boolean autoCommit)
  throws SQLException
  {
    checkValid();
    this.connection.setAutoCommit(autoCommit);
    this.setAutoCommit = true;
  }

  /**
   *  See <tt>java.sql.Connection</tt>.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public boolean getAutoCommit()
  throws SQLException
  {
    checkValid();
    return this.connection.getAutoCommit();
  }

  /**
   *  See <tt>java.sql.Connection</tt>.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public void commit()
  throws SQLException
  {
    checkValid();
    this.connection.commit();
  }

  /**
   *  See <tt>java.sql.Connection</tt>.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public void rollback()
  throws SQLException
  {
    checkValid();
    this.connection.rollback();
  }

  /**
   *  Close the connection.  The underlying connection is checked
   *  back into the pool so it can be used by someone else.  If this
   *  method completes without throwing a
   *  <tt>SQLException</tt>, then calling any method on this class
   *  (except this <tt>close()</tt> method) will throw a
   *  <tt>SQLException</tt> stating that the connection is closed.
   *  Repeatedly calling this method has no effect and will not
   *  throw exceptions (which some JDBC drivers do).<P>
   *
   *  This method will call <tt>commit()</tt> on the connection
   *  if auto-commit is turned on, and will call <tt>rollback()</tt>
   *  otherwise.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public void close()
  throws SQLException
  {
    // check the this.connection back in... don't close it.
    if (isClosed())
      return;

    if (this.canCheckAutoCommit)
    {
      if (this.connection.getAutoCommit())
      {
        try
        {
          this.connection.commit();
        }
        catch (SQLException x)
        {
          // this is here since the mm.mysql driver freaks out if
          // you call commit and autocommit is true.  Bah!
          ; // ignore
        }
      }
      else
      {
  // if we are not doing autocommit, when they
  // close the connection we should rollback so
  // that the next time this connection is taken
  // from the pool it is completely fresh.
        try
  {
    this.connection.rollback();
  }
  catch (SQLException x)
  {
    ;
  }
      }
    }

    this.isClosed = true;
    try
    {
      pool.checkin(this);
    }
   catch (Exception x)
    {
      throw new PoolSQLException(MessageFormat.format(
        PoolResources.getResourceString(MessageConstants.CANNOT_CHECKIN_CONNECTION_MESSAGE),
        new Object[] { pool.getName() }), x);
    }
  }

  /**
   *  See <tt>java.sql.Connection</tt>.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public boolean isClosed()
  throws SQLException
  {
    // since we don't really close the this.connection, we need to
    // tell the if they think the closed it ;-)
    return this.isClosed;
  }

  /**
   *  See <tt>java.sql.Connection</tt>.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public DatabaseMetaData getMetaData()
  throws SQLException
  {
    checkValid();
    return this.connection.getMetaData();
  }

  /**
   *  See <tt>java.sql.Connection</tt>.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public void setReadOnly(boolean readOnly)
  throws SQLException
  {
    checkValid();
    this.connection.setReadOnly(readOnly);
    this.setReadOnly = true;
  }

  /**
   *  See <tt>java.sql.Connection</tt>.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public boolean isReadOnly()
  throws SQLException
  {
    checkValid();
    return this.connection.isReadOnly();
  }

  /**
   *  See <tt>java.sql.Connection</tt>.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public void setCatalog(String catalog)
  throws SQLException
  {
    checkValid();
    this.connection.setCatalog(catalog);
    this.setCatalog = true;
  }

  /**
   *  See <tt>java.sql.Connection</tt>.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public String getCatalog()
  throws SQLException
  {
    checkValid();
    return this.connection.getCatalog();
  }

  /**
   *  See <tt>java.sql.Connection</tt>.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public void setTransactionIsolation(int level)
  throws SQLException
  {
    checkValid();
    this.connection.setTransactionIsolation(level);
    this.setTransactionIsolation = true;
  }

  /**
   *  See <tt>java.sql.Connection</tt>.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public int getTransactionIsolation()
  throws SQLException
  {
    checkValid();
    return this.connection.getTransactionIsolation();
  }

  /**
   *  See <tt>java.sql.Connection</tt>.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public SQLWarning getWarnings()
  throws SQLException
  {
    checkValid();
    return this.connection.getWarnings();
  }

  /**
   *  See <tt>java.sql.Connection</tt>.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public void clearWarnings()
  throws SQLException
  {
    checkValid();
    this.connection.clearWarnings();
  }

  /**
   *  See <tt>java.sql.Connection</tt>.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public Statement createStatement(int resultSetType, int resultSetConcurrency)
  throws SQLException
  {
    checkValid();
    return this.connection.createStatement(resultSetType, resultSetConcurrency);
  }

  /**
   *  See <tt>java.sql.Connection</tt>.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public Map getTypeMap()
  throws SQLException
  {
    checkValid();
    return this.connection.getTypeMap();
  }

  /**
   *  See <tt>java.sql.Connection</tt>.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
  throws SQLException
  {
    checkValid();
    return this.connection.prepareCall(sql, resultSetType, resultSetConcurrency);
  }

  /**
   *  See <tt>java.sql.Connection</tt>.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
  throws SQLException
  {
    checkValid();
    return this.connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
  }

  /**
   *  See <tt>java.sql.Connection</tt>.
   *
   *  @see java.sql.Connection
   *
   *  @exception SQLException Because the underlying connection can throw one.
   */
  public void setTypeMap(Map typeMap)
  throws SQLException
  {
    checkValid();
    this.connection.setTypeMap(typeMap);
    this.setTypeMap = true;
  }

  public void setHoldability(int holdability) throws SQLException {
    checkValid();
    this.connection.setHoldability(holdability);
  }

  public int getHoldability() throws SQLException {
    checkValid();
    return this.getHoldability();
  }

  public Savepoint setSavepoint() throws SQLException {
    checkValid();
    return this.connection.setSavepoint();
  }

  public Savepoint setSavepoint(String name) throws SQLException {
    checkValid();
    return this.connection.setSavepoint(name);
  }

  public void rollback(Savepoint savepoint) throws SQLException {
    checkValid();
    this.connection.rollback(savepoint);
  }

  public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    checkValid();
    this.connection.releaseSavepoint(savepoint);
  }

  public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    checkValid();
    return this.connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    checkValid();
    return this.connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    checkValid();
    return this.connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
    checkValid();
    return this.connection.prepareStatement(sql, autoGeneratedKeys);
  }

  public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
    checkValid();
    return this.connection.prepareStatement(sql, columnIndexes);
  }

  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
    checkValid();
    return this.connection.prepareStatement(sql, columnNames);
  }

  public Clob createClob() throws SQLException {
    checkValid();
    return this.connection.createClob();
  }

  public Blob createBlob() throws SQLException {
    checkValid();
    return this.connection.createBlob();
  }

  public NClob createNClob() throws SQLException {
    checkValid();
    return this.connection.createNClob();
  }

  public SQLXML createSQLXML() throws SQLException {
    checkValid();
    return this.connection.createSQLXML();
  }

  public boolean isValid(int timeout) throws SQLException {
    checkValid();
    return this.connection.isValid(timeout);
  }

  public void setClientInfo(String name, String value) throws SQLClientInfoException {
    this.connection.setClientInfo(name,value);
  }

  public void setClientInfo(java.util.Properties properties) throws SQLClientInfoException{
    this.connection.setClientInfo(properties);
  }

  public String getClientInfo(String name) throws SQLException {
    checkValid();
    return this.connection.getClientInfo(name);
  }

  public java.util.Properties getClientInfo() throws SQLException {
    checkValid();
    return this.connection.getClientInfo();
  }

  public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
    checkValid();
    return this.connection.createArrayOf(typeName, elements);
  }

  public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
    checkValid();
    return this.connection.createStruct(typeName,attributes);
  }

  public void setSchema(String schema) throws SQLException {
    checkValid();
    this.connection.setSchema(schema);
  }

  public String getSchema() throws SQLException {
    checkValid();
    return this.connection.getSchema();
  }

  public void abort(java.util.concurrent.Executor executor) throws SQLException {
    checkValid();
    this.connection.abort(executor);
  }

  public void setNetworkTimeout(java.util.concurrent.Executor executor, int milliseconds) throws SQLException {
    checkValid();
    this.connection.setNetworkTimeout(executor,milliseconds);
  }

  public int getNetworkTimeout() throws SQLException {
    checkValid();
    return this.connection.getNetworkTimeout();
  }

  public Object unwrap(Class c) throws SQLException {
    throw new SQLException("not implemented");
  }

  public boolean isWrapperFor(java.lang.Class c) throws SQLException {
    return false;
  }

}

