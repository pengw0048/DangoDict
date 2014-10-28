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
import javax.microedition.lcdui.TextField;


/**
 * @author padeler
 *
 */
public class InputComponent extends Component
{
	public static final int LINE_DISTANCE=0;
	
	public static final byte TEXT=0x01;
	public static final byte RADIO=0x02;
	public static final byte CHECKBOX=0x03;
	public static final byte SWITCH=0x04;
	public static final byte BUTTON=0x05;
	public static final byte SUBMIT=0x06;
	public static final byte RESET=0x07;
	public static final byte MENU=0x08;
	public static final byte HIDDEN=0x09;
	
	public static final int RADIO_WIDTH=16;
	public static final int RADIO_HEIGHT=16;
	
	public static final int TEXT_BORDER=4;
	public static final int SWITCH_BORDER=3;

	protected String name,value,initialValue,text;
	protected boolean enabled=true;
	protected int maxWidth=1000;

	private byte type=TEXT;	
	private int size=200,maxLen=100,rows=1;	
	private boolean checked=false;
	private int textConstraints = TextField.ANY;

	private Vector formatedText=null;
	
	
	public InputComponent()
	{}
	
	public InputComponent(byte type)
	{
		this.type=type;
		if(font==null)
			font = FireScreen.getTheme().getFontProperty("font");
	}
	
	public void validate()
	{
		
		int []ps= getPrefSize();
		if(ps==null)
		{
			ps = getMinSize();
		}
		
		if(type==TEXT)
		{
			formatedText = StringUtil.format(value,font,ps[0],ps[0]);
		}
		
		if(width==0 && height==0) // width, height are not set
		{
			width = ps[0];height=ps[1];
		}
		
		valid=true;
	}
	
	public boolean isFocusable()
	{
		return enabled;
	}
	
	public void paint(Graphics g)
	{
		switch (type)
		{
		case TEXT:
			paintText(g);
			break;
		case RADIO:
			paintRadioOrCheckbox(g);
			break;
		case CHECKBOX:
			paintRadioOrCheckbox(g);
			break;
		case BUTTON:
			paintButton(g);
			break;
		case RESET:
			paintButton(g);
			break;
		case SUBMIT:
			paintButton(g);
			break;
		case SWITCH:
			paintSwitch(g);
			break;
		case MENU:
			paintMenu(g);
			break;
		}
	}
	
	private void paintSwitch(Graphics g)
	{
		// create a bordered area with the text inside.
		int valign = getValign();
		int halign = getHalign();
		
		Theme theme = FireScreen.getTheme();
		int width = getWidth();
		int height = getHeight();
		g.setFont(font);
		
		if(isSelected())
		{
			g.setColor(theme.getIntProperty("link.active.bg.color"));
			g.fillRect(0,0,width,height);
		}
		else if(checked)
		{
			g.setColor(theme.getIntProperty("bg.alt2.color"));
			g.fillRect(0,0,width,height);
		}
		else if(backgroundColor!=Theme.TRANSPARENT)
		{
			g.setColor(backgroundColor);
			g.fillRect(0,0,width,height);
		}
			
		
		int rowHeight = font.getHeight();

		String str = text;
		
		int x=0,y=0;
		switch(valign)
		{
		case FireScreen.TOP:
			y = SWITCH_BORDER;
			break;
		case FireScreen.VCENTER:
			y = (height -(rowHeight+LINE_DISTANCE))/2;
			break;
		case FireScreen.BOTTOM:
			y = height - ((rowHeight+LINE_DISTANCE) )-SWITCH_BORDER;
			break;	
		}
		
		switch(halign)
		{
		case FireScreen.LEFT:
			x =SWITCH_BORDER;
			break;
		case FireScreen.CENTER:
			x = width/2 - font.stringWidth(str)/2;
			break;
		case FireScreen.RIGHT:
			x = width- font.stringWidth(str)-SWITCH_BORDER;
			break;
		}

		if(isSelected())
			g.setColor(theme.getIntProperty("link.active.fg.color"));
		else
			g.setColor(foregroundColor);
		
		g.drawString(str,x, y, Graphics.TOP | Graphics.LEFT);

	}
	
