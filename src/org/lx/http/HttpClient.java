package org.lx.http;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URLConnection;
import java.util.EventObject;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlparser.lexer.Stream;
import org.htmlparser.util.ParserException;
import org.lx.crawler.Crawler.IEventHandler;
import org.lx.crawler.Crawler.IProcessor;
import org.lx.crawler.ISnapShot;
import org.lx.crawler.Request;

public class HttpClient implements IProcessor
{
	public final static String K_URL = "url";

	public final static String K_COOKIE = "cookie";

	public final static String K_POST = "post";

	public final static String K_REFRESH = "refresh";

	static Log m_log = LogFactory.getLog(HttpClient.class);

	/**
	 * The default charset. This should be <code>{@value}</code>, see RFC 2616
	 * (http://www.ietf.org/rfc/rfc2616.txt?number=2616) section 3.7.1 <p>
	 * Another alias is "8859_1".
	 */
	public static final String DEFAULT_CHARSET = "ISO-8859-1";

	/**
	 * The default content type. In the absence of alternate information, assume
	 * html content ( {@value} ).
	 */
	public static final String DEFAULT_CONTENT_TYPE = "text/html";

	static HttpConnectionManager m_cm;

	public HttpClient()
	{
		if (DEFAULT_PARAMETER == null)
		{
			// DEFAULT_PARAMETER = new HttpRequest();
			// DEFAULT_PARAMETER. = "GET";
		}
	}

