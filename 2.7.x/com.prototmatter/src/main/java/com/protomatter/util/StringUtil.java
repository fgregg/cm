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

import java.util.*;

/**
 * Utility methods for manipulating strings.
 */
public class StringUtil
{
  /**
   *  Private constructor so nobody constructs us.
   */
  private StringUtil()
  {
    super();
  }

  /**
   * Makes sure that a string has no more than n characters,
   * and pads it with spaces if it does.
   */
  public static void pad(StringBuffer b, String s, int n)
  {
    int l = s.length();
    if (l == n)
    {
      b.append(s);
      return;
    }
    else if (l > n)
    {
      b.append(s.substring(0, n));
      return;
    }
    else
    {
      b.append(s.substring(0, l));
      while (l++<n)
        b.append(' ');
      return;
    }
  }

  /**
   * Makes sure that a string has no more than n characters,
   * and pads it with spaces if it does.
   */
  public static String pad(String s, int n)
  {
    int l = s.length();
    if (l == n)
     return s;
    else if (l > n)
     return s.substring(0, n);
    else
    {
      char[] carr = new char[n];
      s.getChars(0, l, carr, 0);
      while (l<n)
        carr[l++] = ' ';
      return new String(carr);
    }
  }

  /**
   * Truncate a string to the given length.
   * If s.length() <= n, returns s.
   * Else, returns the first n characters of s.
   */
  public static String truncate(String s, int n)
  {
    if (s.length() <= n)
      return s;
    else
      return s.substring(0, n);
  }

  /**
   * Nicely truncate a string.
   * Truncates s to n chars breaking s on whitespace,
   * and adding a "..." to the end.
   */
  public static String truncateNicely(String s, int n)
  {
    if (n <= 3)
    {
      StringBuffer b = new StringBuffer(n);
      for (int i=0; i<n; i++)
        b.append(".");
      return b.toString();
    }
    int sublen = n-3;
    StringBuffer clippedtext = new StringBuffer();
    StringTokenizer st = new StringTokenizer(s);
    while (st.hasMoreTokens())
    {
      String word = st.nextToken();
      int cliplen = clippedtext.length();
      if(word.length() + cliplen + 1 <= sublen)
      {
        if (cliplen == 0)
        {
          clippedtext.append(word);
        }
        else
        {
          clippedtext.append(" ");
          clippedtext.append(word);
        }
      }
      else
      {
        if(clippedtext.length() == 0)
          clippedtext.append(s.substring(0,sublen));
        clippedtext.append("...");
        break;
      }
    }
    return clippedtext.toString();
  }

  public static String replace(String inputString, String token, String replacement)
  {
      int index = 0;
      int replacementLength = replacement.length();
      int tokenLength = token.length();
      String input = inputString;
      while (true)
      {
          index = input.indexOf(token, index);
          if (index == -1)
          {
              return input;
          }
          StringBuffer buf = new StringBuffer(input.length() + replacementLength);
          buf.append(input.substring(0, index));
          buf.append(replacement);
          buf.append(input.substring(index + tokenLength));
          input = buf.toString();
      }
  }
}
