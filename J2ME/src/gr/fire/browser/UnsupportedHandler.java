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
package gr.fire.browser;

import gr.fire.browser.util.Page;
import gr.fire.browser.util.Response;
import gr.fire.ui.Alert;
import gr.fire.util.Lang;
import gr.fire.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * This is a ResponseHandler for all unsupported content types. 
 * It will produce an alert to the user informing him/her that the requested URL 
 * is a file that the Browser does not know how to handle.
 *  
 * @author padeler
 */
public class UnsupportedHandler implements ResponseHandler
{
	public Page handleResponse(Response rsp, Browser browser) throws UnsupportedEncodingException, IOException, Exception
	{
		Page res = new Page(rsp.getURL());
		Log.logWarn("Unsupported content-type: "+rsp.getContentType()+". Requested URL: "+rsp.getURL());
		
		res.setUserMessage(Lang.get("Usupported content type")+" "+rsp.getContentType()+". "+Lang.get("Cannot open file on location")+": "+rsp.getURL());
		res.setUserMessageType(Alert.TYPE_WARNING);
		try{ // the ResponseHandler is responsible to close the connection.
			rsp.close();
		}catch(IOException e){
			Log.logWarn("Failed to close response.",e);
		}
		return res;
	}
}
