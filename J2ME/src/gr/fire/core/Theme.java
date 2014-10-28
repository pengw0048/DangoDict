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

import gr.fire.util.Log;

import java.util.Hashtable;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;

/**
 * A class that extends Theme can control the look and feel of Fire Components.
 *  
 * @author padeler
 */
public class Theme
{
	/**
	 * Value for completly TRANSPARENT "color" value 
	 */
	public static final int TRANSPARENT = 0xFF000000;
		
	/**
	 * The size of a scrollbar in pixels. The scroll bar is part of the decoration (right or bottom) and it's size 
	 * should be always smaller than the corresponding size of the border it is drawn on.
	 */ 
	public int scrollSize=4; // size (width) of the scrollbar in pixels  
	public int scrollLenght=15; // lenght of the scrollbar in pixels
	
	public int decorLeft=0,decorRight=0; // size of the decorations (pixels)
	public int decorTop=0,decorBottom=scrollSize;
	
	protected Hashtable themeProperties;
	
	public Theme()
	{
		Integer black = new Integer(0x00000000);
		Integer gray = new Integer(0x00707070);
		Integer white = new Integer(0x00FFFFFF);
		Integer blue = new Integer(0x000000FF);
		
		themeProperties=new Hashtable();
		themeProperties.put("bg.color",new Integer(TRANSPARENT));
		themeProperties.put("fg.color",black);
		themeProperties.put("bg.alt1.color",gray);
		themeProperties.put("bg.alt2.color",gray);
		themeProperties.put("border.color",black);
		themeProperties.put("link.active.fg.color",white);
		themeProperties.put("link.active.bg.color",blue);
		
		themeProperties.put("navbar.bg.color",black);
		themeProperties.put("navbar.fg.color",white);
		
		Font font = Font.getDefaultFont();
		themeProperties.put("font",font);
		themeProperties.put("label.font",font);
		themeProperties.put("navbar.font",font);
		themeProperties.put("titlebar.font",font);

	}
	

	/**
	 * This method creates an array of colors that are the gradient from the given starting color to the given end color. 
	 *  
	 * @param start
	 * @param end
	 * @param length The length of the resulting gradient array
	 * @return
	 */
	public static int[] createGradient(int start, int end, int length)
	{
		if(length<=2) throw new IllegalArgumentException("Length of the gradient must be greater than two.");
		
		int []result = new int[length];
		
		
		int startAlpha = start>>> 24;
		int startRed = (start>>> 16) & 0x00FF;
		int startGreen = (start>>> 8) & 0x0000FF;
		int startBlue = start& 0x00000FF;

		int endAlpha = end>>> 24;
		int endRed = (end>>> 16) & 0x00FF;
		int endGreen = (end>>> 8) & 0x0000FF;
		int endBlue = end& 0x00000FF;
		
		// the first color is the start
		result[0] = start;
		// number of colors until the end:
		int count = length-1;
		

		int stepAlpha = ((endAlpha - startAlpha) << 8) / count;
		int stepRed = ((endRed - startRed) << 8) / count;
		int stepGreen = ((endGreen - startGreen) << 8) / count;
		int stepBlue = ((endBlue - startBlue) << 8) / count;

		startAlpha <<= 8;
		startRed <<= 8;
		startGreen <<= 8;
		startBlue <<= 8;

		for (int i = 0; i < count; i++)
		{
			startAlpha += stepAlpha;
			startRed += stepRed;
			startGreen += stepGreen;
			startBlue += stepBlue;

			result[i+1] = ((startAlpha << 16) & 0xFF000000) | ((startRed << 8) & 0x00FF0000) | (startGreen & 0x0000FF00) | (startBlue >>> 8);
		}
		return result;
	}
	
	/**
	 * Returns the logo (if any) set for this theme.
	 * @return
	 */
	public Image getLogo(){ return null;}
	public Image getBackgroundTexture(int width,int height){ return null;}
	public Image getTitlebarTexture(int width,int height){ return null;}
	public Image getNavbarTexture(int width,int height){ return null;}
	public Image getDecorLeftTexture(int width, int height){ return null;}
	public Image getDecorRightTexture(int width, int height){ return null;}
	
	public Font getFontProperty(String key) 
	{
		Font v = (Font)themeProperties.get(key);
		return (v!=null)?v:Font.getDefaultFont();
	}
	
	public String getStringProperty(String key) 
	{
		String v = (String)themeProperties.get(key);
		if(v!=null) return v.trim();
		return null;
	}
	
	public int getIntProperty(String key) 
	{
		Integer v = ((Integer)themeProperties.get(key));
		if(v==null) {Log.logWarn("Unknown theme int property requested: "+key);return 0x00000000;}
		return v.intValue();
	}
	
	public boolean getBooleanProperty(String key) {return ((Boolean)themeProperties.get(key)).booleanValue();}
	
	
	public void setFontProperty(String key,Font o) 
	{
		themeProperties.put(key,o);
	}
	
	public void setStringProperty(String key,String o) 
	{
		themeProperties.put(key,o);
	}
	
	public void setIntProperty(String key,int o) 
	{
		themeProperties.put(key,new Integer(o));
	}
	
	public void setBooleanProperty(String key,boolean o) 
	{
		themeProperties.put(key,new Boolean(o));
	}
}