	private void paintMenu(Graphics g)
	{
		// create a bordered area with the text inside.
		int valign = getValign();
		int halign = getHalign();
		
		Theme theme = FireScreen.getTheme();
		int width = getWidth();
		int height = getHeight();
		g.setFont(font);
		
		g.setColor(theme.getIntProperty("bg.alt2.color"));
		g.fillRect(2,2,width-3,height-3);
		
		int rowHeight = font.getHeight();

		String str = text;
		
		int x=0,y=0;
		switch(valign)
		{
		case FireScreen.TOP:
			y = SWITCH_BORDER;
			break;
		case FireScreen.VCENTER:
			y = (height -(rowHeight+LINE_DISTANCE))/2;
			break;
		case FireScreen.BOTTOM:
			y = height - ((rowHeight+LINE_DISTANCE) )-SWITCH_BORDER;
			break;	
		}
		
		switch(halign)
		{
		case FireScreen.LEFT:
			x =SWITCH_BORDER;
			break;
		case FireScreen.CENTER:
			x = width/2 - font.stringWidth(str)/2;
			break;
		case FireScreen.RIGHT:
			x = width- font.stringWidth(str)-SWITCH_BORDER;
			break;
		}

		if(!isSelected())
			g.setColor(foregroundColor);
		else
			g.setColor(theme.getIntProperty("link.active.fg.color"));

		g.drawString(str,x, y, Graphics.TOP | Graphics.LEFT);
	
		g.setColor(theme.getIntProperty("border.color"));
		g.drawRect(2,2,width-4,height-4);
		
		if(isSelected())
		{
			g.setColor(theme.getIntProperty("link.active.bg.color"));
			g.drawRect(1,1,width-2,height-2);			
		}
	}


	private void paintButton(Graphics g)
	{
		if(formatedText==null)
		{
			int[] ps = getPrefSize();
			if(ps==null) ps = getMinSize();
			formatedText = StringUtil.format(value,font,ps[0],ps[1]);
		}
		// create a bordered area with the text inside.
		int valign = getValign();
		int halign = getHalign();
		
		Theme theme = FireScreen.getTheme();
		int width = getWidth();
		int height = getHeight();
		g.setFont(font);
		Vector lines = formatedText;
		//g.setColor(theme.getIntProperty("bg.alt2.color"));
		if(backgroundColor!=Theme.TRANSPARENT)
			g.setColor(backgroundColor);
		else
			g.setColor(theme.getIntProperty("bg.alt2.color"));
		g.fillRoundRect(2,2,width-4,height-4,6,6);
		
		int rowHeight = font.getHeight();
		int lineCount = lines.size();
		int txtHeight = (rowHeight + LINE_DISTANCE)*lines.size();

		for (int i = 0; i < lineCount && i<rows; ++i)
		{
			String str = (String) lines.elementAt(i);
			
			if((textConstraints & TextField.PASSWORD) == TextField.PASSWORD)
			{ // just draw a string of starts
				int len = str.length();
				str="";
				for (int j = 0; j < len; ++j)
					str += "*";
			}
			
			int x=0,y=0;
			switch(valign)
			{
			case FireScreen.TOP:
				y = (rowHeight+LINE_DISTANCE) * i +TEXT_BORDER;
				break;
			case FireScreen.VCENTER:
				y = (height/2 - txtHeight/2) +  (rowHeight+LINE_DISTANCE) * i;
				break;
			case FireScreen.BOTTOM:
				y = height - txtHeight +((rowHeight+LINE_DISTANCE) * i)-TEXT_BORDER;
				break;	
			}
			
			switch(halign)
			{
			case FireScreen.LEFT:
				x =TEXT_BORDER;
				break;
			case FireScreen.CENTER:
				x = width/2 - font.stringWidth(str)/2;
				break;
			case FireScreen.RIGHT:
				x = width- font.stringWidth(str)-TEXT_BORDER;
				break;
			}

			if(!isSelected())
				g.setColor(foregroundColor);
			else
				g.setColor(theme.getIntProperty("link.active.fg.color"));

			g.drawString(str,x, y, Graphics.TOP | Graphics.LEFT);
		}

		
		if(isSelected())
		{
			g.setColor(theme.getIntProperty("link.active.bg.color"));
			g.drawRoundRect(1,1,width-2,height-2,8,8);
		}
		else
		{
			g.setColor(theme.getIntProperty("border.color"));
		}
		g.drawRoundRect(2,2,width-4,height-4,6,6);
	}
	
