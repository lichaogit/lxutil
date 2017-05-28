package org.lx.arch;

import java.util.List;
import java.util.Map;

public interface Configuration
{
	/**
	 * get the xpath info for the specific base.
	 * @param base
	 * @return
	 */
	public String getXPath(Object base);

	/**
	 * return the handle specified by the nodePatch, base is the current node,
	 * the current node is root if the base is null.
	 * @param base
	 * @param nodePath
	 * @return
	 * @throws Exception
	 */
	public Object getHandler(Object base, String nodePath)throws Exception;

	public List getHandlers(Object base, String nodePath)throws Exception;

	/**
	 * create one level node
	 * @param parentNodePath
	 * @param newNodeName
	 * @throws Exception
	 */
	public Object createNode(Object parentNode, String nodeName);

	/**
	 * add a attribute for a element
	 * @param handler
	 * @param key
	 * @param val
	 */
	public void addAttribute(Object node, String key, String val);

	/**
	 * fetch the value for specific attribute.
	 * @param handler
	 * @return
	 */
	public String getAttributeValue(Object node, String key);
	
	public Map getAttributes(Object node);

	public String asXML();

	/**
	 * convert the specific object to string
	 */
	public String toString(Object obj);
}
