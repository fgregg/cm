package com.protomatter.syslog.util.logging;

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
  public static final String ENTERING_MESSAGE = "Logging.Entering";

  public static final String ENTERING_WITH_PARAM_MESSAGE = "Logging.EnteringWithParam";

  public static final String EXITING_MESSAGE = "Logging.Exiting";

  public static final String EXITING_WITH_PARAM_MESSAGE = "Logging.ExitingWithParam";

  public static final String THROWING_MESSAGE = "Logging.Throwing";

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
        String val = LogResources.getResourceString(value);
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