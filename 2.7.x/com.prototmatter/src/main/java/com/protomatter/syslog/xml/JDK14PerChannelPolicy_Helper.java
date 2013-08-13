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
import com.protomatter.syslog.*;
import com.protomatter.xml.*;
import org.jdom.*;
import org.jdom.output.*;
import org.jdom.input.*;

/**
 *  XML configuration helper for <tt>JDK14PerChannelPolicy</tt>.
 */
public class JDK14PerChannelPolicy_Helper
extends SimpleLogPolicy_Helper
{
  /**
   *  Configure this policy given the XML element.
   *  This class inherits the policy element parameters
   *  that {@link SimpleLogPolicy_Helper#configure(Object,Element) SimpleLogPolicy_Helper}
   *  understands.  The additional <tt>Policy</tt> elements should
   *  look like this:<P>
   *
   *  <TABLE BORDER=1 CELLPADDING=4 CELLSPACING=0 WIDTH="90%">
   *  <TR><TD>
   *  <PRE><B>
   *
   *  &lt;Policy class="com.protomatter.syslog.JDK14PerChannelPolicy" &gt;
   *
   *    <font color="#888888">&lt;!--
   *     Config params from {@link SimpleLogPolicy_Helper#configure(Object,Element) SimpleLogPolicy_Helper} can
   *     get inserted here.
   *    --&gt;</font>
   *
   *    &lt;PolicyGroup&gt;
   *
   *      <font color="#888888">&lt;!--
   *       Config params from {@link SimpleLogPolicy_Helper#configure(Object,Element) SimpleLogPolicy_Helper} can
   *       get inserted here.
   *      --&gt;</font>
   *
   *      &lt;channelPattern&gt;<i>ChannelPattern-1</i>&lt;/channelPattern&gt;
   *      &lt;channelPattern&gt;<i>ChannelPattern-2</i>&lt;/channelPattern&gt;
   *      ...
   *      &lt;channelPattern&gt;<i>ChannelPattern-N</i>&lt;/channelPattern&gt;
   *
   *      &lt;channelName&gt;<i>ChannelName-1</i>&lt;/channelName&gt;
   *      &lt;channelName&gt;<i>ChannelName-2</i>&lt;/channelName&gt;
   *      ...
   *      &lt;channelName&gt;<i>ChannelName-N</i>&lt;/channelName&gt;
   *    &lt;/PolicyGroup&gt;
   *
   *    <font color="#888888">&lt;!-- Insert more PolicyGroups here --&gt;</font>
   *
   *  &lt;/Policy&gt;
   *  </B></PRE>
   *  </TD></TR></TABLE><P>
   *
   *  This class basically reads each <TT>&lt;PolicyGroup&gt;</tt>
   *  element and creates a new {@link com.protomatter.syslog.JDK14PerChannelPolicy.PolicyGroup PolicyGroup} instance and
   *  configures that instance with the <tt>&lt;PolicyGroup&gt;</tt>
   *  element.<P>
   *
   *  If none of the pattern groups has anything to say about
   *  logging the message, the decision is deferred to the
   *  {@link com.protomatter.syslog.SimpleLogPolicy superclass} and it makes the
   *  decision based on the log mask and channel information
   *  that was defined in the enclosing <tt>Policy</tt> element.<P>
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
   *  <TD VALIGN=TOP><tt>PolicyGroup</tt></TD>
   *  <TD VALIGN=TOP>See {@link com.protomatter.syslog.JDK14PerChannelPolicy.PolicyGroup PolicyGroup} docs.</TD>
   *  <TD VALIGN=TOP><i>no</i></TD>
   *  </TR>
   *
   *  </TABLE><P>
   *
   *  The following example should explain things better:<P>
   *
   *  <TABLE BORDER=1 CELLPADDING=4 CELLSPACING=0 WIDTH="90%">
   *  <TR><TD>
   *  <PRE><B>
   *
   *  &lt;Policy class="com.protomatter.syslog.JDK14PerChannelPolicy" &gt;
   *
   *    <font color="#888888">&lt;!--
   *     Defaults -- if none of the groups below match,
   *     then only allow messages at or above the WARNING level.
   *    --&gt;</font>
   *    &lt;logMask&gt;WARNING&lt;/logMask&gt;
   *    &lt;channels&gt;ALL_CHANNEL&lt;/channels&gt;
   *
   *    <font color="#888888">&lt;!--
   *     Log DEBUG and above messages for messages coming from
   *     the <tt>com.protomatter.xml.*</tt> and <tt>com.protomatter.jdbc.*</tt>
   *     packages, and from the <tt>com.protomatter.util.Mutex</tt> channel.
   *    --&gt;</font>
   *    &lt;PolicyGroup&gt;
   *      &lt;logMask&gt;DEBUG&lt;/logMask&gt;
   *      &lt;channels&gt;ALL_CHANNEL&lt;/channels&gt;
   *
   *      &lt;channelPattern&gt;com.protomatter.xml.*&lt;/channelPattern&gt;
   *      &lt;channelPattern&gt;com.protomatter.jdbc.*&lt;/channelPattern&gt;
   *
   *      &lt;channelName&gt;com.protomatter.util.Mutex&lt;/channelName&gt;
   *    &lt;/PolicyGroup&gt;
   *
   *    <font color="#888888">&lt;!--
   *     Log INFO and above messages for messages coming from
   *     the <tt>com.protomatter.pas.*</tt> and <tt>com.protomatter.util.*</tt>
   *     packages, and from the <CC>com.protomatter.jdbc.pool</TT> channel.
   *    --&gt;</font>
   *    &lt;PolicyGroup&gt;
   *      &lt;logMask&gt;INFO&lt;/logMask&gt;
   *      &lt;channels&gt;ALL_CHANNEL&lt;/channels&gt;
   *
   *      &lt;channelPattern&gt;com.protomatter.pas.*&lt;/channelPattern&gt;
   *      &lt;channelPattern&gt;com.protomatter.util.*&lt;/channelPattern&gt;
   *
   *      &lt;channelName&gt;com.protomatter.jdbc.pool&lt;/channelName&gt;
   *    &lt;/PolicyGroup&gt;
   *
   *    <font color="#888888">&lt;!--
   *     If the channel name contains the word "Debug" then allow
   *     messages at or above the ERROR level.
   *    --&gt;</font>
   *    &lt;PolicyGroup&gt;
   *      &lt;logMask&gt;ERROR&lt;/logMask&gt;
   *      &lt;channels&gt;ALL_CHANNEL&lt;/channels&gt;
   *
   *      &lt;channelPattern&gt;*Debug*&lt;/channelPattern&gt;
   *    &lt;/PolicyGroup&gt;
   *
   *  &lt;/Policy&gt;
   *  </B></PRE>
   *  </TD></TR></TABLE><P>
   *
   */
  public void configure(Object o, Element e)
  throws SyslogInitException
  {
    super.configure(o, e);
    JDK14PerChannelPolicy policy = (JDK14PerChannelPolicy)o;

    List myList = new ArrayList();

    Iterator params = e.getChildren("PolicyGroup", e.getNamespace()).iterator();
    while (params.hasNext())
    {
      Element param = (Element)params.next();
      JDK14PerChannelPolicy.PolicyGroup g = new JDK14PerChannelPolicy.PolicyGroup();
      configureGroup(g, param);
      myList.add(g);
    }

    policy.setGroupList(myList);
  }

  public void configureGroup(JDK14PerChannelPolicy.PolicyGroup g, Element e)
  throws SyslogInitException
  {
    super.configure(g, e);

    Iterator kids = e.getChildren("channelPattern", e.getNamespace()).iterator();
    while (kids.hasNext())
    {
      Element param = (Element)kids.next();
      String value = param.getTextTrim();
      if (value != null)
        g.addChannelPattern(value);
    }

    kids = e.getChildren("channelName", e.getNamespace()).iterator();
    while (kids.hasNext())
    {
      Element param = (Element)kids.next();
      String value = param.getTextTrim();
      if (value != null)
        g.addChannelName(value);
    }
  }

  public Element getConfiguration(Object o, Element element)
  {
    element = super.getConfiguration(o, element);

    JDK14PerChannelPolicy policy = (JDK14PerChannelPolicy)o;

    Iterator i = policy.getPolicyGroups();
    while (i.hasNext())
    {
      JDK14PerChannelPolicy.PolicyGroup g = (JDK14PerChannelPolicy.PolicyGroup)i.next();
      Element e = new Element("PolicyGroup");
      e = getConfigurationGroup(g, e);
      element.getChildren().add(e);
    }
    return element;
  }

  public Element getConfigurationGroup(JDK14PerChannelPolicy.PolicyGroup g, Element element)
  {
    Element e = super.getConfiguration(g, element);

    // fill in the list of channeles.
    Iterator i = g.getPatternSet();
    while (i.hasNext())
    {
      e.getChildren().add(
        (new Element("channelPattern")).setText((String)i.next()));
    }

    // fill in the list of channeles.
    i = g.getChannelSet();
    while (i.hasNext())
    {
      e.getChildren().add(
        (new Element("channelName")).setText((String)i.next()));
    }

    return e;
  }
}
