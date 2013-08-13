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
 *  A benchmarking utility class.  Basically encapsulates a name,
 *  start time and end time.<P>
 *
 *  Usually used like this:<P>
 *
 *  <PRE>
 *  OpTimer timer = new OpTimer("Some long operation");
 *  ...
 *  // a lot of junk
 *  ...
 *  timer.stop();
 *  Syslog.debug(this, timer);
 *  </PRE><P>
 *
 *  This class can be used in conjunction with the
 *  {@link OpTimerUtil OpTimerUtil} class, which can
 *  parse log files for timer traces and produce
 *  aggregate timing information.
 *
 *  @see OpTimerUtil
 */
public class OpTimer
implements java.io.Serializable
{
  private String name = null;
  private long start = 0;
  private long stop = 0;
  private boolean showThread = true;
  private String threadName = null;

  /**
   *  Create a new timer with the given name.  The timer
   *  is started when it is created.
   */
  public OpTimer(String name)
  {
    this(name, true);
  }

  /**
   *  Create a new timer with the given name.  The timer
   *  is started when it is created.
   */
  public OpTimer(String name, boolean showThread)
  {
    this.name = name;
    this.showThread = showThread;
    start();
  }

  /**
   *  Re-start the timer.  There's no need to call
   *  this method if you're not re-using timer instances.
   *  Returns this timer for convenience.
   */
  public OpTimer start()
  {
    this.start = System.currentTimeMillis();
    this.stop = 0;
    if (showThread)
      this.threadName = Thread.currentThread().getName();
    return this;
  }

  /**
   *  Return elapsed time.
   */
  public long elapsed()
  {
    return (this.stop != 0)
         ? (this.stop - this.start)
         : (System.currentTimeMillis() - this.start);
  }

  /**
   *  Get the name of this timer.
   */
  public String getName()
  {
    return this.name; }

  /**
   *  Set the name of this timer.
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   *  Get the start time of this timer.
   */
  public long startTime()
  {
    return this.start;
  }

  /**
   *  Get the time this timer was stopped.  If
   *  the timer is still running, the stop time
   *  will be <TT>0</TT>.
   */
  public long stopTime()
  {
    return this.stop;
  }

  /**
   *  Stop the timer.  Returns this timer instance for convenience.
   */
  public OpTimer stop()
  {
    this.stop = System.currentTimeMillis();
    return this;
  }

  public String toString()
  {
    long now = System.currentTimeMillis();
    return "OpTimer[" + this.name + ", " +
      (showThread ? ("thread=" + threadName + ", ") : "") +
      ((stop == 0) ? "still running, " : "") +
      "took " +
      ((this.stop != 0)
      ? (this.stop - this.start)
      : (now - this.start)) +
      "ms]";
  }
}
