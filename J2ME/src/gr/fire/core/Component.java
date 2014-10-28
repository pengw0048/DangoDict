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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * @author padeler
 *
 */
public class Component
{
	
	protected Component parent;
	private String id;
	
	
	/** Constrains for this object, used by the layoutManager */
	Object constrains=null; 
	
	/**
	 * Location of the component inside its parent container. 
	 * These coordinates are refer to the top left corner of this component, relative to the 
	 * top left corder (0,0) of the parent container. 
	 */
	int x,y;
	
	
	
	/** 
	 * The actual dimensions of this component 
	 */
	protected int width,height;
	
	/**
	 * The font used by this component when displaying text.
	 */
	protected Font font;
	
	/**
	 * The layout of this Component's contents i.e (FireScreen.TOP|FireScreen.LEFT)
	 */
	protected int layout;
	protected int foregroundColor;
	protected int backgroundColor;

	/*
	 * Padding is the distance of this component's border from its content
	 * The Component is responsible for rendering correctly the padding.
	protected int paddingLeft;
	protected int paddingTop;
	protected int paddingRight;
	protected int paddingBottom;
	/*
	 * The internal padding between the component's elements 
	protected int paddingVertical;
	protected int paddingHorizontal;
	
	/*
	 * The margin is the distance of the component from its neighbors. 
	 * The parent container is responsible for rendering the margins correctly.
	protected int marginLeft;
	protected int marginTop;
	protected int marginRight;
	protected int marginBottom;
	 */
	
	protected boolean border=false;
	
	
	
	/** If the Component is not valid, its characteristics (with,height,etc) need validation */
	protected boolean valid;
	/** If it can be focused (traversed) by the user */
	private boolean focusable;
	
	boolean visible=true;

	
	/** 
	 * If this component is in animation mode then the animation is held in this field.
	 */
	Animation animation=null;
	
	/** If the component is currently selected, i.e. the curson is on it */
	boolean selected; 
	
	/**
	 * Dimensions of the component that control its layout inside a Container. 
	 */
	private int prefWidth=-1,prefHeight=-1;
	
	protected CommandListener commandListener;
	protected KeyListener keyListener;
	protected PointerListener pointerListener;
	protected Command command;
	protected Command leftSoftKeyCommand;
	protected Command rightSoftKeyCommand;
	
	/**
	 * Component's default constructor.
	 */
	public Component() 
	{
		Theme t = FireScreen.getTheme();
		backgroundColor = t.getIntProperty("bg.color");
		foregroundColor = t.getIntProperty("fg.color");
	}
	
	/**
	 * Paint is called by the container of the component to allow it to draw itself on Graphics g
	 * The drawable area on g is (0,0,width,height).
	 * 
	 * @param g the area on witch the component will draw it self.
	 */
	public void paint(Graphics g)
	{
	}
	
	/**
	 * Sets this component on/off selected mode. 
	 * 
	 * When a component is selected it should render itself 
	 * approprietly in order to be easily identified by the user
	 *  
	 * @param v
	 */
	public void setSelected(boolean v){selected = v;}
	
	/**
	 * @see #setSelected(boolean)
	 * @return
	 */
	public boolean isSelected(){return selected;}
	
	/**
	 * A validate event requests from the component to recalculate its internal properties such as width/height etc.
	 */
	public void validate(){valid=true;}

	/**
	 * @see #validate()
	 * @return
	 */
	public boolean isValid(){
		return valid;
	}
	
	/**
	 * Returns the height of this component
	 * @return
	 */
	public int getHeight()
	{
		return height;
	}

	/**
	 * Sets the height of this component
	 * @param height
	 */
	public void setHeight(int height)
	{
		if(height<0) throw new IllegalArgumentException("Height cannot be negative.");
		this.height = height;
	}

	/**
	 * Returns the width of this component 
	 * @return
	 */
	public int getWidth()
	{
		return width;
	}

	/**
	 * Sets the width of this component
	 * @param width
	 */
	public void setWidth(int width)
	{
		if(width<0) throw new IllegalArgumentException("Width cannot be negative.");
		this.width = width;
	}
	
