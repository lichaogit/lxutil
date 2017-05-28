package org.lx.arch.expr;

import java.util.regex.Pattern;

public class RegexFunction implements IExprFunction
{
	public Object handle(Object[] params) throws ExprException
	{
		String regex = params[0].toString();
		Object val = params[1];
		Pattern p = Pattern.compile(regex);
		return Boolean.valueOf(p.matcher(val.toString()).matches());
	}
}
