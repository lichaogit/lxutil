package org.lx.arch.expr;

public class EqualsFunction implements IExprFunction
{
	public Object handle(Object[] params) throws ExprException
	{
		return Boolean.valueOf(String.valueOf(params[0]).equals(params[1]));
	}
}
