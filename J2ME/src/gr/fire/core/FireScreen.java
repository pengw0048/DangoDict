/*
 * Fire is a fast, themable UI toolkit and xHTML/CSS renderer for mobile application 
 * and game development. It is an eye-candy alternative to the standard midp2 UI 
 * components and unlike them it produces a superior UI result on all mobile devices!
 *  
 * Copyright (C) 2006,2007,2008,2009,2010 Pashalis Padeleris (padeler at users.sourceforge.net)
 * 
 * This file is part of Fire.
 *
 * Fire is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Fire is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Fire.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/*
 * Created on Feb 22, 2008
 */
package gr.fire.core;

import gr.fire.ui.Alert;
import gr.fire.ui.AlertAnimation;
import gr.fire.ui.SoftKeyAnimation;
import gr.fire.util.FireConnector;
import gr.fire.util.Log;
import gr.fire.util.Queue;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.game.Sprite;

/**
 * @author padeler
 * 
 */
public class FireScreen extends GameCanvas implements Runnable
{
	/**
	 * FireScreen NORMAL Orientation
	 * 
	 * @see #setOrientation(int)
	 */
	public static final byte NORMAL = 0x00;

	/**
	 * FireScreen landscape (right handed) orientation
	 * 
	 * @see #setOrientation(int)
	 */
	public static final byte LANDSCAPERIGHT = 0x01;
	/**
	 * FireScreen landscape (left handed) orientation
	 * 
	 * @see #setOrientation(int)
	 */
	public static final byte LANDSCAPELEFT = 0x02;

	public static final int NONE = 0x00000000;
	public static final int CENTER = 0x00000001;
	public static final int RIGHT = 0x00000002;
	public static final int LEFT = 0x00000004;
	public static final int TOP = 0x00000008;
	public static final int BOTTOM = 0x00000010;
	public static final int VCENTER = 0x00000020;

	/**
	 * FireScreen determines the handset type on startup and assignes the left
	 * Softkey key code to this variable
	 */
	public static int leftSoftKey = -6;
	/**
	 * FireScreen determines the handset type on startup and assignes the right
	 * Softkey key code to this variable
	 */
	public static int rightSoftKey = -7;

	private static final Object[][] keyMaps =
	{
	{ "Nokia", new Integer(-6), new Integer(-7) },
	{ "ricsson", new Integer(-6), new Integer(-7) },
	{ "iemens", new Integer(-1), new Integer(-4) },
	{ "otorola", new Integer(-21), new Integer(-22) },
	{ "harp", new Integer(-21), new Integer(-22) },
	{ "j2me", new Integer(-6), new Integer(-7) },
	{ "intent JTE", new Integer(57345), new Integer(57346) } ,
	{ "RIM", new Integer(-21), new Integer(27) } };

	/**
	 * ZINDEX is the location a component on the Z axis (depth) it controls in
	 * which order the Component will be painted. <br/> The current component
	 * set in the methods getCurrent and setCurrent is always considered the
	 * component on zindex=0. <br/> The indexes should be used as follows (this
	 * is a recomendation, it is not forced): <br/> -3, -2, -1 : Backgrounds and
	 * background animations <br/> 0, 1, 2, 3 : Panels, Containers, popup menus
	 * and user traversable components in general. <br/> 4, 5, 6, 7, ... :
	 * Animations, effects, transition animations, mouse pointers, etc. <br/>
	 * 
	 * @see #setCurrent(Component)
	 * @see #getCurrent()
	 * @see #addComponent(Component, int)
	 * 
	 */
	public static final int ZINDEX_MAX = 11;

	/**
	 * @see #ZINDEX_MAX
	 */
	public static final int ZINDEX_MIN = -3;
	
	public static final int LEFT_SOFTKEY_ZINDEX=7;
	public static final int RIGHT_SOFTKEY_ZINDEX=8;

	private static FireScreen singleton = null;

	private static Theme theme;
	private static Font navbarFont;

	/* ***************** Support variables ***************************** */
	private Display display = null;
	private Image offscreen; // offscreen image used when rendering in
								// landscape modes.
	private int softkeyController = -1; // holds the max zindex of a top level
										// component thas is focusable and has
										// softkeys

	// variables that control tha behaivior of the FireScreen canvas
	private boolean visible = true; // flag toggled by showNotify and hideNotify
									// to start or stop animations and repaint
									// screen when its displayed.
	private boolean generateRepeatEvents = false; // if the platform does not
													// support keyRepeated
													// events the FireScreen
													// will produce these
													// manually.
	private static final int NO_PRESSED_KEY=-1000;
	private int pressedKeyCode = NO_PRESSED_KEY;
	
	private long pressedKeyTime = 0;
	private long keyRepeatPeriod = 200; // if generateRepeatEvents==true,
										// keyRepeated events will be generated
										// every this amount of miliseconds

	private int orientation = NORMAL;
	private boolean animationsEnabled = true;
	private boolean paintSoftkeys=true;

	private FireListener fireListener=null;
	private Queue animationQueue;

	/** Vector containing all the stacked components currently open on the scren. */
	private Component selectedComponent = null; // holds the currently selected
												// component.

	private Component[] componentSlots = new Component[(ZINDEX_MAX - ZINDEX_MIN) + 1];
	
	private Thread animationThread;

	private FireScreen(Display display,boolean suppressKeyEvents)
	{
		super(suppressKeyEvents);
		/* **** Hack for Motorola phones ********** Thanks to Maxim Blagov */
		/*
		 * **** Some (if not all) Motorola phones, return "j2me" to
		 * getProperty("microedition.platform")
		 */
		String platform = null;
		String prop = System.getProperty("com.mot.carrier.URL");
		if (prop != null)
		{
			platform = "motorola";
		} else
		{
			try
			{
				Class.forName("com.motorola.graphics.j3d.Graphics3D");
				platform = "motorola";
			} catch (Throwable e)
			{
			}
		}

		if (platform == null) // ok its probably not a Motorola phone.
		{
			platform = System.getProperty("microedition.platform");
		}
		/* ******************************************************** */
		Log.logDebug("Running on platform: " + platform);

		for (int i = 0; i < keyMaps.length; ++i)
		{
			String manufacturer = (String) keyMaps[i][0];

			if (platform.indexOf(manufacturer) != -1)
			{
				if (i == 1) // ta sony ericsson exoun enalaktika keys sta
							// p800/p900/p908/p802
				{
					if (platform.indexOf("P900") != -1 || platform.indexOf("P908") != -1)
					{
						leftSoftKey = ((Integer) keyMaps[i][2]).intValue();
						rightSoftKey = ((Integer) keyMaps[i][1]).intValue();
					} else
					{
						leftSoftKey = ((Integer) keyMaps[i][1]).intValue();
						rightSoftKey = ((Integer) keyMaps[i][2]).intValue();
					}
				} else
				{
					leftSoftKey = ((Integer) keyMaps[i][1]).intValue();
					rightSoftKey = ((Integer) keyMaps[i][2]).intValue();
				}
				break;
			}
		}

		if (theme == null)
			setTheme(new Theme());// default theme
		this.display = display;

		// if platform does not have support ket repeat events, i must generate
		// them.
		generateRepeatEvents = !hasRepeatEvents();

		animationQueue = new Queue();
		animationThread = new Thread(this);
		animationThread.start();
	}

