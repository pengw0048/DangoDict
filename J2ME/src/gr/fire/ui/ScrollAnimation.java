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
package gr.fire.ui;

import gr.fire.core.Animation;
import gr.fire.core.Panel;

import javax.microedition.lcdui.Graphics;

/**
 * @author padeler
 *
 */
public class ScrollAnimation extends Animation
{
	public static final long MILISECONDS_PER_FRAME=40;
	
	public static final int SCROLL_FRAMES = 3; // number of frames in this animation.
	
	private int frameCount=0;
	private int stepHChange=0,stepVChange=0;
	private long lastFrame;
	private int endVpX,endVpY;
	
	public void paint(Graphics g)
	{
		parent.paint(g);
	}
	
	public boolean isRunning()
	{
		return (frameCount<SCROLL_FRAMES);
	}

	/**
	 * Setup this scrollAnimation object. 
	 * @param Owner is the Panel which will scroll
	 * @param Trigger is ignored.
	 * @param properties is an Integer with the animation direction. Animation directions are FireScreen.LEFT, FireScreen.RIGHT, FireScreen.UP, FireScreen.DOWN. 
	 */
	public ScrollAnimation(Panel destinationPanel,int startVpX,int startVpY,int endVpX,int endVpY)
	{
		super(destinationPanel);
		this.endVpX = endVpX;
		this.endVpY = endVpY;
		destinationPanel.setViewPortPosition(startVpX,startVpY);
		
		int hPixels = endVpX - startVpX;
		int vPixels = endVpY - startVpY;
		
		stepHChange = hPixels/SCROLL_FRAMES;
		stepVChange = vPixels/SCROLL_FRAMES;

		width = destinationPanel.getWidth();
		height = destinationPanel.getHeight();
		setX(destinationPanel.getX());
		setY(destinationPanel.getY());
		lastFrame = System.currentTimeMillis();
	}

	/* (non-Javadoc)
	 * @see gr.fire.core.Animation#step()
	 */
	public boolean step()
	{
		long now = System.currentTimeMillis();
		
		if(now-lastFrame>=MILISECONDS_PER_FRAME)
		{
			Panel destinationPanel = (Panel)parent; 
			lastFrame = now;
			frameCount++;
			int vpX = destinationPanel.getViewPortPositionX();
			int vpY = destinationPanel.getViewPortPositionY();
			destinationPanel.setViewPortPosition(vpX+stepHChange,vpY+stepVChange);
			
			int nvpX = destinationPanel.getViewPortPositionX();
			int nvpY = destinationPanel.getViewPortPositionY();
			if(nvpX==vpX && nvpY==vpY)
			{ // stop
				stop();
				return false;				
			}
			return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see gr.fire.core.Animation#stop()
	 */
	public void stop()
	{
		frameCount = SCROLL_FRAMES;
		((Panel)parent).setViewPortPosition(endVpX,endVpY);
	}
}