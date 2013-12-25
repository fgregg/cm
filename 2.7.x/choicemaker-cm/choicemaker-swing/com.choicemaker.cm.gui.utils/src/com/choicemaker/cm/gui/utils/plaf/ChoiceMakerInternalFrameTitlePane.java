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
package com.choicemaker.cm.gui.utils.plaf;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.beans.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.swing.plaf.metal.MetalLookAndFeel;


/**
 * Class that manages a JLF title bar
 * @version 1.49 12/03/01
 * @author Steve Wilson
 * @author Brian Beck
 * @since 1.3
 */

public class ChoiceMakerInternalFrameTitlePane  extends BasicInternalFrameTitlePane {

    protected boolean isPalette = false;  	
    protected Icon paletteCloseIcon;
    protected int paletteTitleHeight;

    private static final Border handyEmptyBorder = new EmptyBorder(0,0,0,0);

    /**
     * Key used to lookup Color from UIManager. If this is null,
     * <code>getWindowTitleBackground</code> is used.
     */
    private String selectedBackgroundKey;
    /**
     * Key used to lookup Color from UIManager. If this is null,
     * <code>getWindowTitleForeground</code> is used.
     */
    private String selectedForegroundKey;
    /**
     * Key used to lookup shadow color from UIManager. If this is null,
     * <code>getPrimaryControlDarkShadow</code> is used.
     */
    private String selectedShadowKey;
    /**
     * Boolean indicating the state of the <code>JInternalFrame</code>s
     * closable property at <code>updateUI</code> time.
     */
    private boolean wasClosable;

    int buttonsWidth = 0;	
    
    ChoiceMakerBumps activeBumps 
        = new ChoiceMakerBumps( 0, 0,
                          MetalLookAndFeel.getPrimaryControlHighlight(),
                          MetalLookAndFeel.getPrimaryControlDarkShadow(),
                          MetalLookAndFeel.getPrimaryControl() );
    ChoiceMakerBumps inactiveBumps 
        = new ChoiceMakerBumps( 0, 0,
                          MetalLookAndFeel.getControlHighlight(),
                          MetalLookAndFeel.getControlDarkShadow(),
                          MetalLookAndFeel.getControl() );
    ChoiceMakerBumps paletteBumps;

    private Color activeBumpsHighlight = MetalLookAndFeel.
                             getPrimaryControlHighlight();
    private Color activeBumpsShadow = MetalLookAndFeel.
                             getPrimaryControlDarkShadow();
					    
    public ChoiceMakerInternalFrameTitlePane(JInternalFrame f) {
        super( f );
    }

    public void addNotify() {
        super.addNotify();
        // This is done here instead of in installDefaults as I was worried
        // that the BasicInternalFrameUI might not be fully initialized, and
        // that if this resets the closable state the BasicInternalFrameUI
        // Listeners that get notified might be in an odd/uninitialized state.
        updateOptionPaneState();
    }

    protected void installDefaults() {
        super.installDefaults();
        setFont( UIManager.getFont("InternalFrame.titleFont") );
        paletteTitleHeight
            = UIManager.getInt("InternalFrame.paletteTitleHeight");
        paletteCloseIcon = UIManager.getIcon("InternalFrame.paletteCloseIcon");
        wasClosable = frame.isClosable();
        selectedForegroundKey = selectedBackgroundKey = null;
    }
    
    protected void uninstallDefaults() {
        super.uninstallDefaults();
        if (wasClosable != frame.isClosable()) {
            frame.setClosable(wasClosable);
        }
    }

