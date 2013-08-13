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

import java.sql.*;
import java.text.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import com.protomatter.Protomatter;

/**
 *  A simple SQL tool.  Run it, and type "help".
 *  A list of files are taken as command line arguments -- each
 *  of these files are run as command files at startup.
 */
public class SimpleListener
{
  private Connection connection;
  private Driver driver;
  private String url;
  private Properties props;

  private String lastCommandLine = "";
  private Vector history = new Vector();
  private int historySize = 20;
  private String prompt = "SL> ";
  private String morePrompt = "";
  private boolean showStackTrace = false;
  private boolean showQuotes = false;
  private String defaultSchema = null;

  private SimpleListener()
  {
    super();
  }

  public static void main(String args[])
  {
    SimpleListener s = new SimpleListener();
    try
    {
      if (args.length > 0)
      {
        for (int i=0; i<args.length; i++)
        {
          String file = args[i];
          Vector theArgs = new Vector();
          theArgs.addElement(file);
          s.handleCommand_run(theArgs);
        }
      }
      s.commandLoop();
    }
    catch (Exception x)
    {
      x.printStackTrace();
    }
  }

  private void commandLoop()
  throws IOException
  {
    // try a statement
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    String line = "";
    System.out.print(prompt);
    System.out.flush();
    while ((line = reader.readLine()) != null)
    {
      StringBuffer cmd = new StringBuffer();
      while (!line.equals("") && !line.endsWith(";") && !line.endsWith("/"))
      {
        if (!line.startsWith("#") && !line.startsWith("--"))
        {
          cmd.append(line);
          cmd.append(" ");
        }
        System.out.print(morePrompt);
        System.out.flush();
        line = reader.readLine();
      }
      if (!line.equals("") && !line.startsWith("#") && !line.startsWith("--"))
        cmd.append(line.substring(0, line.length() -1));
      executeCommand(cmd.toString());
      System.out.print(prompt);
      System.out.flush();
    }
  }

  private void executeCommand(String line)
  throws IOException
  {
      if (line.equals("/"))
      {
        line = lastCommandLine;
        System.out.println("");
        System.out.println("Executing last command:");
        System.out.println("  " + lastCommandLine);
        System.out.println("");
      }

      if (line.startsWith("!"))
      {
        int index = 0;
        try
        {
          index = Integer.parseInt(line.substring(1));
          line = getHistoryCommand(index);
          System.out.println("");
          System.out.println("Executing command from history:");
          System.out.println("  " + line);
          System.out.println("");
        }
        catch (Exception x)
        {
          System.out.println("What?");
          line = "";
        }
      }

      if (!line.equals(""))
      {
        lastCommandLine = line;
        addToHistory(line);
        try
        {
          StringTokenizer st = new StringTokenizer(line);
          String commandName = st.nextToken();
          if (commandName.equals("connect"))
          {
            // different handling here for "connect" because
            // some drivers put spaces into their URLs.  Bah!
            long time = System.currentTimeMillis();
            handleCommand_connect(line.substring(7).trim());
            time = System.currentTimeMillis() - time;
            System.out.println("Command took " + time + "ms");
          }
          else
          {
            Class c = this.getClass();
            Class params[] = new Class[1];
            Vector args = new Vector();
            String s = null;
            while (st.hasMoreTokens())
            {
              args.addElement(st.nextToken());
            }
            try
            {
              params[0] = Class.forName("java.util.Vector");
            }
            catch (ClassNotFoundException e)
            {
              throw new InternalError("Can't find java.util.Vector! Doh!");
            }
            Method method = null;
            try
            {
              method = c.getMethod("handleCommand_" + commandName, params);
              System.out.println("");
            }
            catch (Exception e)
            {
              handleCommandDefault(commandName, args);
              return;
            }
            Object methodArguments[] = new Object[1];
            try
            {
              methodArguments[0] = args;
              long time = System.currentTimeMillis();
              method.invoke(this, methodArguments);
              time = System.currentTimeMillis() - time;
              System.out.println("Command took " + time + "ms");
            }
            catch (InvocationTargetException e)
            {
              System.out.println("Exception calling method:");
              Throwable x = e.getTargetException();

              if (showStackTrace)
                x.printStackTrace();
              else
                System.out.println("Exception: " + x);
              System.out.println("");
              return;
            }
          }
        }
        catch (Exception e)
        {
          if (showStackTrace)
            e.printStackTrace();
          else
            System.out.println("Exception: " + e);
        }
      }
    }

