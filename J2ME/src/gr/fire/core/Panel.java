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

package gr.fire.core;

import gr.fire.ui.ScrollAnimation;

import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * A Panel is a special type of Container. It can contain at most one container.
 * If the inner Container is bigger that the Panel's dimensions the Panel can be configured
 * to display scrollbars.
 * 
 * The Panel will allow navigation on the Container's components thought its viewport. 
 * The Panel will scroll to bring the focused component inside the viewport.<br/>
 * 
 * The panel can display decorations around its viewport, a navigation bar (navbar) at the bottom and a 
 * title bar  (titlebar) at the top of the screen. Also a logo image can be displaed on a Theme specific location. 
 * If the {@link #setShowBackground(boolean)} flag is set, the panel will also paint a theme specific background behing the container.
 * Note that the container must be transparent in order for the background to be visible.<br/>
 * 
 * 
 * On the navbar the softkey commands (if any) will appear. 
 * On the titlebar the label of the Panel will be displayed {@link #setLabel(String)}.<br/>
 * 
 * On phones with touchscreens a Panel with scrollbars will intercept tap events on the scrollbars and scroll the container.<br/> 
 * 
 * @see Container
 * @author padeler
 * 
 */
public class Panel extends Container
{
	/**
	 * To make the navigation inside the panel easier the Panel will scroll faster when several conditions are met.
	 * For example it will scroll fast down if there are no hotizontal scrollbars AND the user pressed right to move to the next
	 * selectable component.<br/>
	 * 
	 * This static value defines the percentage of the phones screen on the dimension of the scroll that will be used to 
	 * calculate the fast scroll.
	 * 
	 */
	public static final int FAST_SCROLL_PERCENT=60;
	/**
	 * This static value defines the percentage of the phones screen on the dimension of the scroll that will be used to 
	 * calculate the normal scroll.
	 * 
	 */
	public static final int NORMAL_SCROLL_PERCENT=35;
	

	/**
	 * If set, and the Container inside this Panel has container.height>this.height then vertival scrollbars will be visible.
	 */
	public static final int VERTICAL_SCROLLBAR = 0x00000001;
	/**
	 * If set, and the Container inside this Panel has container.width>this.width then horizontal scrollbars will be visible.
	 */
	public static final int HORIZONTAL_SCROLLBAR = 0x00000100;
	/**
	 * Default scrollbar policy.
	 */
	public static final int NO_SCROLLBAR = 0x00000000;

	private int scrollBarPolicy = NO_SCROLLBAR;// no scrollbars
	private boolean showDecorations = false;
	private int scrollX,scrollY;
	
	protected Container container = null;
	private boolean showBackground=false;
	
	
	int viewPortWidth, viewPortHeight;
	
	private boolean closeOnOutofBoundsPointerEvents=true; // controls if this panel should close when the user taps outside its bounding box. 

	private Theme theme;
	
	private String label;
	private int labelX=0,labelY=0;

	private Image titlebarImage, navbarImage,backgroundTexture;

	private int decorLeft=0,titlebarSize=0,decorRight=0,navbarSize=0;
	
	private int dragX=-1,dragY=-1;
	private boolean dragScroll=false;
	private int dragDiff=25; // 25 pixels drag will cause scroll if drag scroll is enabled.
	
	int normalVScrollLength,fastVScrollLength,normalHScrollLength,fastHScrollLength;
	
	/**
	 * Constructs a Panel. 
	 * @param cnt The container placed inside this panel 
	 * @param scrollbarPolicy show vertical/horizontal scrollbars or not.
	 * @param showDecorations display decorations. Decorations are theme specific.
	 */
	public Panel(Container cnt, int scrollbarPolicy, boolean showDecorations)
	{
		setFocusable(true);
		this.scrollBarPolicy = scrollbarPolicy;
		this.showDecorations = showDecorations;
		if(cnt!=null)
			set(cnt);

		theme = FireScreen.getTheme();
	}

	/**
	 * Constructs a Panel without a container with the given scrollbar policy and with no decorations.
	 * @param scrollBarPolicy
	 */
	public Panel(int scrollBarPolicy)
	{
		this(null, scrollBarPolicy, false);
	}

	/**
	 * Constructs a Panel with no scrollbars no inner container and no decorations
	 */
	public Panel()
	{
		this(null, NO_SCROLLBAR, false);
	}

	public void paint(Graphics g)
	{
		int originalTrX = g.getTranslateX();
		int originalTrY = g.getTranslateY();
		int originalClipX = g.getClipX();
		int originalClipY = g.getClipY();
		int originalClipWidth = g.getClipWidth();
		int originalClipHeight = g.getClipHeight();

		Component cmp = container;
		if (cmp != null) // draw only visible components
		{
			if (cmp.valid == false)
			{ // validate my component.
				cmp.validate();
			}
			
			if(showBackground)
			{
				if(backgroundTexture==null)
					backgroundTexture = theme.getBackgroundTexture(viewPortWidth,viewPortHeight);
				if(backgroundTexture!=null)
					g.drawImage(backgroundTexture,decorLeft,titlebarSize,Graphics.TOP|Graphics.LEFT);
			}

			if (cmp.visible)
			{

				if (cmp.animation == null)
				{
					if(cmp.intersects(originalClipX, originalClipY, originalClipWidth, originalClipHeight))
					{
						g.clipRect(cmp.x, cmp.y, cmp.width, cmp.height);
						g.translate(cmp.x, cmp.y);
						cmp.paint(g);
						// return to the coordinates of this component.
						g.translate(originalTrX - g.getTranslateX(), originalTrY - g.getTranslateY());
						g.setClip(originalClipX, originalClipY, originalClipWidth, originalClipHeight);

					}
				}
				else 
				{
					if(cmp.animation.intersects(originalClipX, originalClipY, originalClipWidth, originalClipHeight))
					{
						g.clipRect(cmp.animation.x, cmp.animation.y, cmp.animation.width, cmp.animation.height);
						g.translate(cmp.animation.x, cmp.animation.y);
						cmp.animation.paint(g);						
						// return to the coordinates of this component.
						g.translate(originalTrX - g.getTranslateX(), originalTrY - g.getTranslateY());
						g.setClip(originalClipX, originalClipY, originalClipWidth, originalClipHeight);
					}
				}
			}
		}
		
		if (showDecorations) // this panel has decorations
		{
			if(originalClipY < titlebarSize)
				drawTitlebar(g);
			if((originalClipY + originalClipHeight) > (height - navbarSize))
				drawNavbar(g);
			if(originalClipX < decorLeft)
				drawDecorLeft(g);
			if((originalClipX + originalClipWidth) > (width - decorRight))
				drawDecorRight(g);
			Image logo = theme.getLogo();
			if(logo!=null)
			{
				int lx=0,ly=0;
				switch (theme.getIntProperty("logo.icon.valign"))
				{
					case FireScreen.TOP:
						ly=0;
						break;
					case FireScreen.BOTTOM:
						ly = getHeight()-logo.getHeight();
						break;
					case FireScreen.VCENTER:
						ly = (getHeight()-logo.getHeight())/2;
						break;
				}
				switch (theme.getIntProperty("logo.icon.align"))
				{
					case FireScreen.LEFT:
						lx=0;
						break;
					case FireScreen.RIGHT:
						lx= getWidth()-logo.getWidth();
						break;
					case FireScreen.CENTER:
						lx= (getWidth()-logo.getWidth())/2;
						break;
				}
				g.drawImage(logo,lx,ly,Graphics.TOP|Graphics.LEFT);
			}
			if(label!=null)
			{
				Font labelFont = theme.getFontProperty("label.font"); 
				g.setFont(labelFont);
				g.setColor(theme.getIntProperty("titlebar.fg.color"));
				int lw = labelFont.stringWidth(label);
				g.drawString(label, labelX, labelY, Graphics.TOP | Graphics.LEFT);
			}
		}
		
		if(border)
		{
			g.setColor(theme.getIntProperty("border.color"));
			g.drawRect(0,0,width-1,height-1);
		}
		
		drawScrollbars(g);
	}
	
	/**
	 * Returns the label set to this Panel. The label is displayed on the top side of the Panel, on the area of the titlebar.
	 * The alignment, the font and color of the label are theme specific.
	 * 
	 * @return
	 */
	public String getLabel()
	{
		return label;
	}

	/** 
	 * Sets a label for this panel.
	 * @see #getLabel()
	 * @param label
	 */
	public void setLabel(String label)
	{
		this.label=label;
		if(label!=null)
		{// find the labelX and labelY.
			int align = theme.getIntProperty("label.align");
			int valign = theme.getIntProperty("label.valign");
			Font f = theme.getFontProperty("label.font");
			int strLen = f.stringWidth(label);
			int strHeight = f.getHeight();
			
			switch(align)
			{
			case FireScreen.CENTER:
				labelX= FireScreen.getScreen().getWidth()/2-strLen/2;
				break;
			case FireScreen.RIGHT:
				labelX= FireScreen.getScreen().getWidth()-strLen;
				break;
			default:
				labelX=0;
			}
			switch(valign)
			{
			case FireScreen.VCENTER:
				labelY= FireScreen.getScreen().getHeight()/2-strHeight/2;
				break;
			case FireScreen.BOTTOM:
				labelY= FireScreen.getScreen().getHeight()-strHeight;
				break;
			default:
				labelY=0;
			}
		}
	}
	
	private void drawScrollbars(Graphics g)
	{
		if(container!=null)
		{
			if ((scrollBarPolicy & VERTICAL_SCROLLBAR) == VERTICAL_SCROLLBAR && container.height > viewPortHeight)
			{ // draw vertical scrollbar
				int rightHeight = height - navbarSize - titlebarSize;
				g.setColor(theme.getIntProperty("scrollbar.color"));
				int vpPosY = getViewPortPositionY();
				scrollY = (rightHeight * (100 * (vpPosY + viewPortHeight / 2)) / container.height) / 100;
				int tl = theme.scrollLenght / 2;
				if (scrollY < tl || vpPosY == 0)
					scrollY = tl;
				else if (scrollY > rightHeight - tl || vpPosY == container.height - viewPortHeight)
					scrollY = rightHeight - tl;
		
				g.fillRect(width - theme.scrollSize + 1, titlebarSize + scrollY - tl, theme.scrollSize - 1, theme.scrollLenght);
			}		
	
			if ((scrollBarPolicy & HORIZONTAL_SCROLLBAR) == HORIZONTAL_SCROLLBAR && container.width > viewPortWidth)
			{ // draw vertical scrollbar
				int bottomWidth = width - decorLeft - decorRight;
				// draw scroll bar area.
				g.setColor(theme.getIntProperty("scrollbar.color"));
				int vpPosX = getViewPortPositionX();
				scrollX = (bottomWidth * (100 * (vpPosX + viewPortWidth / 2)) / container.width) / 100;
				int tl = theme.scrollLenght / 2;
				if (scrollX < tl || vpPosX == 0)
					scrollX = tl;
				else if (scrollX > bottomWidth - tl || vpPosX == container.width - viewPortWidth)
					scrollX = bottomWidth - tl;
	
				g.fillRect(decorLeft + scrollX - tl, height - navbarSize + 1, theme.scrollLenght, theme.scrollSize - 1);
			}
		}
	}

	private void drawTitlebar(Graphics g)
	{
		if (titlebarSize == 0)
			return;
		if (titlebarImage == null)
		{
			titlebarImage = theme.getTitlebarTexture(width, titlebarSize);
		}
		if(titlebarImage!=null)
			g.drawImage(titlebarImage, 0, 0, Graphics.TOP | Graphics.LEFT);

	}

	private void drawNavbar(Graphics g)
	{
		if (navbarSize == 0)
			return;

		if (navbarImage == null)
		{
			navbarImage = theme.getNavbarTexture(width, navbarSize);
		}
		if(navbarImage!=null)
		g.drawImage(navbarImage, 0, height - navbarSize, Graphics.TOP | Graphics.LEFT);
	}

	private void drawDecorLeft(Graphics g)
	{
		// decor left not supported by this panel implementation.
	}

	private void drawDecorRight(Graphics g)
	{
		// decor right not supported by this panel implementation.
	}

	public void validate()
	{
		theme = FireScreen.getTheme();
		if(showDecorations)
		{
			decorLeft = theme.decorLeft;
			titlebarSize = theme.decorTop;
			navbarSize = theme.decorBottom;
			decorRight = theme.decorRight;
		}
		else
		{
			decorLeft=0;
			titlebarSize=0;
			navbarSize=0;
			decorRight=0;
		}
		
		navbarImage = null;
		titlebarImage = null;

		int[] d = getPrefSize();
		if (d == null)
		{
			d = getMinSize();
		}
		width = d[0];
		height = d[1];


		if (container == null)
		{
			valid = true;
			return;
		}
		
		viewPortHeight = height - navbarSize - titlebarSize;
		viewPortWidth = width - decorLeft - decorRight;
		
		int []ps = container.getPrefSize();
		if(ps!=null) // a container inside a panel must be atleast the size of the viewport
		{
			if(ps[0]<viewPortWidth) ps[0] = viewPortWidth;
			if(ps[1]<viewPortHeight) ps[1] = viewPortHeight;
			container.setPrefSize(ps[0],ps[1]);
		}
		
		container.validate(); // validate the container

		container.x = decorLeft;
		container.y = titlebarSize;

		normalVScrollLength = (NORMAL_SCROLL_PERCENT*viewPortHeight)/100;
		normalHScrollLength = (NORMAL_SCROLL_PERCENT*viewPortWidth)/100;
		fastVScrollLength = (FAST_SCROLL_PERCENT*viewPortHeight)/100;
		fastHScrollLength = (FAST_SCROLL_PERCENT*viewPortWidth)/100;
		
		valid = true;
	}

	/**
	 * A Panel can have at most one container at any given time.
	 * 
	 * @param container
	 */
	public void set(Container container)
	{
		if(container==null) throw new IllegalArgumentException("Cannot set a null container to a Panel. Use remove() instead.");
		if (this.container != null)
			this.container.parent = null;

		this.container = container;
		
		if (container.parent != null)
		{
			if (container.parent instanceof Container)
				((Container) container.parent).remove(container);
		}
		container.parent = this;
		valid = false;
	}
	
	public Component getComponent(int i)
	{
		if(i==0)
			return container;
		else throw new ArrayIndexOutOfBoundsException("A panel has at most one container on index 0.");
	}
	
	public boolean remove(Component c)
	{
		if (container != c)
		{
			throw new IllegalArgumentException("Container "+c+" is not inside this panel.");
		}
		else if(container!=null)
		{
			container.parent = null;
			if(container.animation!=null) FireScreen.getScreen().removeAnimation(container.animation);
			container = null;
			valid = false;
			return true;
		}
		return false;
	}
	
	public int countComponents()
	{
		if(container!=null)
			return container.countComponents();
		else return 0;
	}
	
	

	/**
	 * 
	 * Sets the viewport position relative to the top left corner of the
	 * container inside this panel.
	 * 
	 * 
	 * @param x
	 *            the distance (in pixels) of the left side of the viewport from
	 *            the left side of the container
	 * @param y
	 *            the distance (in pixels) of the top side of the viewport from
	 *            the top side of the container
	 */
	public boolean setViewPortPosition(int x, int y)
	{
		if (container == null)
			return false;

		int tx = decorLeft - container.x;
		int ty = titlebarSize - container.y;


		if (x + viewPortWidth > container.width)
			x = container.width - viewPortWidth;
		else if (x < 0)
			x = 0;
		if (y + viewPortHeight > container.height)
			y = container.height - viewPortHeight;
		else if (y < 0)
			y = 0;
		
		if(x==tx && y==ty) // nothing changed return false
		{
			return false;
		}
		
		// ok now set the correct offset to the container.
		container.x = decorLeft - x;
		container.y = titlebarSize - y;
		repaint();
		return true; // change in the viewport position was successfull. 
	}

	/**
	 * Returns the vertical (X) coordinate of the viewport inside the innner container.
	 * @return
	 */
	public int getViewPortPositionX()
	{
		if (container == null)
			return 0;

		return decorLeft - container.x;
	}

	/**
	 * Returns the horizontal (Y) coordinate of the viewport inside the inner container.
	 * @return
	 */
	public int getViewPortPositionY()
	{
		if (container == null)
			return 0;
		return titlebarSize - container.y;
	}

	/**
	 * Returns the width of the viewport of this panel.
	 * @return
	 */
	public int getViewPortWidth()
	{
		return viewPortWidth;
	}
	
	/**
	 * Returns the height of the viewport of this panel.
	 * @return
	 */
	public int getViewPortHeight()
	{
		return viewPortHeight;
	}

	protected void pointerDragged(int x, int y)
	{
		if (container != null)
		{
			if(dragScroll && dragX==-1 && dragY==-1) // intercept drag events end use them for navigation.
			{ // keep the starting drag point. when the pointer is released a pointer event will be sent.
				dragX = x;
				dragY = y;
			}
			else if (x > decorLeft && x < width - decorRight && y > titlebarSize && y < height - navbarSize)
			{
				container.pointerDragged(x - container.x, y - container.y); 
			}
		}
		super.pointerDragged(x,y);
	}

	protected void pointerPressed(int x, int y)
	{
		if (container != null)
		{
			if (x > decorLeft && x < width - decorRight && y > titlebarSize && y < height - navbarSize)
			{
				container.pointerPressed(x - container.x, y - container.y);
			}
		}
		super.pointerPressed(x,y);
	}

	protected void pointerReleased(int x, int y)
	{
		if(closeOnOutofBoundsPointerEvents && (x<0 || x>width || y<0 || y>height)) FireScreen.getScreen().removeComponent(this);
		
		if(container != null)
		{
			int dx = 0;
			int dy = 0;
			if(dragX!=-1 && dragY!=-1){
				dx = x - dragX;
				dy = y - dragY;
			}
			if(dragScroll && (dx>dragDiff || dx<-dragDiff || dy>dragDiff || dy<-dragDiff))
			{ // drag event.
				dx = x - dragX;
				dy = y - dragY;
				
				dragX=-1;dragY=-1;
				int horizontal=0,vertical=0;
				if(animation==null)
				{
					if(dy<-dragDiff) vertical = fastVScrollLength;
					else if(dy>dragDiff) vertical =-fastVScrollLength;

					if(dx<-dragDiff) horizontal = fastHScrollLength;
					else if(dx>dragDiff) horizontal= -fastHScrollLength;
				}
				scrollPanel(horizontal, vertical);
			}
			else//If there is not a big dx or dy fire the event
			{
				int rightMargin = decorRight;
				if((scrollBarPolicy & VERTICAL_SCROLLBAR) == VERTICAL_SCROLLBAR && rightMargin<2*theme.scrollSize)
				{
					rightMargin = 2*theme.scrollSize;
				}
				
				if (x > decorLeft && x < width - rightMargin && y > titlebarSize && y < height - navbarSize)
				{
					container.pointerReleased(x - container.x, y - container.y);
				}
				else
				{
					if (((scrollBarPolicy & VERTICAL_SCROLLBAR) == VERTICAL_SCROLLBAR) && x > width - rightMargin && y < height - navbarSize)
					{ // check if click is on scrollbar
						if (y > scrollY ) // scroll down
						{
							scrollVertically(normalVScrollLength);
						} else
							// scroll up
						{
							scrollVertically(-normalVScrollLength);
						}
					} else if (((scrollBarPolicy & HORIZONTAL_SCROLLBAR) == HORIZONTAL_SCROLLBAR) && y > (height - navbarSize-theme.scrollSize) && y < height - navbarSize + theme.scrollSize)
					{
						if (x > scrollX)
						{
							scrollHorizontally(-normalHScrollLength);
						} else
						{
							scrollHorizontally(normalHScrollLength);
						}
					}
				}
			}
		}
		if(pointerListener!=null) pointerListener.pointerReleased(x,y,this);
	}

	/**
	 * Utility method to move the viewport vertically by the given number of pixels.
	 * If the number is negative the panel will scroll up, otherwise if will scroll down.
	 * @param pixels
	 */
	public void scrollVertically(int pixels)
	{
		scrollPanel(0,pixels);
	}

	/**
	 * Utility method to move the viewport horizontally by the given number of pixels.
	 * If the number is negative the panel will scroll right, otherwise if will scroll left.
	 * @param pixels
	 */
	public void scrollHorizontally(int pixels)
	{
		scrollPanel(pixels,0);
	}
	
	public void scrollPanel(int horizontal,int vertical)
	{
		if ((scrollBarPolicy & HORIZONTAL_SCROLLBAR) != HORIZONTAL_SCROLLBAR)
		{
			horizontal=0;		
		}
		
		if ((scrollBarPolicy & VERTICAL_SCROLLBAR) != VERTICAL_SCROLLBAR)
		{
			vertical=0;
		}
		if(horizontal==0 && vertical==0) return;
		
		int vpx = getViewPortPositionX();
		int vpy = getViewPortPositionY();
		setViewPortPosition(vpx+horizontal,vpy+vertical);
		
		FireScreen screen = FireScreen.getScreen();
		if(this.animation==null && screen.isAnimationsEnabled()) //only animate scroll if there is no other animation running on this panel.
		{
			ScrollAnimation anim = new ScrollAnimation(this,vpx,vpy,vpx+horizontal,vpy+vertical);
			screen.registerAnimation(anim);			
		}
	}

	protected void keyPressed(int keyCode)
	{
		if (container != null)
			container.keyPressed(keyCode);

		if(keyListener!=null) keyListener.keyPressed(keyCode,this);
	}

	protected void keyReleased(int keyCode)
	{
		FireScreen screen = FireScreen.getScreen();
		int gameCode = screen.getGameAction(keyCode);

		if (container != null && (gameCode==Canvas.LEFT || gameCode==Canvas.RIGHT || gameCode == Canvas.UP || gameCode == Canvas.DOWN ))
		{
			if(container.focusableComponents==null)
				container.focusableComponents = container.generateListOfFocusableComponents(true);

			Component lastSelected=screen.getSelectedComponent(); // previously selected component (may be outside this panel
			int index=-1;
			int focusableComponentsCount = container.focusableComponents.size();
			if(lastSelected!=null)
				index = container.focusableComponents.indexOf(lastSelected); // find the index of the lastSelected if it is inside this panel
			
			
			int vpx = getViewPortPositionX();
			int vpy = getViewPortPositionY();
			int vpmx = vpx+viewPortWidth;
			int vpmy = vpy+viewPortHeight;
			
			 
			// there are no focusable components or focus is already on the first or last. Check if we must move out of this container.
			if(((gameCode==Canvas.UP || gameCode==Canvas.LEFT) && (index==0 || focusableComponentsCount==0) && vpy==0) ||
			   ((gameCode==Canvas.DOWN || gameCode==Canvas.RIGHT) && (index==focusableComponentsCount-1 || focusableComponentsCount==0) && vpmy==container.height))
			{ // user wants to move out of this Panel. 
				screen.setSelectedComponent(this);
				// if parent is null i am a top level component, no need to send the event above me.
				if(parent!=null) parent.keyReleased(keyCode);
				else // top level component. Cycle the event only if there are selectable components
				{
					if(focusableComponentsCount>0)					
						screen.keyReleased(keyCode);
				}
				return;
			}
			
			if((gameCode==Canvas.LEFT || gameCode==Canvas.RIGHT) && 
				(container.layoutManager instanceof GridLayout)==false) // not grid layout
			{// special case for fast scrolling.
				if((container.width<=viewPortWidth || (scrollBarPolicy&HORIZONTAL_SCROLLBAR)!=HORIZONTAL_SCROLLBAR)) // no horizontal scrolling)
				{
					if(gameCode==Canvas.LEFT)
					{
						scrollVertically(-fastVScrollLength);
					}
					else if(gameCode==Canvas.RIGHT)
					{
						scrollVertically(fastVScrollLength);
					}					
				}
				else // horizontal scrolling
				{
					if(gameCode==Canvas.LEFT)
					{
						scrollHorizontally(-normalHScrollLength);
					}
					else if(gameCode==Canvas.RIGHT)
					{
						scrollHorizontally(normalHScrollLength);
					}
				}
			}
			else if(focusableComponentsCount>0)
			{
				// first locate the previously selected component (if any)
				if(index==-1) // selected component does not belong to this container. Deselect it.
				{
					screen.setSelectedComponent(null);
					lastSelected=null;
				}
				
				if(lastSelected!=null)
				{
					// check if the component is inside the viewport.		
					int []coords = getCoordsOfComponentInContainer(lastSelected,container);
					coords[0] += lastSelected.width/2; // translate coords to center of component
					coords[1] += lastSelected.height/2;
					
					if((coords[0]>= vpx && coords[0]<vpmx) && (coords[1]>= vpy && coords[1]<vpmy))
					{// component is inside the viewport, send the event to the container to select the next component
						container.keyReleased(keyCode);
						// check if newly selected component is outside the viewport
						Component newSelected = screen.getSelectedComponent();
						if(newSelected!=null && newSelected!=lastSelected)
						{
							coords = getCoordsOfComponentInContainer(newSelected,container);
							if(coords[0]<vpx || coords[0]+newSelected.width>vpmx || coords[1]<vpy || coords[1]+newSelected.height>vpmy)
							{// component is partially or completelly outside the viewport, scroll is good.
								scrollToSelectedComponent(fastHScrollLength,fastVScrollLength);
							}
						}
						else scroll(gameCode, (gameCode==Canvas.LEFT|| gameCode==Canvas.RIGHT)?normalHScrollLength:normalVScrollLength);
					}
					else 
					{// component is not inside the viewport
					 // if component is not in the direction of the key press (i.e. it is bellow the bottom of the viewport and the 
					 // key press is Canvas.UP) We should deselect it. 
						if((gameCode==Canvas.UP && coords[1]>vpmy) || 
						   (gameCode==Canvas.DOWN && coords[1]<vpy) ||
						   (gameCode==Canvas.LEFT && coords[0]>vpmx) ||
						   (gameCode==Canvas.RIGHT && coords[0]<vpx) )
						{ // deselect the component
							screen.setSelectedComponent(null);
							// send the event again.
							screen.keyReleased(keyCode);
							return;
						}
						// alternatevly, the movement is towards the component so scroll to the component.
						// bring the component inside the viewport at once
						scrollToSelectedComponent(fastHScrollLength,fastVScrollLength); 
					}
				}
				
				if(lastSelected==null) // find a component inside the viewport and send the event to it.
				{
					int newIndex;
					if(gameCode==Canvas.DOWN || gameCode==Canvas.RIGHT)
						newIndex = getFirstFocusableComponentInsideViewport(true);
					else  // UP or LEFT
						newIndex = getFirstFocusableComponentInsideViewport(false);
					if(newIndex==-1) // nothing found just scroll
						scroll(gameCode, (gameCode==Canvas.LEFT|| gameCode==Canvas.RIGHT)?normalHScrollLength:normalVScrollLength);
					else
					{
						Component newSelected = (Component)container.focusableComponents.elementAt(newIndex);
						screen.setSelectedComponent(newSelected);
						newSelected.keyReleased(keyCode);
					}
				}
			}
			else scroll(gameCode, (gameCode==Canvas.LEFT|| gameCode==Canvas.RIGHT)?normalHScrollLength:normalVScrollLength); // nothing to select. only scroll
		}
		if(keyListener!=null) keyListener.keyReleased(keyCode,this);
	}
	
	/**
	 * Brings the top left corner and as much of the selected component as possible inside the viewport of this panel.
	 * @param maxhscroll
	 * @param maxvscroll
	 */
	public void scrollToSelectedComponent(int maxhscroll,int maxvscroll)
	{
		Component cmp = FireScreen.getScreen().getSelectedComponent();
		if(cmp==null) return;
		if(cmp.selected==false) cmp.setSelected(true);
		
		int coords[] = getCoordsOfComponentInContainer(cmp,container);
		int vpx = getViewPortPositionX();
		int vpy = getViewPortPositionY();
		int diffX = coords[0]-vpx;
		int diffY = coords[1]-vpy;
		
		if(vpx+coords[0]+cmp.width<=viewPortWidth) // no need for horizontal scrolling
			diffX=0;
		
		if(diffY<0 && cmp.height<((25*viewPortHeight)/100)) diffY-= ((20*viewPortHeight)/100);
		
		if(maxvscroll>-1) 
		{
			if(diffY>0 && diffY>maxvscroll) diffY = maxvscroll;
			else if(diffY<0 && -diffY>maxvscroll) diffY = -maxvscroll;
		}
		if(maxhscroll>-1) 
		{
			if(diffX>0 && diffX>maxhscroll) diffX = maxhscroll;
			else if(diffX<0 && -diffX>maxhscroll) diffX = -maxhscroll;
		}

		setViewPortPosition(vpx+diffX,vpy+diffY);
		FireScreen screen = FireScreen.getScreen();
		if(this.animation==null && screen.isAnimationsEnabled())
		{ 
			ScrollAnimation anim = new ScrollAnimation(this,vpx,vpy,vpx+diffX,vpy+diffY);
			screen.registerAnimation(anim);
		}
	}
	
	private void scroll(int gameCode, int pixels)
	{
		int vpx = getViewPortPositionX();
		if(gameCode==Canvas.LEFT)
		{
			if(container.width>viewPortWidth && vpx>0)
				scrollHorizontally(-pixels);
			else scrollVertically(-pixels);				
		}
		else if(gameCode==Canvas.RIGHT)
		{
			if(container.width>viewPortWidth && (vpx+viewPortWidth)<container.width)
				scrollHorizontally(pixels);
			else scrollVertically(pixels);
		}
		else if(gameCode==Canvas.UP)
			scrollVertically(-pixels);
		else if(gameCode==Canvas.DOWN)
			scrollVertically(pixels);
		
		FireScreen screen = FireScreen.getScreen();
		Component lastSelected = screen.getSelectedComponent();
		if(lastSelected!=null && lastSelected.selected==false)  
			lastSelected.setSelected(true);// set the component as selected again.
	}
	
	/**
	 * Finds a selectable component inside the container that is visible throuht the viewport. 
	 * It will return either the first component (top most) or the last (bottom most) inside the 
	 * viewport, depending on the parameter.
	 * @param startFromTop
	 * @return
	 */
	private int getFirstFocusableComponentInsideViewport(boolean startFromTop)
	{
		int step,index;
		if(startFromTop)
		{
			index=0;
			step=+1;
		}
		else
		{
			index = container.focusableComponents.size()-1;
			step=-1;
		}
		
		int vpx = getViewPortPositionX();
		int vpy = getViewPortPositionY();
		int vpmx = vpx+viewPortWidth;
		int vpmy = vpy + viewPortHeight;
		
		int coords[];
		for(;index>-1 && index<container.focusableComponents.size();index+=step)
		{
			Component cmp = (Component)container.focusableComponents.elementAt(index);
			coords = getCoordsOfComponentInContainer(cmp,container);
			coords[0] += cmp.width/2; // translate coords to center of component
			coords[1] += cmp.height/2;
			
			if((coords[0]>= vpx && coords[0]<vpmx) && (coords[1]>= vpy && coords[1]<vpmy))
			{
				return index;
			}
		}
		// no selectable component inside viewport.
		return -1;
	}
	
	
	protected void keyRepeated(int keyCode)
	{
		keyReleased(keyCode);
	}
	
	public Vector generateListOfFocusableComponents(boolean recursive)
	{
		if(container!=null) return container.generateListOfFocusableComponents(recursive);
		else return new Vector();
	}
	

	public int[] getMinSize()
	{
		if (parent == null)
		{
			FireScreen screen = FireScreen.getScreen();
			return new int[]{screen.getWidth(), screen.getHeight()};
		}
		return super.getMinSize();
	}

	public int getScrollBarPolicy()
	{
		return scrollBarPolicy;
	}

	/**
	 * A panel can be set to remove itself from the FireScreen when it receives a pointer event that is outside it.
	 * The firescreen will only send pointer and key events to the top most (relatively to ZINDEX) focusable component or container.
	 * If a for example a popup window (a Panel) is open which is smaller than the screen size, 
	 * then it is common behaivior to close the popup when the user taps outside the window.<br/>
	 * 
	 * This is the default behaivior of the Panel. 
	 * 
	 * @return true if this Panel is set to close when receiving pointer events outside its dimensions (default is true)
	 */
	public boolean isCloseOnOutofBoundsPointerEvents()
	{
		return closeOnOutofBoundsPointerEvents;
	}

	/**
	 * @see #isCloseOnOutofBoundsPointerEvents()
	 * @param closeOnOutofBoundsPointerEvents
	 */
	public void setCloseOnOutofBoundsPointerEvents(boolean closeOnOutofBoundsPointerEvents)
	{
		this.closeOnOutofBoundsPointerEvents = closeOnOutofBoundsPointerEvents;
	}

	/**
	 * If true, the user can also scroll by draging the container inside the panel. Default is false.
	 * @return true is dragscroll is enabled (default is false)
	 */
	public boolean isDragScroll()
	{
		return dragScroll;
	}

	/**
	 * @see #setDragScroll(boolean)
	 * @param dragScroll
	 */
	public void setDragScroll(boolean dragScroll)
	{
		this.dragScroll = dragScroll;
	}

	/**
	 * If showBackground is enabled the Panel will draw a theme specific background behind the Container. 
	 * The Container or parts of it must be ofcource transparent for the background to be visible. 
	 * 
	 * @return true if this Panel is set to show a background. (default is false)
	 */
	public boolean isShowBackground()
	{
		return showBackground;
	}

	public void setShowBackground(boolean showBackground)
	{
		this.showBackground = showBackground;
	}

	/**
	 * If this flag is set then the Panel will display decorations.
	 * @return true is the decorations are enabled. (default is true)
	 */
	public boolean isShowDecorations()
	{
		return showDecorations;
	}

	/**
	 * @see #isShowDecorations()
	 * @param showDecorations
	 */
	public void setShowDecorations(boolean showDecorations)
	{
		this.showDecorations = showDecorations;
	}

	public Image getBackgroundTexture()
	{
		return backgroundTexture;
	}

	public void setBackgroundTexture(Image backgroundTexture)
	{
		this.backgroundTexture = backgroundTexture;
	}
}
