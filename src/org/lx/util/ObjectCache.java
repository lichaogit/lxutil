package org.lx.util;

import java.io.IOException;
import java.util.Map;

/**
 * <p> Title: </p> <p> Description: </p> <p> Copyright: Copyright (c) 2003-2005
 * </p> <p> Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ObjectCache extends NamedCache
{
	ClassManager m_cm;

	public ObjectCache()
	{
		this(null);
	}

	public ObjectCache(Map classInfo)
	{
		this(classInfo, null);
	}

	public ObjectCache(Map classInfo, ClassLoader cl)
	{
		m_cm = new ClassManager(classInfo, cl);
	}

	/**
	 * convert the key to the real id.
	 * @param key
	 * @return
	 */
	public Object resolve(Object key)
	{
		return key;
	}

	/**
	 * return the ObjectCache's embed ClassManager.
	 * @return
	 */
	protected ClassManager getClassManager()
	{
		return m_cm;
	}

	/**
	 * Load the Object by the class, the load method will only load first.
	 * @param key
	 * @return
	 */
	protected Object load(Object key)
	{
		Object retval = null;
		Object cls;
		do
		{
			try
			{
				cls = m_cm.get(key);
				if (cls instanceof Class == false)
				{
					break;
				}
				Class clazz = (Class) cls;
				// skip the interface.
				if (clazz.isInterface())
				{
					break;
				}
				retval = clazz.newInstance();
			} catch (IOException e)
			{
			} catch (InstantiationException e)
			{
			} catch (IllegalAccessException e)
			{
			}
		} while (false);
		return retval;
	}

	/**
	 * get the class by the class name.
	 * @param id
	 * @return
	 * @throws IOException
	 */
	public Class getClass(Object cls_name) throws IOException
	{
		// get from cache.
		return (Class) m_cm.get(cls_name);
	}
}