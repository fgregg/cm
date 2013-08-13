package com.protomatter.syslog.commons;

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

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.text.MessageFormat;
import org.apache.commons.logging.*;
import com.protomatter.syslog.*;
import com.protomatter.syslog.xml.*;
import com.protomatter.util.Debug;

/**
 *  A subclass of the <tt>org.apache.commons.logging.LogFactory</tt> class.
 */
public class SyslogChannelLogFactory
extends LogFactory
{
    private static Object lock = new Object();
    private static boolean configured = false;

    private Map attributes = new HashMap();

    private static String CONFIG_FILE_ATTRIBUTE = "Syslog.config.xml";

    // We need local copies of these since we can't reference
    // the Syslog.WARNING_PROPERTY constant without loading
    // the Syslog class, which would show the warning ;-)
    private static String CLASSLOADER_WARNING_PROPERTY = "Syslog.classloader.warning";
    private static String WARNING_OFF = "off";

    // By default, we show the classloader warning
    private static boolean WARNING_DEFAULT = true;

    /**
     *  Get a <tt>Log</tt> instance with the name of the
     *  given class's package (or the class name if
     *  it has no package).
     *
     *  @see com.protomatter.syslog.commons.SyslogChannelLog
     */
    public Log getInstance(Class clazz)
    throws LogConfigurationException
    {
        String name = clazz.getName();
        int index = name.lastIndexOf(".");
        if (index > 0)
            return getInstance(name.substring(0, index));
        return getInstance(name);
    }

    /**
     *  Get a <tt>Log</tt> instance with the given name.
     *
     *  @see com.protomatter.syslog.commons.SyslogChannelLog
     */
    public Log getInstance(String name)
    throws LogConfigurationException
    {
        if (!configured)
        {
            synchronized (lock)
            {
                if (!configured)
                {
                    try
                    {
                        String configFileName = (String)attributes.get(CONFIG_FILE_ATTRIBUTE);
                        String xmlParserClass = (String)attributes.get(SyslogXML.XML_PARSER_PROPERTY);
                        boolean showClassloaderWarning = WARNING_DEFAULT;
                        if (attributes.get(CLASSLOADER_WARNING_PROPERTY) != null)
                        {
                            showClassloaderWarning = !(WARNING_OFF.equalsIgnoreCase((String)attributes.get(CLASSLOADER_WARNING_PROPERTY)));
                        }

                        if (configFileName == null)
                        {
                            throw new LogConfigurationException(
                                MessageFormat.format(Syslog.getResourceString(MessageConstants.MUST_SPECIFY_INIT_PARAM_MESSAGE),
                                new Object[] { CONFIG_FILE_ATTRIBUTE } ));
                        }

                        SyslogXML.configure(new File(configFileName), xmlParserClass, showClassloaderWarning);
                    }
                    catch (SyslogInitException x)
                    {
                        throw new LogConfigurationException(Syslog.getResourceString(MessageConstants.CANNOT_CONFIGURE_MESSAGE), x);
                    }
                }
            }
        }

        return new SyslogChannelLog(name);
    }

    public void release()
    {
    }

    public Object getAttribute(String name)
    {
        return attributes.get(name);
    }

    public String[] getAttributeNames()
    {
        return (String[])attributes.keySet().toArray(new String[0]);
    }

    public void removeAttribute(String name)
    {
        attributes.remove(name);
    }

    public void setAttribute(String name, Object value)
    {
        if (value == null)
            removeAttribute(name);
        else
            attributes.put(name, value);
    }
}