  private void handleCommandDefault(String cmdName, Vector args)
  throws SQLException
  {
    System.out.println("Executing sql");
    if (checkConnected())
    {
      String line = cmdName;

      boolean query = false;
      if (cmdName.equalsIgnoreCase("select"))
        query = true;

      Enumeration e = args.elements();
      while (e.hasMoreElements())
      {
        line += " " + e.nextElement();
      }

      PreparedStatement s = connection.prepareStatement(line);
      long time = System.currentTimeMillis();

      ResultSet r = null;
      int rows = 0;

      if (query)
        r = s.executeQuery();
      else
        rows = s.executeUpdate();

      time = System.currentTimeMillis() - time;
      if (query)
      {
        System.out.println("PreparedStatement.executeQuery() took " + time + "ms");
        ResultSetUtil.formatResultSet(r, System.out, showQuotes);
      }
      else
      {
        System.out.println("PreparedStatement.executeUpdate() took " + time + "ms");
        System.out.println("Affected " + rows + " row" + (rows == 1 ? "" : "s"));
      }
      if (r != null)
        r.close();
      if (s != null)
        s.close();
    }
  }

  public void handleCommand_quit(Vector args)
  {
    if (connection != null)
    {
      try
      {
        System.out.println("Disconnecting...");
        connection.close();
      }
      catch (Exception x)
      {
        System.out.println("Exception closing connection...");
        if (showStackTrace)
          x.printStackTrace();
        else
          System.out.println("Exception: " + x);
      }
    }
    System.out.println("See ya, sucker!");
    System.exit(0);
  }

  public void handleCommand_disconnect(Vector args)
  throws Exception
  {
    if (!checkConnected())
      return;
    connection.close();
    connection = null;
    this.url = null;
    this.props = null;
  }

  public void handleCommand_commit(Vector args)
  throws Exception
  {
    if (!checkConnected())
      return;
    connection.commit();
  }

  public void handleCommand_rollback(Vector args)
  throws Exception
  {
    if (!checkConnected())
      return;
    connection.rollback();
  }

  public void handleCommand_driver(Vector args)
  throws Exception
  {
    try
    {
      driver = (Driver)Class.forName((String)args.firstElement()).newInstance();
    }
    catch (Exception x)
    {
      if (showStackTrace)
        x.printStackTrace();
      else
        System.out.println("Exception: " + x);
      throw x;
    }
  }

  public void handleCommand_connect(String args)
  throws Exception
  {
    if (!checkDriver()) return;
    try
    {
      // the argument should be:
      //
      //  URL [username [password [key=val ...]]]
      //
      if (args.equals(""))
      {
        System.out.println("Usage:  connect URL [username [password [key=val ...]]]");
        System.out.println("  You can use double-quotes to quote things.");
        System.out.println("");
        return;
      }

      String url = null;
      String user = null;
      String pass = null;
      Properties props = new Properties();

      // parse things out and pay attention to double quotes.
      Vector list = new Vector();
      boolean inQuote = false;
      StringBuffer currString = new StringBuffer();
      for (int i=0; i<args.length(); i++)
      {
        char c = args.charAt(i);
        if (c == '"')
        {
          if (inQuote)
          {
            list.add(currString.toString());
            currString = new StringBuffer();
            inQuote = false;
          }
          else
          {
            inQuote = true;
          }
        }
        else if (c == ' ')
        {
          if (inQuote)
          {
            currString.append(c);
          }
          else
          {
            if (currString.length() != 0)
            {
              list.add(currString.toString());
              currString = new StringBuffer();
            }
          }
        }
        else
        {
          currString.append(c);
        }
      }
      if (currString.length() != 0)
        list.add(currString.toString());


      Enumeration e = list.elements();
      if (e.hasMoreElements())
        url = (String)e.nextElement();
      if (e.hasMoreElements())
        user = (String)e.nextElement();
      if (e.hasMoreElements())
        pass = (String)e.nextElement();

      Properties p = new Properties();
      if (user != null)
        p.put("user", user);
      if (pass != null)
        p.put("password", pass);
      while (e.hasMoreElements())
      {
        String tok = (String)e.nextElement();
        StringTokenizer st = new StringTokenizer(tok, "=");
        p.put(st.nextToken(), st.nextToken());
      }

      System.out.println("Connecting....");
      System.out.println("  URL:  \"" + url + "\"");
      System.out.println("  Properties:");
      Enumeration en = p.keys();
      int width = 0;
      while (en.hasMoreElements())
      {
        String key = (String)en.nextElement();
        if (key.length() > width)
          width = key.length();
      }

      en = p.keys();
      while (en.hasMoreElements())
      {
        String key = (String)en.nextElement();
        String val = p.getProperty(key);
        int size = key.length();
        System.out.print("    ");
        System.out.print(key);
        for (int i=0; i<(width-size); i++)
          System.out.print(" ");
        System.out.println(" = " + val);
      }

      this.url = url;
      this.props = p;
      connection = DriverManager.getConnection(url, p);
    }
    catch (Exception x)
    {
      if (showStackTrace)
        x.printStackTrace();
      else
        System.out.println("Exception: " + x);
      throw x;
    }
  }

