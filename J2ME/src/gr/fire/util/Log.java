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

package gr.fire.util;

/**
 * Utility class for easily logging on fire application.
 * 
 * It writes the messages sent to it using the Log.logInfo, Log.logWarn, Log.logError and Log.logDebug 
 * methods to one or more predefined Loggers.
 * 
 * The default logger is standard output. The developer can add more loggers or set a different (remove standard out) logger.
 *  
 * @see #setLogDestination(Logger)
 * @see #addLogDestination(Logger)
 * @see Logger
 * @author padeler
 *
 */
public final class Log
{
	/**
	 * Flag to enable or disable debuging information. If false the calls to Log.logDebug will not produce any output.
	 */
	public static boolean showDebug=false;
	
	private static Logger[] out;
	
	static{
		Logger l = new Logger(){
			public void println(String txt)
			{
				System.out.println(txt);
			}
		};
		out = new Logger[]{l};
	}
	
	private Log()
	{
	}
	
	/**
	 * Sets the only logger to be the given.
	 * @param l the new logger. All previous loggers are removed.
	 */
	public static void setLogDestination(Logger l)
	{
		Log.out= new Logger[]{l};
	}
	/**
	 * Adds the given logger to the loggers list. 
	 * All loggers will receive the output.
	 * @param o
	 */
	public static void addLogDestination(Logger o)
	{
		if(o==null) return;
		
		if(out==null){
			out = new Logger[]{o};
			return;
		}
		
		Logger []old =out;
		out = new Logger[old.length+1];
		for(int i =0 ;i<old.length;++i)
		{
			out[i] = old[i];
		}
		out[old.length] = o;
	}
	
	/**
	 * Removes the given logger from the loggers list. 
	 * @param o
	 * @return Returns true if the logger was removed
	 */
	public static boolean removeLogDestination(Logger o)
	{
		if(o==null || out==null) return false;
		
		boolean removed=false;
		
		Logger []old =out;
		for(int i =0 ;i<old.length;++i)
		{
			if(old[i]==o)
			{
				
				old[i]=null;
				
				removed=true;
				break;
			}
		}
		if(removed)
		{
			Logger n[] = new Logger[old.length-1];
			int p=0;
			for(int i=0;i<n.length;++i)
			{
				if(old[p]==null) p++;
				n[i] = old[p];
				p++;
			}
			out=n;
		}
		return removed;
	}
	
	public static void logInfo(String str)
	{
		if(out!=null)
		{
			for(int i=0;i<out.length;++i)
				out[i].println("INFO: "+str);
		}
	}
	
	public static void logError(String str,Throwable e)
	{
		if(out!=null)
		{
			for(int i=0;i<out.length;++i)
			{
				Logger o = out[i];
				o.println("ERROR: "+str);
				if(e!=null)
				{
					o.println("ERROR: "+e.getClass().getName());
					o.println("ERROR: "+e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	public static void logWarn(String str, Throwable e)
	{
		if(out!=null)
		{
			for(int i=0;i<out.length;++i)
			{
				Logger o = out[i];
				o.println("WARN: "+str);
				if(e!=null)
				{
					o.println("WARN: "+e.getClass().getName());
					o.println("WARN: "+e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void logWarn(String str)
	{
		if(out!=null)
		{
			for(int i=0;i<out.length;++i)
				out[i].println("WARN: "+str);
		}
	}
	
	/**
	 * Writes str to all the registed loggers only is the flag {@link #showDebug} is set to true.
	 * @param str
	 */
	public static void logDebug(String str)
	{
		if(showDebug && out!=null)
		{
			for(int i=0;i<out.length;++i)
				out[i].println("DEBUG: "+str);
		}
	}
}