	public void paint(Graphics g)
	{
		int width = getWidth();
		int height = getHeight();

		int clipX = g.getClipX(), clipY = g.getClipY(), clipW = g.getClipWidth(), clipH = g.getClipHeight();
		int trX = g.getTranslateX();
		int trY = g.getTranslateY();
		try
		{
			Graphics dest;
			if (orientation != NORMAL)
			{
				if (offscreen == null || offscreen.getWidth() != width || offscreen.getHeight() != height)
				{
					offscreen = Image.createImage(width, height);
				}
				dest = offscreen.getGraphics();

				// invert the clip information.
				// NOTE: No need to invert the translate information.
				
				int t;
				if (orientation == LANDSCAPELEFT)
				{
					t = clipY;
					clipY = clipX;
					clipX = width - t - clipH;
				} else
				{
					t = clipX;
					clipX = clipY;
					clipY = height - t - clipW;
				}

				t = clipW;
				clipW = clipH;
				clipH = t;

				dest.translate(-dest.getTranslateX() + trX, -dest.getTranslateY() + trY);
				dest.setClip(clipX, clipY, clipW, clipH);
			} else
			{
				dest = g;				
			}

			/* ***** Clean the area that will be repainted. ***** */
			// clean the bg to white...
			dest.setColor(0x00FFFFFF);
			dest.fillRect(clipX, clipY, clipW, clipH); // this will only
														// repaint the clipped
														// region.

			/*
			 * ************ paint the fireScreen animations behind the
			 * components. *****************
			 */
			for (int i = 0; i < componentSlots.length; ++i)
			{
				if(!paintSoftkeys && (i==(LEFT_SOFTKEY_ZINDEX-ZINDEX_MIN) || i==(RIGHT_SOFTKEY_ZINDEX-ZINDEX_MIN))){
					continue; // skip softkey painting.
				}
				
				Component paintable = componentSlots[i];
				if (paintable != null && paintable.visible)
				{
					if (!paintable.valid)
					{
						paintable.validate();
					}

					// ok, now paint or animate the component.
					if (paintable.animation != null)
					{
						if (paintable.animation.intersects(clipX, clipY, clipW, clipH))
						{
							dest.clipRect(paintable.animation.x, paintable.animation.y, paintable.animation.width, paintable.animation.height);
							dest.translate(paintable.animation.x, paintable.animation.y);
							paintable.animation.paint(dest);
							dest.translate(-dest.getTranslateX() + trX, -dest.getTranslateY() + trY);
							dest.setClip(clipX, clipY, clipW, clipH);
						}
					} else
					{
						if (paintable.intersects(clipX, clipY, clipW, clipH))
						{
							dest.clipRect(paintable.x, paintable.y, paintable.width, paintable.height);
							dest.translate(paintable.x, paintable.y);

							paintable.paint(dest);
							dest.translate(-dest.getTranslateX() + trX, -dest.getTranslateY() + trY);
							dest.setClip(clipX, clipY, clipW, clipH);
						}
					}

					dest.translate(-dest.getTranslateX() + trX, -dest.getTranslateY() + trY);
					dest.setClip(clipX, clipY, clipW, clipH);
				}
			}
		} catch (OutOfMemoryError e)
		{
			Runtime.getRuntime().gc();
			animationQueue.removeAll();
			animationsEnabled = false; // not much to do at this point but
										// disable animations anyway.
			Runtime.getRuntime().gc();
			Log.logError("OutOfMemory inside FireScreen.paint().", e);
		} catch (Throwable e)
		{
			Log.logError("Exception inside FireScreen.paint().", e);
		}

		/*
		 * **** Finally paint the offscreen. This step is only used when drawing
		 * in landscape modes. ****
		 */
		switch (orientation)
		{
		case NORMAL:
			break;
		case LANDSCAPELEFT:
			g.drawRegion(offscreen, 0, 0, width, height, Sprite.TRANS_ROT270, 0, 0, Graphics.TOP | Graphics.LEFT);
			break;
		case LANDSCAPERIGHT:
			g.drawRegion(offscreen, 0, 0, width, height, Sprite.TRANS_ROT90, 0, 0, Graphics.TOP | Graphics.LEFT);
			break;
		}
	}

	/**
	 * Registers an animation on the internal animation queue. The animatation
	 * will remain in the queue until its completed or its removed using the
	 * {@link #removeAnimation(Animation)} method.
	 * 
	 * @see Animation
	 * @param anim
	 */
	public void registerAnimation(Animation anim)
	{
		animationQueue.add(anim);
		if (anim.parent != null)
			anim.parent.animation = anim;
	}

	/**
	 * Removes the animation from the internal animation queue.
	 * 
	 * <b>Note:</b> If this animation is a top level component it should be
	 * removed using the removeComponent instead. This method will remove the
	 * animation from the Queue but will not remove it from the FireScreen top
	 * level components list.
	 * 
	 * @see #registerAnimation(Animation)
	 * @param anim
	 */
	public void removeAnimation(Animation anim)
	{
		animationQueue.remove(anim);
		if (anim.parent != null)
			anim.parent.animation = null;
	}
	
	/*
	 * This method is called by a component each time its right or left softkey commands are set. 
	 * If the component's softkeys are currently displayed, the updateSoftKeys() method is called 
	 * to change the displayed softkeys.
	 */
	void componentSoftKeyUpdate(Component cmp)
	{
		if(cmp==null) return;
		if(cmp==selectedComponent || (softkeyController>-1 && componentSlots[softkeyController]==cmp))
		{ // update the softkeys is needed.
			updateSoftKeys();
		}
	}

