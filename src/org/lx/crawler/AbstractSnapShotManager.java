package org.lx.crawler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.dom4j.Dom4jXPath;
import org.lx.arch.UrlRule;
import org.lx.arch.expr.ExprParser;
import org.lx.http.HttpClient;
import org.lx.io.XmlEncodeReader;
import org.lx.util.Cache;
import org.lx.util.MapValueRender;

public abstract class AbstractSnapShotManager extends Cache implements
		ISnapShotManager
{

	UrlRule m_urlRule;

	String m_snapShotBase;

	ExprParser m_parser;

	protected final static Log s_log = LogFactory
			.getLog(AbstractSnapShotManager.class);

	public AbstractSnapShotManager(UrlRule urlRule, String snapShotBase)
	{
		super(new WeakHashMap());
		m_snapShotBase = snapShotBase;
		m_urlRule = urlRule;
		m_parser = ExprParser.getDefaultExprParser();
	}

	public UrlRule getUrlRule()
	{
		return m_urlRule;
	}

	public void setSnapShotLocation(String snapShotBase)
	{
		m_snapShotBase = snapShotBase;
	}

	public String getSnapShotLocation()
	{
		return m_snapShotBase;
	}

	/**
	 * get the SnapShot info.
	 * @param request
	 * @param versionExpress
	 * @return
	 */
	public ISnapShot getSnapShot(Request request, String versionExpress)
			throws IOException
	{
		return (ISnapShot) get(new Object[] { request, versionExpress });
	}

	/**
	 * determine whether the SnapShot need update.
	 * @param htmlUrl
	 * @param param
	 * @return
	 */
	public boolean needUpdate(Request request) throws IOException
	{
		boolean retval = false;
		do
		{
			// 1.check the SnapShot integrality.
			ISnapShot snapshot = getSnapShot(request, null);
			if (snapshot == null || (snapshot.checkIntegrity() == false))
			{
				retval = true;
				break;
			}

			// 2. if the SnapShot is ok, then do not refresh when time less than
			// the internal.
			long cur_time = new Date().getTime();
			long sep = cur_time - snapshot.getSnapShotModified();
			if (snapshot != null && snapshot.checkIntegrity())
			{
				/* check the refresh step */
				if (sep < 1000 * m_urlRule.getRefreshStep(request.getURI()))
				{
					break;
				}
			}

			// 3. do not refresh if the SnapShot refresh property is false.
			String refresh = snapshot.getProperty(ISnapShot.PROP_REQMETA,
					HttpClient.K_REFRESH);
			if (refresh != null && "false".equalsIgnoreCase(refresh))
			{
				/*
				 * refresh when SnapShot less than 12 hours when the 'refresh'
				 * property is 'false'
				 */
				if (sep < 1000 * 60 * 60 * 12)
				{
					break;
				}
				break;
			}

			retval = true;
		} while (false);
		return retval;
	}

	protected void checkRefresh(ISnapShot snapshot) throws IOException
	{
		// recalculate the refresh property by the cache content.
		String cs = snapshot.getProperty(ISnapShot.PROP_RSPMETA,
				ISnapShot.K_CHARSET);
		String real_cs = SmartPage.getBigestCharset(cs);
		String url = snapshot.getProperty(ISnapShot.PROP_REQMETA,
				ISnapShot.K_URL);
		try
		{
			// calculate whethe the cache need refresh in the future.
			String refresn_xpath = m_urlRule.getRefreshXpath(url);
			boolean refresh_flag = true;
			if (refresn_xpath != null)
			{
				do
				{
					InputStream is = snapshot.getInputStream();

					Reader r = new XmlEncodeReader(new InputStreamReader(is,
							real_cs));
					// Reader r = new InputStreamReader(is,
					// real_cs);

					org.jaxen.dom4j.Dom4jXPath xpath = new org.jaxen.dom4j.Dom4jXPath(
							refresn_xpath);
					org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
					org.dom4j.Document doc = reader.read(r);
					List results = xpath.selectNodes(doc);
					if (results == null || results.size() == 0)
					{

						// need refresh as default
						break;
					}
					Object obj = results.get(0);
					if (obj instanceof Boolean == false)
					{
						// need refresh as default
						break;
					}

					refresh_flag = ((Boolean) obj).booleanValue();
				} while (false);
			}
			// update the SnapShot's refresh property.
			snapshot.setProperty(ISnapShot.PROP_REQMETA, HttpClient.K_REFRESH,
					String.valueOf(refresh_flag));

			// create the
			setUrlRuleProperties(snapshot, null, url);

			snapshot.save(null);// save the properties info.
		} catch (DocumentException e)
		{
			s_log.warn(e.getLocalizedMessage(), e);
		} catch (JaxenException e)
		{
			s_log.warn(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * load the specific resouecce.
	 * @param key
	 * @return
	 */
	protected Object load(Object key) throws IOException
	{
		Object[] params = (Object[]) key;
		Request request = (Request) (params[0]);
		Object[] newParams = params.length == 1 ? null
				: new Object[params.length - 1];

		System.arraycopy(params, 1, newParams, 0, newParams.length);

		return loadCache(request, newParams);
	}

	/**
	 * get the cache info id, that will be used in the other methods of
	 * ISnapShot.
	 * @param request
	 * @return
	 * @throws IOException
	 */
	protected abstract ISnapShot loadCache(Request request, Object[] params)
			throws IOException;

	protected List xpath(Document doc, String xpathExpr, Map namespaces)
			throws JaxenException
	{
		Dom4jXPath xpath = new org.jaxen.dom4j.Dom4jXPath(xpathExpr);
		// set the namespace info.
		if (namespaces != null && namespaces.size() > 0)
		{
			xpath.setNamespaceContext(new SimpleNamespaceContext(namespaces));
		}
		return xpath.selectNodes(doc);
	}

	/**
	 * create the properties that defined in the URLRule.
	 * @param urlrule
	 * @param url
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws DocumentException
	 * @throws JaxenException
	 */
	protected void setUrlRuleProperties(ISnapShot snapshot, InputStream in,
			String url) throws UnsupportedEncodingException, IOException
	{
		try
		{
			Map params = m_urlRule.getParams(url);
			Map namespaces = m_urlRule.getNameSpaces(url);

			Map paramsTypes = m_urlRule.getNamesType(url);

			if (params != null && params.size() > 0)
			{

				if (in == null)
				{
					in = snapshot.getInputStream();
				}
				// String charset = snapshot.getProperty(ISnapShot.PROP_CONTENT,
				// ISnapShot.K_CHARSET);
				// if (charset == null)
				// {
				// charset = "ISO-8859-1";
				// }
				// Reader r1 = new XmlEncodeReader(new InputStreamReader(in,
				// charset));

				Dom4jXPath xpath = null;
				List results = null;

				BufferedInputStream bin = new BufferedInputStream(in);

				SAXReader reader = new org.dom4j.io.SAXReader();
				/* reserved max 1024 */
				bin.mark(1024 * 1024);
				Document doc = reader.read(bin);
				String storeCharset = "UTF-8";// doc.getXMLEncoding();

				String key = null;
				String val = null;
				String type = null;
				Object obj = null;
				Iterator it = params.keySet().iterator();
				while (it.hasNext())
				{
					key = (String) it.next();
					obj = params.get(key);
					if (obj == null)
					{
						continue;
					}
					val = obj.toString();

					type = (String) paramsTypes.get(key);
					if (type == null)
					{
						type = "xpath";
					}
					if ("xpath".equals(type))
					{
						// parse the val as xpath and put to the
						// abstract file.
						results = xpath(doc, val, namespaces);
						if (results != null && results.size() == 1)
						{
							obj = results.get(0);
							if (obj instanceof Node)
							{
								val = ((Node) obj).getText();
							} else
							{
								val = obj.toString();
							}

							// convert the string to 8859-1 and store in the
							// string.
							byte[] iosVal = val.getBytes(storeCharset);
							snapshot.setProperty(ISnapShot.PROP_CONTENT, key,
									new String(iosVal, "ISO-8859-1"));
						}
					} else if ("glueExpr".equals(type))
					{
						bin.reset();
						Map map = new HashMap();
						map.put("content", bin);
						m_parser.parse(val, new MapValueRender(map));
					}
				}
			}
		} catch (JaxenException e)
		{
			throw new IOException(e);
		} catch (DocumentException e)
		{
			throw new IOException(e);
		} catch (Exception e)
		{
			throw new IOException(e);
		}
	}

	/**
	 * build propperty for a SnapShot Object
	 * @param snapshot
	 * @param propName
	 * @return
	 * @throws Exception
	 */
	public ISnapShot buildSnapShotProperty(ISnapShot snapshot, String propName)
			throws IOException
	{
		throw new IOException("no implement");
	}
}
