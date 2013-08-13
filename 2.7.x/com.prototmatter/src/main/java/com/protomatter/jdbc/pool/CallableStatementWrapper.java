package com.protomatter.jdbc.pool;

import java.io.*;
import java.sql.*;
import java.util.Map;
import java.util.Calendar;

import com.protomatter.syslog.Channel;

/**
 *  A wrapper for JDBC callable statements.
 *
 *  @see CallableStatement
 */
class CallableStatementWrapper
extends PreparedStatementWrapper
implements CallableStatement
{
    private ConnectionWrapper connection = null;
    private CallableStatement statement = null;
    private boolean isClosed = false;
    private Channel log = Channel.forPackage(CallableStatementWrapper.class);

    public CallableStatementWrapper(ConnectionWrapper connection, CallableStatement statement)
    {
        super(connection, statement);
        this.connection = connection;
        this.statement = statement;
    }

    public CallableStatement getCallableStatement()
    {
        if (this.statement == null)
            return null;
        return this.statement;
    }

    private final void checkClosed()
    throws SQLException
    {
        if (this.isClosed)
            throw new SQLException("CallableStatement is already closed.");
    }

    public void registerOutParameter(int index, int type)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "registerOutParameter",
                new Class[] { Integer.TYPE, Integer.TYPE }, new Object[] { new Integer(index), new Integer(type) });
    }

    public void registerOutParameter(int index, int type, int scale)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "registerOutParameter",
                new Class[] { Integer.TYPE, Integer.TYPE, Integer.TYPE },
                new Object[] { new Integer(index), new Integer(type), new Integer(scale) });
    }

    public boolean wasNull()
    throws SQLException
    {
        checkClosed();
        return callBooleanMethod(statement, "wasNull", new Class[0], new Object[0]);
    }

    public String getString(int index)
    throws SQLException
    {
        checkClosed();
        return callStringMethod(statement, "getString", new Class[] { Integer.TYPE }, new Object[] { new Integer(index) });
    }

    public boolean getBoolean(int index)
    throws SQLException
    {
        checkClosed();
        return callBooleanMethod(statement, "getBoolean", new Class[] { Integer.TYPE }, new Object[] { new Integer(index) });
    }

    public byte getByte(int index)
    throws SQLException
    {
        checkClosed();
        return callByteMethod(statement, "getByte", new Class[] { Integer.TYPE }, new Object[] { new Integer(index) });
    }

    public short getShort(int index)
    throws SQLException
    {
        checkClosed();
        return callShortMethod(statement, "getShort", new Class[] { Integer.TYPE }, new Object[] { new Integer(index) });
    }

    public int getInt(int index)
    throws SQLException
    {
        checkClosed();
        return callIntMethod(statement, "getInt", new Class[] { Integer.TYPE }, new Object[] { new Integer(index) });
    }

    public long getLong(int index)
    throws SQLException
    {
        checkClosed();
        return callLongMethod(statement, "getLong", new Class[] { Integer.TYPE }, new Object[] { new Integer(index) });
    }

    public float getFloat(int index)
    throws SQLException
    {
        checkClosed();
        return callFloatMethod(statement, "getFloat", new Class[] { Integer.TYPE }, new Object[] { new Integer(index) });
    }

    public double getDouble(int index)
    throws SQLException
    {
        checkClosed();
        return callDoubleMethod(statement, "getDouble", new Class[] { Integer.TYPE }, new Object[] { new Integer(index) });
    }

    public java.math.BigDecimal getBigDecimal(int index, int scale)
    throws SQLException
    {
        checkClosed();
        return (java.math.BigDecimal)callMethod(statement, "getBigDecimal",
                new Class[] { Integer.TYPE, Integer.TYPE },
                new Object[] { new Integer(index), new Integer(scale) });
    }

    public byte[] getBytes(int index)
    throws SQLException
    {
        checkClosed();
        return (byte[])callMethod(statement, "getBytes", new Class[] { Integer.TYPE }, new Object[] { new Integer(index) });
    }

    public Date getDate(int index)
    throws SQLException
    {
        checkClosed();
        return (Date)callMethod(statement, "getDate", new Class[] { Integer.TYPE }, new Object[] { new Integer(index) });
    }

    public Time getTime(int index)
    throws SQLException
    {
        checkClosed();
        return (Time)callMethod(statement, "getTime", new Class[] { Integer.TYPE }, new Object[] { new Integer(index) });
    }

    public Timestamp getTimestamp(int index)
    throws SQLException
    {
        checkClosed();
        return (Timestamp)callMethod(statement, "getTimestamp", new Class[] { Integer.TYPE }, new Object[] { new Integer(index) });
    }

    public Object getObject(int index)
    throws SQLException
    {
        checkClosed();
        return callMethod(statement, "getObject", new Class[] { Integer.TYPE }, new Object[] { new Integer(index) });
    }

    public java.math.BigDecimal getBigDecimal(int index)
    throws SQLException
    {
        checkClosed();
        return (java.math.BigDecimal)callMethod(statement, "getBigDecimal", new Class[] { Integer.TYPE }, new Object[] { new Integer(index) });
    }

    public Object getObject(int index, Map map)
    throws SQLException
    {
        checkClosed();
        return callMethod(statement, "getObject", new Class[] { Integer.TYPE, Map.class }, new Object[] { new Integer(index), map });
    }

    public Ref getRef(int index)
    throws SQLException
    {
        checkClosed();
        return (Ref)callMethod(statement, "getRef", new Class[] { Integer.TYPE }, new Object[] { new Integer(index) });
    }

    public Blob getBlob(int index)
    throws SQLException
    {
        checkClosed();
        return (Blob)callMethod(statement, "getBlob", new Class[] { Integer.TYPE }, new Object[] { new Integer(index) });
    }

    public Clob getClob(int index)
    throws SQLException
    {
        checkClosed();
        return (Clob)callMethod(statement, "getClob", new Class[] { Integer.TYPE }, new Object[] { new Integer(index) });
    }

    public Array getArray(int index)
    throws SQLException
    {
        checkClosed();
        return (Array)callMethod(statement, "getArray", new Class[] { Integer.TYPE }, new Object[] { new Integer(index) });
    }

    public Date getDate(int index, Calendar cal)
    throws SQLException
    {
        checkClosed();
        return (Date)callMethod(statement, "getDate", new Class[] { Integer.TYPE, Calendar.class },
                new Object[] { new Integer(index), cal });
    }

    public Time getTime(int index, Calendar cal)
    throws SQLException
    {
        checkClosed();
        return (Time)callMethod(statement, "getTime", new Class[] { Integer.TYPE, Calendar.class },
                new Object[] { new Integer(index), cal });
    }

    public Timestamp getTimestamp(int index, Calendar cal)
    throws SQLException
    {
        checkClosed();
        return (Timestamp)callMethod(statement, "getTimestamp", new Class[] { Integer.TYPE, Calendar.class },
                new Object[] { new Integer(index), cal });
    }

    public void registerOutParameter(int index, int type, String typeName)
    throws SQLException
    {
        checkClosed();
        this.statement.registerOutParameter(index, type, typeName);
        callMethod(statement, "registerOutParameter",
                new Class[] { Integer.TYPE, Integer.TYPE, String.class },
                new Object[] { new Integer(index), new Integer(type), typeName });
    }

  public Reader getCharacterStream(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public Reader getCharacterStream(String s) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public Reader getNCharacterStream(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public Reader getNCharacterStream(String s) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public Object getObject(String i, Map m) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public String getNString(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public String getNString(String s) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public NClob getNClob(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public NClob getNClob(String s) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public RowId getRowId(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public RowId getRowId(String s) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public SQLXML getSQLXML(int i) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public SQLXML getSQLXML(String s) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public Object getObject(int i, Class c) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public Object getObject(String s, Class c) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setAsciiStream(String s, InputStream in, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setAsciiStream(String s, InputStream in) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setBinaryStream(String s, InputStream in, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setBinaryStream(String s, InputStream in) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setBlob(String s, InputStream in, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setBlob(String s, InputStream in) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setBlob(String s, Blob b) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setCharacterStream(String s, Reader r, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setCharacterStream(String s, Reader r) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setClob(String s, Reader r, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setClob(String s, Reader r) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setClob(String s, Clob c) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setNCharacterStream(String s, Reader r, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setNCharacterStream(String s, Reader r) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setNClob(String s, Reader r, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setNClob(String s, Reader r) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setNClob(String s, NClob nc) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setNString(String s, String s2) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setRowId(String s, RowId rowId) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setSQLXML(String s, SQLXML sqlXml) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

}
