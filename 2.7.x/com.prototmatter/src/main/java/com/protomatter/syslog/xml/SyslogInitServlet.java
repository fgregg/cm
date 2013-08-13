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
import java.text.MessageFormat;
import com.protomatter.syslog.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *  A servlet that configures Syslog when its <TT>init()</TT>
 *  method is called.  It should be configured in a WebApp
 *  by adding the following declaration to the <TT>web.xml</TT>
 *  file for the WebApp:<P>
 *
 *  <PRE>
 *    <B>&lt;servlet&gt;</B>
 *      <B>&lt;servlet-name&gt;</B>SyslogInitServlet<B>&lt;/servlet-name&gt;</B>
 *      <B>&lt;description&gt;</B>Syslog Initialization Servlet<B>&lt;/description&gt;</B>
 *      <B>&lt;servlet-class&gt;</B>com.protomatter.syslog.xml.SyslogInitServlet<B>&lt;/servlet-class&gt;</B>
 *      <B>&lt;init-param&gt;</B>
 *        <B>&lt;param-name&gt;</B>config.xml<B>&lt;/param-name&gt;</B>
 *        <B>&lt;param-value&gt;</B>/path/to/syslog.xml<B>&lt;/param-value&gt;</B>
 *      <B>&lt;/init-param&gt;</B>
 *      <B>&lt;init-param&gt;</B>
 *        <B>&lt;param-name&gt;</B>show.config.on.get<B>&lt;/param-name&gt;</B>
 *        <B>&lt;param-value&gt;</B>false<B>&lt;/param-value&gt;</B>
 *      <B>&lt;/init-param&gt;</B>
 *      <B>&lt;init-param&gt;</B>
 *        <B>&lt;param-name&gt;</B>load.config.as.resource<B>&lt;/param-name&gt;</B>
 *        <B>&lt;param-value&gt;</B>false<B>&lt;/param-value&gt;</B>
 *      <B>&lt;/init-param&gt;</B>
 *      <B>&lt;init-param&gt;</B>
 *        <B>&lt;param-name&gt;</B>Syslog.xml.parser<B>&lt;/param-name&gt;</B>
 *        <B>&lt;param-value&gt;</B>org.apache.xerces.parsers.SAXParser<B>&lt;/param-value&gt;</B>
 *      <B>&lt;/init-param&gt;</B>
 *      <B>&lt;init-param&gt;</B>
 *        <B>&lt;param-name&gt;</B>Syslog.classloader.warning<B>&lt;/param-name&gt;</B>
 *        <B>&lt;param-value&gt;</B>true<B>&lt;/param-value&gt;</B>
 *      <B>&lt;/init-param&gt;</B>
 *      <B>&lt;load-on-startup&gt;</B>1<B>&lt;/load-on-startup&gt;</B>
 *    <B>&lt;/servlet&gt;</B>
 *    <B>&lt;servlet-mapping&gt;</B>
 *      <B>&lt;servlet-name&gt;</B>SyslogInitServlet<B>&lt;/servlet-name&gt;</B>
 *      <B>&lt;url-pattern&gt;</B>/SyslogInitServlet<B>&lt;/url-pattern&gt;</B>
 *    <B>&lt;/servlet-mapping&gt;</B>
 *  </PRE><P>
 *
 *  The <TT>config.xml</TT> init param is requred and should point
 *  to the XML configuration file for Syslog.<P>
 *
 *  If the optional <TT>show.config.on.get</TT> init param is set
 *  to <TT>true</TT> (the default is <TT>false</TT>), then when
 *  the servlet is hit with a <TT>GET</TT> request, it will return the
 *  current Syslog configuration.  Care should be taken when using this
 *  option since the syslog configuration file may contain database
 *  passwords, etc.  It is a good idea to use declarative security
 *  to protect this servlet if you are allowing it to display the
 *  configuration file.<P>
 *
 *  If the optional <TT>load.config.as.resource</TT> init param is set
 *  to <TT>true</TT> (the default is <TT>false</TT>), then when the
 *  servlet initializes, it will try and find the configuration file
 *  as a resource using the classloader that loaded the servlet.  This
 *  is a useful option if you are using this servlet to allow you
 *  to use a different Syslog instance for each EAR in a J2EE server.
 *  For instance, if you set the value of the <TT>config.xml</TT>
 *  parameter to "<TT>syslog-config.xml</TT>", and then set
 *  <TT>load.config.as.resource</TT> to "<TT>true</TT>", then you
 *  can put a file called "<TT>syslog-config.xml</TT>" in the base
 *  of the EAR directory, and the servlet will find it.  This is
 *  a nice option to use if you don't like to have hard-coded
 *  paths in the <TT>web.xml</TT> file.<P>
 *
 *  The optional <TT>Syslog.xml.parser</TT> init param can be used
 *  to set the class name of the XML parser used to read the XML
 *  configuration file.  It is possible that a confused JAXP
 *  registry can cause problems.  You should set this property
 *  to "<TT>org.apache.xerces.parsers.SAXParser</TT>" or something
 *  similar.  Note that this has the exact same effect as setting
 *  the same property as a system property (using the "<TT>-D</TT>"
 *  option to the JVM).  It is preferable to use that property
 *  in the <TT>web.xml</TT> file instead of the system property
 *  because you can then have the ability to use different
 *  XML parsers for different EAR files.<P>
 *
 *  The optional <TT>Syslog.classloader.warning</TT> init param can be used
 *  to turn off the classloader warning that is issued if Syslog is
 *  loaded with a ClassLoader other than the system one.  The default
 *  value for this property is <TT>true</TT>, meaning that the warning
 *  <i>should</i> be displayed.  Note that this has the exact same effect as setting
 *  the same property as a system property (using the "<TT>-D</TT>"
 *  option to the JVM).  Some people prefer to use that property
 *  in the <TT>web.xml</TT> file instead of the system property
 *  because they don't like to have to set system properties.
 *  Please read <a href="../classloader-warning.html">this document</a>
 *  regarding classloader issues with Syslog.<P>
 *
 *  The <TT>&lt;servlet-mapping&gt;</TT> tag above is optional.  If
 *  it is not specified, the servlet will still initialize syslog when
 *  the WebApp comes up, but the servlet will not be visible to
 *  web browsers.  This is generally a good thing.<P>
 */

