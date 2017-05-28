package org.lx.arch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;

public class ConfigurationView
{
	final static String ATTR_TP_NAME = "name";

	final static String ATTR_TP_VALUE = "value";

	protected Configuration m_cfg;

	protected String m_basePath;

	ConfigurationView m_parent;

	public interface ITransform
	{
		public Object convert(Object obj);
	}

	public static class StringTransform implements ITransform
	{
		public Object convert(Object obj)
		{
			return Dom4jNodeConfiguration.stringValueOf(obj);
		}
	}

	public static class AttributesTransform implements ITransform
	{
		public Object convert(Object obj)
		{

			return Dom4jNodeConfiguration.stringValueOf(obj);
		}
	}

	public static StringTransform STRING_TRANSFORM = new StringTransform();

	public static AttributesTransform ATTRIBUTES_TRANSFORM = new AttributesTransform();

	static Log m_log = LogFactory.getLog(ConfigurationView.class);

	public void setParent(ConfigurationView parent)
	{
		m_parent = parent;
	}

	public ConfigurationView getParent()
	{
		return m_parent;
	}

	protected Object getBaseObject()
	{
		Object retval = null;
		try
		{
			String nodePath = m_basePath == null ? "." : m_basePath;
			retval = m_cfg.getHandler(null, nodePath);
		} catch (Exception e)
		{
			m_log.warn(e.getLocalizedMessage(), e);
		}
		return retval;
	}

	public List queryAll(String xpath)
	{
		return queryAll(xpath, null, null);
	}

	public List queryAll(String xpath, ITransform transform)
	{
		return queryAll(xpath, null, transform);
	}

	public List queryAll(String xpath, Object baseObj, ITransform transform)
	{
		List retval = null;
		try
		{
			if (baseObj == null)
			{
				baseObj = getBaseObject();
			}
			retval = m_cfg.getHandlers(baseObj, xpath);
			if ((retval == null || retval.size() == 0) && m_parent != null)
			{
				retval = m_parent.queryAll(xpath);
			}
			if (transform != null && retval != null && retval.size() > 0)
			{
				retval = transform(retval, transform);
			}

		} catch (Exception e)
		{
			m_log.warn(e.getLocalizedMessage(), e);
		}
		return retval;
	}

	public Object query(String xpath)
	{
		return query(xpath, null, null);
	}

	public Object query(String xpath, ITransform transform)
	{
		return query(xpath, null, transform);
	}

	public Object query(String xpath, Object baseObj, ITransform transform)
	{
		Object retval = null;
		try
		{
			if (baseObj == null)
			{
				baseObj = getBaseObject();
			}
			retval = m_cfg.getHandler(baseObj, xpath);
			if (retval == null && m_parent != null)
			{
				retval = m_parent.query(xpath);
			}

			if (transform != null && retval != null)
			{
				retval = transform.convert(retval);
			}
		} catch (Exception e)
		{
			m_log.warn(e.getLocalizedMessage(), e);
		}
		return retval;
	}

	protected List transform(List list, ITransform transform)
	{
		List retval = new ArrayList(list.size());
		Iterator it = list.iterator();
		while (it.hasNext())
		{
			retval.add(transform.convert(it.next()));
		}
		return retval;
	}

	public String asXML()
	{
		String retval = null;
		Object base = getBaseObject();
		if (base != null)
		{
			retval = ((Node) base).asXML();
		} else if (m_parent != null)
		{
			retval = m_parent.asXML();
		}
		return retval;
	}

	protected ConfigurationView()
	{

	}

	public ConfigurationView(Configuration cfg, String basePath)
	{
		m_cfg = cfg;
		m_basePath = basePath;
	}

	public Configuration getConfigutation()
	{
		return m_cfg;
	}

	/**
	 * create a new node in current view.
	 * @param newNodeName
	 * @return
	 */
	public Object createNode(String newNodeName)
	{
		Object baseObj = getBaseObject();
		return m_cfg.createNode(baseObj, newNodeName);
	}

	protected Object getObject(Configuration cfg, String xpath)
			throws Exception
	{
		return cfg.getHandler(null, xpath);
	}