	private void updateSoftKeys()
	{
		Command left=null,right=null;
		Component softKeyOwner = selectedComponent;
		if((softKeyOwner==null || (softKeyOwner.leftSoftKeyCommand==null && softKeyOwner.rightSoftKeyCommand==null)) && softkeyController>-1)
		{
			softKeyOwner = componentSlots[softkeyController];
		}
		
		if(softKeyOwner!=null)
		{
			left = softKeyOwner.leftSoftKeyCommand;
			right = softKeyOwner.rightSoftKeyCommand;
		}
		
		int lidx = LEFT_SOFTKEY_ZINDEX - ZINDEX_MIN;
		int ridx = RIGHT_SOFTKEY_ZINDEX - ZINDEX_MIN;
		
		SoftKeyAnimation leftSoftKeyCmp=null,rightSoftKeyCmp=null;
		
		if (left != null || right != null)
		{
			int keyH = navbarFont.getHeight();
			int lh=keyH,rh=keyH;
			
			int dw = getWidth();
			int dh = getHeight();
	
			int navbarBgColor = theme.getIntProperty("navbar.bg.color");
			int navbarFgColor = theme.getIntProperty("navbar.fg.color");
			
			int lx=0,ly=0,rx=0,ry=0;
			int lw = (left!=null?navbarFont.stringWidth(left.getLabel()):0);
			int rw = (right!=null?navbarFont.stringWidth(right.getLabel()):0);
			
			switch(orientation)
			{
			case NORMAL:
				if(theme.decorBottom>keyH) {lh = theme.decorBottom;rh= theme.decorBottom;}
				lx=0;ly=dh-lh;
				rx=dw-rw;ry=dh-rh;
				break;
			case LANDSCAPELEFT:
				if(theme.decorTop>keyH) lh = theme.decorTop;
				if(theme.decorBottom>keyH) rh= theme.decorBottom;
				lx=0;ly=0;
				rx=0;ry=dh-rh;
				break;
			case LANDSCAPERIGHT:
				if(theme.decorTop>keyH) rh = theme.decorTop;
				if(theme.decorBottom>keyH) lh= theme.decorBottom;
				lx=dw-lw;ly=dh-lh;
				rx=dw-rw;ry=0;
				break;			
			}
	
			if (left != null)
			{
				leftSoftKeyCmp = new SoftKeyAnimation(left.getLabel());
				leftSoftKeyCmp.setFont(navbarFont);
				leftSoftKeyCmp.setBackgroundColor(navbarBgColor);
				leftSoftKeyCmp.setForegroundColor(navbarFgColor);
				leftSoftKeyCmp.setPosition(lx,ly);
				leftSoftKeyCmp.setWidth(lw);
				leftSoftKeyCmp.setHeight(lh);
				leftSoftKeyCmp.validate();
			}
	
			if (right != null)
			{
				rightSoftKeyCmp = new SoftKeyAnimation(right.getLabel());
				rightSoftKeyCmp.setFont(navbarFont);
				rightSoftKeyCmp.setBackgroundColor(navbarBgColor);
				rightSoftKeyCmp.setForegroundColor(navbarFgColor);
				rightSoftKeyCmp.setPosition(rx,ry);
				rightSoftKeyCmp.setWidth(rw);
				rightSoftKeyCmp.setHeight(rh);
				rightSoftKeyCmp.validate();
			}
		}
		
		if(componentSlots[lidx]!=null) repaintScreen(componentSlots[lidx].x,componentSlots[lidx].y,componentSlots[lidx].width,componentSlots[lidx].height); 
		if(componentSlots[ridx]!=null) repaintScreen(componentSlots[ridx].x,componentSlots[ridx].y,componentSlots[ridx].width,componentSlots[ridx].height); 

		componentSlots[lidx]=leftSoftKeyCmp;
		componentSlots[ridx]=rightSoftKeyCmp;
		
		if(leftSoftKeyCmp!=null) leftSoftKeyCmp.repaint();
		if(rightSoftKeyCmp!=null) rightSoftKeyCmp.repaint();
	}

	/**
	 * Returns the current panel set on the FireScreen.
	 * 
	 * @return
	 */
	public Component getCurrent()
	{
		return componentSlots[-ZINDEX_MIN];
	}

	/**
	 * Set a Displayable to the FireScreen.
	 * @param d The displayable 
	 */
	public void setCurrent(Displayable d)
	{
		display.setCurrent(d);
	}
	
	
	/**
	 * Utility method to easily display popups that should be located near the component that triggered them.
	 * For example when opening a drop-down menu, it should be opened near the menu component that triggered it.
	 * This method provides an easy way to open the popup, without the need to calculate its position (x,y).
	 * It takes into acount the location of the trigger component and the orientation of the screen.
	 * 
	 * 
	 * @param popup The popup component to display
	 * @param cmp The component that is the logical trigger for this popup to apear
	 * @param zindex The zindex on which the popup will be added using the {@link #addCommand(Command)} method.
	 */
	public void showPopupOnComponent(Component popup, Component cmp, int zindex)
	{
		int popupX=0,popupY=0,cmpX=0,cmpY=0,cmpW=0,cmpH=0;
		// first calculate the position of the trigger component
		if(cmp!=null)
		{
			Component t = cmp;
			while(t!=null) 
			{
				cmpX+=t.x;
				cmpY+=t.y;
				t = t.parent;
			}
			cmpW = cmp.getWidth();
			cmpH = cmp.getHeight();
		}
		
		if (!popup.valid)
		{
			popup.validate();
		}
		
		int sw = getWidth();
		int sh = getHeight();
		int pw = popup.getWidth();
		int ph = popup.getHeight();
		
		// first try to place the popup above the trigger cmp.
		// if it doesn't fit check bottom of the trigger cmp
		// else check right
		// else check left 
		// else try to fit it over the trigger cmp.
		
		if(cmpY-ph>0) // popup height fits above the trigger component.
		{
			popupY = cmpY-ph;
			if(pw>=sw) popupX = 0;
			else if(cmpX+pw<sw)
				popupX = cmpX;
			else if(cmpX+cmpW-pw>0) popupX = cmpX+cmpW-pw;
			else {
				popupX = cmpX + (sw - (cmpX+pw));
			}
		}
		else if(cmpY+cmpH+ph<sh) // popup fits below the trigger component
		{
			popupY = cmpY+cmpH;
			if(pw>=sw) popupX = 0;
			else if(cmpX+pw<sw)
				popupX = cmpX;
			else if(cmpX+cmpW-pw>0) popupX = cmpX+cmpW-pw;
			else {
				popupX = cmpX + (sw - (cmpX+pw));
			}
		}
		else if(cmpX+cmpW+pw<sw) // popup fits on the right of the trigger component.
		{
			popupX = cmpX+cmpW;
			if(ph>=sh) popupY =0;
			else if(cmpY+ph<sh) popupY = cmpY;
			else {
				popupX = cmpY + (sh - (cmpY+ph));
			}
		}
		else if(cmpX-pw>=0) // popup fits on the left of the trigger component
		{
			popupX = cmpX-pw;
			if(ph>=sh) popupY =0;
			else if(cmpY+ph<sh) popupY = cmpY;
			else {
				popupX = cmpY + (sh - (cmpY+ph));
			}			
		}
		else // try to show the popup over the trigger component
		{
			popupX = cmpX;
			popupY = cmpY;
			if(popupX+pw>sw) popupX += (sw - (popupX+pw));
			if(popupY+ph>sh) popupY += (sh - (popupY+ph));
			if(popupX<0) popupX=0;
			if(popupY<0) popupY=0;
		}
		
		popup.setX(popupX);
		popup.setY(popupY);
		addComponent(popup,zindex);
	}
	
