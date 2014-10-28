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

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * @author padeler
 * 
 */
public class ImageComponent extends Component
{
	private static final String EMPTY_STRING="";

	public static final int LINE_DISTANCE = 0;

	private Image img;
	private String alt;

	public ImageComponent(Image img, int width, int height,Font font, String alt)
	{
		if (alt == null)
			alt = EMPTY_STRING;

		this.alt = alt;
		this.img = img;

		if (img != null)
		{
			if (width == -1)
				width = img.getWidth();
			if (height == -1)
				height = img.getHeight();
		} else
		{
			if (height == -1)
				height = font.getHeight();
			if (width == -1)
			{
				width = font.stringWidth(alt);
				if (width > 100)
					width = 100; // keep the unloaded ImagePrimitives small
			}
		}
		setPrefSize(width, height);
	}
	
	public void setImage(Image img)
	{
		this.img=img;
		if (img != null)
		{
			if(width<=0  && height<=0)
			{
				width = img.getWidth();
				height = img.getHeight();
				setPrefSize(width, height);
				valid=false;
			}
		}
		repaint();
	}
	
	
	public ImageComponent(Image img, String alt)
	{
		this(img,-1,-1,FireScreen.getTheme().getFontProperty("font"),alt);
	}

	public void validate()
	{
		if(width==0 && height==0) // width, height are not set
		{
			int []ps = getPrefSize();
			if(ps==null) 
			{
				ps = getMinSize();
			}
			width = ps[0];height=ps[1];
		}
		valid = true;
	}

	public void paint(Graphics g)
	{
		int imgTopLeftX = 0, imgTopLeftY = 0;
		if (img != null)
		{
			if ((layout & FireScreen.TOP) == FireScreen.TOP)
			{
				imgTopLeftY = 0;
			} else if ((layout & FireScreen.VCENTER) == FireScreen.VCENTER)
			{
				imgTopLeftY = getHeight() / 2 - img.getHeight() / 2;
			} else if ((layout & FireScreen.BOTTOM) == FireScreen.BOTTOM)
			{
				imgTopLeftY = getHeight() - img.getHeight();
			}

			if ((layout & FireScreen.LEFT) == FireScreen.LEFT)
			{
				imgTopLeftX = 0;
			} else if ((layout & FireScreen.CENTER) == FireScreen.CENTER)
			{
				imgTopLeftX = getWidth() / 2 - img.getWidth() / 2;
			} else if ((layout & FireScreen.RIGHT) == FireScreen.RIGHT)
			{
				imgTopLeftX = getWidth() - img.getWidth();
			}
		}
		boolean selected = isSelected();
		Theme theme = FireScreen.getTheme();
		if (selected)
		{
			g.setColor(theme.getIntProperty("link.active.bg.color"));
			g.fillRect(g.getClipX(), g.getClipY(), g.getClipWidth(), g.getClipHeight());
		} 
		else if(backgroundColor!=Theme.TRANSPARENT)
		{
			g.setColor(backgroundColor);
			g.fillRect(g.getClipX(), g.getClipY(), g.getClipWidth(), g.getClipHeight());
		}
		if (img != null)
			g.drawImage(img, imgTopLeftX, imgTopLeftY, Graphics.TOP | Graphics.LEFT);
		else
		{
			g.setColor(foregroundColor);
			g.drawString(alt, imgTopLeftX, imgTopLeftY, Graphics.TOP | Graphics.LEFT);
		}

		if (selected)
		{
			g.setColor(theme.getIntProperty("link.fg.color"));
			g.drawRect(0, 0, width - 1, height - 1);
		}
		if(border)
		{
			g.setColor(theme.getIntProperty("border.color"));
			g.drawRect(0,0,width-1,height-1);
		}
	}

	protected void pointerReleased(int x, int y)
	{

		if (command != null && commandListener != null)
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
		if ((key == Canvas.LEFT || key == Canvas.DOWN || key == Canvas.RIGHT || key == Canvas.UP))
		{
			setSelected(!isSelected());
		} else if (command != null && commandListener != null && key == Canvas.FIRE)
			commandListener.commandAction(command, this);		
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

	public Image getImage()
	{
		return img;
	}

	public int [] getMinSize()
	{
		if (img != null)
			return new int[]{img.getWidth(), img.getHeight()};
		return super.getMinSize();
	}

	public int getContentWidth()
	{
		if (!valid)
			throw new IllegalStateException("The element is not validated.");
		if (img != null)
			return img.getWidth();
		return 0; // empty element.
	}

	public int getContentHeight()
	{
		if (!valid)
			throw new IllegalStateException("The element is not validated.");
		if (img != null)
			return img.getHeight();
		return 0; // empty element.
	}
}
