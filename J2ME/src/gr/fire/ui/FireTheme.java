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

import gr.fire.browser.util.StyleSheet;
import gr.fire.core.FireScreen;
import gr.fire.core.Theme;
import gr.fire.util.FireConnector;
import gr.fire.util.Log;
import gr.fire.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;


/**
 * The default implementation of a Theme. This implementation reads key=value pairs from a given file or map 
 * and parses font, color, etc values.
 *  
 * @author padeler
 *
 */
public class FireTheme extends Theme
{
	private Image logo=null;
	
	/**
	 * Creates an instance of this theme with the values loaded from the given file name. 
	 * 
	 * @param themeFile
	 * @throws IOException
	 */
	public FireTheme(String themeFile) throws IOException
	{
		this(StringUtil.loadProperties((new FireConnector()).openInputStream(themeFile),':',"UTF-8"));
	}
	
	/**
	 * Creates an instance of this theme with the values of the given hashtable
	 * This constructor does not copy the values of the hashtable internally but alters the one given. 
	 * If the hashtable contents change later, the behaivior is not defined. 
	 * @param props
	 * @throws IOException
	 */
	public FireTheme(Hashtable props) throws IOException
	{
		this.themeProperties = props;
		// parse the properties to convert color values to integers, align values to integers etc.
		Enumeration keys = themeProperties.keys();
		while(keys.hasMoreElements())
		{
			String k = (String)keys.nextElement();
			if(k.endsWith("color"))
			{
				Integer colorValue = new Integer(StyleSheet.parseColorValue(new StringUtil((String)themeProperties.get(k))));
				themeProperties.put(k,colorValue);
			}
			else if(k.endsWith("valign"))
			{
				Integer valignValue = new Integer(StyleSheet.parseVAlignValue(((String)themeProperties.get(k)).trim()));
				themeProperties.put(k,valignValue);
			}
			else if(k.endsWith("align"))
			{
				Integer alignValue = new Integer(StyleSheet.parseAlignValue(((String)themeProperties.get(k)).trim()));
				themeProperties.put(k,alignValue);
			}
			else if(k.endsWith("font"))
			{
				Font f = parseFont((String)themeProperties.get(k));
				themeProperties.put(k,f);
			}
		}
		
		
		String tmp = ((String)themeProperties.get("scrollbar.size"));
		if(tmp!=null)
		{
			scrollSize = Integer.parseInt(tmp.trim());
		}

		tmp = ((String)themeProperties.get("scrollbar.length"));
		if(tmp!=null)
		{
			scrollLenght = Integer.parseInt(tmp.trim());
		}

		tmp = ((String)themeProperties.get("navbar.size"));
		if(tmp!=null)
		{
			decorBottom = Integer.parseInt(tmp.trim());
			if(decorBottom<scrollSize) decorBottom = scrollSize;
		}
		
		tmp = ((String)themeProperties.get("titlebar.size"));
		if(tmp!=null)
		{
			decorTop = Integer.parseInt(tmp.trim());
		}
		
		Font f = getFontProperty("titlebar.font");
		if (f != null && f.getHeight() > decorTop)
		{
			decorTop = f.getHeight();
		}
		f = getFontProperty("navbar.font");
		if (f != null && f.getHeight() > decorBottom)
		{
			decorBottom = f.getHeight();
		}
	}
	
	private Font parseFont(String fontDef)
	{
		Vector v = StringUtil.split(fontDef,",");
		int fstyle = Font.STYLE_PLAIN;
		int fface = Font.FACE_SYSTEM;
		int fsize = Font.SIZE_MEDIUM;
		
		if(v.size()>=1)
		{
			String face = ((String)v.elementAt(0)).trim();
			if(face.equals("system"))
				fface = Font.FACE_SYSTEM;
			else if(face.equals("monospace"))
				fface = Font.FACE_MONOSPACE;
			else if(face.equals("proportional"))
				fface = Font.FACE_PROPORTIONAL;
		}
		if(v.size()>=2)
		{
			String style = ((String)v.elementAt(1)).trim();
			if(style.equals("plain"))
				fstyle = Font.STYLE_PLAIN;
			else if(style.equals("bold"))
				fstyle = Font.STYLE_BOLD;
			else if(style.equals("italic"))
				fstyle = Font.STYLE_ITALIC;
			else if(style.equals("underlined"))
				fstyle = Font.STYLE_UNDERLINED;
		}
		if(v.size()>=3)
		{
			String size = ((String)v.elementAt(2)).trim();
			if(size.equals("medium"))
				fsize = Font.SIZE_MEDIUM;
			else if(size.equals("small"))
				fsize = Font.SIZE_SMALL;
			else if(size.equals("large"))
				fsize = Font.SIZE_LARGE;
		}
		return Font.getFont(fface,fstyle,fsize);
	}
	
	public Image getLogo()
	{
		if(logo==null)
		{
			String url = getStringProperty("logo.icon");
			if(url!=null)
			{
				try{
					InputStream in = (new FireConnector()).openInputStream(url);
					logo = Image.createImage(in);
				}catch(IOException e){
					Log.logError("Failed to load logo image from: "+url,e);
				}
			}
		}
		return logo;
	}
	