    protected void createButtons() {
        super.createButtons();

        Boolean paintActive = frame.isSelected() ? Boolean.TRUE:Boolean.FALSE;
        iconButton.putClientProperty("paintActive", paintActive);
        iconButton.setBorder(handyEmptyBorder);
        iconButton.getAccessibleContext().setAccessibleName(
            UIManager.getString(
                "InternalFrameTitlePane.iconifyButtonAccessibleName"));
    
        maxButton.putClientProperty("paintActive", paintActive);
        maxButton.setBorder(handyEmptyBorder);
        maxButton.getAccessibleContext().setAccessibleName(
            UIManager.getString("InternalFrameTitlePane.maximizeButtonAccessibleName"));
        
        closeButton.putClientProperty("paintActive", paintActive);
        closeButton.setBorder(handyEmptyBorder);
        closeButton.getAccessibleContext().setAccessibleName(
            UIManager.getString("InternalFrameTitlePane.closeButtonAccessibleName"));

        // The palette close icon isn't opaque while the regular close icon is.
        // This makes sure palette close buttons have the right background.
        closeButton.setBackground(MetalLookAndFeel.getPrimaryControlShadow());
    }

    /**
     * Override the parent's method to do nothing. Metal frames do not 
     * have system menus.
     */
    protected void assembleSystemMenu() {}

    /**
     * Override the parent's method to do nothing. Metal frames do not 
     * have system menus.
     */
    protected void addSystemMenuItems(JMenu systemMenu) {}

    /**
     * Override the parent's method avoid creating a menu bar. Metal frames
     * do not have system menus.
     */
    protected void addSubComponents() {
        add(iconButton);
        add(maxButton);
        add(closeButton);
    }

    protected PropertyChangeListener createPropertyChangeListener() {
        return new ChoiceMakerPropertyChangeHandler();
    }
    
    protected LayoutManager createLayout() {
        return new ChoiceMakerTitlePaneLayout();
    }

    class ChoiceMakerPropertyChangeHandler
        extends BasicInternalFrameTitlePane.PropertyChangeHandler
    {
        public void propertyChange(PropertyChangeEvent evt) {
	    String prop = (String)evt.getPropertyName();
            if( prop.equals(JInternalFrame.IS_SELECTED_PROPERTY) ) {
                Boolean b = (Boolean)evt.getNewValue();
                iconButton.putClientProperty("paintActive", b);
                closeButton.putClientProperty("paintActive", b);
                maxButton.putClientProperty("paintActive", b);
            }
            else if ("JInternalFrame.messageType".equals(prop)) {
                updateOptionPaneState();
                frame.repaint();
            }
            super.propertyChange(evt);
        }
    }

    class ChoiceMakerTitlePaneLayout extends TitlePaneLayout {    
        public void addLayoutComponent(String name, Component c) {}
        public void removeLayoutComponent(Component c) {}   
        public Dimension preferredLayoutSize(Container c)  {
            return minimumLayoutSize(c);
        }

        public Dimension minimumLayoutSize(Container c) {
        	Dimension returnValue = SUPERminimumLayoutSize(c);
        	return new Dimension(returnValue.height, returnValue.width);
        }

        public Dimension SUPERminimumLayoutSize(Container c) {
            // Compute width.
            int width = 30;
            if (frame.isClosable()) {
                width += 21;
            }
            if (frame.isMaximizable()) {
                width += 16 + (frame.isClosable() ? 10 : 4);
            }
            if (frame.isIconifiable()) {
                width += 16 + (frame.isMaximizable() ? 2 :
                    (frame.isClosable() ? 10 : 4));
            }
            FontMetrics fm = getFontMetrics(getFont());
            String frameTitle = frame.getTitle();
            int title_w = frameTitle != null ? fm.stringWidth(frameTitle) : 0;
            int title_length = frameTitle != null ? frameTitle.length() : 0;

            if (title_length > 2) {
                int subtitle_w =
                    fm.stringWidth(frame.getTitle().substring(0, 2) + "...");
                width += (title_w < subtitle_w) ? title_w : subtitle_w;
            }
            else {
                width += title_w;
            }

            // Compute height.
            int height = 0;
            if (isPalette) {
                height = paletteTitleHeight;
            } else {
                int fontHeight = fm.getHeight();
                fontHeight += 7;
                Icon icon = frame.getFrameIcon();
                int iconHeight = 0;
                if (icon != null) {
                    // SystemMenuBar forces the icon to be 16x16 or less.
                    iconHeight = Math.min(icon.getIconHeight(), 16);
                }
                iconHeight += 5;
                height = Math.max(fontHeight, iconHeight);
            }

            return new Dimension(width, height);
        } 
    
