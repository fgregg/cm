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

import java.util.*;
import java.lang.reflect.*;

/**
 *  Constants for messages loaded from resource bundles.
 */
public class MessageConstants
{
  public static final String CONNECTION_CHECKOUT_TRACE_MESSAGE = "ConnectionCheckoutTrace";

  public static final String CONNECTION_IS_CLOSED = "ConnectionIsClosed";

  public static final String GETTING_CONNECTION_MESSAGE = "GettingConnection";

  public static final String REFRESHING_CONNECTION_MESSAGE = "RefreshingConnection";

  public static final String CLOSING_CONNECTION_MESSAGE = "ClosingConnection";

  public static final String STILL_NOT_OK_MESSAGE = "StillNotOK";

  public static final String CONNECTION_INVALID = "ConnectionInvalid";

  public static final String CONNECTION_INVALID_REFRESH = "ConnectionInvalidRefresh";

  public static final String CONNECTION_OK = "ConnectionOK";

  public static final String CANNOT_RESET_CONNECTION = "CannotResetPoolConnection";

  public static final String CANNOT_ASK_AUTOCOMMIT_MESSAGE = "CannotAskAutoCommit";

  public static final String CANNOT_ASK_ISOLATION_MESSAGE = "CannotAskIsolation";

  public static final String CANNOT_ASK_READONLY_MESSAGE = "CannotAskReadOnly";

  public static final String CANNOT_CHECKIN_CONNECTION_MESSAGE = "CannotCheckinConnection";

  public static final String EXCEPTION_RECREATING_CONNECTION_MESSAGE = "ExceptionRecreatingConnection";

  public static final String UNKNOWN_POOL_MESSAGE = "UnknownPool";

  public static final String CANNOT_GET_CONNECTION_TIMEOUT_MESSAGE = "CannotGetConnectionTimeout";

  public static final String CANNOT_CLOSE_OPEN_CONNECTION_MESSAGE = "CannotCloseOpenConnection";

  public static final String CANNOT_CHECKOUT_MESSAGE = "CannotCheckout";

  public static final String MUST_SPECIFY_PROP_MESSAGE = "MustSpecifyProperty";

  public static final String MUST_SPECIFY_INT_PROP_MESSAGE = "MustSpecifyIntegerProperty";

  public static final String MUST_SPECIFY_IF_PROP_MESSAGE = "MustSpecifyIfProperty";

  public static final String MUST_SPECIFY_LESS_THAN_IF_PROP_MESSAGE = "MustSpecifyLessThanIfProperty";

  public static final String LOOKING_FOR_IDLE_CONNECTIONS_MESSAGE = "LookingForIdleConnections";

  public static final String CLOSING_IDLE_CONNECTION_MESSAGE = "ClosingIdleConnection";

  public static final String CANNOT_LOAD_DRIVER_MESSAGE = "CannotLoadDriver";

  public static final String CONNECTION_CHECKOUT_MESSAGE = "ConnectionCheckoutHere";

  public static final String EXCEPTION_CLOSE_CONNECTION_MESSAGE = "ExceptionClosingConnection";

  public static final String MAID_EXCEPTION_MESSAGE = "MaidException";

  public static final String REFRESHING_CONNECTIONS_MESSAGE = "RefreshingConnectionsInPool";

  public static final String CONNECTION_IS_INVALID_MESSAGE = "ConnectionIsInvalid";

  public static final String SHUTDOWN_POOLS_MESSAGE = "ShutdownPools";

  public static final String CREATING_POOL_MESSAGE = "CreatingPool";

  public static final String CREATING_POOLS_FAILED_MESSAGE = "CreatingPoolsFailed";

  public static final String CANNOT_CREATE_POOL_MESSAGE = "CannotCreateConnectionPool";

  public static final String STARTUP_SUCCESS = "StartupSuccess";

  private MessageConstants()
  {
    super();
  }

  /**
   *  Display values for known messages.
   */
  public static void main(String args[])
  throws Exception
  {
    Class c = MessageConstants.class;
    System.out.println("Constants class: " + c.getName());
    System.out.println("Locale:          " + Locale.getDefault());
    System.out.println("");
    Field fields[] = c.getDeclaredFields();
    int count = 0;
    for (int i=0; i<fields.length; i++)
    {
      if (fields[i].getType() == String.class)
      {
        String name = fields[i].getName();
        String value = (String)fields[i].get(null);
        String val = PoolResources.getResourceString(value);
        System.out.println("Variable name:   " + name);
        System.out.println("        value:   " + value);
        System.out.println("     property:   " + val);
        System.out.println("");
        count++;
      }
    }
    System.out.println("Found " + count + " messages.");
  }
}
