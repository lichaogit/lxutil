package org.lx.util;

import java.util.HashMap;

/* The element will be removed after fetch. */
public class SemaphoreMap extends HashMap
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5776587147751692595L;

	boolean m_bSemaphoreMode = false;

	public void setSemaphoreMode(boolean mode)
	{
		m_bSemaphoreMode = mode;
	}

	public boolean isSemaphoreMode()
	{
		return m_bSemaphoreMode;
	}

	public SemaphoreMap()
	{
		super();
	}

	public Object get(Object key)
	{
		Object retval = super.get(key);
		if (isSemaphoreMode())
		{
			remove(key);
		}
		return retval;
	}

}
