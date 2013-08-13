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

import java.util.*;
import com.protomatter.pool.*;
import com.protomatter.syslog.Syslog;

/**
 *  A thread-blocking queue.  The <tt>getNextObject()</tt> method blocks until
 *  there's something added to the queue.
 */
public final class BlockingQueue
extends GrowingObjectPool
{
  /**
   *  Create a new queue.
   */
  public BlockingQueue()
  {
    super(false);
    try
    {
      init(new Hashtable());
    }
    catch (Exception x)
    {
      // never get here.
      Syslog.log(this, x);
    }
  }

  public ObjectPoolObject createObjectPoolObject()
  {
    return null;
  }

  /**
   *  Get the length of the queue (number of work elements yet to
   *  be processed).
   */
  public int getQueueLength()
  {
    return getObjectPoolSize();
  }

  /**
   *  Get the next object in the queue.  This method blocks
   *  until something has been added.
   */
  public final Object getNextObject()
  {
    BlockingQueueOPO o = null;
    while (true)
    {
      try
      {
        o = (BlockingQueueOPO)checkout();
      }
      catch (Exception x)
      {
        ; // nothin to do here but try again.
      }
      if (o != null)
        return o.getObject();
      Thread.yield();
    }
  }

  /**
   *  Add an object to the queue.  If there is a thread blocked waiting
   *  on the <tt>getNextObject()</tt> method, it will be un-blocked.
   */
  public final void add(Object object)
  {
    try
    {
      checkin(new BlockingQueueOPO(object));
    }
    catch (Exception x)
    {
      // never get here.
      Syslog.log(this, x);
    }
  }

  private class BlockingQueueOPO
  implements ObjectPoolObject
  {
    private Object object = null;
    private boolean valid = true;

    public BlockingQueueOPO(Object o)
    {
      this.object = o;
      this.valid = true;
    }

    public Object getObject()
    {
      return this.object;
    }

    public void deleteObjectPoolObject()
    {
      this.valid = false;
    }

    public boolean isObjectPoolObjectValid()
    {
      return this.valid;
    }

    public void beforeObjectPoolObjectCheckout()
    {
    }

    public void afterObjectPoolObjectCheckin()
    {
    }
  }
}
