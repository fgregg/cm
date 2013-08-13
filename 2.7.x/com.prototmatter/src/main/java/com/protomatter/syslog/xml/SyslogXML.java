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
import com.protomatter.syslog.*;
import com.protomatter.util.Debug;
import com.protomatter.xml.*;
import org.jdom.*;
import org.jdom.output.*;
import org.jdom.input.*;

/**
 *  A utility class for configuring Syslog from an XML file.
 */
public class SyslogXML
{
  // We need local copies of these since we can't reference
  // the Syslog.WARNING_PROPERTY constant without loading
  // the Syslog class, which would show the warning ;-)
  private static String CLASSLOADER_WARNING_PROPERTY = "Syslog.classloader.warning";
  private static String WARNING_OFF = "off";

  // By default, we show the classloader warning
  private static boolean WARNING_DEFAULT = true;

  /**
   *  The system property used to set the XML parser class.
   */
  public static String XML_PARSER_PROPERTY = "Syslog.xml.parser";

  /**
   *  Default constructor.
   */
  private SyslogXML()
  {
    super();
  }

  /**
   *  Get an XML representation of the current configuration state of
   *  Syslog including all loggers, etc and write it to the given
   *  output stream.
   */
  public static void writeConfiguration(OutputStream out)
  throws IOException
  {
    Document d = getConfiguration();
    XMLOutputter output = new XMLOutputter();
    //output.setIndent(true);
    //output.setIndentSize(2);
    output.setIndent("  ");
    output.setNewlines(true);
    //output.setSuppressDeclaration(true);
    output.output(d, out);
  }

  /**
   *  Get an XML representation of the current configuration state of
   *  Syslog including all loggers, etc.
   */
  public static Document getConfiguration()
  {
    Element e = new Element("Syslog");
    Document d = new Document(e);
    e.setAttribute("defaultMask", Syslog.getLogMaskAsString());

    long sleepInterval = Syslog.getFlushThreadInterval();
    if (sleepInterval != 0)
      e.setAttribute("flushThreadInterval", String.valueOf(sleepInterval));

    if (Syslog.getLocalHostName() != null)
    {
      InetAddress addr = Syslog.getLocalHostName();
      String name = addr.getHostName();
      if (name == null)
        name = addr.getHostAddress();
      e.setAttribute("hostname", name);
    }

    e.setAttribute("computeCaller", String.valueOf(Syslog.getComputeCaller()));
    e.setAttribute("alwaysComputeCaller", String.valueOf(Syslog.getAlwaysComputeCaller()));

    Element debug = new Element("Debug");
    debug.setAttribute("enabled", String.valueOf(Debug.isEnabled()));
    debug.setAttribute("scan", String.valueOf(Debug.getScan()));
    Iterator list = Debug.getTraceNames();
    while (list.hasNext())
    {
      Element config = new Element("name");
      config.setText((String)list.next());
      config.setAttribute("level", "trace");
      debug.getChildren().add(config);
    }
    list = Debug.getDebugNames();
    while (list.hasNext())
    {
      Element config = new Element("name");
      config.setText((String)list.next());
      config.setAttribute("level", "debug");
      debug.getChildren().add(config);
    }
    list = Debug.getInfoNames();
    while (list.hasNext())
    {
      Element config = new Element("name");
      config.setText((String)list.next());
      config.setAttribute("level", "info");
      debug.getChildren().add(config);
    }
    e.getChildren().add(debug);

    list = Syslog.getLoggers();
    while (list.hasNext())
    {
      Syslogger logger = (Syslogger)list.next();
      XMLConfigHelper helper = null;
      try
      {
        helper = XMLConfigUtil.getConfigHelper(logger);
      }
      catch (Exception x)
      {
        x.printStackTrace();
      }
      Element config = helper.getConfiguration(logger, null);
      e.getChildren().add(config);
    }

    return d;
  }

  /**
   *  Configure syslog from the given XML file.  The root
   *  element of the file must either be a <TT>&lt;Syslog&gt;</TT>
   *  tag, or it must contain a child that is a <TT>&lt;Syslog&gt;</TT>
   *  tag.<P>
   *
   *  By default, this method will use the default SAX parser
   *  to parse the configuration file using JDom.  If you wish
   *  to change the parser, you can specify the
   *  "<TT>Syslog.xml.parser</TT>" system property.  That property
   *  should be set to the fully qualified class name of a SAX
   *  parser, such as "<TT>org.apache.xerces.parsers.SAXParser</TT>".
   *
   *  @see #configure(Element element)
   */
  public static void configure(File xmlFile)
  throws SyslogInitException
  {
    configure(xmlFile, System.getProperty(XML_PARSER_PROPERTY), WARNING_DEFAULT);
  }

