package org.lx.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * <p> Title: </p> <p> Description: </p> <p> Copyright: Copyright (c) 2003-2005
 * </p> <p> Company: uilogic </p>
 * @author zhaolc
 * @version 1.0
 */

public class MapTree
{
	Map m_root;

	public MapTree()
	{
		this(new HashMap());
	}

	public MapTree(Map map)
	{
		m_root = map;
	}

	public Map createMap(Object[] path, int index)
	{
		return new HashMap(5);
	}

	protected Object getDefaultKey()
	{
		// use the null as the default key, the value of a node is stored in the
		// default key.
		return null;
	}

	public Object get(Object[] path)
	{
		Object retval = null;
		if (path == null)
		{
			retval = m_root.get(getDefaultKey());
		} else
		{
			Map work = m_root;
			Object obj = null;
			for (int i = 0; i < path.length; i++)
			{
				obj = work.get(path[i]);
				if (obj == null)
				{
					break;
				}
				work = (Map) obj;
				if (i == path.length - 1)
				{
					retval = work.get(getDefaultKey());
					break;
				}
			}
		}

		return retval;
	}

	public boolean contains(Object[] path)
	{
		boolean retval = false;
		if (path == null)
		{
			retval = m_root.containsKey(getDefaultKey());
		} else
		{
			Map work = m_root;
			Object obj = null;
			for (int i = 0; i < path.length - 1; i++)
			{
				obj = work.get(path[i]);
				if (obj == null)
				{
					break;
				}
				work = (Map) obj;
			}
			if (work != null)
			{
				retval = work.containsKey(path[path.length - 1]);
			}
		}

		return retval;
	}

	public void clear()
	{
		m_root.clear();
	}

	public Collection getChildren(Object[] path)
	{
		Collection retval = null;
		if (path == null)
		{
			retval = m_root == null ? null : m_root.keySet();
		} else
		{
			Map work = m_root;
			Object obj = null;
			for (int i = 0; i < path.length; i++)
			{
				obj = work.get(path[i]);
				if (obj == null)
				{
					break;
				}
				work = (Map) obj;
				if (i == path.length - 1)
				{
					retval = work.keySet();
					break;
				}
			}

		}
		if (retval != null)
		{
			// remove the defaultKey.
			retval.remove(getDefaultKey());
		}
		return retval;
	}

	public boolean add(Object[] path, Object value)
	{
		boolean retval = false;
		if (path == null)
		{
			m_root.put(getDefaultKey(), value);
			retval = true;
		} else
		{
			Map work = m_root;
			Object obj = null;
			Map node = null;
			for (int i = 0; i < path.length; i++)
			{
				if (work.containsKey(path[i]))
				{
					obj = work.get(path[i]);
					if (obj instanceof Map)
					{
						work = (Map) obj;
					}
				} else
				{
					node = createMap(path, i);
					work.put(path[i], node);
					work = node;
				}
			}
			work.put(getDefaultKey(), value);
			retval = true;
		}
		return retval;

	}
}
