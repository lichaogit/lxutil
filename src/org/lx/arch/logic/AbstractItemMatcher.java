package org.lx.arch.logic;

import java.util.Collection;
import java.util.Map;

import org.lx.util.IMatcher;

public abstract class AbstractItemMatcher implements IMatcher
{
	Map m_vars;

	protected void setVars(Map vars)
	{
		m_vars = vars;
	}

	protected Map getVars()
	{
		return m_vars;
	}
}