	/**
	 * Utility method to open a popup on the left softkey.
	 * It is equivalent to: 
	 * FireScreen.getScreen().showPopupOnComponent(popup,FireScreen.getScreen().getComponent(FireScreen.LEFT_SOFTKEY_ZINDEX),zindex)
	 * 
	 * @see #showPopupOnComponent(Component, Component, int)
	 * @param popup 
	 * @param zindex
	 */
	public void showPopupOnLeftSoftkey(Component popup,int zindex)
	{
		showPopupOnComponent(popup,componentSlots[LEFT_SOFTKEY_ZINDEX-ZINDEX_MIN],zindex);
	}
	
	/**
	 * Utility method to open a popup on the right softkey.
	 * It is equivalent to: 
	 * FireScreen.getScreen().showPopupOnComponent(popup,FireScreen.getScreen().getComponent(FireScreen.RIGHT_SOFTKEY_ZINDEX),zindex)
	 * 
	 * @see #showPopupOnComponent(Component, Component, int)
	 * @param popup 
	 * @param zindex
	 */
	public void showPopupOnRightSoftkey(Component popup,int zindex)
	{
		showPopupOnComponent(popup,componentSlots[RIGHT_SOFTKEY_ZINDEX-ZINDEX_MIN],zindex);
	}

	/**
	 * Adds the given component as a top level component on index zidx
	 * 
	 * @see #ZINDEX_MAX
	 * @param c
	 * @param zidx
	 */
	public void addComponent(Component c, int zidx)
	{
		if (zidx < ZINDEX_MIN || zidx > ZINDEX_MAX)
			throw new IllegalArgumentException("zindex must be between [" + ZINDEX_MIN + "," + ZINDEX_MAX + "].");

		int zindex = zidx - ZINDEX_MIN;

		if (c.parent != null)
		{
			if (c.parent instanceof Container)
				((Container) c.parent).remove(c);

			c.parent = null;
		}

		if (!c.valid)
		{
			c.validate();
		}

		Component last = componentSlots[zindex];
		if (last != null)
		{
			removeComponent(zidx);
		}

		componentSlots[zindex] = c;

		if (c instanceof Animation)
		{
			registerAnimation((Animation) c);
		} else
		{
			setSelectedComponent(null); // this will allow for returning to the
										// selected input field after editing
										// it.
			if (c.animation != null)
			{
				registerAnimation(c.animation);
			}

			if (c instanceof Container || c.isFocusable())
			{
				if (softkeyController < zindex)
				{
					// new softkey controller.
					softkeyController = zindex;
					// softkeys should be updated.
					updateSoftKeys();
				}
			}
		}
		c.repaint();

		if (display.getCurrent() != this)
		{
			display.setCurrent(this);
			repaintScreen(0, 0, getWidth(), getHeight());
		}
	}

	/**
	 * Utility method for fast and easy user notification. Creates an alert with
	 * the given parameters and displayes it on the screen using zindex (see
	 * {@link #ZINDEX_MAX} 4
	 * 
	 * @see Alert
	 * 
	 * @param message
	 *            The message to be shown in the alert
	 * @param type
	 *            The type of the alert
	 * @param defaultSelection
	 *            The default button selected.
	 * @param command
	 *            The command to be send to the listener when the user presses a
	 *            button on the alert.
	 * @param listener
	 *            The listener to receive the command when the user presses a
	 *            button on the alert.
	 *
	 */
	public int showAlert(String message, byte type, byte defaultSelection, Command command, CommandListener listener)
	{
		return showAlert(message, type, defaultSelection, command, listener,animationsEnabled);
	}
	
	public int showAlert(String message, byte type, byte defaultSelection, Command command, CommandListener listener,boolean animate)
	{
		Image icon = null;
		String iconPath;
		AlertType alertType;
		switch (type)
		{
		case Alert.TYPE_WARNING:
			alertType = AlertType.WARNING;
			iconPath = theme.getStringProperty("alert.warning.icon");
			break;
		case Alert.TYPE_ERROR:
			alertType = AlertType.ERROR;
			iconPath = theme.getStringProperty("alert.error.icon");
			break;
		case Alert.TYPE_YESNO:
			alertType = AlertType.CONFIRMATION;
			iconPath = theme.getStringProperty("alert.yesno.icon");
			break;
		case Alert.TYPE_YESNOCANCEL:
			alertType = AlertType.CONFIRMATION;
			iconPath = theme.getStringProperty("alert.yesnocancel.icon");
			break;
		case Alert.TYPE_CANCEL:
			alertType = AlertType.INFO;
			iconPath = theme.getStringProperty("alert.info.icon");
			break;
		default: // default is Information alert.
			alertType = AlertType.INFO;
			iconPath = theme.getStringProperty("alert.info.icon");
			break;
		}

		try
		{
			if (iconPath != null)
				icon = Image.createImage(new FireConnector().openInputStream(iconPath));
		} catch (Exception e)
		{
			Log.logWarn("Failed to load Alert icon form: " + iconPath, e);
		}

		Alert alert = new Alert(message, icon, type, defaultSelection);

		alert.setCommand(command);
		alert.setCommandListener(listener);

		int[] ps = alert.getPrefSize();
		alert.setX(getWidth() / 2 - ps[0] / 2);
		alert.setY(getHeight()  - ps[1] - (theme.decorBottom*4)/3);

		Component current = getCurrent();
		if (animate && current!=null && current.animation==null) 
		{ // only animate if there is a component on index0 and it does not have another animation running.
			try{
				alert.validate(); // alert need to be validated before the animation.
				AlertAnimation anim = new AlertAnimation(alert);
				getCurrent().setAnimation(anim);
				registerAnimation(anim);
				alertType.playSound(display);
				return alert.getAlertIndex();
			}catch(OutOfMemoryError e){ // out of memory just disable animations.
				Log.logError("Outofmemory on alert animation. Disabling animations.",e);
				animationsEnabled=false;
			}
		}
		

		
		// no animations. Just display the alert.
		addComponent(alert, alert.getAlertIndex());
		setSelectedComponent(alert.getSelectedButton());
		
		alertType.playSound(display);

		return alert.getAlertIndex();
	}