  private String getIsolationString(int isolation)
  {
    switch (isolation)
    {
      case Connection.TRANSACTION_NONE:
        return "None";
      case Connection.TRANSACTION_READ_COMMITTED:
        return "Read Committed";
      case Connection.TRANSACTION_READ_UNCOMMITTED:
        return "Read Uncommitted";
      case Connection.TRANSACTION_REPEATABLE_READ:
        return "Repeatable Read";
      case Connection.TRANSACTION_SERIALIZABLE:
        return "Serializable";
    }
    return "Unknown";
  }

  public void handleCommand_isolation(Vector args)
  throws SQLException
  {
    if (!checkConnected())
      return;

    if (args.size() == 1)
    {
      String l = (String)args.elementAt(0);
      if (l.equalsIgnoreCase("none"))
        connection.setTransactionIsolation(Connection.TRANSACTION_NONE);
      else if (l.equalsIgnoreCase("read_committed"))
        connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
      else if (l.equalsIgnoreCase("read_uncommitted"))
        connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
      else if (l.equalsIgnoreCase("repeatable_read"))
        connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
      else if (l.equalsIgnoreCase("serializable"))
        connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
      else
      {
        System.out.println("  You must specify one of the following levels:");
        System.out.println("    NONE");
        System.out.println("    READ_COMMITTED");
        System.out.println("    READ_UNCOMMITTED");
        System.out.println("    REPEATABLE_READ");
        System.out.println("    SERIALIZABLE");
        System.out.println("");
      }
    }
    System.out.println("Current isolation level: "
      + getIsolationString(connection.getTransactionIsolation()));
    System.out.println("");
  }

  public void handleCommand_metadata(Vector args)
  throws SQLException
  {
    if (!checkConnected())
      return;
    DatabaseMetaData m = connection.getMetaData();
    System.out.println("Connection Class name     = " + connection.getClass().getName());
    System.out.println("Driver Name               = " + m.getDriverName());
    System.out.println("Driver Version            = " + m.getDriverVersion());
    System.out.println("Database Product Name     = " + m.getDatabaseProductName());
    System.out.println("Database Product Version  = " + m.getDatabaseProductVersion());

    System.out.println("");
    System.out.println("Default Isolation  = " + getIsolationString(m.getDefaultTransactionIsolation()));

    System.out.println("");
    System.out.println("Maximum length for:");
    System.out.println("  Catalog name     = " + m.getMaxCatalogNameLength());
    System.out.println("  Schema name      = " + m.getMaxSchemaNameLength());
    System.out.println("  Table name       = " + m.getMaxTableNameLength());
    System.out.println("  Column name      = " + m.getMaxColumnNameLength());
    System.out.println("  Procedure name   = " + m.getMaxProcedureNameLength());
    System.out.println("  Binary literal   = " + m.getMaxBinaryLiteralLength());
    System.out.println("");
    System.out.println("Other misc maximums:");
    System.out.println("  Columns in table = " + m.getMaxColumnsInTable());
    System.out.println("  Connections      = " + m.getMaxConnections());
    System.out.println("  Row size         = " + m.getMaxRowSize());
    System.out.println("  Open statements  = " + m.getMaxStatements());
    System.out.println("");
    System.out.println("ANSI Support declared:");
    System.out.println("  Entry-Level SQL92:              " + m.supportsANSI92EntryLevelSQL());
    System.out.println("  Full SQL92:                     " + m.supportsANSI92FullSQL());
    System.out.println("  Intermediate SQL92:             " + m.supportsANSI92IntermediateSQL());
    System.out.println("  Integrity Enhancement Facility: " + m.supportsIntegrityEnhancementFacility());
    System.out.println("");
  }

