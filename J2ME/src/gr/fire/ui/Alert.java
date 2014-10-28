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


import gr.fire.browser.util.Command;
import gr.fire.core.BoxLayout;
import gr.fire.core.CommandListener;
import gr.fire.core.Component;
import gr.fire.core.Container;
import gr.fire.core.FireScreen;
import gr.fire.core.GridLayout;
import gr.fire.core.Panel;
import gr.fire.core.Theme;
import gr.fire.util.Lang;
import gr.fire.util.Log;

import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;

/**
 * The Alert is a utility class that displays an alert box (or popup message).
 * It is basically a Panel with some extra functionallity to make it easy to use as an Alert windo.
 *   
 * @author padeler
 *
 */
public class Alert extends Container implements CommandListener
{
	/**
	 * Alert message with an OK button to close.
	 */
	public static final byte TYPE_INFO=0x01;
	
	public static final byte TYPE_WARNING=0x02;
	public static final byte TYPE_ERROR=0x03;
	/**
	 * Question with two buttons, Yes and No.
	 */
	public static final byte TYPE_YESNO=0x04;
	/**
	 * Question with three buttons, Yes and No and Cancel.
	 */	
	public static final byte TYPE_YESNOCANCEL=0x05;
	/**
	 * Alert with only a cancel button. 
	 * Used for blocking operations that can be canceled.
	 */
	public static final byte TYPE_CANCEL=0x06;
	
	
	public static final byte USER_SELECTED_OK=0x01;
	public static final byte USER_SELECTED_YES=0x02;
	public static final byte USER_SELECTED_NO=0x03;
	public static final byte USER_SELECTED_CANCEL=0x04;
	
	
	private Command yes,no,ok,cancel;
	private byte type=0x00,userSelection=0x00;
	
	private Component selectedButton;
	private Component lastComponent;
	
	private int alertIndex = 4;
	private int shadedBackgroundIndex=3;
	private boolean shadeBackground=true;
	
