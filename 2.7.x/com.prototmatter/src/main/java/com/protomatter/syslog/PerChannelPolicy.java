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

import java.util.*;
import java.text.MessageFormat;
import org.apache.oro.text.regex.*;

/**
 *  A policy that can make decision on a per-channel basis.
 *  It maintains a default log mask and channel list
 *  itself, but also has a list of "policy groups" that
 *  each have a log mask and channel list of their own
 *  in addition to a list of channel names that their
 *  mask and channel list applies to.  If a message
 *  coming from a channel in one of those lists matches
 *  the mask and channel list, the message is logged. If
 *  not, but the message severity and channel match the
 *  default mask and channel list, it is also logged.
 *  Otherwise, the message is ignored.<P>
 *
 *  This policy can be used to effectively say that
 *  messages from channels <TT>A</TT> and <TT>B</TT> should be logged
 *  if their severity is <TT>WARNING</TT> or greater, and that
 *  messages from channels <TT>C</TT> and <TT>D</TT> should be logged
 *  if their severity is <TT>INFO</TT> or greater and on a certain
 *  set of channels and that if all else fails, messages at
 *  or above the <TT>INFO</TT> level will be logged.  It's
 *  very configurable.<P>
 *
 *  Each "channel name" is actually a Perl5 regular expression,
 *  so you can match things like "<tt>com.protomatter.syslog.*</tt>"
 *  and other stuff.  This functionality uses the
 *  <a href="http://jakarta.apache.org/oro/index.html">ORO regular expression package</a>,
 *  now part of the
 *  <a href="http://jakarta.apache.org">Apache Jakarta</a> project.<P>
 *
 *  @see com.protomatter.syslog.xml.PerChannelPolicy_Helper XML configuration class
 */
public class PerChannelPolicy
extends SimpleLogPolicy
{
  private List groupList = new ArrayList();
  private static Perl5Compiler patternCompiler = new Perl5Compiler();

  /**
   *  Default constructor.
   */
  public PerChannelPolicy()
  {
    super();
    removeAllChannels();
    setLogMask(0);
  }

  /**
   *  Get the list of policy groups.
   */
  public List getGroupList()
  {
    return this.groupList;
  }
  /**
   *  Set the list of policy groups.
   */
  public void setGroupList(List list)
  {
    this.groupList = list;
  }

  /**
   *  Decide if the message should be logged.  Each
   *  policy group is asked if it should be logged,
   *  and if none of them say yes, then we defer
   *  to our superclass.  Each policy group maintains
   *  a channel list, log mask and a set of channel names
   *  -- to decide if it should log the message,
   *  the policy group first checks the message's
   *  severity and channel against its log mask
   *  and channel list.  If it passes this test,
   *  the policy group checks to see if the
   *  channel name of the logger is in it's list.
   */
  public boolean shouldLog(SyslogMessage message)
  {
    // if any of the policy groups say yes, let it through
    if (message.channel != null)
    {
      int size = groupList.size();
      PolicyGroup group = null;
      for (int i=0; i<size; i++)
      {
        group = (PolicyGroup)groupList.get(i);
        if (group.shouldLog(message))
          return true;
      }
    }

    return false;
  }

  /**
   *  Get the list of policy groups.
   */
  public Iterator getPolicyGroups()
  {
    return this.groupList.iterator();
  }

  /**
   *  Add a policy group to our list.
   */
  public void addPolicyGroup(PolicyGroup group)
  {
    this.groupList.add(group);
  }

  /**
   *  Remove a policy group from our list.
   */
  public void removePolicyGroup(PolicyGroup group)
  {
    this.groupList.remove(group);
  }

  /**
   *  A policy within a policy -- this is exactly like
   *  the SimpleLogPolicy except that it also checks to
   *  see if the channel issuing the log message is
   *  in some set.
   *
   *  @see PerChannelPolicy
   */
  public static class PolicyGroup
  extends SimpleLogPolicy
  {
    private Set patternSet = new HashSet();
    private Set channelNameSet = new HashSet();
    private Pattern pattern = null;
    private Object lock = new Object();
    private Perl5Matcher patternMatcher = new Perl5Matcher();

    /**
     *  Default constructor.
     */
    public PolicyGroup()
    {
      super();
      removeAllChannels();
      setLogMask(Syslog.DEBUG);
    }

    /**
     *  Get the set of channel names (exact matches) we're listing to.
     */
    public Iterator getChannelSet()
    {
      return this.channelNameSet.iterator();
    }

    /**
     *  Clear out the set of channel names
     *  we're listing to.
     */
    public void clearChannelSet()
    {
      this.channelNameSet = new HashSet();
    }

    /**
     *  Add a channelname to the set of channel names
     *  we're listening to.
     */
    public void addChannelName(String channelname)
    {
      this.channelNameSet.add(channelname);
    }

    /**
     *  Remove a channel name from the set of
     *  channel names we're listening to.
     */
    public void removeChannelName(String channelname)
    {
      this.channelNameSet.remove(channelname);
    }

    /**
     *  Get the set of channel patterns we're listing to.
     */
    public Iterator getPatternSet()
    {
      return this.patternSet.iterator();
    }

    /**
     *  Clear out the set of channel name patterns
     *  we're listing to.
     */
    public void clearPatternSet()
    {
      this.patternSet = new HashSet();
      this.pattern = null;
    }

    /**
     *  Add a channelname to the set of channel name
     *  patterns we're listening to.
     */
    public void addChannelPattern(String channelname)
    {
      this.patternSet.add(channelname);
      generatePattern();
    }

    /**
     *  Remove a channel name pattern from the set of
     *  channel name patterns we're listening to.
     */
    public void removeChannelPattern(String channelname)
    {
      this.patternSet.remove(channelname);
      generatePattern();
    }

    /**
     *  Recompile the pattern each time something
     *  about the set of patterns changes.
     */
    private void generatePattern()
    throws IllegalArgumentException
    {
      StringBuffer thePattern = new StringBuffer();
      try
      {
        Iterator patterns = getPatternSet();
        while (patterns.hasNext())
        {
          thePattern.append(patterns.next());
          if (patterns.hasNext())
            thePattern.append("|");
        }
        pattern = patternCompiler.compile(thePattern.toString());
      }
      catch (MalformedPatternException x)
      {
        throw new IllegalArgumentException(
          MessageFormat.format(Syslog.getResources().getString(MessageConstants.INVALID_PATTERN_MESSAGE),
          new Object[] { thePattern }));
      }
    }

    /**
     *  Determine if the given message should be
     *  logged.  The message severity and channel
     *  are first checked by our superclass, then
     *  we see if the logger channel name is in our
     *  set.
     */
    public boolean shouldLog(SyslogMessage m)
    {
      // passes the level test.
      if (inMask(m.level, getLogMask()))
      {
        // exact matches...
        if (channelNameSet.contains(m.channel))
        {
          return true;
        }
        else if (pattern != null) // pattern matches
        {
          synchronized (lock)
          {
            if (patternMatcher.contains(m.channel, pattern))
            {
              return true;
            }
          }
        }
      }

      // if it's not in our list, we don't care about it.
      return false;
    }
  }
}