  public void handleCommand_catalogs(Vector args)
  throws SQLException
  {
    if (!checkConnected()) return;
    ResultSet r = connection.getMetaData().getCatalogs();
    System.out.println("Available catalogs:");
    String current = connection.getCatalog();
    while (r.next())
    {
      String cname = r.getString(1);
      System.out.println("  " + cname + (cname.equals(current) ? "   <== current catalog" : ""));
    }
    r.close();
    System.out.println("");
  }

  public void handleCommand_catalog(Vector args)
  throws SQLException
  {
    if (!checkConnected()) return;
    if (args.size() != 1)
    {
      System.out.println("Usage: catalog <catalogname>");
    }
    else
    {
      connection.setCatalog((String)args.firstElement());
      System.out.println("Changed catalogs to " + args.firstElement());
    }
  }

  public void handleCommand_schema(Vector args)
  throws SQLException
  {
    if (!checkConnected()) return;
    if (args.size() != 1)
    {
      System.out.println("Usage: schema <schema>");
      System.out.println("Current default schema is \"" + defaultSchema + "\"");
    }
    else
    {
      String schema = (String)args.firstElement();
      if ("null".equals(schema))
          defaultSchema = null;
      else
          defaultSchema = schema;
      System.out.println("Changed default schema to " + args.firstElement());
    }
  }

  public void handleCommand_schemas(Vector args)
  throws SQLException
  {
    if (!checkConnected()) return;
    if (args.size() != 0)
    {
      System.out.println("Usage: schemas");
      return;
    }

    ResultSet r = connection.getMetaData().getSchemas();
    while (r.next())
    {
      System.out.println("  " + r.getString("TABLE_SCHEM"));
    }
    System.out.println("");
    r.close();
  }

  public void handleCommand_procedures(Vector args)
  throws SQLException
  {
    if (!checkConnected()) return;
    String catalog = connection.getCatalog();
    if (args.size() > 1)
    {
      System.out.println("Usage: procedures [<schemaname>]");
      return;
    }
    String schema = null;
    if (args.size() == 1)
    {
      schema = (String)args.firstElement();
    }

    ResultSet r = connection.getMetaData().getProcedures(catalog, schema, null);
    System.out.println("Schema             Procedure");
    System.out.println("-------------------------------------------------");
    //                    xxxxxxxxxxxxxxxx xxxxxxxxxxxxx...
    while (r.next())
    {
      System.out.println("  " +
        format(r.getString("PROCEDURE_SCHEM"), 16) + " " +
        r.getString("PROCEDURE_NAME"));
    }
    r.close();
    System.out.println("");
  }

  public void handleCommand_tables(Vector args)
  throws SQLException
  {
    if (!checkConnected()) return;
    String catalog = connection.getCatalog();
    String schema = null;
    String tablename = null;
    if (args.size() > 0)
    {
      schema = (String)args.firstElement();
    }
    if (args.size() > 1)
    {
      tablename = ((String)args.elementAt(1)).toUpperCase();
    }

    ResultSet r = connection.getMetaData().getTables(catalog, schema, null, null);
    //System.out.println(ResultSetUtil.formatResultSet(r));

    System.out.println("Schema             Tablename");
    System.out.println("-------------------------------------------------");
    //                    xxxxxxxxxxxxxxxx xxxxxxxxxxxxx...
    while (r.next())
    {
      String name = r.getString("TABLE_NAME");
      if (tablename == null || (name.toUpperCase().indexOf(tablename) != -1))
        System.out.println("  " + format(r.getString("TABLE_SCHEM"), 16) + " " + name);
    }
    r.close();

    System.out.println("");
  }