public class SyslogInitServlet
            extends HttpServlet
{
    private boolean showConfigurationOnGet = false;
    private boolean loadConfigAsResource = false;

    // By default, we show the classloader warning
    private static final boolean WARNING_DEFAULT = true;

    private static final String WARNING_OFF = "off";

    private static final String TRUE = "true";

    private static final String CONFIG_FILE_PARAM = "config.xml";

    private static final String SHOW_CONFIG_ON_GET_PARAM = "show.config.on.get";

    private static final String LOAD_CONFIG_AS_RESOURCE_PARAM = "load.config.as.resource";

    /**
     *  Default constructor.
     */
    public SyslogInitServlet()
    {
        super();
    }

    /**
     *  Initialize syslog.
     */
    public void init(ServletConfig config)
    throws ServletException
    {
        super.init(config);

        try
        {
            if (TRUE.equalsIgnoreCase(config.getInitParameter(SHOW_CONFIG_ON_GET_PARAM)))
                this.showConfigurationOnGet = true;

            if (TRUE.equalsIgnoreCase(config.getInitParameter(LOAD_CONFIG_AS_RESOURCE_PARAM)))
                this.loadConfigAsResource = true;

            boolean showClassloaderWarning = WARNING_DEFAULT;

            if (WARNING_OFF.equalsIgnoreCase(config.getInitParameter(Syslog.WARNING_PROPERTY)))
                showClassloaderWarning = false;

            String filename = config.getInitParameter(CONFIG_FILE_PARAM);

            if (filename == null)
                throw new IllegalArgumentException(MessageFormat.format(
                                                       Syslog.getResourceString(MessageConstants.MUST_SPECIFY_INIT_PARAM_MESSAGE),
                                                       new Object[] { CONFIG_FILE_PARAM } ));

            String xmlParserClass = System.getProperty(SyslogXML.XML_PARSER_PROPERTY);

            if (isValid(config.getInitParameter(SyslogXML.XML_PARSER_PROPERTY)))
            {
                xmlParserClass = config.getInitParameter(SyslogXML.XML_PARSER_PROPERTY).trim();
            }

            if (loadConfigAsResource)
            {
                // try and find the file as a resource using the
                // ClassLoader that we were loaded with.
                ClassLoader loader = this.getClass().getClassLoader();
                InputStream input = loader.getResourceAsStream(filename);

                if (input != null)
                {
                    SyslogXML.configure(input, xmlParserClass, showClassloaderWarning);
                }
                else
                {
                    throw new SyslogInitException(MessageFormat.format(
                                                      Syslog.getResourceString(MessageConstants.RESOURCE_NOT_FOUND),
                                                      new Object[] { filename }));
                }

            }
            else
            {
                File file = new File(filename);
                SyslogXML.configure(file, xmlParserClass, showClassloaderWarning);
            }

        }
        catch (SyslogInitException x)
        {
            throw new ServletException(Syslog.getResourceString(MessageConstants.CANNOT_CONFIGURE_MESSAGE), x);
        }
    }

    /**
     *  Showdown syslog.
     */
    public void destroy()
    {
        Syslog.shutdown();
    }

    private static boolean isValid(String s)
    {
        if ((s == null) || (s.trim().length() == 0))
            return false;

        return true;
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
    {
        if (this.showConfigurationOnGet)
        {
            resp.setContentType("text/plain");
            OutputStream out = resp.getOutputStream();
            SyslogXML.writeConfiguration(out);
        }
        else
        {
            resp.setContentType("text/html");
            PrintWriter out = resp.getWriter();
            out.println(Syslog.getResourceString(MessageConstants.SERVLET_SNIPPY_RESPONSE));
        }
    }
}
