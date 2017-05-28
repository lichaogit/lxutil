package org.lx.util;

import java.util.Map;

public class JsonValueRender implements ValueRender
{
	Map m_map;

	public JsonValueRender(Map map)
	{
		m_map = map;
	}

	@Override
	public Object get(String key)
	{
		return JsonUtil.parseJpath(m_map, key, false);
	}

}
