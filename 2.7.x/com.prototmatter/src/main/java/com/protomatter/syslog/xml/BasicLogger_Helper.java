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

import java.net.*;
import java.util.*;
import java.text.*;
import org.jdom.*;
import com.protomatter.xml.*;
import com.protomatter.syslog.*;

/**
 *  XML configuration helper for <tt>BasicLogger</tt>.
 */

public abstract class BasicLogger_Helper
            implements XMLConfigHelper
{
    /**
     *  Configure this logger given the XML element.
     *  The <TT>&lt;Logger&gt;</tt> element should look like this:<P>
     *
     *  <TABLE BORDER=1 CELLPADDING=4 CELLSPACING=0 WIDTH="90%">
     *  <TR><TD>
     *  <PRE><B>
     *
     *  &lt;Logger
     *    name="<i>LoggerName</i>"
     *    class="<i>LoggerClassName</i>"
     *  &gt;
     *
     *    <font color="#888888">&lt;!-- Logger subclass directives --&gt;</font>
     *
     *    &lt;Policy class="<i>LogPolicyClass</i>" &gt;
     *      <font color="#888888">&lt;!-- Policy directives --&gt;</font>
     *    &lt;/Policy&gt;
     *
     *    &lt;Format class="<i>LogFormatClass</i>" &gt;
     *      <font color="#888888">&lt;!-- Formatter directives --&gt;</font>
     *    &lt;/Format&gt;
     *
     *  &lt;/Logger&gt;
     *  </B></PRE>
     *  </TD></TR></TABLE><P>
     *
     *  <TABLE BORDER=1 CELLPADDING=2 CELLSPACING=0 WIDTH="90%">
     *  <TR CLASS="TableHeadingColor">
     *  <TD COLSPAN=3><B>Attributes:</B></TD>
     *  </TR>
     *  <TR CLASS="TableHeadingColor">
     *  <TD><B>name</B></TD>
     *  <TD><B>value</B></TD>
     *  <TD><B>required</B></TD>
     *  </TR>
     *
     *  <TR CLASS="TableRowColor">
     *  <TD VALIGN=TOP><TT>name</TT></TD>
     *  <TD VALIGN=TOP>Symbolic name for the logger.
     *  </TD>
     *  <TD VALIGN=TOP>no</TD>
     *  </TR>
     *
     *  <TR CLASS="TableRowColor">
     *  <TD VALIGN=TOP><TT>class</TT></TD>
     *  <TD VALIGN=TOP>Full class name of the logger to load.
     *    Must be a class that implements the
     *    <tt>com.protomatter.syslog.Syslogger</tt> interface.
     *  </TD>
     *  <TD VALIGN=TOP>yes</TD>
     *  </TR>
     *
     *  </TABLE><P>
     *
     *  If the logger is a subclass of this class (<TT>BasicLogger</TT>),
     *  then the optional <tt>&lt;Policy&gt;</tt> and <tt>&lt;Format&gt;</tt>
     *  elements are processed.  If each is present, it is passed
     *  to the <tt>configure()</tt> method of the
     *  <TT>LogPolicy</TT> or <TT>SyslogTextFormatter</TT> object specified
     *  so they can configure themselves.  It is assumed that each of
     *  those elements contains other configuration information that
     *  the individual policy or format class would understand.
     *
     *  @see SimpleSyslogTextFormatter_Helper#configure(Object,Element)
     *  @see HTMLSyslogTextFormatter_Helper#configure(Object,Element)
     *  @see SimpleLogPolicy_Helper#configure(Object,Element)
     *  @see PerClassPolicy_Helper#configure(Object,Element)
     */
    public void configure(Object o, Element e)
    throws SyslogInitException
    {
        configure(o, e, true, true);
    }

    public void configure(Object o, Element e, boolean includeFormat, boolean includePolicy)
    throws SyslogInitException
    {
        BasicLogger log = (BasicLogger)o;

        // name
        String tmp = e.getAttributeValue("name");

        if (tmp != null && !tmp.equals(""))
            log.setName(tmp);

        // figure out what text formatter to use
        if (includeFormat)
        {
            Element formatElement = null;
            String formatClass = null;
            formatElement = e.getChild("Format", e.getNamespace());

            if (formatElement != null)
            {
                formatClass = formatElement.getAttributeValue("class");

                if (formatClass == null)
                {
                    throw new IllegalArgumentException(MessageFormat.format(
                                                           Syslog.getResourceString(MessageConstants.XML_MUST_SPECIFY_ATTRIBUTE_MESSAGE),
                                                           new Object[] { "class", "Format" } ));
                }

                try
                {
                    SyslogTextFormatter formatter
                    = (SyslogTextFormatter)Class.forName(formatClass).newInstance();

                    try
                    {
                        // configure the formatter
                        XMLConfigHelper helper = XMLConfigUtil.getConfigHelper(formatter);
                        helper.configure(formatter, formatElement);
                        log.setTextFormatter(formatter);
                    }
                    catch (Exception x)
                    {
                        x.printStackTrace();
                        throw new IllegalArgumentException(MessageFormat.format(
                                                               Syslog.getResourceString(MessageConstants.BASICLOG_CANNOT_CONFIGURE_FORMATTER_MESSAGE),
                                                               new Object[] { formatClass } ));
                    }

                }
                catch (Exception x)
                {
                    x.printStackTrace();
                    throw new IllegalArgumentException(MessageFormat.format(
                                                           Syslog.getResourceString(MessageConstants.BASICLOG_CANNOT_LOAD_FORMATTER_MESSAGE),
                                                           new Object[] { formatClass } ));
                }

            }
            else
            {
                log.setTextFormatter(new SimpleSyslogTextFormatter());
            }

        }

        // figure out what policy to use
        if (includePolicy)
        {
            Element policyElement = null;
            String policyClass = null;
            policyElement = e.getChild("Policy", e.getNamespace());

            if (policyElement != null)
            {
                policyClass = policyElement.getAttributeValue("class");

                if (policyClass == null)
                {
                    throw new IllegalArgumentException(MessageFormat.format(
                                                           Syslog.getResourceString(MessageConstants.XML_MUST_SPECIFY_ATTRIBUTE_MESSAGE),
                                                           new Object[] { "class", "Policy" } ));
                }

                try
                {
                    LogPolicy policy = (LogPolicy)Class.forName(policyClass).newInstance();

                    try
                    {
                        // configure the formatter
                        XMLConfigHelper helper = XMLConfigUtil.getConfigHelper(policy);
                        helper.configure(policy, policyElement);
                        log.setPolicy(policy);
                    }
                    catch (Exception x)
                    {
                        x.printStackTrace();
                        throw new IllegalArgumentException(MessageFormat.format(
                                                               Syslog.getResourceString(MessageConstants.BASICLOG_CANNOT_CONFIGURE_POLICY_MESSAGE),
                                                               new Object[] { policyClass } ));
                    }

                }
                catch (Exception x)
                {
                    x.printStackTrace();
                    throw new IllegalArgumentException(MessageFormat.format(
                                                           Syslog.getResourceString(MessageConstants.BASICLOG_CANNOT_LOAD_POLICY_MESSAGE),
                                                           new Object[] { policyClass } ));
                }

            }
            else
            {
                log.setPolicy(new SimpleLogPolicy());
            }

        }
    }

    public Element getConfiguration(Object o, Element element)
    {
        return getConfiguration(o, element, true, true);
    }

    public Element getConfiguration(Object o, Element element, boolean includeFormat, boolean includePolicy)
    {
        BasicLogger log = (BasicLogger)o;

        if (element == null)
        {
            element = new Element("Logger");
        }

        // set the name of this logger.
        if (log.getName() != null)
        {
            element.setAttribute("name", log.getName());
        }

        element.setAttribute("class", log.getClass().getName());

        // text formatter stuff
        if (includeFormat)
        {
            Object format = log.getTextFormatter();
            Element formatterElement = new Element("Format");
            formatterElement.setAttribute("class", format.getClass().getName());

            XMLConfigHelper helper = null;

            try
            {
                helper = XMLConfigUtil.getConfigHelper(format);
            }
            catch (Exception x)
            {
                x.printStackTrace();
            }

            formatterElement = helper.getConfiguration(format, formatterElement);

            element.getChildren().add(formatterElement);
        }

        // policy stuff
        if (includePolicy)
        {
            Object policy = log.getPolicy();
            Element policyElement = new Element("Policy");
            policyElement.setAttribute("class", policy.getClass().getName());

            XMLConfigHelper helper = null;

            try
            {
                helper = XMLConfigUtil.getConfigHelper(policy);
            }
            catch (Exception x)
            {
                x.printStackTrace();
            }

            policyElement = helper.getConfiguration(policy, policyElement);

            element.getChildren().add(policyElement);
        }

        return element;
    }
}