  public void handleCommand_indexes(Vector args)
  throws SQLException
  {
    if (!checkConnected()) return;
    String catalog = connection.getCatalog();
    if (args.size() != 1)
    {
      System.out.println("Usage: indexes [<schema>.]<tablename>");
      return;
    }
    String table = (String)args.firstElement();
    String schema = null;
    if (table.indexOf(".") != -1)
    {
      StringTokenizer st = new StringTokenizer(table, ".");
      schema = st.nextToken();
      table = st.nextToken();
    }

    ResultSet r = connection.getMetaData().getIndexInfo(catalog, schema, table, false, true);
    boolean first = true;
    while (r.next())
    {
      if (first)
      {
        System.out.println("Schema:  " + r.getString("TABLE_SCHEM"));
        System.out.println("Table:   " + r.getString("TABLE_NAME"));
        first = false;
      }

      String indexName = r.getString("INDEX_NAME");
      boolean nonUnique = r.getBoolean("NON_UNIQUE");
      String qualifier = r.getString("INDEX_QUALIFIER");
      short indexType = r.getShort("TYPE");
      short position = r.getShort("ORDINAL_POSITION");
      String column = r.getString("COLUMN_NAME");
      String ascDesc = r.getString("ASC_OR_DESC");
      int cardinality = r.getInt("CARDINALITY");
      int pages = r.getInt("PAGES");
      String filter = r.getString("FILTER_CONDITION");

      System.out.println("  Index name:          " + indexName);
      System.out.println("    Non unique vals?:  " + nonUnique);
      System.out.println("    Column:            " + column);
      System.out.println("    Qualifier:         " + qualifier);
      System.out.print("    Type:              ");
      switch (indexType)
      {
        case DatabaseMetaData.tableIndexStatistic:
          System.out.println("Statistic");
          break;
        case DatabaseMetaData.tableIndexClustered:
          System.out.println("Clustered");
          break;
        case DatabaseMetaData.tableIndexHashed:
          System.out.println("Hashed");
          break;
        case DatabaseMetaData.tableIndexOther:
          System.out.println("Other");
          break;
        default:
          System.out.println("Unknown");
      }
      System.out.println("    Positon:           " + position);
      System.out.println("    Asc/Desc:          " + ascDesc);
      System.out.println("    Cardinality:       " + cardinality);
      System.out.println("    Pages:             " + pages);
      System.out.println("    Filter:            " + filter);
      System.out.println("");
    }

    if (first) // no rows
    {
      System.out.println("  Table \"" + table + "\" does not appear to have any indexes.");
      System.out.println("");
    }

    r.close();
  }

  public void handleCommand_run(Vector args)
  throws SQLException, IOException
  {
    if (args.size() != 1)
    {
      System.out.println("Usage: run <filename>");
      System.out.println("  (executes commands from the given file)");
      return;
    }
    String file = (String)args.elementAt(0);
    System.out.println("Processing commands from file:");
    System.out.println("  file=" + file);
    BufferedReader r = new BufferedReader(new FileReader(new File(file)));
    String line = "";
    while ((line = r.readLine()) != null)
    {
      StringBuffer cmd = new StringBuffer();
      while (!line.equals("") && !line.endsWith(";") && !line.endsWith("/"))
      {
        if (!line.startsWith("#") && !line.startsWith("--"))
        {
          cmd.append(line);
          cmd.append(" ");
        }
        line = r.readLine();
      }
      if (!line.equals(""))
        cmd.append(line.substring(0, line.length() -1));
      if (!cmd.toString().startsWith("#") && !cmd.toString().startsWith("--") && !cmd.toString().trim().equals(""))
      {
        System.out.println("  cmd=\"" + cmd + "\"");
        executeCommand(cmd.toString());
      }
    }
    r.close();
  }

