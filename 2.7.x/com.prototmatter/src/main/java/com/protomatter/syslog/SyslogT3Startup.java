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
import java.util.*;
import java.text.*;

import javax.naming.*;
// REMOVED 2013-08-13 for ChoiceMaker
// import weblogic.common.*;
// END REMOVED
import com.protomatter.syslog.xml.*;

/**
 *  Configure syslog to start when WebLogic does.
 *  For use with <a href="http://www.beasys.com">BEA WebLogic Server</a>.
 *  This class implements the <tt>weblogic.common.T3StartupDef</tt>
 *  interface and is able to initialize Syslog when WebLogic starts.
 *  This class also implements the <TT>T3ShutdownDef</TT> interface and
 *  so can be used to shutdown syslog when WebLogic shuts down.
 *
 *  @see Syslog
 *  @see Syslogger
 */
public class SyslogT3Startup
// REMOVED 2013-08-13 for ChoiceMaker
// implements T3StartupDef, T3ShutdownDef
// END REMOVED
{
// REMOVED 2013-08-13 for ChoiceMaker
//   private T3ServicesDef services = null;
//   private static boolean configured = false;
// 
//   private static String LOG_PREFIX = "Protomatter Syslog: ";
// 
//   /**
//    *  Default constructor -- called by WebLogic.
//    */
//   public SyslogT3Startup()
//   {
//     super();
//   }
// 
//   /**
//    *  Part of the <tt>weblogic.common.T3StartupDef</tt> interface.
//    *
//    *  @see weblogic.common.T3StartupDef
//    */
//   public void setServices(T3ServicesDef services)
//   {
//     this.services = services;
//   }
// 
//   /**
//    *  Start Syslog services.
//    *  <P>
//    *  Syslog is configured from an XML file specified by the
//    *  <tt>Syslog.config.xml</tt> system property.
//    *  Multiple calls to this method are ignored.
//    *
//    *  @see com.protomatter.syslog.xml.SyslogXML#configure(File)
//    */
//   public String startup(String name, Hashtable ht)
//   throws Exception
//   {
//     // setup syslog
//     LogServicesDef log = services.log();
//     try
//     {
//       log.info(LOG_PREFIX + "SyslogT3Startup " + Syslog.getResourceString(MessageConstants.T3_INIT_MESSAGE));
// 
//       // make sure someone else didn't already run us.
//       if (configured)
//         return Syslog.getResourceString(MessageConstants.T3_ALREADY_INIT_MESSAGE);
// 
//       // get the path to the config file from the
//       // "Syslog.config.xml" system property.
//       String xmlConfigFile = System.getProperty("Syslog.config.xml");
//       if (xmlConfigFile == null)
//       {
//         log.error(LOG_PREFIX + MessageFormat.format(
//           Syslog.getResourceString(MessageConstants.T3_MUST_SPECIFY_1_MESSAGE),
//           new Object[] { "Syslog.config.xml" }));
//         log.error(LOG_PREFIX + Syslog.getResourceString(MessageConstants.T3_MUST_SPECIFY_2_MESSAGE));
//         log.error(LOG_PREFIX + "  -DSyslog.config.xml=" +
//           Syslog.getResourceString(MessageConstants.T3_MUST_SPECIFY_BLAH_MESSAGE));
//         return Syslog.getResourceString(MessageConstants.T3_FAILURE_MESSAGE);
//       }
//       log.info(LOG_PREFIX + MessageFormat.format(
//         Syslog.getResourceString(MessageConstants.CONFIGURING_SYSLOG_FROM_MESSAGE),
//         new Object[] { xmlConfigFile }));
//       SyslogXML.configure(new File(xmlConfigFile));
//       this.configured = true;
// 
//       Iterator loggers = Syslog.getLoggers();
//       while (loggers.hasNext())
//       {
//         Syslogger logger = (Syslogger)loggers.next();
//         if (logger.getName() != null)
//         {
//           log.info(LOG_PREFIX + MessageFormat.format(
//             Syslog.getResourceString(MessageConstants.T3_LOGGER_ISA_MESSAGE),
//             new Object[] { logger.getName(), logger.getClass().getName() }));
//         }
//         else
//         {
//           log.info(LOG_PREFIX + MessageFormat.format(
//             Syslog.getResourceString(MessageConstants.T3_LOGGER_NONAME_ISA_MESSAGE),
//             new Object[] { logger.getClass().getName() }));
//         }
//       }
//     }
//     catch (Exception x)
//     {
//       this.configured = false;
//       log.error(LOG_PREFIX + Syslog.getResourceString(MessageConstants.CANNOT_CONFIGURE_MESSAGE), x);
//       return Syslog.getResourceString(MessageConstants.T3_FAILURE_MESSAGE);
//     }
// 
//     return Syslog.getResourceString(MessageConstants.T3_SUCCESS_MESSAGE);
//   }
// 
//   /**
//    *  Shutdown Syslog services.  This method simply calls
//    *  <TT>Syslog.shutdown()</TT>.
//    */
//   public String shutdown(String name, Hashtable ht)
//   {
//     Syslog.shutdown();
//     return Syslog.getResourceString(MessageConstants.T3_SUCCESS_MESSAGE);
//   }
// 
//   /**
//    *  A shortcut to starting syslog services.  This is
//    *  generally used by classes that want to ensure that
//    *  Syslog has been started before they start.  You can
//    *  basically do this:<P>
//    *
//    *  <blockquote><pre>
//    *  (new SyslogT3Startup()).startup(services);
//    *  </pre></blockquote>
//    *
//    *  From inside the <tt>startup(...)</tt> method in
//    *  your startup class to make sure that syslog
//    *  gets started.<P>
//    */
//   public boolean startup(T3ServicesDef services)
//   {
//     try
//     {
//       setServices(services);
//       startup(null, null);
//       return true;
//     }
//     catch (Exception x)
//     {
//       return false;
//     }
//   }
// END REMOVED
}
