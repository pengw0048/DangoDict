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

import gr.fire.browser.util.Page;
import gr.fire.core.FireScreen;
import gr.fire.core.Theme;
import gr.fire.ui.ImageComponent;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import org.kxml2.io.KXmlParser;

/**
 * @author padeler
 *
 */
public class ListBlockTag extends BlockTag
{

	public static final String TAG_DL = "dl";
	public static final String TAG_DT = "dt";
	public static final String TAG_DD = "dd";
	public static final String TAG_OL = "ol";
	public static final String TAG_UL = "ul";
	public static final String TAG_LI = "li";
		
	
	private int listIndentation = 10; // pixels
	private int count=0;
	private Image ulBullet = null;
	private boolean filledBullet=true;
	
	public ListBlockTag()
	{
		super();
	}
	
	private Image getUlBullet()
	{
		if(ulBullet==null)
		{
			ulBullet = Image.createImage(listIndentation-6,listIndentation-6);
			Graphics g = ulBullet.getGraphics();
			g.setColor(foregroundColor);
			if(filledBullet)
				g.fillRect(0,0,listIndentation-6,listIndentation-6);
			else
			{
				if(backgroundColor!=Theme.TRANSPARENT)
				{
					g.setColor(backgroundColor);
					g.fillRect(0,0,listIndentation-6,listIndentation-6);
				}
				g.drawRect(0,0,listIndentation-6,listIndentation-6);
			}
		}
		return ulBullet;		
	}
	
	
	public void inherit(Tag parent)
	{
		if(parent!=null)
		{
			inheritStyle(parent); // inherit style information
			if(parent instanceof BlockTag) this.parentBlockTag = (BlockTag)parent;
			else this.parentBlockTag = parent.parentBlockTag;
		}
	}

	public void handleTagStart(Browser browser,Page page, KXmlParser parser)
	{
		String name = parser.getName().toLowerCase(); // the name of the tag
		setName(name);
		
		Tag parentElement = browser.topTag(); // may be a block element or an inline element
		
		inherit(parentElement); // first inherit from parents
		handleCommonAttributes(parser); // then get extra style from attributes
		
		if(TAG_UL.equals(name) || TAG_OL.equals(name) || TAG_DL.equals(name))
		{
			if(parentBlockTag!=null)
			{
				int tempIndent = 0;
				if(parentBlockTag instanceof ListBlockTag)
				{
					tempIndent = listIndentation; 
					filledBullet = !((ListBlockTag)parentBlockTag).filledBullet; 
				}
				parentBlockTag.elementContainer.add(elementContainer);
				parentBlockTag.lineBreak(parentBlockTag.font.getHeight(),false);
 
				elementContainer.setX(parentBlockTag.getPointerX()+tempIndent);
				elementContainer.setY(parentBlockTag.getBaseLine()-parentBlockTag.getLineHeight());
				containerWidth = parentBlockTag.getContainerWidth()-parentBlockTag.getPointerX()-tempIndent;	
			}
		}
		else if(parentBlockTag!=null && parentBlockTag instanceof ListBlockTag)
		{
			((ListBlockTag)parentBlockTag).count++;
			count =  ((ListBlockTag)parentBlockTag).count;
			parentBlockTag.lineBreak(font.getHeight(),false);
			if(TAG_LI.equals(name))
			{
				filledBullet = ((ListBlockTag)parentBlockTag).filledBullet;
				
				int tempIndentation = listIndentation;
				// add the bullet or the number
				if(TAG_OL.equals(parentBlockTag.getName()))
				{
					String txt = count +".";
					tempIndentation -= font.stringWidth(txt);
					parentBlockTag.handleText(this,txt);
				}
				else
				{
					Image b = getUlBullet();
					tempIndentation -= b.getWidth();
					ImageComponent imCmp = new ImageComponent(b,listIndentation,listIndentation,this.font,"");
					imCmp.setLayout(FireScreen.CENTER|FireScreen.VCENTER);
					copyStyle(imCmp);
					parentBlockTag.handleComponent(this,imCmp);
				}
				
				parentBlockTag.elementContainer.add(elementContainer);

				elementContainer.setX(parentBlockTag.getPointerX()+tempIndentation);
				elementContainer.setY(parentBlockTag.getBaseLine()-parentBlockTag.getLineHeight());
				containerWidth = parentBlockTag.getContainerWidth()-parentBlockTag.getPointerX()-tempIndentation;
			}	
			else if(TAG_DT.equals(name))
			{
				parentBlockTag.elementContainer.add(elementContainer);

				elementContainer.setX(parentBlockTag.getPointerX());
				elementContainer.setY(parentBlockTag.getBaseLine()-parentBlockTag.getLineHeight());
				containerWidth = parentBlockTag.getContainerWidth()-parentBlockTag.getPointerX();
			}
			else if(TAG_DD.equals(name))
			{
				parentBlockTag.lineBreak(parentBlockTag.font.getHeight(),false);
				parentBlockTag.elementContainer.add(elementContainer);

				elementContainer.setX(parentBlockTag.getPointerX()+listIndentation);
				elementContainer.setY(parentBlockTag.getBaseLine()-parentBlockTag.getLineHeight());
				containerWidth = parentBlockTag.getContainerWidth()-parentBlockTag.getPointerX()-listIndentation;
			}
		}
		else return; // ignore these tags if not inside a list block
		
		copyStyle(elementContainer);
	}
	
	public void handleText(Tag topLevelTag, String txt)
	{
		super.handleText(topLevelTag, txt);
	}

	public void handleTagEnd(Browser browser,Page page, KXmlParser parser)
	{
		int baseLine = getBaseLine();
		if(parentBlockTag!=null)
		{
			parentBlockTag.increaseBaseLine(baseLine-parentBlockTag.getLineHeight());
			parentBlockTag.pointerX += containerWidth;
		}
		elementContainer.setPrefSize(containerWidth,baseLine);
	}
}