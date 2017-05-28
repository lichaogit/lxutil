package org.lx.arch;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.filters.MappingPropertyFilter;
import net.sf.json.processors.JsonBeanProcessor;
import net.sf.json.util.JSONUtils;
import net.sf.json.util.PropertyFilter;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.tree.DefaultAttribute;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultText;
import org.lx.util.LogicUtil;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class JSONExchange implements StreamExchange
{

	public Object fromObject(Object jsonObject, Class clazz)
	{
		return JSONObject.toBean((JSONObject) jsonObject, clazz);
	}

	public Object fromString(String express, Class clazz)
	{
		Object retval = null;
		if (express.startsWith("["))
		{
			retval = JSONArray.fromObject(express);
		} else
		{
			JSONObject jsonObject = JSONObject.fromObject(express);
			retval = clazz == null ? jsonObject : JSONObject.toBean(jsonObject,
					clazz);
		}
		return retval;
	}

	class MyMappingPropertyFilter extends MappingPropertyFilter
	{
		protected boolean keyMatches(Object key, Object source, String name,
				Object value)
		{
			return ((Class) key).isAssignableFrom(source.getClass());
		}
	}

	class AttributeBeanProcessor implements JsonBeanProcessor
	{

		public JSONObject processBean(Object bean, JsonConfig jsonConfig)
		{
			JSONObject retval = new JSONObject();
			Attribute attr = (Attribute) bean;
			retval.put(attr.getName(), attr.getText());
			return retval;
		}
	}

	class TextBeanProcessor implements JsonBeanProcessor
	{

		public JSONObject processBean(Object bean, JsonConfig jsonConfig)
		{
			JSONObject retval = new JSONObject();
			org.dom4j.Text attr = (org.dom4j.Text) bean;
			retval.put("text", attr.getText());
			return retval;
		}
	}

	/**
	 * process the dom4j 'Element' bean.
	 * @author Administrator
	 */
	class ElementBeanProcessor implements JsonBeanProcessor
	{

		public JSONObject processBean(Object bean, JsonConfig jsonConfig)
		{
			JSONObject retval = new JSONObject();

			Element ele = (Element) bean;

			// create a json object to contain all attribute info.
			JSONObject attrsJson = new JSONObject();
			Iterator it = ele.attributes().iterator();
			Attribute attr = null;
			while (it.hasNext())
			{
				attr = (Attribute) it.next();
				attrsJson.put(attr.getName(), attr.getText());
			}

			retval.put("attributes", attrsJson);

			String text = ele.getText();
			if (text != null)
			{
				retval.put("text", text);
			}

			List childs = ele.elements();
			if (childs.size() > 0)
			{
				retval.put("childs", childs);
			}
			return retval;
		}
	}

	class PropertiesFilter implements PropertyFilter
	{
		String[] properties;

		boolean isExclude;

		public PropertiesFilter(String[] properties, boolean isExclude)
		{
			this.properties = properties;
			this.isExclude = isExclude;
		}

		public boolean apply(Object source, String name, Object value)
		{
			boolean retval = false;
			boolean inArray = LogicUtil.isInArray(properties, name);
			if ((inArray && isExclude) || (!inArray && !isExclude))
			{
				retval = true;// exclude
			}
			return retval;
		}
	}

	public String toString(Object obj, Map convertIinfo)
	{
		String retval = null;
		JsonConfig config = new JsonConfig();

		// MappingPropertyFilter filter = new MyMappingPropertyFilter();
		// for (int i = 0; i < target.length; i++)
		// {
		// filter.addPropertyFilter(target[i], new PropertiesFilter(
		// properties[i], isExclude[i]));
		// }
		//
		// config.setJsonPropertyFilter(filter);

		config.registerJsonBeanProcessor(DefaultElement.class,
				new ElementBeanProcessor());
		config.registerJsonBeanProcessor(DefaultAttribute.class,
				new AttributeBeanProcessor());
		config.registerJsonBeanProcessor(DefaultText.class,
				new TextBeanProcessor());

		if (JSONUtils.isArray(obj))
		{
			JSONArray array = JSONArray.fromObject(obj, config);
			retval = array.toString();
		} else
		{
			JSONObject jsonObject = JSONObject.fromObject(obj, config);
			retval = jsonObject.toString();
		}

		return retval;
	}
}
