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

import java.sql.*;
import javax.sql.*;
import java.util.*;
import com.protomatter.util.*;
import com.protomatter.pool.*;

/**
 *  An implementation of the <tt>javax.sql.PooledConnection</tt> interface.
 *  This is only for use with the <tt>JdbcConnectionPoolDataSource</tt>
 *  class.
 *
 *  @see JdbcConnectionPoolDataSource
 *  @see JdbcConnectionPoolDriver
 *  @see JdbcConnectionPool
 *  @see javax.sql.PooledConnection
 */
public class JdbcConnectionPoolPooledConnection
implements PooledConnection
{
  private JdbcConnectionPoolConnection connection = null;

  /**
   *  Create a new <tt>JdbcConnectionPoolPooledConnection</tt> wrapping
   *  the given <tt>JdbcConnectionPoolConnection</tt>.
   */
  JdbcConnectionPoolPooledConnection(JdbcConnectionPoolConnection connection)
  {
    this.connection = connection;
  }

  /**
   *  @see javax.sql.ConnectionPoolDataSource
   */
  public Connection getConnection()
  throws SQLException
  {
    if (this.connection.isObjectPoolObjectValid())
      return (Connection)this.connection;
    throw new SQLException(PoolResources.getResourceString(MessageConstants.CONNECTION_IS_INVALID_MESSAGE));
  }

  /**
   *  @see javax.sql.ConnectionPoolDataSource
   */
  public void close()
  throws SQLException
  {
    if (this.connection.isObjectPoolObjectValid())
      this.connection.reallyClose();
  }

  /**
   *  Not currently implemented.
   *
   *  @see javax.sql.ConnectionPoolDataSource
   */
  public void addConnectionEventListener(ConnectionEventListener listener)
  {
    // not implemented
  }

  /**
   *  Not currently implemented.
   *
   *  @see javax.sql.ConnectionPoolDataSource
   */
  public void removeConnectionEventListener(ConnectionEventListener listener)
  {
    // not implemented
  }
}
