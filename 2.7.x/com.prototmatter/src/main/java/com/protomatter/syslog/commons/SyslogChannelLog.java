package com.protomatter.syslog.commons;

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

import org.apache.commons.logging.*;
import com.protomatter.syslog.*;
import com.protomatter.util.*;

/**
 *  An implementation of the <tt>org.apache.commons.logging.Log</tt> interface.
 */
public class SyslogChannelLog
implements Log
{
    /**
     *  A <tt>Debug</tt> instance with the same name as this object.
     */
    protected Debug debug = null;

    /**
     *  The name of this <tt>Log</tt> instance.
     */
    protected String name = null;

    /**
     *  Create a new log adapter with the given name.
     */
    public SyslogChannelLog(String name)
    {
        super();

        this.name = name;
        this.debug = Debug.forName(name);
    }

    /**
     *  Calls <tt>debug()</tt> on the <tt>Debug</tt> instance
     *  associated with this class.
     *
     *  @see com.protomatter.util.Debug
     */
    public boolean isDebugEnabled()
    {
        return debug.debug();
    }

    /**
     *  Always returns <tt>true</tt>.
     */
    public boolean isErrorEnabled()
    {
        return true;
    }

    /**
     *  Always returns <tt>true</tt>.
     */
    public boolean isFatalEnabled()
    {
        return true;
    }

    /**
     *  Calls <tt>info()</tt> on the <tt>Debug</tt> instance
     *  associated with this class.
     */
    public boolean isInfoEnabled()
    {
        return debug.info();
    }

    /**
     *  Calls <tt>trace()</tt> on the <tt>Debug</tt> instance
     *  associated with this class.
     *
     *  @see com.protomatter.util.Debug
     */
    public boolean isTraceEnabled()
    {
        return debug.trace();
    }

    /**
     *  Always returns <tt>true</tt>.
     */
    public boolean isWarnEnabled()
    {
        return true;
    }


    /**
     *
     */
    public void trace(Object message)
    {
        Syslog.log(Syslog.getLocalHostName(),
            this, this.name, message, null, Syslog.DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *
     */
    public void trace(Object message, Throwable t)
    {
        Syslog.log(Syslog.getLocalHostName(),
            this, this.name, message, t, Syslog.DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *
     */
    public void debug(Object message)
    {
        Syslog.log(Syslog.getLocalHostName(),
            this, this.name, message, null, Syslog.DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *
     */
    public void debug(Object message, Throwable t)
    {
        Syslog.log(Syslog.getLocalHostName(),
            this, this.name, message, t, Syslog.DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *
     */
    public void info(Object message)
    {
        Syslog.log(Syslog.getLocalHostName(),
            this, this.name, message, null, Syslog.INFO,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *
     */
    public void info(Object message, Throwable t)
    {
        Syslog.log(Syslog.getLocalHostName(),
            this, this.name, message, t, Syslog.INFO,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *
     */
    public void warn(Object message)
    {
        Syslog.log(Syslog.getLocalHostName(),
            this, this.name, message, null, Syslog.WARNING,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *
     */
    public void warn(Object message, Throwable t)
    {
        Syslog.log(Syslog.getLocalHostName(),
            this, this.name, message, t, Syslog.WARNING,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *
     */
    public void error(Object message)
    {
        Syslog.log(Syslog.getLocalHostName(),
            this, this.name, message, null, Syslog.ERROR,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *
     */
    public void error(Object message, Throwable t)
    {
        Syslog.log(Syslog.getLocalHostName(),
            this, this.name, message, t, Syslog.ERROR,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *
     */
    public void fatal(Object message)
    {
        Syslog.log(Syslog.getLocalHostName(),
            this, this.name, message, null, Syslog.FATAL,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    /**
     *
     */
    public void fatal(Object message, Throwable t)
    {
        Syslog.log(Syslog.getLocalHostName(),
            this, this.name, message, t, Syslog.FATAL,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }
}
