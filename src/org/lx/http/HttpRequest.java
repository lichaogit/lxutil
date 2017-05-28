package org.lx.http;

import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.lx.crawler.Request;
import org.lx.util.Base64;

public class HttpRequest extends Request {
	final static String P_BODY = "a";

	final static String P_CHARSET = "b";

	final static String P_HEADERS = "c";

	final static String P_COOKIES = "d";

	final static String P_REMOTEIP = "e";

	final static String P_METHOD = "f";

	final static String P_PROXY = "g";

	public HttpRequest(String uri, String user, Map params) {
		super(uri, user, params);
	}

	/**
	 * build a http request
	 * 
	 * @param uri
	 * @param user
	 * @param method
	 * @param headers
	 * @param cookies
	 * @param body
	 * @param charset
	 *            the charset of body.
	 */
	public HttpRequest(String uri, String user, String method,
			String[][] headers, String[][] cookies, String body, String charset) {
		super(uri, user, null);
		if (params == null) {
			params = new HashMap();
		}
		params.put(P_METHOD, method);
		params.put(P_HEADERS, headers);
		params.put(P_COOKIES, cookies);
		params.put(P_BODY, body);
		params.put(P_CHARSET, charset);

	}

	public String getRemoteIp() {
		return params == null ? null : (String) params.get(P_REMOTEIP);
	}

	public String getMethod() {
		return params == null ? null : (String) params.get(P_METHOD);
	}

	public String[][] getHeaders() {
		return params == null ? null : (String[][]) params.get(P_HEADERS);
	}

	public Proxy getProxy() {
		return params == null ? null : (Proxy) params.get(P_PROXY);
	}

	public void setProxy(Proxy proxy) {
		params.put(P_PROXY, proxy);
	}

	public void setHeaders(String[][] headers)

	{
		params.put(P_HEADERS, headers);
	}

	public String[][] getCookies() {
		return params == null ? null : (String[][]) params.get(P_COOKIES);
	}

	public String getBody() {
		return params == null ? null : (String) params.get(P_BODY);
	}

	public String getHeader(String key) {
		String retval = null;
		String[][] headers = getHeaders();

		if (headers != null) {
			for (int i = 0; i < headers.length; i++) {
				if (key.equalsIgnoreCase(headers[i][0])) {
					retval = headers[i][1];
					break;
				}
			}
		}
		return retval;
	}

	public String getCharset() {
		return params == null ? null : (String) params.get(P_CHARSET);
	}

	public static String[][] parseBase64Param(String param, String cs)
			throws UnsupportedEncodingException {
		String[][] retval = null;
		do {
			if (param == null) {
				break;
			}
			String[] item = param.split(",");
			if (item == null || item.length == 0) {
				break;
			}
			ArrayList al = new ArrayList(item.length);
			String[] wk = null;
			for (int i = 0; i < item.length; i++) {
				wk = item[i].split(":");
				if (wk.length == 2) {
					wk[0] = Base64.decodeString(wk[0], cs);
					wk[1] = Base64.decodeString(wk[1], cs);
					al.add(wk);
				}
			}
			retval = (String[][]) al.toArray(new String[0][0]);
		} while (false);

		return retval;
	}

	public String getCookiesString() {
		String retval = null;
		String[][] cookies = getCookies();
		if (cookies != null) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < cookies.length; i++) {
				sb.append(cookies[i][0]);

				sb.append(cookies[i][1]);
			}
			retval = sb.toString();
		}
		return retval;
	}

	/**
	 * just concern the uri+method.
	 */
	public int hashCode() {
		StringBuffer sb = new StringBuffer();
		sb.append(getMethod());
		sb.append(uri);
		return sb.toString().hashCode();
	}

}
