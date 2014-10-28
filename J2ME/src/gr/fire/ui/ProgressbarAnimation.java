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
import gr.fire.core.FireScreen;
import gr.fire.core.Theme;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * @author padeler
 *
 */
public class ProgressbarAnimation extends Animation
{
	public static final long MILISECONDS_PER_FRAME=500;
	private static final int MAX_STEP=10;
	
	private String message,dots= "";
	private boolean running=true;
	private Theme theme;
	private long lastFrame,lastAutoProgress;
	private Font font;
	
	private String grad=null;
	private Image gradImg=null;
	
	private int step=0,msgLen=0;
	
	private int progressbarColor,fgcolor,fillColor,border1,border2;
	private boolean autoProgress=false;
	private long autoProgressPeriod=1000;
	
	public void paint(Graphics g)
	{
		int w = getWidth();
		int h = getHeight();
		if(grad!=null)
		{
			if(gradImg==null) gradImg = createGradImage(w,h,theme.getIntProperty("progressbar.gradient.start.color"),theme.getIntProperty("progressbar.gradient.end.color"),grad);
			g.drawImage(gradImg, 0,0, Graphics.TOP|Graphics.LEFT);			
		}
		else if(fillColor!=Theme.TRANSPARENT)
		{
			g.setColor(fillColor);
			g.fillRect(0,0,w,h);
		}
		
		if(step>0)
		{
			g.setColor(progressbarColor);
			g.fillRoundRect(-3,1,((w)*step)/MAX_STEP,h-2,6,6);
		}
		g.setFont(font);
		g.setColor(fgcolor);
		int y = h/2-font.getHeight()/2+1;
		g.drawString(message,5,y,Graphics.TOP|Graphics.LEFT);
		g.drawString(dots,msgLen+5,y,Graphics.TOP|Graphics.LEFT);
	
		if(border1!=Theme.TRANSPARENT)
		{
			g.setColor(border1);
			g.drawRect(0,0,w,h);
		}
		if(border2!=Theme.TRANSPARENT)
		{
			g.setColor(border2);
			g.drawRect(1,1,w-2,h-2);
		}
	}
	
	public Image createGradImage(int width,int height,int startColor,int endColor,String grType)
	{
		Image img = Image.createImage(width,height);
		Graphics g = img.getGraphics();
		if("horizontal".equals(grType))
		{
			int gr[] = theme.createGradient(startColor,endColor,width);
			for(int x=0;x<width;++x)
			{
				g.setColor(gr[x]);
				g.drawLine(x,0,x,height);
			}
		}
		else // vertical
		{
			int gr[] = theme.createGradient(startColor,endColor,height);
			for(int y=0;y<height;++y)
			{
				g.setColor(gr[y]);
				g.drawLine(0,y,width,y);
			}
		}
		return img;
	}
	
	public boolean isRunning()
	{
		return running;
	}

	public ProgressbarAnimation(String str)
	{
		theme = FireScreen.getTheme();
		font = theme.getFontProperty("progressbar.font");
		progressbarColor = theme.getIntProperty("progressbar.color");
		fgcolor = theme.getIntProperty("progressbar.fg.color");
		grad = theme.getStringProperty("progressbar.gradient.type");

		border1 =  theme.getIntProperty("progressbar.border1.color");
		border2 = theme.getIntProperty("progressbar.border2.color");

		fillColor = theme.getIntProperty("progressbar.bg.color");
		this.message = str;
		this.msgLen = font.stringWidth(message);
	}
	
	boolean repaintNeeded=false;

	public void progress()
	{
		step++;
		step = step%(MAX_STEP+1);	
		repaintNeeded=true;
	}
	
	public void progress(int percent)
	{
		step = (percent*MAX_STEP)/100;
		step = step%(MAX_STEP+1);	
		repaintNeeded=true;
	}


	public boolean step()
	{
		long now = System.currentTimeMillis();

		if((now-lastFrame)>autoProgressPeriod/2)
		{
			if(dots.length()<6)
				dots = dots+". ";
			else dots= "";
			repaintNeeded=true;
		}
		
		if(autoProgress && (now-lastAutoProgress)>autoProgressPeriod)
		{
			lastAutoProgress=now;
			progress();
		}
		if(now-lastFrame>=MILISECONDS_PER_FRAME && repaintNeeded && running)
		{
			lastFrame = now;
			repaintNeeded=false;
			return true;
		}
		return false;
	}

	public void stop()
	{
		running=false;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
		if(font!=null)
		{
			msgLen = font.stringWidth(message);
		}
		repaintNeeded=true;
	}

	/**
	 * @return the autoProgress
	 */
	public boolean isAutoProgress()
	{
		return autoProgress;
	}

	/**
	 * @param autoProgress the autoProgress to set
	 */
	public void setAutoProgress(boolean autoProgress)
	{
		this.autoProgress = autoProgress;
	}

	/**
	 * @return the autoProgressPeriod
	 */
	public long getAutoProgressPeriod()
	{
		return autoProgressPeriod;
	}

	/**
	 * @param autoProgressPeriod the autoProgressPeriod to set
	 */
	public void setAutoProgressPeriod(long autoProgressPeriod)
	{
		this.autoProgressPeriod = autoProgressPeriod;
	}
}