	private void paintText(Graphics g)
	{
		if(formatedText==null)
		{
			int[] ps = getPrefSize();
			if(ps==null) ps = getMinSize();
			formatedText = StringUtil.format(value,font,ps[0],ps[0]);
		}
		// create a bordered area with the text inside.
		int valign = getValign();
		int halign = getHalign();
		
		Theme theme = FireScreen.getTheme();
		int width = getWidth();
		int height = getHeight();
		g.setFont(font);
		Vector lines = formatedText;
		
		int rowHeight = font.getHeight();
		int lineCount = lines.size();
		int txtHeight = (rowHeight + LINE_DISTANCE)*lines.size();
		
		// paint background if needed
		if(backgroundColor!=Theme.TRANSPARENT)
		{
			g.setColor(backgroundColor);
			g.fillRect(2,2,width-3,height-3);
		}
		
		for (int i = 0; i < lineCount && i<rows; ++i)
		{
			String str = (String) lines.elementAt(i);
			
			if((textConstraints & TextField.PASSWORD) == TextField.PASSWORD)
			{ // just draw a string of starts
				int len = str.length();
				str="";
				for (int j = 0; j < len; ++j)
					str += "*";
			}
			
			int x=0,y=0;
			switch(valign)
			{
			case FireScreen.TOP:
				y = (rowHeight+LINE_DISTANCE) * i +TEXT_BORDER;
				break;
			case FireScreen.VCENTER:
				y = (height/2 - txtHeight/2) +  (rowHeight+LINE_DISTANCE) * i;
				break;
			case FireScreen.BOTTOM:
				y = height - txtHeight +((rowHeight+LINE_DISTANCE) * i)-TEXT_BORDER;
				break;	
			}
			
			switch(halign)
			{
			case FireScreen.LEFT:
				x =TEXT_BORDER;
				break;
			case FireScreen.CENTER:
				x = width/2 - font.stringWidth(str)/2;
				break;
			case FireScreen.RIGHT:
				x = width- font.stringWidth(str)-TEXT_BORDER;
				break;
			}
			
			g.setColor(foregroundColor);
			g.drawString(str,x, y, Graphics.TOP | Graphics.LEFT);
		}

		g.setColor(theme.getIntProperty("border.color"));
		g.drawRect(2,2,width-4,height-4);
		
		if(isSelected())
		{
			g.setColor(theme.getIntProperty("link.active.bg.color"));
			g.drawRect(1,1,width-2,height-2);			
		}
	}
	
	private void paintRadioOrCheckbox(Graphics g)
	{
		Theme theme = FireScreen.getTheme();
		int width = 0, height = 0;
		if(getPrefSize()==null){
			width = getWidth();
			height = getHeight();
		}else{
			width = getPrefSize()[0];
			height = getPrefSize()[1];
		}

		if(type==CHECKBOX)
		{
			if(checked)
			{
				g.setColor(theme.getIntProperty("bg.alt1.color"));
				g.fillRect(2,2,width-4,height-4);
				g.setColor(theme.getIntProperty("border.color"));
				g.fillRect(5,5,width-9,height-9);
			}
			else
			{
				if(backgroundColor!=Theme.TRANSPARENT)
				{
					g.setColor(backgroundColor);
					g.fillRect(2,2,width-4,height-4);
				}
			}
			
			g.setColor(theme.getIntProperty("border.color"));
			g.drawRect(2,2,width-4,height-4);
			if(isSelected())
			{
				g.setColor(theme.getIntProperty("link.active.bg.color"));
				g.drawRect(1,1,width-2,height-2);
			}
		}
		else // radiobox
		{
			if(checked)
			{
				g.setColor(theme.getIntProperty("bg.alt1.color"));
				g.fillArc(1,1,width-2,height-2,0,360);
				g.setColor(theme.getIntProperty("border.color"));
				g.fillArc(4,4,width-8,height-8,0,360);
			}
			else
			{
				if(backgroundColor!=Theme.TRANSPARENT)
				{
					g.setColor(backgroundColor);
					g.fillArc(1,1,width-2,height-2,0,360);
				}
			}
			
			if(isSelected())
			{
				g.setColor(theme.getIntProperty("link.active.bg.color"));
				g.drawArc(1,1,width-2,height-2,0,360);
			}
			else g.setColor(theme.getIntProperty("border.color"));
			
			g.drawArc(2,2,width-4,height-4,90,360);
		}
	}

