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
import gr.fire.core.Component;
import gr.fire.core.FireScreen;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * @author padeler
 */
public class TransitionAnimation extends Animation
{
	public static final int TRANSITION_NONE = 0x00000000;
	public static final int TRANSITION_LEFT = 0x00000001;
	public static final int TRANSITION_RIGHT = 0x00000002;
	public static final int TRANSITION_CARD = 0x00000100;
	public static final int TRANSITION_SCROLL= 0x00000200;
	public static final int TRANSITION_FLIP= 0x00000400;
	
	public static final long MILISECONDS_PER_FRAME=30;
	
	public static final int FRAMES = 3; // number of frames in this animation.
	
	private int properties;
	private int frameCount=0;
	
	private Image destination=null,offScreen;
	
	private long lastFrame;
	
	private int diff;
	

	private int animationX,animationY;
	
	public void paint(Graphics g)
	{
		if(offScreen==null) return;
		if(destination==null)
		{
			try{
				destination = Image.createImage(width,height);
				Graphics gg = destination.getGraphics();
				getParent().paint(gg);
			}catch(OutOfMemoryError e){
				// out of memory. skip animations
				FireScreen.getScreen().setAnimationsEnabled(false);
				stop();
				return;
			}
		}
		int transition = properties&0x0000FF00;
		int direction = properties & 0x000000FF;	


		switch(transition)
		{
		case TRANSITION_CARD:
			{
				g.drawImage(offScreen, 0, 0, Graphics.TOP|Graphics.LEFT);
				g.drawImage(destination,animationX,animationY,Graphics.TOP|Graphics.LEFT);			
				break;
			}
		case TRANSITION_FLIP:
			break;
		case TRANSITION_SCROLL:
			{
				if(direction==TRANSITION_RIGHT)
				{
					g.drawImage(offScreen, width+animationX, 0, Graphics.TOP|Graphics.LEFT);
					g.drawImage(destination,animationX,0,Graphics.TOP|Graphics.LEFT);
				}
				else
				{
					g.drawImage(offScreen, 0- (width-animationX), 0, Graphics.TOP|Graphics.LEFT);
					g.drawImage(destination,animationX,0,Graphics.TOP|Graphics.LEFT);
				}
				break;
			}
		}
	}
	
	public boolean isRunning()
	{
		return (frameCount<FRAMES);
	}

	public TransitionAnimation(Component destinationCmp,int properties)
	{
		super(destinationCmp);
		this.properties = properties;
		

		FireScreen fs = FireScreen.getScreen();
		
		setWidth(fs.getWidth());
		setHeight(fs.getHeight());
		
		offScreen = FireScreen.getScreen().getScreenshot(false);		

		diff = width/FRAMES;

		int direction = properties & 0x000000FF;
		if(direction==TRANSITION_LEFT)
		{
			animationX = width;
		}
		else if(direction==TRANSITION_RIGHT)
		{
			animationX=-width;
		}
		animationY = 0;
		

		lastFrame = System.currentTimeMillis();
		
	}

	public boolean step()
	{
		long now = System.currentTimeMillis();
		if(now-lastFrame>=MILISECONDS_PER_FRAME)
		{
			lastFrame=now;
			frameCount++;
			int direction = properties & 0x000000FF;
			switch (direction)
			{
				case TRANSITION_LEFT:
					animationX -= diff;
					break;
				case TRANSITION_RIGHT:
					animationX += diff;
					break;
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
		frameCount = FRAMES;
	}

}
