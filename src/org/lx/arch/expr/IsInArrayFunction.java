package org.lx.arch.expr;

import org.lx.util.LogicUtil;

public class IsInArrayFunction implements IExprFunction
{
	public Object handle(Object[] params) throws ExprException
	{
		Object val = params[0];
		Object vals = params[1];
		Object[] paramsArray = null;
		if (vals.getClass().isArray())
		{
			paramsArray = ((Object[]) vals);
		} else
		{
			paramsArray = vals.toString().split(";");
		}
		return Boolean.valueOf(LogicUtil.isInArray(paramsArray, val));
	}
}