	public void reset()
	{
		if(type==CHECKBOX || type==RADIO) checked=(initialValue!=null);
		else if(initialValue!=null){
			value = initialValue;
			formatedText=null;
		}
		repaint();
	}

	public byte getType()
	{
		return type;
	}

	public void setType(byte type)
	{
		this.type = type;
	}

	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	public int getMaxLen()
	{
		return maxLen;
	}

	public void setMaxLen(int maxLen)
	{
		this.maxLen = maxLen;
	}


	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
		formatedText = null;
		repaint();
	}

	public String getInitialValue()
	{
		return initialValue;
	}

	public void setInitialValue(String initialValue)
	{
		this.initialValue = initialValue;
	}

	public int getTextConstraints()
	{
		return textConstraints;
	}

	public void setTextConstraints(int textConstraints)
	{
		this.textConstraints = textConstraints;
	}
	public void addTextConstraints(int textConstraints)
	{
		this.textConstraints |= textConstraints;
	}

	public int getRows()
	{
		return rows;
	}

	public void setRows(int rows)
	{
		this.rows = rows;
	}

	public void setSelected(boolean v)
	{
		super.setSelected(v);
		repaint();
	}

	public int[] getMinSize()
	{
		if(type==TEXT )
		{
			int w = font.charWidth('W')*size+(TEXT_BORDER*2);
			if(w>maxWidth) w = maxWidth;
			return new int[]{w,font.getHeight()*rows+(TEXT_BORDER*2)};
		}
		else if(type==BUTTON || type==RESET || type==SUBMIT )// type button or reset
		{
			int width = maxWidth-(TEXT_BORDER*2);		
			if(value!=null)
			{
				int tmp = font.stringWidth(value);
				if(tmp<width) width=tmp;
			}	
			return new int[]{width+(TEXT_BORDER*2),font.getHeight()*rows+(TEXT_BORDER*2)};
		}else if(type==SWITCH || type==MENU)
		{
			int width = maxWidth-(SWITCH_BORDER*2);		
			if(text!=null)
			{
				int tmp = font.stringWidth(text);
				if(tmp<width) width=tmp;
			}
			return new int[]{width+(SWITCH_BORDER*2),font.getHeight()*rows+(SWITCH_BORDER*2)};
			
		}
		return new int[]{RADIO_WIDTH,RADIO_HEIGHT};
	}

	public int getContentWidth()
	{
		if (!valid)
			throw new IllegalStateException("The element is not validated.");
		
		return getPrefSize()[0];
	}

	public int getContentHeight()
	{
		if (!valid)
			throw new IllegalStateException("The element is not validated.");
		
		return getPrefSize()[1];
	}
	
	
	protected void pointerReleased(int x, int y)
	{
		if(command!=null && commandListener!=null)
		{
			setSelected(!isSelected());
			if(isSelected() // component just became selected or  
					|| type==BUTTON) // if type==button. Do not act as switch, every pointer event on a button must result to an commandAction event. 
			{
				commandListener.commandAction(command,this);
			}
		}
		super.pointerReleased(x, y);
	}
	
	private boolean keyPressed = false;
	//This is needed because some nokia phones have there "Ok" in the middle, which will case
	//the TextArea to appear again. 
	protected void keyPressed(int keyCode){
		keyPressed = true;
		super.keyPressed(keyCode);
	}
	
	private void handleKeyEvent(int keyCode)
	{
		int key = FireScreen.getScreen().getGameAction(keyCode);
		if((key==Canvas.LEFT|| key==Canvas.DOWN ||key==Canvas.RIGHT || key==Canvas.UP))
		{
			setSelected(!isSelected());
		}
		else if( command!=null && commandListener!=null && (type==TEXT||key==Canvas.FIRE) && keyPressed) 
			commandListener.commandAction(command,this);

		keyPressed = false;		
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
	
	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public boolean isChecked()
	{
		return checked;
	}

	public void setChecked(boolean checked)
	{
		this.checked = checked;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public int getMaxWidth()
	{
		return maxWidth;
	}

	public void setMaxWidth(int maxWidth)
	{
		this.maxWidth = maxWidth;
	}
}
