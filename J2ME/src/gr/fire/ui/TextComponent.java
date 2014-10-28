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

import gr.fire.core.Component;
import gr.fire.core.FireScreen;
import gr.fire.core.Theme;
import gr.fire.util.StringUtil;

import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * @author padeler
 *
 */
public class TextComponent extends Component
{
	public static final int LINE_DISTANCE=0;
	public static final byte HIGHLIGHT_MODE_NONE=0x00;
	public static final byte HIGHLIGHT_MODE_LINK=0x01;
	public static final byte HIGHLIGHT_MODE_FULL=0x02;
	
	private String text;
	private Vector formatedText;
	private int textWidth,startX;
	private int lastLineExtraHeight=0;
	
	private byte hightlightMode =  HIGHLIGHT_MODE_LINK;
	
	
	public TextComponent(String txt,int textWidth,int startX)
	{
		setText(txt);
		this.textWidth = textWidth;
		this.startX = startX;
		if(font==null) font = FireScreen.getTheme().getFontProperty("font");
	}
	
	public TextComponent(String txt)
	{
		this(txt,FireScreen.getScreen().getWidth());
	}

	public TextComponent(String txt,int textWidth)
	{
		this(txt,textWidth,0);
	}
	
	/**
	 * @param text the text to set
	 */
	public void setText(String txt)
	{
		if(txt==null) throw new NullPointerException("Cannot have null text in the Text component.");
		this.text = txt;
		valid=false;
	}

	
	public void validate()
	{
		if(font==null) font = FireScreen.getTheme().getFontProperty("font");
		formatedText = StringUtil.format(text,font,textWidth-startX,textWidth);
		if(width==0 && height==0) // width, height are not set
		{
			int []ps = getPrefSize();
			if(ps==null) 
			{
				ps = getMinSize();
			}
			width = ps[0];height=ps[1];
		}
		valid=true;
	}
	
	public void paint(Graphics g)
	{
		int valign = getValign();
		int halign = getHalign();
		
		
		Theme theme = FireScreen.getTheme();
		int width = getWidth();
		int height = getHeight();
		g.setFont(font);
		Vector lines = formatedText;
		
		int rowHeight = font.getHeight();
		int lineCount = lines.size();
		int txtHeight = (rowHeight + LINE_DISTANCE)*lines.size() + lastLineExtraHeight;

		for (int i = 0; i < lineCount; ++i)
		{
			String str = (String) lines.elementAt(i);
			
			int x=0,y=0;
			switch(valign)
			{
			case FireScreen.TOP:
				y = (rowHeight+LINE_DISTANCE) * i;
				break;
			case FireScreen.VCENTER:
				y = (height/2 - txtHeight/2) +  (rowHeight+LINE_DISTANCE) * i;
				break;
			case FireScreen.BOTTOM:
				y = height - txtHeight +((rowHeight+LINE_DISTANCE) * i);
				break;	
			}
			
			if(i==lineCount-1) y += lastLineExtraHeight;

			switch(halign)
			{
			case FireScreen.LEFT:
				if(i==0) x = startX;
				else x =0;
				break;
			case FireScreen.CENTER:
				if(i==0)
					x = width/2 - font.stringWidth(str)/2 + startX;
				else 
					x = width/2 - font.stringWidth(str)/2;
				break;
			case FireScreen.RIGHT:
				x = width- font.stringWidth(str);
				break;
			}

			if(!isSelected())
			{
				// paint background is needed
				if(backgroundColor!=Theme.TRANSPARENT)
				{
					if(hightlightMode!=HIGHLIGHT_MODE_FULL)
					{
						g.setColor(backgroundColor);
						g.fillRect(x , y , font.stringWidth(str), rowHeight);
					}
					else
					{
						g.setColor(backgroundColor);
						g.fillRect(x , y , width, rowHeight);
					}
				}
				g.setColor(foregroundColor);
			}
			else
			{
				int bgColor = theme.getIntProperty("link.active.bg.color");
				if(bgColor!=Theme.TRANSPARENT)
				{
					if(hightlightMode==HIGHLIGHT_MODE_LINK)
					{
						g.setColor(bgColor);
						g.fillRect( x , y , font.stringWidth(str), rowHeight);										
					}
					else if(hightlightMode==HIGHLIGHT_MODE_FULL)
					{
						g.setColor(bgColor);
						g.fillRect( 0 , y , width, rowHeight);
					}
				}
				g.setColor(theme.getIntProperty("link.active.fg.color"));
			}
//			System.out.println("PRINTING LINE: ["+ str+"]("+x+"+ "+(i==0?startX:0)+"),"+y+" Width: "+ font.stringWidth(str) +" COLOR "+g.getColor());
			g.drawString(str,x, y, Graphics.TOP | Graphics.LEFT);
		}
		
		if(border)
		{
			g.setColor(theme.getIntProperty("border.color"));
			g.drawRect(0,0,width-1,height-1);			
		}
	}
	
