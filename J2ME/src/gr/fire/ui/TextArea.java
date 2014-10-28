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

import gr.fire.core.CommandListener;
import gr.fire.core.Component;
import gr.fire.core.FireScreen;
import gr.fire.core.Panel;
import gr.fire.util.Lang;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;

/**
 * @author padeler
 *
 */
public class TextArea extends TextBox implements CommandListener
{
	private Component lastComponent;
	private InputComponent owner;
	private Command ok;
	private Command back;
	
	public TextArea(InputComponent owner)
	{
		super("", owner.getValue(), owner.getMaxLen(), owner.getTextConstraints());
		this.owner = owner;
		
		ok = new Command(Lang.get("Ok"),Command.OK,1);
		back = new Command(Lang.get("Back"),Command.BACK,1);
		addCommand(ok);
		addCommand(back);
		setCommandListener(this);
		lastComponent = FireScreen.getScreen().getCurrent();
	}

	public void commandAction(Command cmd, Component c)
	{

	}

	public void commandAction(Command cmd, Displayable d)
	{
		if(cmd==ok)
		{ // update owner's value, return to lastComponent
			owner.setValue(getString());
		}
		// else its back
		FireScreen screen = FireScreen.getScreen();
		screen.setCurrent(lastComponent);
		screen.setSelectedComponent(owner);
		if(lastComponent instanceof Panel)
		{
			((Panel)lastComponent).scrollToSelectedComponent(-1,-1);
		}
		// fully repaint screen (some phones, like nokia s60, need this)
		screen.repaint();
	}
}
