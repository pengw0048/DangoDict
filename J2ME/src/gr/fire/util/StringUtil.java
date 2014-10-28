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
 * Created on May 17, 2006
 */
package gr.fire.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Font;

import org.kxml2.io.KXmlParser;

/**
 * StringUtil is a utility class that splits a string into lines, according to a specified width. 
 * Up until Fire v1.2 this class was called FString 
 * @author padeler
 *
 */
public final class StringUtil
{	
	private String src;
	private int pos=0;

	public StringUtil(String src)
	{
		if(src==null) throw new IllegalStateException("I need a string to work with.");
		this.src=src;
		this.pos=0;
	}
	
	public String nextToken(char delim)
	{
		StringBuffer buf=new StringBuffer();
		int len = src.length();
		
		for(;pos<len;++pos)
		{
			char c = src.charAt(pos);
			if(c==delim)
			{ 
				do{ // skip all consequent delims
					++pos;
				}while(pos<len && src.charAt(pos)==delim);
				break;
			}
			buf.append(c);
		}
		if(buf.length()==0) return null; // indicate there are no more tokens by returning null
		return buf.toString();	// return the token.
	}
	
	public void skipWhiteChars()
	{
		char c;
		for(;pos<src.length();++pos) // skip white characters at start
		{
			c = src.charAt(pos);
			if(c!=' ' && c!='\t' && c!='\n') break;
		}
	}
	
	public char peakNextChar()
	{
		if(pos<src.length()) return src.charAt(pos);
		else return '\0';
	}
	
	public Integer nextInteger()
	{
		skipWhiteChars();
		char c= peakNextChar();
		
		if(c<'0' || c>'9')  return null; // not a digit.
		
		int start = pos;
		++pos;
		for(;pos<src.length();++pos) // find the last digit of the number
		{
			c = src.charAt(pos);
			if(c<'0' || c>'9') break;// not a digit.	
		}
		
		return new Integer(Integer.parseInt(src.substring(start,pos)));
	}
	
	public String nextWord()
	{
		skipWhiteChars();
		char c= peakNextChar();
		
		if(c=='\0') // no more words
			return null;
		
		int start = pos;
		++pos;
		for(;pos<src.length();++pos) // a word is a series of characters that are not white characters
		{
			c = src.charAt(pos);
			if(c==' ' || c=='\t' || c=='\n') break;// end of word.
		}
		return src.substring(start,pos);
	}
	
	public String lastToken()
	{
		if(pos<src.length()) return src.substring(pos);
		return null;
	}
	
	public static String proccessUrl(String action, boolean acceptSpace)
	{
		action = trimStart(action);
		StringBuffer buf = new StringBuffer(action.length());
		StringBuffer entity= null;
		char c;
		for(int i =0;i<action.length();++i)
		{
			c = action.charAt(i);
			if(!acceptSpace && c==' ') c='+';
			if(c=='&')
			{
				if(entity!=null) // ignore last &
					buf.append("&"+ entity.toString());
				
				entity = new StringBuffer();
			}
			else if(c==';' && entity!=null)
			{ // end of entity. replace.
				String ent = entity.toString();
				String rep = (String)KXmlParser.entityMap.get(ent);
				
				if(rep!=null) buf.append(rep);
				else 
				{ 
					if(ent.charAt(0)=='#') // its the numerical representation of the character
					{
						try{buf.append((char)Integer.parseInt(ent.substring(1)));}catch(NumberFormatException e){
							Log.logWarn("Failed to convert &#"+ent+"; to character.",e);
						}
					}
					else buf.append("&"+ent+";"); // ignore
				}
				entity=null;
			}
			else if(entity!=null) entity.append(c);
			else buf.append(c);
		}
		
		if(entity!=null) buf.append("&"+ entity.toString());
		
		String res = buf.toString();
		return res;
	}
	