  public void handleCommand_desc(Vector args)
  throws SQLException
  {
    if (!checkConnected()) return;
    String catalog = connection.getCatalog();
    if (args.size() != 1)
    {
      System.out.println("Usage: desc [<schema>.]<tablename>");
      return;
    }
    String table = (String)args.firstElement();
    String schema = defaultSchema;
    if (table.indexOf(".") != -1)
    {
      StringTokenizer st = new StringTokenizer(table, ".");
      schema = st.nextToken();
      table = st.nextToken();
    }

    // get PK info
    Hashtable pks = new Hashtable();
    ResultSet r = connection.getMetaData().getPrimaryKeys(catalog, schema, table);
    while (r.next())
      pks.put(r.getString("COLUMN_NAME"), "blah");
    r.close();

    r = connection.getMetaData().getColumns(catalog, schema, table, null);
    //System.out.println(ResultSetUtil.formatResultSet(r));
    boolean first = true;
    while (r.next())
    {
      if (first)
      {
        if (schema == null)
        {
            schema = r.getString("TABLE_SCHEM");
        }
        System.out.println("Schema:  " + r.getString("TABLE_SCHEM"));
        System.out.println("Column Name                       Type          Size            Null? PK");
        System.out.println("------------------------------------------------------------------------");
        //                     xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx xxxxxxxxxxxxx xxxxxxxxxxxxxxx xxxxx xx
        //                     30                             13            15              5
        first = false;
      }
      String colSchema = r.getString("TABLE_SCHEM");
      if ((schema == null) || (schema != null && schema.equals(colSchema)))
      {
        String colName = r.getString("COLUMN_NAME");
        System.out.print(
          "  " +
          format(colName, 30) + "  " +
          format(r.getString("TYPE_NAME"), 13) + " " +
          format(String.valueOf(r.getInt("COLUMN_SIZE")), 15) + " " +
          format(r.getString("IS_NULLABLE"), 6)
          );
        if (pks.containsKey(colName))
          System.out.println("Y");
        else
          System.out.println("");
      }
    }

    if (first) // no rows
    {
      System.out.println("  Table \"" + table + "\" does not appear to exist.");
    }

    r.close();
    System.out.println("");
  }

  public void handleCommand_fkdesc(Vector args)
  throws SQLException
  {
    if (!checkConnected()) return;
    String catalog = connection.getCatalog();
    if (args.size() != 1)
    {
      System.out.println("Usage: fkdesc [<schema>.]<tablename>");
      return;
    }
    String table = (String)args.firstElement();
    String schema = defaultSchema;
    if (table.indexOf(".") != -1)
    {
      StringTokenizer st = new StringTokenizer(table, ".");
      schema = st.nextToken();
      table = st.nextToken();
    }

    DatabaseMetaData md = connection.getMetaData();

    System.out.println("Exported keys:");
    ResultSet fkr = md.getCrossReference(catalog, schema, table, null, null, null);
    while (fkr.next())
    {
      System.out.print("  " + fkr.getString("PKCOLUMN_NAME"));
      System.out.print(" is referenced by ");
      System.out.println(fkr.getString("FKTABLE_NAME") + "." + fkr.getString("FKCOLUMN_NAME"));
    }
    System.out.println("");
    fkr.close();


    System.out.println("Imported keys:");
    fkr = md.getCrossReference(null, null, null, catalog, schema, table);
    while (fkr.next())
    {
      System.out.print("  " + fkr.getString("FKCOLUMN_NAME"));
      System.out.print(" references ");
      System.out.println(fkr.getString("PKTABLE_NAME") + "." + fkr.getString("PKCOLUMN_NAME"));
    }
    System.out.println("");
    fkr.close();
  }