	public boolean contains(int x, int y)
	{
		if(super.contains(x, y))
		{
			if(formatedText.size()==1)
			{ 
				int halign = getHalign();
				int offx=0;
				int llw = getLastLineWidth();
				switch(halign)
				{ // XXX This is a hack for pointer events to work correctly, until the alignment bug is fixed.
				case FireScreen.LEFT:
					offx = 0;
					break;
				case FireScreen.CENTER:
					offx = width/2 - llw/2;
					break;
				case FireScreen.RIGHT:
					offx = width-startX - llw;
					break;
				}
				if(x>(offx+startX) && x<(offx+startX +llw )) return true;
			}
			else // more than one lines. 
			{
				if(x<startX && y<(font.getHeight()+LINE_DISTANCE)) return false;
				if(x>getLastLineWidth() && y>(getContentHeight()-getLastLineHeight())) return false;
				return true;
			}
		}
		return false;
	}
	
	protected void pointerReleased(int x, int y)
	{
		if(command!=null && commandListener!=null)
		{
			setSelected(!isSelected());
			if(isSelected())
				commandListener.commandAction(command,this);
		}
		super.pointerReleased(x, y);
	}
	
	private void handleKeyEvent(int keyCode)
	{
		int key = FireScreen.getScreen().getGameAction(keyCode);
		if((key==Canvas.LEFT|| key==Canvas.DOWN ||key==Canvas.RIGHT || key==Canvas.UP))
		{
			setSelected(!isSelected());
		}
		else if(command!=null && commandListener!=null && key==Canvas.FIRE)
			commandListener.commandAction(command,this);		
	}
	
	protected void keyRepeated(int keyCode)
	{
		handleKeyEvent(keyCode);
		super.keyRepeated(keyCode);
	}
	
	protected void keyReleased(int keyCode)
	{
		handleKeyEvent(keyCode);
		super.keyReleased(keyCode);
	}
	
	public void setSelected(boolean v)
	{
		super.setSelected(v);
		repaint();
	}
	
	public String getText()
	{
		return text;
	}
		
	public int[] getMinSize()
	{
		int h;
		if(formatedText!=null)
		{
			h = ((font.getHeight() + LINE_DISTANCE)*formatedText.size()) + lastLineExtraHeight;
		}
		else h = font.getHeight();
		return new int[]{textWidth,h};
	}

	public Vector getFormatedText()
	{
		return formatedText;
	}
	
	public int getContentHeight()
	{
		if(!valid) throw new IllegalStateException("The element is not validated.");
		return ((font.getHeight() + LINE_DISTANCE)*formatedText.size()) + lastLineExtraHeight;
	}
	
	public int getContentWidth()
	{
		if(!valid) throw new IllegalStateException("The element is not validated.");
		if(formatedText.size()==0) return 0;
		if(formatedText.size()==1) return font.stringWidth((String)formatedText.lastElement());
		// more than two lines:
		return textWidth;
	}
	
	public int getLastLineWidth()
	{
		if(!valid) throw new IllegalStateException("The element is not validated.");
		if(formatedText.size()==0) return 0;
		return font.stringWidth((String)formatedText.lastElement());		
	}
	
	public int getLastLineHeight()
	{
		if(!valid) throw new IllegalStateException("The element is not validated.");
		if(formatedText.size()==0) return 0;
		return (font.getHeight()+LINE_DISTANCE)+lastLineExtraHeight;		
	}

	public int getLastLineExtraHeight()
	{
		return lastLineExtraHeight;
	}

	public void setLastLineExtraHeight(int lastLineExtraHeight)
	{
		this.lastLineExtraHeight = lastLineExtraHeight;
	}

	public byte getHightlightMode()
	{
		return hightlightMode;
	}

	public void setHightlightMode(byte hightlightMode)
	{
		this.hightlightMode = hightlightMode;
	}
}
