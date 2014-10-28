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

package gr.fire.browser.util;

/**
 * This class extends the standard j2me Command class, with some extra fields for use with the Fire Browser component.
 * 
 * @author padeler
 *
 */
public class Command extends javax.microedition.lcdui.Command
{
	private String url;
	private Form form;
	
	/* Menu command fields */
	private String name=null;
	private boolean multiple=false,enabled=true;
	private int size=5; // number of visible items in the menu.
	private boolean menuCommand=false; // indicates tha this Command instance is used in a menu item (drop down menu)
	
	public Command(String name)
	{
		super("Menu", Command.OK,1);
		this.name=name;
		this.menuCommand=true;
	}
	
	public Command(String label, int commandType, int priority)
	{
		super(label, commandType, priority);
	}
	
	public Command(String label, String longLabel, int commandType, int priority)
	{
		super(label, longLabel, commandType, priority);
	}
	
	
	public Command(String label,int commandType, int priority, String url)
	{
		super(label, commandType, priority);
		this.url=url;
	}
	
	public Command(String label, int commandType, int priority, String url,Form form)
	{
		super(label, commandType, priority);
		this.url=url;
		this.form=form;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public Form getForm()
	{
		return form;
	}

	public void setForm(Form form)
	{
		this.form = form;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean isMultiple()
	{
		return multiple;
	}

	public void setMultiple(boolean multiple)
	{
		this.multiple = multiple;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	public boolean isMenuCommand()
	{
		return menuCommand;
	}

	public void setMenuCommand(boolean menuCommand)
	{
		this.menuCommand = menuCommand;
	}

}
