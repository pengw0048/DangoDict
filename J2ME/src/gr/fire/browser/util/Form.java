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
import gr.fire.core.BoxLayout;
import gr.fire.core.CommandListener;
import gr.fire.core.Component;
import gr.fire.core.Container;
import gr.fire.core.FireScreen;
import gr.fire.core.KeyListener;
import gr.fire.core.Panel;
import gr.fire.ui.InputComponent;
import gr.fire.ui.TextArea;
import gr.fire.util.Log;
import gr.fire.util.StringUtil;

import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Displayable;

/**
 * 
 * @author padeler
 *
 */
public class Form implements CommandListener,KeyListener
{
	private Vector primitivesVector;
	private String method="get",action,enctype="application/x-www-form-urlencoded";
	private Browser browser;
	private Command formCommand;
	
	private Command menuCommand =null;
	private Panel popupMenu=null;
	
	private CommandListener formListener=this;
	
	public Form(Browser browser, String action, String method,String enctype)
	{
		this.browser = browser;
		this.action=action;//StringUtil.proccessUrl(action);
		this.method=method;
		this.formCommand = new Command("form",Command.OK,1,action,this);
		
		if(enctype!=null) 
			this.enctype=enctype;
		
		primitivesVector = new Vector();
	}
		
	public void addInputComponent(InputComponent p)
	{
		if(p.getType()!=InputComponent.HIDDEN)
		{ // an input primitive intercepts events if it is not HIDDEN
			if(menuCommand!=null)
				p.setCommand(menuCommand);
			else
				p.setCommand(formCommand);
			
			p.setCommandListener(this);
		}
		primitivesVector.addElement(p);
	}
	
	public Vector getSubmitData(InputComponent source)
	{
		// first generate a list of all the parameters.		
		Vector res = new Vector();
		
		for(int i=0;i<primitivesVector.size();++i)
		{
			InputComponent p = ((InputComponent)primitivesVector.elementAt(i));
			String name = p.getName();
			if(name==null) continue; // ignore null named input fields
			int type = p.getType();
			
			if((type==InputComponent.RADIO || type==InputComponent.CHECKBOX || type==InputComponent.SWITCH) && p.isChecked()==false) continue; // ignore unchecked radio and checkboxes
			if((type==InputComponent.SUBMIT || type==InputComponent.RESET || type==InputComponent.MENU) && source!=p) continue;// ignore buttons (reset, submit) except the one that submited the form	
			
			
			String value = p.getValue();
			if(value==null) value="";
			res.addElement(new String[]{name,value});			
		}
		return res;
	}
	
	
	public void reset()
	{
		for(int i=0;i<primitivesVector.size();++i)
		{
			((InputComponent)primitivesVector.elementAt(i)).reset();
		}
	}
	
	private void handleMenuCommand(Command menu,InputComponent src)
	{
		String name = menu.getName();
		if(name==null) // ignore...
			return;

		if(src.getType()==InputComponent.MENU)
		{ // create a panel with the items of this menu and display it
			
			Container cnt = new Container(new BoxLayout(BoxLayout.Y_AXIS));
			int panelWidth=30;
			int panelHeight=0;
			for(int i=0;i<primitivesVector.size();++i)
			{
				InputComponent p = (InputComponent)primitivesVector.elementAt(i);
				if(p!=src && name.equals(p.getName()))
				{ // add to panel
					int[] ms  = p.getMinSize();
					panelHeight += ms[1]; 
					if(ms[0]>panelWidth) panelWidth=ms[0];
					
					cnt.add(p);
				}
			}
			
			popupMenu = new Panel(cnt,Panel.VERTICAL_SCROLLBAR,false);
			popupMenu.setKeyListener(this);
			cnt.setBackgroundColor(FireScreen.getTheme().getIntProperty("bg.alt1.color"));
			popupMenu.setShowBackground(true);
			
			popupMenu.setBorder(true);
			
			int tmp = (src.getPrefSize()[1]*menu.getSize());
			if(panelHeight>tmp)
				panelHeight = tmp;

			popupMenu.setPrefSize(panelWidth,panelHeight);
			FireScreen.getScreen().showPopupOnComponent(popupMenu,src,1);	
		}
		else if(src.getType()==InputComponent.SWITCH) // event from inside the popup menu
		{
			if(menu.isMultiple())
			{
				src.setChecked(!src.isChecked());
				src.repaint();
			}
			else // diselect all other elements and close popup 
			{
				InputComponent menuSwitch =null; 
				for(int i=0;i<primitivesVector.size();++i)
				{
					InputComponent p = (InputComponent)primitivesVector.elementAt(i);
					if(name.equals(p.getName()) && p!=src)
					{
						if(p.isChecked())
						{
							p.setChecked(false);
						}
						if(p.getType()==InputComponent.MENU)
							menuSwitch  = p;
					}
				}
				src.setChecked(true);
				if(popupMenu!=null)
				{
					FireScreen.getScreen().removeComponent(popupMenu);
					popupMenu=null;
				}
				if(menuSwitch!=null) // update the menu button text.
				{
					menuSwitch.setText(src.getText());
					menuSwitch.repaint();
				}
			}
		}
	}

