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

/*
 * Created on Aug 25, 2006
 */
package gr.fire.core;



import javax.microedition.lcdui.Command;


/**
 * All command events on the FireEngine components are passed to CommandListeners. 
 * The engine does not make a distinction between Commands on components or softkey/popup commands.
 * When a command is fired, the Listener is notified for the command as well as the Component that fired it. 
 * @author padeler
 *
 */
public interface CommandListener extends javax.microedition.lcdui.CommandListener
{
	
	/**
	 * Informs the listener about a Command Action.
	 * @param cmd the command that was fired 
	 * @param c the component that fired the command
	 */
	public void commandAction(Command cmd, Component c);
}
