package com.protomatter.util;

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
import java.util.*;
import com.protomatter.syslog.Syslog;

/**
 *  Database-related utility class.
 */
public class DatabaseUtil
{
  // make the constructor private so nobody calls it.
  private DatabaseUtil()
  {
    super();
  }

  /**
   *  Ensure that the given driver has been registered.
   *  This method makes sure that the given driver is
   *  loaded and registered with the JDBC Driver Manager.
   *  Calling this method multiple times will <i>not</i>
   *  result in the driver being registered twice.
   */
  public static void registerDriver(String driverName)
  throws Exception
  {
    boolean found = false;
    Enumeration e = DriverManager.getDrivers();
    while (e.hasMoreElements() && !found)
    {
      Object o = e.nextElement();
      if (o.getClass().getName().equals(driverName))
        found = true;
    }

    if (!found)
    {
      Driver d = (Driver)Class.forName(driverName).newInstance();
      found = false;
      e = DriverManager.getDrivers();
      while (e.hasMoreElements() && !found)
      {
        Object o = e.nextElement();
        if (o.getClass().getName().equals(driverName))
          found = true;
      }

      if (!found)
        DriverManager.registerDriver(d);
    }
  }

  /**
   *  Close the given Connection. Return true when there's no
   *  exception thrown, false if there was one.
   */
  public static boolean close(Connection c)
  {
    return close(c, DatabaseUtil.class);
  }

  /**
   *  Close the given Connection. Return true when there's no
   *  exception thrown, false if there was one.
   *  The logger is passed onto a call to <tt>Syslog.log()</tt>
   *  in the event of an exception.
   */
  public static boolean close(Connection c, Object logger)
  {
    if (c == null) return true;
    try
    {
      c.close();
      return true;
    }
    catch (SQLException x)
    {
      Syslog.log(logger, x);
    }
    return false;
  }

  /**
   *  Close the given Statement. Return true when there's no
   *  exception thrown, false if there was one.
   */
  public static boolean close(Statement s)
  {
    return close(s, DatabaseUtil.class);
  }

  /**
   *  Close the given Statement. Return true when there's no
   *  exception thrown, false if there was one.
   *  The logger is passed onto a call to <tt>Syslog.log()</tt>
   *  in the event of an exception.
   */
  public static boolean close(Statement s, Object logger)
  {
    if (s == null) return true;
    try
    {
      s.close();
      return true;
    }
    catch (SQLException x)
    {
      Syslog.log(logger, x);
    }
    return false;
  }

  /**
   *  Close the given ResultSet. Return true when there's no
   *  exception thrown, false if there was one.
   */
  public static boolean close(ResultSet r)
  {
    return close(r, DatabaseUtil.class);
  }

  /**
   *  Close the given ResultSet. Return true when there's no
   *  exception thrown, false if there was one.
   *  The logger is passed onto a call to <tt>Syslog.log()</tt>
   *  in the event of an exception.
   */
  public static boolean close(ResultSet r, Object logger)
  {
    if (r == null) return true;
    try
    {
      r.close();
      return true;
    }
    catch (SQLException x)
    {
      Syslog.log(logger, x);
    }
    return false;
  }
}
