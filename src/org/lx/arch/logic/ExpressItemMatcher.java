package org.lx.arch.logic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.lx.arch.expr.ExprParser;
import org.lx.util.InheritValueRender;
import org.lx.util.MapValueRender;
import org.lx.util.ValueRender;

public class ExpressItemMatcher extends AbstractItemMatcher
{
	private String m_value = null;

	Map attrs;

	ExprParser m_parser;

	public ExpressItemMatcher(Map attrs, ExprParser parser)
	{
		this.attrs = attrs;
		this.m_parser = parser;
		m_value = (String) attrs.get("value");
	}

	public boolean isMatch(Object params)
	{
		boolean retval = true;
		try
		{ // check whether the var exists
			Map vars = getVars();
			if (vars != null && vars.size() > 0
					&& params instanceof ValueRender)
			{
				Iterator it = vars.keySet().iterator();
				String name = null;
				String value = null;
				HashMap varsMap = new HashMap(vars.size());
				while (it.hasNext())
				{
					name = (String) it.next();
					value = (String) vars.get(name);
					// parse the value base on the type.
					Object oValue = parse(value, params);
					if (oValue != null)
					{
						varsMap.put(name, oValue);
					}
				}
				ValueRender finalVr = null;
				if (varsMap.size() > 0)
				{
					finalVr = new InheritValueRender(
							new MapValueRender(varsMap), (ValueRender) params);
					params = finalVr;
				}
			}

			Object parseVal;

			parseVal = parse(m_value, params);

			if (parseVal instanceof Boolean)
			{
				retval = ((Boolean) parseVal).booleanValue();
			} else if (parseVal != null)
			{
				retval = Boolean.parseBoolean(parseVal.toString());
			}
		} catch (Exception e)
		{
			// do nothing
		}
		return retval;
	}

	protected Object parse(String expr, Object params) throws Exception
	{
		return m_parser.parse(expr, (ValueRender) params);
	}
}
