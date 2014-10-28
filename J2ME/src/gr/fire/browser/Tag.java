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
package gr.fire.browser;

import gr.fire.browser.util.Command;
import gr.fire.browser.util.Page;
import gr.fire.browser.util.StyleSheet;
import gr.fire.core.CommandListener;
import gr.fire.core.Component;
import gr.fire.core.Theme;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import org.kxml2.io.KXmlParser;

/**
 * @author padeler
 */
public abstract class Tag
{

	
	public static final String TAG_NOTSET = "";
	public static final String INPUT_TYPE = "type";
	public static final String INPUT_NAME = "name";
	public static final String INPUT_VALUE = "value";
	public static final String INPUTTYPE_TEXT = "text";
	public static final String INPUTTYPE_SUBMIT = "submit";
	public static final String ATTR_HREF = "href";
	public static final String ATTR_TYPE = "type";
	public static final String ATTR_VALUE = "value";
	public static final String ATTR_NAME = "name";
	
	
	protected Font font;
	protected int foregroundColor=0x00000000;
	protected int backgroundColor=Theme.TRANSPARENT;
	protected int layout;
	protected Command href;
	protected CommandListener listener;
	
	// common attributes:
	protected String tagClass,tagId,tagTitle;
	// i18N
	protected String lang;
	// style
	protected String style;
		
	protected int pointerX; // horizontal point where the next component starts

	private int baseLine; // bottom of the next component 
	private int lineHeight=0; // max height of the next component. If a component has bigger height, then the base line should be moved lower.
	
	protected BlockTag parentBlockTag;
	
	private String name=TAG_NOTSET;
	
	protected int border=0; // border width , if border==0 no border is drawn.  
	protected int borderStyle=Graphics.SOLID;
	protected int borderColor=0x00000000;

	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	protected void handleCommonAttributes(KXmlParser parser)
	{
		// retrieve common attributes.
		String tmp = parser.getAttributeValue(null,"id");
		if(tmp!=null) tagId = tmp;
		tmp = parser.getAttributeValue(null,"class");
		if(tmp!=null) tagClass = tmp;
		tmp = parser.getAttributeValue(null,"title");
		if(tmp!=null) tagTitle = tmp;
		
		tmp = parser.getAttributeValue(null,"style");
		if(tmp!=null)
		{
			StyleSheet.parseDeclaration(this,tmp);
			style=tmp;
		}

		tmp = parser.getAttributeValue(null,"lang");
		if(tmp!=null) lang= tmp;		
	}
	
	/**
	 * Make the next element inside this block element to be painted
	 * on the next line.
	 */	
	public void lineBreak(int lh, boolean forced)
	{
		if(!forced && pointerX==0) return; // do not add a line if already at the start of it.
		pointerX = 0;
		baseLine += lh; 
		lineHeight = lh;
	}
	
	public void increaseBaseLine(int amount)
	{
		baseLine +=amount;
	}
	
	protected void handleAlignAttributes(KXmlParser parser)
	{
		String tmp = parser.getAttributeValue(null,"align");
		if(tmp!=null)
		{
			layout |= StyleSheet.parseAlignValue(tmp);			
		}
		
		tmp = parser.getAttributeValue(null,"valign");
		if(tmp!=null)
		{
			layout |= StyleSheet.parseVAlignValue(tmp);
		}
	}
	
	protected void handleColorAttributes(KXmlParser parser)
	{
		String tmp = parser.getAttributeValue(null,"bgcolor");
		if(tmp!=null)
		{
			tmp = tmp.toLowerCase();
			Integer c = (Integer)StyleSheet.colors.get(tmp);
			if(c!=null) backgroundColor = c.intValue();
		}
		
		tmp = parser.getAttributeValue(null,"text");
		if(tmp!=null)
		{
			tmp = tmp.toLowerCase();
			Integer c = (Integer)StyleSheet.colors.get(tmp);
			if(c!=null) foregroundColor = c.intValue();
		}		
	}
	
	public void copyStyle(Component cmp)
	{
		cmp.setFont(font);
		cmp.setForegroundColor(foregroundColor);
		cmp.setBackgroundColor(backgroundColor);
		cmp.setLayout(cmp.getLayout()|layout);
		if(border>0) cmp.setBorder(true); // TODO add support for border color and style (solid, dotted,...)
		
		if(href!=null)
		{
			cmp.setCommand(href);
			cmp.setCommandListener(listener);
		}
	}
	
	public void inheritStyle(Tag parentTag)
	{
		font =  parentTag.font;
		foregroundColor = parentTag.foregroundColor;
		//backgroundColor = parentTag.backgroundColor; // Background color is not inherited, says the CSS1 Spec.
		layout = parentTag.layout;
		
		lang = parentTag.lang;
		
		href = parentTag.href;
		listener = parentTag.listener;
	}
	
	/**
	 * Returns the pointer's X position.
	 * 
	 * The pointer is considered to be at the location where the content of this Element ends.
	 * For example is this is an Element containing Text. PX,PY should be the point, relevant to the start of this component,
	 * where the next characted would be drawn if the string had one more characted. 
	 * The point where drawing starts is always considered the top-left corner of the bounding box of the letter.
	 * 
	 * The component must be validated to return the correct px,py.
	 * If the component is not validated an Exception is thrown
	 * @return
	 */
	public int getPointerX()
	{
		return pointerX;
	}
	
	public void setPointerX(int pointerX)
	{
		this.pointerX = pointerX;
	}
 
	public abstract void handleTagStart(Browser browser,Page page,KXmlParser parser);
	public abstract void handleTagEnd(Browser browser,Page page,KXmlParser parser);
	
	public abstract void handleText(Tag browser,String txt);	

	public Tag getParentBlockTag()
	{
		return parentBlockTag;
	}

	public int getBaseLine()
	{
		return baseLine;
	}

	public void setBaseLine(int baseLine)
	{
		this.baseLine = baseLine;
	}

	public int getLineHeight()
	{
//		if(lineHeight<=0)
//		{
//			lineHeight = font.getHeight();
//		}
		return lineHeight;
	}

	public void setLineHeight(int lineHeight)
	{
		this.lineHeight = lineHeight;
	}

	public int getForegroundColor()
	{
		return foregroundColor;
	}

	public void setForegroundColor(int foregroundColor)
	{
		this.foregroundColor = foregroundColor;
	}

	public int getBackgroundColor()
	{
		return backgroundColor;
	}

	public void setBackgroundColor(int backgroundColor)
	{
		this.backgroundColor = backgroundColor;
	}

	public int getBorder()
	{
		return border;
	}

	public void setBorder(int border)
	{
		this.border = border;
	}

	public Font getFont()
	{
		return font;
	}

	public void setFont(Font font)
	{
		this.font = font;
	}
}
