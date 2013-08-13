package com.protomatter.jdbc.pool;

import java.io.*;
import java.sql.*;
import java.util.*;

import com.protomatter.syslog.Channel;

/**
 *  A wrapper for JDBC statements.
 *
 *  @see Statement
 */
class StatementWrapper
extends JDBCWrapper
implements Statement
{
    private ConnectionWrapper connection = null;
    private Statement statement = null;
    private boolean isClosed = false;
    private Channel log = Channel.forPackage(StatementWrapper.class);

    public StatementWrapper(ConnectionWrapper connection, Statement statement)
    {
        super();
        this.connection = connection;
        this.statement = statement;
    }

    public Statement getStatement()
    {
        if (this.statement == null)
            return null;
        return this.statement;
    }

    private final void checkClosed()
    throws SQLException
    {
        if (this.isClosed)
            throw new SQLException("Statement is already closed.");
    }
    
    public ResultSet executeQuery(String sql)
    throws SQLException
    {
        checkClosed();
        return (ResultSet)callMethod(statement, "executeQuery", new Class[] { String.class }, new Object[] { sql });
    }

    public int executeUpdate(String sql)
    throws SQLException
    {
        checkClosed();
        return callIntMethod(statement, "executeUpdate", new Class[] { String.class }, new Object[] { sql });
    }

    public void close()
    throws SQLException
    {
        if (this.isClosed)
            return;

        callMethod(statement, "close");

        this.isClosed = true;
        this.connection = null;
        this.statement = null;
    }

    public int getMaxFieldSize()
    throws SQLException
    {
        checkClosed();
        return callIntMethod(statement, "getMaxFieldSize", new Class[0], new Object[0]);
    }

    public void setMaxFieldSize(int size)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setMaxFieldSize", new Class[] { Integer.TYPE }, new Object[] { new Integer(size) });
    }

    public int getMaxRows()
    throws SQLException
    {
        checkClosed();
        return callIntMethod(statement, "getMaxRows", new Class[0], new Object[0]);
    }

    public void setMaxRows(int rows)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setMaxRows", new Class[] { Integer.TYPE }, new Object[] { new Integer(rows) });
    }

    public void setEscapeProcessing(boolean setting)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setEscapeProcessing", new Class[] { Boolean.class }, new Object[] { new Boolean(setting) });
    }

    public int getQueryTimeout()
    throws SQLException
    {
        checkClosed();
        return callIntMethod(statement, "getQueryTimeout", new Class[0], new Object[0]);
    }

    public void setQueryTimeout(int timeout)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setQueryTimeout", new Class[] { Integer.TYPE }, new Object[] { new Integer(timeout) });
    }

    public void cancel()
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "cancel");
    }

    public SQLWarning getWarnings()
    throws SQLException
    {
        checkClosed();
        return (SQLWarning)callMethod(statement, "getWarnings");
    }

    public void clearWarnings()
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "clearWarnings");
    }

    public void setCursorName(String name)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setCursorName", new Class[] { String.class }, new Object[] { name });
    }

    public boolean execute(String sql)
    throws SQLException
    {
        checkClosed();
        return callBooleanMethod(statement, "execute", new Class[] { String.class }, new Object[] { sql });
    }

    public ResultSet getResultSet()
    throws SQLException
    {
        checkClosed();
        return (ResultSet)callMethod(statement, "getResultSet");
    }

    public int getUpdateCount()
    throws SQLException
    {
        checkClosed();
        return callIntMethod(statement, "getUpdateCount", new Class[0], new Object[0]);
    }

    public boolean getMoreResults()
    throws SQLException
    {
        checkClosed();
        return callBooleanMethod(statement, "getMoreResults", new Class[0], new Object[0]);
    }

    public void setFetchDirection(int direction)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setFetchDirection", new Class[] { Integer.TYPE }, new Object[] { new Integer(direction) });
    }

    public int getFetchDirection()
    throws SQLException
    {
        checkClosed();
        return callIntMethod(statement, "getFetchDirection", new Class[0], new Object[0]);
    }

    public void setFetchSize(int size)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setFetchSize", new Class[] { Integer.TYPE }, new Object[] { new Integer(size) });
    }

    public int getFetchSize()
    throws SQLException
    {
        checkClosed();
        return callIntMethod(statement, "getFetchSize", new Class[0], new Object[0]);
    }

    public int getResultSetConcurrency()
    throws SQLException
    {
        checkClosed();
        return callIntMethod(statement, "getResultSetConcurrency", new Class[0], new Object[0]);
    }

    public int getResultSetType()
    throws SQLException
    {
        checkClosed();
        return callIntMethod(statement, "getResultSetType", new Class[0], new Object[0]);
    }

    public void addBatch(String sql)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "addBatch", new Class[] { String.class }, new Object[] { sql });
    }

    public void clearBatch()
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "clearBatch", new Class[0], new Object[0]);
    }

    public int[] executeBatch()
    throws SQLException
    {
        checkClosed();
        // yes, this does work... int[] is an Object.
        return (int[])callMethod(statement, "executeBatch", new Class[0], new Object[0]);
    }

    public Connection getConnection()
    throws SQLException
    {
        checkClosed();
        return (Connection)callMethod(statement, "getConnection", new Class[0], new Object[0]);
    }
}