	public static void disableSSLVerfication()
	{
		// disable the SSL verification.
		// Create a trust manager that does not validate certificate
		// chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers()
			{
				return null;
			}

			public void checkClientTrusted(
					java.security.cert.X509Certificate[] certs, String authType)
			{
			}

			public void checkServerTrusted(
					java.security.cert.X509Certificate[] certs, String authType)
			{
			}

		} };

		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String urlHostName, SSLSession session)
			{
				return urlHostName.equals(session.getPeerHost());
			}
		};

		try
		{
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(hv);

		} catch (Exception e)
		{

		}

	}

	public void setSnapShotDie(String dir)
	{

	}

	/**
	 * Get a CharacterSet name corresponding to a charset parameter.
	 * @param content
	 *        A text line of the form: <pre> text/html; charset=Shift_JIS </pre>
	 *        which is applicable both to the HTTP header field Content-Type and
	 *        the meta tag http-equiv="Content-Type". Note this method also
	 *        handles non-compliant quoted charset directives such as: <pre>
	 *        text/html; charset=&quot;UTF-8&quot; </pre> and <pre> text/html;
	 *        charset='UTF-8' </pre>
	 * @return The character set name to use when reading the input stream. For
	 *         JDKs that have the Charset class this is qualified by passing the
	 *         name to findCharset() to render it into canonical form. If the
	 *         charset parameter is not found in the given string, the default
	 *         character set is returned.
	 * @see #findCharset
	 * @see #DEFAULT_CHARSET
	 */
	protected String getCharset(String charset, String content)
	{
		final String CHARSET_STRING = "charset";
		int index;
		String ret;

		if (null == charset)
			ret = DEFAULT_CHARSET;
		else
			// use existing (possibly supplied) character set:
			// bug #1322686 when illegal charset specified
			ret = charset;
		if (null != content)
		{
			index = content.indexOf(CHARSET_STRING);

			if (index != -1)
			{
				content = content.substring(index + CHARSET_STRING.length())
						.trim();
				if (content.startsWith("="))
				{
					content = content.substring(1).trim();
					index = content.indexOf(";");
					if (index != -1)
						content = content.substring(0, index);

					// remove any double quotes from around charset string
					if (content.startsWith("\"") && content.endsWith("\"")
							&& (1 < content.length()))
						content = content.substring(1, content.length() - 1);

					// remove any single quote from around charset string
					if (content.startsWith("'") && content.endsWith("'")
							&& (1 < content.length()))
						content = content.substring(1, content.length() - 1);

					ret = findCharset(content, ret);

				}
			}
		}

		return (ret);
	}

	/**
	 * Lookup a character set name. <em>Vacuous for JVM's without
	 * <code>java.nio.charset</code>.</em> This uses reflection so the code will
	 * still run under prior JDK's but in that case the default is always
	 * returned.
	 * @param name
	 *        The name to look up. One of the aliases for a character set.
	 * @param fallback
	 *        The name to return if the lookup fails.
	 * @return The character set name.
	 */
	public static String findCharset(String name, String fallback)
	{
		String ret;

		try
		{
			Class cls;
			Method method;
			Object object;

			cls = Class.forName("java.nio.charset.Charset");
			method = cls.getMethod("forName", new Class[] { String.class });
			object = method.invoke(null, new Object[] { name });
			method = cls.getMethod("name", new Class[] {});
			object = method.invoke(object, new Object[] {});
			ret = (String) object;
		} catch (ClassNotFoundException cnfe)
		{
			// for reflection exceptions, assume the name is correct
			ret = name;
		} catch (NoSuchMethodException nsme)
		{
			// for reflection exceptions, assume the name is correct
			ret = name;
		} catch (IllegalAccessException ia)
		{
			// for reflection exceptions, assume the name is correct
			ret = name;
		} catch (InvocationTargetException ita)
		{
			// java.nio.charset.IllegalCharsetNameException
			// and java.nio.charset.UnsupportedCharsetException
			// return the default
			ret = fallback;
			m_log.warn("unable to determine cannonical charset name for "
					+ name + " - using " + fallback);
		}

		return (ret);
	}

	public String getCharset(URLConnection connection)
	{
		String type = getContentType(connection);
		return getCharset(null, type);
	}

	public URLConnection getConnection(HttpRequest request) throws IOException
	{
		HttpConnectionManager cm = getConnectionManager();
		return cm.open(request);
	}

	public void connect(URLConnection uc, HttpRequest request)
			throws IOException
	{
		HttpConnectionManager cm = getConnectionManager();
		cm.connect(uc, request);
	}

	public InputStream getInputStream(HttpRequest request) throws IOException
	{
		URLConnection uc = getConnection(request);
		return getInputStream(uc);
	}

	/**
	 * Set the URLConnection to be used by this page. Starts reading from the
	 * given connection. This also resets the current url.
	 * @param connection
	 *        The connection to use. It will be connected by this method.
	 * @exception ParserException
	 *            If the <code>connect()</code> method fails, or an I/O error
	 *            occurs opening the input stream or the character set
	 *            designated in the HTTP header is unsupported.
	 */
	public static InputStream getInputStream(URLConnection connection)
			throws IOException
	{
		InputStream retval = null;
		Stream stream;
		String contentEncoding;
		connection.connect();

		contentEncoding = connection.getContentEncoding();
		if ((null != contentEncoding)
				&& (-1 != contentEncoding.indexOf("gzip")))
		{
			stream = new Stream(
					new GZIPInputStream(connection.getInputStream()));
		} else if ((null != contentEncoding)
				&& (-1 != contentEncoding.indexOf("deflate")))
		{
			stream = new Stream(new InflaterInputStream(
					connection.getInputStream(), new Inflater(true)));
		} else
		{
			stream = new Stream(connection.getInputStream());
		}
		/* stream is a inputStream of htmlParser */
		retval = stream;
		return retval;
	}

	public static HttpConnectionManager getConnectionManager()
	{
		if (m_cm == null)
		{
			m_cm = new HttpConnectionManager();
			m_cm.setRedirectionProcessingEnabled(true);
			m_cm.setCookieProcessingEnabled(true);
			HttpConnectionManager.getDefaultRequestProperties().put(
					"User-Agent", "Mozilla/5.0 (X11;)");
		}

		return m_cm;
	}

	/**
	 * get the reader resource for the specific url.
	 * @param html_url
	 * @return
	 */
	public ISnapShot getSnapShot(Request req, ISnapShot snapshot)
			throws Exception
	{
		ISnapShot retval = null;
		do
		{
			if (req instanceof HttpRequest == false)
			{
				break;
			}
			HttpRequest request = (HttpRequest) req;

			// get content from URI
			URLConnection uc = getConnection(request);

			if (uc instanceof HttpURLConnection)// http&https
			{
				HttpURLConnection http_uc = (HttpURLConnection) uc;

				// if the SnapShot existed in the local.add the ETAG and
				// If-Modified-Since
				// headers.
				if (snapshot.checkIntegrity())
				{
					String etag = snapshot.getProperty(ISnapShot.PROP_RSPMETA,
							ISnapShot.K_ETAG);
					if (etag != null)
					{
						uc.setRequestProperty("If-None-Match", etag);
					}
					String last_modif = snapshot.getProperty(
							ISnapShot.PROP_RSPMETA, ISnapShot.K_LAST_MODIF);
					if (last_modif != null)
					{
						uc.setRequestProperty("If-Modified-Since", last_modif);
					}
				}

				try
				{
					// make the real connection to server.
					if (m_log.isWarnEnabled())
					{
						StringBuffer sb = new StringBuffer();
						Proxy proxy = request.getProxy();
						sb.append(request.getMethod());
						sb.append(":" + http_uc.getURL().toString() + "\n");
						// add proxy info.
						if (proxy != null)
						{
							sb.append("[Proxy]" + proxy.toString() + "\n");
						}
						// output the request headers
						Map props = uc.getRequestProperties();
						Iterator it = props.keySet().iterator();
						String key = null;
						while (it.hasNext())
						{
							key = String.valueOf(it.next());
							sb.append("[REQ]");
							sb.append(key + "=");
							sb.append(props.get(key));
							sb.append('\n');
						}
						m_log.warn(sb.toString());
					}

					connect(uc, request);
					// check the response.
					handleHttp(snapshot, http_uc, request);
					// Log the result.
					if (m_log.isWarnEnabled())
					{
						StringBuffer sb = new StringBuffer();
						sb.append("code=" + http_uc.getResponseCode() + "\n");

						// output the response headers.
						Map props = uc.getHeaderFields();
						Iterator it = props.keySet().iterator();
						String key = null;
						while (it.hasNext())
						{
							key = String.valueOf(it.next());
							sb.append("[RSP]");
							sb.append(key + "=");
							sb.append(props.get(key));
							sb.append('\n');
						}
						m_log.warn(sb.toString());
					}
				} catch (Throwable e)
				{
					m_log.error(http_uc.getURL().toString() + "->"
							+ e.getLocalizedMessage());
				}
			}
			retval = snapshot;
		} while (false);

		return retval;
	}

	public static class HttpEvent extends EventObject
	{
		private static final long serialVersionUID = 88201642487039255L;

		public int responseCode;

		public ISnapShot snapshot;

		public Request req;

		public HttpEvent(ISnapShot snapshot, Request req, int rspCode,
				Object source)
		{
			super(source);
			this.responseCode = rspCode;
			this.req = req;
			this.snapshot = snapshot;
		}

	}

	protected void handleHttp(ISnapShot snapshot, HttpURLConnection uc,
			HttpRequest request) throws IOException
	{
		int code = uc.getResponseCode();
		switch (code)
		{
		case 302:
		{
			// TODO:implement 302.
			m_log.debug(" code=302: Move!");
			HttpURLConnection new_uc = null;
			HttpRequest new_request = null;
			handleHttp(snapshot, new_uc, new_request);
			break;
		}
		case 304:
		{
			m_log.debug(" code=304: Not Modified!");
			break;
		}
		case 412:
		{
			m_log.debug(" code=412: Precondition Failed!");
			break;
		}
		default:
		{
			m_log.debug(" http response code=" + code);
			break;
		}
		}

		IEventHandler handle = getEventHandler();
		if (handle != null)
		{
			handle.onEvent(new HttpEvent(snapshot, request, code, uc));
		}
	}

	/**
	 * Try and extract the content type from the HTTP header.
	 * @return The content type.
	 */
	public String getContentType(URLConnection connection)
	{
		String content;
		String ret;

		ret = DEFAULT_CONTENT_TYPE;
		if (null != connection)
		{
			// can't use connection#getContentType
			// see Bug #1467712 Page#getCharset never works
			content = connection.getHeaderField("Content-Type");
			if (null != content)
				ret = content;
		}

		return (ret);
	}

	protected static HttpRequest DEFAULT_PARAMETER;

	public HttpRequest getDefaultParameter()
	{
		return DEFAULT_PARAMETER;
	}

	IEventHandler m_eventHandle;

	public void setEventListener(IEventHandler handler)
	{
		m_eventHandle = handler;
	}

	protected IEventHandler getEventHandler()
	{
		return m_eventHandle;
	}

}
