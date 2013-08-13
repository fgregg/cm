package com.protomatter.jdbc.pool;
import com.protomatter.syslog.Channel;

import com.protomatter.util.Debug;

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

import java.sql.*;
import java.text.*;
import java.util.*;
import com.protomatter.syslog.Channel;
import com.protomatter.util.Debug;

/**
 *  The driver for use with JDBC connection pools. When this class is loaded, it
 *  registers itself with the DriverManager. This driver accepts JDBC connection
 *  URLs of the form:<P>
 *
 *
 *  <ul><tt>jdbc:protomatter:pool:<i>PoolName</i> </tt>
 *  </ul>
 *  <P>
 *
 *  This class also keeps a static reference to the list of all known connection
 *  pools, which is updated by creating a new JdbcConnectionPool object. Pools
 *  can be un-registered by calling <tt>unRegisterPool()</tt> on the
 *  JdbcConnectionPool object.
 *
 *@author     nate
 *@created    October 19, 2002
 *@see        java.sql.DriverManager
 *@see        JdbcConnectionPool
 *@see        JdbcConnectionPoolConnection
 */
public class JdbcConnectionPoolDriver
implements Driver
{
    private static Map pools = new HashMap();
    private static int MAJOR_VERSION = 1;
    private static int MINOR_VERSION = 1;

    protected static Debug DEBUG = Debug.forPackage(JdbcConnectionPoolDriver.class);
    protected static Channel log = Channel.forPackage(JdbcConnectionPoolDriver.class);


    /**
     *  Default constructor.
     */
    public JdbcConnectionPoolDriver()
    {
        super();
    }

    /**
     *  Static initializer. When this class is loaded, it registers itself with
     *  the JDBC driver manager.
     */
    static
    {
        // register ourselves on load with the driver manager.
        try
        {
            if (DEBUG.debug())
            {
                log.debug(JdbcConnectionPoolDriver.class, "Registering JDBC driver");
            }
            Driver driver = new JdbcConnectionPoolDriver();
            DriverManager.registerDriver(driver);
        }
        catch (SQLException x)
        {
            log.error(JdbcConnectionPoolDriver.class, "Cannot register JDBC driver", x);
        }
    }


    static int countRegisteredPools()
    {
        return pools.size();
    }


    static void registerPool(JdbcConnectionPool pool)
    {
        if (DEBUG.debug())
            log.debug(JdbcConnectionPoolDriver.class, "Registering pool: " + pool);
        pools.put(pool.getName(), pool);
    }


    static void unRegisterPool(JdbcConnectionPool pool)
    {
        if (DEBUG.debug())
            log.debug(JdbcConnectionPoolDriver.class, "Un-registering pool: " + pool);
        pools.remove(pool.getName());
    }


    /**
     *  Get one of the currently registered JDBC Connection Pools.
     *
     *@param  poolName  Description of the Parameter
     *@return           The pool value
     */
    public static JdbcConnectionPool getPool(String poolName)
    {
        return (JdbcConnectionPool)pools.get(poolName);
    }


    /**
     *  Get the list of registered pool names. The returned enumeration contains
     *  strings.
     *
     *@return    The poolNames value
     */
    public static Iterator getPoolNames()
    {
        return pools.keySet().iterator();
    }


    /**
     *  Shuts down all connections for all JDBC connection pools. This should
     *  only be used as part of a system shutdown of some kind. All pools are
     *  un-registered when this method is run.
     *
     *@see    JdbcConnectionPool#closeAllConnections
     */
    public static void shutdownAllConnections()
    {
        if (DEBUG.debug())
            log.debug(JdbcConnectionPoolDriver.class, "Shutting down all connections");
        // lock everyone out first.
        Map thePools = pools;
        pools = new HashMap();

        Iterator i = thePools.keySet().iterator();
        while (i.hasNext())
        {
            String poolName = (String)i.next();
            if (DEBUG.debug())
                log.debug(JdbcConnectionPoolDriver.class, "Closing connections on pool " + poolName);
            JdbcConnectionPool pool = (JdbcConnectionPool)thePools.get(poolName);
            pool.closeAllConnections();
        }
    }


    /**
     *  The JDBC URL prefix for making connections. The poolname should be
     *  appended to this string when asking for connections from the
     *  DriverManager.
     */
    public static String URL_PREFIX = "jdbc:protomatter:pool:";


    /**
     *  Check a connection out of the pool specified in the URL.
     *
     *@param  url               Description of the Parameter
     *@param  props             Description of the Parameter
     *@return                   Description of the Return Value
     *@exception  SQLException  If there is a problem checking a connection out
     *      of the pool.
     *@see                      java.sql.Driver
     */
    public Connection connect(String url, Properties props)
        throws SQLException
    {
        if (DEBUG.debug())
            log.debug(this, "connect(" + url + ", " + props + ")");

        if (!url.startsWith(URL_PREFIX))
            return null;

        String poolName = url.substring(URL_PREFIX.length());

        JdbcConnectionPool pool = (JdbcConnectionPool)pools.get(poolName);
        if (pool == null)
        {
            throw new SQLException(MessageFormat.format(
                    PoolResources.getResourceString(MessageConstants.UNKNOWN_POOL_MESSAGE),
                    new Object[]{poolName}));
        }

        // checkout a connection
        try
        {
            JdbcConnectionPoolConnection c = (JdbcConnectionPoolConnection)pool.checkout();
            c.setCheckoutStackTrace(new JdbcCheckoutExceptionTrace());
            c.resetLastTimeUsed();
            return new ConnectionWrapper(c, pool);
        }
        catch (Exception x)
        {
            throw new PoolSQLException(MessageFormat.format(
                    PoolResources.getResourceString(MessageConstants.CANNOT_CHECKOUT_MESSAGE),
                    new Object[]{pool.getName(), x.toString()}), x);
        }
    }


    /**
     *@param  url               Description of the Parameter
     *@return                   Description of the Return Value
     *@exception  SQLException  Because java.sql.Driver throws one here.
     *@see                      java.sql.Driver
     */
    public boolean acceptsURL(String url)
        throws SQLException
    {
        return url.startsWith(URL_PREFIX);
    }


    /**
     *@param  url               Description of the Parameter
     *@param  props             Description of the Parameter
     *@return                   The propertyInfo value
     *@exception  SQLException  Because java.sql.Driver throws one here.
     *@see                      java.sql.Driver
     */
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties props)
        throws SQLException
    {
        return null;
    }


    /**
     *@return    The majorVersion value
     *@see       java.sql.Driver
     */
    public int getMajorVersion()
    {
        return MAJOR_VERSION;
    }


    /**
     *@return    The minorVersion value
     *@see       java.sql.Driver
     */
    public int getMinorVersion()
    {
        return MINOR_VERSION;
    }


    /**
     *@return    Description of the Return Value
     *@see       java.sql.Driver
     */
    public boolean jdbcCompliant()
    {
        return false;
    }
}

