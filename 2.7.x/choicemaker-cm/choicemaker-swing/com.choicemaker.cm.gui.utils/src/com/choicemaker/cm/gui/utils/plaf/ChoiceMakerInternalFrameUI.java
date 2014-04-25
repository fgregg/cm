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
import java.awt.event.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.plaf.metal.MetalInternalFrameUI;

/**
 * .
 *
 * @author   Arturo Falck
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class ChoiceMakerInternalFrameUI extends MetalInternalFrameUI {

	//************************ Static Methods

	/**
	 * This method is necessary because of the way that sun uses introspection to load
	 * the appropriate UI classes.
	 */
	public static ComponentUI createUI(JComponent c) {
		return new ChoiceMakerInternalFrameUI((JInternalFrame) c);
	}

	/**
	 * Constructor for ChoiceMakerInternalFrameUI.
	 * @param b
	 */
	public ChoiceMakerInternalFrameUI(JInternalFrame b) {
		super(b);
	}

	//************************ Overridden BasicInternalFrameUI Methods and Fields

	private Rectangle parentBounds;

	/**
	 * Overridden to actually add the JComponent... For some reason they commented that code out in the BasicInternalFrameUI.
	 */
	public void setWestPane(JComponent c) {
		replacePane(westPane, c);
		super.setWestPane(c);
	}

	/**
	 * Overridden because for some reason  - David Kloba and Rich Schiavi - decided to
	 * make parentBounds private and not use an accessor to access the variable from within their inner class.
	 */
	protected void installListeners() {
		super.installListeners();
		
		if (frame.getParent() != null) {
			parentBounds = frame.getParent().getBounds();
		}
	}

	/**
	 * Overridden because for some reason  - David Kloba and Rich Schiavi - decided to
	 * make parentBounds private and not use an accessor to access the variable from within their inner class.
	 */
	protected PropertyChangeListener createPropertyChangeListener() {
		return new InternalFramePropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				super.propertyChange(evt);

				String prop = evt.getPropertyName();
				JInternalFrame f = (JInternalFrame) evt.getSource();
				if (prop.equals("ancestor")) {
					if (frame.getParent() != null) {
						parentBounds = f.getParent().getBounds();
					} else {
						parentBounds = null;
					}
				}
			}
		};
	}

	/**
	 * Overridden to introcude a new implementation of the Inner Class.
	 */
	protected MouseInputAdapter createBorderListener(JInternalFrame w) {
		return new AdjustedBorderListener();
	}

	/**
	 * Overridden to redirect the sensitivity from the NorthPane to the WestPane.
	 * 
	 * Unfortunately  - David Kloba and Rich Schiavi - were too short sighted to provide me with some
	 * protected method that I could use to simply redirect their algorithms to the new target, so, I had
	 * to copy their code (which is not the best).
	 * 
	 * I don't understand why they make the JComponent on the North be the sensitive component, ignoring
	 * the fact that they created a TitlePane that is supposed to be responsible for that functionality!
	 */
	protected class AdjustedBorderListener extends BasicInternalFrameUI.BorderListener {
		
		/**
		 * I wish that - David Kloba and Rich Schiavi - had at least done this
		 * 
		 * Introduced this accessor to redirect the sensitivity to the WestPane.
		 */
		protected JComponent getNorthPane(){
			return ChoiceMakerInternalFrameUI.this.getWestPane();
		}
		
		// _x & _y are the mousePressed location in absolute coordinate system
		int _x, _y;
		// __x & __y are the mousePressed location in source view's coordinate system
		int __x, __y;
		Rectangle startingBounds;
		int resizeDir;

		protected final int RESIZE_NONE = 0;
		private boolean discardRelease = false;

		int resizeCornerSize = 16;

		public void mouseClicked(MouseEvent e) {
            if(e.getClickCount() > 1 && e.getSource() == getNorthPane()) {
				if (frame.isIconifiable() && frame.isIcon()) {
					try {
						frame.setIcon(false);
					} catch (PropertyVetoException e2) {
					}
				} else if (frame.isMaximizable()) {
					if (!frame.isMaximum())
						try {
							frame.setMaximum(true);
						} catch (PropertyVetoException e2) {
						} else
						try {
							frame.setMaximum(false);
						} catch (PropertyVetoException e3) {
						}
				}
			}
		}

		public void mouseReleased(MouseEvent e) {
			if (discardRelease) {
				discardRelease = false;
				return;
			}
			if (resizeDir == RESIZE_NONE)
				getDesktopManager().endDraggingFrame(frame);
			else {
				Container c = frame.getTopLevelAncestor();
				if (c instanceof JFrame) {
					((JFrame) frame.getTopLevelAncestor()).getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

					((JFrame) frame.getTopLevelAncestor()).getGlassPane().setVisible(false);
				} else if (c instanceof JApplet) {
					((JApplet) c).getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					((JApplet) c).getGlassPane().setVisible(false);
				} else if (c instanceof JWindow) {
					((JWindow) c).getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					((JWindow) c).getGlassPane().setVisible(false);
				} else if (c instanceof JDialog) {
					((JDialog) c).getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					((JDialog) c).getGlassPane().setVisible(false);
				}
				getDesktopManager().endResizingFrame(frame);
			}
			_x = 0;
			_y = 0;
			__x = 0;
			__y = 0;
			startingBounds = null;
			resizeDir = RESIZE_NONE;
		}

		public void mousePressed(MouseEvent e) {
			Point p = SwingUtilities.convertPoint((Component) e.getSource(), e.getX(), e.getY(), null);
			__x = e.getX();
			__y = e.getY();
			_x = p.x;
			_y = p.y;
			startingBounds = frame.getBounds();
			resizeDir = RESIZE_NONE;

			if (!frame.isSelected()) {
				try {
					frame.setSelected(true);
				} catch (PropertyVetoException e1) {
				}
			}
            if( e.getSource() == getNorthPane()) {
				getDesktopManager().beginDraggingFrame(frame);
				return;
			}
			if (!frame.isResizable()) {
				return;
			}

			if (e.getSource() == frame) {
				Insets i = frame.getInsets();
				if (e.getX() <= i.left) {
					if (e.getY() < resizeCornerSize + i.top) {
						resizeDir = NORTH_WEST;
					} else if (e.getY() > frame.getHeight() - resizeCornerSize - i.bottom) {
						resizeDir = SOUTH_WEST;
					} else {
						resizeDir = WEST;
					}
				} else if (e.getX() >= frame.getWidth() - i.right) {
					if (e.getY() < resizeCornerSize + i.top) {
						resizeDir = NORTH_EAST;
					} else if (e.getY() > frame.getHeight() - resizeCornerSize - i.bottom) {
						resizeDir = SOUTH_EAST;
					} else {
						resizeDir = EAST;
					}
				} else if (e.getY() <= i.top) {
					if (e.getX() < resizeCornerSize + i.left) {
						resizeDir = NORTH_WEST;
					} else if (e.getX() > frame.getWidth() - resizeCornerSize - i.right) {
						resizeDir = NORTH_EAST;
					} else {
						resizeDir = NORTH;
					}
				} else if (e.getY() >= frame.getHeight() - i.bottom) {
					if (e.getX() < resizeCornerSize + i.left) {
						resizeDir = SOUTH_WEST;
					} else if (e.getX() > frame.getWidth() - resizeCornerSize - i.right) {
						resizeDir = SOUTH_EAST;
					} else {
						resizeDir = SOUTH;
					}
				} else {
					/* the mouse press happened inside the frame, not in the
					   border */
					discardRelease = true;
					return;
				}
				Cursor s = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
				switch (resizeDir) {
					case SOUTH :
						s = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
						break;
					case NORTH :
						s = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
						break;
					case WEST :
						s = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
						break;
					case EAST :
						s = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
						break;
					case SOUTH_EAST :
						s = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
						break;
					case SOUTH_WEST :
						s = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
						break;
					case NORTH_WEST :
						s = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
						break;
					case NORTH_EAST :
						s = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
						break;
				}
				Container c = frame.getTopLevelAncestor();
				if (c instanceof JFrame) {
					((JFrame) c).getGlassPane().setVisible(true);
					((JFrame) c).getGlassPane().setCursor(s);
				} else if (c instanceof JApplet) {
					((JApplet) c).getGlassPane().setVisible(true);
					((JApplet) c).getGlassPane().setCursor(s);
				} else if (c instanceof JWindow) {
					((JWindow) c).getGlassPane().setVisible(true);
					((JWindow) c).getGlassPane().setCursor(s);
				} else if (c instanceof JDialog) {
					((JDialog) c).getGlassPane().setVisible(true);
					((JDialog) c).getGlassPane().setCursor(s);
				}
				getDesktopManager().beginResizingFrame(frame, resizeDir);
				return;
			}
		}

		public void mouseDragged(MouseEvent e) {

			if (startingBounds == null) {
				// (STEVE) Yucky work around for bug ID 4106552
				return;
			}

			Point p = SwingUtilities.convertPoint((Component) e.getSource(), e.getX(), e.getY(), null);
			int deltaX = _x - p.x;
			int deltaY = _y - p.y;
			Dimension min = frame.getMinimumSize();
			Dimension max = frame.getMaximumSize();
			int newX, newY, newW, newH;

			// Handle a MOVE 
            if(e.getSource() == getNorthPane()) {
				if (frame.isMaximum() || ((e.getModifiers() & InputEvent.BUTTON1_MASK) != InputEvent.BUTTON1_MASK)) {
					// don't allow moving of frames if maximixed or left mouse
					// button was not used.
					return;
				}
				Insets i = frame.getInsets();
				int pWidth, pHeight;
				Dimension s = frame.getParent().getSize();
				pWidth = s.width;
				pHeight = s.height;

				newX = startingBounds.x - deltaX;
				newY = startingBounds.y - deltaY;

				// Make sure we stay in-bounds
				if (newX + i.left <= -__x)
					newX = -__x - i.left;
				if (newY + i.top <= -__y)
					newY = -__y - i.top;
				if (newX + __x + i.right > pWidth)
					newX = pWidth - __x - i.right;
				if (newY + __y + i.bottom > pHeight)
					newY = pHeight - __y - i.bottom;

				getDesktopManager().dragFrame(frame, newX, newY);
				return;
			}

			if (!frame.isResizable()) {
				return;
			}

			newX = frame.getX();
			newY = frame.getY();
			newW = frame.getWidth();
			newH = frame.getHeight();

			parentBounds = frame.getParent().getBounds();

			switch (resizeDir) {
				case RESIZE_NONE :
					return;
				case NORTH :
					if (startingBounds.height + deltaY < min.height)
						deltaY = - (startingBounds.height - min.height);
					else if (startingBounds.height + deltaY > max.height)
						deltaY = max.height - startingBounds.height;
					if (startingBounds.y - deltaY < 0) {
						deltaY = startingBounds.y;
					}

					newX = startingBounds.x;
					newY = startingBounds.y - deltaY;
					newW = startingBounds.width;
					newH = startingBounds.height + deltaY;
					break;
				case NORTH_EAST :
					if (startingBounds.height + deltaY < min.height)
						deltaY = - (startingBounds.height - min.height);
					else if (startingBounds.height + deltaY > max.height)
						deltaY = max.height - startingBounds.height;
					if (startingBounds.y - deltaY < 0) {
						deltaY = startingBounds.y;
					}

					if (startingBounds.width - deltaX < min.width)
						deltaX = startingBounds.width - min.width;
					else if (startingBounds.width - deltaX > max.width)
						deltaX = - (max.width - startingBounds.width);
					if (startingBounds.x + startingBounds.width - deltaX > parentBounds.width) {
						deltaX = startingBounds.x + startingBounds.width - parentBounds.width;
					}

					newX = startingBounds.x;
					newY = startingBounds.y - deltaY;
					newW = startingBounds.width - deltaX;
					newH = startingBounds.height + deltaY;
					break;
				case EAST :
					if (startingBounds.width - deltaX < min.width)
						deltaX = startingBounds.width - min.width;
					else if (startingBounds.width - deltaX > max.width)
						deltaX = - (max.width - startingBounds.width);
					if (startingBounds.x + startingBounds.width - deltaX > parentBounds.width) {
						deltaX = startingBounds.x + startingBounds.width - parentBounds.width;
					}

					newW = startingBounds.width - deltaX;
					newH = startingBounds.height;
					break;
				case SOUTH_EAST :
					if (startingBounds.width - deltaX < min.width)
						deltaX = startingBounds.width - min.width;
					else if (startingBounds.width - deltaX > max.width)
						deltaX = - (max.width - startingBounds.width);
					if (startingBounds.x + startingBounds.width - deltaX > parentBounds.width) {
						deltaX = startingBounds.x + startingBounds.width - parentBounds.width;
					}

					if (startingBounds.height - deltaY < min.height)
						deltaY = startingBounds.height - min.height;
					else if (startingBounds.height - deltaY > max.height)
						deltaY = - (max.height - startingBounds.height);
					if (startingBounds.y + startingBounds.height - deltaY > parentBounds.height) {
						deltaY = startingBounds.y + startingBounds.height - parentBounds.height;
					}

					newW = startingBounds.width - deltaX;
					newH = startingBounds.height - deltaY;
					break;
				case SOUTH :
					if (startingBounds.height - deltaY < min.height)
						deltaY = startingBounds.height - min.height;
					else if (startingBounds.height - deltaY > max.height)
						deltaY = - (max.height - startingBounds.height);
					if (startingBounds.y + startingBounds.height - deltaY > parentBounds.height) {
						deltaY = startingBounds.y + startingBounds.height - parentBounds.height;
					}

					newW = startingBounds.width;
					newH = startingBounds.height - deltaY;
					break;
				case SOUTH_WEST :
					if (startingBounds.height - deltaY < min.height)
						deltaY = startingBounds.height - min.height;
					else if (startingBounds.height - deltaY > max.height)
						deltaY = - (max.height - startingBounds.height);
					if (startingBounds.y + startingBounds.height - deltaY > parentBounds.height) {
						deltaY = startingBounds.y + startingBounds.height - parentBounds.height;
					}

					if (startingBounds.width + deltaX < min.width)
						deltaX = - (startingBounds.width - min.width);
					else if (startingBounds.width + deltaX > max.width)
						deltaX = max.width - startingBounds.width;
					if (startingBounds.x - deltaX < 0) {
						deltaX = startingBounds.x;
					}

					newX = startingBounds.x - deltaX;
					newY = startingBounds.y;
					newW = startingBounds.width + deltaX;
					newH = startingBounds.height - deltaY;
					break;
				case WEST :
					if (startingBounds.width + deltaX < min.width)
						deltaX = - (startingBounds.width - min.width);
					else if (startingBounds.width + deltaX > max.width)
						deltaX = max.width - startingBounds.width;
					if (startingBounds.x - deltaX < 0) {
						deltaX = startingBounds.x;
					}

					newX = startingBounds.x - deltaX;
					newY = startingBounds.y;
					newW = startingBounds.width + deltaX;
					newH = startingBounds.height;
					break;
				case NORTH_WEST :
					if (startingBounds.width + deltaX < min.width)
						deltaX = - (startingBounds.width - min.width);
					else if (startingBounds.width + deltaX > max.width)
						deltaX = max.width - startingBounds.width;
					if (startingBounds.x - deltaX < 0) {
						deltaX = startingBounds.x;
					}

					if (startingBounds.height + deltaY < min.height)
						deltaY = - (startingBounds.height - min.height);
					else if (startingBounds.height + deltaY > max.height)
						deltaY = max.height - startingBounds.height;
					if (startingBounds.y - deltaY < 0) {
						deltaY = startingBounds.y;
					}

					newX = startingBounds.x - deltaX;
					newY = startingBounds.y - deltaY;
					newW = startingBounds.width + deltaX;
					newH = startingBounds.height + deltaY;
					break;
				default :
					return;
			}
			getDesktopManager().resizeFrame(frame, newX, newY, newW, newH);
		}

		public void mouseMoved(MouseEvent e) {

			if (!frame.isResizable())
				return;

			if (e.getSource() == frame) {
				Insets i = frame.getInsets();
				if (e.getX() <= i.left) {
					if (e.getY() < resizeCornerSize + i.top)
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
					else if (e.getY() > frame.getHeight() - resizeCornerSize - i.bottom)
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
					else
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
				} else if (e.getX() >= frame.getWidth() - i.right) {
					if (e.getY() < resizeCornerSize + i.top)
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
					else if (e.getY() > frame.getHeight() - resizeCornerSize - i.bottom)
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
					else
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				} else if (e.getY() <= i.top) {
					if (e.getX() < resizeCornerSize + i.left)
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
					else if (e.getX() > frame.getWidth() - resizeCornerSize - i.right)
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
					else
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
				} else if (e.getY() >= frame.getHeight() - i.bottom) {
					if (e.getX() < resizeCornerSize + i.left)
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
					else if (e.getX() > frame.getWidth() - resizeCornerSize - i.right)
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
					else
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
				} else
					frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				return;
			}

			frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

		public void mouseExited(MouseEvent e) {
			frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	//************************ Overridden MetalInternalFrameUI Methods

	protected JComponent createNorthPane(JInternalFrame w) {
		return null;
	}

	protected JComponent createWestPane(JInternalFrame w) {
		return new ChoiceMakerInternalFrameTitlePane(w);
	}

	/**
	 * overridden to avoid confusion.
	 * @deprecated DO NOT USE... it has been deactivated.
	 */
	public void setPalette(boolean isPalette) {
		//DO NOTHING
	}

}