	/**
	 * Utility method to easilly remove the top most component that is NOT an
	 * animation. It is used to close popups etc.
	 * 
	 * @see #removeComponent(int)
	 * @return
	 */
	public boolean removeTopComponent()
	{
		for (int i = componentSlots.length - 1; i >= 0; --i)
		{
			Component c = componentSlots[i];
			if (c != null && (c instanceof Animation) == false)
			{
				return removeComponent(i) != null;
			}
		}
		return false;
	}

	/**
	 * Removes the top level component, returns true if the component was found
	 * among the top level components and was removed.
	 * 
	 * @param c
	 * @return
	 */
	public boolean removeComponent(Component c)
	{
		if (c == null)
			throw new NullPointerException("Cannot remove null component.");

		for (int i = componentSlots.length - 1; i >= 0; --i)
		{
			if (componentSlots[i] == c)
				return (removeComponent(i + ZINDEX_MIN) != null);
		}
		return false;
	}

	/**
	 * Returns the component on given zindex (see {@link #ZINDEX_MAX})
	 * 
	 * @param zindex
	 * @return
	 */
	public Component getComponent(int zindex)
	{
		if (zindex < ZINDEX_MIN || zindex > ZINDEX_MAX)
			throw new IllegalArgumentException("zindex must be between [" + ZINDEX_MIN + "," + ZINDEX_MAX + "].");

		return componentSlots[zindex - ZINDEX_MIN];
	}

	/**
	 * Removes and returns the component on given zindex
	 * 
	 * @param zindex
	 * @return
	 */
	public Component removeComponent(int zindex)
	{
		if (zindex < ZINDEX_MIN || zindex > ZINDEX_MAX)
			throw new IllegalArgumentException("zindex must be between [" + ZINDEX_MIN + "," + ZINDEX_MAX + "].");

		Component c = componentSlots[zindex - ZINDEX_MIN];
		if (c == null)
			return null;

		if (c instanceof Animation)
		{ // remove the animation from the queue
			animationQueue.remove((Animation) c);
		}

		animationQueue.removeAllWithParent(c);

		if (selectedComponent != null)
		{
			Component tmp = selectedComponent;
			while (tmp.parent != null)
				tmp = tmp.parent;
			if (tmp == c) // selected component is inside the component that
							// is going to be removed.
			{ // so we must deselect it
				setSelectedComponent(null);
			}
		}

		componentSlots[zindex - ZINDEX_MIN] = null;
		repaintScreen(c.x, c.y, c.width, c.height);

		if(softkeyController==(zindex-ZINDEX_MIN)) // find a new softkeyController
		{
			softkeyController=-1;
			for(int i=componentSlots.length-1;i>=0;--i)
			{
				if(componentSlots[i]!=null && (componentSlots[i] instanceof Container || componentSlots[i].isFocusable()))
				{
					softkeyController=i;
					break;
				}
			}
			updateSoftKeys();
		}else if(zindex==LEFT_SOFTKEY_ZINDEX || zindex==RIGHT_SOFTKEY_ZINDEX)
		{ // the component beeing removed is a softkey. need to update softkeys now.
			updateSoftKeys();
		}
		
		return c;
	}

	/**
	 * Sets the given component on zindex 0.
	 * 
	 * @param p
	 * @param animDirection
	 */
	public void setCurrent(Component p)
	{
		addComponent(p, 0);
	}

	private boolean destroyed = false;

	public void run()
	{
		long minLoopTime = 30; // dont loop faster than this period, in order
								// to avoid busy waits
		long start, period;
		try
		{
			while (!destroyed)
			{
				start = System.currentTimeMillis();
				if (visible)
				{
					if (generateRepeatEvents)
					{
						generateRepeatEvent(start);
					}
					Animation anim = (Animation) animationQueue.getNext();
					if(!anim.isRunning())// animation completed. remove it from the queue.
					{
						if (anim.parent == null) // top level animations must be removed using the removeComponent
						{
							if(removeComponent(anim)==false) 
							{// animation not in componentSlot[] remove it from queue. bug fix for #2802477
							 // XXX thanks to Murat(sadiemae)
								Log.logDebug("Failed to remove top level animation. Not found in a slot.");
								animationQueue.remove(anim);
							}
							
						} else
						// all other animations just need to be removed from the
						// animation Queue, using the removeAnimation.
						{
							// ask for a repaint of its owner component.
							animationQueue.remove(anim);
							anim.parent.animation = null;
							anim.parent.repaint();
						}
						continue;
					}

					int x = anim.x, y = anim.y, w = anim.width, h = anim.height;// get coords and dimensions before step
					boolean repaintNeeded = anim.step();
					// animations can move around on the screen or change size.
					// we must make sure all
					// artifacts are cleared from the screen before redrawing
					// the animation
					if (anim.x != x || anim.y != y || anim.width != w || anim.height != h)
					{// i need to clear the old frame of the animation
						if (anim.parent != null && anim.parent.parent != null) // NOT a top level animation, or animation of top level component
						{// find the real x,y coords of the animation
							anim.parent.repaint(x, y, w, h);
						}
						repaintScreen(x, y, w, h);
					}

					if (repaintNeeded && anim.visible)
					{
						anim.repaint(); // repaint the animation's new frame
					}
				}
				period = System.currentTimeMillis() - start;
				if (period < minLoopTime)
				{
					try
					{
						Thread.sleep(minLoopTime - period);
					} catch (InterruptedException e)
					{
						Log.logError("Interrupted inside animation thread.", e);
					}
				}
			}
		} catch (OutOfMemoryError e)
		{
			animationsEnabled = false; // not much to do at this point but
										// disable animations anyway.
			animationQueue.removeAll();
			Runtime.getRuntime().gc();
			Log.logError("Animation thread OutOfMemory.", e);
		} catch (Throwable e)
		{
			Log.logError("Animation thread died.", e);
		}
	}
	
	/**
	 * A wrapper method for the GameCanvas.getKeyStates()
	 * 
	 * @see javax.microedition.lcdui.game.GameCanvas#getKeyStates()
	 */
	public int getKeyStates()
	{
		return super.getKeyStates();
	}

