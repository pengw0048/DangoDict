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

import gr.fire.util.Log;

import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * A container is a Components that can contain other components. The components
 * inside the Container are layed out using a layout manager. The default layout
 * manager is null (AbsolutLayout). The default layout manager lays out the
 * components according to their preset component.x and component.y values and
 * their preffered sizes (if set).
 * 
 * A Container can have leftSoftKey and rightSoftKey commands assotiated with
 * it.
 * 
 * @author padeler
 * 
 */
public class Container extends Component
{
	protected Vector components;
	LayoutManager layoutManager = AbsolutLayout.defaultLayout;
	protected Vector focusableComponents=null;
	
	/**
	 * Constructs a container with absolut layout.
	 */
	public Container()
	{
		this(null);
	}

	/**
	 * Constructs a container with the given layout manager.
	 * @param manager
	 */
	public Container(LayoutManager manager)
	{
		components = new Vector();
		if(manager!=null)
			layoutManager = manager;
	}

	/** 
	 * This method should always return false for a container and its subclasses. 
	 * A container does not gain focus directly, only its contents do.
	 */
	public final boolean isFocusable()
	{
		return false;
	}

	/**
	 * Adds a given component to this Container, with the given constrains. Depending on the 
	 * LayoutManager used and the implementation of the Container, the constrains may define the 
	 * way the Component will be located and rendered inside this Container. 
	 * @param cmp
	 * @param constrains
	 */
	public void add(Component cmp, Object constrains)
	{
		if (cmp.parent != null)
		{ // remove it from its current parent.
			if (cmp.parent instanceof Container)
				((Container) cmp.parent).remove(cmp);
		}
		cmp.parent = this;
		cmp.constrains = constrains;
		components.addElement(cmp);
		valid = false; // needs validation.
	}

	/**
	 * Adds a Component in this Container.
	 * @param cmp
	 */
	public void add(Component cmp)
	{
		add(cmp, null);
	}
	
	/**
	 * Removed all components from this container.
	 * @see #remove(int)
	 */
	public void removeAll()
	{
		int count =0;
		for(int i=components.size()-1;i>=0;--i)
		{
			if(remove(i)!=null) count++;
		}
	}

	/**
	 * Removes the components from this Container. If the component is in the container and succesfully removed it returns true.
	 * @param cmp 
	 * @return true if the component is removed.
	 */
	public boolean remove(Component cmp)
	{
		int idx = getComponentIndex(cmp);
		if(idx>-1)
		{
			remove(idx);
			return true;
		}
		return false;
	}
	
	/**
	 * Removed the component at the given index from the container. 
	 * @param index
	 * @return Returns the component that was removed
	 */
	public Component remove(int index)
	{
		Component cmp = (Component)components.elementAt(index);
		if(cmp!=null)
		{
			cmp.parent = null;
			components.removeElementAt(index);
			valid = false;
			if(cmp.animation!=null) FireScreen.getScreen().removeAnimation(cmp.animation);
			cmp.parent=null;
		}
		return cmp;
	}

