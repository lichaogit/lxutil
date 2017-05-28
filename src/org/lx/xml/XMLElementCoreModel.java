package org.lx.xml;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.lx.util.LogicUtil;
import org.lx.util.SimpleTreeModel;

public class XMLElementCoreModel implements SimpleTreeModel
{
	public static class DefaultMatchCondition implements
			LogicUtil.MatchCondition
	{

		String m_tagName;

		Object m_attrName;

		Object m_value;

		boolean m_bContinue;

		boolean m_bCase;

		public DefaultMatchCondition(String tagName, Object attrName,
				Object value, boolean bContinue, boolean bCase)
		{
			m_tagName = tagName;
			m_attrName = attrName;
			m_value = value;
			m_bCase = bCase;
			m_bContinue = bContinue;
		}

		public DefaultMatchCondition(String tagName, Object attrName,
				Object value)
		{
			this(tagName, attrName, value, false, false);
		}

		/**
		 * if the model match the info, return true; Match rules: the tag and
		 * attr's value must match with the Map, <P> it will be ignore if the
		 * attr or tag is null.
		 * @param model
		 * @param info
		 * @return
		 */
		public boolean isMatch(SimpleTreeModel model_data)
		{
			XMLElementCoreModel model = (XMLElementCoreModel) model_data;
			boolean retval = false;
			do
			{
				Object workValue = null;
				if (m_tagName != null)
				{
					if (m_bCase == true)
					{
						if (m_tagName.equals(model.getTag()) == false)
						{
							break;
						}
					} else
					{
						if (m_tagName.equalsIgnoreCase(model.getTag()) == false)
						{
							break;
						}
					}
				}
				// 2. if the attribute is not null, check it.
				if (m_attrName != null)
				{
					workValue = model.getAttribute(m_attrName);
					if (workValue != null)
					{
						if (m_bCase == true)
						{
							if (workValue.equals(m_value) == false)
							{
								break;
							}
						} else
						{
							if (workValue.equals(m_value) == false)
							{
								break;
							}
						}
					} else if (m_value != workValue) // break if the
					// workValue==null and
					// the m_value is not
					// null, ;
					{
						break;
					}
				}
				retval = true;
			} while (false);
			return retval;
		}

		/**
		 * determine whether to search the node.
		 * @param node
		 * @return
		 */
		public boolean searchContinue()
		{
			return m_bContinue;
		}

	}

	protected String tag;

	protected Map attributes;

	protected Vector childs;

	protected XMLElementCoreModel parent;

	protected String text;

	// the text locate the begin tag and end tag <tag> text </tag>
	public XMLElementCoreModel()
	{
	}

	public XMLElementCoreModel(String tag, Map attrMap)
	{
		if (attrMap != null)
		{
			attributes = new HashMap();
			attributes.putAll(attrMap);
		}
		this.tag = tag;
	}

