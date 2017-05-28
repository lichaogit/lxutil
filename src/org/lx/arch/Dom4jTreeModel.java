package org.lx.arch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.lx.util.SimpleTreeModel;

public class Dom4jTreeModel implements SimpleTreeModel
{
	Element m_ele;

	public Dom4jTreeModel(Element ele)
	{
		m_ele = ele;
	}

	public Element getElement()
	{
		return m_ele;
	}

	public Collection getChildren()
	{
		ArrayList al = new ArrayList();
		List l = m_ele.elements();
		if (l != null && l.size() > 0)
		{
			Iterator it = l.iterator();

			while (it.hasNext())
			{
				al.add(new Dom4jTreeModel((Element) it.next()));
			}

		}
		return al;
	}
}