	/**
	 * Used to create and retrieve the FireScreen singleton.
	 * 
	 * @param display if not null and its the first call of the method, a FireScreen instance for this display is created.
	 * @return the FireScreen singleton.
	 */
	public static FireScreen getScreen(Display display)
	{
		return getScreen(display, false);	
	}
	
	/**
	 * Used to create and retrieve the FireScreen singleton.
	 * 
	 * @param display if not null and its the first call of the method, a FireScreen instance for this display is created.
	 * @param suppressKeyEvents suppress normal key events @see #GameCanvas
	 * 
	 * @return the FireScreen singleton.
	 */
	public static FireScreen getScreen(Display display,boolean suppressKeyEvents)
	{
		if (display != null && singleton == null)
		{
			singleton = new FireScreen(display,suppressKeyEvents);
		}
		return singleton;
	}


	/**
	 * Used to create and retrieve the FireScreen singleton.
	 * 
	 * @return the FireScreen singleton.
	 */
	public static FireScreen getScreen()
	{
		if (singleton == null)
			throw new NullPointerException("FireScreen is not initialized.");
		return singleton;
	}

	/**
	 * Returns the current Theme set to Fire
	 * 
	 * @return
	 */
	public static Theme getTheme()
	{
		return theme;
	}

	/**
	 * Sets the given theme as the default theme of fire<br/> Note: Some
	 * components might need redrawing after a theme change. This method does
	 * not cause anything to be redrawn or recreated with the new theme settings
	 * 
	 * @param theme
	 */
	public static void setTheme(Theme theme)
	{
		if (theme != null)
			FireScreen.theme = theme;
		else
			FireScreen.theme = new Theme();

		navbarFont = theme.getFontProperty("navbar.font");
	}

	protected void pointerDragged(int x, int y)
	{
		if (orientation != NORMAL)
		{ // screen is on landscape mode, width is height and vise versa
			int t = x;
			if (orientation == LANDSCAPELEFT)
			{
				x = super.getHeight() - y;
				y = t;
			} else
			{
				x = y;
				y = super.getWidth() - t;
			}
		}
		for (int i = componentSlots.length - 1; i >= 0; --i)
		{
			Component cmp = componentSlots[i];
			if (cmp != null && (cmp instanceof Container || cmp.isFocusable()))
			{ // only send events to containers or focusable components
				cmp.pointerDragged(x - cmp.x, y - cmp.y);
				break;// only send the pointer event once.
			}
		}
	}

	protected void pointerPressed(int x, int y)
	{
		if (orientation != NORMAL)
		{ // screen is on landscape mode, width is height and vise versa
			int t = x;
			if (orientation == LANDSCAPELEFT)
			{
				x = super.getHeight() - y;
				y = t;
			} else
			{
				x = y;
				y = super.getWidth() - t;
			}
		}
		for (int i = componentSlots.length - 1; i >= 0; --i)
		{
			Component cmp = componentSlots[i];
			if (cmp != null && (cmp instanceof Container || cmp.isFocusable()))
			{ // only send events to containers or focusable components
				cmp.pointerPressed(x - cmp.x, y - cmp.y);
				break;// only send the pointer event once.
			}
		}
	}

	protected void pointerReleased(int x, int y)
	{
		if (orientation != NORMAL)
		{ // screen is on landscape mode, width is height and vise versa
			int t = x;
			if (orientation == LANDSCAPELEFT)
			{
				x = super.getHeight() - y;
				y = t;
			} else
			{
				x = y;
				y = super.getWidth() - t;
			}
		}
		
		Component lsk = componentSlots[LEFT_SOFTKEY_ZINDEX-ZINDEX_MIN];
		Component rsk = componentSlots[RIGHT_SOFTKEY_ZINDEX-ZINDEX_MIN];
		Component softKeyOwner = selectedComponent;
		if((softKeyOwner==null || (softKeyOwner.leftSoftKeyCommand==null && softKeyOwner.rightSoftKeyCommand==null)) && softkeyController>-1)
		{
			softKeyOwner = componentSlots[softkeyController];
		}
		
		if(softKeyOwner!=null)
		{
			if(lsk!=null && x>=lsk.x && x<(lsk.x+lsk.width) && y>=lsk.y && y<(lsk.y+lsk.height))
			{ // left softkey event
				handleSoftKeyEvent(softKeyOwner, FireScreen.leftSoftKey);			
				return;
			}
			else if(rsk!=null && x>=rsk.x && x<(rsk.x+rsk.width) && y>=rsk.y && y<(rsk.y+rsk.height))
			{ // right softkey event
				handleSoftKeyEvent(softKeyOwner, FireScreen.rightSoftKey);			
				return;
			}
		}

		for (int i = componentSlots.length - 1; i >= 0; --i)
		{
			Component cmp = componentSlots[i];
			if (cmp != null && (cmp instanceof Container || cmp.isFocusable()))
			{ // only send events to containers or focusable components
				cmp.pointerReleased(x - cmp.x, y - cmp.y);
				break;// only send the pointer event once.
			}
		}
	}

	/**
	 * The keyState array holds the time that each game key was pressed. The
	 * animation thread checks the time to generate repeat events
	 * 
	 * @param pressed
	 * @param keyCode
	 */
	private void updateRepeatEventData(boolean pressed, int keyCode)
	{
		if (pressed)
		{
			pressedKeyCode = keyCode;
			pressedKeyTime = System.currentTimeMillis();
		} else
		{
			pressedKeyCode = NO_PRESSED_KEY;
		}
	}

	private void generateRepeatEvent(long now)
	{
		if (pressedKeyCode != NO_PRESSED_KEY && (now - pressedKeyTime) > keyRepeatPeriod)
		{
			pressedKeyTime = now;
			keyRepeated(pressedKeyCode);
		}
	}

	private int getFireScreenKey(int keyCode)
	{
		int ga = super.getGameAction(keyCode);
		switch (ga)
		{
		case Canvas.UP:
			if (orientation == LANDSCAPELEFT) // up key is right
				return getKeyCode(Canvas.RIGHT);
			if (orientation == LANDSCAPERIGHT) // up key is left
				return getKeyCode(Canvas.LEFT);
			return keyCode;
		case Canvas.DOWN:
			if (orientation == LANDSCAPELEFT) // down key is left
				return getKeyCode(Canvas.LEFT);
			if (orientation == LANDSCAPERIGHT) // down key is right
				return getKeyCode(Canvas.RIGHT);
			return keyCode;
		case Canvas.LEFT:
			if (orientation == LANDSCAPELEFT) // left key is up
				return getKeyCode(Canvas.UP);
			if (orientation == LANDSCAPERIGHT) // left key is down
				return getKeyCode(Canvas.DOWN);
			return keyCode;
		case Canvas.RIGHT:
			if (orientation == LANDSCAPELEFT) // right key is down
				return getKeyCode(Canvas.DOWN);
			if (orientation == LANDSCAPERIGHT) // left key is up
				return getKeyCode(Canvas.UP);
			return keyCode;
		default:
			return keyCode;
		}
	}

