package org.lx.arch.expr;

public class CompareFunction implements IExprFunction
{
	public Object handle(Object[] params) throws ExprException
	{
		int result = params[0].toString().compareTo(params[1].toString());
		if (result < 0)
			result = -1;
		else if (result > 0)
			result = 1;
		return Integer.valueOf(result);
	}
}
