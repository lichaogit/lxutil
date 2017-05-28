package org.lx.crawler;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lx.util.StringEx;

public class Request
{
	protected String uri;

	protected String user;

	protected Map params;

	protected Object result;

	public Request(String uri, String user, Map params)
	{
		this.uri = uri;
		this.user = user;
		this.params = params;
	}

	public String getUser()
	{
		return user;
	}

	public String getParameter(String key)
	{
		return params == null ? null : (String) params.get(key);
	}

	public String getURI()
	{
		return uri;
	}

	public int hashCode()
	{
		StringBuffer sb = new StringBuffer();
		if (params != null)
		{
			sb.append(params.hashCode());
		}
		sb.append(uri);
		// attention:the string.hashCode is different with the
		// StringBuffer.hashcode.
		return sb.toString().hashCode();
	}

	public String getProtocol()
	{
		return getProtocol(getURI());
	}

	public static String getProtocol(String url)
	{
		String retval = null;

		Pattern p = Pattern.compile(StringEx.DEFAULT_URLDOMAIN_MATCHER);
		Matcher matcher = p.matcher(url);
		if (matcher.find())
		{
			retval = matcher.group(1);
		}
		return retval;
	}
}
