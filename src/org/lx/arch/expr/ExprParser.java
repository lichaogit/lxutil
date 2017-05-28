package org.lx.arch.expr;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lx.util.StringEx;
import org.lx.util.ValueRender;

public class ExprParser
{

	protected final static String EXPR_VAR_MATCHER = "\\$\\(([\\w-/]+)\\)";// '\$\((\w+\)'

	HashMap m_func = new HashMap();

	public synchronized void registerFunction(String funName, IExprFunction fun)
	{
		m_func.put(funName, fun);
	}

	public synchronized IExprFunction getFunction(String funName)
	{
		Object obj = m_func.get(funName);
		return obj == null ? null : (IExprFunction) obj;
	}

	public String parse(String express, ValueRender vr) throws Exception
	{
		String retval = null;
		String funcRE = "(\\w+){1}\\((.+)\\){1}";

		Object result = null;
		StringBuffer resultBuf = new StringBuffer();

		Pattern funcP = Pattern.compile(funcRE);
		Matcher matcher = funcP.matcher(express);

		String funcName = null;
		String param = null;
		Matcher paramMatcher = null;
		Object[] params = null;
		String[] rawParams = null;

		StringBuffer paramBuf = new StringBuffer();
		while (matcher.find())
		{
			funcName = matcher.group(1);
			param = matcher.group(2);
			rawParams = StringEx.parseParameters(param, ',', '(', ')');
			params = new Object[rawParams.length];
			System.arraycopy(rawParams, 0, params, 0, rawParams.length);

			// 1.process the parameters.
			for (int i = 0; i < params.length; i++)
			{
				param = params[i].toString();
				paramMatcher = funcP.matcher(param);
				// 1.1 clear the buf before any operation.
				if (paramBuf.length() > 0)
				{
					paramBuf.delete(0, paramBuf.length());
				}
				while (paramMatcher.find())
				{
					paramMatcher.appendReplacement(paramBuf,
							parse(paramMatcher.group(0), vr));
				}
				// 1.2 process if the parameter contain any function.
				if (paramBuf.length() > 0)
				{
					paramMatcher.appendTail(paramBuf);
					params[i] = paramBuf.toString();
				} else
				{
					// the param don't contain any other functions.
					params[i] = StringEx.parseVar(params[i].toString(),
							EXPR_VAR_MATCHER, vr);
				}
			}
			// 2.invoke the function.
			IExprFunction func = getFunction(funcName);
			if (func == null)
			{
				throw new ExprException("Unkown function:" + funcName);
			}
			result = func.handle(params);

			// process the return value
			matcher.appendReplacement(resultBuf, result.toString());
		}

		matcher.appendTail(resultBuf);
		retval = resultBuf.toString();
		// the plain string.
		if (retval == null)
		{
			retval = express;
		}
		return retval;
	}

	protected static ExprParser PARSER = null;

	public static ExprParser getDefaultExprParser()
	{
		if (PARSER == null)
		{
			PARSER = new ExprParser();
			PARSER.registerFunction("equals", new EqualsFunction());
			PARSER.registerFunction("crc", new Adler32Function());
			PARSER.registerFunction("compare", new CompareFunction());
			PARSER.registerFunction("isInArray", new IsInArrayFunction());
			PARSER.registerFunction("propertyAt", new PropertyFunction());
			PARSER.registerFunction("xpath", new XPathFunction());
			PARSER.registerFunction("regex", new RegexFunction());
		}
		return PARSER;
	}
}
