package com.protomatter.syslog;

/**
 *  The Protomatter Software License, Version 1.0
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
 *  SUCH DAMAGE.
 */

import java.util.*;
import java.lang.reflect.*;

/**
 *  Constants for messages loaded from resource bundles.
 */
public class MessageConstants
{
  public static final String CRUMB_MESSAGE = "BreadCrumb";

  public static final String FLUSH_LOGGERS_MESSAGE = "FlushLoggers";

  public static final String FLUSH_PROBLEM_MESSAGE = "FlushProblem";

  public static final String FLUSH_THREAD_NAME_MESSAGE = "FlushThreadName";

  public static final String SHUTDOWN_SYSLOG_MESSAGE = "ShutdownSyslog";

  public static final String CONFIGURING_SYSLOG_MESSAGE = "ConfiguringSyslog";

  public static final String CONFIGURING_SYSLOG_FROM_MESSAGE = "ConfiguringSyslogFrom";

  public static final String CONFIGURED_SYSLOG_MESSAGE = "ConfiguredSyslog";

  public static final String INVALID_PATTERN_MESSAGE = "InvalidPattern";

  public static final String WRITE_PROBLEM = "WriteProblem";

  public static final String RESOURCE_NOT_FOUND = "ResourceNotFound";


  public static final String XML_MUST_SPECIFY_ATTRIBUTE_MESSAGE = "XML.MustSpecifyAttribute";
  public static final String XML_MUST_SPECIFY_PARAM_MESSAGE = "XML.MustSpecifyParameter";
  public static final String XML_NO_SYSLOG_ELEMENT_MESSAGE = "XML.NoSyslogElement";
  public static final String XML_LOADING_CONFIG_FILE = "XML.LoadingConfigFile";
  public static final String XML_LOADED_CONFIG_IN = "XML.LoadedConfigIn";
  public static final String XML_DUMPING_CONFIG = "XML.DumpingConfig";
  public static final String XML_CONFIG_OK = "XML.ConfigOK";
  public static final String XML_CONFIG_EXCEPTION = "XML.ConfigException";

  public static final String SERVLET_SNIPPY_RESPONSE = "Servlet.SnippyResponse";


  public static final String MUST_BE_INTEGER_PARAM_MESSAGE = "ParameterMustBeInteger";

  public static final String PARAM_MUST_BE_A_OR_B_MESSAGE = "ParameterMustBeAorB";

  public static final String MUST_SPECIFY_INIT_PARAM_MESSAGE = "MustSpecifyInitParam";


  public static final String CHANNEL_NOT_NULL_MESSAGE = "Channel.ChannelNameNullMessage";


  public static final String DATABASELOG_NO_TEXT_FORMATTER_MESSAGE = "DatabaseLog.NoTextFormatterMessage";

  public static final String DATABASELOG_CANNOT_LOAD_STATEMENT_ADAPTER_MESSAGE = "DatabaseLog.CannotLoadStatementAdapter";

  public static final String DATABASELOG_ATTEMPT_MESSAGE_1 = "DatabaseLog.AttemptMessage1";

  public static final String DATABASELOG_ATTEMPT_MESSAGE_2 = "DatabaseLog.AttemptMessage2";

  public static final String DATABASELOG_ATTEMPT_MESSAGE_3 = "DatabaseLog.AttemptMessage3";

  public static final String DATABASELOG_ATTEMPT_MESSAGE_4 = "DatabaseLog.AttemptMessage4";

  public static final String DATABASELOG_ATTEMPT_MESSAGE_5 = "DatabaseLog.AttemptMessage5";

  public static final String DATABASELOG_ATTEMPT_CAUSE_MESSAGE = "DatabaseLog.AttemptCause";

  public static final String DATABASELOG_ORIGINAL_MESSAGE = "DatabaseLog.OriginalMessage";


  public static final String FILELOG_CANNOT_WRITE_MESSAGE = "FileLog.CannotWriteMessage";

