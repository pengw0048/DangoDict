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
package gr.fire.browser.util;

import gr.fire.browser.Browser;
import gr.fire.browser.Tag;
import gr.fire.core.FireScreen;
import gr.fire.util.Log;
import gr.fire.util.StringUtil;

import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Font;

/**
 * @author padeler
 *
 */
public class StyleSheet
{
	
	public static final Hashtable colors = new Hashtable();
	static{
		colors.put("black",new Integer(0x00000000)); 		
		colors.put("green",new Integer(0x00008000));
		colors.put("silver",new Integer(0x00C0C0C0)); 		
		colors.put("lime",new Integer(0x0000FF00));
		colors.put("gray",new Integer(0x00808080));		
		colors.put("olive",new Integer(0x00808000));
		colors.put("white",new Integer(0x00FFFFFF));
		colors.put("yellow",new Integer(0x00FFFF00));
		colors.put("maroon",new Integer(0x00800000)); 		
		colors.put("navy",new Integer(0x00000080));
		colors.put("red",new Integer(0x00FF0000));	
		colors.put("blue",new Integer(0x000000FF));
		colors.put("purple",new Integer(0x00800080)); 		
		colors.put("teal",new Integer(0x00008080));
		colors.put("fuchsia",new Integer(0x00FF00FF)); 		
		colors.put("aqua",new Integer(0x0000FFFF));
		colors.put("transparent",new Integer(0xFF000000));
	}
	
	public static final String COLOR = "color";
	public static final String BACKGROUND_COLOR = "background-color";
	public static final String BACKGROUND = "background";
	public static final String BORDER = "border";
	public static final String FONT = "font";
	public static final String FONT_WEIGHT = "font-weight";
	public static final String FONT_SIZE = "font-size";
	
	
	/**
	 * Initializes the default stylesheet.
	 */
	public StyleSheet()
	{
		
	}
	
	
	public void parseDocument(String css)
	{
		
	}
	
	public void applyStyle(Browser browser,Tag t)
	{
		
	}
	

	public static void parseDeclaration(Tag owner, String declaration)
	{
		Vector properties = StringUtil.split(declaration,";");
		for(int i=0;i<properties.size();++i)
		{
			String keyValue = ((String)properties.elementAt(i)).trim();
			parseDeclarationKeyValue(owner,keyValue);
		}
	}
	
	private static void parseDeclarationKeyValue(Tag owner,String keyValue)
	{
		StringUtil tokenizer = new StringUtil(keyValue);
		String key = tokenizer.nextToken(':').trim();
		if(COLOR.equals(key))
		{
			owner.setForegroundColor(parseColorValue(tokenizer));
			return;
		}
		
		if(BACKGROUND_COLOR.equals(key))
		{
			owner.setBackgroundColor(parseColorValue(tokenizer));
			return;
		}
		
		if(BACKGROUND.equals(key))
		{
			owner.setBackgroundColor(parseColorValue(tokenizer));
			return;
		}
		
		if(BORDER.equals(key))
		{ // ignore all other border parameters since they are not supported by the browser (yet)
			String val = tokenizer.nextWord();
			if("1".equals(val))
				owner.setBorder(1);
			
			return;
		}
		
		if(FONT_WEIGHT.equals(key))
		{ // ignore all other border parameters since they are not supported by the browser (yet)
			String value = tokenizer.nextToken(';');
			if(value!=null)
			{
				value = value.toLowerCase().trim();
				Font f = owner.getFont();
				if(value.startsWith("bold")) // bold or bolder
				{
					owner.setFont(Font.getFont(f.getFace(),f.getStyle()|Font.STYLE_BOLD,f.getSize()));
				}
				else if(value.startsWith("normal"))
				{
					owner.setFont(Font.getFont(f.getFace(),f.getStyle()|Font.STYLE_PLAIN,f.getSize()));
				}
			}
			return;
		}
		if(FONT_SIZE.equals(key))
		{ // ignore all other border parameters since they are not supported by the browser (yet)
			String value = tokenizer.nextToken(';');
			if(value!=null)
			{
				value = value.toLowerCase().trim();
				Font f = owner.getFont();
				if(value.startsWith("large")) // large/larger
				{
					owner.setFont(Font.getFont(f.getFace(),f.getStyle(),Font.SIZE_LARGE));
				}
				else if(value.startsWith("small")) // small/smaller
				{
					owner.setFont(Font.getFont(f.getFace(),f.getStyle(),Font.SIZE_SMALL));
				}
				else if(value.startsWith("medium")) // small/smaller
				{
					owner.setFont(Font.getFont(f.getFace(),f.getStyle(),Font.SIZE_MEDIUM));
				}
			}
			return;
		}
	}
	
