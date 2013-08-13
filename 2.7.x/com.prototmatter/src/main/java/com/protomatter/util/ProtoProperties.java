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

/**
 *  A Properties object that allows in-line file imports.
 *  This is just like java.util.Properties, except that
 *  it honors the following directives:<P>
 *
 *  <blockquote><tt>Import <i>filename</i></tt></blockquote><P>
 *
 *  Which imports the given filename in place (imported files can
 *  also have imports, but circular refs are <b>NOT</b> checked).
 */
public class ProtoProperties
extends Properties
{
  /**
   *  Create a blank properties object with nothing defined.
   *
   *  @see java.util.Properties
   */
  public ProtoProperties()
  {
    super();
  }

  /**
   *  Create a properties object with the given default settings.
   *
   *  @see java.util.Properties
   */
  public ProtoProperties(Properties props)
  {
    super(props);
  }

  public synchronized void load(InputStream in)
  throws IOException
  {
    read(new BufferedReader(new InputStreamReader(in)));
  }

  /**
   *  Read lines from the given buffered reader and append them
   *  to the given vector.
   */
  private void read(BufferedReader reader)
  throws IOException
  {
    String line = null;
    while (true)
    {
      if (line == null)
        line = reader.readLine();
      if (line == null)
        return;

      if ((line.length() > 0) && (line.charAt(0) != '#'))
      {
        if (line.startsWith("Import "))
        {
          String file = line.substring(7); // "Import ".length()
          read(new BufferedReader(new FileReader(new File(file))));
          line = null; // don't re-use this line.
        }
        else if (line.charAt(line.length() -1) == '\\')
        {
          line = line.substring(0, line.length() -1);
          StringBuffer b = new StringBuffer();
          b.append(line);

          boolean done = false;
          while (!done)
          {
            line = reader.readLine();
            if (line != null)
              line = line.trim();
            if (line == null || line.length() == 0)
            {
              line = null; // don't re-use this line
              done = true;
            }
            else if (line.charAt(0) == '#') // comment (skip)
            {
              ; // no-op
            }
            else
            {
              if (line.charAt(line.length() -1) == '\\')
              {
                line = line.substring(0, line.length() -1);
                b.append(line);
              }
              else
              {
                b.append(line);
                line = null; // don't re-use this line
                done = true;
              }
            }
          }

          addProp(b.toString());
        }
        else // "normal" line
        {
          addProp(line);
          line = null;
        }
      }
      else
      {
        line = null; // want to read the next line.
      }
    }
  }

  private void addProp(String line)
  {
    int index = line.indexOf("=");
    if (index != -1)
    {
      String key = line.substring(0, index).trim();
      String val = line.substring(index +1).trim();
      put(key, val);
    }
  }

  /**
   *  A simple test program that reads a properties file defined as the
   *  first command-line argument, and then lists the properties to
   *  Standard Out.
   */
  public static void main(String args[])
  {
    if (args.length != 1)
    {
      System.out.println("Usage: com.protomatter.util.ProtoProperties file");
      System.exit(0);
    }
    try
    {
      Properties p = new ProtoProperties();
      p.load(new FileInputStream(new File(args[0])));

      Enumeration e = p.keys();
      while (e.hasMoreElements())
      {
        String key = (String)e.nextElement();
        String val = p.getProperty(key);
        System.out.println("'" + key + "' = '" + val + "'");
        System.out.println("");
      }
    }
    catch (Exception x)
    {
      x.printStackTrace();
    }
  }
}
