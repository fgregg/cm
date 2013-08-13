package com.protomatter.jdbc.pool;

import java.io.*;
import java.sql.*;

import java.lang.reflect.*;

import com.protomatter.syslog.Channel;
import com.protomatter.util.Debug;

/**
 *  A base wrapper class that uses reflection to call methods.
 */
class JDBCWrapper
{
    private Channel log = Channel.forPackage(JDBCWrapper.class);
    
    private Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private Class[] EMPTY_CLASS_ARRAY = new Class[0];

    private String trimFromLastPeriod(String s)
    {
        int index = s.lastIndexOf(".");
        if (index != -1)
            return s.substring(index +1);
        return s;
    }

    protected Object callMethod(Object target, String method, Class paramTypes[], Object params[])
    throws SQLException
    {
        Class targetClass = target.getClass();
        Class thisClass = this.getClass();
        try
        {
            Method m = targetClass.getMethod(method, paramTypes);
            long time = System.currentTimeMillis();
            Object returnValue = m.invoke(target, params);
            time = System.currentTimeMillis() - time;
            
            if (m.getReturnType() == Void.TYPE)
                log.debug(this, trimFromLastPeriod(thisClass.getName()) + "." + method + "(" + toString(params) + ")"
                    + " call took " + time + "ms");
            else
                log.debug(this, trimFromLastPeriod(thisClass.getName()) + "." + method + "(" + toString(params) + ") = " + toString(returnValue)
                    + " call took " + time + "ms");

            return returnValue;
        }
        catch (InvocationTargetException tx)
        {
            Throwable thrownException = tx.getTargetException();
            if (thrownException instanceof SQLException)
            {
                SQLException thrown = (SQLException)thrownException;
                log.debug(this, trimFromLastPeriod(thisClass.getName()) + "." + method + "(" + toString(params) + ") threw " + thrown, thrown);
                throw thrown;
            }
            throw new SQLException("Protomatter JDBCWrapper can't call method " + targetClass.getName() + "." + method + "(" + toString(params) + "): " + tx.toString());
        }
        catch (Exception x)
        {
            throw new SQLException("Protomatter JDBCWrapper can't call method " + targetClass.getName() + "." + method + "(" + toString(params) + "): " + x.toString());
        }
    }

    protected Object callMethod(Object target, String methodName)
    throws SQLException
    {
        return callMethod(target, methodName, EMPTY_CLASS_ARRAY, EMPTY_OBJECT_ARRAY);
    }

    private String toString(Object o[])
    {
        if (o == null)
            return "[null-array]";

        StringBuffer b = new StringBuffer();
        for (int i=0; i<o.length; i++)
        {
            b.append(toString(o[i]));
            if (i != (o.length -1))
                b.append(", ");
        }
        return b.toString();
    }

    private String toString(Object o)
    {
        if (o == null)
            return "<null>";

        if (o instanceof String)
            return "\"" + o.toString() + "\"";

        return String.valueOf(o);
    }

    protected int callIntMethod(Object target, String methodName, Class paramTypes[], Object params[])
    throws SQLException
    {
        Integer value = (Integer)callMethod(target, methodName, paramTypes, params);
        return value.intValue();
    }

    protected float callFloatMethod(Object target, String methodName, Class paramTypes[], Object params[])
    throws SQLException
    {
        Float value = (Float)callMethod(target, methodName, paramTypes, params);
        return value.floatValue();
    }

    protected long callLongMethod(Object target, String methodName, Class paramTypes[], Object params[])
    throws SQLException
    {
        Long value = (Long)callMethod(target, methodName, paramTypes, params);
        return value.longValue();
    }

    protected boolean callBooleanMethod(Object target, String methodName, Class paramTypes[], Object params[])
    throws SQLException
    {
        Boolean value = (Boolean)callMethod(target, methodName, paramTypes, params);
        return value.booleanValue();
    }

    protected short callShortMethod(Object target, String methodName, Class paramTypes[], Object params[])
    throws SQLException
    {
        Short value = (Short)callMethod(target, methodName, paramTypes, params);
        return value.shortValue();
    }

    protected byte callByteMethod(Object target, String methodName, Class paramTypes[], Object params[])
    throws SQLException
    {
        Byte value = (Byte)callMethod(target, methodName, paramTypes, params);
        return value.byteValue();
    }

    protected double callDoubleMethod(Object target, String methodName, Class paramTypes[], Object params[])
    throws SQLException
    {
        Double value = (Double)callMethod(target, methodName, paramTypes, params);
        return value.doubleValue();
    }

    protected String callStringMethod(Object target, String methodName, Class paramTypes[], Object params[])
    throws SQLException
    {
        return (String)callMethod(target, methodName, paramTypes, params);
    }

}