        public void layoutContainer(Container c) {
            boolean leftToRight = ChoiceMakerUtils.isLeftToRight(frame);
       
            int w = getWidth();
            int x = leftToRight ? w : 0;
            int y = 2;
            int spacing;
      
            // assumes all buttons have the same dimensions
            // these dimensions include the borders
            int buttonHeight = closeButton.getIcon().getIconHeight(); 
            int buttonWidth = closeButton.getIcon().getIconWidth();

            if(frame.isClosable()) {
                if (isPalette) {
                    spacing = 3;
                    x += leftToRight ? -spacing -(buttonWidth+2) : spacing;
                    closeButton.setBounds(x, y, buttonWidth+2, getHeight()-4);
                    if( !leftToRight ) x += (buttonWidth+2);
                } else {
                    spacing = 4;
                    x += leftToRight ? -spacing -buttonWidth : spacing;
                    closeButton.setBounds(2, 2, buttonWidth, buttonHeight);
                    if( !leftToRight ) x += buttonWidth;
                }
            }

//            if(frame.isMaximizable() && !isPalette ) {
//                spacing = frame.isClosable() ? 10 : 4;
//                x += leftToRight ? -spacing -buttonWidth : spacing;
//                maxButton.setBounds(x, y, buttonWidth, buttonHeight);
//                if( !leftToRight ) x += buttonWidth;
//            } 
//        
//            if(frame.isIconifiable() && !isPalette ) {
//                spacing = frame.isMaximizable() ? 2
//                          : (frame.isClosable() ? 10 : 4);
//                x += leftToRight ? -spacing -buttonWidth : spacing;
//                iconButton.setBounds(x, y, buttonWidth, buttonHeight);      
//                if( !leftToRight ) x += buttonWidth;
//            }
        
            buttonsWidth = leftToRight ? w - x : x;
        } 
    }

	public int getVirtualWidth(){
		return super.getHeight();
	}

	public int getVirtualHeight(){
		return super.getWidth();
	}
	
	public void paintChildren(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		
		AffineTransform transform = g2d.getTransform();
		transform.rotate(Math.PI/2, getHeight()/2, getHeight()/2);
		g2d.setTransform(transform);
		
		super.paintChildren(g2d);
	}
	
	/**
	 * 
	 */
	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		
		AffineTransform transform = g2d.getTransform();
		transform.rotate(-Math.PI/2, getHeight()/2, getHeight()/2);
		g2d.setTransform(transform);
		
