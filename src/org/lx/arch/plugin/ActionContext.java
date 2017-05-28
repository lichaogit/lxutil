package org.lx.arch.plugin;

import java.util.Map;

public class ActionContext implements IActionContext
{

	Map _params;

	Object _result;

	public ActionContext(Map params)
	{
		_params = params;
	}

	@Override
	public Object getParameter(String attrname)
	{
		return _params.get(attrname);
	}

	@Override
	public void setReturnResult(Object val)
	{
		_result = val;
	}

	public Object getReturnResult()
	{
		return _result;
	}

	@Override
	public void setParameter(String attrname, Object val)
	{
		_params.put(attrname, val);
	}

	@Override
	public Map getParameters()
	{
		return _params;
	}
}
