package org.lx.util;

import java.util.Map;

public class MapStringValueRender implements StringValueRender
{
	Map m_map;

	public MapStringValueRender(Map map)
	{
		m_map = map;
	}

	public String get(String old_value)
	{
		String retval = null;
		if (m_map != null)
		{
			Object obj = m_map == null || old_value == null ? null : m_map
					.get(old_value);
			retval = obj == null ? "" : obj.toString();
		}
		return retval;
	}

}