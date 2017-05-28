package org.lx.arch;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.Element;
import org.dom4j.Node;
import org.jaxen.JaxenException;
import org.jaxen.dom4j.Dom4jXPath;

public class Dom4jNodeConfiguration implements Configuration
{
	Node m_node;

	public Dom4jNodeConfiguration()
	{

	}

	public Dom4jNodeConfiguration(Node node)
	{
		init(node);
	}

	public Dom4jNodeConfiguration(Dom4jTreeModel treeModel)
	{
		init(treeModel.m_ele);
	}

	public void addAttribute(Object handler, String key, int iVal)
	{
		addAttribute(handler, key, Integer.toString(iVal));
	}

	public void addAttribute(Object node, String key, String val)
	{
		if (node instanceof Element)
		{
			Element ele = (Element) node;
			synchronized (ele.getDocument())
			{
				ele.addAttribute(key, val);
			}
		}
	}

	/**
	 * set the valuie for the specific attribute
	 * @param handler
	 * @param val
	 */
	public void setAttributeValue(Object handler, String val)
	{
		if (handler instanceof Attribute)
		{
			Attribute attr = (Attribute) handler;
			synchronized (attr.getDocument())
			{
				attr.setValue(val);
			}
		}
	}

	public String asXML()
	{
		return asXML(null);
	}

	public String asXML(String charset)
	{
		if (charset != null)
		{
			m_node.getDocument().setXMLEncoding(charset);
		}
		return m_node.asXML();
	}

	protected void init(Node node)
	{
		m_node = node;
	}

	/**
	 * create one level node
	 * @param parentNodePath
	 * @param newNodeName
	 * @throws Exception
	 */
	public Object createNode(Object handler, String newNodeName)
	{
		Object retval = null;
		Branch branch = m_node == null ? null : (Branch) m_node;
		if (handler instanceof Branch)
		{
			branch = (Branch) handler;
		}

		synchronized (branch.getDocument())
		{
			retval = branch.addElement(newNodeName);
		}
		return retval;
	}

	/**
	 * fetch the value for specific attribute.
	 * @param handler
	 * @return
	 */
	public String getAttributeValue(Object node, String key)
	{
		String retval = null;
		if (node instanceof Element)
		{
			Element ele = (Element) node;
			Attribute attr = ele.attribute(key);
			if (attr != null)
			{
				synchronized (ele.getDocument())
				{
					retval = attr.getValue();
				}
			}
		}
		return retval;
	}

	public Map getAttributes(Object node)
	{
		Map retval = null;
		Attribute attr = null;
		if (node instanceof Element)
		{
			Element ele = (Element) node;
			retval = new LinkedHashMap(ele.attributeCount());
			Iterator it = ele.attributeIterator();
			while (it.hasNext())
			{
				attr = (Attribute) it.next();
				retval.put(attr.getName(), attr.getValue());
			}
		} else if (node instanceof Collection)
		{
			Iterator it = ((Collection) node).iterator();
			retval = new LinkedHashMap(((Collection) node).size());
			while (it.hasNext())
			{
				attr = (Attribute) it.next();
				retval.put(attr.getName(), attr.getValue());
			}
		}
		return retval;
	}

	public Object getHandler(Object base, String nodePath) throws Exception
	{
		Object retval = null;
		Dom4jXPath xpath = new Dom4jXPath(nodePath);
		Node node = (base == null) ? m_node : (Node) base;
		synchronized (node.getDocument())
		{
			retval = xpath.selectSingleNode(node);
		}
		// process for the attribute
		if (retval instanceof Attribute)
		{
			Attribute attr = (Attribute) retval;
			retval = attr.getValue();
		}
		return retval;
	}

	public List getHandlers(Object base, String nodePath) throws Exception
	{
		Dom4jXPath dom4jxpath = new Dom4jXPath(nodePath);
		return getHandlers(base, dom4jxpath);
	}

	public List getHandlers(Object base, Dom4jXPath xpath) throws Exception
	{
		List retval = null;
		Node node = (base == null) ? m_node : (Node) base;

		synchronized (node.getDocument())
		{
			retval = xpath.selectNodes(node);
		}

		return retval;
	}

	/**
	 * get the xpath info for the specific base.
	 * @param base
	 * @return
	 */
	public String getXPath(Object base)
	{
		return ((Node) base).getPath();
	}

	/**
	 * convert the specific object to string
	 */
	public static String stringValueOf(Object obj)
	{
		String retval = null;
		// process for the attribute
		if (Node.class.isAssignableFrom(obj.getClass()))
		{
			Node node = (Node) obj;
			retval = node.getText();
		} else
		{
			retval = obj.toString();
		}
		return retval;
	}

	/**
	 * convert the specific object to string
	 */
	public String toString(Object obj)
	{
		return stringValueOf(obj);
	}

	public void remove(Object handler)
	{
		if (handler != null && handler instanceof List)
		{
			List list = (List) handler;
			Iterator it = list.iterator();
			Object wk = null;
			synchronized (m_node.getDocument())
			{
				while (it.hasNext())
				{
					wk = it.next();
					if (wk instanceof Node)
					{
						((Node) wk).detach();
					}
				}
			}
		}
	}

}
