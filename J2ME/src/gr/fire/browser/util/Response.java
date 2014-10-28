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

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.Hashtable;

import javax.microedition.io.HttpConnection;


/**
 * 
 * This method wraps a request made with the HttpClient. It can handle both requests to local files (in recordstore or the jar file) 
 * as well as requests to external resource via http.
 * 
 * @author padeler
 *
 */
public class Response
{
	public static final String defaultEncoding="UTF-8";
	public static final String defaultContentType="text/html";
	
	public static final Hashtable knownContentTypes = new Hashtable();
	
	private boolean canceled=false;
	
	private boolean closed=false;
	
	static{
		knownContentTypes.put("html","text/html");
		knownContentTypes.put("xhtml","text/html");
		knownContentTypes.put("htm","text/html");
		
		knownContentTypes.put("jpg","image/jpeg");
		knownContentTypes.put("jpeg","image/jpeg");
		knownContentTypes.put("gif","image/gif");
		knownContentTypes.put("png","image/png");
		
		knownContentTypes.put("3gp","video/3gpp");
		knownContentTypes.put("mpg","video/mpeg");
		knownContentTypes.put("avi","video/mpeg");
		knownContentTypes.put("mpeg","video/mpeg");
		
		knownContentTypes.put("mp3","audio/x-mp3");
		knownContentTypes.put("mp3","audio/mp3");
		knownContentTypes.put("arm","audio/amr");
		knownContentTypes.put("awb","audio/amr-wb");
		knownContentTypes.put("wav","audio/x-wav");		
		knownContentTypes.put("mid","audio/x-midi");
		knownContentTypes.put("kar","audio/x-midi");
		knownContentTypes.put("jts","audio/x-tone-seq");
		
		knownContentTypes.put("jar","application/java");
		knownContentTypes.put("sis","application/vnd.symbian.install");
		knownContentTypes.put("thm","application/vnd.eri.thm");
	}

	private String host;
	private String protocol;
	private String query;
	private String file;
	private int port=80;
	
	private String encoding;
	private String contentType;
	

	private HttpConnection connection=null;
	private InputStream in=null;
	

	
	public Response(String contentType, String encoding, InputStream in)
	{
		this("localhost","file","unnamed",contentType,encoding,in);
	}
	
	/**
	 * Default constructor for text/html responses.
	 * @param encoding the encoding of the html document
	 * @param in
	 */
	public Response(String encoding, InputStream in)
	{
		this("localhost","file","unnamed",defaultContentType,encoding,in);
	}

	public Response(String host, String protocol, String file,InputStream in)
	{
		this(host,protocol,file,guessContentType(file),defaultEncoding,in);
	}
	
	public Response(String host, String protocol, String file,String contentType, String encoding, InputStream in)
	{
		this.host = host;
		this.protocol = protocol;
		this.file =  file;
		this.encoding = encoding;
		this.contentType = contentType;
		this.in=in;
	}	

	public Response(HttpConnection conn) throws IOException
	{
		setConnection(conn);
	}
	
	public Response() 
	{
		
	}
	
	public void setConnection(HttpConnection conn) throws IOException
	{
		if(closed) throw new InterruptedIOException("Response is closed.");
		if(closed) throw new InterruptedIOException("Response is canceled.");
		
		protocol = conn.getProtocol();
		host = conn.getHost();
		port = conn.getPort();
		query = conn.getQuery();
		file = conn.getFile();
		this.connection=conn;

		String type=null;
		try
		{
			type = conn.getHeaderField("Content-Type");
		} catch (IOException e)
		{
			Log.logError("Failed to read header field Content-Type",e);
		}
		
		encoding = conn.getEncoding();
		if(encoding==null) 
		{
			// else check the content-type field
			if(type!=null) 
			{
				int idx = type.indexOf("charset=");
				if(idx>-1){
					encoding = type.substring(idx+8);
					idx = encoding.indexOf(";");
					if(idx>-1) // cut everything after the semicolon.
						encoding = encoding.substring(0,idx);
					encoding = encoding.toUpperCase();
				}
				else encoding = defaultEncoding;
			}
			else encoding = defaultEncoding;
		}
		else 
		{
			encoding = encoding.toUpperCase();
			if(encoding.equals("UTF_8") || encoding.equals("UTF8"))
			{// UTF_8 with underscore and UTF8 is not supported by all phones but is sent by some http servers.
				encoding = defaultEncoding; // set it to UTF-8 (replace underscore with minus)				
			}
		}
		
		if(type!=null)
		{
			int idx = type.indexOf(';');
			if(idx>-1) contentType = type.substring(0,idx).trim().toLowerCase();
			else contentType=type.trim().toLowerCase();
		}
		else
		{
			contentType = guessContentType(file);
		}	
	}
	
	public static String guessContentType(String filename)
	{
		if(filename==null) return defaultContentType;
		int idx = filename.lastIndexOf('.');
		if(idx==-1 || filename.length()<=idx+1) return defaultContentType;
		String suffix = filename.substring(idx+1);
		String type = (String)knownContentTypes.get(suffix.toLowerCase());
		return (type!=null)?type:defaultContentType;
	}
	
	public String getBaseURL()
	{
		if("file".equals(protocol)) return protocol +"://";
		if(port==80)
			return protocol+"://"+host;
		else
			return protocol+"://"+host+":"+port;
	}
	
	public String getURL()
	{
		if(connection!=null) return connection.getURL();
		return getBaseURL()+getFile();
	}


	public String getHost()
	{
		return host;
	}
	
	public String getFile()
	{
		return file;
	}
	


	public String getProtocol()
	{
		return protocol;
	}


	public String getQuery()
	{
		return query;
	}


	public int getPort()
	{
		return port;
	}
	
	public int getResponseCode() throws IOException
	{
		return (connection==null)?HttpConnection.HTTP_OK:connection.getResponseCode();
	}
	
	public String getHeaderField(String h) throws IOException
	{
		return (connection!=null)?connection.getHeaderField(h):null;
	}

	public String getHeaderField(int i) throws IOException
	{
		return (connection!=null)?connection.getHeaderField(i):null;
	}

	public String getEncoding()
	{
		return (encoding!=null)?encoding:defaultEncoding;
	}

	public InputStream getInputStream() throws IOException
	{
		if(!closed && in==null && connection!=null)
		{
			in = connection.openInputStream();
		}
		return in;
	}
	
	public void close() throws IOException
	{
		if(closed) return;
		try{
			if(in!=null) in.close();
			if(connection!=null) connection.close();
			
			in=null;
			
		}catch(IOException e){
			Log.logWarn("Failed to close connection "+getBaseURL());
			throw e;
		}finally{
			closed=true;
		}
	}

	/**
	 * @return the contentType
	 */
	public String getContentType()
	{
		return contentType;
	}

	/**
	 * @return the closed
	 */
	public boolean isClosed()
	{
		return closed;
	}

	/**
	 * @return the canceled
	 */
	public boolean isCanceled()
	{
		return canceled;
	}

	/**
	 * @param canceled the canceled to set
	 */
	public void setCanceled(boolean canceled)
	{
		this.canceled = canceled;
	}	
}
