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
package gr.fire.ui;

import gr.fire.core.Animation;
import gr.fire.core.FireScreen;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * 
 * Brings a window flying on the screen on top of another. 
 * This animation takes over the whole FireScreen size.
 * 
 * @author padeler
 *
 */
public class AlertAnimation extends Animation
{
	public static final long MILISECONDS_PER_FRAME=30;
	private static final int MAX_STEP=8;
	
	private boolean running=true;
	private long lastFrame;
	private int xstep=0,ystep=0,step=0;
	private int endX,endY;
	private int cX,cY; // current target X,Y 
	
	private Image background=null,targetImg=null;
	private Alert target;
	
	private int[]dvy = new int[]{1,3,5,1,0,0,-1,-1};
	
	public AlertAnimation(Alert target)
	{
		this.target=target;
		
		FireScreen fireScreen = FireScreen.getScreen();
		width = fireScreen.getWidth();
		height = fireScreen.getHeight();
		setPrefSize(width,height);

		// these start and end values might change so i like to keep them here even though they are not needed now.
		int startX = target.getX();
		int startY = height;
		
		this.endX = target.getX();
		this.endY = target.getY();
		
		int totalX = endX-startX;
		int totalY = endY-startY;
		xstep = totalX/MAX_STEP;
		ystep = totalY/(MAX_STEP);
		
		cX = startX;
		cY = startY;
		
		
		Image screen = FireScreen.getScreen().getScreenshot(false);
		if(target.isShadeBackground())
		{
			int rgb[] = new int[screen.getWidth()*screen.getHeight()];
			
			screen.getRGB(rgb, 0, width, 0, 0, width, height);
			screen=null;
			
			for(int i=0;i<rgb.length;++i)
			{
				//Y = 0.3*R + 0.59*G + 0.11*B
				int r = (rgb[i]>>16)&0x000000FF;
				r = (r*30)/100;
				int g = (rgb[i]>>8)&0x000000FF;
				g = (g*59)/100;
				int b = (rgb[i])&0x000000FF;
				b = (b*11)/100;
				int y = (r+g+b)&0x000000FF;
				y = (y<<16)|(y<<8)|y;
				rgb[i] = y;
			}
			screen = Image.createRGBImage(rgb, width, height, false);
		}
		background=screen;
	}

	public boolean isRunning()
	{
		return running;
	}

	/* (non-Javadoc)
	 * @see gr.fire.core.Animation#paint(javax.microedition.lcdui.Graphics)
	 */
	public void paint(Graphics g)
	{
		if(targetImg==null)
		{
			if(!target.isValid()) return;
			targetImg = getAlertImg();
		
		}
		g.drawImage(background,0,0,Graphics.TOP|Graphics.LEFT);
		g.drawImage(targetImg,cX,cY,Graphics.TOP|Graphics.LEFT);
	}
	
	private Image getAlertImg()
	{
		int tw = target.getWidth();
		int th = target.getHeight();
		Image img = Image.createImage(tw,th);
		Graphics g = img.getGraphics();
		if(target.isShadeBackground())
		{
			g.setColor(0x838383);
			g.fillRect(0,0,tw,th);
		}
		target.paint(img.getGraphics());		
		return img;
	}

	public boolean step()
	{
		long now = System.currentTimeMillis();
		
		if(step>=MAX_STEP)
		{
			stop();
		}
		else if(now-lastFrame>=MILISECONDS_PER_FRAME)
		{ 
			lastFrame = now;
			cX += xstep;
			cY += (dvy[step]*ystep);
			step++;
		}
		return running;
	}

	public void stop()
	{
		running=false;
		FireScreen fs = FireScreen.getScreen();
		if(target.isShadeBackground())
		{
			ImageComponent bg = new ImageComponent(background, "");			
			fs.addComponent(bg, target.getShadedBackgroundIndex());
		}
		fs.addComponent(target,target.getAlertIndex());
		fs.setSelectedComponent(target.getSelectedButton());
	}

}
