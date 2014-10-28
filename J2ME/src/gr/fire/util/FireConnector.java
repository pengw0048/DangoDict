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
 * Created on Sep 14, 2006
 *
 */
package gr.fire.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connection;
import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;

/**
 * FireConnector is a utility class containing a set of I/O supporting methods.
 * This implementation is a wrapper for Connector and also an easy way to access the 
 * RecordStore.
 * 
 * All methods starting with "rms" have RecordStore related functionality. 
 * 
 * @author padeler
 *
 */
public class FireConnector
{
	public FireConnector()
	{
	}

	/**
	 * 
	 * Lists all record stores of this middlet.
	 *  
	 * @return A vector of Strings with the names of the record stores.
	 * 
	 */
	public Vector rmslist()  
	{
		Vector res = new Vector();
		try{
			String []names = RecordStore.listRecordStores();
			if(names!=null)
			{
				for(int i =0 ;i<names.length;++i)
				{
					String fileName = names[i];
					res.addElement(fileName);
				}
			}
		}catch(Exception e)
		{
			Log.logError("List Files Error",e);
		}
		return res;
	}
	
	/**
	 * Deletes the record store with the given name 
	 * @param file
	 * @throws RecordStoreNotFoundException
	 * @throws RecordStoreException
	 */
	public void rmsDelete(String file) throws RecordStoreNotFoundException, RecordStoreException 
	{
		RecordStore.deleteRecordStore(file);
	}
	
	/**
	 * Returns an int[2] where the first value is the size of the record store
	 * and the second is the size available.
	 * @param name the name of the record store to check for.
	 * 
	 * @return if the record store exists returns an int[2] with the size/free. If it does not returns {0,0}
	 */
	public int[] rmsSize(String name)
	{
		RecordStore rs =null;
		try{
			rs = RecordStore.openRecordStore(name,false);
			return new int[]{rs.getSize(),rs.getSizeAvailable()};
		}catch(Exception e)
		{
			Log.logError("Failed to get size for RecordStore "+name,e);
		}finally{
			try{if(rs!=null) rs.closeRecordStore();}catch(Throwable e){}
		}
		return new int[]{0,0};
	}
	
	/**
	 * Returns the total space used by all record stores.
	 * @return
	 */
	public int rmsUsedSpace()
	{
		String recordStores[] = RecordStore.listRecordStores();
		if(recordStores!=null)
		{
			int sum=0;
			for(int i =0 ;i<recordStores.length;++i)
			{
				sum+= rmsSize(recordStores[i])[0];
			}
			return sum;
		}
		return 0;
	}

	
	/**
	 * Returns the available space in the phone's memory for recordstores.
	 * @return
	 */
	public int rmsFree()
	{
		String recordStores[] = RecordStore.listRecordStores();
		if(recordStores!=null && recordStores.length>0)
		{
			return rmsSize(recordStores[0])[1];
		}
		return 0;
	}
		
	/**
	 * Creates a new recordstore or ovewrites an old one with the same name and stores the bytes in buffer to it.
	 * @param file the name of the recordstore
	 * @param buffer the data of the recordstore
	 * @throws RecordStoreException 
	 * @throws RecordStoreFullException 
	 * @throws Exception
	 */
	public void rmsWrite(String file,byte[] buffer) throws RecordStoreFullException, RecordStoreException 
	{
		RecordStore fr=null;
		try{
			fr = RecordStore.openRecordStore(file,true,RecordStore.AUTHMODE_PRIVATE,true);
			RecordEnumeration re = fr.enumerateRecords(null,null,false);
			if(re.hasNextElement())
			{
				int id = re.nextRecordId();
				fr.deleteRecord(id);	
			}
			fr.addRecord(buffer,0,buffer.length);
		}finally{
			 try{if(fr!=null)fr.closeRecordStore();}catch(Exception e){}
		}
	}
	
	/**
	 * Creates a new recordstore or ovewrites an old one with the same name and stores the UTF String to it.
	 * @param file the name of the recordstore
	 * @param str the String that will be stored
	 * @throws RecordStoreException 
	 * @throws RecordStoreFullException 
	 * @throws IOException
	 */
	public void rmsWriteUTF(String file,String str) throws RecordStoreFullException, RecordStoreException, IOException 
	{
		RecordStore fr=null;
		try{
			fr = RecordStore.openRecordStore(file,true,RecordStore.AUTHMODE_PRIVATE,true);
			RecordEnumeration re = fr.enumerateRecords(null,null,false);
			if(re.hasNextElement())
			{
				int id = re.nextRecordId();
				fr.deleteRecord(id);	
			}
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream d = new DataOutputStream(bout);
			d.writeUTF(str);
			d.close();
			byte[] buf = bout.toByteArray();
			fr.addRecord(buf,0,buf.length);
		}finally{
			 try{if(fr!=null)fr.closeRecordStore();}catch(Exception e){}
		}
	}
	