  /**
   *  Configure syslog from the given XML file.  The root
   *  element of the file must either be a <TT>&lt;Syslog&gt;</TT>
   *  tag, or it must contain a child that is a <TT>&lt;Syslog&gt;</TT>
   *  tag.<P>
   *
   *  By default, this method will use the default SAX parser
   *  to parse the configuration file using JDom.  If you wish
   *  to change the parser, you can specify the
   *  "<TT>Syslog.xml.parser</TT>" system property.  That property
   *  should be set to the fully qualified class name of a SAX
   *  parser, such as "<TT>org.apache.xerces.parsers.SAXParser</TT>".
   *
   *  @see #configure(Element element)
   */
  public static void configure(InputStream input)
  throws SyslogInitException
  {
    configure(input, System.getProperty(XML_PARSER_PROPERTY), WARNING_DEFAULT);
  }

  /**
   *  Configure syslog from the given XML file using the
   *  given JDom SAX driver class as a parser.  The driver
   *  class is given as a parameter to the constructor for a
   *  <TT>org.jdom.input.SAXBuilder</TT> object.  For instance,
   *  to use the Apache Xerces parser, pass in
   *  "<TT>org.apache.xerces.parsers.SAXParser</TT>" as the
   *  driver.
   *
   *  @param xmlFile                  Configuration file
   *  @param saxDriverClass           XML parser class name
   *  @param showClassloaderWarning   Should the classloader warning be shown?
   *  @see #configure(Element element)
   */
  public static void configure(File xmlFile, String saxDriverClass, boolean showClassloaderWarning)
  throws SyslogInitException
  {
    if (!xmlFile.exists())
    {
      throw new SyslogInitException(Syslog.getResourceString(MessageConstants.CANNOT_CONFIGURE_MESSAGE),
        new FileNotFoundException(
          MessageFormat.format(Syslog.getResourceString(MessageConstants.FILE_NOT_FOUND_MESSAGE),
          new Object[] { xmlFile })
          ));
    }

    // Ditch the classloader warning
    if (!showClassloaderWarning)
        System.setProperty(CLASSLOADER_WARNING_PROPERTY, WARNING_OFF);

    try
    {
      String rootName = "Syslog";
      SAXBuilder builder = null;
      if (saxDriverClass == null)
        builder = new SAXBuilder();
      else
        builder = new SAXBuilder(saxDriverClass);
      Document document = builder.build(xmlFile);
      Element c = document.getRootElement();
      if (c == null)
        throw new SyslogInitException(MessageFormat.format(
          Syslog.getResourceString(MessageConstants.XML_NO_SYSLOG_ELEMENT_MESSAGE),
          new Object[] { "Syslog" }));
      if (!c.getName().equals(rootName))
      {
        c = c.getChild(rootName, c.getNamespace());
        if ((c == null) || (!rootName.equals(c.getName())))
          throw new SyslogInitException(MessageFormat.format(
            Syslog.getResourceString(MessageConstants.XML_NO_SYSLOG_ELEMENT_MESSAGE),
            new Object[] { "Syslog" }));
      }
      configure(c);
    }
    catch (Exception x)
    {
      if (x instanceof SyslogInitException)
        throw (SyslogInitException)x;
      throw new SyslogInitException(Syslog.getResourceString(MessageConstants.CANNOT_CONFIGURE_MESSAGE), x);
    }
  }

