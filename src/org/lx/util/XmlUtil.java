package org.lx.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultDocument;
import org.lx.arch.ConfigurationView;
import org.lx.arch.Dom4jConfiguration;
import org.lx.arch.Dom4jNodeConfiguration;

public class XmlUtil
{
	protected static Element buildParent(Element targetElement, String buildPath)
	{
		Element retval = null;
		String workXpath = buildPath;
		int offset = -1;
		Object obj = null;

		Dom4jNodeConfiguration dom4jconf = new Dom4jNodeConfiguration(
				targetElement);
		ConfigurationView confView = new ConfigurationView(dom4jconf, null);

		while (true)
		{
			obj = confView.query(workXpath);
			if (obj != null)
			{
				break;
			}
			offset = workXpath.lastIndexOf("/");
			if (offset < 0)
			{
				break;
			}
			workXpath = workXpath.substring(0, offset);
		}

		Element ancientElement = obj == null ? targetElement : (Element) obj;
		if (workXpath.length() == buildPath.length())
		{
			retval = ancientElement;
		} else
		{
			String absentPath = buildPath.substring(workXpath.length() + 1);

			// modify the value base on the Document.
			String[] paths = StringEx.split(absentPath, "/");
			Element ele = ancientElement;

			// create the absent Element.
			for (int i = 0; i < paths.length; i++)
			{
				ele = ele.addElement(paths[i]);
			}

			retval = ele;
		}
		return retval;
	}

	public static String xcapAdd(String originalXML, String xpath, String value)
			throws DocumentException
	{
		return xcapAdd(originalXML, xpath, value, false);
	}

	public static String xcapAdd(String originalXML, String xpath,
			String value, boolean bSupportCreate) throws DocumentException
	{
		String retval = null;
		// 1. build the proerty.
		do
		{
			if (xpath == null)
			{
				// the value should be XML if xpath is null.
				retval = value;
				break;
			}

			String[] segments = null;
			try
			{
				segments = StringEx.parseParameters(xpath, '/', '[', ']');
			} catch (Exception e)
			{
				break;
			}

			// 1.initialize the root/leaf/parentPath
			String leafName = segments[segments.length - 1];
			String rootName = segments[0];
			if (rootName.length() == 0 && segments.length > 1)
			{
				rootName = segments[1];
			}

			// build the parent path
			String parentPath = null;
			if (segments.length > 1)
			{
				StringBuffer tmp = new StringBuffer();
				tmp.append(segments[0]);
				for (int i = 1; i < segments.length - 1; i++)
				{
					tmp.append('/');
					tmp.append(segments[i]);
				}
				parentPath = tmp.toString();
			}

			// 2.parse the existed object.
			Document doc = null;
			if (originalXML != null)
			{
				// read the old property and change it.
				SAXReader saxReader = new SAXReader();
				Reader reader = new StringReader(originalXML);

				doc = saxReader.read(reader);
			}

			if (doc == null)
			{ // create a new object.
				doc = new DefaultDocument();
				doc.addElement(rootName);
			}

			// get the parent.
			Dom4jConfiguration dom4jconf = new Dom4jConfiguration(doc);
			ConfigurationView confView = new ConfigurationView(dom4jconf, null);
			Element parent = (Element) confView.query(parentPath);

			if (parent == null && bSupportCreate)
			{
				parent = buildParent(doc.getRootElement(), parentPath);

			}

			// the element must be present here.
			if (parent == null)
			{
				break;
			}

			// 3. add the attribute to existed element.
			if (leafName.startsWith("@"))
			{
				leafName = leafName.substring(1);
				parent.addAttribute(leafName, value);
			} else
			{
				// 4.add/replace a element.

				// parse the value as xml node.
				SAXReader saxReader = new SAXReader();
				Document valueDoc = saxReader.read(new StringReader("<a>"
						+ value + "</a>"));
				Element root = valueDoc.getRootElement();
				List valueNodes = root.elements();

				// build the specific object.
				if (parentPath == null || parentPath.length() == 0
						|| parentPath == "/")
				{
					// set the value as the root.
					if (valueNodes.size() != 1)
					{
						// the root can only one element.
						break;
					}
					Element rootEle = (Element) valueNodes.get(0);
					// detach the node from the original tree firstly.
					rootEle.detach();
					doc.setRootElement(rootEle);
				} else
				{
					Dom4jNodeConfiguration dom4jNodeconf = new Dom4jNodeConfiguration(
							parent);
					confView = new ConfigurationView(dom4jNodeconf, null);
					List matches = confView.queryAll(leafName);
					// modify when the element refered by the xpath exists.
					if (matches != null && matches.size() > 0)
					{
						// remove
						Iterator it = matches.iterator();
						Element ele = null;
						while (it.hasNext())
						{
							ele = (Element) it.next();
							ele.detach();
						}
					}

					Element child = null;
					Iterator it = valueNodes.iterator();
					while (it.hasNext())
					{
						child = (Element) it.next();
						child.detach();
						parent.add(child);
					}
				}
			}
			retval = doc.asXML();

		} while (false);

		return retval;
	}

	public static String xcapRemove(String originalXML, String xpath)
	{
		String retval = null;
		do
		{
			if (xpath == null)
			{
				break;
			}

			// read the old property and change it.
			SAXReader saxReader = new SAXReader();
			Reader reader = new StringReader(originalXML);
			try
			{
				// get the XML document and remove the element/attribute.
				Document doc = new DefaultDocument();
				doc = saxReader.read(reader);

				Dom4jConfiguration dom4jconf = new Dom4jConfiguration(doc);
				ConfigurationView confView = new ConfigurationView(dom4jconf,
						null);
				List matchsNode = confView.queryAll(xpath);
				Iterator it = matchsNode.iterator();
				while (it.hasNext())
				{
					// delete this node.
					((Node) it.next()).detach();
				}
				retval = doc.asXML();

			} catch (DocumentException e)
			{
				break;
			}

		} while (false);
		return retval;
	}

	public static Object parseXpath(String value, String xpath)
			throws IOException
	{
		// convert the prop to XML
		// use the configurationView to handle the xpath
		Object retval = value;
		if (xpath != null)
		{
			Dom4jConfiguration conf = new Dom4jConfiguration(value, null);
			ConfigurationView confView = new ConfigurationView(conf, null);
			retval = confView.queryAll(xpath);
		}
		return retval;
	}

}