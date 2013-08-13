package com.protomatter.syslog.util.logging;

import java.io.Serializable;
import com.protomatter.syslog.Syslog;

/**
 * See <TT><a target="_top" href="http://java.sun.com/j2se/1.4/docs/api/java/util/logging/Level.html">java.util.logging.Level</A></TT>.
 */
public class Level
implements Serializable
{
    public static final Level OFF = new Level("OFF", 0);

    public static final Level SEVERE = new Level("SEVERE", Syslog.ERROR);

    public static final Level WARNING = new Level("WARNING", Syslog.WARNING);

    public static final Level INFO = new Level("INFO", Syslog.INFO);

    public static final Level CONFIG = new Level("CONFIG", Syslog.DEBUG);

    public static final Level FINE = new Level("FINE", Syslog.DEBUG);

    public static final Level FINER = new Level("FINER", Syslog.DEBUG);

    public static final Level FINEST = new Level("FINEST", Syslog.DEBUG);

    public static final Level ALL = new Level("ALL", Syslog.atOrAbove(Syslog.DEBUG));

    private String name = null;
    private int value = 0;

    private String resourceBundleName = null;

    protected Level(String name, int value)
    {
        this(name, value, null);
    }

    protected Level(String name, int value, String resourceBundleName)
    {
        this.name = name;
        this.value = value;
        this.resourceBundleName = resourceBundleName;
    }

    public String getResourceBundleName()
    {
        return this.resourceBundleName;
    }

    public String getName()
    {
        return this.name;
    }

    public String getLocalizedName()
    {
        return this.name;
    }

    public final String toString()
    {
        return "Level[name=" + name + ", value=" + value + "]";
    }

    public final int intValue()
    {
        return value;
    }

    public static synchronized Level parse(String name)
    throws IllegalArgumentException
    {
        if (name == null)
        {
            // should throw NullPointerException
        }

        if ("OFF".equals(name))
            return OFF;
        else if ("SEVERE".equals(name))
            return SEVERE;
        else if ("WARNING".equals(name))
            return WARNING;
        else if ("INFO".equals(name))
            return INFO;
        else if ("CONFIG".equals(name))
            return CONFIG;
        else if ("FINE".equals(name))
            return FINE;
        else if ("FINER".equals(name))
            return FINER;
        else if ("FINEST".equals(name))
            return FINEST;
        else if ("ALL".equals(name))
            return ALL;

        throw new IllegalArgumentException();
    }
}
