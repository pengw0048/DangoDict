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

import gr.fire.util.Log;
import gr.fire.util.StringUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

/**
 * A utility class that represents a cookie. 
 * @author padeler
 *
 */
public class Cookie
{
	public static final String months[] =  { "jan","feb","mar","apr","may","jun","jul","aug","sep","oct","nov","dec"};
	public static final String days[] =  { "Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
	
	private String name;
	private String value;
	private String domain=null;
	private String path=null;
	private Date expires=null;
	private boolean secure=false;
	
	/**
	 * Empty constructor used only for deserialization
	 * @see Cookie#deSerialize(DataInputStream)
	 */
	private Cookie()
	{
	}
	
	/**
	 * Creates a cookie instance from the given cookie string returned by an http request to the given host
	 * @param cookieStr the cookie string
	 * @param host the host that returned the cookie string
	 */
	public Cookie(String cookieStr,String host) 
	{
		Vector avpairs = StringUtil.split(cookieStr,";");
		if(avpairs.size()<1) throw new IllegalArgumentException("Bad cookie, no name=value pair found.");
		
		String nameValue = (String)avpairs.elementAt(0);
		int idx = nameValue.indexOf('=');
		if(idx==-1) throw new IllegalArgumentException("Bad cookie. malformed name=value pair");
		
		name = nameValue.substring(0,idx).trim();
		value = nameValue.substring(idx+1).trim();
		
		
		for(int i=1;i<avpairs.size();++i)
		{
			nameValue = (String)avpairs.elementAt(i);
			String attr,attrValue;
			
			idx = nameValue.indexOf('=');
			if(idx==-1){attr=nameValue.toLowerCase();attrValue=attr;}
			else{
				attr = nameValue.substring(0,idx).trim().toLowerCase();
				attrValue = nameValue.substring(idx+1).trim();
			}
			
			if(attr.length()==0) continue;
			
			if(attr.equals("path"))
			{
				path = attrValue;
			}
			else if(attr.equals("domain"))
			{
				domain = attrValue;
			}
			else if(attr.equals("expires"))
			{
				String expStr = attrValue;
				Vector expV = StringUtil.split(expStr," ");
				if(expV.size()<3) throw new IllegalArgumentException("Bad cookie, malformed expiration date "+expStr);
				try{
					// date is formated : Fri, 31-Dec-2010 23:59:59 GMT
					// but the splitCookies method will remove the day part (Fri,)
					// so the date is formated 31-Dec-2010 23:59:59 GMT
					String date,time;
					
					if(expV.size()>3)
					{ // Date is seperated by spaces not dashes '-'
						date =  (String)expV.elementAt(0)+"-"+(String)expV.elementAt(1)+"-"+(String)expV.elementAt(2);
						time = (String)expV.elementAt(3);
					}
					else
					{
						date = (String)expV.elementAt(0);
						time = (String)expV.elementAt(1);	
					}
					
					Calendar cal = Calendar.getInstance();
					cal.setTimeZone(TimeZone.getTimeZone("GMT"));
					
					// parse date
					expV = StringUtil.split(date,"-");
					cal.set(Calendar.YEAR,Integer.parseInt((String)expV.elementAt(2)));
					
					String m = ((String)expV.elementAt(1)).toLowerCase();
					int month=0;
					for(month =0;month<months.length;++month)
						if(months[month].startsWith(m)) break;
					
					cal.set(Calendar.MONTH,month);
					cal.set(Calendar.DAY_OF_MONTH,Integer.parseInt((String)expV.elementAt(0)));
					
					//parse time
					expV = StringUtil.split(time,":");
					cal.set(Calendar.HOUR,Integer.parseInt((String)expV.elementAt(0)));
					cal.set(Calendar.MINUTE,Integer.parseInt((String)expV.elementAt(1)));
					cal.set(Calendar.SECOND,Integer.parseInt((String)expV.elementAt(2)));
					expires = cal.getTime();
					
				}catch(Exception e)
				{
					Log.logWarn("Failed to parse expiration date of cookie.",e);
					throw new IllegalArgumentException("Bad cookie, malformed expiration date "+expStr);
				}
			}
			else if(attr.equals("secure"))
			{
				secure=true;
			}
			else
			{
				Log.logDebug("Unknown cookie attribute "+nameValue);
			}
		}
		if(path==null) path="/";
		if(domain==null)
		{
			// get the domain name from the host.
			// 
			Vector v = StringUtil.split(host,".");
			if(v.size()<3) domain = host;
			else
			{
				domain="."+v.elementAt(v.size()-2)+"."+v.elementAt(v.size()-1);
			}
		}
	}
	
	
	public boolean equals(Object obj)
	{
		if(obj instanceof Cookie)
		{
			Cookie c = (Cookie)obj;
			return c.name.equals(name) && c.domain.equals(domain) && c.path.equals(path);
		}
		return false;
	}
	
	/**
	 * From a string that contains one or more cookies this method creates a Vector with Cookie instances.<br/>
	 * 
	 * The string can have the following form:
	 * COOKIE1_NAME=COOKIE1_VALUE;ATTR1=ATTR1_VAL;ATTR2=ATTR2_VAL, COOKIE2_NAME=COOKIE2_VALUE;ATTR1=ATTR1_VAL;ATTR2=ATTR2_VAL
	 * 
	 * @param cookieStr
	 * @return
	 */
	public static Vector splitCookies(String cookieStr)
	{
		Vector res = new Vector();
		
		// remove all day strings from the string
		StringUtil tokenizer = new StringUtil(cookieStr);
		StringBuffer cookie=new StringBuffer();
		
		String token;
		while((token = tokenizer.nextToken(','))!=null)
		{
			boolean inDay=false;
			for(int i =0 ;i<days.length;++i)
			{
				if(token.endsWith(days[i]))
				{
					token = token.substring(0,token.length()-days[i].length());
					inDay=true;
					break;
				}
			}
			cookie.append(token);
			
			if(!inDay)
			{
				res.addElement(cookie.toString());
				cookie = new StringBuffer();
			}
		}
		return res;
	}
	
	/**
	 * Writes the given cookie to the supplied data output stream.
	 * @param c
	 * @param out
	 * @throws IOException
	 */
	public static void serialize(Cookie c,DataOutputStream out) throws IOException
	{
		out.writeUTF(c.name);
		out.writeUTF(c.value);
		out.writeUTF(c.domain);
		out.writeUTF(c.path);
		if(c.expires!=null)
			out.writeLong(c.expires.getTime());
		else 
			out.writeLong(0L);
		out.writeBoolean(c.secure);
	}
	
	/**
	 * Reads a serialized Cookie from the supplied DataInputStream, and returns a new instance of that Cookie.
	 * The serialized cookie must have been serialized using the {@link #serialize(Cookie, DataOutputStream)}.
	 *  
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static Cookie deSerialize(DataInputStream in) throws IOException
	{
		Cookie c = new Cookie();
		c.name = in.readUTF();
		c.value= in.readUTF();
		c.domain=in.readUTF();
		c.path = in.readUTF();
		long exp = in.readLong();
		if(exp==0L) c.expires=null;
		else c.expires = new Date(exp);
		c.secure = in.readBoolean();
		return c;
	}
	
	/**
	 * Checks if this Cookie mathes the given domain and path.
	 * @param withDomain
	 * @param withPath
	 * @return
	 */
	public boolean match(String withDomain,String withPath)
	{
		return (withDomain.endsWith(domain) && withPath.startsWith(path));
	}
	
	public String toString()
	{
		return name+"="+value;
	}

	public String getName()
	{
		return name;
	}

	public String getValue()
	{
		return value;
	}

	public String getDomain()
	{
		return domain;
	}

	public String getPath()
	{
		return path;
	}

	public Date getExpires()
	{
		return expires;
	}

	public boolean isSecure()
	{
		return secure;
	}
}