	protected void keyPressed(int keyCode)
	{
		if (orientation != NORMAL)
		{
			keyCode = getFireScreenKey(keyCode);
		}
		if (generateRepeatEvents)
		{
			updateRepeatEventData(true, keyCode);
		}
		if (selectedComponent != null)
		{
			selectedComponent.keyPressed(keyCode);
		} else
		{
			for (int i = componentSlots.length - 1; i >= 0; --i)
			{
				Component cmp = componentSlots[i];
				if (cmp != null && (cmp instanceof Container || cmp.isFocusable()))
				{ // only send events to containers or focusable components
					cmp.keyPressed(keyCode);
					break;// only send the key event once.
				}
			}
		}
	}

	public int getGameAction(int keyCode)
	{
		return super.getGameAction(keyCode);
	}

	private boolean handleSoftKeyEvent(Component current, int k)
	{
		if (k == leftSoftKey && current.leftSoftKeyCommand != null)
		{
			if (animationsEnabled && componentSlots[LEFT_SOFTKEY_ZINDEX - ZINDEX_MIN]!=null) 
				registerAnimation((Animation)componentSlots[LEFT_SOFTKEY_ZINDEX - ZINDEX_MIN]);

			final Component trigger = current;
			Thread th = new Thread()
			{
				public void run()
				{
					trigger.commandListener.commandAction(trigger.leftSoftKeyCommand, trigger);
				}
			};
			th.start();
			return true;
		}
		if (k == rightSoftKey && current.rightSoftKeyCommand != null)
		{
			if (animationsEnabled && componentSlots[RIGHT_SOFTKEY_ZINDEX - ZINDEX_MIN]!=null) 
				registerAnimation((Animation)componentSlots[RIGHT_SOFTKEY_ZINDEX - ZINDEX_MIN]);

			final Component trigger = current;
			Thread th = new Thread()
			{
				public void run()
				{					
					trigger.commandListener.commandAction(trigger.rightSoftKeyCommand, trigger);
				}
			};
			th.start();
			return true;
		}
		return false;

	}

	protected void keyReleased(int k)
	{
		if (orientation != NORMAL)
		{
			k = getFireScreenKey(k);
		}

		if (generateRepeatEvents)
		{
			updateRepeatEventData(false, k);
		}

		Component current = selectedComponent;
		// handle softkey events first
		if (k == leftSoftKey || k == rightSoftKey)
		{
			if (current != null && current.selected && current.commandListener != null && handleSoftKeyEvent(current, k))
			{
				if (current.keyListener != null)
					current.keyListener.keyReleased(k, current); // send the
																	// key event
																	// if a
																	// listener
																	// is
																	// present
				return;
			}
			// softkey event goes to top level Component. The first top level
			// focusable or container component will be used.
			// even if it does not have a commandListener or an associated
			// softkey event.
			for (int i = componentSlots.length - 1; i >= 0; --i)
			{
				Component cmp = componentSlots[i];
				if (cmp != null && (cmp instanceof Container || cmp.isFocusable()))
				{ // only send events to containers or focusable components
					if (cmp.commandListener != null)
						handleSoftKeyEvent(cmp, k);
					if (cmp.keyListener != null)
						cmp.keyListener.keyReleased(k, cmp); // send the key
																// event if a
																// listener is
																// present
					return;// only send the key event once.
				}
			}
		}
		if (current != null && current.selected)
		{
			if ((current instanceof Container) == false)
			{ // keyReleased might change the selectedComponent
				current.keyReleased(k);
				if (current.selected)
					return;
			}
			// else component is not anymore selected due to keyReleased event.
			// send the event to the containers
			// find a panel that contains this component and send the event to
			// it.
			Component tmp = current.parent;
			while (tmp != null && (tmp instanceof Panel) == false)
			{
				tmp = tmp.parent;
			}
			if (tmp != null) // a panel found
			{
				((Panel) tmp).keyReleased(k);
				return;
			}
		}

		for (int i = componentSlots.length - 1; i >= 0; --i)
		{
			Component cmp = componentSlots[i];
			if (cmp != null && (cmp instanceof Container || cmp.isFocusable()))
			{ // only send events to containers or focusable components
				cmp.keyReleased(k);
				return;// only send the key event once.
			}
		}
	}

	protected void keyRepeated(int k)
	{
		if (orientation != NORMAL)
		{
			k = getFireScreenKey(k);
		}

		Component current = selectedComponent;

		if (current != null && current.selected)
		{
			if ((current instanceof Container) == false)
			{ // keyReleased might change the selectedComponent
				current.keyReleased(k);
				if (current.selected)
					return;
			}
			// else component is not anymore selected due to keyReleased event.
			// send the event to the containers
			// find a panel that contains this component and send the event to
			// it.
			Component tmp = current.parent;
			while (tmp != null && (tmp instanceof Panel) == false)
			{
				tmp = tmp.parent;
			}
			if (tmp != null) // a panel found
			{
				((Panel) tmp).keyReleased(k);
				return;
			}
		}

		for (int i = componentSlots.length - 1; i >= 0; --i)
		{
			Component cmp = componentSlots[i];
			if (cmp != null && (cmp instanceof Container || cmp.isFocusable()))
			{ // only send events to containers or focusable components
				cmp.keyReleased(k);
				return;// only send the key event once.
			}
		}
	}

	protected void sizeChanged(int w, int h)
	{
		super.sizeChanged(w, h);
		sizeChangedImpl(w, h);
		if(fireListener!=null)
		{
			fireListener.sizeChanged(w, h);
		}
	}

	private void sizeChangedImpl(int w, int h)
	{
		offscreen = null;

		for (int i = componentSlots.length - 1; i >= 0; --i)
		{
			Component cmp = componentSlots[i];
			if (cmp != null)
			{
				cmp.valid = false;
			}
		}
		
		updateSoftKeys();

		repaint();
	}

	/**
	 * Returns the width of this FireScreen. If the screen is in landscape mode,
	 * it will return the real height of the screen.
	 * 
	 * @see javax.microedition.lcdui.Displayable#getWidth()
	 */
	public int getWidth()
	{
		if (orientation == NORMAL)
		{
			return super.getWidth();
		}
		return super.getHeight();
	}