  public void handleCommand_jdesc(Vector args)
  throws SQLException
  {
    if (!checkConnected()) return;
    String catalog = connection.getCatalog();
    if (args.size() != 1)
    {
      System.out.println("Usage: jdesc [<schema>.]<tablename>");
      return;
    }
    String table = (String)args.firstElement();
    String schema = defaultSchema;
    if (table.indexOf(".") != -1)
    {
      StringTokenizer st = new StringTokenizer(table, ".");
      schema = st.nextToken();
      table = st.nextToken();
    }

    // get PK info
    Hashtable pks = new Hashtable();
    ResultSet r = connection.getMetaData().getPrimaryKeys(catalog, schema, table);
    while (r.next())
      pks.put(r.getString("COLUMN_NAME"), "blah");
    r.close();

    r = connection.getMetaData().getColumns(catalog, schema, table, null);
    boolean first = true;
    while (r.next())
    {
      if (first)
      {
        if (schema == null)
        {
            schema = r.getString("TABLE_SCHEM");
        }
        //System.out.println(ResultSetUtil.formatResultSet(r));
        System.out.println("Schema:  " + r.getString("TABLE_SCHEM"));
        System.out.println("Column Name                       java.sql.Types   Size         Null? PK");
        System.out.println("------------------------------------------------------------------------");
        //                     xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx xxxxxxxxxxxxxxxx xxxxxxxxxxxx xxxxx xx
        //                     30                             16               12               5
        first = false;
      }
      String colSchema = r.getString("TABLE_SCHEM");

      if ((schema == null) || (schema.equals(colSchema)))
      {
        String nullable = r.getString("IS_NULLABLE");
        if (nullable.equalsIgnoreCase("YES"))
          nullable = "YES";
        else
          nullable = "NO";
        String colName = r.getString("COLUMN_NAME");
        System.out.print(
          "  " +
          format(colName, 30) + "  " +
          format(getType(r.getInt("DATA_TYPE")), 16) + " " +
          format(String.valueOf(r.getInt("COLUMN_SIZE")), 12) + " " +
          format(nullable, 6)
          );
        if (pks.containsKey(colName))
          System.out.println("Y");
        else
          System.out.println("");
      }
    }

    if (first) // no rows
    {
      System.out.println("  Table \"" + table + "\" does not appear to exist.");
    }

    r.close();
    System.out.println("");
  }

  public void handleCommand_driverdata(Vector args)
  throws SQLException
  {
    if (!checkDriver()) return;
    System.out.println("Class name:           " + driver.getClass().getName());
    System.out.println("Driver Major Version: " + driver.getMajorVersion());
    System.out.println("Driver Minor Version: " + driver.getMinorVersion());
  }

  public void handleCommand_about(Vector args)
  {
    System.out.println("");
    System.out.println("SimpleListener -- a (very) simple SQL tool");
    System.out.println("");
  }

  private boolean checkDriver()
  {
    if (driver == null)
    {
      System.out.println("No driver loaded -- use \"driver drivername\"");
      return false;
    }
    return true;
  }

  private boolean checkConnected()
  {
    if (!checkDriver())
      return false;
    else if (connection == null)
    {
      System.out.println("Not connected -- use \"connect\"");
      return false;
    }
    return true;
  }

  private String format(String s, int length)
  {
    StringBuffer b = new StringBuffer(length);
    b.append(s);
    for (int i=0; i<length-s.length(); i++)
      b.append(" ");
    return b.toString();
  }

  public void handleCommand_h(Vector args)
  {
    handleCommand_history(args);
  }

  public void handleCommand_history(Vector args)
  {
    if (args.size() > 0)
    {
      try
      {
        historySize = Integer.parseInt((String)args.elementAt(0));
        trimHistory();
      }
      catch (Exception x) { ; }
    }
    System.out.println("Command history (max size = " + historySize + "):");
    System.out.println("");
    Enumeration e = history.elements();
    int index = 0;

    while (e.hasMoreElements())
    {
      System.out.println("  " + format(String.valueOf(index), 2) + ": " + e.nextElement());
      index++;
    }
    System.out.println("");
    System.out.println("  Type \"!X\" to execute command number X.");
    System.out.println("");
    System.out.println("  Type \"history n\" to set the length.");
    System.out.println("");
  }

