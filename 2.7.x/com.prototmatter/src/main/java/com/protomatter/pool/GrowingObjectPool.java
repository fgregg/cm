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
import com.protomatter.syslog.Syslog;

/**
 *  An ObjectPool implementation that has an initial size and an
 *  optional max size.  The pool will grow as needed by blocks
 *  until the max size is reached.
 *
 *  @see ObjectPool
 *  @see SimpleObjectPool
 */
public abstract class GrowingObjectPool
extends SimpleObjectPool
{
  private int maxPoolSize = -1;
  private int initialPoolSize = 0;
  private int createWaitTime = 0;
  private int growBlock = 1;
  private List pool = new ArrayList();

  /**
   *  Initialize the object pool.  Checked out
   *  objects will be monitored.
   */
  public GrowingObjectPool()
  {
    super();
  }

  /**
   *  Initialize the object pool.  Checked out
   *  objects will be monitored if <TT>monitorCheckedOutObjects</TT>
   *  is true.
   */
  public GrowingObjectPool(boolean monitorCheckedOutObjects)
  {
    super(monitorCheckedOutObjects);
  }

  /**
   *  Get the pool.
   */
  protected List getPool()
  {
    return this.pool;
  }

  /**
   *  Get the number of objects in the pool that are available.
   */
  public int getObjectPoolSize()
  {
    return this.pool.size();
  }

  /**
   *  Get the current size of the pool.  Returns (objects available
   *  + objects in use)
   */
  public int getCurrentPoolSize()
  {
    int size = 0;
    synchronized (sync)
    {
      size = getObjectPoolSize() + getObjectsInUse();
    }
    return size;
  }

  /**
   *  Gets the next object from the pool.  If the pool is
   *  empty, it is grown by not more than <tt>poolGrowBlock</tt> elements
   *  up to <tt>maxPoolSize</tt> total.
   *
   *  @exception Exception If there is a problem getting the next
   *  object from the pool -- could be caused by the
   *  createObjectPoolObject() method throwing an exception.
   */
  protected ObjectPoolObject getNextPoolObject()
  throws Exception
  {
    synchronized (sync)
    {
      // pool's not empty -- just get one from there
      if (pool.size() > 0)
      {
        return (ObjectPoolObject)pool.remove(0);
      }
      // grow the pool by "growBlock" elements.
      else if (getCurrentPoolSize() < maxPoolSize)
      {
        int currentPoolSize = getCurrentPoolSize();
        for (int i=1; i<growBlock && (maxPoolSize + growBlock) >= currentPoolSize; i++)
        {
          pool.add(createObjectPoolObject());
          doCreateWait();
        }
        return createObjectPoolObject();
      }
      // special case -- if pool size == -1, grow forever.
      else if (maxPoolSize == -1)
      {
        for (int i=1; i<growBlock; i++)
        {
          pool.add(createObjectPoolObject());
          doCreateWait();
        }
        return createObjectPoolObject();
      }
    }
    // SOL -- better luck next time.
    return null;
  }

  /**
   *  Change the maximum size of the object pool.  The pool will be
   *  shrunk unless the number of objects in use (checked out of
   *  the pool) is larger than the new desired size of the pool or
   *  the desired size is smaller than the initial size of this pool.
   *
   *  @throws PoolException If the number of objects in use is
   *          greater than the desired size of the pool, or if the
   *          desired size is smaller than the initial size for
   *          this pool.
   */
  public void setMaxObjectPoolSize(int size)
  throws PoolException
  {
    synchronized (sync)
    {
      if (size < getObjectsInUse())
      {
        throw new PoolException(PoolResources.getResourceString(MessageConstants.CANNOT_SHRINK_POOL_1));
      }
      if (size < initialPoolSize)
      {
        throw new PoolException(PoolResources.getResourceString(MessageConstants.CANNOT_SHRINK_POOL_2));
      }

      // how many objects get evicted?
      int evict = getCurrentPoolSize() - size;
      if (evict > 0) // need to evict anybody?
      {
        for (int i=0; i<evict; i++)
        {
          ObjectPoolObject o = (ObjectPoolObject)pool.remove(0);
          o.deleteObjectPoolObject(); // should handle cleanup
        }
      }
      maxPoolSize = size;
    }
  }

  private void doCreateWait()
  {
    if (createWaitTime > 0)
    {
      try
      {
        Thread.sleep(createWaitTime);
      }
      catch (InterruptedException x)
      {
        ;
      }
    }
  }

  /**
   *  Initialize the pool.
   *  Reads the following from the Map:<P>
   *  <dl>
   *
   *  <dt><tt>pool.initialSize</tt> (Integer)</dt>
   *  <dd>The initial pool size (default is 0).</dd>
   *
   *  <dt><tt>pool.maxSize</tt> (Integer)</dt>
   *  <dd>The max pool size (default is -1).  If the max
   *  pool size is -1, the pool grows infinitely.</dd>
   *
   *  <dt><tt>pool.growBlock</tt> (Integer)</dt>
   *  <dd>The grow size (default is 1).  When a new
   *  object is needed, this many are created.</dd>
   *
   *  <dt><tt>pool.createWaitTime</tt> (Integer)</dt>
   *  <dd>The time (in ms) to sleep between pool object creates
   *  (default is 0). This is useful for database connection
   *  pools where it's possible to overload the database by
   *  trying to make too many connections too quickly.</dd>
   *
   *  </dl><P>
   *
   *  @exception Exception If there is an exception initializing the pool.
   */
  public void init(Map ht)
  throws Exception
  {
    Integer ips = (Integer)ht.get("pool.initialSize");
    if (ips != null)
      initialPoolSize = ips.intValue();

    Integer mps = (Integer)ht.get("pool.maxSize");
    if (mps != null)
      maxPoolSize = mps.intValue();

    Integer pgb = (Integer)ht.get("pool.growBlock");
    if (pgb != null)
      growBlock = pgb.intValue();

    Integer cwt = (Integer)ht.get("pool.createWaitTime");
    if (cwt != null)
      createWaitTime = cwt.intValue();

    // load up the pool.
    for (int i=0; i<initialPoolSize; i++)
    {
      pool.add(createObjectPoolObject());
    }
  }

  /**
   *  Calls <tt>deleteObjectPoolObject()</tt> on all objects
   *  currently in the pool, and then re-creates the initial
   *  number of them.
   *
   *  @exception Exception If there is an exception re-initializing the pool.
   */
  public void reInitializeObjectPool()
  throws Exception
  {
    synchronized (sync)
    {
      Iterator it = pool.iterator();
      while (it.hasNext())
      {
        ObjectPoolObject obj = (ObjectPoolObject)it.next();
        obj.deleteObjectPoolObject();
      }
      pool.clear();

      for (int i=0; i<initialPoolSize; i++)
      {
        pool.add(createObjectPoolObject());
        doCreateWait();
      }
    }
  }

  /**
   *  @see SimpleObjectPool
   */
  protected void checkinPoolObject(ObjectPoolObject o)
  {
    pool.add(o);
  }

  /**
   *  Get the initial size of the pool.
   */
  public int getInitialObjectPoolSize()
  {
    return this.initialPoolSize;
  }

  /**
   *  Get the maximum number of objects this pool will hold.
   */
  public int getMaxObjectPoolSize()
  {
    return this.maxPoolSize;
  }

  /**
   *  Get the number of objects the pool should grow by when
   *  it needs to grow.
   */
  public int getObjectPoolGrowSize()
  {
    return this.growBlock;
  }

  /**
   *  Get the number of milliseconds to sleep between
   *  creates of new objects for the pool.
   */
  public int getCreateWaitTime()
  {
    return this.createWaitTime;
  }
}
