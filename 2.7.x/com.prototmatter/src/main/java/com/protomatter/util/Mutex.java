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

import com.protomatter.syslog.Syslog;
import java.util.*;

/**
 *  A mutex.
 */
public class Mutex
{
  private MutexObject mutex = new MutexObject();
  private MutexToken token = null;
  private Thread currentThread = null;

  /**
   *  Create a new mutex.
   */
  public Mutex()
  {
    super();
  }

  /**
   *  Obtain the lock on the mutex.  This method blocks, and threads
   *  are stacked in the order they call this method.
   */
  public void getLock()
  {
    try
    {
      token = (MutexToken)mutex.checkout();
      this.currentThread = Thread.currentThread();
    }
    catch (Exception x)
    {
      Syslog.log(this, x);
      ; // this cannot happen the way the MutexObject is written.
        // it's part of the generic object pooling stuff.
    }
  }

  /**
   *  Determine if this mutex is currently locked by anyone.
   */
  public boolean isLocked()
  {
    return (this.currentThread != null);
  }

  /**
   *  Get a reference to the thread that currently has the lock.
   *  Returns null if nobody has the lock.
   */
  public Thread getLockingThread()
  {
    return this.currentThread;
  }

  /**
   *  Release the lock on the mutex.  Users of this class should be
   *  nice about using this method -- don't call it unless you've
   *  already called <tt>getLock()</tt>, since this does not check
   *  to make sure the caller is really the lock owner.
   */
  public void releaseLock()
  {
    try
    {
      synchronized (mutex.getSyncObject())
      {
        mutex.checkin(token);
        this.currentThread = null;
      }
    }
    catch (Exception x)
    {
      Syslog.log(this, x);
      ; // this cannot happen the way the MutexObject is written.
        // it's part of the generic object pooling stuff.
    }
  }
}