  public void handleCommand_help(Vector args)
  {
    System.out.println("");
    System.out.println("Protomatter SimpleListener -- a simple SQL client.");
    System.out.println("  version " + Protomatter.VERSION);
    System.out.println("");
    System.out.println("Available commands: (in addition to normal SQL)");
    System.out.println("  about");
    System.out.println("  driver <drivername>");
    System.out.println("  connect <URL> [<username> [<password> [ key=val ... key=val ]]]");
    System.out.println("    (note: double-quotes are interpreted as you expect in \"connect\")");
    System.out.println("  disconnect");
    System.out.println("  commit");
    System.out.println("  rollback");
    System.out.println("  help");
    System.out.println("  metadata");
    System.out.println("  driverdata");
    System.out.println("  connectioninfo");
    System.out.println("  autocommit [on|off]");
    System.out.println("  showstacktrace [on|off]");
    System.out.println("  showquotes [on|off]");
    System.out.println("  isolation [level]");
    System.out.println("  run <filename>");
    System.out.println("  schemas");
    System.out.println("  schema <schema>");
    System.out.println("  catalogs");
    System.out.println("  catalog <catalogname>");
    System.out.println("  tables [<schema> [<table_name_substring>]]");
    System.out.println("  procedures [<schema>]");
    System.out.println("  indexes [<schema>.]<tablename>");
    System.out.println("  desc [<schema>.]<tablename>");
    System.out.println("  jdesc [<schema>.]<tablename> (describe table in terms of Java types)");
    System.out.println("  fkdesc [<schema>.]<tablename> (describe foreign keys on table)");
    System.out.println("  history ('h' works too) (display command history)");
    System.out.println("  / (executes last command again)");
    System.out.println("  quit");
    System.out.println("");
    System.out.println(" Note:  commands can be spread over multiple lines");
    System.out.println("        if the last character on each line is a \"\\\"");
    System.out.println("");
  }

  public void handleCommand_connectioninfo(Vector args)
  throws SQLException
  {
    if (checkConnected())
    {
      System.out.println("");
      System.out.println("Connection information:");
      System.out.println("  URL:      " + this.url);
      System.out.println("  Properties:");
      int maxlen = 0;
      Enumeration e = this.props.keys();
      while (e.hasMoreElements())
      {
        String key = (String)e.nextElement();
        if (key.length() > maxlen)
          maxlen = key.length();
      }

      e = this.props.keys();
      while (e.hasMoreElements())
      {
        String key = (String)e.nextElement();
        System.out.println("    " + format(key, maxlen) + " = " + this.props.get(key));
      }
      System.out.println("");
      System.out.println("  Isolation level:  " + getIsolationString(connection.getTransactionIsolation()));
      System.out.println("  Read Only:        " + connection.isReadOnly());
      System.out.println("  Auto Commit:      " + connection.getAutoCommit());
      System.out.println("");
    }
    else
    {
      System.out.println("");
      System.out.println("You are not connected to a database at this time.");
      System.out.println("");
    }
  }

  public void handleCommand_autocommit(Vector args)
  throws SQLException
  {
    if (checkConnected())
    {
      boolean b = false;
      if (args.isEmpty())
      {
        b = connection.getAutoCommit();
      }
      else
      {
        String value = (String)args.firstElement();
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("on"))
          b = true;
        connection.setAutoCommit(b);
      }
      System.out.println("Auto-Commit set to " + b);
      System.out.println("");
    }
    else
    {
      System.out.println("");
      System.out.println("You are not connected to a database at this time.");
      System.out.println("");
    }
  }

  public void handleCommand_showstacktrace(Vector args)
  throws SQLException
  {
    boolean b = false;
    if (args.isEmpty())
    {
      b = showStackTrace;
    }
    else
    {
      String value = (String)args.firstElement();
      if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("on"))
        b = true;
      showStackTrace = b;
    }
    System.out.println("Show stack trace set to " + b);
    System.out.println("");
  }

  public void handleCommand_showquotes(Vector args)
  throws SQLException
  {
    boolean b = false;
    if (args.isEmpty())
    {
      b = showQuotes;
    }
    else
    {
      String value = (String)args.firstElement();
      if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("on"))
        b = true;
      showQuotes = b;
    }
    System.out.println("Show quotes set to " + b);
    System.out.println("");
  }

  private static final String getType(int type)
  {
    // look for a type of that value on the Types object
    try
    {
      Field fields[] = Types.class.getFields();
      for (int i=0; i<fields.length; i++)
      {
        Number n = (Number)fields[i].get(null);
        int val = n.intValue();
        if (val == type)
          return fields[i].getName();
      }
    }
    catch (Exception x)
    {
      ;
    }
    return null;
  }

  private void addToHistory(String command)
  {
    if (!history.contains(command))
      history.addElement(command);
    trimHistory();
  }

  private String getHistoryCommand(int index)
  {
    return (String)history.elementAt(index);
  }

  private void trimHistory()
  {
    if (history.size() > historySize)
    {
      int remove = history.size() - historySize;
      for (int i=0; i<remove; i++)
        history.removeElementAt(i);
    }
  }
}
