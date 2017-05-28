package org.lx.arch.plugin;

import java.util.Map;

public abstract class AbstractAction implements IAction
{
	protected String getParameter(Map param, String attrname)
	{
		Object obj = param.get(attrname);
		return obj == null ? null : String.valueOf(obj);
	}

}
