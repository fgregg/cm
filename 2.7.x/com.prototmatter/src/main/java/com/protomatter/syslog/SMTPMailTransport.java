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
import java.net.*;
import java.util.*;
import java.text.*;
import com.protomatter.util.*;

/**
 *  A simple class that talks SMTP to servers.  This and the
 *  <tt>MailMessage</tt> classes were created since we'll want to
 *  create messages independantly of a mail transport, and that's
 *  not possible with the JavaMail API.  This class is thread
 *  safe, but note that multiple parallel calls to the
 *  sending API will cause multiple connections to be made
 *  to the SMTP server.
 */
class SMTPMailTransport
{
  private String hostname = null;
  private int port = 25;
  private String transportAgent = "protomatter-syslog-agent";
  private String CRLF = "\r\n";
  private SimpleDateFormat dateFormat = new SimpleDateFormat ("EEE, d MMM yyyy HH:mm:ss");

  /**
   *  Create a new transport to speak to the given SMTP
   *  server on port 25.
   */
  public SMTPMailTransport(String hostname)
  {
    this(hostname, 25);
    this.hostname = hostname;
  }

  /**
   *  Create a new transport to speak to the given SMTP
   *  server on the given port.
   */
  public SMTPMailTransport(String hostname, int port)
  {
    this.hostname = hostname;
    this.port = port;
  }

  /**
   *  Get the transport agent name.
   */
  public String getTransportAgentName()
  {
    return this.transportAgent;
  }

  /**
   *  Set the transport agent name.
   */
  public void setTransportAgentName(String transportAgent)
  {
    this.transportAgent = transportAgent;
  }

  /**
   *  Send a message.
   */
  public void sendMessage(MailMessage message)
  throws MailException, IOException
  {
    try
    {
      Socket s = new Socket(hostname, port);

      PrintWriter writer = new PrintWriter(s.getOutputStream());
      BufferedReader reader = new BufferedReader(
        new InputStreamReader(s.getInputStream()));

      String UR = Syslog.getResourceString(MessageConstants.MAILLOG_UNEXPECTED_RESPONSE_MESSAGE);

      String line = reader.readLine();
      if (!line.startsWith("2"))
        throw new MailException(MessageFormat.format(UR, new Object[] { line }));

      writer.print("HELO ");
      writer.print(transportAgent);
      writer.print(CRLF);
      writer.flush();

      line = reader.readLine();
      if (!line.startsWith("2"))
        throw new MailException(MessageFormat.format(UR, new Object[] { line }));

      writer.print("MAIL FROM: ");
      writer.print(message.getFromAddress());
      writer.print(CRLF);
      writer.flush();

      line = reader.readLine();
      if (!line.startsWith("2"))
        throw new MailException(MessageFormat.format(UR, new Object[] { line }));


      // add recipients ("To" list)
      Enumeration e = message.getTo().elements();
      while (e.hasMoreElements())
      {
        writer.print("RCPT TO: ");
        writer.print((String)e.nextElement());
        writer.print(CRLF);
        writer.flush();

        line = reader.readLine();
        if (!line.startsWith("2"))
          throw new MailException(MessageFormat.format(UR, new Object[] { line }));
      }

      // add recipients ("CC" list)
      e = message.getCC().elements();
      while (e.hasMoreElements())
      {
        writer.print("RCPT TO: ");
        writer.print((String)e.nextElement());
        writer.print(CRLF);
        writer.flush();

        line = reader.readLine();
        if (!line.startsWith("2"))
          throw new MailException(MessageFormat.format(UR, new Object[] { line }));
      }

      // add recipients ("BCC" list)
      e = message.getBCC().elements();
      while (e.hasMoreElements())
      {
        writer.print("RCPT TO: ");
        writer.print((String)e.nextElement());
        writer.print(CRLF);
        writer.flush();

        line = reader.readLine();
        if (!line.startsWith("2"))
          throw new MailException(MessageFormat.format(UR, new Object[] { line }));
      }

      Object body = message.getBody();

      writer.print("DATA");
      writer.print(CRLF);
      writer.flush();
      line = reader.readLine();
      if (!line.startsWith("3"))
        throw new MailException(MessageFormat.format(UR, new Object[] { line }));

      writer.print("To: ");
      e = message.getTo().elements();
      while (e.hasMoreElements())
      {
        writer.print((String)e.nextElement());
        if (e.hasMoreElements())
          writer.print(", ");
      }
      writer.print(CRLF);

      writer.print("CC: ");
      e = message.getCC().elements();
      if (e.hasMoreElements())
      {
        while (e.hasMoreElements())
        {
          writer.print((String)e.nextElement());
          if (e.hasMoreElements())
            writer.print(", ");
        }
      }
      writer.print(CRLF);

      String fromName = message.getFromName();
      if (fromName == null)
        fromName = "";
      writer.print("From: ");
      writer.print(fromName);
      writer.print(" <");
      writer.print(message.getFromAddress());
      writer.print(">");
      writer.print(CRLF);

      writer.print("Subject: ");
      writer.print(message.getSubject());
      writer.print(CRLF);

      writer.print("Date: ");
      writer.print(dateFormat.format(new Date()));
      writer.print(CRLF);

      if (body instanceof MIMEMessage)
      {
        MIMEMessage mBody = (MIMEMessage)body;
        writer.print("Content-type: multipart/alternative; boundary=\"");
        writer.print(mBody.getBoundary());
        writer.print("\"");
        writer.print(CRLF);
      }

      writer.print(CRLF);
      writer.print(convertText(message.getBody().toString()));
      writer.print(CRLF);
      writer.print(CRLF);
      writer.print(".");
      writer.print(CRLF);
      writer.flush();

      line = reader.readLine();
      if (!line.startsWith("2"))
        throw new MailException(MessageFormat.format(UR, new Object[] { line }));

      writer.print("QUIT");
      writer.print(CRLF);
      writer.flush();

      line = reader.readLine();
      if (!line.startsWith("2"))
        throw new MailException(MessageFormat.format(UR, new Object[] { line }));
    }
    catch (Exception x)
    {
      if (x instanceof IOException)
        throw (IOException)x;
      throw new MailException(Syslog.getResourceString(MessageConstants.MAILLOG_TRANSPORT_EXCEPTION_MESSAGE), x);
    }
  }
  
  private String convertText(String text)
  {
      // swaps out end of line markers for CRLF so that
      // the message will behave right in SMTP mail.
      
      String endOfLine = System.getProperty("line.separator");
      if (CRLF.equals(endOfLine))
      {
          // no conversion needed, probably windows.
          return text;
      }
      
      StringBuffer newText = new StringBuffer(text.length() + 512);
      if (endOfLine.length() == 1)
      {
          char eol = endOfLine.charAt(0);
          char c;
          for (int i=0; i<text.length(); i++)
          {
              c = text.charAt(i);
              if (c == eol)
              {
                  newText.append(CRLF);
              }
              else
              {
                  newText.append(c);
              }
          }
          return newText.toString();
      }
      else
      {
          // bah.  inefficient.  we need to replace
          // all occurrances of "endOfLine" with "CRLF"
          
          // this should never happen, though, since every
          // OS uses a single char EOL except windows,
          // which already uses CRLF.
          return text;
      }
  }
}