	public void commandAction(javax.microedition.lcdui.Command c, Component cmp)
	{
		InputComponent src = (InputComponent)cmp;
		
		if(c instanceof Command) 
		{
			Command cmd = (Command)c;
			
			if(cmd.isMenuCommand())
			{
				handleMenuCommand(cmd,src);
				return;				
			}
			
			byte type = src.getType();
			if(type==InputComponent.TEXT)
			{
				TextArea ta = new TextArea(src);
				FireScreen.getScreen().setCurrent(ta);
				return;				
			}
			if(type==InputComponent.SUBMIT)
			{
				if(formListener!=null) formListener.commandAction(cmd,src);
				else submit(src);
				return;
			}
			if(type==InputComponent.RESET)
			{
				reset();
				return;
			}
			if(type==InputComponent.CHECKBOX || type==InputComponent.SWITCH)
			{
				src.setChecked(!src.isChecked());
				src.repaint();
				return;
			}
			
			if(type==InputComponent.RADIO)
			{
				if(!src.isChecked())
				{
					String name = src.getName();
					if(name!=null)
					{
						// find all radioboxes in the form, with the same name, and deselect anyone that is selected.
						for(int i=0;i<primitivesVector.size();++i)
						{
							InputComponent prim = (InputComponent)primitivesVector.elementAt(i);
							if(prim.getType()==InputComponent.RADIO && name.equals(prim.getName()) && prim.isChecked())
							{ // deselect this one
								prim.setChecked(false);
								prim.repaint();
							}
						}
					}
					src.setChecked(true);
					src.repaint();
				}
				return;
			}
		}
	}

	public void commandAction(javax.microedition.lcdui.Command arg0, Displayable arg1)
	{
	}

	public Command getMenuCommand()
	{
		return menuCommand;
	}

	public void setMenuCommand(Command newMenu)
	{
		this.menuCommand = newMenu;
	}

	public void keyPressed(int code, Component src)
	{
	}

	public void keyReleased(int code, Component src)
	{
		if(popupMenu!=null && src==popupMenu)
		{
			int ga = FireScreen.getScreen().getGameAction(code);
			if(ga==Canvas.LEFT || ga==Canvas.RIGHT) // close the popup
			{
				FireScreen.getScreen().removeComponent(popupMenu);
				popupMenu=null;
			}
		}
	}

	public void keyRepeated(int code, Component src)
	{
	}

	public Vector getPrimitivesVector()
	{
		return primitivesVector;
	}

	public void submit(InputComponent src)
	{
		byte []data =null;
		Hashtable reqParams = null;
		
		Vector paramsVector = getSubmitData(src);

		StringBuffer paramsBuf = new StringBuffer();
		
		for(int i=0;i<paramsVector.size();++i)
		{
			String []nameVal = (String[])paramsVector.elementAt(i);
			String pair = StringUtil.urlEncode(nameVal[0])+"="+StringUtil.urlEncode(nameVal[1]) + "&";
			paramsBuf.append(pair);
			Log.logDebug("Form-Field: "+nameVal[0]+"="+nameVal[1] +" ==> "+ pair);
		}
		
		if(paramsBuf.length()>0) 
		{
			paramsBuf.deleteCharAt(paramsBuf.length()-1); // delete last &
			
			String params = paramsBuf.toString();
			if(method.equals(HttpConnection.POST))
			{
				data = params.getBytes();
				reqParams = new Hashtable();
				reqParams.put("content-type",enctype);
			}
			else
			{
				if(action.indexOf("?")!=-1) // action url already contains parameters.
					action += "&"+params;
				else
					action += "?"+params;
			}
		}
		
		
		Log.logInfo("Submit of Form ["+method+"]: "+action);
		// ok now send the request to the browser.
		browser.loadPageAsync(action,method,reqParams,data);
	}

	public String getMethod()
	{
		return method;
	}

	public String getAction()
	{
		return action;
	}

	public String getEnctype()
	{
		return enctype;
	}

	public CommandListener getFormListener()
	{
		return formListener;
	}

	public void setFormListener(CommandListener formListener)
	{
		this.formListener = formListener;
	}
	


}