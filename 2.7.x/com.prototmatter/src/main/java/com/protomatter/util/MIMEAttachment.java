package com.protomatter.util;

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

/**
 *  This class is used in conjunction with the MIMEMessage class
 *  to make a multipart MIME message.  A MIMEAttachment enacpsulates
 *  a single attachment (i.e. an image, a document, etc).  Attached
 *  binaries are encoded using the Base64 method.
 *
 *  @see MIMEMessage
 */
public class MIMEAttachment
implements Serializable
{
  private Hashtable headers = new Hashtable();
  private boolean isBinary = false;
  private byte[] content = null;
  private static String CRLF = "\r\n";

  /**
   *  Create a MIMEAttachment object with the given MIME content type and
   *  description.  The content will not be encoded using Base64.
   */
  public MIMEAttachment(String type, String description, String content)
  {
    this();
    setHeader("Content-Type", type);
    setHeader("Content-Description", description);
    this.content = content.getBytes();
    setBinary(false);
  }

  /**
   *  Create a MIMEAttachment object with the given MIME content type and
   *  description.  The content will be encoded using Base64 if
   *  the <TT>isBinary</TT> flag is <tt>true</TT>.
   */
  public MIMEAttachment(String type, String description, byte[] data, boolean isBinary)
  {
    this(type, description, data);
    setBinary(isBinary);
  }

  /**
   *  Create a MIMEAttachment object with the given MIME type and
   *  description.  The content will be encoded using Base64.
   */
  public MIMEAttachment(String type, String description, byte[] data)
  {
    this();
    setHeader("Content-Type", type);
    setHeader("Content-Description", description);
    this.content = data;
    setBinary(true);
  }

  /**
   *  Create an empty attachment.
   */
  public MIMEAttachment()
  {
    super();
  }

  /**
   *  Set the headers.  Keys and values in
   *  must be strings.
   */
  public void setHeaders(Hashtable headers)
  {
    this.headers = headers;
  }

  /**
   *  Set the flag to indicate that the content
   *  of this attachment is binary.
   */
  public void setBinary(boolean b)
  {
    isBinary = b;
  }

  /**
   *  Set the content of this attachment.
   */
  public void setContent(String content)
  {
    this.content = content.getBytes();
  }

  /**
   *  Set the content of this attachment.
   */
  public void setContent(byte[] content)
  {
    this.content = content;
  }

  /**
   *  Set a header value.
   */
  public void setHeader(String headerName, String headerVal)
  {
    headers.put(headerName, headerVal);
  }

  /**
   *  Remove a header value.
   */
  public void removeHeader(String headerName)
  {
    headers.remove(headerName);
  }

  /**
   *  Get a header value.
   */
  public String getHeader(String headerName)
  {
    String val = (String)headers.get(headerName);
    return val;
  }

  /**
   *  Some headers (such as Content-Disposition) have multiple
   *  key="value" pairs associated with them.  This method allows
   *  you to get at those values easily.  For instance, to get
   *  the "filename" chunk of the "Content-Disposition" header,
   *  call getSubHeader("Content-Disposition", "filename");
   *  If you call it with sub = "", it will retrieve the first
   *  value (which doesn't have a name)
   */
  public String getSubHeader(String name, String sub)
  {
    String h = getHeader(name);
    if (h == null) // not going to find much there.
      return h;
    StringTokenizer st = new StringTokenizer(h, ";");
    if (sub.equals(""))
      return stripSurroundingWhiteSpace(st.nextToken());
    else
      st.nextToken(); // skip the first one.
    while (st.hasMoreTokens())
    {
      StringTokenizer nst = new StringTokenizer(stripSurroundingWhiteSpace(st.nextToken()), "=");
      String key = nst.nextToken();
      String val = nst.nextToken();
      if (key.equals(sub))
      {
        if (val.startsWith("\"") && val.endsWith("\""))
          return val.substring(1, val.length() -1);
        else
          return val;
      }
    }
    // not found.
    return "";
  }

  // used by getSubHeader
  private static String stripSurroundingWhiteSpace(String s)
  {
    int i=0; // index of first non-whitespace char
    int j=0; // index of last non-whitespace char
    for (i=0; i<s.length() && Character.isWhitespace(s.charAt(i)); i++);
    for (j=s.length()-1; j>=0 && Character.isWhitespace(s.charAt(j)); j--);
    return s.substring(i, j+1);
  }

  /**
   *  Produces a chunk of text, including the encoded attachment object
   */
  public String toString()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    write(pw);
    pw.flush();
    return sw.toString();
  }

  /**
   *  Append the content of this attachment to
   *  the given StringBuffer.
   */
  public void write(PrintWriter w)
  {
    Enumeration e = headers.keys();
    while (e.hasMoreElements())
    {
      String hName = (String)e.nextElement();
      String hVal = getHeader(hName);
      w.print(hName);
      w.print(": ");
      w.print(hVal);
      w.print(CRLF);
    }
    if (isBinary())
    {
      if (getHeader("Content-Transfer-Encoding") == null)
      {
        w.print("Content-Transfer-Encoding: BASE64");
        w.print(CRLF);
      }
      w.print(CRLF);
      w.print(CRLF);
      String encoded = Base64.encode(content);

      // split the content up into 65 character wide blocks
      int start = 0;
      int end = 65;
      while (start < encoded.length())
      {
        if (end >= encoded.length())
          end = encoded.length();
        w.print(encoded.substring(start, end));
        w.print(CRLF);
        start = end;
        end += 65;
      }
    }
    else // content == null, contentString != null
    {
      w.print(CRLF);
      w.print(new String(content));
    }
  }

  /**
   *  Return the content.  If it's binary, it's a byte array
   *  of binary data, if it's ASCII, you can just call
   *  <TT>new String(attachmentgetContent())</TT>.
   */
  public byte[] getContent()
  {
    return content;
  }

  /**
   *  Get an Enumeration of the header names.
   */
  public Enumeration getHeaderNames()
  {
    return headers.keys();
  }

  /**
   *  Is the content of the attachment ascii or binary?
   */
  public boolean isBinary()
  {
    return isBinary;
  }
}