	/**
	 * Creates a new recordstore or ovewrites an old one with the same name and saves up to len bytes from the inputstream.
	 * @param file The name of the record store
	 * @param in The InputStream from where to read bytes
	 * @param len The maximum number of bytes that will be written to the record store.
	 * @return The number of bytes that where written to the record store
	 * @throws RecordStoreFullException 
	 * @throws RecordStoreException 
	 * @throws IOException 
	 */
	public int rmsWrite(String file,InputStream in,int len) throws RecordStoreFullException, RecordStoreException, IOException 
	{
		RecordStore fr=null;
		try{
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			int count=0,s;
			byte buf[] = new byte[512];
			while((count<len || len==0) && (s=in.read(buf))>-1)
			{
				count+=s;
				bout.write(buf,0,s);
			}
			if(count==0) throw new IOException("Failed to read from InputStream. Nothing written to record store.");
			buf=null;
			
			fr = RecordStore.openRecordStore(file,true,RecordStore.AUTHMODE_PRIVATE,true);
			RecordEnumeration re = fr.enumerateRecords(null,null,false);
			if(re.hasNextElement())
			{
				int id = re.nextRecordId();
				fr.deleteRecord(id);	
			}
			fr.addRecord(bout.toByteArray(),0,count);
			return count;
		}finally{
			 try{if(fr!=null)fr.closeRecordStore();}catch(Exception e){}
		}
	}

	/**
	 * If the given URL starts with "file://" this method open a stream to the local file (inside the jar) or if that 
	 * does not exist it will try to find a recordstore with the same name (without the "file:/" prefix).
	 * Note that the name of the recordstore searched will start with a "/". 
	 * 
	 * If it does not start with "file://" it will use Connector.openInputStream(url) to try and return an InputStream 
	 * to the url using the platform's capabilities. 
	 * 
	 * @param url
	 * @return
	 * @throws IllegalArgumentException 
	 * @throws ConnectionNotFoundException 
	 * @throws IOException 
	 * @throws SecurityException 
	 * 
	 */
	public InputStream openInputStream(String url) throws IllegalArgumentException,ConnectionNotFoundException,IOException,SecurityException
	{
		if(url.startsWith("file://"))
		{
			String file = url.substring(7);
			Log.logDebug("Loading local resource: "+file);
			return this.getClass().getResourceAsStream("/"+file);
		}
		if(url.startsWith("rms://"))
		{
			String file = url.substring(6);
			Log.logDebug("Loading rms resource: "+file);
			return rmsRead(file);
		}
		
		return Connector.openInputStream(url);
	}
	
	/**
	 * 
	 * @see #openInputStream(String)
	 * @param url
	 * @return
	 * @throws IllegalArgumentException 
	 * @throws ConnectionNotFoundException 
	 * @throws IOException
	 * @throws SecurityException 
	 */
	public DataInputStream openDataInputStream(String url)  throws IllegalArgumentException,ConnectionNotFoundException,IOException,SecurityException
	{
		return new DataInputStream(openInputStream(url));
	}
	
	/**
	 * Wrapper for Connector.openOutputStream(url)
	 * @param url
	 * @return
	 * @throws IllegalArgumentException 
	 * @throws ConnectionNotFoundException 
	 * @throws IOException
	 * @throws SecurityException 
	 */
	public OutputStream openOutputStream(String url)  throws IllegalArgumentException,ConnectionNotFoundException,IOException,SecurityException
	{
		return Connector.openOutputStream(url);
	}
	
	/**
	 * Wrapper for Connector.openDataOutputStream(url)
	 * @param url
	 * @return
	 * @throws IllegalArgumentException 
	 * @throws ConnectionNotFoundException 
	 * @throws IOException
	 * @throws SecurityException 
	 */
	public DataOutputStream openDataOutputStream(String url)  throws IllegalArgumentException,ConnectionNotFoundException,IOException,SecurityException
	{
		return Connector.openDataOutputStream(url);
	}
	