	public String getXPath(Object base)
	{
		return m_cfg.getXPath(base);
	}

	public ConfigurationView createSubView(String nodePath) throws Exception
	{
		// create a new node if the node is not exist
		Object obj = m_cfg.getHandler(getBaseObject(), nodePath);

		if (obj == null)
		{
			String[] nodesName = nodePath.split("/");
			Object curNode = getBaseObject();
			for (int i = 0; i < nodesName.length; i++)
			{
				curNode = m_cfg.createNode(curNode, nodesName[i]);
			}
		}
		return getSubView(nodePath);
	}

	public ConfigurationView getSubView(String nodePath)
	{
		ConfigurationView newView = new ConfigurationView();

		newView.m_cfg = m_cfg;
		String newPath = null;
		if (m_basePath != null)
		{
			newPath = nodePath.startsWith("/") ? m_basePath + nodePath
					: m_basePath + "/" + nodePath;
		} else
		{
			newPath = nodePath.startsWith("/") ? nodePath.substring(1)
					: nodePath;
		}

		newView.m_basePath = newPath;
		// the parent of the new View should getSubView as well.
		if (m_parent != null)
		{
			newView.m_parent = m_parent.getSubView(nodePath);
		}
		return newView;
	}

	public void addAttribute(Object obj, String key, String val)
	{
		m_cfg.addAttribute(obj, key, val);
	}

	public void addAttribute(String key, String val)
	{
		addAttribute(getBaseObject(), key, val);
	}

	public String getAttribute(String nodePath, String key)
	{
		return getAttribute(nodePath, key, null);
	}

	public String getAttribute(String nodePath, String key, Object baseObj)
	{
		String retval = null;
		String xpath = nodePath == null ? "@" + key : nodePath + "/@" + key;
		Object obj = query(xpath, baseObj, null);

		if (obj != null)
		{
			retval = Dom4jNodeConfiguration.stringValueOf(obj);
		}
		return retval;
	}

	public Map getAttributes(String nodePath)
	{
		Map retval = null;
		String xpath = nodePath == null ? "@*" : nodePath + "/@*";
		// Object obj = queryAll(xpath,ATTRIBUTES_TRANSFORM);
		Object obj = queryAll(xpath);

		if (obj != null)
		{
			retval = m_cfg.getAttributes(obj);
		}
		return retval;
	}

	public String getAttribute(String key)
	{
		return getAttribute(null, key);
	}

	public Map getNameValuePaired(String nodesXpath, ITransform transform)
	{
		return getNameValuePaired(nodesXpath, ATTR_TP_NAME, ATTR_TP_VALUE,
				transform);
	}

	public Map getNameValuePaired(String nodesXpath)
	{
		return getNameValuePaired(nodesXpath, ATTR_TP_NAME, ATTR_TP_VALUE, null);
	}

	public Map getNameValuePaired(String nodesXpath, String nameTag,
			String valTag)
	{
		return getNameValuePaired(nodesXpath, nameTag, valTag, null);
	}

	public Map getNameValuePaired(String nodesXpath, String nameTag,
			String valTag, ITransform transform)
	{

		Map retval = null;

		do
		{
			// 1.load from dom4j
			// for dom4j Configuration.
			List results = queryAll(nodesXpath);

			if (results != null && results.size() > 0)
			{
				Iterator it = results.iterator();
				Element ele = null;
				retval = new HashMap();
				Attribute name_attr = null;
				Attribute val_attr = null;
				Object val = null;
				while (it.hasNext())
				{
					ele = (Element) it.next();
					name_attr = ele.attribute(nameTag);
					val_attr = ele.attribute(valTag);
					if (name_attr != null && val_attr != null)
					{

						if (transform != null)
						{
							val = transform.convert(val_attr.getText());
						} else
						{
							val = val_attr.getText();
						}
						retval.put(name_attr.getText(), val);
					}
				}
			}

			// 2.for MapConfiguration
			if (m_cfg instanceof MapConfiguration)
			{
				retval.putAll(((MapConfiguration) m_cfg).m_map);
				break;
			}
		} while (false);

		return retval;
	}
}
