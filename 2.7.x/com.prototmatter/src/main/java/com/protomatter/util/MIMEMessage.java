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
import java.text.*;

/**
 *  A MIME encoded message.
 *  This is basically a collection of MIMEAttachment objects. This
 *  class takes care of the ASCII encoding of the message as a whole,
 *  including the segment boundary, etc...  It does <b><i>NOT</i></b>
 *  take care of any headers other than the Content-Type, which it
 *  always identifies as "MULTIPART/MIXED".
 *  This class can also be used to parse "file upload" information
 *  out of HTML forms.
 *
 *  @see MIMEAttachment
 */
public class MIMEMessage
implements Serializable
{
  private Vector attachments;
  private String boundary;
  private static String CRLF = "\r\n";

  /**
   *  Initialize the MIMEMessage.
   */
  public MIMEMessage()
  {
    attachments = new Vector();
    boundary = "--------------74329329-84328432-279-4382"; // some gibberish
  }

  /**
   *  Get the Content-Type of this message, also includes the boundary.
   */
  public String getContentType()
  {
    return "MULTIPART/MIXED; BOUNDARY=\"" + boundary + "\"";
  }

  /**
   *  Add an attachment to this message
   */
  public void addAttachment(MIMEAttachment a)
  {
    attachments.addElement(a);
  }

  /**
   *  Remove an attachment to this message
   */
  public void removeAttachment(MIMEAttachment a)
  {
    attachments.removeElement(a);
  }

  /**
   *  Get an enumeration of the attachments to this message.
   */
  public Enumeration getAttachments()
  {
    return attachments.elements();
  }

  /**
   *  Get the boundary between parts of the message.
   */
  public String getBoundary()
  {
    return boundary;
  }

  /**
   *  Set the boundary between parts of the message.
   */
  public void setBoundary(String boundary)
  {
    this.boundary = boundary;
  }

  /**
   *  Return the encoded message (including all attachments)
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
   *  Write this message to the given output stream.
   */
  public void write(PrintWriter w)
  {
    Enumeration e = getAttachments();
    while (e.hasMoreElements())
    {
      MIMEAttachment a = (MIMEAttachment)e.nextElement();
      w.print("--");
      w.print(boundary);
      w.print(CRLF);
      a.write(w);
      w.print(CRLF);
    }
    w.print("--");
    w.print(boundary);
    w.print("--");
    w.print(CRLF);
  }

  /**
   *  Return a MIMEMessage built from the InputStream that
   *  points to a MIME message.  Reads the stream fully
   *  before parsing, so watch out.
   */
  public static MIMEMessage parse(InputStream s)
  throws MIMEException
  {
    byte[] data = null;
    try
    {
      data = readInputStreamFully(s);
    }
    catch (Exception x)
    {
      throw new MIMEException(MessageFormat.format(
        UtilResources.getResourceString(MessageConstants.MIME_EXCEPTION_IN_INPUT),
        new Object[] { x.toString() }));
    }
    return parse(data);
  }

  /**
   *  Return a MIMEMessage built from the data.
   */
  public static MIMEMessage parse(byte data[])
  throws MIMEException
  {
    try
    {
    MIMEMessage message = new MIMEMessage();

    // set up a vector for passing a second argument back out of methods.
    // v holds the new current index after calls to readLine() and
    // readBody()
    int index = 0;
    int endIndex = data.length -1;

    // MSIE 4.0Bsomething puts in extra whitespace at the front of
    // the file, which does not conform to the specification!
    // In the immortal words of Charlton Heston "Damn You!  Damn You!"
    // also clip whitespace at the end of the message.
    try
    {
      while (Character.isWhitespace((char)data[index])) ++index;
      while (Character.isWhitespace((char)data[endIndex])) --endIndex;
      endIndex++;
    }
    catch (Exception x)
    {
      throw new MIMEException(UtilResources.getResourceString(MessageConstants.MIME_ALL_WHITESPACE));
    }

    // this vector contains the begin and end indexes of the data.  I didn't
    // truncate the data, since it's a waste of time and memory.
    Vector v = new Vector(2);
    v.addElement(new Integer(index));
    v.addElement(new Integer(endIndex));

    // first line is the separator
    String sep = null;
    try
    {
      sep = readLine(data, v);
    }
    catch (Exception x)
    {
      throw new MIMEException(MessageFormat.format(
        UtilResources.getResourceString(MessageConstants.MIME_EXCEPTION_IN_SEPARATOR),
        new Object[] { x.toString() }));
    }
    if (sep == null)
    {
      throw new MIMEException(UtilResources.getResourceString(MessageConstants.MIME_SEPARATOR_NOT_FOUND));
    }

    try
    {
      while (index < endIndex)
      {
        // read headers
        String line = "x";
        Hashtable headers = new Hashtable();
        line = readLine(data, v);
        while (!line.equals("")) // headers are separated from body by a blank line
        {
          // header looks like "name: value"
          int cIndex = line.indexOf(":");
          if (cIndex != -1) // dodge MSIE lameness.
          {
            headers.put(line.substring(0, cIndex), line.substring(cIndex +2));
          }
          line = readLine(data, v);
        }

        // read content
        StringBuffer info = new StringBuffer(); // either "ascii" or "binary"
        byte[] content = readBody(sep, data, info, v);

        // add the attachment to the list.
        if (content != null)
        {
          MIMEAttachment a = new MIMEAttachment();
          //a.setBinary(info.toString().equals("binary"));
          a.setHeaders(headers); // set headers in the attachment.
          String encoding = a.getHeader("Content-Transfer-Encoding");
          if (encoding != null && encoding.equalsIgnoreCase("BASE64"))
          {
            byte[] c = Base64.decode(removeWhitespace(content));
            a.setContent(c);
            a.setBinary(isBinaryContent(c));
          }
          else
          {
            a.setContent(content);
            a.setBinary(isBinaryContent(content));
          }
          message.addAttachment(a); // now add the attachment.
        }
        else
        {
          return message;
        }
        index = getIndex(v);
      }
    }
    catch (Exception x)
    {
      ; // done reading... not a problem.
    }
    return message;
    }
    catch (Exception x)
    {
      throw new MIMEException(MessageFormat.format(
        UtilResources.getResourceString(MessageConstants.MIME_EXCEPTION_IN_PARSE),
        new Object[] { x.toString() }));
    }
  }

  // these should get inlined when -O is used to compile
  private static final int getIndex(Vector v)
  {
    return ((Integer)v.firstElement()).intValue();
  }
  private static final int getEndIndex(Vector v)
  {
    return ((Integer)v.elementAt(1)).intValue();
  }
  private static final void setIndex(Vector v, int i)
  {
    v.setElementAt(new Integer(i), 0);
  }
  private static final void setEndIndex(Vector v, int i)
  {
    v.setElementAt(new Integer(i), 1);
  }

  // reads a line of text.  The end can be any of:
  // CR, LF, CRLF  Sets index in v to be the first char
  // *AFTER* the end of the line marker.  Text returned does
  // *NOT* include the end of line marker.
  private final static String readLine(byte[] data, Vector v)
  throws Exception
  {
    int index = getIndex(v);
    int endIndex = getEndIndex(v);
    if (index == endIndex) return new String();
    int c;
    ByteArrayOutputStream b = new ByteArrayOutputStream();
    while (index < endIndex)
    {
      c = (int)data[index];
      if (isLF(c)) // "\n"
      {
        index++;
        setIndex(v, index);
        return new String(b.toByteArray());
      }
      if (isCR(c)) // "\r"
      {
        index++;
        if (isLF((int)data[index])) ++index;
        setIndex(v, index);
        return new String(b.toByteArray());
      }
      b.write(c);
      index++;
    }
    setIndex(v, index);
    return new String(b.toByteArray());
  }

  /**
   *  Scan the content and decide if it's binary or ASCII data.
   */
  public static boolean isBinaryContent(byte[] data)
  {
    return isBinaryContent(data, 0, data.length);
  }

  /**
   *  Scan the content and decide if it's binary or ASCII data.
   */
  public static boolean isBinaryContent(byte[] data, int start, int len)
  {
    byte[] d = data;
    for (int i=start; i<len; i++)
    {
      if (((int)d[i]) < 0)
        return true;
    }
    return false;
  }

  //
  // read until, and including, the separator.  Sets index in v to be the first
  // char after any CRLF action after the separator.
  //
  //  reads:
  //   binary data<CRLF>
  //   SEPARATOR<CRLF>
  //
  //  or
  //
  //   binary data<CRLF>
  //   SEPARATOR--<CRLF>
  //
  private final static byte[] readBody(String sep, byte data[], StringBuffer info, Vector v)
  throws MIMEException
  {
    int index = getIndex(v);
    int endIndex = getEndIndex(v);
    int sepLen = sep.length();
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    // assume ascii
    boolean isBinary = false;
    info.insert(0, "ascii");
    info.setLength(5);

    while (index < endIndex)
    {
      // check to see if we're reading binary or ascii
      if ((int)data[index] < 0 && !isBinary) // binary (only do this once)
      {
        info.insert(0, "binary");
        info.setLength(6);
        isBinary = true;
      }

      // if we get a CRLF, check some stuff.
      if ( isCR((int)data[index]) && isLF((int)data[index +1]) ||
           isLF((int)data[index]) && !isCRLF((int)data[index +1]) )
      {
        int skip = 0;
        if (isLF((int)data[index +1]))
          skip = 2;
        else
          skip = 1;

        // look ahead and see if the separator is coming up
        //
        // index
        //  vv     <- sep ->
        // [CR][LF][?][?][?][END]               // MSIE end (incorrect)
        // [CR][LF][?][?][?][-][-][END]         // MSIE end (incorrect)
        // [CR][LF][?][?][?][-][-][CR][LF][END] // netscape end (correct)
        // [CR][LF][?][?][?][CR][LF]            // middle (both)
        //
        // Remember that trailing whitespace was remove above.
        //

        // sepTry is a string we think might be the separator.
        String sepTry = null;
        sepTry = new String(data, index + skip, sepLen);

        // if the attempt at a separator is indeed the separator...
        if (sepTry.equals(sep))
        {
          // separator is the end of the data, or
          // separator is followed by "--" and then the end.
          if ( ((index + skip + sepLen) == endIndex) ||
               ((index + skip + sepLen + 2) == endIndex) )
          {
            setIndex(v, endIndex);
            return buffer.toByteArray();
          }

          // check if this is not the end of the entire message,
          // but is the end of the current attachment's body
          // (separator is followed by CRLF)
          if ( isCR((int)data[skip + index + sepLen]) && isLF((int)data[skip + index + sepLen +1]) ||
               isLF((int)data[skip + index + sepLen]) && !isCRLF((int)data[skip + index + sepLen +1]) )
          {
            // position index after the CRLF action
            if (isLF((int)data[index + sepLen +1]))
              setIndex(v, index + sepLen + 2);
            else
              setIndex(v, index + sepLen + 1);
            return buffer.toByteArray();
          }
          sepTry = null;
          buffer.write(data, index, skip);
          index += skip;
        }

        // separator didn't match... we can fast-forward a bit.
        // all we do here is write the CRLF to the buffer.
        else
        {
          buffer.write(data, index, skip);
          index += skip;
        }
      }
      else
      {
        buffer.write((int)data[index++]);
      }
    }

    // hit the end -- return null -- should not get here.
    setIndex(v, index);
    return buffer.toByteArray();
  }

  //
  // read a stream fully and return it's contents as an
  // array of bytes.
  //
  private static byte[] readInputStreamFully(InputStream is)
  throws IOException
  {
    ByteArrayOutputStream b = new ByteArrayOutputStream();
    {
      int i = 0;
      while ((i = is.read()) != -1)
        b.write(i);
    }
    return b.toByteArray();
  }

  // is the given character a CR?
  private final static boolean isCR(int i)
  {
    return (i == 13);
  }

  // is the given character a LF?
  private final static boolean isLF(int i)
  {
    return (i == 10);
  }

  // is the given character a CR or an LF?
  private final static boolean isCRLF(int i)
  {
    return ((i == 10) || (i == 13));
  }

  private final static byte[] removeWhitespace(byte[] data)
  {
    byte[] d = data;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    for (int i=0; i<d.length; i++)
    {
      if (!Character.isWhitespace((char)d[i]))
        out.write(d[i]);
    }
    return out.toByteArray();
  }

  public static void main(String args[])
  {
    if (args.length == 0)
    {
      System.out.println("Usage: MIMEMessage parse filename");
      System.out.println(" or    MIMEMessage create file1..fileN");
      System.exit(0);
    }

    try
    {
      String cmd = args[0];
      if (cmd.equalsIgnoreCase("parse"))
      {
        BufferedInputStream in = new BufferedInputStream(
          new FileInputStream(new File(args[1])));
        long time = System.currentTimeMillis();
        MIMEMessage m = MIMEMessage.parse(in);
        time = System.currentTimeMillis() - time;
        System.err.println("Parse took " + time + "ms");
        System.err.println("");
        Enumeration e = m.getAttachments();
        while (e.hasMoreElements())
        {
          MIMEAttachment a = (MIMEAttachment)e.nextElement();
          System.err.println("Attachment:");
          System.err.println("  Headers:");
          Enumeration h = a.getHeaderNames();
          while (h.hasMoreElements())
          {
            String header = (String)h.nextElement();
            System.err.println("    " + header + ": " + a.getHeader(header));
          }
          System.err.println("  Info:");
          System.err.println("    Content length: " + a.getContent().length);
          System.err.println("    Binary:         " + a.isBinary());
          System.err.println("");
        }
        System.out.println(m);
      }
      else
      {
        System.err.println("Creating new MIMEMessage");
        MIMEMessage m = new MIMEMessage();
        for (int i=1; i<args.length; i++)
        {
          String file = args[i];
          String type = "unknown";

          ByteArrayOutputStream bout = new ByteArrayOutputStream();
          BufferedInputStream in = new BufferedInputStream(
            new FileInputStream(new File(file)));
          byte[] buffer = new byte[8192];
          int read = 0;
          while ((read = in.read(buffer)) != -1)
            bout.write(buffer, 0, read);

          byte[] data = bout.toByteArray();
          boolean binary = isBinaryContent(data);
          MIMEAttachment a = new MIMEAttachment(type, file,
            data, binary);
          System.err.println("binary = " + binary);
          m.addAttachment(a);
        }

        System.out.println(m);
      }
    }
    catch (Exception x)
    {
      x.printStackTrace();
    }
  }
}
