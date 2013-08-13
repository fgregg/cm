package com.protomatter.syslog.xml;

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
import java.util.*;
import java.text.*;

import com.protomatter.xml.*;
import com.protomatter.syslog.*;
import com.protomatter.util.ChainedRuntimeException;
import org.jdom.*;

/**
 *  XML configuration helper for <tt>DatabaseLog</tt>.
 */
public class DatabaseLog_Helper
extends BasicLogger_Helper
{
  /**
   *  Configure this logger given the XML element.
   *  The <tt>&lt;Logger&gt;</tt> element should look like
   *  this:<P>
   *
   *  <TABLE BORDER=1 CELLPADDING=4 CELLSPACING=0 WIDTH="90%">
   *  <TR><TD>
   *  <PRE><B>
   *
   *  &lt;Logger class="com.protomatter.syslog.DatabaseLog" &gt;
   *
   *    <font color="#888888">&lt;!--
   *     Config params from {@link BasicLogger_Helper#configure(Object,Element) BasicLogger_Helper} can
   *     get inserted here.
   *    --&gt;</font>
   *
   *    <font color="#888888">&lt;!--
   *     The &lt;Format&gt; tag is included here, as it is used
   *     to set field truncation sizes.
   *    --&gt;</font>
   *    &lt;Format class="com.protomatter.syslog.SimpleSyslogTextFormatter"&gt;
   *      &lt;classWidth&gt;<i>column-width</i>&lt;/classWidth&gt;
   *      &lt;channelWidth&gt;<i>column-width</i>&lt;/channelWidth&gt;
   *      &lt;threadWidth&gt;<i>column-width</i>&lt;/threadWidth&gt;
   *      &lt;hostWidth&gt;<i>column-width</i>&lt;/hostWidth&gt;
   *    &lt;/Format&gt;
   *
   *    &lt;messageWidth&gt;<i>column-width</i>&lt;/messageWidth&gt;
   *    &lt;detailWidth&gt;<i>column-width</i>&lt;/detailWidth&gt;
   *
   *    &lt;driver&gt;<i>JDBCDriver</i>&lt;/driver&gt;
   *    &lt;url&gt;<i>JDBC-URL</i>&lt;/url&gt;
   *    &lt;tablePrefix&gt;<i>TablePrefix</i>&lt;/tablePrefix&gt;
   *    &lt;numRetries&gt;<i>NumRetries</i>&lt;/numRetries&gt;
   *
   *    &lt;statementAdapter&gt;<i>StatementAdapterClass</i>&lt;/statementAdapter&gt;
   *
   *    &lt;ConnectionProperties&gt;
   *      &lt;property&gt;
   *        &lt;name&gt;<i>name-1</i>&lt;/name&gt;
   *        &lt;value&gt;<i>value-1</i>&lt;/value&gt;
   *      &lt;/property&gt;
   *      &lt;property&gt;
   *        &lt;name&gt;<i>name-2</i>&lt;/name&gt;
   *        &lt;value&gt;<i>value-2</i>&lt;/value&gt;
   *      &lt;/property&gt;
   *      ...
   *      &lt;property&gt;
   *        &lt;name&gt;<i>name-N</i>&lt;/name&gt;
   *        &lt;value&gt;<i>value-N</i>&lt;/value&gt;
   *      &lt;/property&gt;
   *    &lt;/ConnectionProperties&gt;
   *
   *  &lt;/Logger&gt;
   *  </B></PRE>
   *  </TD></TR></TABLE><P>
   *
   *  <TABLE BORDER=1 CELLPADDING=2 CELLSPACING=0 WIDTH="90%">
   *  <TR CLASS="TableHeadingColor">
   *  <TD COLSPAN=3><B>Element</B></TD>
   *  </TR>
   *  <TR CLASS="TableHeadingColor">
   *  <TD><B>name</B></TD>
   *  <TD><B>value</B></TD>
   *  <TD><B>required</B></TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><tt>messageWidth</tt></TD>
   *  <TD VALIGN=TOP>Short message column width.</TD>
   *  <TD VALIGN=TOP>no (default is 255)</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><tt>detailWidth</tt></TD>
   *  <TD VALIGN=TOP>Short message detail width.</TD>
   *  <TD VALIGN=TOP>no (default is 4000)</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><tt>driver</tt></TD>
   *  <TD VALIGN=TOP>JDBC Driver class name</TD>
   *  <TD VALIGN=TOP>yes</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><tt>url</tt></TD>
   *  <TD VALIGN=TOP>JDBC connection URL</TD>
   *  <TD VALIGN=TOP>yes</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><tt>tablePrefix</tt></TD>
   *  <TD VALIGN=TOP>A prefix for table names</TD>
   *  <TD VALIGN=TOP>no</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><tt>numRetries</tt></TD>
   *  <TD VALIGN=TOP>Number of times to retry writing each message</TD>
   *  <TD VALIGN=TOP>no (default is 3)</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><tt>statementAdapter</tt></TD>
   *  <TD VALIGN=TOP>Full classname of the database statement adapter.</TD>
   *  <TD VALIGN=TOP>no (default is "<TT>StringDatabaseStatementAdapter</TT>")</TD>
   *  </TR>
   *
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><tt>ConnectionProperties</tt></TD>
   *  <TD VALIGN=TOP>
   *  Contains a set of <TT>&lt;property&gt;</TT> elements,
   *  each containing a <TT>&lt;name&gt;</TT> and
   *  <TT>&lt;value&gt;</TT> elements defining the connection
   *  properties.
   *  </TD>
   *  <TD VALIGN=TOP>no</TD>
   *  </TR>
   *
   *  </TR>
   *
   *  </TABLE><P>
   *
   */
  public void configure(Object o, Element e)
  throws SyslogInitException
  {
    super.configure(o, e);

    if (Syslog.getLocalHostName() == null)
    {
      throw new IllegalArgumentException(
        MessageFormat.format(
          Syslog.getResourceString(MessageConstants.XML_MUST_SPECIFY_ATTRIBUTE_MESSAGE),
          new Object[] { "hostname", "Syslog" } ));
    }

    DatabaseLog log = (DatabaseLog)o;

    String tmp = null;

    tmp = e.getChildTextTrim("driver", e.getNamespace());
    if (tmp != null)
    {
      log.setDriver(tmp);
    }
    else
    {
      throw new IllegalArgumentException(
        MessageFormat.format(
        Syslog.getResourceString(MessageConstants.XML_MUST_SPECIFY_PARAM_MESSAGE),
          new Object[] { "driver" } ));
    }

    tmp = e.getChildTextTrim("url", e.getNamespace());
    if (tmp != null)
    {
      log.setURL(tmp);
    }
    else
    {
      throw new IllegalArgumentException(
        MessageFormat.format(
        Syslog.getResourceString(MessageConstants.XML_MUST_SPECIFY_PARAM_MESSAGE),
          new Object[] { "url" } ));
    }

    tmp = e.getChildTextTrim("messageWidth", e.getNamespace());
    if (tmp != null)
    {
      log.setMessageWidth(Integer.parseInt(tmp));
    }

    tmp = e.getChildTextTrim("detailWidth", e.getNamespace());
    if (tmp != null)
    {
      log.setDetailWidth(Integer.parseInt(tmp));
    }

    tmp = e.getChildTextTrim("tablePrefix", e.getNamespace());
    if (tmp != null)
    {
      log.setTablePrefix(tmp);
    }

    tmp = e.getChildTextTrim("numRetries", e.getNamespace());
    if (tmp != null)
    {
      try
      {
        log.setNumRetries(Integer.parseInt(tmp.trim()));
      }
      catch (NumberFormatException x)
      {
        throw new IllegalArgumentException(
          MessageFormat.format(Syslog.getResourceString(MessageConstants.MUST_BE_INTEGER_PARAM_MESSAGE),
          new Object[] { "numRetries" } ));
      }
    }

    tmp = e.getChildTextTrim("statementAdapter", e.getNamespace());
    if (tmp != null)
    {
      try
      {
        DatabaseLogStatementAdapter adapter = (DatabaseLogStatementAdapter)Class.forName(tmp).newInstance();
        log.setStatementAdapter(adapter);
      }
      catch (Exception x)
      {
         throw new ChainedRuntimeException(MessageFormat.format(
           Syslog.getResourceString(MessageConstants.DATABASELOG_CANNOT_LOAD_STATEMENT_ADAPTER_MESSAGE),
           new Object[] { tmp }), x);
      }
    }

    Element element = e.getChild("ConnectionProperties", e.getNamespace());
    Properties props = new Properties();
    if (element != null)
    {
      Iterator params = element.getChildren("property", e.getNamespace()).iterator();
      while (params.hasNext())
      {
        Element param = (Element)params.next();
        String name = param.getChildTextTrim("name", e.getNamespace());
        String value = param.getChildTextTrim("value", e.getNamespace());
	    if (name != null && value != null)
	      props.put(name.trim(), value.trim());
      }
    }
    log.setProperties(props);
  }

  public Element getConfiguration(Object o, Element element)
  {
    Element e = super.getConfiguration(o, element);

    DatabaseLog log = (DatabaseLog)o;

    Element param = new Element("driver");
    param.setText(log.getDriver());
    e.getChildren().add(param);

    param = new Element("url");
    param.setText(log.getURL());
    e.getChildren().add(param);

    param = new Element("tablePrefix");
    param.setText(log.getTablePrefix());
    e.getChildren().add(param);

    param = new Element("numRetries");
    param.setText(String.valueOf(log.getNumRetries()));
    e.getChildren().add(param);

    param = new Element("messageWidth");
    param.setText(String.valueOf(log.getMessageWidth()));
    e.getChildren().add(param);

    param = new Element("detailWidth");
    param.setText(String.valueOf(log.getDetailWidth()));
    e.getChildren().add(param);

    param = new Element("ConnectionProperties");

    Properties props = log.getProperties();
    Enumeration pe = props.keys();
    while (pe.hasMoreElements())
    {
      String key = (String)pe.nextElement();
      String val = props.getProperty(key);

      Element prop = new Element("property");
      prop.getChildren().add((new Element("name")).setText(key));
      prop.getChildren().add((new Element("value")).setText(val));
      param.getChildren().add(prop);
    }
    e.getChildren().add(param);

    param = new Element("statementAdapter");
    param.setText(String.valueOf(log.getStatementAdapter().getClass().getName()));
    e.getChildren().add(param);


    return e;
  }
}