		SUPERpaintComponent(g2d);
	}

    public void paintPalette(Graphics g)  {
//        boolean leftToRight = ChoiceMakerUtils.isLeftToRight(frame);
//
//        int width = getWidthInternal();
//        int height = getHeightInternal();
//    
//        if (paletteBumps == null) {
//            paletteBumps 
//                = new ChoiceMakerBumps(0, 0,
//                                 MetalLookAndFeel.getPrimaryControlHighlight(),
//                                 MetalLookAndFeel.getPrimaryControlInfo(),
//                                 MetalLookAndFeel.getPrimaryControlShadow() );
//        }
//
//        Color background = MetalLookAndFeel.getPrimaryControlShadow();     
//        Color darkShadow = MetalLookAndFeel.getPrimaryControlDarkShadow();
//
//        g.setColor(background);
//        g.fillRect(0, 0, width, height);
//
//        g.setColor( darkShadow );
//        g.drawLine ( 0, height - 1, width, height -1);
//
//        int xOffset = leftToRight ? 4 : buttonsWidth + 4;
//        int bumpLength = width - buttonsWidth -2*4;
//        int bumpHeight = getHeightInternal()  - 4;
//        paletteBumps.setBumpArea( bumpLength, bumpHeight );
//        paletteBumps.paintIcon( this, g, xOffset, 2);
    }

	/**
	 * NOTE: we are painting on a Graphics that was rotated 90 degrees:
	 * 
	 *  ___
	 * |   |
	 * | X |
	 * |   |
	 * | e |
	 * | l |
	 * | t |
	 * | i |
	 * | T |
	 * |   |  
	 * | I |  
	 * |___|
	 *        __________________
	 *       |                  |
	 *       | I  T i t l e   X |
	 *       |__________________|
	 */
    public void SUPERpaintComponent(Graphics g)  {
        if(isPalette) {
            paintPalette(g);
            return;
        }

        boolean leftToRight = ChoiceMakerUtils.isLeftToRight(frame);
        boolean isSelected = frame.isSelected();

        int virtualWidth = getVirtualWidth();
        int virtualHeight = getVirtualHeight();
    
        Color background = null;
        Color foreground = null;
        Color shadow = null;

        ChoiceMakerBumps bumps;

        if (isSelected) {
            if (selectedBackgroundKey != null) {
                background = UIManager.getColor(selectedBackgroundKey);
            }
            if (background == null) {
                background = MetalLookAndFeel.getWindowTitleBackground();
            }
            if (selectedForegroundKey != null) {
                foreground = UIManager.getColor(selectedForegroundKey);
            }
            if (selectedShadowKey != null) {
                shadow = UIManager.getColor(selectedShadowKey);
            }
            if (shadow == null) {
                shadow = MetalLookAndFeel.getPrimaryControlDarkShadow();
            }
            if (foreground == null) {
                foreground = MetalLookAndFeel.getWindowTitleForeground();
            }
            activeBumps.setBumpColors(activeBumpsHighlight, activeBumpsShadow,
                                      background);
            bumps = activeBumps;
        } else {
            background = MetalLookAndFeel.getWindowTitleInactiveBackground();
            foreground = MetalLookAndFeel.getWindowTitleInactiveForeground();
            shadow = MetalLookAndFeel.getControlDarkShadow();
            bumps = inactiveBumps;
        }

        g.setColor(background);
        g.fillRect(0, 0, virtualWidth, virtualHeight);

        g.setColor( shadow );
        g.drawLine ( 0, virtualHeight - 1, virtualWidth, virtualHeight -1);
        g.drawLine ( 0, 0, 0 ,0);    
        g.drawLine ( virtualWidth - 1, 0 , virtualWidth -1, 0);


        int titleLength = 0;
        int xOffset = leftToRight ? 5 : virtualWidth - 5;
        String frameTitle = frame.getTitle();

        Icon icon = frame.getFrameIcon();
        if ( icon != null ) {
            if( !leftToRight ) 
                xOffset -= icon.getIconWidth();
            int iconY = ((virtualHeight / 2) - (icon.getIconHeight() /2));
            icon.paintIcon(frame, g, xOffset, iconY);
            xOffset += leftToRight ? icon.getIconWidth() + 5 : -5;
        }

        if(frameTitle != null) {
            Font f = getFont();
            g.setFont(f);
            FontMetrics fm = g.getFontMetrics();
//            int fHeight = fm.getHeight();

            g.setColor(foreground);

            int yOffset = ( (virtualHeight - fm.getHeight() ) / 2 ) + fm.getAscent();

            Rectangle actualRect = new Rectangle(0, 0, 0, 0);
            if (frame.isIconifiable()) { actualRect = iconButton.getBounds(); }
            else if (frame.isMaximizable()) { actualRect = maxButton.getBounds(); }
            else if (frame.isClosable()) { actualRect = closeButton.getBounds(); }
            int titleW;

			Rectangle virtualRect = new Rectangle(0, 0, 0, 0);
			virtualRect.height = actualRect.width;
			virtualRect.width = actualRect.height;
			virtualRect.x = virtualWidth - virtualRect.width - actualRect.y;
			virtualRect.y = actualRect.x;

            if( leftToRight ) {
              if (virtualRect.x == 0) {
		virtualRect.x = frame.getWidth()-frame.getInsets().right-2;
	      }
              titleW = virtualRect.x - xOffset - 4;
              frameTitle = getTitle(frameTitle, fm, titleW);
            } else {
              titleW = xOffset - virtualRect.x - virtualRect.width - 4;
              frameTitle = getTitle(frameTitle, fm, titleW);
              xOffset -= SwingUtilities.computeStringWidth(fm, frameTitle);
            }

            titleLength = SwingUtilities.computeStringWidth(fm, frameTitle);
            g.drawString( frameTitle, xOffset, yOffset );
            xOffset += leftToRight ? titleLength + 5  : -5;
        }
  
        int bumpXOffset;
        int bumpLength;
        if( leftToRight ) {
            bumpLength = virtualWidth - buttonsWidth - xOffset - 5;
            bumpXOffset = xOffset;
        } else {
            bumpLength = xOffset - buttonsWidth - 5;
            bumpXOffset = buttonsWidth + 5;
        }
        int bumpYOffset = 3;
        int bumpHeight = getVirtualHeight() - (2 * bumpYOffset);        
        bumps.setBumpArea( bumpLength, bumpHeight );
        bumps.paintIcon(this, g, bumpXOffset, bumpYOffset);
    }
					     				    
    public void setPalette(boolean b) {
        isPalette = b;

	if (isPalette) {
            closeButton.setIcon(paletteCloseIcon);
         if( frame.isMaximizable() )
                remove(maxButton);
            if( frame.isIconifiable() )
                remove(iconButton);
        } else {
 	    closeButton.setIcon(closeIcon);
            if( frame.isMaximizable() )
                add(maxButton);
            if( frame.isIconifiable() )
                add(iconButton);
	}		
	revalidate();
	repaint();
    }		     

    /**
     * Updates any state dependant upon the JInternalFrame being shown in
     * a <code>JOptionPane</code>.
     */
    private void updateOptionPaneState() {
        int type = -2;
        boolean closable = wasClosable;
        Object obj = frame.getClientProperty("JInternalFrame.messageType");

        if (obj == null) {
            // Don't change the closable state unless in an JOptionPane.
            return;
        }
        if (obj instanceof Integer) {
            type = ((Integer) obj).intValue();
        }
        switch (type) {
        case JOptionPane.ERROR_MESSAGE:
            selectedBackgroundKey =
                              "OptionPane.errorDialog.titlePane.background";
            selectedForegroundKey =
                              "OptionPane.errorDialog.titlePane.foreground";
            selectedShadowKey = "OptionPane.errorDialog.titlePane.shadow";
            closable = false;
            break;
        case JOptionPane.QUESTION_MESSAGE:
            selectedBackgroundKey =
                            "OptionPane.questionDialog.titlePane.background";
            selectedForegroundKey =
                    "OptionPane.questionDialog.titlePane.foreground";
            selectedShadowKey =
                          "OptionPane.questionDialog.titlePane.shadow";
            closable = false;
            break;
        case JOptionPane.WARNING_MESSAGE:
            selectedBackgroundKey =
                              "OptionPane.warningDialog.titlePane.background";
            selectedForegroundKey =
                              "OptionPane.warningDialog.titlePane.foreground";
            selectedShadowKey = "OptionPane.warningDialog.titlePane.shadow";
            closable = false;
            break;
        case JOptionPane.INFORMATION_MESSAGE:
        case JOptionPane.PLAIN_MESSAGE:
            selectedBackgroundKey = selectedForegroundKey = selectedShadowKey =
                                    null;
            closable = false;
            break;
        default:
            selectedBackgroundKey = selectedForegroundKey = selectedShadowKey =
                                    null;
            break;
        }
        if (closable != frame.isClosable()) {
            frame.setClosable(closable);
        }
    }
}  
