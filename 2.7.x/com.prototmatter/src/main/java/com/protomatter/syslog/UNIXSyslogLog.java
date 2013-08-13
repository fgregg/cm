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
import java.text.*;
import java.util.*;

import com.protomatter.util.*;

/**
 *  A logger that sends UDP packets to a UNIX syslog server.
 *  This logger sends UDP packets to UNIX servers running a BSD-style
 *  <tt>syslogd</tt> daemon.  Refer to
 *  <a href="http://www.ietf.org/rfc/rfc3164.txt">RFC 3164</A> for
 *  information about the protocol.<P>
 *
 *  With the default formatting options, a call to <TT>Syslog.info(this, "Hello there")</TT>
 *  will result in the following to be written to <tt>/var/log/messages</tt> (or its
 *  equivalent) on the UNIX box:<P>
 *
 *  <ul><table border=1 cellpadding=4 cellspacing=0>
 *  <tr><td><pre>
 *
 *  Feb 10 16:02:21 sillysailor ProtomatterSyslog: [INFO] MyClass: Hello there &nbsp;
 *  </pre></td></tr>
 *  </table></ul><P>
 *
 *  This assumes you logged from a class called <tt>MyClass</tt>, that you
 *  havn't set the tag to some more meaningful value, and that your hostname
 *  is "<tt>sillysailor</tt>".
 *
 *  @see com.protomatter.syslog.xml.UNIXSyslogLog_Helper XML configuration class
 */
