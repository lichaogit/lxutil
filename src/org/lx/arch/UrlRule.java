package org.lx.arch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jaxen.JaxenException;
import org.lx.util.GeneralException;
import org.lx.util.LogicUtil;
import org.lx.util.XmlRegexMatchCondition;

public class UrlRule
{
	final static String TAG_MATCHER = "UrlRule";

	final static String ATTR_TM_SENSITIVE = "sensitive";

	final static String ATTR_TM_REGEX = "regex";

	final static String TAG_XSL = "xsl";

	final static String ATTR_TX_URL = "url";

	final static String TAG_REFRESH = "refresh";

	final static String ATTR_TR_XVALUE = "value";

	final static String ATTR_TR_STEP = "step";

	final static String TAG_PARAMS = "params";

	final static String TAG_PARAM = "param";

	final static String ATTR_TP_NAME = "name";

	final static String ATTR_TP_VALUE = ATTR_TR_XVALUE;

	Document m_doc;

	String m_uri;

	private long m_xslTimeStamp = 0;

	public UrlRule(String uri)
	{
		m_uri = uri;
	}

	/**
	 * get the mather node base on the url(regex express)
	 * @param url
	 * @return
	 */
	protected Dom4jTreeModel getRuleModel(String url)
	{
		Dom4jTreeModel retval = null;
		Dom4jTreeModel dt = new Dom4jTreeModel(m_doc.getRootElement());
		ArrayList al = new ArrayList();

		LogicUtil.shallowSearch(dt, al, new XmlRegexMatchCondition(url,
				"regex", "sensitive"));
		if (al.size() > 0)
		{
			retval = (Dom4jTreeModel) al.get(0);
		}
		return retval;
	}

	protected String getMatcherAttribute(String url, String attrPath)
	{
		String retval = null;
		Dom4jTreeModel dt = getRuleModel(url);
		if (dt != null)
		{
			Dom4jNodeConfiguration nodeConf = new Dom4jNodeConfiguration(dt);
			ConfigurationView confView = new ConfigurationView(nodeConf, "/"
					+ TAG_MATCHER);
			Object obj = confView.query(attrPath);
			retval = obj == null ? null : Dom4jNodeConfiguration
					.stringValueOf(obj);
		}
		return retval;
	}

	public Element getCrawlerFilterModel(String url)
	{
		return getFilterModel(url, "crawlerFilter");
	}

	public Element getSaveFilterModel(String url)
	{
		return getFilterModel(url, "saveFilter");
	}

	protected Element getFilterModel(String url, String matcherName)
	{
		Element retval = null;
		Dom4jTreeModel model = getRuleModel(url);
		if (model != null)
		{
			Dom4jNodeConfiguration nodeConfig = new Dom4jNodeConfiguration(
					model);
			ConfigurationView view = new ConfigurationView(nodeConfig,
					"/UrlRule");
			retval = (Element) view.query("Matcher[@name='" + matcherName
					+ "']/*[1]");
		}
		return retval;
	}

	public String getXsl(String url)
	{
		return getMatcherAttribute(url, TAG_XSL + "/@" + ATTR_TX_URL);
	}

	public long getRefreshStep(String url)
	{
		long retval = 0;
		String tmp = getMatcherAttribute(url, TAG_REFRESH + "/@" + ATTR_TR_STEP);
		if (tmp != null)
		{
			retval = Long.parseLong(tmp);
		}
		return retval;
	}

	public String getRefreshXpath(String url)
	{
		return getMatcherAttribute(url, TAG_REFRESH + "/@" + ATTR_TR_XVALUE);
	}

	public Map getParams(String url) throws JaxenException
	{
		return getNameValuePaired(url, "params/param");
	}

	public Map getNamesType(String url) throws JaxenException
	{
		return getNameValuePaired(url, "params/param", ATTR_TP_NAME, "type");
	}

	public Map getNameSpaces(String url) throws JaxenException
	{
		return getNameValuePaired(url, "params/namespace");
	}

	public Map getNameValuePaired(String url, String nodesXpath)
			throws JaxenException
	{
		return getNameValuePaired(url, nodesXpath, ATTR_TP_NAME, ATTR_TP_VALUE);
	}

	public Map getNameValuePaired(String url, String nodesXpath,
			String nameTag, String valTag) throws JaxenException
	{
		Map retval = null;
		Dom4jTreeModel dt = getRuleModel(url);
		if (dt != null)
		{
			Dom4jNodeConfiguration nodeConf = new Dom4jNodeConfiguration(dt);
			ConfigurationView confView = new ConfigurationView(nodeConf,
					"/matcher");

			retval = confView.getNameValuePaired(nodesXpath, nameTag, valTag,
					null);
		}
		return retval;
	}

	public synchronized void write(OutputStream out)
			throws UnsupportedEncodingException, IOException
	{
		String cs = m_doc.getXMLEncoding();
		OutputFormat format = OutputFormat.createCompactFormat();
		format.setEncoding(cs);

		Writer w = new OutputStreamWriter(out, cs);
		XMLWriter xw = new XMLWriter(w, format);
		xw.write(m_doc);

		w.flush();
		xw.close();
		w.close();

	}

	public synchronized void read(InputStream in) throws GeneralException
	{
		try
		{
			SAXReader saxReader = new SAXReader();
			m_doc = saxReader.read(in);
		} catch (DocumentException e)
		{
			throw new GeneralException(e);
		}

	}

	public synchronized void reloadXslLibrary() throws GeneralException
	{
		reloadXslLibrary(new File(m_uri));
	}

	private synchronized void reloadXslLibrary(File file)
			throws GeneralException
	{
		InputStream in = null;
		try
		{
			long f_ts = file.lastModified();
			if (f_ts != m_xslTimeStamp)
			{
				in = new FileInputStream(file);
				read(in);
				in.close();
				m_xslTimeStamp = f_ts;
			}
		} catch (IOException e)
		{
			throw new GeneralException(e);
		}
	}
}
