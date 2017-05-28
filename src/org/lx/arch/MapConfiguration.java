package org.lx.arch;

import java.util.List;
import java.util.Map;

/**
 * A simple configuration with Map, it isn't a tree model.
 * @author Administrator
 */
public class MapConfiguration implements Configuration
{
	Map m_map;
	/**
	 * get the xpath info for the specific base.
	 * @param base
	 * @return
	 */
	public String getXPath(Object base)
	{
		return "/";
	}

	public MapConfiguration(Map map)
	{
		m_map = map;
	}

	public void addAttribute(Object node, String key, String val)
	{
		m_map.put(key, val);
	}
	public Map getAttributes(Object node)
	{
		return m_map;
	}
	public String asXML()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Object createNode(Object parentNode, String nodeName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getAttributeValue(Object node, String key)
	{
		Object obj = m_map.get(key);
		return obj == null ? null : obj.toString();
	}

	public Object getHandler(Object base, String nodePath)
	{
		// remove the @
		return m_map.get(nodePath.substring(1));
	}

	public List getHandlers(Object base, String nodePath)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String toString(Object obj)
	{
		return obj.toString();
	}

}