  /**
   *  Configure syslog from the given XML file using the
   *  given JDom SAX driver class as a parser.  The driver
   *  class is given as a parameter to the constructor for a
   *  <TT>org.jdom.input.SAXBuilder</TT> object.  For instance,
   *  to use the Apache Xerces parser, pass in
   *  "<TT>org.apache.xerces.parsers.SAXParser</TT>" as the
   *  driver.
   *
   *  @param input                    InputStream carrying the XML configuration data
   *  @param saxDriverClass           XML parser class name
   *  @param showClassloaderWarning   Should the classloader warning be shown?
   *  @see #configure(Element element)
   */
  public static void configure(InputStream input, String saxDriverClass, boolean showClassloaderWarning)
  throws SyslogInitException
  {
    try
    {
      // Ditch the classloader warning
      if (!showClassloaderWarning)
          System.setProperty(CLASSLOADER_WARNING_PROPERTY, WARNING_OFF);
      String rootName = "Syslog";
      SAXBuilder builder = null;
      if (saxDriverClass == null)
        builder = new SAXBuilder();
      else
        builder = new SAXBuilder(saxDriverClass);
      Document document = builder.build(input);
      Element c = document.getRootElement();
      if (c == null)
        throw new SyslogInitException(MessageFormat.format(
          Syslog.getResourceString(MessageConstants.XML_NO_SYSLOG_ELEMENT_MESSAGE),
          new Object[] { "Syslog" }));
      if (!c.getName().equals(rootName))
      {
        c = c.getChild(rootName, c.getNamespace());
        if ((c == null) || (!c.getName().equals(rootName)))
          throw new SyslogInitException(MessageFormat.format(
            Syslog.getResourceString(MessageConstants.XML_NO_SYSLOG_ELEMENT_MESSAGE),
            new Object[] { "Syslog" }));
      }
      configure(c);
    }
    catch (Exception x)
    {
      x.printStackTrace();
      if (x instanceof SyslogInitException)
        throw (SyslogInitException)x;
      throw new SyslogInitException(Syslog.getResourceString(MessageConstants.CANNOT_CONFIGURE_MESSAGE), x);
    }
  }

  /**
   *  Configure syslog from the given XML document.
   *  The document should look like this:<P>
   *
   *  <TABLE BORDER=1 CELLPADDING=4 CELLSPACING=0 WIDTH="90%">
   *  <TR><TD>
   *  <PRE><B>
   *
   *  &lt;Syslog
   *    defaultMask="<i>DefaultSyslogLogMask</i>"
   *    hostname="<i>LocalHostName</i>"
   *    computeCaller="<i>true|false</i>"
   *    alwaysComputeCaller="<i>true|false</i>"
   *    flushThreadInterval="<i>milliseconds</i>"
   *  &gt;
   *
   *    &lt;Debug enabled="<i>true|false</i>" scan="<i>true|false</i>"&gt;
   *      &lt;name level="<i>trace|debug|info</i>"&gt;<i>name1</i>&lt;/name&gt;
   *      &lt;name level="<i>trace|debug|info</i>"&gt;<i>pattern1</i>&lt;/name&gt;
   *      &lt;name level="<i>trace|debug|info</i>"&gt;<i>name2</i>&lt;/name&gt;
   *      &lt;name level="<i>trace|debug|info</i>"&gt;<i>pattern2</i>&lt;/name&gt;
   *    &lt;/Debug&gt;
   *
   *    &lt;Logger
   *      name="<i>LoggerName</i>"
   *      class="<i>LoggerClassName</i>"
   *    &gt;
   *      <font color="#888888">&lt;!-- Logger directives --&gt;</font>
   *    &lt;/Logger&gt;
   *
   *    <font color="#888888">&lt;!-- More loggers here --&gt;</font>
   *
   *  &lt;/Syslog&gt;
   *  </B></PRE>
   *  </TD></TR></TABLE><P>
   *
   *  Each logger is loaded and configured with it's <tt>Logger</tt>
   *  XML element.  See the Javadoc for individual loggers for more
   *  information.<P>
   *
   *  The default, system-wide log mask is set to be "at or above"
   *  the value of the <tt>defaultMask</tt> attribute (if present).
   *  The hostname that Syslog thinks is "local" is set to the value
   *  of the <tt>hostname</tt> attribute (if present).<P>
   *
   *  If the <TT>flushThreadInterval</TT> attribute is set,
   *  it is interpreted as the number of milliseconds for a
   *  background thread to sleep between attempts to flush
   *  all the loggers.<P>
   *
   *  The <tt>computeCaller</tt> and <tt>alwaysComputeCaller</tt>
   *  attributes correspond to the
   *  <tt><a href="../Syslog.html#setComputeCaller(boolean)">Syslog.setComputeCaller()</a></tt>
   *  and
   *  <tt><a href="../Syslog.html#setAlwaysComputeCaller(boolean)">Syslog.setAlwaysComputeCaller()</a></tt>
   *  methods.<P>
   *
   *  The optional <tt>&lt;Debug&gt;</tt> element contains a list of
   *  <tt>&lt;name&gt;</tt> elements, which are passed to the 
   *  <tt>addXXXName()</tt> method of the 
   *  <tt><a href="../../util/Debug.html">com.protomatter.util.Debug</a></tt> class.<P>
   *
   *  @see BasicLogger
   */
  public static boolean configure(Element syslogConfig)
  throws SyslogInitException
  {
    // setup syslog
    try
    {
      Syslog.removeAllLoggers();
      Debug.clear();

      String syslogLevel = syslogConfig.getAttributeValue("defaultMask");
      if (syslogLevel != null)
        Syslog.setLogMask(syslogLevel);

      String interval = syslogConfig.getAttributeValue("flushThreadInterval");
      if (interval != null)
        Syslog.setFlushThreadInterval(Long.parseLong(interval));

      String name = syslogConfig.getAttributeValue("hostname");
      if (name != null)
      {
        Syslog.setLocalHostName(InetAddress.getByName(name));
      }

      String computeCaller = syslogConfig.getAttributeValue("computeCaller");
      if (computeCaller != null)
      {
        Syslog.setComputeCaller("true".equalsIgnoreCase(computeCaller));
      }

      computeCaller = syslogConfig.getAttributeValue("alwaysComputeCaller");
      if (computeCaller != null)
      {
        Syslog.setAlwaysComputeCaller("true".equalsIgnoreCase(computeCaller));
      }

      // setup debug
      Iterator list = syslogConfig.getChildren("Debug", syslogConfig.getNamespace()).iterator();
      if (list.hasNext())
      {
        Element element = (Element)list.next();

        String enabled = element.getAttributeValue("enabled");
        if ("true".equalsIgnoreCase(enabled))
            Debug.enable();
        else
            Debug.disable();

        String scan = element.getAttributeValue("scan");
        if (scan != null)
          Debug.setScan("true".equalsIgnoreCase(scan));

        Iterator children = element.getChildren("name", element.getNamespace()).iterator();
        while (children.hasNext())
        {
          Element child = (Element)children.next();
          String severity = child.getAttributeValue("level");
          if ("trace".equals(severity))
            Debug.addInfoName(child.getText());
          else if ("debug".equals(severity))
            Debug.addDebugName(child.getText());
          else if ("info".equals(severity))
            Debug.addInfoName(child.getText());
        }
      }

      list = syslogConfig.getChildren("Logger",
        syslogConfig.getNamespace()).iterator();
      while (list.hasNext())
      {
        Element element = (Element)list.next();
        String lClass = element.getAttributeValue("class");
        String lName = element.getAttributeValue("name");
        try
        {
          Syslogger logger = (Syslogger)Class.forName(lClass).newInstance();
          XMLConfigHelper helper = XMLConfigUtil.getConfigHelper(logger);
          helper.configure(logger, element);
          logger.setName(lName);
          Syslog.addLogger(logger);
        }
        catch (Exception x)
        {
          throw new SyslogInitException(
            MessageFormat.format(Syslog.getResourceString(MessageConstants.CANNOT_REGISTER_LOGGER_MESSAGE),
              new Object[] { lName }), x);
        }
      }
    }
    catch (Exception x)
    {
      if (x instanceof SyslogInitException)
        throw (SyslogInitException)x;
      throw new SyslogInitException(Syslog.getResourceString(MessageConstants.CANNOT_CONFIGURE_MESSAGE), x);
    }

    return true;
  }