	public Image getNavbarTexture(int width,int height)
	{
		Image img = Image.createImage(width,height);
		Graphics g = img.getGraphics();
		
		// draw bottom border.
		String grType= getStringProperty("navbar.gradient.type");
		if(grType!=null)
		{
			if("horizontal".equals(grType))
			{
				int gr[] = createGradient(getIntProperty("navbar.gradient.start.color"),getIntProperty("navbar.gradient.end.color"),width);
				for(int x=0;x<width;++x)
				{
					g.setColor(gr[x]);
					g.drawLine(x,0,x,height);
				}
			}
			else // vertical
			{
				int gr[] = createGradient(getIntProperty("navbar.gradient.start.color"),getIntProperty("navbar.gradient.end.color"),height);
				for(int y=0;y<height;++y)
				{
					g.setColor(gr[y]);
					g.drawLine(0,y,width,y);
				}
			}
		}
		else
		{
			g.setColor(((Integer)themeProperties.get("navbar.bg.color")).intValue());
			g.fillRect(0,0,width,height);			
		}
		
		g.setColor(((Integer)themeProperties.get("navbar.ruler2.color")).intValue());;
		g.drawLine(0,1,width,1);
		g.setColor(((Integer)themeProperties.get("navbar.ruler1.color")).intValue());;
		g.drawLine(0,0,width,0);
		return img;
	}
	
	public Image getTitlebarTexture(int width,int height)
	{
		Image img = Image.createImage(width,height);
		Graphics g = img.getGraphics();
		
		String grType= getStringProperty("titlebar.gradient.type");
		if(grType!=null)
		{
			if("horizontal".equals(grType))
			{
				int gr[] = createGradient(getIntProperty("titlebar.gradient.start.color"),getIntProperty("titlebar.gradient.end.color"),width);
				for(int x=0;x<width;++x)
				{
					g.setColor(gr[x]);
					g.drawLine(x,0,x,height);
				}
			}
			else // vertical
			{
				int gr[] = createGradient(getIntProperty("titlebar.gradient.start.color"),getIntProperty("titlebar.gradient.end.color"),height);
				for(int y=0;y<height;++y)
				{
					g.setColor(gr[y]);
					g.drawLine(0,y,width,y);
				}
			}
		}
		else
		{
			g.setColor(((Integer)themeProperties.get("titlebar.bg.color")).intValue());
			g.fillRect(0,0,width,height);			
		}
		
		
		g.setColor(((Integer)themeProperties.get("titlebar.ruler2.color")).intValue());
		g.drawLine(0,height-2,width,height-2);
		g.setColor(((Integer)themeProperties.get("titlebar.ruler1.color")).intValue());
		g.drawLine(0,height-1,width,height-1);

		return img;
	}
	
	public Image getBackgroundTexture(int width, int height)
	{
		int color = getIntProperty("bg.color");
		String grType= getStringProperty("bg.gradient.type");
		String bgIcon = (String)themeProperties.get("bg.icon");
		
		if(grType==null && color==TRANSPARENT && bgIcon==null)
		{
			return null;// no bg
		}
			
		Image img = Image.createImage(width,height);
		Graphics g = img.getGraphics();
		
		if(grType!=null)
		{
			if("horizontal".equals(grType))
			{
				int gr[] = createGradient(getIntProperty("bg.gradient.start.color"),getIntProperty("bg.gradient.end.color"),width);
				for(int x=0;x<width;++x)
				{
					g.setColor(gr[x]);
					g.drawLine(x,0,x,height);
				}
			}
			else // vertical
			{
				int gr[] = createGradient(getIntProperty("bg.gradient.start.color"),getIntProperty("bg.gradient.end.color"),height);
				for(int y=0;y<height;++y)
				{
					g.setColor(gr[y]);
					g.drawLine(0,y,width,y);
				}
			}
		}
		else if(color!=TRANSPARENT)
		{
			g.setColor(color);
			g.fillRect(0,0,width,height);
		}

		//boolean tiled =false; // FIXME: Must set a field in the theme.properties file to define tile behavior
		// draw bgImage
		if(bgIcon!=null)
		{
			try
			{
				Image defaultBgImageSrc = Image.createImage((new FireConnector()).openInputStream(bgIcon.trim()));
				if(defaultBgImageSrc!=null)
				{
					int w = width;
					int h = height;
					Integer align = (Integer)themeProperties.get("bg.icon.align");
					Integer valign = (Integer)themeProperties.get("bg.icon.valign");
					
					int offx = defaultBgImageSrc.getWidth()/2,offy=defaultBgImageSrc.getHeight()/2;
					int x=0,y=0;
					if(align==null) x=0; // Align left is the default
					else if((align.intValue()&FireScreen.CENTER)==FireScreen.CENTER) x = w/2 - offx;
					else if((align.intValue()&FireScreen.RIGHT)==FireScreen.RIGHT) x=w-offx-offx;
					
					if(valign==null) y= 0; // Valign top is the default
					else if((valign.intValue()&FireScreen.VCENTER)==FireScreen.VCENTER) y = h/2-offy; 
					else if((valign.intValue()&FireScreen.BOTTOM)==FireScreen.BOTTOM) y=h-offy-offy;
					g.drawImage(defaultBgImageSrc,x,y,Graphics.TOP|Graphics.LEFT);
				}
			} catch (IOException e)
			{
				Log.logWarn("Failed to load background image "+bgIcon+". "+e.getMessage());
			}
		}
		
		return img;
	}
}