	public static String trimStart(String str)
	{
		int pos = 0;
		for(;pos<str.length();++pos)
		{
			char c = str.charAt(pos);
			if(c!=' ' && c!='\t' && c!='\n') break;
		}
		return str.substring(pos);
	}

	
	public static String urlEncode(String str)
	{
		StringBuffer buf = new StringBuffer();
		byte c;
		byte[] utfBuf;
		try
		{
			utfBuf = str.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e)
		{
			Log.logWarn("URLEncode: Failed to get UTF-8 bytes from string.",e);
			utfBuf = str.getBytes();
		}
		for(int i = 0; i < utfBuf.length; i++)
		{ 
			c = utfBuf[i];
			if ((c >= '0' && c <= '9')||
				(c >= 'A' && c <= 'Z')||
				(c >= 'a' && c <= 'z')||
				(c=='.' || c=='-' || c=='*' || c=='_'))
			{
				buf.append((char)c);
			}
			else
			{	
				buf.append("%").append(Integer.toHexString((0x000000FF&c)));	
			}
		}
		return buf.toString();
	}

	
	/**
	 * Splits a String into lines of at most normalWidth pixels. 
	 */
	public static Vector format(String txt,Font font,int startWidth, int normalWidth)
	{
		
		Vector formatedText = new Vector();		
		
		int minWidth = font.charWidth('W');
		if(normalWidth<minWidth || txt==null || txt.length()==0)
		{ // den xoraei tpt, den emfanizoume tpt.
			return formatedText;
		}
		
		Vector words = split(txt," \n\t\r");
		
		String word;
		StringBuffer line = new StringBuffer();
		int length=0,tl=0;
		// for the first line we use the startWidth, for the rest of the lines we use normalWidth
		int width = startWidth;
		int spaceLength = font.stringWidth(" ");
		for(int w=0;w<words.size();++w)
		{
			word = (String)words.elementAt(w);// get the word
			tl = font.stringWidth(word); // find out the word's length
			
			if(length + tl<width) // the word can be added in the current line
			{
				line.append(word);
				line.append(" ");
				length +=tl+spaceLength;
			}
			else // we need to start a new line
			{
				if(line.length()>0) // first start a new line
				{
					formatedText.addElement(line.toString());
					line.delete(0,line.length()); // reset the string buffer
					width = normalWidth;	
					length=0;
				}
				
				if(tl<=width)
				{
					line.append(word);
					line.append(" ");
					length = tl+spaceLength;
				}
				else //tl>width the word is too big, I must split it.
				{
					if(width<normalWidth) 
					{ // fix, for lines that start intended (that is startWidth<normalWidth)
						formatedText.addElement(""); // add empty line
						width = normalWidth;
						length=0; // reset line length
						w--; // recheck the same word.
						continue;
					}
					else // split the word into two or more lines.
					{	
						int l=0,cw;
						StringBuffer tmpWord= new StringBuffer();
						for(int i=0;i<word.length();++i)
						{
							char c =word.charAt(i);
							cw = font.charWidth(c);
							l += cw;
							if(l<width)
							{
								tmpWord.append(c);
							}
							else
							{
								l = cw;
								formatedText.addElement(tmpWord.toString()); // add the line
								tmpWord.delete(0,tmpWord.length()); // reset buffer
								tmpWord.append(c); // continue with the new line
							}
						}
						word = tmpWord.toString(); // final part of the word.
						line.append(word);
						line.append(" ");
						tl = font.stringWidth(word)+spaceLength;
						length=tl;
					}
				}
			}
		}
		if(line.length()>0) formatedText.addElement(line.toString()); // add last line
		if(formatedText.size()>0)
		{
			int pos = formatedText.size()-1;
			String lastLine = (String)formatedText.elementAt(pos);
			lastLine = lastLine.substring(0,lastLine.length()-1); // remove the last " " (space) from the last line
			formatedText.setElementAt(lastLine,pos);
		}
		return formatedText;
	}
	
	public static Vector split(String txt,String delim)
	{
		Vector result = new Vector();
		char []delims = delim.toCharArray();
		StringBuffer resBuf = new StringBuffer();
		
		if(contains(txt.charAt(0),delims)) resBuf.append(' ');
		
		int i=0;
		boolean split=false;
		char c=0;
		while(i<txt.length())
		{
			while(i<txt.length() && contains((c = txt.charAt(i)),delims))
			{
				split=true; // word ended
				i++;
			}			
			if(split)
			{
				split =false;
				if(resBuf.length()>0)
				{
					String word = resBuf.toString();
					result.addElement(word); // add word to the result vector.
					resBuf = new StringBuffer(); // prepare a new stringbuffer for the next word
					resBuf.append(c);
				}
			}
			else resBuf.append(c);
			i++;
		}
		
		if(resBuf.length()>0){
			if(!contains(resBuf.charAt(0),delims)) // its not a last delim. 
				result.addElement(resBuf.toString()); // add last word
		}
		
		// if a string ends with white character then the last word should have a white character two
		// this is to implement functionality such as:
		// <p>A paragraph containing an <b>inline element</b>.</p>
		// It the above example the space after the "an" should not be ommited.
		if(result.size()>0 && contains(txt.charAt(txt.length()-1),delims))
		{
			String last = (String)result.lastElement();
			result.removeElementAt(result.size()-1);
			result.addElement(last+" ");
		}
		
		return result;
	}
	
	public static Hashtable loadProperties(InputStream in,char delim,String encoding) throws IOException,UnsupportedEncodingException
	{
		Hashtable result = new Hashtable();
		InputStreamReader rin = new InputStreamReader(in,encoding);
		int c=0,idx;
		StringBuffer strBuf=new StringBuffer();
		
		while(c!=-1)
		{
			strBuf.delete(0,strBuf.length()); // clear the buffer
			while((c=rin.read())!=-1 && c!='\n')
			{
				strBuf.append((char)c);
			}
			String line = strBuf.toString().trim();
			if(line.length()==0 || line.startsWith("#")) // comment or empty line
				continue;
			
			idx = line.indexOf(delim);
			if(idx==-1) // no delimiter found. ignore line.
			{
				Log.logWarn("Malformed line found while parsing properties: "+line);
				continue;
			}
			// add key,value pair to result map
			result.put(line.substring(0,idx),line.substring(idx+1));
		}		
		
		return result;
	}
	
	public static byte[] serializeProperties(Hashtable properties,char delim,String encoding) throws IOException,UnsupportedEncodingException
	{
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		OutputStreamWriter out = new OutputStreamWriter(bout,encoding);
		
		Enumeration keys = properties.keys();
		while(keys.hasMoreElements())
		{
			String k = (String)keys.nextElement();
			String v = (String)properties.get(k);
			out.write(k+delim+v+'\n');
		}
		return bout.toByteArray();
	}
	
	public static boolean contains(char c,char[]array)
	{
		for(int i=0;i<array.length;++i)
		{
			if(c==array[i]) return true;
		}
		return false;
	}
	
}