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
 *  A string tokenizer.  Behaves like <tt>java.util.StringTokenizer</tt>
 *  extept that two delimiters next to eachother are recognized as
 *  two separate delimiters.  For instance, splitting "<tt>foo||bar</tt>"
 *  using "<tt>|</tt>" as the delimiter will return three tokens
 *  ("<tt>foo</tt>", "" (empty string), and "<tt>bar</tt>").
 */
public class ProtoStringTokenizer
{
  private String string;
  private int index = 0;
  private int length = 0;
  private Dictionary tokens = new Hashtable();

  /**
   *  Create a new ProtoStringTokenizer with the given string
   *  to tokenize and the given list of tokens.
   */
  public ProtoStringTokenizer(String string, String tokens)
  {
    this.string = string;
    this.length = string.length();
    String value = "";
    for (int i=0; i<tokens.length(); i++)
      this.tokens.put(new Character(tokens.charAt(i)), value);
  }

  /**
   *  Determine if there are more tokens available
   */
  public boolean hasMoreTokens()
  {
    return (index < length);
  }

  /**
   *  Get the next token
   */
  public String nextToken()
  {
    String s = this.string;
    for (int i=index; i<length; i++)
    {
      if (tokens.get(new Character(s.charAt(i))) != null)
      {
        String ret = s.substring(index, i);
        this.index = i +1;
        return ret;
      }
    }
    String ret = s.substring(index, length);
    this.index = length;
    return ret;
  }

  /**
   *  A test program.
   */
  public static void main(String args[])
  {
    if (args.length != 2)
    {
      System.out.println("Usage: ProtoStringTokenizer string delim");
      System.exit(0);
    }
    ProtoStringTokenizer st = new ProtoStringTokenizer(args[0], args[1]);
    System.out.println("String = '" + args[0] + "'");
    System.out.println("Delim  = '" + args[1] + "'");
    while (st.hasMoreTokens())
    {
      System.out.println("Token = '" + st.nextToken() + "'");
    }
  }
}
