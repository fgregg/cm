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
package com.choicemaker.cm.gui.utils.swing;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

/**
 * A JPanel that Displays a single line of text and an Icon. Both of which come from an Action.
 * The JFeedbackPanel will update its UI to reflect changes in its Action.
 *
 * @author   Arturo Falck
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class JFeedbackPanel extends JPanel{

	//************************ Constants

	private static final long serialVersionUID = 1L;
	public static final Color DEFAULT_WARNING_BACKGROUND = new Color(250,250,220);
	private static final int INTERNAL_BORDER_SIZE = 4;

	//************************ Fields

	private Color warningBackground = DEFAULT_WARNING_BACKGROUND;
	private Color internalBackground;

	//************************ Construction

	/**
     * Creates a JFeedbackPanel where properties are taken from the 
     * <code>Action</code> supplied.
     *
     * @param a the <code>Action</code> used to specify the new button
     *
	 */
	public JFeedbackPanel(Action action){
		final JPanel panel = this;
		panel.setBorder(new EtchedBorder());
		
		final JButton feedbackButton = new JButton(action);
		final JLabel feedbackLabel = new JLabel(feedbackButton.getText());
		
		panel.setLayout(new FlowLayout(FlowLayout.CENTER));
		feedbackButton.setVisible(false);
		panel.add(feedbackButton);	//NOTE: the button must be added to receive events (it is hidden).
		panel.add(feedbackLabel);
		
		feedbackButton.addPropertyChangeListener(new PropertyChangeListener() {
			/**
			 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
			 */
			public void propertyChange(PropertyChangeEvent evt) {
				String text = feedbackButton.getText();
				Icon icon = feedbackButton.getIcon();
				feedbackLabel.setText(text);
				feedbackLabel.setIcon(icon);
				if(icon == null){
					setInternalBackground(getBackground());
				}
				else{
					setInternalBackground(getWarningBackground());
				}
			}
		});
	}

	//************************ Overridden Methods

	/**
	 * Overridden to color the background according to the severity of the Message.
	 * 
	 * Messages that include icons are considdered warning messages and therefore are
	 * displayed over the warningBackgound.
	 * 
	 * @see JFeedbackPanel#getWarningBackground()
	 * @see JFeedbackPanel#setWarningBackground(Color)
	 */
	public void paintComponent(Graphics g){
		super.paintComponent(g);

		Color old = g.getColor();
		g.setColor(getInternalBackground());
		g.fillRect(	Math.min(INTERNAL_BORDER_SIZE,getWidth()),
					Math.min(INTERNAL_BORDER_SIZE,getHeight()),
					Math.max(0,getWidth() - INTERNAL_BORDER_SIZE * 2),
					Math.max(0,getHeight() - INTERNAL_BORDER_SIZE * 2)
		);
		g.setColor(old);
	}

	//************************ Helper Methods

	private Color getInternalBackground(){
		if (internalBackground == null) {
			setInternalBackground(getBackground());
		}

		return internalBackground;
	}
	
	private void setInternalBackground(Color newColor){
		internalBackground = newColor;
		repaint();
	}

	//************************ Access Methods

	/**
	 * Returns the warningBackground.
	 * @return Color
	 */
	public Color getWarningBackground() {
		return warningBackground;
	}

	/**
	 * Sets the warningBackground.
	 * @param warningBackground The warningBackground to set
	 */
	public void setWarningBackground(Color warningBackground) {
		this.warningBackground = warningBackground;
	}

}
