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

/**
 * 
 */
package gr.fire.core;

/**
 * This layout manager mimics the behaiviour of the BoxLayout manager of j2se.
 * 
 * It allows multiple components to be laid out either vertically or horizontally. 
 * The BoxLayout manager is constructed with an axis parameter that specifies the type of layout that will be done. There are four choices:
 * X_AXIS - Components are laid out horizontally from left to right.
 * Y_AXIS - Components are laid out vertically from top to bottom.
 * 
 * 
 * BoxLayout attempts to arrange components at their preferred widths (for horizontal layout) or heights (for vertical layout). 
 * For a horizontal layout, if not all the components are the same height, BoxLayout attempts to make all the components 
 * as high as the highest component.
 * 
 * @author padeler
 *
 */
public class BoxLayout implements LayoutManager
{
	public static final int X_AXIS=0;
	public static final int Y_AXIS=1;
	
	
	private int axis;
	
	public BoxLayout(int axis)
	{
		this.axis=axis;
	}

	/**
	 * @see gr.fire.core.LayoutManager#layoutContainer(gr.fire.core.Container)
	 */
	public void layoutContainer(Container parent)
	{
		switch(axis)
		{
		case X_AXIS:
			layoutXaxis(parent);
			break;
		case Y_AXIS:
			layoutYaxis(parent);
			break;
		}
	}
	
	private void layoutXaxis(Container parent)
	{
		// first find the width and the height of each slot.
		int w,h;
		
		int []contentSize = layoutChildren(parent);// recursivelly layout children.
		
		int[] d = parent.getPrefSize();
		if(d==null)
		{
			d = parent.getMinSize();
			if(contentSize[0]>d[0]) d[0] = contentSize[0];
			if(contentSize[1]>d[1]) d[1] = contentSize[1];
		}
		
		w=d[0];
		h=d[1];
		
		parent.width=w;
		parent.height=h;
			
		int totalW=0,maxH=0;
		int splitCount = 0; // the number of components with no prefSize.
		// first get the preffered size of each component.
		for(int i =0 ;i <parent.components.size();++i)
		{
			Component cmp = (Component)parent.components.elementAt(i);
					
			d = cmp.getPrefSize();
			if(d==null) {
				d = cmp.getMinSize();
				splitCount++;// count how many components do not have set prefered dimensions. 
				// Any space left on the container will be split to these components
			}
			if(d!=null)
			{
				totalW += d[0];
				int cmpH = d[1]; 
				if(cmpH>maxH) maxH = cmpH;
			}
		}
		// now we need to adjust the components' sizes
		int adjustment = 0;
		int splitW=0;
		if(totalW>w)
		{
			adjustment = (1000 * w)/totalW;
		}
		else if(totalW<w)
		{
			if(splitCount==0) splitCount=1; // to avoid arithmetic exceptions
			splitW = (w-totalW)/splitCount;
		}
		
		if(maxH>h || maxH==0) maxH=h;
		int ypos = (h-maxH)/2;
		
		int xpointer=0;
		for(int i =0 ;i <parent.components.size();++i)
		{
			Component cmp = (Component)parent.components.elementAt(i);
			d = cmp.getPrefSize();
			if(d!=null)
			{
				int prefWidthInPixels = d[0];
				
				if(adjustment!=0)
				{
					cmp.width = (prefWidthInPixels * adjustment)/1000;
				}
				else cmp.width = prefWidthInPixels;
				
			}else
			{
				d = cmp.getMinSize();
				int tempW = splitW;
				
				if(d!=null) tempW+=d[0];
				cmp.width = tempW;
			}
			
			cmp.height=maxH;
			
			cmp.x = xpointer;
			cmp.y = ypos;
			
			xpointer += cmp.width;
		}
		
		// the last component also gets the remaining space (usually 1pixel)
		if(parent.components.size()>0)
		{
			Component cmp = (Component)parent.components.elementAt(parent.components.size()-1);
			if(cmp.x+cmp.width<w) cmp.width = w-cmp.x;
		}
	}

	private void layoutYaxis(Container parent)
	{
		// first find the width and the height of each slot.
		int w,h;
		int []contentSize = layoutChildren(parent);// recursivelly layout children.
		
		int[] d = parent.getPrefSize();
		if(d==null)
		{
			d = parent.getMinSize();
			if(contentSize[0]>d[0]) d[0] = contentSize[0];
			if(contentSize[1]>d[1]) d[1] = contentSize[1];
		}

		w=d[0];
		h=d[1];

		parent.width=w;
		parent.height=h;
	
		int totalH=0,maxW=0;
		int splitCount = 0; // the number of components with no prefSize.
		// first get the preffered size of each component.
		for(int i =0 ;i <parent.components.size();++i)
		{
			Component cmp = (Component)parent.components.elementAt(i);
			
			d = cmp.getPrefSize();
			if(d==null) {
				d = cmp.getMinSize();
				splitCount++;// count how many components do not have set prefered dimensions. 
				// Any space left on the container will be split to these components
			}
			
			if(d!=null)
			{
				totalH += d[1];
				int cmpW = d[0]; 
				if(cmpW>maxW) maxW = cmpW;
			} 
		}
		// now we need to adjust the components' sizes
		int adjustment = 0;
		int splitH=0;
		if(totalH>h)
		{
			adjustment = (1000 * h)/totalH;
		}
		else if(totalH<h)
		{
			if(splitCount==0) splitCount=1; // to avoid arithmetic exceptions
			splitH = (h-totalH)/splitCount;
		}
		if(maxW>w || maxW==0) maxW=w;
		int xpos = (w-maxW)/2;
		int ypointer=0;
		for(int i =0 ;i <parent.components.size();++i)
		{
			Component cmp = (Component)parent.components.elementAt(i);
			d = cmp.getPrefSize();
			
			if(d!=null)
			{
				int prefHeightInPixels = d[1];
				
				if(adjustment!=0)
				{
					cmp.height = (prefHeightInPixels * adjustment)/1000;
				}
				else cmp.height = prefHeightInPixels;
			}else
			{
				d = cmp.getMinSize();
				int tmpH = splitH;
				if(d!=null) tmpH +=d[1];
				
				cmp.height = tmpH;
			}
			
			cmp.width=maxW;
			
			cmp.x = xpos;
			cmp.y = ypointer;
			
			ypointer += cmp.height;
		}
		
		// the last component also gets the remaining space (usually 1pixel)
		if(parent.components.size()>0)
		{
			Component cmp = (Component)parent.components.elementAt(parent.components.size()-1);
			if(cmp.y+cmp.height<h) cmp.height = h-cmp.y;
		}
	}
	

	public int getAxis()
	{
		return axis;
	}

	
	private int[] layoutChildren(Container cnt)
	{
		// if prefSize is null we must calculate it based on the childred of cnt.
		int maxW = 0,maxH=0;
		int totalW=0,totalH=0;
		for(int i=0;i<cnt.components.size();++i)
		{
			Component c = ((Component)cnt.components.elementAt(i));			
			if (c instanceof Container)
			{
				((Container) c).layoutManager.layoutContainer((Container) c);
			}
			int []prefSize = c.getPrefSize();
			if(prefSize==null) prefSize = c.getMinSize();
			
			if(prefSize!=null)
			{
				int w = prefSize[0];
				int h = prefSize[1];
				
				if(w>maxW) maxW = w;
				if(h>maxH) maxH = h;
				totalW += w;
				totalH += h;
			}
		}
		int w= totalW;
		int h= totalH;
		if(axis==X_AXIS) h = maxH;
		else if(axis==Y_AXIS) w = maxW;
		
		return new int[]{w,h};
	}
}
