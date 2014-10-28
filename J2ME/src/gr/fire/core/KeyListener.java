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

package gr.fire.core;

/**
 * A KeyListener is notified when a key is pressed, released or repeated on a Component.
 * @author padeler
 *
 */
public interface KeyListener 
{
	/**
	 * The key with keycode=code was pressed while the focus was in component src
	 * @param code
	 * @param src
	 */
	public void keyPressed(int code, Component src);
	/**
	 * The key with keycode=code was released while the focus was in component src
	 * @param code
	 * @param src
	 */
	public void keyReleased(int code, Component src);
	/**
	 * The key with keycode=code was repeated while the focus was in component src
	 * @param code
	 * @param src
	 */
	public void keyRepeated(int code, Component src);
}
