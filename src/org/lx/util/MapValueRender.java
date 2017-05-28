package org.lx.util;

import java.util.Map;

public class MapValueRender implements ValueRender
{
	Map m_map;

	public MapValueRender(Map map)
	{
		m_map = map;
	}

	public Object get(String key)
	{

		return m_map == null ? null : m_map.get(key);
	}

}