  public static final String SERVER_USAGE_MESSAGE = "Server.Usage";
  public static final String SERVER_LOADING_MESSAGE = "Server.Loading";
  public static final String SERVER_CONFIG_PROP_ERROR_MESSAGE = "Server.ConfigPropertyError";
  public static final String SERVER_INIT_JNDI_MESSAGE = "Server.InitJNDI";
  public static final String SERVER_LISTEN_JMS_TOPIC_MESSAGE = "Server.ListenJMSTopic";
  public static final String SERVER_STARTING_JMS_MESSAGE = "Server.StartingJMS";
  public static final String SERVER_SUSPEND_MESSAGE = "Server.Suspend";
  public static final String SERVER_BINDING_MESSAGE = "Server.Binding";

  public static final String T3_INIT_MESSAGE = "T3.Initializing";
  public static final String T3_SUCCESS_MESSAGE = "T3.Success";
  public static final String T3_FAILURE_MESSAGE = "T3.Failure";
  public static final String T3_ALREADY_INIT_MESSAGE = "T3.AlreadyInitialized";
  public static final String T3_MUST_SPECIFY_1_MESSAGE = "T3.MustSpecify1";
  public static final String T3_MUST_SPECIFY_2_MESSAGE = "T3.MustSpecify1";
  public static final String T3_MUST_SPECIFY_BLAH_MESSAGE = "T3.MustSpecifyBlah";
  public static final String T3_LOGGER_ISA_MESSAGE = "T3.LoggerIsA";
  public static final String T3_LOGGER_NONAME_ISA_MESSAGE = "T3.NoNameLoggerIsA";


  public static final String WLS_HOST_MESSAGE = "WLS.host";
  public static final String WLS_THREAD_MESSAGE = "WLS.thread";


  public static final String HTML_OUTPUT_MESSAGE = "HTML.Output";

  public static final String HTML_DATE_MESSAGE = "HTML.Date";
  public static final String HTML_SEVERITY_MESSAGE = "HTML.Severity";
  public static final String HTML_CHANNEL_MESSAGE = "HTML.Channel";
  public static final String HTML_HOSTNAME_MESSAGE = "HTML.Hostname";
  public static final String HTML_THREAD_MESSAGE = "HTML.Thread";
  public static final String HTML_LOGGER_MESSAGE = "HTML.Logger";
  public static final String HTML_MESSAGE_MESSAGE = "HTML.Message";


  public static final String JMS_NAMING_EXCEPITON_MESSAGE = "JMSLog.NamingException";

  public static final String JMS_JMS_EXCEPITON_MESSAGE = "JMSLog.JMSException";

  public static final String JMS_ATTEMPT_MESSAGE_1 = "JMSLog.AttemptMessage1";

  public static final String JMS_ATTEMPT_MESSAGE_2 = "JMSLog.AttemptMessage2";

  public static final String JMS_ATTEMPT_CAUSE_MESSAGE = "JMSLog.AttemptCause";

  public static final String JMS_ATTEMPT_ORIGINAL_MESSAGE = "JMSLog.AttemptOriginal";


  public static final String PRINTWRITER_BAD_STREAM_NAME_MESSAGE = "PrintWriterLog.BadStreamName";


  public static final String REMOTELOG_JNDI_INIT_EXCEPTION_MESSAGE = "RemoteLog.JNDIInitException";

  public static final String REMOTELOG_CANNOT_WRITE_REMOTE_MESSAGE = "RemoteLog.CannotWriteToRemote";

  public static final String REMOTELOG_REMOVE_RECEIVER_MESSAGE = "RemoteLog.RemoveReceiver";


  public static final String MAILLOG_UNEXPECTED_RESPONSE_MESSAGE = "MailLog.UnexpectedResponse";

  public static final String MAILLOG_TRANSPORT_EXCEPTION_MESSAGE = "MailLog.TransportException";

  public static final String MAILLOG_CANNOT_WRITE_SMTP_MESSAGE = "MailLog.CannotWriteSMTP";


  public static final String OPENFLOG_CANNOT_OPEN_MESSAGE = "OpenFileLog.CannotOpen";


  public static final String CANNOT_CONFIGURE_MESSAGE = "CannotConfigure";

  public static final String FILE_NOT_FOUND_MESSAGE = "FileNotFound";

  public static final String CANNOT_REGISTER_LOGGER_MESSAGE = "CannotRegisterLogger";

  public static final String CANNOT_SET_FLUSH_INTERVAL_MESSAGE = "CannotSetFlushInterval";

