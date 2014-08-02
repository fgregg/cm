/*
 * Copyright (c) 2001, 2009 ChoiceMaker Technologies, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ChoiceMaker Technologies, Inc. - initial API and implementation
 */
package com.choicemaker.cm.modelmaker.gui.utils;

import java.awt.Frame;
import java.io.IOException;
import java.io.Writer;

import javax.swing.SwingUtilities;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.util.LoggingObject;
import com.choicemaker.cm.gui.utils.dialogs.ErrorDialog;

/**
 *
 * @author    
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:10 $
 */
public class Log4jAppender extends AppenderSkeleton {
	private Logger attachedTo;
	private Writer writer;
	private Frame frame;

	public Log4jAppender(Frame frame, Writer writer) {
		this.writer = writer;
		this.frame = frame;
		setLayout(new PatternLayout("%p %c{1} - %m%n"));
		setThreshold(Level.WARN);
	}

	public void addTo(Object accessorInstance) {
		//		if (accessorInstance == null) {
		//			if (attachedTo != null) {
		//				attachedTo.removeAppender(this);
		//			}
		//		} else {
		//			String name = accessorInstance.getClass().getName();
		//			addTo(name.substring(0, name.lastIndexOf('.')));
		//		}
	}

	public void addTo(String name) {
		if (attachedTo != null) {
			attachedTo.removeAppender(this);
		}
		attachedTo = Logger.getLogger(name);
		attachedTo.addAppender(this);
	}

	protected void append(final LoggingEvent event) {
		final Object message = event.getMessage();
		if (message instanceof LoggingObject) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					LoggingObject logObj = (LoggingObject) message;
					if (event.getThrowableInformation() != null) {
						Throwable ex = event.getThrowableInformation().getThrowable();
						ErrorDialog.showErrorDialog(frame, logObj.getFormattedMessage(), ex);
					} else {
						ErrorDialog.showErrorDialog(frame, logObj.getFormattedMessage());
					}
				}
			});
		} else {
			try {
				writer.write(layout.format(event));
				String[] t = event.getThrowableStrRep();
				if (t != null) {
					for (int i = 0; i < Math.min(2, t.length); ++i) {
						writer.write(t[i] + Constants.LINE_SEPARATOR);
					}
				}
			} catch (IOException ex) {
				// last resort
				ex.printStackTrace();
			}
		}
	}

	public void close() {
		closed = true;
	}

	public boolean requiresLayout() {
		return true;
	}
}
