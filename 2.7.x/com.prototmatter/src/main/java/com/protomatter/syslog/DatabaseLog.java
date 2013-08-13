package com.protomatter.syslog;

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
import java.net.*;
import java.sql.*;
import java.util.*;
import java.text.*;

import com.protomatter.util.*;

/**
 *  A logger that writes to a database.  The tables it requires are:<P>
 *
 *  <BLOCKQUOTE><PRE>
 *  CREATE TABLE SYSLOG_LEVEL
 *  (
 *    SYSLOG_LEVEL         INT           PRIMARY KEY,
 *    SYSLOG_LEVEL_NAME    VARCHAR(8)    NOT NULL
 *  )
 *
 *  CREATE TABLE SYSLOG_CHANNEL
 *  (
 *    CHANNEL       INT           PRIMARY KEY,
 *    CHANNEL_NAME  VARCHAR(128)  NOT NULL
 *  )
 *
 *  CREATE TABLE SYSLOG_LOG
 *  (
 *    LOG_TIME      DATETIME      NOT NULL,
 *    SYSLOG_LEVEL  INT           NOT NULL
 *      REFERENCES SYSLOG_LEVEL(SYSLOG_LEVEL),
 *    HOST          VARCHAR(32)   NOT NULL,
 *    LOGGER        VARCHAR(64)   NOT NULL,
 *    CHANNEL       INT           NOT NULL
 *      REFERENCES SYSLOG_CHANNEL(CHANNEL),
 *    MESSAGE       VARCHAR(255)  NOT NULL,
 *    DETAIL        LONG          NULL,
 *    THREAD_NAME   VARCHAR(255)  NULL
 *  )
 *  </PRE></BLOCKQUOTE><P>
 *
 *  NOTE:  To use this logger you must specify the "<tt>hostname</tt>"
 *  attribute for the <TT>&lt;Syslog&gt;</TT> tag in your configuration
 *  so that the hostname is correctly recorded.
 *  <P>
 *
 *  Column widths are defined as follows:<P>
 *  <DL>
 *    <DT><TT>HOST</TT></DT>
 *    <DD><TT>getHostWidth()</TT> on format class.</DD><P>
 *
 *    <DT><TT>LOGGER</TT></DT>
 *    <DD><TT>getClassWidth()</TT> on format class.</DD><P>
 *
 *    <DT><TT>THREAD_NAME</TT></DT>
 *    <DD><TT>getThreadWidth()</TT> on format class.</DD><P>
 *
 *    <DT><TT>MESSAGE</TT></DT>
 *    <DD><TT>getMessageWidth()</TT> on this class.</DD><P>
 *
 *    <DT><TT>DETAIL</TT></DT>
 *    <DD><TT>getDetailWidth()</TT> on this class.</DD><P>
 *  </DL>
 *  <P>
 *
 *  You may add a prefix to the names of each of the tables and specify
 *  that with the "<tt>jdbc.tablePrefix</tt>" property when initializing
 *  this logger.  The default prefix is nothing.<P>
 *
 *  You may have to change the type for <tt>SYSLOG_LOG.DETAIL</tt>
 *  to be something different for your database
 *  (<TT>VARCHAR2</TT>, <tt>LONG VARCHAR</tt> or <tt>LONG RAW</tt>, etc).
 *  It is written using an adapter class which implements
 *  the {@link DatabaseLogStatementAdapter DatabaseLogStatementAdapter}
 *  interface.  Currently there are adapters for drivers which
 *  use the {@link StringDatabaseLogStatementAdapter setString()} ,
 *  {@link AsciiStreamDatabaseLogStatementAdapter setAsciiStream()}, or
 *  {@link CharacterStreamDatabaseLogStatementAdapter setCharacterStream()}
 *  methods.<P>
 *
 *  The <tt>SYSLOG_LEVEL</tt> table needs to contain the following rows:<P>
 *
 *  <UL>
 *  <TABLE BORDER=1 CELLPADDING=2 CELLSPACING=0>
 *  <TR CLASS="TableHeadingColor">
 *  <TD><B>SYSLOG_LEVEL</B></TD>
 *  <TD><B>SYSLOG_LEVEL_NAME</B></TD>
 *  </TR>
 *
 *  <TR CLASS="TableRowColor">
 *  <TD VALIGN=TOP><tt>1</tt></TD>
 *  <TD VALIGN=TOP><TT>DEBUG</TT></TD>
 *  </TR>
 *
 *  <TR CLASS="TableRowColor">
 *  <TD VALIGN=TOP><tt>2</tt></TD>
 *  <TD VALIGN=TOP><TT>INFO</TT></TD>
 *  </TR>
 *
 *  <TR CLASS="TableRowColor">
 *  <TD VALIGN=TOP><tt>4</tt></TD>
 *  <TD VALIGN=TOP><TT>WARNING</TT></TD>
 *  </TR>
 *
 *  <TR CLASS="TableRowColor">
 *  <TD VALIGN=TOP><tt>8</tt></TD>
 *  <TD VALIGN=TOP><TT>ERROR</TT></TD>
 *  </TR>
 *
 *  <TR CLASS="TableRowColor">
 *  <TD VALIGN=TOP><tt>16</tt></TD>
 *  <TD VALIGN=TOP><TT>FATAL</TT></TD>
 *  </TR>
 *
 *  </TABLE>
 *  </UL><P>
 *
 *  The <tt>SYSLOG_CHANNEL</tt> table is populated automatically.  Channel
 *  IDs start at 1 and go up from there.
 *
 *  @see com.protomatter.syslog.xml.DatabaseLog_Helper XML configuration class
 */
