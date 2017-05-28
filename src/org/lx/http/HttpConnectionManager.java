package org.lx.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.htmlparser.http.ConnectionManager;

public class HttpConnectionManager extends ConnectionManager
{
	public boolean onPreConnection(URLConnection conn)
	{
		boolean retval = true;
		return retval;
	}

	public void onPostConnection(URLConnection conn, HttpRequest request)
			throws IOException
	{
		if (conn instanceof HttpURLConnection)
		{
			HttpURLConnection http = (HttpURLConnection) conn;
			// process the HttpParameters
			if (request != null)
			{

				// process the body.
				String body = request.getBody();
				String cs = request.getCharset();
				if (body != null && "POST".equals(request.getMethod()))
				{
					http.getOutputStream().write(body.toString().getBytes(cs));
				}
			}

			if (getCookieProcessingEnabled())
			{
				parseCookies(conn);
			}
		}
	}

	public boolean onPreOpenConnection()
	{
		boolean retval = true;
		do
		{
			// set up for proxy
			if ((null == getProxyHost()) || (0 == getProxyPort()))
			{
				break;
			}
			Properties sysprops;
			String set = null; // old proxySet value
			String host = null; // old proxyHost value
			String port = null; // old proxyPort value
			String host2 = null; // old http.proxyHost value
			String port2 = null; // old http.proxyPort value

			sysprops = System.getProperties();
			set = (String) sysprops.put("proxySet", "true");
			host = (String) sysprops.put("proxyHost", getProxyHost());
			port = (String) sysprops.put("proxyPort",
					Integer.toString(getProxyPort()));
			// see http://java.sun.com/j2se/1.4.2/docs/guide/net/properties.html
			host2 = (String) sysprops.put("http.proxyHost", getProxyHost());
			port2 = (String) sysprops.put("http.proxyPort",
					Integer.toString(getProxyPort()));
			System.setProperties(sysprops);

		} while (false);
		return retval;
	}

	public void onPostOpenConnection(URLConnection conn, HttpRequest request)
			throws ProtocolException, IOException
	{
		// set cookie here
		conn.setDoInput(true);
		conn.setDoOutput(true);
		if (conn instanceof HttpURLConnection)
		{
			HttpURLConnection http = (HttpURLConnection) conn;

			if (getRedirectionProcessingEnabled())
				http.setInstanceFollowRedirects(false);

			// set the fixed request properties
			Hashtable properties = getRequestProperties();
			if (null != properties)
			{
				String key = null;
				String value = null;
				for (Enumeration enumeration = properties.keys(); enumeration
						.hasMoreElements();)
				{
					key = (String) enumeration.nextElement();
					value = (String) properties.get(key);
					http.setRequestProperty(key, value);
				}
			}
			// set the proxy name and password
			try
			{
				String auth = null;
				String encoded = null;
				String charset = "ISO-8859-1";
				if (request != null)
				{
					charset = request.getCharset();
				}
				if ((null != getProxyUser()) && (null != getProxyPassword()))
				{
					auth = getProxyUser() + ":" + getProxyPassword();
					encoded = encode(auth.getBytes(charset));
					http.setRequestProperty("Proxy-Authorization", encoded);
				}

				// set the URL name and password
				if ((null != getUser()) && (null != getPassword()))
				{
					auth = getUser() + ":" + getPassword();
					encoded = encode(auth.getBytes(charset));
					http.setRequestProperty("Authorization", "Basic " + encoded);
				}

				if (getCookieProcessingEnabled())
					// set the cookies based on the url
					addCookies(http);
			} catch (UnsupportedEncodingException e)
			{
				// ISO-8859 must be supportted.
			}

			// set the user headers with the HttpParameters info.
			if (request != null)
			{
				// process the method.
				String method = request.getMethod();
				if (method != null)
				{
					http.setRequestMethod(method);
				}
				// process headers.
				String[][] headers = request.getHeaders();
				if (headers != null)
				{
					for (int i = 0; i < headers.length; i++)
					{
						http.setRequestProperty(headers[i][0], headers[i][1]);
					}
				}
				// process cookie
				String[][] cookies = request.getCookies();
				if (cookies != null)
				{
					for (int i = 0; i < cookies.length; i++)
					{
						// http.setRequestProperty(headers[i][0],
						// headers[i][1]);
					}
				}
			}
		}
	}

	public URLConnection open(HttpRequest request) throws IOException
	{
		// open a Http Connection with the specific parameters.
		URLConnection retval = null;
		if (onPreOpenConnection())
		{
			URL url = new URL(request.getURI());
			Proxy proxy = request.getProxy();
			retval = proxy == null ? url.openConnection() : url
					.openConnection(proxy);
			onPostOpenConnection(retval, request);
		}
		return retval;
	}

	public void connect(URLConnection conn, HttpRequest request)
			throws IOException
	{
		if (onPreConnection(conn))
		{
			conn.connect();
			onPostConnection(conn, request);
		}
	}
}