	/**
	 * @param awriter
	 * @param header
	 * @throws IOException
	 */
	public void store(BufferedWriter awriter) throws IOException
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<");
		sb.append(tag);
		// write the attribute
		if (attributes != null && attributes.size() > 0)
		{
			Iterator it = attributes.keySet().iterator();
			String key = null;
			while (it.hasNext())
			{
				key = (String) it.next();
				sb.append(" ");
				sb.append(key);
				sb.append("=\"");
				sb.append(attributes.get(key).toString());
				sb.append("\"");
			}
		}
		if (childs != null && childs.size() > 0)
		{
			sb.append(">");
			writeln(awriter, sb.toString());
			sb.delete(0, sb.length());
			Iterator it = childs.iterator();
			XMLElementCoreModel model = null;
			while (it.hasNext())
			{
				model = (XMLElementCoreModel) it.next();
				model.store(awriter);
			}

			sb.append("</");
			sb.append(tag);
			sb.append(">");
		} else
		{
			sb.append("/>");
		}
		writeln(awriter, sb.toString());
	}

	public void store(OutputStream out, String header) throws IOException
	{
		store(out, header, "8859_1");
	}

	/**
	 * store the XMLElement to the out.
	 * @param out
	 * @param header
	 * @param charsetName
	 * @throws IOException
	 */
	public void store(OutputStream out, String header, String charsetName)
			throws IOException
	{
		// 1. write the time stap.
		BufferedWriter awriter = new BufferedWriter(new OutputStreamWriter(out,
				charsetName));
		writeln(awriter, "<!--" + new Date().toString() + "-->");
		// 2. write the header.
		if (header != null)
		{
			writeln(awriter, header);
		}
		store(awriter);
		awriter.flush();
	}

	private static void writeln(BufferedWriter bw, String s) throws IOException
	{
		bw.write(s);
		bw.newLine();
	}

	public Collection getChildren()
	{
		return childs;
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
	public XMLElementCoreModel getChild(String tagName, Object attrName,
			Object value)
	{
		ArrayList array = new ArrayList(1);
		LogicUtil.shallowSearch(this, array, new DefaultMatchCondition(tagName,
				attrName, value, false, false));
		return array.size() > 0 ? (XMLElementCoreModel) array.get(0) : null;
	}

	/**
	 * get the all of childs by tag and attributes name.
	 * @param tag,
	 *        tag's name, ignore it if it is null.
	 * @param attrName,
	 *        attributes's name, ignore it if it is null.
	 * @param value
	 * @return
	 */
	public Collection getChildren(Object attrName, Object value)
	{
		return getChildren(null, attrName, value);
	}

	public int getChildrenSize()
	{
		return childs == null ? 0 : childs.size();
	}

	/**
	 * get the all of childs by tag and attributes name.
	 * @param tag,
	 *        tag's name, ignore it if it is null.
	 * @param attrName,
	 *        attributes's name, ignore it if it is null.
	 * @param value
	 * @return
	 */
	public Collection getChildren(String tagName, Object attrName, Object value)
	{
		ArrayList array = new ArrayList();
		LogicUtil.shallowSearch(this, array, new DefaultMatchCondition(tagName,
				attrName, value, true, true));
		return array;
	}

	/**
	 * get the child by the specfic path
	 * @param attr
	 * @param value
	 * @return
	 */
	public XMLElementCoreModel getChild(Object[] attrs, Object[] values)
	{
		return getChild((String[]) null, attrs, values);
	}

	/**
	 * [core]get the child by the specfic attrPath and tagpath.
	 * @param tagPath
	 * @param attrs
	 * @param values
	 * @return
	 */
	public XMLElementCoreModel getChild(String[] tagPath, Object[] attrs,
			Object[] values)
	{
		XMLElementCoreModel retval = null;
		if (attrs.length <= values.length)
		{
			XMLElementCoreModel work = this;
			boolean bfind = true;
			for (int i = 0; i < attrs.length; i++)
			{
				work = tagPath != null && tagPath.length > i ? work.getChild(
						tagPath[i], attrs[i], values[i]) : work.getChild(null,
						attrs[i], values[i]);
				if (work == null)
				{
					bfind = false;
					break;
				}
			}
			if (bfind)
			{
				retval = work;
			}
		}
		return retval;
	}

	public Map getAttribute()
	{
		return attributes;
	}

	public void setAttributes(Map attrMap)
	{
		this.attributes = attrMap;
	}

	/**
	 * get the attribute case senseiive.
	 * @param key
	 * @return
	 */
	public Object getAttribute(Object key)
	{
		return attributes == null ? null : attributes.get(key);
	}

	public boolean hasAttribute(Object key)
	{
		boolean retval = false;
		if (attributes != null)
		{
			retval = attributes.containsValue(key);
		}
		return retval;
	}

	public void setParent(XMLElementCoreModel parent)
	{
		this.parent = parent;
	}

	public XMLElementCoreModel getParent()
	{
		return parent;
	}

	/**
	 * get the root
	 * @return
	 */
	public XMLElementCoreModel getRoot()
	{
		XMLElementCoreModel retval = this;
		while (retval.getParent() != null)
		{
			retval = retval.getParent();
		}
		return retval;
	}

	/**
	 * get the attribute recursive through the parent.
	 * @param key
	 * @return
	 */
	public Object getRecursiveAttribute(Object key)
	{
		Object retval = null;
		XMLElementCoreModel model = this;
		while (model != null)
		{
			retval = model.getAttribute(key);
			if (retval != null)
			{
				break;
			}
			model = model.getParent();
		}
		return retval;
	}

	public XMLElementCoreModel getLastElementByLevel(int level)
	{
		XMLElementCoreModel retval = null;
		XMLElementCoreModel work = this;
		int i = 0;
		for (; i < level; i++)
		{
			if (work == null)
			{
				break;
			}
			if (work.childs == null)
			{
				break;
			}
			if (work.childs.size() == 0)
			{
				break;
			}
			work = (XMLElementCoreModel) work.childs.lastElement();
		}
		if (i != level)
		{
			retval = null;
		} else
		{
			retval = work;
		}
		return retval;
	}

	/**
	 * add attributes to the current model.
	 * @param attrMap
	 *        the attributes will be added in.
	 */
	public void addAttribute(Map attrMap)
	{
		if (attrMap != null)
		{
			if (attributes == null)
			{
				attributes = new HashMap();
			}
			attributes.putAll(attrMap);
		}
	}

	public void addAttribute(Object attrName, Object value)
	{
		if (attributes == null)
		{
			attributes = new HashMap();
		}
		attributes.put(attrName, value);
	}

	public void removeAllAttributes()
	{
		attributes.clear();
	}

	public void removeAttribute(Object key)
	{
		attributes.remove(key);
	}

	public String getText()
	{
		return this.text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public String getTag()
	{
		return this.tag;
	}

	public void setTag(String tag)
	{
		this.tag = tag;
	}

	/**
	 * add a batch subElements in the tail.
	 * @param elements,
	 *        element collection
	 */
	public void add(Collection elements)
	{
		if (elements == null)
		{
			return;
		}
		if (childs == null)
		{
			childs = new Vector();
		}
		Iterator it = elements.iterator();
		while (it.hasNext())
		{
			add((XMLElementCoreModel) it.next());
		}
	}

	/**
	 * insert a XMLElementCoreModel as the current model's subElement in
	 * specific index.
	 * @param ele:
	 *        the subElement
	 * @param index
	 *        the insert index.
	 */
	public void add(XMLElementCoreModel ele, int index)
	{
		childs.insertElementAt(ele, index);
	}

	/**
	 * add a subElement in the tail.
	 * @param ele
	 *        the subElement.
	 */
	public void add(XMLElementCoreModel ele)
	{
		if (ele == null)
		{
			return;
		}
		if (childs == null)
		{
			childs = new Vector();
		}
		childs.add(ele);
		ele.parent = this;
	}

	/**
	 * remove all of the subElements.
	 */
	public void removeAllChilds()
	{
		if (childs != null)
		{
			childs.removeAllElements();
		}
	}

	/**
	 * remove the child.
	 * @param attr
	 * @param value
	 */
	public void removeChild(Object attr, Object value)
	{
		Iterator it = childs.iterator();
		XMLElementCoreModel model = null;
		Object work_value = null;
		while (it.hasNext())
		{
			model = (XMLElementCoreModel) it.next();
			work_value = model.getAttribute(attr);
			if (work_value != null && work_value.equals(value)
					|| work_value == value)
			{
				it.remove();
				break;
			}
		}
	}

	/**
	 * remove the child.
	 * @param attrs,
	 *        the attribute path.
	 * @param values
	 */
	public void removeChild(Object[] attrs, Object[] values)
	{

		XMLElementCoreModel child = getChild(attrs, values);
		if (child != null)
		{
			child = child.getParent();
			child
					.removeChild(attrs[attrs.length - 1],
							values[attrs.length - 1]);
		}
	}

	/**
	 * clone the current XMLElementCoreModel info.
	 */
	public Object clone()
	{
		XMLElementCoreModel retval = new XMLElementCoreModel();
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
				c = (XMLElementCoreModel) it.next();
				retval.add((XMLElementCoreModel) c.clone());
			}
		}
		return retval;
	}
}