public class DatabaseLog
extends BasicLogger
{
  private String driver = null;
  private String url = null;
  private Properties props = null;

  private DatabaseLogStatementAdapter statementAdapter = null;

  private int numRetries = 3;

  private Connection conn = null;

  private PreparedStatement stmt = null;

  // lengths... so we can truncate text.
  private int messageWidth = 255;
  private int detailWidth = 4000;

  private String tablePrefix = "";

  // channels we know are listed in the SYSLOG_CHANNEL
  // table.  Key is channel name, value is an Integer.
  private Hashtable knownChannels = new Hashtable();

  private String INSERT_PREFIX = "INSERT INTO ";

  private String INSERT_LOG_SQL = "SYSLOG_LOG (LOG_TIME, SYSLOG_LEVEL, HOST, LOGGER, CHANNEL, MESSAGE, DETAIL, THREAD_NAME) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

  private String INSERT_CHANNEL_SQL = "SYSLOG_CHANNEL (CHANNEL, CHANNEL_NAME) VALUES (?, ?)";

  private String FIND_CHANNEL_SQL_PREFIX = "SELECT CHANNEL FROM ";
  private String FIND_CHANNEL_SQL = "SYSLOG_CHANNEL WHERE CHANNEL_NAME = ?";

  private String FIND_MAX_CHANNEL_SQL_PREFIX = "SELECT MAX(CHANNEL) FROM ";
  private String FIND_MAX_CHANNEL_SQL = "SYSLOG_CHANNEL";


   /**
    *  You will need to call the configure() method if you use this constructor.
    */
   public DatabaseLog()
   {
     super();

     statementAdapter = new StringDatabaseLogStatementAdapter();
   }

   /**
    *  Close the database connection and cleanup.
    */
   public void shutdown()
   {
     try
     {
       this.conn.close();
     }
     catch (SQLException x)
     {
       // don't really care that there was an error.
     }
   }

   /**
    *  Set the driver to use.
    */
   public void setDriver(String driver)
   {
      this.driver = driver;
   }

   /**
    *  Set the table prefix to use.
    */
   public void setTablePrefix(String tablePrefix)
   {
      this.tablePrefix = tablePrefix;
   }

   /**
    *  Set the JDBC URL.
    */
   public void setURL(String url)
   {
      this.url = url;
   }

   /**
    *  Set the number of retries before failure.
    */
   public void setNumRetries(int retries)
   {
     this.numRetries = retries;
   }

   /**
    *  Set the JDBC connection properties.
    */
   public void setProperties(Properties props)
   {
      this.props = props;
   }

   /**
    *  Set the database statement adapter.
    */
   public void setStatementAdapter(DatabaseLogStatementAdapter adapter)
   {
     this.statementAdapter = adapter;
   }

   /**
    *  Get the database statement adapter.
    */
   public DatabaseLogStatementAdapter getStatementAdapter()
   {
     return this.statementAdapter;
   }

   /**
    *  Get the classname of the driver.
    */
   public String getDriver()
   {
      return this.driver;
   }

   /**
    *  Get the table prefix.
    */
   public String getTablePrefix()
   {
      return this.tablePrefix;
   }

   /**
    *  Get the JDBC URL.
    */
   public String getURL()
   {
      return this.url;
   }

   /**
    *  Get the number of retries before failure.
    */
   public int getNumRetries()
   {
     return this.numRetries;
   }

   /**
    *  Get the JDBC connection properties.
    */
   public Properties getProperties()
   {
      return this.props;
   }

   /**
    *  Set the width of the short message column.
    */
   public void setMessageWidth(int width)
   {
     this.messageWidth = width;
   }

   /**
    *  Get the width of the short message column.
    */
   public int getMessageWidth()
   {
     return this.messageWidth;
   }

   /**
    *  Set the width of the message detail column.
    */
   public void setDetailWidth(int width)
   {
     this.detailWidth = width;
   }

   /**
    *  Get the width of the message detail column.
    */
   public int getDetailWidth()
   {
     return this.detailWidth;
   }

   /**
    *  (re)initialize the connection to the database.
    */
   public void initDatabase()
   throws Exception
   {
     if (stmt != null)
     {
       try { stmt.close(); } catch (Exception x) { ; }
     }
     if (conn != null)
     {
       try { conn.close(); } catch (Exception x) { ; }
     }

     // make sure the driver is registered.
     DatabaseUtil.registerDriver(driver);

     // get a connection
     conn = DriverManager.getConnection(url, props);

     // prepare our insert statement.
     stmt = conn.prepareStatement(INSERT_PREFIX + tablePrefix + INSERT_LOG_SQL);
   }

   private synchronized int ensureChannelExists(String channel, Connection c)
   throws Exception
   {
     SimpleSyslogTextFormatter format = (SimpleSyslogTextFormatter)getTextFormatter();

     String channelName = truncate(channel, format.getChannelWidth());

     Integer id = (Integer)knownChannels.get(channelName);
     if (id != null)
       return id.intValue();

     // see if it's been put in before.
     PreparedStatement s = null;
     ResultSet r = null;

     try
     {
       s = c.prepareStatement(FIND_CHANNEL_SQL_PREFIX + tablePrefix + FIND_CHANNEL_SQL);
       s.setString(1, channelName);
       r = s.executeQuery();
       if (r.next())
       {
         // channel exists, use that ID.
         id = new Integer(r.getInt(1));
         knownChannels.put(channelName, id);
         return id.intValue();
       }

       // we'll need to insert the channel
       r.close();
       s.close();

       // find max channel id
       s = c.prepareStatement(FIND_MAX_CHANNEL_SQL_PREFIX + tablePrefix + FIND_MAX_CHANNEL_SQL);
       r = s.executeQuery();
       int max = 0;
       if (r.next())
         max = r.getInt(1);
       r.close();
       s.close();

       // insert new channel.
       s = c.prepareStatement(INSERT_PREFIX + tablePrefix + INSERT_CHANNEL_SQL);
       s.setInt(1, max +1);
       s.setString(2, channelName);
       s.executeUpdate();

       knownChannels.put(channelName, new Integer(max +1));
       return (max +1);
     }
     finally
     {
       // close up shop
       if (r != null)
       {
         try { r.close(); } catch (Exception x) { ; }
       }
       if (s != null)
       {
         try { s.close(); } catch (Exception x) { ; }
       }
     }
   }

   public final void log(SyslogMessage message)
   {
     log(message, 1, null);
   }

   private final void log(SyslogMessage message, int tries, Exception exception)
   {
     SimpleSyslogTextFormatter format = (SimpleSyslogTextFormatter)getTextFormatter();
     if (tries > numRetries)
     {
       // try a few times and then just give up.
       System.err.println("############################################################");
       System.err.println(MessageFormat.format(
         Syslog.getResourceString(MessageConstants.DATABASELOG_ATTEMPT_MESSAGE_1),
         new Object[] { "DatabaseLog", String.valueOf(numRetries) }));
       System.err.println(MessageFormat.format(
         Syslog.getResourceString(MessageConstants.DATABASELOG_ATTEMPT_MESSAGE_2),
         new Object[] { getName() }));
       System.err.println(MessageFormat.format(
         Syslog.getResourceString(MessageConstants.DATABASELOG_ATTEMPT_MESSAGE_3),
         new Object[] { driver }));
       System.err.println(MessageFormat.format(
         Syslog.getResourceString(MessageConstants.DATABASELOG_ATTEMPT_MESSAGE_4),
         new Object[] { url }));
       System.err.println(MessageFormat.format(
         Syslog.getResourceString(MessageConstants.DATABASELOG_ATTEMPT_MESSAGE_5),
         new Object[] { props }));
       if (exception != null)
       {
         System.err.println("");
         System.err.println(Syslog.getResourceString(MessageConstants.DATABASELOG_ATTEMPT_CAUSE_MESSAGE));
         exception.printStackTrace();
       }
       System.err.println("");
       System.err.println(Syslog.getResourceString(MessageConstants.DATABASELOG_ORIGINAL_MESSAGE));
       StringBuffer buf = new StringBuffer();
       format.formatLogEntry(buf, message);
       System.err.println(buf);
       System.err.println("############################################################");
       return;
     }
     try
     {
       // if our connection is null, statement is null,
       // or we're trying more than once to log this message,
       // re-initialize the database.
       if (conn == null || stmt == null || tries > 1)
         initDatabase();

       int chanID = ensureChannelExists(message.channel, conn);
       String hostname = getHostname(message.host);

       stmt.setTimestamp(1, new Timestamp(message.time));

       stmt.setInt(2, message.level);

       stmt.setString(3, truncate(hostname, format.getHostWidth()));

       stmt.setString(4, truncate(message.loggerClassname, format.getClassWidth()));

       stmt.setInt(5, chanID);

       stmt.setString(6, truncate(message.msg.toString(), messageWidth));

       if (message.detail == null)
       {
         stmt.setNull(7, Types.LONGVARCHAR);
       }
       else
       {
         StringBuffer buffer = new StringBuffer(detailWidth);
         format.formatMessageDetail(buffer, message);
         String messageDetail = truncate(buffer.toString(), detailWidth);
         statementAdapter.handleLogStatement(stmt, messageDetail, 7);
       }

       stmt.setString(8, truncate(message.threadName, format.getThreadWidth()));

       stmt.executeUpdate();
     }
     catch (Exception x)
     {
       log(message, tries +1, x);
     }
   }

   private static final String getHostname(InetAddress host)
   {
     return host.getHostName();
   }

   private static final String truncate(String s, int length)
   {
     return (s.length() > length) ? s.substring(0, length) : s;
   }

  /**
   *  Set the log formatter object used by this logger.  DatabaseLog
   *  makes the extra requirement that this formatter be a subclass
   *  of the <TT>SimpleSyslogTextFormatter</TT> class.
   */
  public void setTextFormatter(SyslogTextFormatter formatter)
  {
    if (!(formatter instanceof SimpleSyslogTextFormatter))
    {
      throw new IllegalArgumentException(MessageFormat.format(
        Syslog.getResourceString(MessageConstants.DATABASELOG_NO_TEXT_FORMATTER_MESSAGE),
        new Object[] { "DatabaseLog", "SimpleSyslogTextFormatter" }));
    }
    super.setTextFormatter(formatter);
  }

   public void flush()
   {
     // do nothing.
   }
}