	/**
	 * set a command to this component. 
	 * @param c
	 */
	public void setCommand(Command c)
	{
		command=c;
		focusable= (focusable||commandListener!=null||keyListener!=null||pointerListener!=null||command!=null);
	}
	
	/**
	 * Events that have a command assosiated with them 
	 * {@link #setCommand(Command)}, {@link #setLeftSoftKeyCommand(Command)}, {@link #setRightSoftKeyCommand(Command)}
	 * will cause this component send the event to the given CommandListener.
	 * 
	 * @see CommandListener
	 * @param listener
	 */
	public void setCommandListener(CommandListener listener)
	{

		this.commandListener=listener;
		focusable= (focusable||commandListener!=null||keyListener!=null||pointerListener!=null||command!=null);
	}
	
	/**
	 * Key events received by this components will cause this component to send
	 * the appropriate key event to the given listener.
	 * 
	 * @see KeyListener 
	 * @param listener
	 */
	public void setKeyListener(KeyListener listener)
	{
		this.keyListener=listener;
		focusable= (focusable||commandListener!=null||keyListener!=null||pointerListener!=null||command!=null);
	}
	
	/**
	 * Pointer events received by this components will cause this component to send
	 * the appropriate pointer event to the given listener.
	 * 
	 * @see PointerListener
	 * @param listener
	 */
	public void setPointerListener(PointerListener listener)
	{
		this.pointerListener=listener;
		focusable= (focusable||commandListener!=null||keyListener!=null||pointerListener!=null||command!=null);
	}
	
