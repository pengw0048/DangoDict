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

import java.util.Vector;

/**
 * Lays out the components of a container on a grid with given rows and columns.
 *  
 * This manager will layout the components of the container given to it in the layoutContainer method.
 * The Components will be layedout on the container from left-to-right and top-to-bottom. 
 * If the container has less components than (rows*columns) it will leave empty slots on the bottom of the container.
 * If the container has more components than (rows*columns) the manager will layout only (rows*columns) components, the remaining
 * components will be left unchanged.
 *  
 * @author padeler
 *
 */
public class GridLayout implements LayoutManager
{
	
	private int rows,columns;
	private int hgap,vgap;
	private int cellWidth=-1,cellHeight=-1;
	
	public GridLayout()
	{
		this(1,1);
	}
	
	public GridLayout(int rows,int columns)
	{
		this(rows,columns,0,0);
	}
	
	public GridLayout(int rows,int columns,int hgap,int vgap)
	{
		this(rows,columns,hgap,vgap,-1,-1);
	}
	
	public GridLayout(int rows,int columns,int hgap,int vgap,int cellWidth,int cellHeight)
	{
		if(rows<1 || columns<1 || hgap<0 || vgap<0) throw new IllegalArgumentException("Illegal arguments on GridLayout manager");
		this.rows=rows;
		this.columns=columns;
		this.hgap=hgap;
		this.vgap=vgap;
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;
	}


	/**
	 * @see gr.fire.core.LayoutManager#layoutContainer(gr.fire.core.Container)
	 */
	public void layoutContainer(Container parent)
	{
		// first find the width and the height of each slot.
		int w,h;
		
		layoutChildren(parent);// recursivelly layout children.
		
		int []d = parent.getPrefSize();
		
		if(d==null)
		{
			d = parent.getMinSize();
		}

		w=d[0];
		h=d[1];

		parent.width=w;
		parent.height=h;
	

		int slotW,slotH;
		
		if(cellWidth==-1)
			slotW = (w-hgap*(columns-1))/columns;
		else slotW = cellWidth;
		
		if(cellHeight==-1)
			slotH = (h-vgap*(rows-1))/rows;
		else slotH = cellHeight;
		
		//decide if we need to center the contained containers. 
		// If we do, then offsetX and offsetY will be used to position the elements accordingly. 
		int allWidth = (columns * slotW) + ((columns-1) * hgap); 
		int allHeight = rows * (slotH + vgap); 
		int offsetX = 0, offsetY = 0; 
		int layout = parent.getLayout();
		if ((layout & FireScreen.TOP) == FireScreen.TOP)
		{
			offsetY = 0;
		} else if ((layout & FireScreen.VCENTER) == FireScreen.VCENTER)
		{
			offsetY = parent.height / 2 - allHeight / 2;
		} else if ((layout & FireScreen.BOTTOM) == FireScreen.BOTTOM)
		{
			offsetY = parent.height - allHeight;
		}

		if ((layout & FireScreen.LEFT) == FireScreen.LEFT)
		{
			offsetX = 0;
		} else if ((layout & FireScreen.CENTER) == FireScreen.CENTER)
		{
			offsetX = (parent.width - allWidth) / 2;
		} else if ((layout & FireScreen.RIGHT) == FireScreen.RIGHT)
		{
			offsetX = parent.width - allWidth;
		}

		Vector components = parent.components;
		// now set the components positions and sizes.
		for(int r=0;r<rows;++r)
		{
			for(int c=0;c<columns;++c)
			{
				// get component
				int elementId = r*columns + c;
				if(elementId>=components.size()) break;

				Component cmp = (Component)components.elementAt(elementId);
				// layout the component.
				cmp.width=slotW;
				cmp.height=slotH;
				cmp.x = c*(slotW+hgap) + offsetX;
				cmp.y = r*(slotH+vgap) + offsetY;
			}
		}
	}
	
	
	private int[] layoutChildren(Container cnt)
	{
		// if prefSize is null we must calculate it based on the childred of cnt.
		int maxW = 0,maxH=0;
		for(int i=0;i<cnt.components.size();++i)
		{
			Component c = ((Component)cnt.components.elementAt(i));
			if(c instanceof Container) // layout this container first.
			{
				Container childCnt = ((Container)c);
				childCnt.layoutManager.layoutContainer(childCnt);
			}
			int []ps=c.getPrefSize();
			if(ps==null) ps = c.getMinSize();
			if(ps!=null)
			{
				if(ps[0]>maxW) maxW = ps[0];
				if(ps[1]>maxH) maxH = ps[1];
			}

		}
		
		int []d = cnt.getPrefSize();
		
		return d!=null?d:new int[]{maxW*columns,maxH*rows};
	}

	public int getRows()
	{
		return rows;
	}

	public int getColumns()
	{
		return columns;
	}

	public int getHgap()
	{
		return hgap;
	}

	public int getVgap()
	{
		return vgap;
	}

}
