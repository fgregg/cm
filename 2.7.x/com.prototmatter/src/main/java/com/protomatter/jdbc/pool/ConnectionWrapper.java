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
 *  A wrapper.
 *
 *  @see java.sql.Connection
 *  @see JdbcConnectionPoolDriver
 *  @see JdbcConnectionPool
 */
class ConnectionWrapper
implements Connection, SyslogChannelAware
{
    private JdbcConnectionPool pool = null;
    private JdbcConnectionPoolConnection connection = null;
    private boolean isClosed = false;
    
    protected static Debug DEBUG = Debug.forPackage(ConnectionWrapper.class);
    protected static Channel log = Channel.forPackage(ConnectionWrapper.class);

    ConnectionWrapper(JdbcConnectionPoolConnection connection, JdbcConnectionPool pool)
    {
        super();
        this.connection = connection;
        this.pool = pool;
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
        if (this.connection == null)
            return null;
        return this.connection.getConnection();
    }

    /**
     *  Get the pool that this connection is associated with.
     */
    public JdbcConnectionPool getConnectionPool()
    {
        return this.pool;
    }

    /**
     *  Returns the channel information from the pool
     *  this connection is associated with.
     */
    public Object getSyslogChannel()
    {
        return this.pool.getSyslogChannel();
    }

    /**
     *  Invalidates this connection manually. When this connection
     *  is closed, the pool will discard it.
     */
    public void invalidate()
    {
        if (DEBUG.debug())
            log.debug(this, StackTraceUtil.whereAmI().getShortClassAndMethod());
        if (this.connection != null)
            this.connection.invalidate();
    }


    /**
     *  Will check that this connection is working and refresh it if not.
     *  This executes the validity check statement that was set when
     *  the connection pool was created.  If that statement was not set,
     *  the connection is refreshed no matter what.  If the statement was
     *  set, it is executed and if an exception is generated, the connection
     *  is refreshed.  If there's a problem refreshing the connection, this
     *  connection wrapper is invalidated -- you will have to either keep
     *  calling <tt>refresh()</tt> until it doesn't throw a <tt>SQLException</tt>, or just call
     *  <tt>close()</tt> and open another connection using the <tt>JdbcConnectionPoolDriver</tt>.
     *  If <tt>verbose</tt> is true, messages are written to Syslog
     *  during the refresh operation.  Note that after the <tt>close()</tt>
     *  method has been called, calling this method will throw a
     *  <tt>SQLException</tt> stating that the connection is closed.
     *
     *  @exception SQLException If the connection needs refreshing and there is
     *                          a problem re-opening the connection.
     */

    public synchronized void refresh(boolean verbose)
    throws SQLException
    {
        if (DEBUG.debug())
            log.debug(this, StackTraceUtil.whereAmI().getShortClassAndMethod());
        checkClosed();
        this.connection.refresh(verbose);
    }

    /**
     *  Performs a non-verbose refresh.  This method
     *  will throw a <tt>SQLException</tt> and
     *  the connection will not be refreshed if
     *  the <tt>close()</tt> method has been called.
     *
     *  @see #refresh(boolean)
     */
    public synchronized void refresh()
    throws SQLException
    {
        refresh(false);
    }

    private final void checkClosed()
    throws SQLException
    {
        if (this.isClosed)
            throw new SQLException(PoolResources.getResourceString(MessageConstants.CONNECTION_IS_CLOSED));
    }

    /**
     *  @see java.sql.Connection
     *
     *  @exception SQLException Because the underlying connection can throw one.
     */
    public Statement createStatement()
    throws SQLException
    {
        checkClosed();
        long time = System.currentTimeMillis();
        Statement stmt = this.connection.createStatement();
        time = System.currentTimeMillis() - time;
        if (DEBUG.debug())
        {
            log.debug(this, StackTraceUtil.whereAmI().getShortClassAndMethod() + " took " + time + "ms");
            return new StatementWrapper(this, stmt);
        }
        return stmt;
    }

    /**
     *  @see java.sql.Connection
     *
     *  @exception SQLException Because the underlying connection can throw one.
     */
    public PreparedStatement prepareStatement(String sql)
    throws SQLException
    {
        checkClosed();
        long time = System.currentTimeMillis();
        PreparedStatement stmt = this.connection.prepareStatement(sql);
        time = System.currentTimeMillis() - time;
        if (DEBUG.debug())
        {
            log.debug(this, StackTraceUtil.whereAmI().getShortClassAndMethod() + " SQL=\"" + sql + "\" took " + time + "ms");
            return new PreparedStatementWrapper(this, stmt);
        }
        return stmt;
    }

    /**
     *  @see java.sql.Connection
     *
     *  @exception SQLException Because the underlying connection can throw one.
     */
    public CallableStatement prepareCall(String sql)
    throws SQLException
    {
        checkClosed();
        long time = System.currentTimeMillis();
        CallableStatement stmt = this.connection.prepareCall(sql);
        time = System.currentTimeMillis() - time;
        if (DEBUG.debug())
        {
            log.debug(this, StackTraceUtil.whereAmI().getShortClassAndMethod() + " SQL=\"" + sql + "\" took " + time + "ms");
            return new CallableStatementWrapper(this, stmt);
        }
        return stmt;
    }

    /**
     *  @see java.sql.Connection
     *
     *  @exception SQLException Because the underlying connection can throw one.
     */
    public String nativeSQL(String sql)
    throws SQLException
    {
        checkClosed();
        long time = System.currentTimeMillis();
        String nsql = this.connection.nativeSQL(sql);
        time = System.currentTimeMillis() - time;
        if (DEBUG.debug())
            log.debug(this, StackTraceUtil.whereAmI().getShortClassAndMethod() + " SQL=\"" + sql + "\"  NativeSQL=\"" + nsql + "\" took " + time + "ms");
        return nsql;
    }

    /**
     *  @see java.sql.Connection
     *
     *  @exception SQLException Because the underlying connection can throw one.
     */
    public void setAutoCommit(boolean autoCommit)
    throws SQLException
    {
        if (DEBUG.debug())
            log.debug(this, StackTraceUtil.whereAmI().getShortClassAndMethod() + " AutoCommit=\"" + autoCommit + "\"");
        checkClosed();
        this.connection.setAutoCommit(autoCommit);
    }

    /**
     *  @see java.sql.Connection
     *
     *  @exception SQLException Because the underlying connection can throw one.
     */
    public boolean getAutoCommit()
    throws SQLException
    {
        checkClosed();
        return this.connection.getAutoCommit();
    }

    /**
     *  @see java.sql.Connection
     *
     *  @exception SQLException Because the underlying connection can throw one.
     */
    public void commit()
    throws SQLException
    {
        if (DEBUG.debug())
            log.debug(this, StackTraceUtil.whereAmI().getShortClassAndMethod());
        checkClosed();
        this.connection.commit();
    }

    /**
     *  @see java.sql.Connection
     *
     *  @exception SQLException Because the underlying connection can throw one.
     */
    public void rollback()
    throws SQLException
    {
        if (DEBUG.debug())
            log.debug(this, StackTraceUtil.whereAmI().getShortClassAndMethod());
        checkClosed();
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
     *  throw exceptions (which some JDBC drivers do).
     *
     *  @see java.sql.Connection
     *
     *  @exception SQLException Because the underlying connection can throw one.
     */
    public void close()
    throws SQLException
    {
        if (DEBUG.debug())
            log.debug(this, StackTraceUtil.whereAmI().getShortClassAndMethod());
        if (this.isClosed)
            return;

        this.connection.close();
        this.isClosed = true;
        this.connection = null;
    }

    /**
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
     *  @see java.sql.Connection
     *
     *  @exception SQLException Because the underlying connection can throw one.
     */
    public DatabaseMetaData getMetaData()
    throws SQLException
    {
        checkClosed();
        return this.connection.getMetaData();
    }

    /**
     *  @see java.sql.Connection
     *
     *  @exception SQLException Because the underlying connection can throw one.
     */
    public void setReadOnly(boolean readOnly)
    throws SQLException
    {
        if (DEBUG.debug())
            log.debug(this, StackTraceUtil.whereAmI().getShortClassAndMethod() + " ReadOnly=\"" + readOnly + "\"");
        checkClosed();
        this.connection.setReadOnly(readOnly);
    }

    /**
     *  @see java.sql.Connection
     *
     *  @exception SQLException Because the underlying connection can throw one.
     */
    public boolean isReadOnly()
    throws SQLException
    {
        checkClosed();
        return this.connection.isReadOnly();
    }

    /**
     *  @see java.sql.Connection
     *
     *  @exception SQLException Because the underlying connection can throw one.
     */
    public void setCatalog(String catalog)
    throws SQLException
    {
        if (DEBUG.debug())
            log.debug(this, StackTraceUtil.whereAmI().getShortClassAndMethod() + " Catalog=\"" + catalog + "\"");
        checkClosed();
        this.connection.setCatalog(catalog);
    }

    /**
     *  @see java.sql.Connection
     *
     *  @exception SQLException Because the underlying connection can throw one.
     */
    public String getCatalog()
    throws SQLException
    {
        checkClosed();
        return this.connection.getCatalog();
    }

    /**
     *  @see java.sql.Connection
     *
     *  @exception SQLException Because the underlying connection can throw one.
     */
    public void setTransactionIsolation(int level)
    throws SQLException
    {
        if (DEBUG.debug())
            log.debug(this, StackTraceUtil.whereAmI().getShortClassAndMethod() + " IsolationLevel=\"" + level + "\"");
        checkClosed();
        this.connection.setTransactionIsolation(level);
    }

    /**
     *  @see java.sql.Connection
     *
     *  @exception SQLException Because the underlying connection can throw one.
     */
    public int getTransactionIsolation()
    throws SQLException
    {
        checkClosed();
        return this.connection.getTransactionIsolation();
    }

    /**
     *  @see java.sql.Connection
     *
     *  @exception SQLException Because the underlying connection can throw one.
     */
    public SQLWarning getWarnings()
    throws SQLException
    {
        checkClosed();
        return this.connection.getWarnings();
    }

    /**
     *  @see java.sql.Connection
     *
     *  @exception SQLException Because the underlying connection can throw one.
     */
    public void clearWarnings()
    throws SQLException
    {
        if (DEBUG.debug())
            log.debug(this, StackTraceUtil.whereAmI().getShortClassAndMethod());
        checkClosed();
        this.connection.clearWarnings();
    }

    /**
     *  @see java.sql.Connection
     *
     *  @exception SQLException Because the underlying connection can throw one.
     */
    public Statement createStatement(int resultSetType, int resultSetConcurrency)
    throws SQLException
    {
        checkClosed();
        long time = System.currentTimeMillis();
        Statement stmt = this.connection.createStatement(resultSetType, resultSetConcurrency);
        time = System.currentTimeMillis() - time;
        if (DEBUG.debug())
        {
            log.debug(this, StackTraceUtil.whereAmI().getShortClassAndMethod() + " Type=\"" + resultSetType + "\" Concurrency=\"" + resultSetConcurrency + "\" took " + time + "ms");
            return new StatementWrapper(this, stmt);
        }
        return stmt;
    }

    /**
     *  @see java.sql.Connection
     *
     *  @exception SQLException Because the underlying connection can throw one.
     */
    public Map getTypeMap()
    throws SQLException
    {
        checkClosed();
        return this.connection.getTypeMap();
    }

    /**
     *  @see java.sql.Connection
     *
     *  @exception SQLException Because the underlying connection can throw one.
     */
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
    throws SQLException
    {
        checkClosed();

        long time = System.currentTimeMillis();
        CallableStatement stmt = this.connection.prepareCall(sql, resultSetType, resultSetConcurrency);
        time = System.currentTimeMillis() - time;
        if (DEBUG.debug())
        {
            log.debug(this, StackTraceUtil.whereAmI().getShortClassAndMethod() + " SQL=\"" + sql + "\" Type=\"" + resultSetType + "\" Concurrency=\"" + resultSetConcurrency + "\" took " + time + "ms");
            return new CallableStatementWrapper(this, stmt);
        }
        return stmt;
    }

    /**
     *  @see java.sql.Connection
     *
     *  @exception SQLException Because the underlying connection can throw one.
     */
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
    throws SQLException
    {
        checkClosed();

        long time = System.currentTimeMillis();
        PreparedStatement stmt = this.connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
        time = System.currentTimeMillis() - time;
        if (DEBUG.debug())
        {
            log.debug(this, StackTraceUtil.whereAmI().getShortClassAndMethod() + " SQL=\"" + sql + "\" Type=\"" + resultSetType + "\" Concurrency=\"" + resultSetConcurrency + "\" took " + time + "ms");
            return new PreparedStatementWrapper(this, stmt);
        }
        return stmt;
    }

    /**
     *  @see java.sql.Connection
     *
     *  @exception SQLException Because the underlying connection can throw one.
     */
    public void setTypeMap(Map typeMap)
    throws SQLException
    {
        if (DEBUG.debug())
            log.debug(this, StackTraceUtil.whereAmI().getShortClassAndMethod() + " TypeMap=\"" + typeMap + "\"");
        checkClosed();
        this.connection.setTypeMap(typeMap);
    }

  public void setHoldability(int holdability) throws SQLException {
    this.connection.setHoldability(holdability);
  }

  public int getHoldability() throws SQLException {
    return this.getHoldability();
  }

  public Savepoint setSavepoint() throws SQLException {
    this.connection.setSavepoint();
  }

  public Savepoint setSavepoint(String name) throws SQLException {
    this.connection.setSavepoint(name);
  }

  public void rollback(Savepoint savepoint) throws SQLException {
    this.connection.rollback(savepoint);
  }

  public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    this.connection.releaseSavepoint(savepoint);
  }

  public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    this.connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    this.connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    this.connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
    this.connection.prepareStatement(sql, autoGeneratedKeys);
  }

  public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
    this.connection.prepareStatement(sql, columnIndexes);
  }

  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
    this.connection.prepareStatement(sql, columnNames);
  }

  public Clob createClob() throws SQLException {
    return this.connection.createClob();
  }

  public Blob createBlob() throws SQLException {
    return this.connection.createBlob();
  }

  public NClob createNClob() throws SQLException {
    return this.connection.createNClob();
  }

  public SQLXML createSQLXML() throws SQLException {
    return this.connection.createSQLXML();
  }

  public boolean isValid(int timeout) throws SQLException {
    return this.connection.isValid(timeout);
  }

  public void setClientInfo(String name, String value) throws SQLClientInfoException{
    this.connection.setClientInfo(name,value);
  }

  public void setClientInfo(java.util.Properties properties) throws SQLClientInfoException{
    this.connection.setClientInfo(properties);
  }

  public String getClientInfo(String name) throws SQLException {
    return this.connection.getClientInfo(name);
  }

  public java.util.Properties getClientInfo() throws SQLException {
    return this.connection.getClientInfo();
  }

  public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
    return this.connection.createArrayOf(typeName, elements);
  }

  public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
    return this.connection.createStruct(typeName,attributes);
  }

  public void setSchema(String schema) throws SQLException {
    this.connection.setSchema(schema);
  }

  public String getSchema() throws SQLException {
    return this.connection.getSchema();
  }

  public void abort(java.util.concurrent.Executor executor) throws SQLException {
    this.connection.abort(executor);
  }

  public void setNetworkTimeout(java.util.concurrent.Executor executor, int milliseconds) throws SQLException {
    this.connection.setNetworkTimeout(executor,milliseconds);
  }

  public int getNetworkTimeout() throws SQLException {
    return this.connection.getNetworkTimeout();
  }

  public Object unwrap(Class c) throws SQLException {
    throw new SQLException("not implemented");
  }

  public boolean isWrapperFor(java.lang.Class c) throws SQLException {
    return false;
  }

}
