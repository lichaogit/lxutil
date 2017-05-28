package org.lx.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p> Title: </p> <p> Description: ClassManager can load the class by the name,
 * and it contain a cache </p> to cache the class that have been loaded. </p>
 * <p> Copyright: Copyright (c) 2003 </p> <p> Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ClassManager extends NamedCache
{
	protected static Log m_log = LogFactory.getLog(ClassManager.class);

	protected Map m_classInfo;

	protected ClassLoader m_cl;

	public ClassManager()
	{
		this(null);
	}

	public ClassManager(ClassLoader cl)
	{
		this(new HashMap(10), cl);
	}

	public ClassManager(Map map, ClassLoader cl)
	{
		m_classInfo = map;
		setClassLoader(cl);
	}

	/**
	 * get the class Name
	 * @param key
	 * @return
	 */
	public Object resolve(Object key)
	{
		return m_classInfo.containsKey(key) ? m_classInfo.get(key) : key;
	}

	public void setClassLoader(ClassLoader cl)
	{
		m_cl = cl;
	}

	public ClassLoader getClassLoader()
	{
		return m_cl;
	}

	public void addClassInfo(Object id, String classInfo)
	{
		m_classInfo.put(id, classInfo);
	}

	public void addClassInfo(Map map)
	{
		m_classInfo.putAll(map);
	}

	public void addClassInfo(Object id, Class classInfo)
	{
		// add to the cache directly.
		add(id, classInfo);
	}

	public void removeClassInfo(Object id)
	{
		m_classInfo.remove(id);
	}

	/**
	 * Determine whether the class have been registered.
	 * @param key
	 * @return
	 */
	public boolean isRegisterdClass(String key)
	{
		boolean retval = false;
		try
		{
			retval = get(key) != null;
		} catch (IOException e)
		{

		}
		return retval;
	}

	/**
	 * load the class from outer by the class id.
	 * @param id
	 * @return
	 */
	public Class loadClass(Object id)
	{
		Class retval = null;
		if (id != null)
		{
			String className = id.toString();
			try
			{
				if ("byte".equals(className))
				{
					retval = Byte.TYPE;
				} else if ("short".equals(className))
				{
					retval = Short.TYPE;
				} else if ("int".equals(className))
				{
					retval = Integer.TYPE;
				} else if ("long".equals(className))
				{
					retval = Long.TYPE;
				} else if ("float".equals(className))
				{
					retval = Float.TYPE;
				} else if ("double".equals(className))
				{
					retval = Double.TYPE;
				} else if ("char".equals(className))
				{
					retval = Character.TYPE;
				} else if ("boolean".equals(className))
				{
					retval = Boolean.TYPE;
				} else if (m_cl != null)
				{
					retval = Class.forName(className, true, m_cl);
				} else
				{
					retval = Class.forName(className);
				}
			} catch (ClassNotFoundException ex)
			{
				m_log.warn("can not load class:" + className);
			}
		}
		return retval;
	}

	/**
	 * get the class by the id
	 * @param id
	 * @return
	 * @throws IOException
	 */
	public Class getClass(Object id) throws IOException
	{
		Class retval = null;
		Object obj = get(id);
		retval = obj == null ? null : (Class) obj;

		return retval;
	}

	/**
	 * load the specific resouecce.
	 * @param key
	 * @return
	 */
	protected Object load(Object key)
	{
		return loadClass(key);
	}
}