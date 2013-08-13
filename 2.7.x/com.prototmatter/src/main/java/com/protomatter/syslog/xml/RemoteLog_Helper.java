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
 *  XML configuration helper for <tt>RemoteLog</tt>.
 */
public class RemoteLog_Helper
extends BasicLogger_Helper
{
 /**
  *  Configure this logger given the XML element.
  *  The <tt>&lt;Logger&gt;</tt> element should look like this:<P>
  *
  *  <TABLE BORDER=1 CELLPADDING=4 CELLSPACING=0 WIDTH="90%">
  *  <TR><TD>
  *  <PRE><B>
  *
  *  &lt;Logger class="com.protomatter.syslog.RemoteLog" &gt;
  *
  *    <font color="#888888">&lt;!--
  *     Config params from {@link BasicLogger_Helper#configure(Object,Element) BasicLogger_Helper}
  *     can get inserted here.
  *    --&gt;</font>
  *
  *  &lt;/Logger&gt;
  *  </B></PRE>
  *  </TD></TR></TABLE><P>
  *
  *  This logger has no configuration parameters of it's own.
  *
  */
  public void configure(Object o, Element e)
  throws SyslogInitException
  {
    super.configure(o, e);

    if (Syslog.getLocalHostName() == null)
      Syslog.setLocalHostName();
  }

  public Element getConfiguration(Object o, Element element)
  {
    return super.getConfiguration(o, element);
  }
}
