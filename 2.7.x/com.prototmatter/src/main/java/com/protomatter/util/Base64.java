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

/**
 *  Base64 encoder/decoder.  Does not stream, so be careful with
 *  using large amounts of data.
 */
public class Base64
{
  private Base64()
  {
    super();
  }

  /**
   *  Encode some data and return a String.
   */
  public final static String encode(byte[] d)
  {
    if (d == null) return null;
    byte data[] = new byte[d.length+2];
        System.arraycopy(d, 0, data, 0, d.length);
    byte dest[] = new byte[(data.length/3)*4];

    // 3-byte to 4-byte conversion
    for (int sidx = 0, didx=0; sidx < d.length; sidx += 3, didx += 4)
    {
      dest[didx]   = (byte) ((data[sidx] >>> 2) & 077);
      dest[didx+1] = (byte) ((data[sidx+1] >>> 4) & 017 |
                  (data[sidx] << 4) & 077);
      dest[didx+2] = (byte) ((data[sidx+2] >>> 6) & 003 |
                  (data[sidx+1] << 2) & 077);
      dest[didx+3] = (byte) (data[sidx+2] & 077);
    }

    // 0-63 to ascii printable conversion
    for (int idx = 0; idx <dest.length; idx++)
    {
      if (dest[idx] < 26)     dest[idx] = (byte)(dest[idx] + 'A');
      else if (dest[idx] < 52)  dest[idx] = (byte)(dest[idx] + 'a' - 26);
      else if (dest[idx] < 62)  dest[idx] = (byte)(dest[idx] + '0' - 52);
      else if (dest[idx] < 63)  dest[idx] = (byte)'+';
      else            dest[idx] = (byte)'/';
    }

    // add padding
    for (int idx = dest.length-1; idx > (d.length*4)/3; idx--)
    {
      dest[idx] = (byte)'=';
    }
    return new String(dest);
  }

  /**
   *  Decode data and return bytes.
   */
  public final static byte[] decode(String str)
  {
    if (str == null)  return  null;
    return decode(str.getBytes());
  }

  /**
   *  Decode data and return bytes.  Assumes that the data passed
   *  in is ASCII text.
   */
  public final static byte[] decode(byte[] data)
  {
    int tail = data.length;
    while (data[tail-1] == '=')  tail--;
    byte dest[] = new byte[tail - data.length/4];

    // ascii printable to 0-63 conversion
    for (int idx = 0; idx <data.length; idx++)
    {
      if (data[idx] == '=')    data[idx] = 0;
      else if (data[idx] == '/') data[idx] = 63;
      else if (data[idx] == '+') data[idx] = 62;
      else if (data[idx] >= '0'  &&  data[idx] <= '9')
        data[idx] = (byte)(data[idx] - ('0' - 52));
      else if (data[idx] >= 'a'  &&  data[idx] <= 'z')
        data[idx] = (byte)(data[idx] - ('a' - 26));
      else if (data[idx] >= 'A'  &&  data[idx] <= 'Z')
        data[idx] = (byte)(data[idx] - 'A');
    }

    // 4-byte to 3-byte conversion
    int sidx, didx;
    for (sidx = 0, didx=0; didx < dest.length-2; sidx += 4, didx += 3)
    {
      dest[didx]   = (byte) ( ((data[sidx] << 2) & 255) |
              ((data[sidx+1] >>> 4) & 3) );
      dest[didx+1] = (byte) ( ((data[sidx+1] << 4) & 255) |
              ((data[sidx+2] >>> 2) & 017) );
      dest[didx+2] = (byte) ( ((data[sidx+2] << 6) & 255) |
              (data[sidx+3] & 077) );
    }
    if (didx < dest.length)
    {
      dest[didx]   = (byte) ( ((data[sidx] << 2) & 255) |
              ((data[sidx+1] >>> 4) & 3) );
    }
    if (++didx < dest.length)
    {
      dest[didx]   = (byte) ( ((data[sidx+1] << 4) & 255) |
              ((data[sidx+2] >>> 2) & 017) );
    }
    return dest;
  }

  /**
   *  A simple test that encodes and decodes the first commandline argument.
   */
  public static final void main(String args[])
  {
    if (args.length != 1)
    {
      System.out.println("Usage: Base64 string");
      System.exit(0);
    }
    try
    {
      String e = Base64.encode(args[0].getBytes());
      String d = new String(Base64.decode(e));
      System.out.println("Input   = '" + args[0] + "'");
      System.out.println("Encoded = '" + e + "'");
      System.out.println("Decoded = '" + d + "'");
    }
    catch (Exception x)
    {
      x.printStackTrace();
    }
  }
}

