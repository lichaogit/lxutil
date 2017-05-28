package org.lx.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>Title: </p> <p>Description: this is a memory-sensetive HashMap, it is
 * more like WeakHashMap.</p> <p>Copyright: Copyright (c) 2003-2005</p> <p>Company:
 * </p>
 * @author zhaolichao
 * @version 1.0
 */
public abstract class Cache
{
	/*
	 * the Map should not be weekHashMap, or the key will become null if the key
	 * have no user refered to.
	 */
	private Map m_cacheMap;

	private Set m_keepSet;

	public Cache(Map map)
	{
		m_cacheMap = map;
		m_keepSet = new HashSet();
	}

	public void lock(Object key)
	{
		// add a reference to prevent the key be release
		m_keepSet.add(key);
		Object val = getFromCache(key);
		if (val != null)
		{
			m_keepSet.add(val);
		}
	}

	public void unlock(Object key)
	{
		// remove the reference for the specific key/val
		m_keepSet.remove(key);
		Object val = getFromCache(key);
		if (val != null)
		{
			m_keepSet.remove(val);
		}
	}

	public Cache()
	{
		this(new SoftHashMap());
	}

	public void add(Object Key, Object value)
	{
		// SoftReference sr = new SoftReference(value);
		m_cacheMap.put(Key, value);
	}

	public void remove(Object Key)
	{
		m_cacheMap.remove(Key);
	}

	// get from reference map.
	protected Object getFromCache(Object Key)
	{
		// Object retval = null;
		// Object objRef = m_cacheMap.get(Key);
		// if (objRef != null)
		// {
		// SoftReference sr = (SoftReference) objRef;
		// retval = sr.get();
		// }
		// return retval;
		return m_cacheMap.get(Key);
	}

	public Object get(Object key) throws IOException
	{
		Object retval = null;
		retval = getFromCache(key);
		if (retval == null)
		{
			retval = load(key);
			if (retval != null)
			{
				add(key, retval);
			}
		}
		return retval;
	}

	/**
	 * load the specific resouecce.
	 * @param key
	 * @return
	 */
	protected abstract Object load(Object key) throws IOException;

}