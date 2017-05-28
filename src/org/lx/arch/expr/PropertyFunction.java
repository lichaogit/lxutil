package org.lx.arch.expr;

import java.util.Map;

public class PropertyFunction implements IExprFunction
{

	public Object handle(Object[] params) throws ExprException
	{
		Map props = (Map) params[0];
		Object key = params[1];
		return props.get(key);
	}
}
