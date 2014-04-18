package com.choicemaker.maven;

import org.apache.maven.plugin.logging.Log;
import org.sonatype.plexus.build.incremental.BuildContext;

import java.io.File;

import javax.annotation.Nonnull;

/**
 * Reports messages to the {@link Log} instance provided by Maven.
 *
 * @author rphall
 */
public class ClueMaker2ErrorLog {

    private final BuildContext buildContext;
    private final Log log;

    /**
     * Creates an instance of {@link ClueMaker2ErrorLog}.
     *
     * @param log The Maven log
     */
    public ClueMaker2ErrorLog(@Nonnull BuildContext buildContext, @Nonnull Log log) {
        this.buildContext = buildContext;
        this.log = log;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation passes the message to the Maven log.
     *
     * @param message The message to send to Maven
     */
    public void info(String message) {
        log.info(message);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation passes the message to the Maven log.
     *
     * @param message The message to send to Maven.
     */
    public void error(String message) {
        log.error(message);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation passes the message to the Maven log.
     *
     * @param message
     */
    public void warning(String message) {
        log.warn(message);
    }
}
