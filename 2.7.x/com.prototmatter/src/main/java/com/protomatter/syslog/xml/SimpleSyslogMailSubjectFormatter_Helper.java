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
import org.jdom.*;

/**
 *  XML configuration helper for <tt>SimpleSyslogMailSubjectFormatter</tt>.
 */
public class SimpleSyslogMailSubjectFormatter_Helper
extends BasicLogger_Helper
{
  /**
   *  Configure this text formatter given the XML element.
   *  The <tt>&lt;SubjectFormat&gt;</tt> element should look like this:<P>
   *
   *  <TABLE BORDER=1 CELLPADDING=4 CELLSPACING=0 WIDTH="90%">
   *  <TR><TD>
   *  <PRE><B>
   *
   *  &lt;SubjectFormat class="com.protomatter.syslog.SimpleSyslogMailSubjectFormatter" &gt;
   *
   *    &lt;showCaller&gt;<i>true|false</i>&lt;/showCaller&gt;
   *    &lt;showChannel&gt;<i>true|false</i>&lt;/showChannel&gt;
   *    &lt;showThreadName&gt;<i>true|false</i>&lt;/showThreadName&gt;
   *    &lt;showHostName&gt;<i>true|false</i>&lt;/showHostName&gt;
   *
   *  &lt;/SubjectFormat&gt;
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
   *  <TD VALIGN=TOP><TT>showCaller</TT></TD>
   *  <TD><tt>true</tt> or <tt>false</tt> -- decide if the caller
   *  name should appear in the log.
   *  </TD>
   *  <TD VALIGN=TOP>no (default is <tt>true</tt>)</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>showChannel</TT></TD>
   *  <TD><tt>true</tt> or <tt>false</tt> -- decide if the channel
   *  name should appear in the log.
   *  </TD>
   *  <TD VALIGN=TOP>no (default is <tt>false</tt>)</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>showThreadName</TT></TD>
   *  <TD><tt>true</tt> or <tt>false</tt> -- decide if the thread
   *  name should appear in the log.
   *  </TD>
   *  <TD VALIGN=TOP>no (default is <tt>false</tt>)</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>showHostName</TT></TD>
   *  <TD><tt>true</tt> or <tt>false</tt> -- decide if the host name
   *  name should appear in the log.
   *  </TD>
   *  <TD VALIGN=TOP>no (default is <tt>false</tt>)</TD>
   *  </TR>
   *
   *  </TABLE><P>
   *
   */
  public void configure(Object o, Element e)
  throws SyslogInitException
  {
    super.configure(o, e);
    SimpleSyslogMailSubjectFormatter format = (SimpleSyslogMailSubjectFormatter)o;

    String tmp = e.getChildTextTrim("showCaller", e.getNamespace());
    if (tmp != null)
        format.setShowCaller("true".equalsIgnoreCase(tmp));

    tmp = e.getChildTextTrim("showChannel", e.getNamespace());
    if (tmp != null)
        format.setShowChannel("true".equalsIgnoreCase(tmp));

    tmp = e.getChildTextTrim("showThreadName", e.getNamespace());
    if (tmp != null)
        format.setShowThreadName("true".equalsIgnoreCase(tmp));

    tmp = e.getChildTextTrim("showHostName", e.getNamespace());
    if (tmp != null)
        format.setShowHostName("true".equalsIgnoreCase(tmp));
  }

  public Element getConfiguration(Object o, Element element)
  {
    Element e = super.getConfiguration(o, element);
    SimpleSyslogMailSubjectFormatter format = (SimpleSyslogMailSubjectFormatter)o;

    element.getChildren().add((new Element("showCaller")).
      setText(String.valueOf(format.getShowCaller())));
    element.getChildren().add((new Element("showChannel")).
      setText(String.valueOf(format.getShowChannel())));
    element.getChildren().add((new Element("showThreadName")).
      setText(String.valueOf(format.getShowThreadName())));
    element.getChildren().add((new Element("showHostName")).
      setText(String.valueOf(format.getShowHostName())));
    return element;
  }
}