	/**
	 * Wrapper for Connector.open(url)
	 * @param url
	 * @return
	 * @throws IllegalArgumentException 
	 * @throws ConnectionNotFoundException 
	 * @throws IOException
	 * @throws SecurityException 
	 */
	public Connection open(String url)  throws IllegalArgumentException,ConnectionNotFoundException,IOException,SecurityException
	{
		return Connector.open(url);
	}

	/**
	 * Wrapper for Connector.open()
	 * @param url
	 * @param mode 
	 * @return
	 * @throws IllegalArgumentException 
	 * @throws ConnectionNotFoundException 
	 * @throws IOException
	 * @throws SecurityException 
	 */	
	public Connection open(String url,int mode)  throws IllegalArgumentException,ConnectionNotFoundException,IOException,SecurityException
	{
		return Connector.open(url,mode);
	}
	
	/**
	 * Wrapper for Connector.open()
	 * @param url
	 * @param mode 
	 * @param timeouts 
	 * @return
	 * @throws IllegalArgumentException 
	 * @throws ConnectionNotFoundException 
	 * @throws IOException
	 * @throws SecurityException 
	 */	
	public Connection open(String url,int mode,boolean timeouts)  throws IllegalArgumentException,ConnectionNotFoundException,IOException,SecurityException
	{
		return Connector.open(url,mode,timeouts);
	}
	
	
	/**
	 * Reads a recordstore and returns a stream to it.
	 * @param f
	 * @return
	 */
	public InputStream rmsRead(String f) throws IOException
	{
		RecordStore fr=null;
		try{
			fr = RecordStore.openRecordStore(f,false,RecordStore.AUTHMODE_PRIVATE,false);
			RecordEnumeration re = fr.enumerateRecords(null,null,false);
			if(re.hasNextElement())
			{
				return new ByteArrayInputStream(re.nextRecord());
			}
			return null;
		}catch(RecordStoreNotFoundException e){
			// record store does not exist. This is not an error. Just return null.
			return null;
		}catch(Exception e){
			throw new IOException("Recordstore read failed for "+f+". "+e.getClass().getName()+": "+e.getMessage());
		}finally{
			 try{if(fr!=null)fr.closeRecordStore();}catch(Exception e){}
		}
	}
	
	/**
	 * Checks if a recordstore exists.
	 * @param f name of the record store
	 * @return true if record store exists 
	 */
	public boolean rmsExists(String f)
	{
		RecordStore fr=null;
		try{
			fr = RecordStore.openRecordStore(f,false,RecordStore.AUTHMODE_PRIVATE,false);
			return true;
		}catch(RecordStoreNotFoundException e){
			// record store does not exist.
			return false;
		}catch(RecordStoreFullException e){
			Log.logWarn("Unexpected recordstore exception",e);
			return false;
		}catch(RecordStoreException e){
			Log.logWarn("Unexpected recordstore exception",e);
			return false;
		}catch(Exception e){
			Log.logWarn("Unexpected exception",e);
			return false;
		}finally{
			 try{if(fr!=null)fr.closeRecordStore();}catch(Exception e){}
		}
	}
	
	/**
	 * Reads a recordstore and returns a utf string from it. The record store must have been writen with the 
	 * writeUTF method.
	 * 
	 * @param f the name of the record store
	 * @return null if the record store does not exist or a String with its contents otherwise.
	 * @throws IOException If an exception is thrown while reading the record store.
	 */
	public String rmsReadUTF(String f) throws IOException
	{
		RecordStore fr=null;
		DataInputStream di=null;
		try{
			fr = RecordStore.openRecordStore(f,false,RecordStore.AUTHMODE_PRIVATE,false);
			RecordEnumeration re = fr.enumerateRecords(null,null,false);
			if(re.hasNextElement())
			{
				di = new DataInputStream(new ByteArrayInputStream(re.nextRecord()));
				return di.readUTF();
			}
			return null;
		}catch(RecordStoreNotFoundException e){
			// record store does not exist. This is not an error. Just return null.
			return null;
		}catch(Exception e){
			throw new IOException("Recordstore read failed for "+f+". "+e.getClass().getName()+": "+e.getMessage());
		}finally{
			 try{
				 if(fr!=null) fr.closeRecordStore();
				 if(di!=null) di.close();
			 }catch(Exception e){}
		}
	}
}