public class UNIXSyslogLog
extends BasicLogger
{
   private static int DEFAULT_PORT = 514;

   private DatagramSocket socket = null;
   private InetAddress address = null;
   private int port = DEFAULT_PORT;
   private int facility = 16; // "Local Use 0" from RFC 3164
   private String tag = Syslog.getResourceString(MessageConstants.UNIX_DEFAULT_TAG);
   private byte[] tagBytes = tag.getBytes();
   private boolean showHostname = false;
   private String messageTemplate = "[{SEVERITY}] {CALLER}: {MESSAGE}";

   public static final String CHANNEL_TOKEN = "{CHANNEL}";
   public static final String FULLCALLER_TOKEN = "{CALLER-FULLNAME}";
   public static final String CALLER_TOKEN = "{CALLER}";
   public static final String MESSAGE_TOKEN = "{MESSAGE}";
   public static final String THREAD_TOKEN = "{THREAD}";
   public static final String SEVERITY_TOKEN = "{SEVERITY}";

   private DateFormat dateFormat = new SimpleDateFormat("MMM dd HH:MM:ss");
   private byte[] cacheDate = null;
   private long cacheTime = 0;
   private static long cacheTimeout = 1000;

   private static int MAX_DATA_LENGTH = 1024; // max packet length from the spec

   private static char SPACE = ' ';
   private static char COLON = ':';
   private static char L_ANGLE = '<';
   private static char R_ANGLE = '>';

   private static SimpleSyslogTextFormatter simpleFormat = null;

   // See RFC 3164 for info on these values
   /** Debug: debug-level messages (RFC 3164). */
   public static final int UNIX_DEBUG = 7;

   /** Informational: informational messages (RFC 3164). */
   public static final int UNIX_INFO = 6;

   /** Notice: normal but significant (RFC 3164). */
   public static final int UNIX_NOTICE = 5;

   /** Warning: warning conditions (RFC 3164). */
   public static final int UNIX_WARNING = 4;

   /** Error: error conditions (RFC 3164). */
   public static final int UNIX_ERROR = 3;

   /** Critical: critical conitions (RFC 3164). */
   public static final int UNIX_CRITICAL = 2;

   /** Alert: take action immediately (RFC 3164). */
   public static final int UNIX_ALERT = 1;

   /** Emergency: system is unusable (RFC 3164). */
   public static final int UNIX_EMERGENCY = 0;

   // map of syslog message severity -> UNIX syslog severity
   private Map severityMap = null;

   /**
    *  The default severity map.
    */
   public static Map DEFAULT_SEVERITY_MAP = null;

   private int[] severityArray = new int[17]; // because Syslog.FATAL = 16

   static
   {
       DEFAULT_SEVERITY_MAP = new HashMap();
       DEFAULT_SEVERITY_MAP.put( new Integer(Syslog.DEBUG),   new Integer(UNIX_DEBUG) );
       DEFAULT_SEVERITY_MAP.put( new Integer(Syslog.INFO),    new Integer(UNIX_INFO) );
       DEFAULT_SEVERITY_MAP.put( new Integer(Syslog.WARNING), new Integer(UNIX_WARNING) );
       DEFAULT_SEVERITY_MAP.put( new Integer(Syslog.ERROR),   new Integer(UNIX_ERROR) );
       DEFAULT_SEVERITY_MAP.put( new Integer(Syslog.FATAL),   new Integer(UNIX_EMERGENCY) );
   }

   /**
    * Create a new UNIX logger sending messages to the given host and port.
    */
   public UNIXSyslogLog(InetAddress address, int port)
   throws SyslogInitException
   {
       this();
       this.address = address;
       this.port = port;
   }

   /**
    * Create a new UNIX logger sending messages to the given host
    * on the default syslog port (514).
    */
   public UNIXSyslogLog(InetAddress address)
   throws SyslogInitException
   {
       this(address, DEFAULT_PORT);
   }

   /**
    * Create a new UNIX logger with no settings.  You must
    * call <tt>setLogServer()</tt> at least before trying
    * to use this.
    */
   public UNIXSyslogLog()
   throws SyslogInitException
   {
       super();

       this.simpleFormat = new SimpleSyslogTextFormatter();
       this.simpleFormat.setClassWidth(0);

       setSeverityMap(DEFAULT_SEVERITY_MAP);

       try
       {
           socket = new DatagramSocket();
       }
       catch (SocketException x)
       {
           throw new SyslogInitException(Syslog.getResourceString(MessageConstants.UNIX_CANNOT_INIT_SOCKET), x);
       }
   }

   /**
    *  Set the message body formatting template.  This string is used
    *  to format the body of the UDP packet in conjunction with the
    *  <tt>java.text.MessageFormat</tt> class.  The following tokens are
    *  replaced in the String during formatting:<P>
    *
    *  <table border=1 cellpadding=3 cellspacing=0>
    *    <tr><td><tt>{CHANNEL}</tt></td><td>Channel name</td></tr>
    *    <tr><td><tt>{CALLER-FULLNAME}</tt></td><td>Full caller class name (with package)</td></tr>
    *    <tr><td><tt>{CALLER}</tt></td><td>Caller class name</td></tr>
    *    <tr><td><tt>{MESSAGE}</tt></td><td>Short message</td></tr>
    *    <tr><td><tt>{THREAD}</tt></td><td>Thread name</td></tr>
    *    <tr><td><tt>{SEVERITY}</tt></td><td>Severity ("<tt>DEBUG</tt>" ... "<tt>FATAL</tt>")</td></tr>
    *  </table><P>
    *
    *  The default value is "<TT>[{SEVERITY}] {CALLER}: {MESSAGE}</TT>".
    *  You can set this to something like "<tt>{CHANNEL}: {CALLER}: {MESSAGE}</TT>" or
    *  "<tt>{CALLER}: [{CHANNEL}] {MESSAGE}</TT>" as examples to see what you can do
    *  with this.
    */
   public void setMessageTemplate(String template)
   {
       this.messageTemplate = template;
   }

   /**
    *  Get the text template used to format part of the packet body.
    */
   public String getMessageTemplate()
   {
       return this.messageTemplate;
   }

   /**
    *  Set the map to use when converting Syslog severities into UNIX severities.
    *  The key in the map should be a <tt>java.lang.Integer</tt> representing the
    *  Syslog severity (<TT>Syslog.DEBUG</TT> ... <tt>Syslog.FATAL</TT>), and
    *  the value should be a <tt>java.lang.Integer</TT> representing the UNIX
    *  severity to use for the given Syslog severity.  The values for UNIX severities
    *  are described in RFC 3164, and are represented as static members of this
    *  class (<TT>UNIXSyslogLog.UNIX_DEBUG</TT> ... <TT>UNIXSyslogLog.UNIX_EMERGENCY</TT>).
    */
   public void setSeverityMap(Map map)
   {
       this.severityMap = map;

       // move things into this array so we can do lookups faster
       // and not create objects needlessly later.
       try
       {
           severityArray[Syslog.DEBUG]   = ((Integer)map.get(new Integer(Syslog.DEBUG))).intValue();
           severityArray[Syslog.INFO]    = ((Integer)map.get(new Integer(Syslog.INFO))).intValue();
           severityArray[Syslog.WARNING] = ((Integer)map.get(new Integer(Syslog.WARNING))).intValue();
           severityArray[Syslog.ERROR]   = ((Integer)map.get(new Integer(Syslog.ERROR))).intValue();
           severityArray[Syslog.FATAL]   = ((Integer)map.get(new Integer(Syslog.FATAL))).intValue();
       }
       catch (NullPointerException x)
       {
           throw new IllegalArgumentException(Syslog.getResourceString(MessageConstants.UNIX_BAD_SEVERITY_MAP));
       }
   }

   /**
    *  Get the map to use when converting Syslog severities into UNIX severities.
    */
   public Map getSeverityMap()
   {
       return this.severityMap;
   }

   /**
    * Get the tag that messages appear as.   Default is "<tt>ProtomatterSyslog</TT>".
    */
   public String getTag()
   {
       return this.tag;
   }

   /**
    * Set the tag that messages appear as.
    */
   public void setTag(String tag)
   {
       this.tag = tag;
       this.tagBytes = tag.getBytes();
   }

   /**
    * Determine if we should show the hostname before the "tag".  Default is "<TT>false</TT>"
    * The spec says that you should do this, but when I tested under Red Hat
    * Linux 7.2, it just repeated the hostname.  Your mileage may vary.  The default
    * is <tt>false</tt>.
    */
   public boolean getShowHostname()
   {
       return this.showHostname;
   }

   /**
    * Set if we should show the hostname before the "tag".
    */
   public void setShowHostname(boolean showHostname)
   {
       this.showHostname = showHostname;;
   }

   /**
    * Set the facility ID that messages appear to be from.
    * Refer to RFC 3164 for more information.  Here is the
    * list:<P>
    * <TABLE cellpadding=4 cellspacing=0>
    *  <TR><TD>0</TD><TD>kernel messages</TD></TR>
    *  <TR><TD>1</TD><TD>user-level messages</TD><TD>
    *  <TR><TD>2</TD><TD>mail system</TD><TD>
    *  <TR><TD>3</TD><TD>system daemons</TD><TD>
    *  <TR><TD>4</TD><TD>security/authorization messages (note 1)</TD><TD>
    *  <TR><TD>5</TD><TD>messages generated internally by syslogd</TD><TD>
    *  <TR><TD>6</TD><TD>line printer subsystem</TD><TD>
    *  <TR><TD>7</TD><TD>network news subsystem</TD><TD>
    *  <TR><TD>8</TD><TD>UUCP subsystem</TD><TD>
    *  <TR><TD>9</TD><TD>clock daemon (note 2)</TD><TD>
    *  <TR><TD>10</TD><TD>security/authorization messages (note 1)</TD><TD>
    *  <TR><TD>11</TD><TD>FTP daemon</TD><TD>
    *  <TR><TD>12</TD><TD>NTP subsystem</TD><TD>
    *  <TR><TD>13</TD><TD>log audit (note 1)</TD><TD>
    *  <TR><TD>14</TD><TD>log alert (note 1)</TD><TD>
    *  <TR><TD>15</TD><TD>clock daemon (note 2)</TD><TD>
    *  <TR><TD>16</TD><TD>local use 0  (local0)</TD><TD>
    *  <TR><TD>17</TD><TD>local use 1  (local1)</TD><TD>
    *  <TR><TD>18</TD><TD>local use 2  (local2)</TD><TD>
    *  <TR><TD>19</TD><TD>local use 3  (local3)</TD><TD>
    *  <TR><TD>20</TD><TD>local use 4  (local4)</TD><TD>
    *  <TR><TD>21</TD><TD>local use 5  (local5)</TD><TD>
    *  <TR><TD>22</TD><TD>local use 6  (local6)</TD><TD>
    *  <TR><TD>23</TD><TD>local use 7  (local7)</TD><TD>
    * </TABLE><P>
    *
    * <B>Note 1:</B> Various operating systems have been found to utilize
    *       Facilities 4, 10, 13 and 14 for security/authorization,
    *       audit, and alert messages which seem to be similar.<P>
    * <B>Note 2:</B> Various operating systems have been found to utilize
    *       both Facilities 9 and 15 for clock (cron/at) messages.<P>
    *
    * The default is 16 (Local Use 0).
    */
   public void setFacility(int facility)
   {
       this.facility = facility;
   }

   /**
    * Get the facility ID that messages appear to be from.
    */
   public int getFacility()
   {
       return this.facility;
   }

   /**
    * Set the address of the log server.
    */
   public void setLogServer(InetAddress address)
   {
       this.address = address;
   }

   /**
    * Get the address of the log server.
    */
   public InetAddress getLogServer()
   {
       return this.address;
   }

   /**
    * Set the port number on the log server.
    */
   public void setPort(int port)
   {
       this.port = port;
   }

   /**
    * Get the port number on the log server.
    */
   public int getPort()
   {
       return this.port;
   }

   private int translateMessageSeverity(int syslogSeverity)
   {
      return severityArray[syslogSeverity];
   }

   /**
    *  Log a message.
    */
   public final void log(SyslogMessage message)
   {
      try
      {
          // fast translation of severity
          int severity = severityArray[message.level];

          ByteArrayOutputStream os = new ByteArrayOutputStream(256);

          // encode facility and severity -- "PRI" section
          os.write(L_ANGLE);
          os.write(String.valueOf(facility).getBytes());
          os.write(String.valueOf((8*facility) + severity).getBytes());
          os.write(R_ANGLE);

          // next is the "HEADER" section -- TIMESTAMP HOSTNAME
          byte timestamp[] = null;
          long now = System.currentTimeMillis();
          if ((now - cacheTime) > cacheTimeout)
          {
              timestamp = dateFormat.format(new Date(message.time)).getBytes();
              cacheDate = timestamp;
              cacheTime = now;
          }
          else
          {
              timestamp = cacheDate;
          }
          os.write(timestamp);
          os.write(SPACE);

          // For some reason, Linux syslog duplicates the hostname?
          // Maybe I'm doing something wrong?
          if (this.showHostname)
          {
              os.write(simpleFormat.getHostname(Syslog.getLocalHostName()).getBytes());
              os.write(SPACE);
          }

          // Next is the MSG section -- TAG: CONTENT
          // TAG is taken as the caller name,
          // CONTENT is the short message
          os.write(this.tagBytes);
          os.write(COLON);
          os.write(SPACE);

          String body = messageTemplate;

          body = StringUtil.replace(body, CHANNEL_TOKEN, message.channel);
          body = StringUtil.replace(body, FULLCALLER_TOKEN, message.loggerClassname);
          body = StringUtil.replace(body, CALLER_TOKEN, trimFromLastPeriod(message.loggerClassname));
          body = StringUtil.replace(body, MESSAGE_TOKEN, String.valueOf(message.msg));
          body = StringUtil.replace(body, THREAD_TOKEN, message.threadName);
          body = StringUtil.replace(body, SEVERITY_TOKEN, getSeverityName(message.level));

          os.write(body.getBytes());

          // Be sure to truncate data according to the spec
          byte[] data = os.toByteArray();
          DatagramPacket packet = new DatagramPacket(data,
            ((data.length > MAX_DATA_LENGTH) ? MAX_DATA_LENGTH : data.length),
            address, port);

          // Stuff the packet down the pipe
          socket.send(packet);
      }
      catch (IOException x)
      {
        System.err.println(MessageFormat.format(
          Syslog.getResourceString(MessageConstants.UNIX_CANNOT_WRITE_MESSAGE),
          new Object[] { x.toString() }));
        x.printStackTrace();
      }
   }

   protected String getSeverityName(int syslogSeverity)
   {
       switch (syslogSeverity)
       {
           case Syslog.DEBUG:   return "DEBUG";
           case Syslog.INFO:    return "INFO";
           case Syslog.WARNING: return "WARNING";
           case Syslog.ERROR:   return "ERROR";
           case Syslog.FATAL:   return "FATAL";
       }
       return "UNKNOWN";
   }

   private static final String trimFromLastPeriod(String s)
   {
       if (s == null || s.length() == 0)
           return "";
       int index = s.lastIndexOf(".");
       if (index == -1)
           return s;
       return s.substring(index +1);
   }

   public synchronized void shutdown()
   {
       // Don't do anything
   }

   public void flush()
   {
       // Don't do anything
   }
}
