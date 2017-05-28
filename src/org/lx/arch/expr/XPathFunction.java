package org.lx.arch.expr;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.lx.arch.ConfigurationView;
import org.lx.arch.Dom4jConfiguration;

public class XPathFunction implements IExprFunction
{
	public Object handle(Object[] params) throws ExprException
	{
		Object retval = null;
		String xpath = params[0].toString();
		Object val = params[1];

		Dom4jConfiguration conf = null;
		try
		{
			if (val instanceof String)
			{
				conf = new Dom4jConfiguration(new StringReader((String) val),
						null);

			} else if (val instanceof Reader)
			{
				conf = new Dom4jConfiguration((Reader) val, null);
			}

			ConfigurationView confView = new ConfigurationView(conf, null);
			retval = confView.query(xpath);
		} catch (IOException e)
		{

		}
		return retval;
	}
}
