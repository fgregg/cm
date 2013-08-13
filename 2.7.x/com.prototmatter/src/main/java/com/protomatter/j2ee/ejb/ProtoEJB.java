package com.protomatter.j2ee.ejb;

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

import javax.ejb.*;
import javax.naming.*;

import java.sql.*;
import java.util.*;
import com.protomatter.util.*;
import com.protomatter.syslog.*;

/**
 *  Base class for all EJBs.
 */
public abstract class ProtoEJB
implements EnterpriseBean, SyslogChannelAware
{
  private String[] channelList = null;

  /**
   *  The JNDI name of the syslog channel list.  The value of this
   *  constant is "<TT>Syslog.channelList</TT>".
   *
   *  @see #initSyslogChannelList()
   */
  public final static String COMP_ENV_CHANNEL_LIST = "Syslog.channelList";

  /**
   *  Default constructor.
   */
  public ProtoEJB()
  {
    super();
  }

  /**
   *  Get the list of channels to log to.
   *
   *  @see SyslogChannelAware
   */
  public Object getSyslogChannel()
  {
    return this.channelList;
  }

  /**
   *  Set the list of channels to log messages to.  The
   *  symbolic names "<TT>DEFAULT_CHANNEL</TT>" and
   *  "<TT>ALL_CHANNEL</TT>" are converted to their
   *  internal meanings.
   */
  protected void setChannelList(String[] channelList)
  {
    this.channelList = channelList;
  }

  /**
   *  Set the list of channels to log messages to.  The
   *  symbolic names "<TT>DEFAULT_CHANNEL</TT>" and
   *  "<TT>ALL_CHANNEL</TT>" are converted to their
   *  internal meanings.
   */
  protected void setChannelList(List channelList)
  {
    Object olist[] = channelList.toArray();
    String list[] = new String[olist.length];
    for (int i=0; i<olist.length; i++)
    {
      list[i] = (String)olist[i];
      if (list[i].equals("DEFAULT_CHANNEL"))
        list[i] = Syslog.DEFAULT_CHANNEL;
      else if (list[i].equals("ALL_CHANNEL"))
        list[i] = Syslog.ALL_CHANNEL;
    }
    setChannelList(list);
  }

  /**
   *  Get the environment context for this component.  This method
   *  looks up the <tt>Context</tt> at the "<tt>java:comp/env</tt>"
   *  location.
   */
  protected Context getComponentContext()
  throws NamingException
  {
    Context ctx = new InitialContext();
    return (Context)ctx.lookup("java:comp/env");
  }

  /**
   *  Initialize the syslog channel list from the component context.
   *  Looks for the "<tt>Syslog.channelList</tt>" component environment
   *  entry, and parses it as a comma-separated list of channel names.
   *  The sumbolic names "<TT>DEFAULT_CHANNEL</TT>" and "<TT>ALL_CHANNEL</TT>"
   *  are converted to their internal meanings.<P>
   *
   *  For example, the following snippet from an <tt>ejb-jar.xml</tt>
   *  file:<P>
   *
   *  <TABLE BORDER=1 CELLPADDING=4 CELLSPACING=0 WIDTH="90%">
   *  <TR><TD>
   *  <PRE><B>
   *  ...
   *  &lt;env-entry&gt;
   *    &lt;env-entry-name&gt;Syslog.channelList&lt;/env-entry-name&gt;
   *    &lt;env-entry-type&gt;java.lang.String&lt;/env-entry-type&gt;
   *    &lt;env-entry-value&gt;THIS_CHANNEL, THAT_CHANNEL&lt;/env-entry-value&gt;
   *  &lt;/env-entry&gt;
   *  ...
   *  </B></PRE>
   *  </TD></TR></TABLE><P>
   *
   *  Will set the list of channels to contain "<TT>THIS_CHANNEL</TT>"
   *  and "<TT>THAT_CHANNEL</TT>".  Also, whitespace around the edges
   *  of the value is stripped, so you can do pretty formatting in
   *  the XML file, like this:<P>
   *
   *  <TABLE BORDER=1 CELLPADDING=4 CELLSPACING=0 WIDTH="90%">
   *  <TR><TD>
   *  <PRE><B>
   *  ...
   *  &lt;env-entry&gt;
   *    &lt;env-entry-name&gt;Syslog.channelList&lt;/env-entry-name&gt;
   *    &lt;env-entry-type&gt;java.lang.String&lt;/env-entry-type&gt;
   *    &lt;env-entry-value&gt;
   *      THIS_CHANNEL,
   *      THAT_CHANNEL
   *    &lt;/env-entry-value&gt;
   *  &lt;/env-entry&gt;
   *  ...
   *  </B></PRE>
   *  </TD></TR></TABLE><P>
   *
   *  And get the same result as above.  Refer to the EJB spec,
   *  version 1.1, for more information about the component
   *  environment.<P>
   *
   */
  protected void initSyslogChannelList()
  {
    try
    {
      Context env = getComponentContext();
      String s = (String)env.lookup(COMP_ENV_CHANNEL_LIST);
      s = (s == null) ? "" : s.trim();
      if (s.length() != 0)
      {
        StringTokenizer st = new StringTokenizer(s, ", ");
        Vector list = new Vector();
        while (st.hasMoreTokens())
          list.add(st.nextToken());
        setChannelList(list);
      }
      else
      {
        setChannelList(new String[] { Syslog.DEFAULT_CHANNEL });
      }
    }
    catch (NamingException x)
    {
      setChannelList(new String[] { Syslog.DEFAULT_CHANNEL });
    }
  }

  /**
   *  Close the given SQL Connection.  If there is an exception,
   *  it is logged using Syslog.
   *
   *  @see DatabaseUtil#close(Connection, Object)
   */
  protected boolean close(Connection c)
  {
    return DatabaseUtil.close(c, this);
  }

  /**
   *  Close the given SQL Statement.  If there is an exception,
   *  it is logged using Syslog.
   *
   *  @see DatabaseUtil#close(Statement, Object)
   */
  protected boolean close(Statement s)
  {
    return DatabaseUtil.close(s, this);
  }

  /**
   *  Close the given SQL ResultSet.  If there is an exception,
   *  it is logged using Syslog.
   *
   *  @see DatabaseUtil#close(ResultSet, Object)
   */
  protected boolean close(ResultSet r)
  {
    return DatabaseUtil.close(r, this);
  }
}
