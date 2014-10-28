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
 * A FireListener is notified by the FireScreen when certain events occur. 
 * It is used for events that need special attention by the application such as 
 * when the screen size changes or the application goes to the background.
 * 
 * @author padeler
 *
 */
public interface FireListener
{
	/**
	 * Notify the FireListener that the size of the FireScreen changed. 
	 * This method is called after all the FireScreen internal resize operations are completed.
	 * @param newWidth
	 * @param newHeight
	 */
	public void sizeChanged(int newWidth,int newHeight);
	
	/**
	 * Notify the FireListener that the FireScreen was hidden, for example went to background.
	 * This method is called after all the FireScreen internal hideNotify operations are completed.
	 */
	public void hideNotify();
	/**
	 * Notify the FireListener that the FireScreen was shown, for example it came to foreground.
	 * This method is called after all the FireScreen internal showNotify operations are completed.
	 */
	public void showNotify();
	
}
