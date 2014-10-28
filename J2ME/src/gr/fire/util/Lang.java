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
 * Created on Sep 16, 2006
 *
 */
package gr.fire.util;

import java.io.InputStream;
import java.util.Hashtable;

/**
 * The Lang class in an easy to use internationallization (i18n) implementation.
 * The default language is English. Every key is the english version of the required text. 
 * If the key string maps to to a translation then the translation is returned. Othewise the key itself is returned.
 * 
 * The class can load files with "key=value" pairs of strings on each line. 
 * The bundles can be loaded (using the FireIO class) either from a file inside the jar, or from a remote location 
 * or from a record in the midlets record store.
 * 
 * The only requirement for a language file to be valid is to contain a key=value pair of the form
 * language_key=IT
 * 
 * For example if a language bundle is the translations to Greek, it should contain one key=value pair of the form
 * language_key=GR
 * 
 * The value part of the pair is not required to be anything specific, it is just an identifier for the given language bundle.
 * 
 * @author padeler
 *
 */
public final class Lang
{
	public static final String defaultLang = "EN";
	
	private static final String languageFile = "language_file";
	private static final String languageKey = "language_key";
	
	private static String lang;
	private static Hashtable bundle=null;
	
	static{
		loadBundle();
		if(lang==null) lang=defaultLang;
	}
	
	private Lang()
	{

	}
	
	/**
	 * Returns a string indicating the current language.
	 * @return
	 */
	public static String getLang()
	{
		return lang;
	}
	
	/**
	 * Sets a resource bundle.
	 * @param lang The string naming the language of the bundle
	 * @param resource The resource stream.
	 * @throws Exception
	 */
	public static void setBundle(InputStream resource) throws Exception
	{
		FireConnector connector = new FireConnector();
		if(resource==null)
		{ // delete current bundle
			try{
				connector.rmsDelete(languageFile);
			}catch(Exception e){}
			Lang.lang=defaultLang;
			Lang.bundle= null;
			return;
		}
		
		Hashtable bundle = StringUtil.loadProperties(resource,'=',"UTF-8");
		String lang = (String)bundle.get(languageKey);
		if(lang==null) // resource does not contain language indentifier.
		{
			Exception e = new Exception("Language resource does not contain "+languageKey+" entry.");
			Log.logError("Lang.setBundle() Failed.",e);
			throw e;
		}		
		Lang.lang = lang;
		Lang.bundle = bundle;
		connector.rmsWrite(languageFile,StringUtil.serializeProperties(bundle,'=',"UTF-8"));
	}
	
	/**
	 * Loads the stored bundle.
	 *
	 */
	public static void loadBundle()
	{
		try{
			InputStream in = (new FireConnector()).rmsRead(languageFile);
			if(in!=null)
			{
				bundle = StringUtil.loadProperties(in,'=',"UTF-8");
				lang = (String)bundle.get(languageKey); 
			}
		}catch(Throwable e)
		{
			Log.logError("Resource Bundle load failed.",e);
		}
	}
	
	/**
	 * Returns the string translation that maps to the key, or the key itself if the translation was not found. 
	 * @param key
	 * @return A translation of key
	 */
	public static String get(String key)
	{
		String res=null;
		if(bundle!=null)
		{
			res = (String)bundle.get(key);
			if(res!=null)
			{
				return res;
			}
		}
		return key;
	}
}