	/**
	 * Constructs an Alert
	 * @param txt The message of the alert 
	 * @param icon The icon displayed
	 * @param type The type of the alert (TYPE_INFO, TYPE_WARNING , TYPE_ERROR, TYPE_YESNO, TYPE_YESNOCANCEL)
	 * @param defaultSelection The preselected button (USER_SELECTED_OK, USER_SELECTED_NO, USER_SELECTED_CANCEL)
	 * 
	 */
	public Alert(String txt,Image icon, byte type, byte defaultSelection)
	{
		super(new BoxLayout(BoxLayout.Y_AXIS));
				
		if(txt==null) throw new NullPointerException("Alert message cannot be null.");
		
		Container buttonCnt=null;
		
		this.type=type;
		FireScreen screen = FireScreen.getScreen();
		lastComponent = screen.getSelectedComponent();
		
		int screenWidth = screen.getWidth();
		int screenHeight = screen.getHeight();
		
		int height=(screenHeight/6),width=((screenWidth*3)/4);
		
		Container messageCnt = new Container(new BoxLayout(BoxLayout.X_AXIS));
		messageCnt.setBorder(true);
		Theme theme = FireScreen.getTheme();
		try{
			InputComponent button;
			switch(type)
			{
			case TYPE_WARNING:

				buttonCnt = new Container(new GridLayout(1,1));
				button= new InputComponent(InputComponent.BUTTON);
				ok = new Command(Lang.get("Ok"),Command.OK,1);
				button.setValue(ok.getLabel());
				button.setMaxWidth(width);
				button.setCommand(ok);
				button.setSelected(true);
				selectedButton = button;
				
				button.setForegroundColor(FireScreen.getTheme().getIntProperty("alert.fg.color"));
				button.setBackgroundColor(FireScreen.getTheme().getIntProperty("alert.bg.color"));
				
				button.setLayout(FireScreen.CENTER|FireScreen.VCENTER);
				button.setCommandListener(this);
				buttonCnt.add(button);
				buttonCnt.setPrefSize(width,button.getMinSize()[1]);
				break;
			case TYPE_CANCEL:
				buttonCnt = new Container(new GridLayout(1,1));
				button= new InputComponent(InputComponent.BUTTON);
				cancel = new Command(Lang.get("Cancel"),Command.CANCEL,1);
				button.setValue(cancel.getLabel());
				button.setMaxWidth(width);
				button.setCommand(cancel);
				button.setSelected(true);
				selectedButton = button;
				
				button.setForegroundColor(FireScreen.getTheme().getIntProperty("alert.fg.color"));
				button.setBackgroundColor(FireScreen.getTheme().getIntProperty("alert.bg.color"));
				
				button.setLayout(FireScreen.CENTER|FireScreen.VCENTER);
				button.setCommandListener(this);
				buttonCnt.add(button);
				buttonCnt.setPrefSize(width,button.getMinSize()[1]);
				break;
			case TYPE_ERROR:
				buttonCnt = new Container(new GridLayout(1,1));
				button= new InputComponent(InputComponent.BUTTON);
				ok = new Command(Lang.get("Ok"),Command.OK,1);
				button.setValue(ok.getLabel());
				button.setMaxWidth(width);
				button.setCommand(ok);
				button.setSelected(true);
				selectedButton = button;
				
				button.setForegroundColor(FireScreen.getTheme().getIntProperty("alert.fg.color"));
				button.setBackgroundColor(FireScreen.getTheme().getIntProperty("alert.bg.color"));
				
				button.setLayout(FireScreen.CENTER|FireScreen.VCENTER);
				button.setCommandListener(this);
				buttonCnt.add(button);
				buttonCnt.setPrefSize(width,button.getMinSize()[1]);
				break;
			case TYPE_YESNO:
				buttonCnt = new Container(new GridLayout(1,2));
				button= new InputComponent(InputComponent.BUTTON); // yes button
				yes = new Command(Lang.get("Yes"),Command.OK,1);
				button.setValue(yes.getLabel());
				button.setMaxWidth(width/2);
				button.setCommand(yes);
				if(defaultSelection==USER_SELECTED_YES)
				{
					button.setSelected(true);
					selectedButton = button;
				}
				
				button.setForegroundColor(FireScreen.getTheme().getIntProperty("alert.fg.color"));
				button.setBackgroundColor(FireScreen.getTheme().getIntProperty("alert.bg.color"));
				
				button.setLayout(FireScreen.CENTER|FireScreen.VCENTER);
				button.setCommandListener(this);
				buttonCnt.add(button);
				button= new InputComponent(InputComponent.BUTTON); // no button
				no= new Command(Lang.get("No"),Command.OK,1);
				button.setValue(no.getLabel());
				button.setMaxWidth(width/2);
				button.setCommand(no);
				if(defaultSelection==USER_SELECTED_NO)
				{
					button.setSelected(true);
					selectedButton = button;
				}
				
				button.setForegroundColor(FireScreen.getTheme().getIntProperty("alert.fg.color"));
				button.setBackgroundColor(FireScreen.getTheme().getIntProperty("alert.bg.color"));
				
				button.setLayout(FireScreen.CENTER|FireScreen.VCENTER);
				button.setCommandListener(this);
				buttonCnt.add(button);
				buttonCnt.setPrefSize(width,button.getMinSize()[1]);
				break;
			case TYPE_YESNOCANCEL:
				buttonCnt = new Container(new GridLayout(1,3));
				button= new InputComponent(InputComponent.BUTTON); // yes button
				yes = new Command(Lang.get("Yes"),Command.OK,1);
				button.setValue(yes.getLabel());
				button.setMaxWidth(width/2);
				button.setCommand(yes);
				if(defaultSelection==USER_SELECTED_YES)
				{
					button.setSelected(true);
					selectedButton = button;
				}

				button.setForegroundColor(FireScreen.getTheme().getIntProperty("alert.fg.color"));
				button.setBackgroundColor(FireScreen.getTheme().getIntProperty("alert.bg.color"));
				
				button.setLayout(FireScreen.CENTER|FireScreen.VCENTER);
				button.setCommandListener(this);
				buttonCnt.add(button);
				button= new InputComponent(InputComponent.BUTTON); // no button
				no= new Command(Lang.get("No"),Command.OK,1);
				button.setValue(no.getLabel());
				button.setMaxWidth(width/2);
				button.setCommand(no);
				if(defaultSelection==USER_SELECTED_NO)
				{
					button.setSelected(true);
					selectedButton = button;
				}

				button.setForegroundColor(FireScreen.getTheme().getIntProperty("alert.fg.color"));
				button.setBackgroundColor(FireScreen.getTheme().getIntProperty("alert.bg.color"));
				
				button.setLayout(FireScreen.CENTER|FireScreen.VCENTER);
				button.setCommandListener(this);
				buttonCnt.add(button);
				button= new InputComponent(InputComponent.BUTTON); // cancel button
				cancel = new Command(Lang.get("Cancel"),Command.OK,1);
				button.setValue(cancel.getLabel());
				button.setMaxWidth(width/2);
				button.setCommand(cancel);
				if(defaultSelection==USER_SELECTED_CANCEL)
				{
					button.setSelected(true);
					selectedButton = button;
				}

				button.setForegroundColor(FireScreen.getTheme().getIntProperty("alert.fg.color"));
				button.setBackgroundColor(FireScreen.getTheme().getIntProperty("alert.bg.color"));				
				
				button.setLayout(FireScreen.CENTER|FireScreen.VCENTER);
				button.setCommandListener(this);
				buttonCnt.add(button);
				buttonCnt.setPrefSize(width,button.getMinSize()[1]);
				break;			
			default: // default is Information alert.
				buttonCnt = new Container(new GridLayout(1,1));
				button= new InputComponent(InputComponent.BUTTON);
				ok = new Command(Lang.get("Ok"),Command.OK,1);
				button.setValue(ok.getLabel());
				button.setMaxWidth(width);
				button.setCommand(ok);
				button.setSelected(true);
				selectedButton = button;
				
				button.setForegroundColor(FireScreen.getTheme().getIntProperty("alert.fg.color"));
				button.setBackgroundColor(FireScreen.getTheme().getIntProperty("alert.bg.color"));
				
				button.setLayout(FireScreen.CENTER|FireScreen.VCENTER);
				button.setCommandListener(this);
				buttonCnt.add(button);
				buttonCnt.setPrefSize(width,button.getMinSize()[1]);
				break;
			}
		}catch(Exception e){
			Log.logWarn("Failed to create Alert.",e);
		}
		
		int w = width;
		
		if(icon!=null)
		{
			ImageComponent iconCmp = new ImageComponent(icon,"");
			iconCmp.setLayout(FireScreen.CENTER|FireScreen.TOP);
			iconCmp.validate();
			int th = icon.getHeight();
			if(th>height)
				height=th;
			w -=icon.getWidth();
			messageCnt.add(iconCmp);
		}

		TextComponent txtCmp = new TextComponent(txt,w);
		txtCmp.setFont(theme.getFontProperty("alert.font"));
		txtCmp.setLayout(FireScreen.LEFT|FireScreen.BOTTOM);
		txtCmp.validate();
		messageCnt.add(txtCmp);
		int th = txtCmp.getContentHeight()+8;
		if(th>height) height=th;
		messageCnt.setPrefSize(width,height);
		
		int maxHeight = ((3*FireScreen.getScreen().getHeight())/4);
		if(height>maxHeight) height=maxHeight;
		Panel messagePanel = new Panel(messageCnt,Panel.VERTICAL_SCROLLBAR,false);
		messagePanel.setShowBackground(true);
		messagePanel.setPrefSize(width,height);

		add(messagePanel);
		add(buttonCnt);
		
		setPrefSize(width,(height+buttonCnt.getPrefSize()[1]));
	}

