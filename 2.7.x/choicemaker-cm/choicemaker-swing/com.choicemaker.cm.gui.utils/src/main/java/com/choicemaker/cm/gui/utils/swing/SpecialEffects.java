package com.choicemaker.cm.gui.utils.swing;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.Timer;

public class SpecialEffects {
	
	private static final Logger logger = Logger.getLogger(SpecialEffects.class
			.getName());
	
	private static final String LOG_SOURCE = SpecialEffects.class.getSimpleName();
	
	public static final Color DEFAULT_FLASH_COLOR = Color.RED;

	// milliseconds
	public static final int DEFAULT_TIMER_DELAY = 500;

	// milliseconds
	public static final int DEFAULT_TOTAL_TIME = 1000;

	private SpecialEffects() {
	}

//	public static void flashJButton(final JButton button) {
//		flashJButton(button, DEFAULT_FLASH_COLOR, DEFAULT_TIMER_DELAY,
//				DEFAULT_TOTAL_TIME);
//	}
//
//	public static void flashJButton(final JButton button,
//			final Color flashColor, final int timerDelay, final int totalTime) {
//		flashJComponent(button.getLabel(),flashColor, timerDelay,totalTime);
//	}

	/**
	 * Two red flashes, lasting a total time of 1 second. Equivalent to:
	 * 
	 * <pre>
	 * flashJComponent(field, DEFAULT_FLASH_COLOR, DEFAULT_TIMER_DELAY,
	 * 		DEFAULT_TOTAL_TIME)
	 * </pre>
	 */
	public static void flashJComponent(final JComponent field) {
		flashJComponent(field, DEFAULT_FLASH_COLOR, DEFAULT_TIMER_DELAY,
				DEFAULT_TOTAL_TIME);
	}

	/**
	 * Doesn't work at all?<ul>
	 * <li> No effect on Mac OS X 10.9.5, Java 1.7</li>
	 * <li> Intermittent effect on Windows 8, Java 1.7</li>
	 * </ul>
	 */
	public static void flashJComponent(final JComponent c,
			final Color flashColor, final int timerDelay, final int totalTime) {

		final String METHOD = "flashJComponent";
		logger.entering(LOG_SOURCE, METHOD, new Object[] {
				flashColor, Integer.valueOf(timerDelay), Integer.valueOf(totalTime)
		});

		final int totalCount = totalTime / timerDelay;
		logger.finer("totalCount: " + totalCount);

		javax.swing.Timer timer =
			new javax.swing.Timer(timerDelay, new ActionListener() {
				int count = 0;

				public void actionPerformed(ActionEvent evt) {
					final String METHOD2 = "actionPerformed";
					logger.entering(LOG_SOURCE, METHOD2, Integer.valueOf(count));
					c.requestFocusInWindow();
					if (count % 2 == 0) {
						c.setBackground(flashColor);
					} else {
						c.setBackground(null);
						if (count >= totalCount) {
							((Timer) evt.getSource()).stop();
						}
					}
					count++;
				}
			});
		timer.start();
		
		logger.exiting(LOG_SOURCE, METHOD);
	}

}
