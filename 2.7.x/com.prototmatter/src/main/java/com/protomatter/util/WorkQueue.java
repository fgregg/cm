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
import com.protomatter.syslog.Syslog;
import com.protomatter.pool.*;

/**
 *  A work queue.  Items of work (runnables) are added, and a pool
 *  of threads works them off in the background.
 */
public final class WorkQueue
extends GrowingObjectPool
{
  // just for naming the threads and thread group
  private static int queueNumber = 0;

  private int numThreads = 0;
  private int id = 0;
  private ThreadGroup threadGroup = null;

  private static synchronized int nextQueueNumber()
  {
    return ++queueNumber;
  }

  /**
   *  Create a new workqueue with the given number of worker
   *  threads.
   */
  public WorkQueue(int numThreads)
  {
    this(null, numThreads);
  }

  /**
   *  Create a new workqueue with the given number of worker
   *  threads.
   */
  public WorkQueue(String name, int numThreads)
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

    this.id = nextQueueNumber();
    if (name == null)
      this.threadGroup = new ThreadGroup("WorkQueueGroup[id="+id+"]");
    else
      this.threadGroup = new ThreadGroup("WorkQueueGroup[id="+id+", name="+name+"]");
    this.threadGroup.setDaemon(true);

    this.numThreads = numThreads;
    for (int i=0; i<numThreads; i++)
    {
      WorkQueueThread t = new WorkQueueThread(this, threadGroup, id, name, i);
      t.start();
    }
  }

  public ObjectPoolObject createObjectPoolObject()
  {
    return null;
  }

  /**
   *  Get the number of worker threads associated with this
   *  WorkQueue.
   */
  public int getNumThreads()
  {
    return this.numThreads;
  }

  /**
   *  Get the number of threads that are currently performing work.
   */
  public int getNumWorkers()
  {
    return (this.numThreads - getNumWaiters());
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
   *  Get the next piece of work to do.  This method blocks
   *  until something has been added.
   */
  final Runnable getWork()
  throws Exception
  {
    Object o = checkout();
    if (o instanceof Runnable)
      return (Runnable)o;
    if (o != null)
      return ((WorkQueueOPO)o).getRunnable();
    return null;
  }

  /**
   *  Add a runnable to the list of things to do.
   */
  public final void addWork(Runnable r)
  {
    try
    {
      if (r instanceof ObjectPoolObject)
        checkin((ObjectPoolObject)r);
      else
        checkin(new WorkQueueOPO(r));
    }
    catch (Exception x)
    {
      // never get here...
      Syslog.log(this, x);
    }
  }

  private class WorkQueueOPO
  implements ObjectPoolObject
  {
    private Runnable runnable = null;
    private boolean valid = true;

    public WorkQueueOPO(Runnable r)
    {
      this.runnable = r;
      this.valid = true;
    }

    public Runnable getRunnable()
    {
      return this.runnable;
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
