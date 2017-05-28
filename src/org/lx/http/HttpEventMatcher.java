package org.lx.http;

import java.net.HttpURLConnection;
import java.net.URLConnection;

import org.lx.http.HttpClient.HttpEvent;
import org.lx.util.IMatcher;
import org.lx.util.StringEx;

public class HttpEventMatcher implements IMatcher
{
	String type;

	String name;

	String value;

	public HttpEventMatcher(String type, String name, String value)
	{
		this.type = type;
		this.name = name;
		this.value = value;
	}

	public boolean isMatch(Object o)
	{
		boolean retval = false;
		do
		{
			if (o instanceof HttpEvent == false)
			{
				break;
			}
			HttpEvent event = (HttpEvent) o;
			if ("responseCode".equals(type))
			{
				if (event.responseCode != Integer.parseInt(value))
				{
					break;
				}
			} else if ("response".equals(type) || "request".equals(type))
			{
				URLConnection uc = (URLConnection) event.getSource();
				String prop = null;
				if ("response".equals(type))
				{
					prop = uc.getHeaderField(name);

				} else
				{
					if ("method".equalsIgnoreCase(name))
					{
						if (uc instanceof HttpURLConnection)
						{
							prop = ((HttpURLConnection) uc).getRequestMethod();
						}
					} else
					{
						prop = uc.getRequestProperty(name);
					}
				}

				if (prop == null)
				{
					break;
				}

				if (StringEx.regexFind(prop, value, 0) == null)
				{
					break;
				}
			}

			retval = true;

		} while (false);

		return retval;
	}
}