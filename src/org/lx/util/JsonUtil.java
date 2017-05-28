package org.lx.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import net.sf.json.JSONObject;

import org.lx.arch.JSONExchange;

public class JsonUtil
{
	static JSONExchange m_jsonExchange = new JSONExchange();

	public static synchronized String setJsonProp(String orgVal, String path,
			String value)
	{
		String retval = value;
		if (path != null && path.trim().length() > 0)
		{
			String opVal = orgVal;
			Map jsonData = opVal == null ? new JSONObject()
					: (Map) m_jsonExchange.fromString(opVal, null);

			String leafName = path;
			String parentName = null;
			int index = path.lastIndexOf("/");
			if (index != -1)
			{
				parentName = path.substring(0, index);
				leafName = path.substring(index + 1);
			}

			Map parent = jsonData;
			if (parentName != null)
			{
				parent = (Map) parseJpath(jsonData, parentName, true);
			}

			parent.put(leafName, value);
			opVal = jsonData.toString();
			retval = opVal;
		}
		return retval;
	}

	public static synchronized String removeJsonProp(String orgVal, String path)
	{
		String retval = orgVal;
		if (path != null && path.trim().length() > 0)
		{
			String opVal = orgVal;
			Map jsonData = opVal == null ? new JSONObject()
					: (Map) m_jsonExchange.fromString(opVal, null);

			String leafName = path;
			String parentName = null;
			int index = path.lastIndexOf("/");
			if (index != -1)
			{
				parentName = path.substring(0, index);
				leafName = path.substring(index + 1);
			}

			Map parent = jsonData;
			if (parentName != null)
			{
				Object parentObj = parseJpath(jsonData, parentName, false);
				parent = parentObj == null ? null : (Map) parentObj;
			}

			if (parent != null)
			{
				parent.remove(leafName);
				opVal = jsonData.toString();
				retval = opVal;
			}
		}
		return retval;
	}

	public static synchronized String parseJpathRawString(String baseVal,
			String jPath)
	{
		String retval = null;
		int count = baseVal.length();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < count; i++)
		{
			char c = baseVal.charAt(i);
			if (c == '{')
			{
				// begin
			}

			if (c == '\"' || c == '\'')
			{

			}

		}
		return retval;
	}

	public static Object parseJpath(String baseVal, String jPath)
	{
		Object retval = m_jsonExchange.fromString(baseVal, null);
		do
		{
			if (retval == null || jPath == null || jPath.length() == 0)
			{
				break;
			}

			Map jsonMap = null;
			if (retval instanceof Map)
			{
				jsonMap = (Map) retval;
			} else
			{
				Object jsonObj = m_jsonExchange.fromString(retval.toString(),
						null);
				// just process the JSONObject
				if (jsonObj instanceof Map)
				{
					jsonMap = (Map) jsonObj;
				}
			}
			retval = parseJpath(jsonMap, jPath, false);

		} while (false);
		return retval;
	}

	public static Object parseJpath(Map val, String jpath, boolean bCreate)
	{
		Object retval = null;
		String[] paths = jpath.split("/");
		Map wk = val;
		Object obj = null;
		int i = 0;
		String path = null;
		while (i < paths.length)
		{
			path = paths[i++];
			if (wk == null)
			{
				break;
			}
			obj = wk.get(path);
			if (obj instanceof Map == false)
			{
				if (!bCreate)
				{
					break;
				}
				wk.put(path, new JSONObject());
				obj = wk.get(path);
			}
			wk = (Map) obj;
		}

		if (obj != null && i == paths.length)
		{
			retval = obj;
		}
		return retval;
	}

	public static JSONObject map2Json(Map map)
	{
		JSONObject retval = new JSONObject();
		Iterator it = map.keySet().iterator();
		while (it.hasNext())
		{
			Object key = it.next();
			retval.put(key, map.get(key));
		}
		return retval;
	}

	public static JSONObject clone(Map obj)
	{
		JSONObject retval = new JSONObject();
		retval.putAll(Collections.unmodifiableMap(obj));
		return retval;
	}
}
