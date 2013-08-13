package com.protomatter.syslog;

/**
 *  The Protomatter Software License, Version 1.0
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
 *  SUCH DAMAGE.
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import java.text.*;
import com.protomatter.util.*;

/**
 *  A logger that will roll files when the minute, hour, day, week or
 *  month change.
 *
 *  @see com.protomatter.syslog.xml.TimeRolloverLog_Helper XML configuration class
 */

public class TimeRolloverLog
            extends BasicLogger
{
    /**
     *  Roll logs on the minute.
     */
    public static int ROLL_MINUTELY = 0;

    /**
     *  Roll logs on the hour.
     */
    public static int ROLL_HOURLY  = 1;

    /**
     *  Roll logs at midnight.
     */
    public static int ROLL_DAILY   = 2;

    /**
     *  Roll logs at midnight at the end of the month.
     */
    public static int ROLL_MONTHLY  = 3;

    /**
     *  Roll logs weekly at midnight on a given day (default is monday).
     */
    public static int ROLL_WEEKLY = 4;

    private Writer out = null;
    private DateFormat namefmt; // format for dates in name of log files
    private Object syncObject = new Object();
    private String basename;
    private long nextRolloverTime = 0;
    private int rolltype;
    private boolean append = true;
    private String extension = "";
    private String nameFormatString;
    private boolean autoFlush = true;
    private File currentFile = null;
    private File previousFile = null;

    // day of week to roll if we're rolling weekly
    private int weeklyRollDay = Calendar.MONDAY;

    /**
     *  Get the roll type.
     */
    public int getRollType()
    {
        return this.rolltype;
    }

    /**
     *  Set the roll type.
     */
    public void setRollType(int type)
    {
        this.rolltype = type;
    }

    /**
     *  Get the day we roll logs on if we're rolling weekly.
     */
    public int getWeeklyRollDay()
    {
        return this.weeklyRollDay;
    }

    /**
     *  Set the day we roll logs on if we're rolling weekly.
     *  This should be <tt>Calendar.MONDAY</tt>, etc.
     */
    public void setWeeklyRollDay(int day)
    {
        if ( (day != Calendar.SUNDAY) &&
             (day != Calendar.MONDAY) &&
             (day != Calendar.TUESDAY) &&
             (day != Calendar.WEDNESDAY) &&
             (day != Calendar.THURSDAY) &&
             (day != Calendar.FRIDAY) &&
             (day != Calendar.SATURDAY))
        {
            throw new IllegalArgumentException(MessageFormat.format(
                  Syslog.getResourceString(MessageConstants.TIMEROLLOVERLOG_BAD_ROLL_DAY_VALUE_MESSAGE),
                  new Object[] { "Calendar.MONDAY", "Calendar.SUNDAY" }));
        }
        this.weeklyRollDay = day;
    }

    /**
     *  Get the base name for the log file.
     */
    public String getBaseFilename()
    {
        return this.basename;
    }

    /**
     *  Set the base name for the log file.
     */
    public void setBaseFilename(String basename)
    {
        this.basename = basename;
    }

    /**
     *  Get the file extension to use.
     */
    public String getFileExtension()
    {
        return this.extension;
    }

    /**
     *  Set the file extension to use.
     */
    public void setFileExtension(String extension)
    {
        this.extension = extension;
    }

    /**
     *  Set if we will append to files that already exist.
     */
    public void setAppend(boolean append)
    {
        this.append = append;
    }

    /**
     *  Determine if we will append to files that already exist.
     */
    public boolean getAppend()
    {
        return this.append;
    }

    /**
     *  Determine if we will automatically flush the writer.
     */
    public boolean getAutoFlush()
    {
        return this.autoFlush;
    }

    /**
     *  Should we will automatically flush the writer?
     */
    public void setAutoFlush(boolean flush)
    {
        this.autoFlush = flush;
    }

    /**
     *  Write log information to the given log, roll when specified.
     *  The file written to will actually be "basenamedate" where
     *  date is the date formatted as "yyyy.MM.dd-HH.mm.ss"
     */
    public TimeRolloverLog(String basename, int roll, String extension)
    {
        this(basename, extension, roll, "yyyy.MM.dd-HH.mm.ss", true, false);
    }

    /**
     *  Write log information to the given log, roll when specified.
     *  The file written to will actually be "basenamedateextension" where
     *  date is the date formatted using the given nameformat.
     */
    public TimeRolloverLog(String basename, String extension, int roll, String nameformat, boolean append, boolean autoFlush)
    {
        this();

        if (roll < 0 || roll > 4)
            throw new IllegalArgumentException(MessageFormat.format(
                                                   Syslog.getResourceString(MessageConstants.TIMEROLLOVERLOG_BAD_ROLL_VALUE_MESSAGE),
                                                   new Object[] { "0", "4" }));

        this.basename = basename;
        this.extension = extension;
        this.rolltype = roll;
        setNameFormat(nameformat);
        this.append = append;
        this.autoFlush = autoFlush;
        rollover(new Date());
    }

    /**
     *  You must call the configure() method to configure this logger
     *  if you use this constructor.
     */
    public TimeRolloverLog()
    {
        super();
    }

    /**
     *  Roll the logs now.
     */
    private void rollover()
    {
        if (nextRolloverTime == 0)
            nextRolloverTime = getNextRolloverTime().getTime();

        rollover(new Date(nextRolloverTime));
    }

    /**
     *  Get the file we were writing to before we rolled.
     */
    public File getPreviousFile()
    {
        return this.previousFile;
    }

    /**
     *  Get the file currently being written to.
     */
    public File getCurrentFile()
    {
        return this.currentFile;
    }

    /**
     *  Roll the logs.  This should only be called by configuration helpers.
     */
    public void rollover(Date d)
    {
        synchronized(syncObject)
        {
            if (out != null)
            {
                try
                {
                    out.write(formatter.getLogFooter());
                }
                catch (IOException x)
                {
                    x.printStackTrace();
                }

            }
            resetDateFormat();
            Writer old = out;

            try
            {
                File f = new File(basename + namefmt.format(d) + extension);
                out = new BufferedWriter(
                          new FileWriter(f.getCanonicalPath(), append));
                out.write(formatter.getLogHeader());

                if (old != null)
                {
                    old.flush();
                    old.close();
                }

                this.previousFile = this.currentFile;
                this.currentFile = f;
            }
            catch (IOException x)
            {
                System.err.println(MessageFormat.format(
                                       Syslog.getResourceString(MessageConstants.TIMEROLLOVER_CANNOT_ROLL_MESSAGE),
                                       new Object[] { "IOException" }));
                x.printStackTrace();
                out = old;
            }

            Date date = getNextRolloverTime();
            nextRolloverTime = date.getTime(); // set the next rollover time.
        }
    }

    /**
     *  Set the dateformat part of the name.
     */
    public void setNameFormat(String fmt)
    {
        this.nameFormatString = fmt;
        namefmt = new SimpleDateFormat(fmt);
        namefmt.setTimeZone(TimeZone.getDefault());
    }

    /**
     *  Get the dateformat part of the name.
     */
    public String getNameFormat()
    {
        return this.nameFormatString;
    }

    public final void log(SyslogMessage message)
    {
        StringBuffer b =
            (message.detail == null) ?
            (new StringBuffer(128)) :
            (new StringBuffer(256));
        formatLogEntry(b, message);

        synchronized (syncObject)
        {
            if (message.time >= nextRolloverTime)
                rollover();

            try
            {
                out.write(b.toString());

                if (autoFlush)
                    out.flush();
            }
            catch (IOException x)
            {
                x.printStackTrace();
            }

        }
    }

    /**
     *  Calculate the next date to rollover the logs
     *  based on how often we should roll.
     */
    public Date getNextRolloverTime()
    {
        GregorianCalendar now = new GregorianCalendar();
        GregorianCalendar c = null;

        if (rolltype == ROLL_MINUTELY)
        {
            now.add(Calendar.MINUTE, 1);
            c = new GregorianCalendar(
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DATE),
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE)
                );
        }
        else if (rolltype == ROLL_HOURLY)
        {
            now.add(Calendar.HOUR_OF_DAY, 1);
            c = new GregorianCalendar(
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DATE),
                    now.get(Calendar.HOUR_OF_DAY),
                    0
                );
        }
        else if (rolltype == ROLL_DAILY)
        {
            now.add(Calendar.DATE, 1);
            c = new GregorianCalendar(
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DATE),
                    0,
                    0
                );
        }
        else if (rolltype == ROLL_MONTHLY)
        {
            now.add(Calendar.MONTH, 1);
            c = new GregorianCalendar(
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    1,
                    0,
                    0
                );
        }
        else if (rolltype == ROLL_WEEKLY)
        {
            // look for the next day that is the
            // given weekly roll day
            while (now.get(Calendar.DAY_OF_WEEK) != weeklyRollDay)
            {
                now.add(Calendar.DATE, 1);
            }
            c = new GregorianCalendar(
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DATE),
                    0,
                    0
                );
            if (c.getTime().before(new Date()))
            {
                c.add(Calendar.DATE, 7);
            }
        }

        return c.getTime();
    }

    /**
     *  Cleanup and prepare for shutdown.
     */
    public void shutdown()
    {
        if (out != null)
        {
            try
            {
                out.write(formatter.getLogFooter());
                out.flush();
                out.close();
                out = null;
            }
            catch (IOException x)
            {
                x.printStackTrace();
            }

        }
    }

    public void flush()
    {
        try
        {
            if (out != null)
                out.flush();
        }
        catch (IOException x)
        {
            x.printStackTrace();
        }
    }
}
