package com.protomatter.pool;

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
import com.protomatter.util.*;
import com.protomatter.syslog.Syslog;

/**
 *  A minimal implementation of an ObjectPool.
 *  This class provides a non-growing pool that
 *  sleeps the threads that are trying to check
 *  something out of the pool, and wakes them
 *  up in-order when something is checked back in.
 *
 *  @see ObjectPool
 */
public abstract class SimpleObjectPool
implements ObjectPool
{
  /**
   *  An object used for synchronization.
   */
  protected Object sync = new Object();

  private List waiters = new ArrayList();
  private List checkedOutObjects = new ArrayList();
  private boolean monitorCheckedOutObjects = true;

  /**
   *  Initialize the object pool.  Checked out
   *  objects will be monitored.
   */
  public SimpleObjectPool()
  {
    this(true);
  }

  /**
   *  Initialize the object pool.  Checked out
   *  objects will be monitored if <TT>monitorCheckedOutObjects</TT>
   *  is true.
   */
  public SimpleObjectPool(boolean monitorCheckedOutObjects)
  {
    super();
    this.monitorCheckedOutObjects = monitorCheckedOutObjects;
  }

  /**
   *  Get a reference to the object being used
   *  as a synchronization lock.  Subclasses that
   *  need to lock the operation of the pool while
   *  they perform maintenance should synchronize
   *  on this object and then manipulate the pool.
   */
  protected Object getSyncObject()
  {
    return this.sync;
  }

  /**
   *  Get the number of threads waiting for an object to become available.
   */
  public int getNumWaiters()
  {
    return this.waiters.size();
  }

  /**
   *  Get the number of objects that are currently checked out.
   */
  public int getObjectsInUse()
  {
    return this.checkedOutObjects.size();
  }

  /**
   * Add a thread to the queue of threads waiting for the shared object.
   * If, for some reason, the thread is already in the queue, then this
   * call will be ignored --- the thread can only be in the queue once
   * via this method.
   */
  private final void addToWaiters(Thread t)
  {
    synchronized (waiters)
    {
      if (!waiters.contains(t))
      {
        waiters.add(t);
        //Syslog.debug(this, "Adding waiter " + t);
      }
    }
  }

  /**
   *  Remove the given thread from the list of waiters.  This
   *  method should only be called if you know what you're
   *  doing.
   */
  public final void removeWaiter(Thread t)
  {
    synchronized (waiters)
    {
      waiters.remove(t);
    }
  }

  /**
   *  Get the next thread in line and remove it from the waiters.
   */
  private final Thread getNextWaiter()
  {
    synchronized (waiters)
    {
      if (waiters.size() != 0)
        return (Thread)waiters.remove(0);
      return null;
    }
  }

  /**
   *  Implements a first in, first out reservation scheme.  If no
   *  threads are waiting for the object, then the caller never blocks.
   *  If there are waiters, then the caller "get's in line," and waits
   *  for their turn with the object.
   *
   *  @exception Exception If there is a problem checking an object out of the pool.
   */
  public ObjectPoolObject checkout()
  throws Exception
  {
    Thread thread = Thread.currentThread();

    ObjectPoolObject obj = null;
    while (true) // mmmm.... infinity
    {
      synchronized (sync)
      {
        boolean done = false;

        while (!done)
        {
          obj = getNextPoolObject(); // can throw exception

          if (obj != null)
          {
            obj.beforeObjectPoolObjectCheckout();
            // make sure the checked out object is still valid.
            if (obj.isObjectPoolObjectValid())
            {
              if (monitorCheckedOutObjects)
                checkedOutObjects.add(obj);
              return obj;
            }
            else
            {
              obj.deleteObjectPoolObject();
            }
          }
          else
          {
            done = true;
          }
        }
      }

      // if we got here it's because we didn't get
      // anything from the pool.  We'll just wait
      // till someone wakes us up and give it another
      // try.
      synchronized (thread)
      {
        addToWaiters(thread);
        thread.wait();
      }
    }
  }

  /**
   *  Check an object back into the pool.  If there are
   *  threads in the wait queue, the thread that has been waiting
   *  longest will get the shared object next.  The waiter may not receive
   *  the shared object immediately, however.
   *
   *  @exception Exception If there was a problem checking in the object.
   */
  public void checkin(ObjectPoolObject opo)
  throws Exception
  {
    opo.afterObjectPoolObjectCheckin();
    synchronized(sync)
    {
      try
      {
        if (opo.isObjectPoolObjectValid())
          checkinPoolObject(opo); // can throw Exception
        else
          opo.deleteObjectPoolObject();
      }
      finally
      {
        // make sure to remove it from the list of
        // checked out objects.
        if (monitorCheckedOutObjects)
          this.checkedOutObjects.remove(opo);

        // wake up the next guy in line.
        Thread waiter = getNextWaiter();
        if (waiter != null)
        {
          synchronized (waiter)
          {
            waiter.notify();
          }
        }
      }
    }
  }

  /**
   *  Get the list of objects that are currently checked out of the pool.
   */
  protected List getCheckedOutObjects()
  {
    return this.checkedOutObjects;
  }

  /**
   *  This method needs to be implemented by sub-classes.
   *  It should return the next element in the pool, or
   *  null if the pool is empty.  Growable implementations
   *  should use this method to add more elements to the
   *  pool.
   *
   *  @exception Exception If there is a problem getting the
   *   next object for the pool -- this is implementation specific.
   */
  protected abstract ObjectPoolObject getNextPoolObject()
  throws Exception;

  /**
   *  This method needs to be implemented by sub-classes.
   *  It should add the given ObjectPoolObject to the pool.
   *
   *  @exception Exception If there is a problem checking in the
   *   object -- this is implementation specific.
   */
  protected abstract void checkinPoolObject(ObjectPoolObject o)
  throws Exception;

  /**
   *  To be implemented by sub-classes.  This is a factory
   *  method for creating objects that go in this pool.
   *
   *  @exception Exception If there is a problem creating the next
   *  object for the pool -- this is implementation specific.
   */
  protected abstract ObjectPoolObject createObjectPoolObject()
  throws Exception;
}