	public byte getType()
	{
		return type;
	}

	public void commandAction(javax.microedition.lcdui.Command cmd, Component c)
	{
		if(cmd==ok)
		{
			userSelection=USER_SELECTED_OK;
		}
		else if(cmd==yes)
		{
			userSelection=USER_SELECTED_YES;
		}
		else if(cmd==no)
		{
			userSelection=USER_SELECTED_NO;
		}
		else if(cmd==cancel)
		{
			userSelection=USER_SELECTED_CANCEL;
		}
		
		FireScreen.getScreen().removeComponent(alertIndex);
		if(shadeBackground)
			FireScreen.getScreen().removeComponent(shadedBackgroundIndex);
		
		if(lastComponent != null){
			lastComponent.setSelected(true);
			FireScreen.getScreen().setSelectedComponent(lastComponent);
		}
		
		if(command!=null && commandListener!=null) commandListener.commandAction(command,this); 
	}
	
	public void commandAction(javax.microedition.lcdui.Command arg0, Displayable arg1)
	{
	}

	public byte getUserSelection()
	{
		return userSelection;
	}

	public Component getSelectedButton()
	{
		return selectedButton;
	}

	public void setSelectedButton(Component selectedButton)
	{
		this.selectedButton = selectedButton;
	}

	/**
	 * @return the alertIndex
	 */
	public int getAlertIndex()
	{
		return alertIndex;
	}

	/**
	 * @param alertIndex the alertIndex to set
	 */
	public void setAlertIndex(int alertIndex)
	{
		this.alertIndex = alertIndex;
	}

	/**
	 * @return the shadedBackgroundIndex
	 */
	public int getShadedBackgroundIndex()
	{
		return shadedBackgroundIndex;
	}

	/**
	 * @param shadedBackgroundIndex the shadedBackgroundIndex to set
	 */
	public void setShadedBackgroundIndex(int shadedBackgroundIndex)
	{
		this.shadedBackgroundIndex = shadedBackgroundIndex;
	}

	/**
	 * @return the shadeBackground
	 */
	public boolean isShadeBackground()
	{
		return shadeBackground;
	}

	/**
	 * @param shadeBackground the shadeBackground to set
	 */
	public void setShadeBackground(boolean shadeBackground)
	{
		this.shadeBackground = shadeBackground;
	}

}