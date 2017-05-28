package org.lx.xml;

import java.util.Iterator;
import java.util.Map;

/**
 * <p>Title: </p> <p>Description: it is a general model for XML information</p>
 * <p>Copyright: Copyright (c) 2003</p> <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class XMLElementModel extends XMLElementCoreModel
{

	// the text locate the begin tag and end tag <tag> text </tag>
	public XMLElementModel()
	{
	}

	public String getAttribute(String key)
	{
		Object obj = super.getAttribute(key);
		return obj == null ? null : obj.toString();

	}

	/**
	 * getChild by the tag name
	 * @param tagName
	 * @return
	 */
	public XMLElementModel getChild(String tagName)
	{
		Object obj = getChild(tagName, null, null);
		return obj == null ? null : (XMLElementModel) obj;
	}

	/**
	 * get the attribute recursive through the parent.
	 * @param key
	 * @return
	 */
	public String getRecursiveAttribute(String key)
	{
		Object obj = super.getRecursiveAttribute(key);
		return obj == null ? null : obj.toString();

	}

	public XMLElementModel getChild(String[] tagPath)
	{
		Object[] dummy = new String[tagPath.length];
		for (int i = 0; i < tagPath.length; i++)
		{
			dummy[0] = null;
		}
		Object obj = getChild(tagPath, dummy, dummy);
		return obj == null ? null : (XMLElementModel) obj;
	}

	/**
	 * get the specific child by attribute's info
	 * @param attrName
	 * @param value
	 * @return
	 */
	public XMLElementModel getChild(String attrName, String value)
	{
		Object obj = getChild(null, attrName, value);
		return obj == null ? null : (XMLElementModel) obj;
	}

	/**
	 * get the first child by tag and attributes name.
	 * @param tag,
	 *        tag's name, ignore it if it is null.
	 * @param attrName,
	 *        attributes's name, ignore it if it is null.
	 * @param value
	 * @return
	 */
	public XMLElementModel getChild(String tagName, String attrName,
			String value)
	{
		Object obj = super.getChild(tagName, attrName, value);
		return obj == null ? null : (XMLElementModel) obj;
	}

	/**
	 * get the child by the specfic path
	 * @param attr
	 * @param values
	 * @return
	 */
	public XMLElementModel getChild(String attr, String[] values)
	{
		String[] attrs = new String[values.length];
		for (int i = 0; i < attrs.length; i++)
		{
			attrs[i] = attr;
		}
		return getChild(attrs, values);
	}

	/**
	 * remove the specific child element.
	 * @param attr,
	 *        the attribute.
	 * @param values,
	 */
	public void removeChild(String attr, String[] values)
	{
		XMLElementCoreModel child = getChild(attr, values);
		if (child != null)
		{
			child = child.getParent();
			child.removeChild(attr, values[values.length - 1]);
		}
	}

	public XMLElementModel getChild(String[] attrs, String[] values)
	{
		Object obj = super.getChild(attrs, values);
		return obj == null ? null : (XMLElementModel) obj;
	}

	/**
	 * clone the current XMLElementCoreModel info.
	 */
	public Object clone()
	{
		XMLElementModel retval = new XMLElementModel();
		retval.setTag(getTag());
		retval.addAttribute(getAttribute());
		retval.setParent(getParent());
		int childs_count = getChildrenSize();
		if (childs_count > 0)
		{
			Iterator it = getChildren().iterator();
			XMLElementCoreModel c = null;
			while (it.hasNext())
			{
				c = (XMLElementModel) it.next();
				retval.add((XMLElementModel) c.clone());
			}
		}
		return retval;
	}

	public XMLElementModel(String tag, Map attrMap)
	{
		super(tag, attrMap);
	}

}