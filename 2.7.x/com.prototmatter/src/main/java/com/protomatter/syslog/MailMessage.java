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
import com.protomatter.util.*;

/**
 *  A simple mail message object.
 */
class MailMessage
implements java.io.Serializable
{
  private String subject = null;
  private Vector to = null;
  private Vector cc = null;
  private Vector bcc = null;
  private String fromAddress = null;
  private String fromName = null;

  private String bodyString = null;
  private MIMEMessage bodyMime = null;

  /**
   *  Create a new, empty mail message.
   */
  public MailMessage()
  {
    super();
  }

  /**
   *  Set the list of addresses this message is to.
   */
  public void setTo(Vector to)
  {
    this.to = to;
  }
  /**
   *  Get the list of addresses this message is to.
   */
  public Vector getTo()
  {
    return (this.to == null) ? new Vector() : this.to;
  }


  /**
   *  Set the list of addresses this message
   *  is carbon-copied to.
   */
  public void setCC(Vector cc)
  {
    this.cc = cc;
  }
  /**
   *  Get the list of addresses this message
   *  is carbon-copied to.
   */
  public Vector getCC()
  {
    return (this.cc == null) ? new Vector() : this.cc;
  }


  /**
   *  Set the list of addresses this message
   *  is blind carbon-copied to.
   */
  public void setBCC(Vector bcc)
  {
    this.bcc = bcc;
  }
  /**
   *  Get the list of addresses this message
   *  is blind carbon-copied to.
   */
  public Vector getBCC()
  {
    return (this.bcc == null) ? new Vector() : this.bcc;
  }


  /**
   *  Set the from address for this message.
   */
  public void setFromAddress(String from)
  {
    this.fromAddress = from;
  }
  /**
   *  Get the from address for this message.
   */
  public String getFromAddress()
  {
    return this.fromAddress;
  }


  /**
   *  Set the from name for this message.
   */
  public void setFromName(String from)
  {
    this.fromName = from;
  }
  /**
   *  Get the from name for this message.
   */
  public String getFromName()
  {
    return this.fromName;
  }


  /**
   *  Set the subject of the message.
   */
  public void setSubject(String subject)
  {
    this.subject = subject;
  }
  /**
   *  Get the subject of the message.
   */
  public String getSubject()
  {
    return this.subject;
  }

  /**
   *  Set the body of the message.
   */
  public void setBody(String body)
  {
    this.bodyString = body;
    this.bodyMime = null;
  }
  /**
   *  Set the body of the message.
   */
  public void setBody(MIMEMessage body)
  {
    this.bodyMime = body;
    this.bodyString = null;
  }
  /**
   *  Get the body of the message.
   */
  public Object getBody()
  {
    return (this.bodyString != null)
      ? (Object)this.bodyString : (Object)this.bodyMime;
  }

  public String toString()
  {
    StringBuffer b = new StringBuffer();

    b.append("To: ");
    Enumeration e = getTo().elements();
    while (e.hasMoreElements())
    {
      b.append((String)e.nextElement());
      if (e.hasMoreElements())
        b.append(", ");
    }
    b.append("\n\r");


    b.append("From: ");
    b.append(getFromName());
    b.append(" <");
    b.append(getFromAddress());
    b.append(">\n\r");


    e = getCC().elements();
    if (e.hasMoreElements())
    {
      b.append("CC: ");
      while (e.hasMoreElements())
      {
        b.append((String)e.nextElement());
        if (e.hasMoreElements())
          b.append(", ");
      }
      b.append("\n\r");
    }


    e = getBCC().elements();
    if (e.hasMoreElements())
    {
      b.append("BCC: ");
      while (e.hasMoreElements())
      {
        b.append((String)e.nextElement());
        if (e.hasMoreElements())
          b.append(", ");
      }
      b.append("\n\r");
    }


    b.append("Subject: ");
    b.append(getSubject());
    b.append("\n\r");

    b.append("\n\r");
    b.append(getBody());

    return b.toString();
  }
}
