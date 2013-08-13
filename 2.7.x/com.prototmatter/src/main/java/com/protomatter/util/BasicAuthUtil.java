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

import java.io.*;
import java.util.*;
import javax.servlet.http.*;

/**
 *  A utility class that provides methods for checking and
 *  requiring basic authentication in servlets.
 */
public class BasicAuthUtil
{
  /**
   *  Private constructor since this is an all-static class.
   */
  private BasicAuthUtil()
  {
    super();
  }

  /**
   *  Decodes the "Authorization" header and retrieves the
   *  user's name from it.  Returns null if the header is not present.
   */
  public static String getAuthUsername(HttpServletRequest request)
  {
    String header = request.getHeader("Authorization");
    if (header == null)
      return null;
    String encoded = header.substring(header.indexOf(" ") + 1);
    String decoded = new String(Base64.decode(encoded));
    return decoded.substring(0, decoded.indexOf(":"));
  }

  /**
   *  Decodes the "Authorization" header and retrieves the
   *  password from it.  Returns null if the header is not present.
   */
  public static String getAuthPassword(HttpServletRequest request)
  {
    String header = request.getHeader("Authorization");
    if (header == null)
      return null;
    String encoded = header.substring(header.indexOf(" ") + 1);
    String decoded = new String(Base64.decode(encoded));
    return decoded.substring(decoded.indexOf(":") +1);
  }

  /**
   *  Sends coprrect headers to require basic authentication for the
   *  given realm.
   */
  public static void requireAuthentication(String realm, HttpServletResponse resp)
  throws IOException
  {
    resp.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
    resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
  }
}