	public void paint(Graphics g)
	{
		int originalTrX = g.getTranslateX();
		int originalTrY = g.getTranslateY();
		int originalClipX = g.getClipX();
		int originalClipY = g.getClipY();
		int originalClipWidth = g.getClipWidth();
		int originalClipHeight = g.getClipHeight();

		// paint background in the repainting area.
		if(backgroundColor!=Theme.TRANSPARENT)
		{
			g.setColor(backgroundColor);
			g.fillRect(originalClipX,originalClipY,originalClipWidth,originalClipHeight);
		}

		for (int i = 0; i < components.size(); ++i)
		{
			Component cmp = (Component) components.elementAt(i);

			if (cmp.valid == false)
			{ // one of my components needs validation. This means that its size may have changed.
				valid = false;
				if(parent!=null) parent.repaint();
				else repaint();
				return;
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
		if(border)
		{
			g.setColor(FireScreen.getTheme().getIntProperty("border.color"));
			g.drawRect(0,0,width-1,height-1);
		}
	}

	/**
	 * Sets the layout manager of this Container. 
	 * @param manager
	 */
	public void setLayoutManager(LayoutManager manager)
	{
		if (manager == null)
			layoutManager = AbsolutLayout.defaultLayout;
		else
			layoutManager = manager;
		valid = false;
	}

	public void validate()
	{
		focusableComponents=null;
		
		for (int i = 0; i < components.size(); ++i)
		{
			Component c = ((Component) components.elementAt(i));
			c.validate();
			if(c.valid==false) {Log.logWarn("Failed to validate component "+c.getClass().getName());}
		}
		
		layoutManager.layoutContainer(this);
		valid = true;
	}

	protected void pointerReleased(int x, int y)
	{
		for (int i = components.size()-1; i >=0 ; --i)
		{
			Component cmp = (Component) components.elementAt(i);
			int cx = x - cmp.x;
			int cy = y - cmp.y;
			if (cmp.contains(cx, cy))
			{
				if (cmp.isFocusable() || cmp instanceof Container)
				{ // only focusable components receive events
					FireScreen.getScreen().setSelectedComponent(cmp);						
					cmp.pointerReleased(cx, cy);
				}
				break;
			}
		}
		super.pointerReleased(x, y);
	}
	
	protected void pointerDragged(int x, int y)
	{
		for (int i = components.size()-1; i >=0 ; --i)
		{
			Component cmp = (Component) components.elementAt(i);
			int cx = x - cmp.x;
			int cy = y - cmp.y;
			if (cmp.contains(cx, cy))
			{
				if (cmp.isFocusable() || cmp instanceof Container)
				{ // only focusable components receive events
					cmp.pointerDragged(cx, cy);
				}
				break;
			}
		}
		super.pointerDragged(x, y);
	}
	
	protected void pointerPressed(int x, int y)
	{
		for (int i = components.size()-1; i >=0 ; --i)
		{
			Component cmp = (Component) components.elementAt(i);
			int cx = x - cmp.x;
			int cy = y - cmp.y;
			if (cmp.contains(cx, cy))
			{
				if (cmp.isFocusable() || cmp instanceof Container)
				{ // only focusable components receive events
					cmp.pointerPressed(cx, cy);
				}
				break;
			}
		}
		super.pointerPressed(x, y);
	}
	
	protected static int[] getCoordsOfComponentInContainer(Component cmp,Container container)
	{
		Component tmp = cmp;
		int realX=0,realY=0;
		while(tmp!=null && tmp!=container)
		{
			realX += tmp.x;
			realY += tmp.y;
			tmp = tmp.parent;
		}
		return new int[]{realX,realY};
	}
	
	protected void keyRepeated(int keyCode)
	{
		keyReleased(keyCode);
	}
	
	protected void keyReleased(int keyCode)
	{
		FireScreen screen = FireScreen.getScreen();
		int gameCode = screen.getGameAction(keyCode);
		
		if (gameCode == Canvas.UP || gameCode == Canvas.DOWN || gameCode == Canvas.LEFT || gameCode == Canvas.RIGHT)
		{
			// first find the next selectable component.
			if(focusableComponents==null)
				focusableComponents = generateListOfFocusableComponents(true);

			int step=0;
			int index=-1;
			
			if(focusableComponents.size()>0)
			{
				if((layoutManager instanceof GridLayout)==false)
				{
					if (gameCode == Canvas.UP || gameCode==Canvas.LEFT) 
					{
						step = -1; // previous component
						index=focusableComponents.size()-1;
					}
					else
					{
						step = +1; // next component
						index=0;
					}
				}
				else
				{
					int stepDiff = ((GridLayout)layoutManager).getColumns();					
	
					if (gameCode == Canvas.UP)
					{
						step = -stepDiff;
						index = focusableComponents.size() - 1;
					}
					else if (gameCode == Canvas.DOWN)
					{
						step = stepDiff;
						index = 0;
					}
					else if (gameCode == Canvas.LEFT)
					{
						step = -1; // previous component
						index = focusableComponents.size() - 1;
					}
					else // gameCode == Canvas.RIGHT
					{
						step = +1; // next component
						index = 0;
					}
				}
				Component lastSelected = screen.getSelectedComponent();
				if(lastSelected!=null)
				{
					int lastPos = focusableComponents.indexOf(lastSelected);
					
					if(lastPos>-1)
					{
						index = lastPos+step;
						//When there are not enought components to move vertically, the panel must select the first or the last
						if(lastPos>0 && lastPos<focusableComponents.size()-1) // not the first or last component
						{
							if(index<0) index=0;
							else if(index>focusableComponents.size()-1) index=focusableComponents.size()-1;
						}
					}
				}
				
				if(index>-1 && index<focusableComponents.size())
				{
					Component next = (Component)focusableComponents.elementAt(index);
					screen.setSelectedComponent(next);	
					next.keyReleased(keyCode);
				}
				else  
				{
					if(parent!= null && parent instanceof Panel)
					{
						// tell the parent to scroll to the correct direction.
						Panel parentPanel = (Panel)parent;
						switch(gameCode)
						{
						case Canvas.UP:
							parentPanel.scrollVertically(-parentPanel.normalVScrollLength);
							break;
						case Canvas.DOWN:
							parentPanel.scrollVertically(parentPanel.normalVScrollLength);
							break;
						case Canvas.LEFT:
							if((parentPanel.getScrollBarPolicy()&Panel.HORIZONTAL_SCROLLBAR)==Panel.HORIZONTAL_SCROLLBAR)
								parentPanel.scrollHorizontally(-parentPanel.normalHScrollLength);
							else 
								parentPanel.scrollVertically(-parentPanel.fastVScrollLength);
							break;
						case Canvas.RIGHT:
							if((parentPanel.getScrollBarPolicy()&Panel.HORIZONTAL_SCROLLBAR)==Panel.HORIZONTAL_SCROLLBAR)
								parentPanel.scrollHorizontally(parentPanel.normalHScrollLength);
							else 
								parentPanel.scrollVertically(parentPanel.fastVScrollLength);
							break;
						}
						return;
					}
					if(parent==null && focusableComponents.size()>0) // only send the event if this container is top level, and there are focusable components
					{
						screen.setSelectedComponent(null);
						screen.keyReleased(keyCode);
						return;						
					}
				}
			}
		}
		super.keyReleased(keyCode);
	}
	
	/**
	 * Creates a vector with all the focusable children of this container. If recursive is true, it will descent into its children 
	 * Containers recursively and add their focusable components to the list. 
	 * 
	 * @param recursive
	 * @return
	 */
	public Vector generateListOfFocusableComponents(boolean recursive)
	{
		Vector res = new Vector();
		
		for(int i=0;i<components.size();++i)
		{
			Component cmp = (Component)components.elementAt(i);
			if(cmp.isFocusable() || cmp instanceof Panel)
			{
				res.addElement(cmp);
			}
			else if(recursive && cmp instanceof Container )
			{
				Container container = (Container)cmp;
				Vector v = container.generateListOfFocusableComponents(recursive);
				for(int j=0;j<v.size();++j)
				{
					res.addElement(v.elementAt(j));
				}
			}
		}
		return res;
	}
	
	/**
	 * Returns the number of components inside this container.
	 * @return
	 */
	public int countComponents()
	{
		return components.size();
	}
	
	/**
	 * Returns the Component at the given index.
	 * @param i
	 * @return
	 */
	public Component getComponent(int i)
	{
		return (Component)components.elementAt(i);
	}
	
	/**
	 * Returns the index of the given Component or -1 if the component is not in the Container.
	 * @param cmp
	 * @return
	 */
	public int getComponentIndex(Component cmp)
	{
		return components.indexOf(cmp);
	}
	
	public int[] getMinSize()
	{
		if(parent==null) 
		{
			FireScreen screen = FireScreen.getScreen();
			return new int[]{screen.getWidth(),screen.getHeight()};
		}
		else if(parent instanceof Panel)
		{
			Panel p = (Panel)parent;
			return new int[]{p.getViewPortWidth(),p.getViewPortHeight()};
		}
		return super.getMinSize();
	}
	
	
	public String toString()
	{
		return super.toString() + " ("+components.size()+")";
	}
}