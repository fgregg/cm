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

/**
 *  A utility class describing a position in a method call stack.
 */
public class StackTraceInfo
{
    /**
     *  Calling method line number is unknown.
     */
    public static int LINE_NUMBER_UNKNOWN = -1;

    /**
     *  The full class name.
     */
    public String className = null;
 
    /**
     *  The method name.
     */
    public String methodName = null;

    /**
     *  The line number, if known.
     */
    public int lineNumber = LINE_NUMBER_UNKNOWN;

    /**
     *  Default constructor.
     */
    public StackTraceInfo()
    {
        this(null, null, LINE_NUMBER_UNKNOWN);
    }

    /**
     *  Utility constructor.
     */
    public StackTraceInfo(String className, String methodName)
    {
        this(className, methodName, LINE_NUMBER_UNKNOWN);
    }

    /**
     *  Utility constructor.
     */
    public StackTraceInfo(String className, String methodName, int line)
    {
        this.className = className;
        this.methodName = methodName;
        this.lineNumber = line;
    }

    /**
     *  Generate a nice display of the stack location.
     *  Output looks like this: "<TT>full.class.Name.methodName():line</TT>"
     *  if the line number is known, and like
     *  "<TT>full.class.Name.methodName()</TT>"
     *  if the line number isn't known.<P>
     *
     *  This is suitable for doing things like
     *  <NOBR>"<TT>Syslog.debug(this, "I'm right here: " + StackTraceUtil.whereAmI());</TT>"</NOBR>
     *  for good debugging statements.  This is
     *  also the format used for when the
     *  <tt><a href="../syslog/Syslog.html#crumb(java.lang.Object)">Syslog.crumb()</a></tt> and
     *  <tt><a href="../syslog/Channel.html#crumb(java.lang.Object)">Channel.crumb()</a></tt>
     *  tracing methods work.
     */
    public String toString()
    {
        StringBuffer b = new StringBuffer(128);
        if (className != null)
        {
            b.append(className);
            if (methodName != null)
            {
                b.append(".");
                b.append(methodName);
                b.append("()");
                if (lineNumber != LINE_NUMBER_UNKNOWN)
                {
                    b.append(":");
                    b.append(lineNumber);
                }
            }
        }
        else
        {
            b.append(UtilResources.getResourceString(MessageConstants.STACK_TRACE_INFO_UNKNOWN));
        }
        return b.toString();
    }
    
    public String getClassAndMethod()
    {
        return className + "." + methodName + "()";
    }

    public String getShortClassAndMethod()
    {
        return trimFromLastPeriod(className) + "." + methodName + "()";
    }
    
    private String trimFromLastPeriod(String s)
    {
        int index = s.lastIndexOf(".");
        if (index != -1)
            return s.substring(index +1);
        return s;
    }
}
