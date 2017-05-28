package org.lx.arch.logic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.lx.util.IMatcher;

public abstract class ConditionBuilder
{
	protected abstract IMatcher createItemMatcher(Map attrs);

	protected IMatcher createItemMatcher(Element model)
	{
		Iterator it = model.attributeIterator();
		Attribute attr = null;
		Map attrs = new HashMap(model.attributeCount());
		while (it.hasNext())
		{
			attr = (Attribute) it.next();
			attrs.put(attr.getName(), attr.getValue());
		}
		return createItemMatcher(attrs);
	}

	// create a internal model.
	public IMatcher compile(Element model) throws ConditionParserException
	{
		IMatcher retval = null;
		String tagName = model.getName().toLowerCase();
		// create a containerMatcher
		ContainerMatcher matcher = null;

		// process the container condition.
		if ("and".equals(tagName))
		{
			matcher = new AndMatcher();
		} else if ("or".equals(tagName))
		{
			matcher = new OrMatcher();
		} else if ("not".equals(tagName))
		{
			matcher = new NotMatcher();
		}

		if (matcher != null)
		{
			// the matcher is a container Matcher.
			Iterator it = model.elements().iterator();
			Element child = null;
			while (it.hasNext())
			{
				child = (Element) it.next();
				// 1.handle variable element.
				if ("variable".equals(child.getName()))
				{
					String name = null, value = null;

					Attribute attrObj = child.attribute("name");
					name = attrObj == null ? null : attrObj.getText();
					attrObj = child.attribute("value");
					value = attrObj == null ? null : attrObj.getText();
					matcher.addVar(name, value);
				} else
				{
					// 2.create child element.
					IMatcher childMatcher = compile(child);
					// if the container have variable.
					if (childMatcher instanceof AbstractItemMatcher)
					{
						// 2.1 get vars from parent.
						ContainerMatcher tmp = matcher;
						Map vars = null;
						while (tmp != null)
						{
							vars = tmp.getVars();
							if (vars != null)
							{
								break;
							}
							tmp = tmp.getParent();
						}
						((AbstractItemMatcher) childMatcher).setVars(vars);
					} else if (childMatcher instanceof ContainerMatcher)
					{
						((ContainerMatcher) childMatcher).setParent(matcher);
					}
					matcher.addChild(childMatcher);
				}
			}
			retval = matcher;
		} else
		{
			// the current model is item(leaf) model.
			retval = createItemMatcher(model);
		}
		return retval;
	}
}
