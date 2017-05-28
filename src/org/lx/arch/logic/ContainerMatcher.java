package org.lx.arch.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.lx.util.IMatcher;

public abstract class ContainerMatcher implements IMatcher
{
	protected Collection childs = new ArrayList(5);

	protected HashMap m_vars;

	protected ContainerMatcher m_parent = null;

	public void addChild(IMatcher matcher) throws ConditionParserException
	{
		childs.add(matcher);
	}

	protected void addVar(String name, String value)
	{
		if (m_vars == null)
		{
			m_vars = new HashMap(5);
		}
		m_vars.put(name, value);
	}

	protected Map getVars()
	{
		return m_vars;
	}

	public ContainerMatcher()
	{

	}

	public ContainerMatcher(Collection childs)
	{
		this.childs.addAll(childs);
	}

	public void setParent(ContainerMatcher parent)
	{
		m_parent = parent;
	}

	public ContainerMatcher getParent()
	{
		return m_parent;
	}
}