	/**
	 * Returns the height of this FireScreen. If the screen is in landscape
	 * mode, it will return the real width of the screen.
	 * 
	 * @see javax.microedition.lcdui.Displayable#getHeight()
	 */
	public int getHeight()
	{
		if (orientation == NORMAL)
		{
			return super.getHeight();
		}
		return super.getWidth();
	}

	/**
	 * Returns the orientation of the FireScreen instance.
	 * 
	 * @see #NORMAL
	 * @see #LANDSCAPELEFT
	 * @see #LANDSCAPERIGHT
	 * 
	 * @return
	 */
	public int getOrientation()
	{
		return orientation;
	}

	/**
	 * Sets the orientation of the FireScreen to the given value. This method
	 * will cause all components to be validated and redrawn on the new
	 * orientation.
	 * 
	 * @throws IllegalArgumentException
	 *             if orientation is no FireScreen.NORMAL,
	 *             FireScreen.LANDSCAPELEFT or FireScreen.LANDSCAPERIGHT
	 * @param orientation
	 */
	public void setOrientation(int orientation)
	{
		
		if (orientation != NORMAL && orientation != LANDSCAPELEFT && orientation != LANDSCAPERIGHT)
			throw new IllegalArgumentException("Unknown orientation value " + orientation);
		
		if(this.orientation==orientation) return; // nothing to do.

		this.orientation = orientation;
		this.sizeChangedImpl(super.getWidth(), super.getHeight());
	}

	/**
	 * Returns the components that currently has keyboard focus (if any)
	 * 
	 * @return
	 */
	public Component getSelectedComponent()
	{
		return selectedComponent;
	}

	/**
	 * Sets the keyboard focus to the given component. If there was already
	 * another component in focus this method will cause the old component to be
	 * deselected.
	 * 
	 * @param newSelectedComponent
	 */
	public void setSelectedComponent(Component newSelectedComponent)
	{
		boolean updateSoftkeys=false;
		if (newSelectedComponent == selectedComponent)
			return; // nothing to do here
		if (selectedComponent != null)
		{
			if(selectedComponent.rightSoftKeyCommand!=null || selectedComponent.leftSoftKeyCommand!=null)
				updateSoftkeys=true;
			if(selectedComponent.selected)
				selectedComponent.setSelected(false);
		}
		this.selectedComponent = newSelectedComponent;
		
		if(newSelectedComponent!=null && (newSelectedComponent.leftSoftKeyCommand!=null || newSelectedComponent.rightSoftKeyCommand!=null))
			updateSoftkeys=true;
		
		if(updateSoftkeys) updateSoftKeys();
	}

	/**
	 * Repaints a part of the FireScreen canvas. The coordinates are in
	 * reference to the FireScreen coordinates and not to the real screen. That
	 * means that in landscape modes the correct part of the screen will be
	 * redrawn.
	 * 
	 * @param cx
	 * @param cy
	 * @param cwidth
	 * @param cheight
	 */
	public void repaintScreen(int cx, int cy, int cwidth, int cheight)
	{
		switch (orientation)
		{
		case NORMAL:
			repaint(cx, cy, cwidth, cheight);
			break;
		case LANDSCAPELEFT:
			repaint(cy, super.getHeight() - cx - cwidth, cheight, cwidth);
			break;
		case LANDSCAPERIGHT:
			repaint(super.getWidth() - cy - cheight, cx, cheight, cwidth);
			break;
		}
	}

	/**
	 * Causes this FireScreen instance to be destroyed. Stopping the animation
	 * handler. This method should be called during the middlet clean-up stage.<br/>
	 * Note: The method will cause the animation handler to die but may return
	 * before the thread is dead.
	 */
	public void destroy()
	{
		destroyed = true;
		// try
		// {
		// animationThread.join();
		// } catch (InterruptedException e)
		// {
		// e.printStackTrace();
		// }
	}

	/**
	 * @see #setAnimationsEnabled(boolean)
	 * @return
	 */
	public boolean isAnimationsEnabled()
	{
		return animationsEnabled;
	}

	/**
	 * On lower-end devices with less CPU and memory available it is often good
	 * practice not to show animations that may consume resources needed for
	 * more usefull operations. When this flag is set the FireScreen and all
	 * Fire components that have animations associated with them (i.e. Panel
	 * scroll, softkey animations etc) will not display animations.
	 * 
	 * @param animationsEnabled
	 */
	public void setAnimationsEnabled(boolean animationsEnabled)
	{
		this.animationsEnabled = animationsEnabled;
	}

	protected void hideNotify()
	{
		pressedKeyCode = NO_PRESSED_KEY;
		visible = false;
		if(fireListener!=null)
		{
			fireListener.hideNotify();
		}
	}

	protected void showNotify()
	{
		pressedKeyCode = NO_PRESSED_KEY; // reset previous keyState,
		visible = true;
		repaintScreen(0, 0, getWidth(), getHeight());
		if(fireListener!=null)
		{
			fireListener.showNotify();
		}
	}
	
	/**
	 * Creates an image with the contents of the FireScreen. 
	 * This method is usefull to get snapshots of the screen to use with animations or
	 * store them in files.
	 * This method will create a new image with the dimensions of the screen and 
	 * will render the FireScreen contents inside it.
	 * @param withSoftkeys If false, the screenshot will not contain the softkeys. @see isPaintSoftkeys
	 * 
	 * @return
	 */
	public Image getScreenshot(boolean withSoftkeys)
	{
		synchronized (this)
		{
			boolean oldv = paintSoftkeys;
			paintSoftkeys=withSoftkeys;
			try{
				Image res = Image.createImage(getWidth(), getHeight());
				paint(res.getGraphics());
				return res;
			}finally{
				paintSoftkeys=oldv;
			}
		}
	}

	/**
	 * If true the FireScreen will paint the softkeys on the screen. This 
	 * @return the paintSoftkeys
	 */
	public boolean isPaintSoftkeys()
	{
		return paintSoftkeys;
	}

	/**
	 * @param paintSoftkeys the paintSoftkeys to set
	 */
	public void setPaintSoftkeys(boolean paintSoftkeys)
	{
		synchronized (this)
		{
			this.paintSoftkeys = paintSoftkeys;			
		}
	}

	/**
	 * @return the fireListener
	 */
	public FireListener getFireListener()
	{
		return fireListener;
	}

	/**
	 * @param fireListener the fireListener to set
	 */
	public void setFireListener(FireListener fireListener)
	{
		this.fireListener = fireListener;
	}
}