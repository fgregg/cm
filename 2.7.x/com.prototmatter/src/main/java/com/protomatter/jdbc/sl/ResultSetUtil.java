package com.protomatter.jdbc.sl;

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

import java.text.*;
import java.sql.*;
import java.io.*;
import java.lang.reflect.*;

/**
 *  A utility class for formatting JDBC ResultSets.
 */
class ResultSetUtil
{
  static void formatResultSet(ResultSet s, PrintStream out)
  throws SQLException
  {
      formatResultSet(s, out, false);
  }

  static void formatResultSet(ResultSet s, PrintStream out, boolean showQuotes)
  throws SQLException
  {
    ResultSetMetaData m = s.getMetaData();

    int nCols = m.getColumnCount();
    int maxcolwidth = 0;
    for (int i=1; i<=nCols; i++)
    {
      String label = m.getColumnLabel(i);
      if (label == null) label = "[NULL]";
      int q = label.length();
      if (q > maxcolwidth)
        maxcolwidth = q;
    }
    maxcolwidth += 2;

    int numRows = 0;
    long time = System.currentTimeMillis();
    while (s.next())
    {
      ++numRows;
      out.println("---------------------------------------------------------");
      for (int i=1; i<=nCols; i++)
      {
        String label = m.getColumnLabel(i);
        if (label == null) label = "[NULL]";
        out.print(label);
        for (int j=0; j<(maxcolwidth - label.length()); j++)
          out.print(" ");
        try
        {
          Object o;
          // lookup what kind of column this is
          if (m.getColumnType(i) == Types.LONGVARBINARY)
          {
            o = s.getBytes(i);
            //System.out.println(printByteArray((byte[])o));
          }
          else
          {
            o = s.getObject(i);
          }

          if (o instanceof byte[])
          {
            if (showQuotes) out.print("'");
            out.print(new String((byte[])o));
            if (showQuotes) out.print("'");
          }
          else if ((o != null) && (o.getClass().getName().equals("oracle.sql.CLOB")))
          {
            try
            {
              Class oc = o.getClass();
              Method method = oc.getMethod("getCharacterStream", new Class[] { });
              Reader r = (Reader)method.invoke(o, new Object[] { });
              char buffer[] = new char[1024];
              int read = 0;
              if (showQuotes) out.print("'");
              while ((read = r.read(buffer)) != -1)
              {
                out.print(new String(buffer, 0, read));
              }
              if (showQuotes) out.print("'");
            }
            catch (Exception x)
            {
              if (x instanceof SQLException)
                throw (SQLException)x;

              System.out.println("");
              System.out.println(" --> " + x.toString());
              if (showQuotes) out.print("'");
              out.print(o.toString());
              if (showQuotes) out.print("'");
            }
          }
          else
          {
            if (showQuotes) out.print("'");
            out.print(o.toString());
            if (showQuotes) out.print("'");
          }
        }
        catch (NullPointerException e)
        {
          out.print("[NULL]");
          if (showQuotes) out.print("'");
        }
        out.println("");
      }
    }
    time = System.currentTimeMillis() - time;
    out.println("Command returned " + numRows + " rows");
    out.println("Getting ResultSet contents took " + time + "ms");
  }

  private static String printByteArray(byte[] b)
  {
    StringBuffer sb = new StringBuffer();
    for (int i=0; i<b.length; i++)
    {
      if ((i%20) == 0) sb.append("\n");
      DecimalFormat format = new DecimalFormat("000");
      sb.append(format.format((int)b[i]));
      sb.append(" ");
    }
    return sb.toString();
  }
}
