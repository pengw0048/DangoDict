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
package gr.fire.util;
import gr.fire.core.Component;

import java.util.Vector;

/**
 * @author padeler
 *
 */
public final class Queue
{
	private final Vector queue = new Vector();
	private final Object lock = new Object();
	
	private int pointer;
	
	public Queue()
	{
		pointer=0;
	}

	public Object getNext() throws InterruptedException
	{
		synchronized (lock)
		{
			while(queue.size()==0)
			{
				lock.wait(); 
			}
			++pointer;
			if(pointer>=queue.size()) pointer=0;
		
			return queue.elementAt(pointer); 
		}
	}
	
	public void add(Object obj)
	{
		if(obj==null) throw new NullPointerException("Parameter cannot be null");
		
		synchronized (lock)
		{
			if(queue.contains(obj)) return;
			queue.addElement(obj);
			// set pointer to a position that the next object served will be this one.
			pointer = queue.size()-2;
			lock.notify(); // notify any thread waiting for the new object.
		}
	}
	
	public boolean remove(Object obj)
	{
		if(obj==null) throw new NullPointerException("Parameter cannot be null");
		synchronized (lock)
		{
			// set pointer to a safe position.
			pointer = 0;
			return queue.removeElement(obj);
		}
	}
	
	public void removeAll()
	{
		synchronized (lock)
		{
			// set pointer to a safe position.
			pointer = 0;
			queue.removeAllElements();
		}
	}
	
	/** 
	 * Removes all objects in the Queue that are components with the given parent. 
	 * 
	 * @param parent
	 */
	public void removeAllWithParent(Component parent)
	{
		synchronized (lock)
		{
			// set pointer to a safe position.
			pointer = 0;
			for(int i=queue.size()-1;i>=0;--i)
			{
				Object o = queue.elementAt(i);
				if(o instanceof Component)
				{
					Component p = ((Component)o).getParent();
					while(p!=null)
					{
						if(p==parent)
						{
							queue.removeElementAt(i);
							break;
						}
						p = p.getParent();
					}
				}
			}
		}
	}
	
	public int size()
	{
		return queue.size();
	}
}