	public static int parseAlignValue(String align)
	{
		String tmp = align.toLowerCase();
		if(tmp.equals("right")) return FireScreen.RIGHT;
		if(tmp.equals("middle")) return FireScreen.CENTER;
		
		return FireScreen.LEFT;
	}
	
	public static int parseVAlignValue(String valign)
	{
		String tmp = valign.toLowerCase();
		if(tmp.equals("middle")) return FireScreen.VCENTER;
		if(tmp.equals("bottom")) return FireScreen.BOTTOM;
		return FireScreen.TOP;
	}
	
	/**
	 * A color value can be:
	 * - color name (i.e red)
	 * - rgb value (i.e. rgb(255,0,0) or rgb(90%,20%,0%) )
	 * - hex value (i.e. #F00 or #FF0000)
	 * 
	 * @param tokenizer
	 * @return
	 */
	public static int parseColorValue(StringUtil tokenizer)
	{
		int res = 0xFF000000;
		tokenizer.skipWhiteChars();
		String hexStr=null,val=null;
		try
		{
			char c = tokenizer.peakNextChar();
			if(c=='#') // hex
			{
				hexStr = tokenizer.nextWord().substring(1);
				if(hexStr.length()==3)
				{ // convert #f00 representation to #ff0000
					char r = hexStr.charAt(0);
					char g = hexStr.charAt(1);
					char b = hexStr.charAt(2);
					hexStr = new String(new char[]{r,r,g,g,b,b});
				}
				res = Integer.parseInt(hexStr,16);
			}
			else // can be color name or rgb(0,0,0) 
			{
				val = tokenizer.lastToken().toLowerCase().trim();
				if(val.startsWith("rgb"))
				{
					String rgb = val.substring(val.indexOf('(')+1,val.length()-1); // comma seperated values
					StringUtil rgbTokens = new StringUtil(rgb);
					String v=null;
					
					while((v=rgbTokens.nextToken(','))!=null)
					{
						v = v.trim();
						int colorVal;
						if(v.length()==0)
						{
							colorVal = 0 ;
						}
						else if(v.endsWith("%")) // percent
						{
							colorVal = Integer.parseInt(v.substring(0,v.length()-1));
							// convert to 255 range
							colorVal  = (colorVal*255)/100;
						}
						else
						{
							colorVal = Integer.parseInt(v);
						}
						if(colorVal<0) colorVal=0;
						else if(colorVal>255) colorVal=255;
						res |=colorVal;
						res <<= 8;
					}
				}else
				{
					Integer color = (Integer)colors.get(val);
					if(color!=null) res = color.intValue();
				}
			}
		}catch(Exception e){
			Log.logWarn("Failed to parse color value: "+hexStr+", "+val);
		}
		return res; 
	}
	
	
	public static int calculateLenght(String value,int maxLen)
	{
		int width=-1;
		if(value!=null)
		{
			try{
				value = value.trim().toLowerCase();
				if(value.endsWith("%")) // percent
				{
					width = Integer.parseInt(value.substring(0,value.length()-1));
					width = (width*maxLen)/100;
				}
				else
				{
					int idx = value.indexOf("px");
					if(idx!=-1) value = value.substring(0,idx);
					width = Integer.parseInt(value);
				}
			}catch(Exception e)
			{
				Log.logWarn("Failed to parse lenght "+value,e);
			}
		}
		return width;
	}
	
}
