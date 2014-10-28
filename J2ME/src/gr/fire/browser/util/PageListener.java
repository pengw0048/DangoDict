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


import gr.fire.browser.Browser;

import java.util.Hashtable;

/**
 * 
 * A PageListener registered to a Browser instance will be notified upon completion of requests to Browser.loadPageAsync.
 * @see Browser#loadPageAsync(String, String, Hashtable, byte[])
 * @author padeler
 *
 */
public interface PageListener
{
	
	public static final byte PAGE_LOAD_START=0x10;
	public static final byte PAGE_LOAD_LOADING_DATA=0x20;
	public static final byte PAGE_LOAD_PARSING_DATA=0x30;
	public static final byte PAGE_LOAD_END=0x70;
	
	
	/**
	 * Only called when pageLoadAsync is used to load a page.
	 * The rendering of a page was completed. The result is in the Page instance.
	 * @param url The usr that was used when the request was made. This may not be the same with the url in the Page parameter since there may have been redirects.
	 * @param method The method that was used for the Http request
	 * @param requestParams The parameters that where send with the request. 
	 * @param page The result of the request.
	 */
	public void pageLoadCompleted(String url,String method,Hashtable requestParams, Page page);
	
	/**
	 * Only called when pageLoadAsync is used to load a page.
	 * The loadPageAsync failed with an exception. A callback to the PageListener is made with the error.
	 * @param url
	 * @param method
	 * @param requestParams
	 * @param error
	 */
	public void pageLoadFailed(String url,String method,Hashtable requestParams, Throwable error);	
	
	/**
	 * This method is called during the loading and rendering of a page to notify about progress. 
	 * 
	 * The pageLoadProgress method will be called at least 2 times during the progress of the loading.
	 * Once on the loading start, and once on the page completion or abort. 
	 * @param url The requested URL.
	 * @param message A short message. Can be the url or a default status message
	 * @param state A value describing the current status of the process. The listener will receive atleast one
	 * method call with state PAGE_LOAD_START and one with PAGE_LOAD_END ({@link #PAGE_LOAD_START}, {@value #PAGE_LOAD_END}).
	 * @param percent The percentage of the page loaded. This is just an estimated value.
	 */
	public void pageLoadProgress(String url, String message,byte state,int percent);
	
}