  public static final String CANNOT_DETERMINE_HOSTNAME_MESSAGE = "CannotDetermineHostname";

  public static final String CANNOT_SET_MASK_MESSAGE = "CannotSetMask";

  public static final String INVALID_MASK_MESSAGE = "InvalidMask";

  public static final String ILLEGAL_ARGUMENT_MESSAGE = "IllegalArgument";


  public static final String SYSLOGWRITER_IS_CLOSED_MESSAGE = "SyslogWriter.IsClosed";


  public static final String TIMEROLLOVERLOG_BAD_ROLL_VALUE_MESSAGE = "TimeRolloverLog.BadRollValue";

  public static final String TIMEROLLOVERLOG_BAD_ROLL_DAY_VALUE_MESSAGE = "TimeRolloverLog.BadRollDayValue";

  public static final String TIMEROLLOVER_ILLEGAL_ROLL_VALUE_MESSAGE = "TimeRolloverLog.IllegalRoll";

  public static final String TIMEROLLOVER_CANNOT_ROLL_MESSAGE = "TimeRolloverLog.CannotRoll";


  public static final String BASICLOG_CANNOT_CONFIGURE_POLICY_MESSAGE = "BasicLogger.CannotConfigurePolicy";

  public static final String BASICLOG_CANNOT_LOAD_POLICY_MESSAGE = "BasicLogger.CannotLoadPolicy";

  public static final String BASICLOG_CANNOT_CONFIGURE_FORMATTER_MESSAGE = "BasicLogger.CannotConfigureFormatter";

  public static final String BASICLOG_CANNOT_LOAD_FORMATTER_MESSAGE = "BasicLogger.CannotLoadFormatter";


  public static final String MAILLOG_CANNOT_CONFIGURE_SUBJECT_FORMAT_MESSAGE = "MailLog.CannotConfigureSubjectFormat";


  public static final String JMX_STOPPED_MESSAGE = "JMX.Stopped";

  public static final String JMX_STOPPING_MESSAGE = "JMX.Stopping";

  public static final String JMX_STARTING_MESSAGE = "JMX.Starting";

  public static final String JMX_STARTED_MESSAGE = "JMX.Started";

  public static final String JMX_UNKNOWN_MESSAGE = "JMX.Unknown";

  public static final String JMX_CANNOT_LOAD_FILE_MESSAGE = "JMX.CannotLoadFile";

  public static final String LENGTHRO_CANNOT_ROLL_MESSAGE = "LengthRolloverLog.CannotRoll";

  public static final String UNIX_CANNOT_INIT_SOCKET = "UNIX.CannotInitSocket";

  public static final String UNIX_CANNOT_WRITE_MESSAGE = "UNIX.CannotWriteMessage";

  public static final String UNIX_DEFAULT_TAG = "UNIX.DefaultTag";

  public static final String UNIX_UNKNOWN_HOST = "UNIX.UnknownHost";

  public static final String UNIX_BAD_FACILITY = "UNIX.BadFacility";

  public static final String UNIX_BAD_PORT = "UNIX.BadPort";

  public static final String UNIX_BAD_SYSLOG_SEVERITY = "UNIX.BadSyslogSeverity";

  public static final String UNIX_BAD_UNIX_SEVERITY = "UNIX.BadUNIXSeverity";

  public static final String UNIX_BAD_SEVERITY_MAP = "UNIX.BadSeverityMap";

  private MessageConstants()
  {
    super();
  }

  /**
   *  Display values for known messages.
   */
  public static void main(String args[])
  throws Exception
  {
    Class c = MessageConstants.class;
    System.out.println("Constants class: " + c.getName());
    System.out.println("Locale:          " + Locale.getDefault());
    System.out.println("");
    Field fields[] = c.getDeclaredFields();
    int count = 0;
    for (int i=0; i<fields.length; i++)
    {
      if (fields[i].getType() == String.class)
      {
        String name = fields[i].getName();
        String value = (String)fields[i].get(null);
        String val = Syslog.getResourceString(value);
        System.out.println("Variable name:   " + name);
        System.out.println("        value:   " + value);
        System.out.println("     property:   " + val);
        System.out.println("");
        count++;
      }
    }
    System.out.println("Found " + count + " messages.");
  }
}