  /**
   *  Write an example XML configuration file to
   *  standard out.  The optional first command line
   *  argument is a filename to load as a configuration
   *  and then print to standard out.  This can be
   *  useful, as all the default values are populated
   *  (which may not have been present in the input
   *  file).
   *
   *  @see #configure(Element element)
   */
  public static final void main(String args[])
  {
    ResourceBundle bundle = null;
    try
    {
      String bundleName = "com.protomatter.syslog.Syslog";
      bundle = ResourceBundle.getBundle(bundleName);
      if (bundle == null)
      {
        System.err.println("ERROR: Cannot load resource bundle \"" + bundleName + "\"");
        System.exit(0);
      }
      if (args.length == 1)
      {
        File file = new File(args[0]);
        System.out.println(MessageFormat.format(bundle.getString(MessageConstants.XML_LOADING_CONFIG_FILE),
          new Object[] { file } ));
        long time = System.currentTimeMillis();
        SyslogXML.configure(file);
        time = System.currentTimeMillis() - time;
        System.out.println(MessageFormat.format(bundle.getString(MessageConstants.XML_LOADED_CONFIG_IN),
          new Object[] { String.valueOf(time) } ));
      }
      System.out.println(bundle.getString(MessageConstants.XML_DUMPING_CONFIG));
      System.out.println("----------------------------------------------------------------------");
      SyslogXML.writeConfiguration(System.out);
      System.out.println("----------------------------------------------------------------------");
      System.out.println(bundle.getString(MessageConstants.XML_CONFIG_OK));
    }
    catch (Exception x)
    {
      System.out.println(bundle.getString(MessageConstants.XML_CONFIG_EXCEPTION));
      x.printStackTrace();
    }
  }
}
