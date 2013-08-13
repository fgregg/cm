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

/**
 *  A worker thread for the <tt>WorkQueue</tt> object.
 *  Threads of this type are started by the WorkQueue to
 *  process elements of work that it's been asked to do.
 *
 *  @see WorkQueue
 */
class WorkQueueThread
extends Thread
{
  private WorkQueue queue = null;
  private int id = 0;

  /**
   *  Create a WorkQueueHandlerThread that gets work from
   *  the given WorkQueueObjectPool (an ObjectPool implementation
   *  that holds elements of work).
   */
  public WorkQueueThread(WorkQueue queue, ThreadGroup group, int qId, String name, int id)
  {
    super(group, "WorkQueueThread[q="+qId+", qName="+name+", id="+id+"]");
    this.queue = queue;
    this.id = id;
    this.setDaemon(true);
  }

  /**
   *  While there's work to do, do it, otherwise we're told to wait
   *  by the ObjectPool.
   */
  public void run()
  {
    WorkQueue q = queue; // local refs are a little faster
    int i=0;
    while (true)
    {
      try
      {
        // this will block until there's something to do.
        Runnable r = q.getWork();
        if (r != null)
        {
          r.run();
        }
        else
        {
          // if there's nothing to do, yield
          Thread.yield();
        }
      }
      catch (Throwable t)
      {
        // should probably log this?
        Syslog.log(this, t);
      }
    }
  }
}
