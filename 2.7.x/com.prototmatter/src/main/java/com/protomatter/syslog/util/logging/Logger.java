package com.protomatter.syslog.util.logging;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import com.protomatter.syslog.Syslog;
import com.protomatter.util.StackTraceUtil;

/**
 * See <TT><a target="_top" href="http://java.sun.com/j2se/1.4/docs/api/java/util/logging/Logger.html">java.util.logging.Logger</A></TT>.
 *
 * Note that if you use this class, and don't have Syslog configured to compute
 * the caller class and method, then the caller name in your logs will be
 * incorrect.  This is because the JDK 1.4 logging API doesn't have a mechanism
 * for directly getting a reference to the caller.<P>
 */
public class Logger
{
    public static final Logger global = getLogger(Syslog.DEFAULT_CHANNEL);

    private String name = null;
    private ResourceBundle bundle = null;
    private String bundleName = null;

    private Level level = null;

    protected Logger(String name, String resourceBundleName)
    {
        this.name = name;
        this.bundleName = resourceBundleName;
        if (bundleName != null)
        {
            bundle = ResourceBundle.getBundle(bundleName);
        }
    }

    public static synchronized Logger getLogger(String name)
    {
        return new Logger(name, null);
    }

    public static synchronized Logger getLogger(String name, String resourceBundleName)
    {
        return new Logger(name, resourceBundleName);
    }

    public static synchronized Logger getAnonymousLogger()
    {
        return global;
    }

    public static synchronized Logger getAnonymousLogger(String resourceBundleName)
    {
        return new Logger(Syslog.DEFAULT_CHANNEL, resourceBundleName);
    }

    public ResourceBundle getResourceBundle()
    {
        return bundle;
    }

    public String getResourceBundleName()
    {
        return bundleName;
    }

    public void log(Level level, String message)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name, message, null, level.intValue(),
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void log(Level level, String message, Object param)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name,
            MessageFormat.format(message, new Object[] { param }), null, level.intValue(),
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void log(Level level, String message, Object[] params)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name,
            MessageFormat.format(message, new Object[] { expand(params) }), null, level.intValue(),
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void log(Level level, String message, Throwable t)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name, message, t, level.intValue(),
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void logp(Level level, String className, String methodName, String message)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name, message, null, level.intValue(),
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void logp(Level level, String className, String methodName, String message, Object param)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name,
            MessageFormat.format(message, new Object[] { param }), null, level.intValue(),
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void logp(Level level, String className, String methodName, String message, Object[] params)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name,
            MessageFormat.format(message, new Object[] { expand(params) }), null, level.intValue(),
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void logp(Level level, String className, String methodName, String message, Throwable t)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name, message, t, level.intValue(),
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void logrb(Level level, String className, String methodName, String bundleName, String message)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name, message, null, level.intValue(),
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void logrb(Level level, String className, String methodName, String bundleName, String message, Object param)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name,
            MessageFormat.format(message, new Object[] { param }),
            null, level.intValue(),
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void logrb(Level level, String className, String methodName, String bundleName, String message, Object[] params)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name,
            MessageFormat.format(message, new Object[] { expand(params) }), null, level.intValue(),
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void logrb(Level level, String className, String methodName, String bundleName, String message, Throwable t)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name,
            message, t, level.intValue(),
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void entering(String className, String methodName)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name,
            MessageFormat.format(
                LogResources.getResourceString(MessageConstants.ENTERING_MESSAGE),
                new Object[] { StackTraceUtil.whereAmI(1) }),
            null, Syslog.DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void entering(String className, String methodName, Object param)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name,
            MessageFormat.format(
                LogResources.getResourceString(MessageConstants.ENTERING_WITH_PARAM_MESSAGE),
                new Object[] { StackTraceUtil.whereAmI(1), param }),
            null, Syslog.DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void entering(String className, String methodName, Object[] params)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name,
            MessageFormat.format(
                LogResources.getResourceString(MessageConstants.ENTERING_WITH_PARAM_MESSAGE),
                new Object[] { StackTraceUtil.whereAmI(1), expand(params) }),
            null, Syslog.DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void exiting(String className, String methodName)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name,
            MessageFormat.format(
                LogResources.getResourceString(MessageConstants.EXITING_MESSAGE),
                new Object[] { StackTraceUtil.whereAmI(1) }),
            null, Syslog.DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void exiting(String className, String methodName, Object param)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name,
            MessageFormat.format(
                LogResources.getResourceString(MessageConstants.EXITING_WITH_PARAM_MESSAGE),
                new Object[] { StackTraceUtil.whereAmI(1), param }),
            null, Syslog.DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void throwing(String className, String methodName, Throwable t)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name,
            MessageFormat.format(
                LogResources.getResourceString(MessageConstants.THROWING_MESSAGE),
                new Object[] { t, StackTraceUtil.whereAmI(1) }), t, Syslog.DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void severe(String message)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name, message, null, Syslog.ERROR,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void warning(String message)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name, message, null, Syslog.WARNING,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void info(String message)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name, message, null, Syslog.INFO,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void config(String message)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name, message, null, Syslog.DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void fine(String message)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name, message, null, Syslog.DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void finer(String message)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name, message, null, Syslog.DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void finest(String message)
    {
        Syslog.log(Syslog.getLocalHostName(), this, name, message, null, Syslog.DEBUG,
            Thread.currentThread(), Thread.currentThread().getName(),
            System.currentTimeMillis(), 1);
    }

    public void setLevel(Level level)
    throws SecurityException
    {
        this.level = level;
    }

    public Level getLevel()
    {
        return this.level;
    }

    public boolean isLoggable(Level level)
    {
        return Syslog.inMask(level.intValue(), Syslog.getLogMask());
    }

    public String getName()
    {
        return this.name;
    }

    private Object expand(Object array[])
    {
        StringBuffer b = new StringBuffer(128);
        for (int i=0; i<array.length; i++)
        {
            b.append(array[i]);
            if (i < (array.length -1))
                b.append(", ");
        }
        return b;
    }
}
