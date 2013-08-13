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
 *  XML configuration helper for <tt>SimpleLogPolicy</tt>.
 */
public class SimpleLogPolicy_Helper
implements XMLConfigHelper
{
  /**
   *  Configure this policy given the XML element.
   *  The <tt>&lt;Policy&gt;</tt> element should look like this:<P>
   *
   *  <TABLE BORDER=1 CELLPADDING=4 CELLSPACING=0 WIDTH="90%">
   *  <TR><TD>
   *  <PRE><B>
   *
   *  &lt;Policy class="<i>PolicyClassName</i>" &gt;
   *
   *    &lt;logMask&gt;<i>LogMask</i>&lt;/logMask&gt;
   *    &lt;channels&gt;<i>ChannelList</i>&lt;/channels&gt;
   *
   *  &lt;/Policy&gt;
   *  </B></PRE>
   *  </TD></TR></TABLE><P>
   *
   *  This class reads the "<tt>logMask</tt>" and "<tt>channels</tt>"
   *  elements.<P>
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
   *  <TD VALIGN=TOP><TT>logMask</TT></TD>
   *  <TD>A log mask string.  Integers are treated as raw masks
   *      and log level names (<TT>DEBUG</TT>, <TT>INFO</TT>,
   *      <tt>WARNING</TT>, <TT>ERROR</TT> and <TT>FATAL</TT>)
   *      are interpreted as at-or-above the given level.
   *  </TD>
   *  <TD VALIGN=TOP>no (default is <tt>INHERIT_MASK</TT>)</TD>
   *  </TR>
   *
   *  <TR CLASS="TableRowColor">
   *  <TD VALIGN=TOP><TT>channels</TT></TD>
   *  <TD>A comma and/or space separated list of channel names.
   *      The constants <TT>DEFAULT_CHANNEL</TT> and <TT>ALL_CHANNEL</TT>
   *      are interpreted as their symbolic values.
   *  </TD>
   *  <TD VALIGN=TOP>no (default is <tt>ALL_CHANNEL</TT>)</TD>
   *  </TR>
   *
   *  </TABLE><P>
   */
  public void configure(Object what, Element e)
  throws SyslogInitException
  {
    SimpleLogPolicy policy = (SimpleLogPolicy)what;

    String tmp = e.getChildTextTrim("logMask", e.getNamespace());
    if (tmp != null)
    {
      policy.setLogMask(tmp);
    }

    tmp = e.getChildTextTrim("channels", e.getNamespace());
    if (tmp == null)
      tmp = "";

    // if they are specifying any channels at all,
    // then make them explicity list the ones they
    // want with no defaults.
    policy.removeAllChannels();
    StringTokenizer st = new StringTokenizer(tmp, ", ");
    while (st.hasMoreTokens())
    {
      String chan = st.nextToken();
      if (chan.equals("ALL_CHANNEL"))
        policy.addChannel(Syslog.ALL_CHANNEL);
      else if (chan.equals("DEFAULT_CHANNEL"))
        policy.addChannel(Syslog.DEFAULT_CHANNEL);
      else
        policy.addChannel(chan);
    }
  }

  public Element getConfiguration(Object o, Element element)
  {
    SimpleLogPolicy policy = (SimpleLogPolicy)o;
    if (element == null)
    {
      element = new Element("Policy");
      element.setAttribute("name", policy.getName());
      element.setAttribute("class", policy.getClass().getName());
    }

    Element param = new Element("channels");
    boolean allChannels = false;
    Iterator i = policy.getChannels();
    while (i.hasNext())
    {
      String channel = (String)i.next();
      if (Syslog.ALL_CHANNEL.equals(channel))
        allChannels = true;
    }

    if (allChannels)
    {
      param.setText("ALL_CHANNEL");
    }
    else
    {
      StringBuffer channelList = new StringBuffer();
      i = policy.getChannels();
      while (i.hasNext())
      {
        channelList.append(i.next());
        if (i.hasNext())
          channelList.append(", ");
      }
      param.setText(channelList.toString());
    }
    element.getChildren().add(param);

    Element mask = new Element("logMask");
    mask.setText(Syslog.getLogMaskAsString(policy.getLogMask()));
    element.getChildren().add(mask);

    return element;
  }
}
