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
import java.text.*;

/**
 *  A binary data printer.  This is basically a pretty-printer for
 *  binary data.
 */
public class ByteDisplay
{
  // things are faster if we cache these strings
  private static String ZERO        = "0";
  private static String DOT         = ".";
  private static String SPACE       = " ";
  private static String TWO_SPACE   = "  ";
  private static String THREE_SPACE = "   ";
  private static String CR          = "\n";
  private static String HEADER
  = "offset   hex data                                          ascii data\n";

  /**
   *  Display the given byte.
   */
  public static String displayBytes(int b)
  {
    byte[] thebyte = new byte[1];
    thebyte[0] = (byte)b;
    return displayBytes(thebyte);
  }

  /**
   *  Display the given byte array.
   */
  public static String displayBytes(byte[] b)
  {
    return displayBytes(b, 0, b.length);
  }

  /**
   *  Display the given byte array, starting at the given offset
   *  and ending after the given length.
   */
  public static String displayBytes(byte[] b, int off, int len)
  {
    StringBuffer buf = new StringBuffer(2048);
    int end = off + len;
    buf.append(HEADER);
    int i, j, c;

    for (i=off; i<end; i+=16)
    {
       if (i<100000)  buf.append(SPACE);
       if (i<10000)   buf.append(SPACE);
       if (i<1000)    buf.append(SPACE);
       if (i<100)     buf.append(SPACE);
       if (i<10)      buf.append(SPACE);
       buf.append(String.valueOf(i));
       buf.append(THREE_SPACE);

       // hex display
       for (j=i; j<end && j<i+16; j++)
       {
         c = b[j] & 0xff;
         if (c < 16) buf.append(ZERO);
         buf.append(Integer.toHexString(c));
         buf.append(SPACE);
       }
       if (j == end) // pad for end
       {
         while (j++<i+16)
           buf.append(THREE_SPACE);
       }

       // ascii display
       buf.append(TWO_SPACE);
       for (j=i; j<end && j<i+16; j++)
       {
         c = b[j] & 0xff;
         if (c < 32 || c > 127)
           buf.append(DOT);
         else
           buf.append((char)c);
       }
       buf.append(CR);
    }
    return buf.toString();
  }

  /**
   *  Display a file's contents.  Takes the first command-line
   *  argument as the name of the file to display.
   */
  public static void main(String args[])
  {
    if (args.length != 1 && args.length != 2)
    {
      System.out.println("Usage: ByteDisplay filename [size]");
      System.exit(0);
    }
    try
    {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      FileInputStream in = new FileInputStream(new File(args[0]));
      int size = Integer.MAX_VALUE;
      if (args.length == 2)
        size = Integer.parseInt(args[1]);

      byte[] buffer = new byte[16];
      int read = 0;
      int total = 0;
      while ( ((read = in.read(buffer)) != -1) && (total < size))
      {
        bytes.write(buffer, 0, read);
        total += read;
      }
      System.out.println("Read " + bytes.size() + " bytes from " + args[0]);
      System.out.println(displayBytes(bytes.toByteArray()));
    }
    catch (Exception x)
    {
      x.printStackTrace();
    }
  }
}
