package com.protomatter.jdbc.pool;

import java.io.*;
import java.sql.*;
import java.util.Calendar;

import com.protomatter.syslog.Channel;

/**
 *  A wrapper for JDBC prepared statements.
 *
 *  @see PreparedStatement
 */
class PreparedStatementWrapper
extends StatementWrapper
implements PreparedStatement
{
    private ConnectionWrapper connection = null;
    private PreparedStatement statement = null;
    private boolean isClosed = false;
    private Channel log = Channel.forPackage(PreparedStatementWrapper.class);

    public PreparedStatementWrapper(ConnectionWrapper connection, PreparedStatement statement)
    {
        super(connection, statement);
        this.connection = connection;
        this.statement = statement;
    }

    public PreparedStatement getPreparedStatement()
    {
        if (this.statement == null)
            return null;
        return this.statement;
    }

    private final void checkClosed()
    throws SQLException
    {
        if (this.isClosed)
            throw new SQLException("PreparedStatement is already closed.");
    }

    public ResultSet executeQuery()
    throws SQLException
    {
        checkClosed();
        return (ResultSet)callMethod(statement, "executeQuery");
    }

    public int executeUpdate()
    throws SQLException
    {
        checkClosed();
        return callIntMethod(statement, "executeUpdate", new Class[0], new Object[0]);
    }

    public void setNull(int index, int type)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setNull", new Class[] { Integer.TYPE, Integer.TYPE }, new Object[] { new Integer(index), new Integer(type) });
    }

    public void setBoolean(int index, boolean value)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setBoolean", new Class[] { Integer.TYPE, Boolean.TYPE }, new Object[] { new Integer(index), new Boolean(value) });
    }

    public void setByte(int index, byte value)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setByte", new Class[] { Integer.TYPE, Integer.TYPE }, new Object[] { new Integer(index), new Integer(value) });
    }

    public void setShort(int index, short value)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setShort", new Class[] { Integer.TYPE, Short.TYPE }, new Object[] { new Integer(index), new Short(value) });
    }

    public void setInt(int index, int value)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setInt", new Class[] { Integer.TYPE, Integer.TYPE }, new Object[] { new Integer(index), new Integer(value) });
    }

    public void setLong(int index, long value)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setLong", new Class[] { Integer.TYPE, Long.TYPE }, new Object[] { new Integer(index), new Long(value) });
    }

    public void setFloat(int index, float value)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setFloat", new Class[] { Integer.TYPE, Float.TYPE }, new Object[] { new Integer(index), new Float(value) });
    }

    public void setDouble(int index, double value)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setDouble", new Class[] { Integer.TYPE, Double.TYPE }, new Object[] { new Integer(index), new Double(value) });
    }

    public void setBigDecimal(int index, java.math.BigDecimal value)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setBigDecimal", new Class[] { Integer.TYPE, java.math.BigDecimal.class },
                new Object[] { new Integer(index), value });
    }

    public void setString(int index, String value)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setString", new Class[] { Integer.TYPE, String.class }, new Object[] { new Integer(index), value });
    }

    public void setBytes(int index, byte[] value)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setBytes", new Class[] { Integer.TYPE, byte[].class }, new Object[] { new Integer(index), value });
    }

    public void setDate(int index, Date value)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setDate", new Class[] { Integer.TYPE, Date.class }, new Object[] { new Integer(index), value });
    }

    public void setTime(int index, Time value)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setTime", new Class[] { Integer.TYPE, Time.class }, new Object[] { new Integer(index), value });
    }

    public void setTimestamp(int index, Timestamp value)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setTimestamp", new Class[] { Integer.TYPE, Timestamp.class }, new Object[] { new Integer(index), value });
    }

    public void setAsciiStream(int index, InputStream input, int length)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setAsciiStream", new Class[] { Integer.TYPE, InputStream.class, Integer.TYPE },
                new Object[] { new Integer(index), input, new Integer(length) });
    }

    public void setUnicodeStream(int index, InputStream input, int length)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setUnicodeStream", new Class[] { Integer.TYPE, InputStream.class, Integer.TYPE },
                new Object[] { new Integer(index), input, new Integer(length) });
    }

    public void setBinaryStream(int index, InputStream input, int length)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setBinaryStream", new Class[] { Integer.TYPE, InputStream.class, Integer.TYPE },
                new Object[] { new Integer(index), input, new Integer(length) });
    }

    public void clearParameters()
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "clearParameters");
    }

    public void setObject(int index, Object value, int targetType, int scale)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setObject", new Class[] { Integer.TYPE, Object.class, Integer.TYPE, Integer.TYPE },
                new Object[] { new Integer(index), value, new Integer(targetType), new Integer(scale) });
    }

    public void setObject(int index, Object value, int targetType)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setObject", new Class[] { Integer.TYPE, Object.class, Integer.TYPE },
                new Object[] { new Integer(index), value, new Integer(targetType) });
    }

    public void setObject(int index, Object value)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setObject", new Class[] { Integer.TYPE, Object.class }, new Object[] { new Integer(index), value });
    }

    public boolean execute()
    throws SQLException
    {
        checkClosed();
        return callBooleanMethod(statement, "execute", new Class[0], new Object[0]);
    }

    public void addBatch()
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "addBatch");
    }

    public void setCharacterStream(int index, Reader reader, int length)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setCharacterStream", new Class[] { Integer.TYPE, Reader.class, Integer.TYPE },
                new Object[] { new Integer(index), reader, new Integer(length) });
    }

    public void setRef(int index, Ref value)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setRef", new Class[] { Integer.TYPE, Ref.class }, new Object[] { new Integer(index), value });
    }

    public void setBlob(int index, Blob value)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setBlob", new Class[] { Integer.TYPE, Blob.class }, new Object[] { new Integer(index), value });
    }

    public void setClob(int index, Clob value)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setClob", new Class[] { Integer.TYPE, Clob.class }, new Object[] { new Integer(index), value });
    }

    public void setArray(int index, Array value)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setArray", new Class[] { Integer.TYPE, Array.class }, new Object[] { new Integer(index), value });
    }

    public ResultSetMetaData getMetaData()
    throws SQLException
    {
        checkClosed();
        return (ResultSetMetaData)callMethod(statement, "getMetaData");
    }

    public void setDate(int index, Date value, Calendar cal)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setDate", new Class[] { Integer.TYPE, Date.class, Calendar.class },
                new Object[] { new Integer(index), value, cal } );
    }

    public void setTime(int index, Time value, Calendar cal)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setTime", new Class[] { Integer.TYPE, Time.class, Calendar.class },
                new Object[] { new Integer(index), value, cal } );
    }

    public void setTimestamp(int index, Timestamp value, Calendar cal)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setTimestamp", new Class[] { Integer.TYPE, Timestamp.class, Calendar.class },
                new Object[] { new Integer(index), value, cal } );
    }

    public void setNull(int index, int type, String typeName)
    throws SQLException
    {
        checkClosed();
        callMethod(statement, "setNull", new Class[] { Integer.TYPE, Integer.TYPE, String.class },
                new Object[] { new Integer(index), new Integer(type), typeName } );
    }

  public void setAsciiStream(int i, InputStream in, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setAsciiStream(int i, InputStream in) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setBinaryStream(int i, InputStream in, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setBinaryStream(int i, InputStream in) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setBlob(int i, InputStream in, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setBlob(int i, InputStream in) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setCharacterStream(int i, Reader r, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setCharacterStream(int i, Reader r) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setClob(int i, Reader r, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setClob(int i, Reader r) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setNCharacterStream(int i, Reader r, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setNCharacterStream(int i, Reader r) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setNClob(int i, Reader r, long l) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setNClob(int i, Reader r) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setNClob(int i, NClob nc) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setNString(int i, String s) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setRowId(int i, RowId rowId) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  public void setSQLXML(int i, SQLXML sqlXml) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

}