	/**
	 * If this component can receive and handle Key events or pointer events then it is Focusable and should return true
	 * @see KeyListener
	 * @see PointerListener  
	 * @see CommandListener
	 * @return
	 */
	public boolean isFocusable()
	{
		return focusable;
	}
	
	
	/**
	 * Checks if the point (x,y) is inside this Component. The point must be on the coordinate system of this Component.
	 * That means that the top left corner of the component is (0,0).
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean contains(int x,int y)
	{
		return (x>=0 && y>=0 && x<width && y<height);
	}
	
    /**
	 * Determines whether or not this <code>Component</code> and the specified
	 * rectangular area intersect. Two rectangles intersect if their
	 * intersection is nonempty.
	 * 
	 * @return <code>true</code> if the specified area and this
	 *         <code>Component</code> intersect; <code>false</code>
	 *         otherwise.
	 */
	public boolean intersects(int rx, int ry, int rw, int rh)
	{
		int tw = width;
		int th = height;
		if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0)
		{
			return false;
		}
		int tx = x;
		int ty = y;
		rw += rx;
		rh += ry;
		tw += tx;
		th += ty;
		// overflow || intersect
		return ((rw < rx || rw > tx) && (rh < ry || rh > ty)
				&& (tw < tx || tw > rx) && (th < ty || th > ry));
	}
	
	/**
	 * Causes this component only to be repainted on the screen.
	 */
	public void repaint()
	{
		repaint(0,0, width, height);
	}

	/**
	 * Get the X position of this component inside its parent. 
	 * @return
	 */
	public int getX()
	{
		return x;
	}

	/**
	 * Set the X position of this component inside its parent.
	 * @param x
	 */
	public void setX(int x)
	{
		this.x = x;
	}

	/**
	 * Get the Y position of this component inside its parent.
	 * @return
	 */
	public int getY()
	{
		return y;
	}

	/**
	 * Set the Y position of this component inside its parent.
	 * @param y
	 */
	public void setY(int y)
	{
		this.y = y;
	}
	
	void repaint(int cx,int cy,int cwidth,int cheight)
	{
		
		if(parent!=null)
		{
//			System.out.println("Repaint for "+cx+","+cy);
			parent.repaint(x+cx, y+cy, cwidth, cheight);
		}
		else
		{ // top level component
			FireScreen.getScreen().repaintScreen(x+cx,y+cy,cwidth,cheight);
		}
	}

	/**
	 * Returns a int[2] array with the width,height which are preffered by this component.
	 * If set it is used by the LayoutManager to correctly layout this Component inside its Container
	 * @return
	 */
	public int[] getPrefSize()
	{
		if(prefWidth==-1 || prefHeight==-1) return null;
		return new int[]{prefWidth,prefHeight};
	}

	/**
	 * @see #getPrefSize()
	 * @param width
	 * @param height
	 */
	public void setPrefSize(int width,int height)
	{
		if (width < 0 || height < 0)
			throw new IllegalArgumentException("Dimensions can not be negative: "+width+"/"+height);
		
		prefWidth = width;
		prefHeight = height;
		valid=false;
	}
	
	
	protected void pointerDragged(int x, int y)
	{
		if(pointerListener!=null) pointerListener.pointerDragged(x, y,this);
	}

	protected void pointerPressed(int x, int y)
	{
		if(pointerListener!=null) pointerListener.pointerPressed(x, y,this);
	}
	
	protected void pointerReleased(int x, int y)
	{
		if(pointerListener!=null) pointerListener.pointerReleased(x, y,this);
	}

	protected void keyPressed(int keyCode)
	{
		if(keyListener!=null) keyListener.keyPressed(keyCode,this);
	}
	
	protected void keyReleased(int keyCode)
	{
		if(keyListener!=null) keyListener.keyReleased(keyCode,this);
	}
	
	protected void keyRepeated(int keyCode)
	{
		if(keyListener!=null) keyListener.keyRepeated(keyCode,this);
	}	

	public Command getLeftSoftKeyCommand()
	{
		return leftSoftKeyCommand;
	}

	public void setLeftSoftKeyCommand(Command leftSoftKeyCommand)
	{
		this.leftSoftKeyCommand = leftSoftKeyCommand;
		FireScreen.getScreen().componentSoftKeyUpdate(this);
	}

	public Command getRightSoftKeyCommand()
	{
		return rightSoftKeyCommand;
	}

	public void setRightSoftKeyCommand(Command rightSoftKeyCommand)
	{
		this.rightSoftKeyCommand = rightSoftKeyCommand;
		FireScreen.getScreen().componentSoftKeyUpdate(this);
	}
	
	/**
	 * If this component is inside another (i.e. a container) then its parent is that container
	 * If this component is directly added to the FireScreen (i.e. using the FireScreen.addComponent() method) the parent is null
	 * @see FireScreen#addComponent(Component, int) 
	 * @return
	 */
	public Component getParent()
	{
		return parent;
	}

	public int getForegroundColor()
	{
		return foregroundColor;
	}

	public void setForegroundColor(int foregroundColor)
	{
		this.foregroundColor = foregroundColor;
	}

	public int getBackgroundColor()
	{
		return backgroundColor;
	}

	public void setBackgroundColor(int backgroundColor)
	{
		this.backgroundColor = backgroundColor;
	}
	
	/**
	 * The minSize is used to calculate the layout of the components inside their container by the LayoutManager.
	 * The minSize is only used when the prefSize is not set. 
	 * If the prefSize is set it will be used even if the minSize is bigger than prefSize.
	 * 
	 * @return the minSize dimensions of this Component.
	 */
	public int[]  getMinSize()
	{
		return new int[]{0,0};
	}

	public Font getFont()
	{
		return font;
	}

	public void setFont(Font font)
	{
		this.font = font;
	}

	/**
	 * Returns the layout set to this component. Using the layout the component can 
	 * decide how to present its content to the screen. 
	 * Possible layouts are combinations of the following 
	 * FireScreen.CENTER,FireScreen.LEFT, FireScreen.RIGHT, FireScreen.TOP, FireScreen.BOTTOM , FireScreen.VCENTER
	 * @see FireScreen#CENTER 
	 * @return
	 */
	public int getLayout()
	{
		return layout;
	}

	/** 
	 * @see #getLayout()
	 * @param layout
	 */
	public void setLayout(int layout)
	{
		this.layout = layout;
	}
	

	/**
	 * Returns the width of the usefull content of this component (i.e. for a TextComponent the width of the text without borders,etc)
	 * It is used by the LayoutManager and the Browser to determine the best way to display this component
	 * 
	 * 
	 * @return
	 */
	public int getContentWidth(){
		return 0;
	}
	/**
	 * Returns the height of the usefull content of this component (i.e. for a TextComponent the height of the text without borders,etc)
	 * It is used by the LayoutManager and the Browser to determine the best way to display this component
	 * 
	 * @return
	 */
	public int getContentHeight()
	{
		return 0;
	}

	/**
	 * Sets this component to be able to receive key and pointer events
	 * @param focusable
	 */
	public void setFocusable(boolean focusable)
	{
		this.focusable = focusable;
	}
	
	
	/**
	 * @see #getId()
	 * @param id
	 */
	public void setId(String id)
	{
		this.id = id;
	}
	
	/**
	 * Every Fire Component can have an Id string assosiated with it. 
	 *  
	 * @return
	 */
	public String getId()
	{
		return id;
	}
	
	public String toString()
	{
		return super.toString() + ((id!=null)?" ["+id+"]":"");
	}
	
	/**
	 * Utility method to easily retrieve 
	 * the vertical layout information of this component 
	 * 
	 * @see #setLayout(int) 
	 * @return
	 */
	public int getValign()
	{
		int valign = FireScreen.TOP;
		
		if ((layout&FireScreen.VCENTER)==FireScreen.VCENTER)
		{
			valign = FireScreen.VCENTER;
		}
		else if ((layout&FireScreen.TOP) == FireScreen.TOP)
		{
			valign = FireScreen.TOP;
		}
		else if ((layout&FireScreen.BOTTOM) == FireScreen.BOTTOM)
		{
			valign = FireScreen.BOTTOM;
		}
		
		return valign;		
	}

	/**
	 * Utility method to easily retrieve 
	 * the horizontal layout information of this component 
	 * 
	 * @see #setLayout(int) 
	 * @return
	 */
	public int getHalign()
	{
		int halign = FireScreen.LEFT;
		
		if ((layout&FireScreen.CENTER)==FireScreen.CENTER) // hcenter
		{
			halign = FireScreen.CENTER;
		}
		else if ((layout&FireScreen.LEFT)==FireScreen.LEFT)
		{
			halign = FireScreen.LEFT;
		}
		else if ((layout&FireScreen.RIGHT)==FireScreen.RIGHT)
		{
			halign = FireScreen.RIGHT;
		}
		return halign;
	}

	/**
	 * A component can have a border. This method returns true if the border flag is set.<br/>
	 * Note: It is up to the component implementation to support and display borders. 
	 * @return
	 */
	public boolean isBorder()
	{
		return border;
	}

	/**
	 * @see #isBorder()
	 * @param border
	 */
	public void setBorder(boolean border)
	{
		this.border = border;
	}

	/**
	 * If a component is visible (default) then it will be drawn by the firescreen or its parent component
	 * @return
	 */
	public boolean isVisible()
	{
		return visible;
	}

	/**
	 * @see #isVisible()
	 * @param visible
	 */
	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}
	

	/**
	 * Utility method to easily translate the component inside its parent by (dx,dy)
	 * @see #setPosition(int, int)
	 * @param dx
	 * @param dy
	 */
	public void move(int dx, int dy)
	{
		repaint();
		this.x+=dx;
		this.y+=dy;
	}
	
	/**
	 * Sets the position of this component relative to its parent top left corner.
	 * If the component does not have a parent (its a top level component of the FireScreen) the
	 * position is relative to the top,left of the FireScreen
	 * 
	 * @param x
	 * @param y
	 */
	public void setPosition(int x,int y)
	{
		repaint();
		this.x=x;
		this.y=y;
	}

	/**
	 * This method returns the animation (if any) associated with this component.
	 * A component can have at most one animation. When a component has an animation
	 * it is considered its parent (this==animation.parent && this.animation==animation) 
	 * @return
	 */
	public Animation getAnimation()
	{
		return animation;
	}

	/**
	 * @see #setAnimation(Animation)
	 * @param animation
	 */
	public void setAnimation(Animation animation)
	{
		if(animation.parent!=null)
		{
			animation.parent.animation=null;
		}
		animation.parent=this;
		this.animation = animation;
	}	
	

	/**
	 * @return the command
	 */
	public Command getCommand()
	{
		return command;
	}
}