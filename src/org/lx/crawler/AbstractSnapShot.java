package org.lx.crawler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.dom4j.Dom4jXPath;
import org.lx.arch.ConfigurationView;
import org.lx.arch.Dom4jConfiguration;
import org.lx.arch.UrlRule;
import org.lx.io.XmlEncodeReader;

public abstract class AbstractSnapShot implements ISnapShot
{

	protected final static Log s_log = LogFactory
			.getLog(AbstractSnapShot.class);

	protected SnapShotInfo m_snapShotInfo;

	ISnapShotManager m_sm;

	public AbstractSnapShot(ISnapShotManager sm)
	{
		m_snapShotInfo = new SnapShotInfo();
		m_sm = sm;
	}

	public ISnapShotManager getSnapShotManager()
	{
		return m_sm;
	}

	/**
	 * get the information for the specific SnapShot.
	 * @param cacheId
	 * @return
	 */
	protected SnapShotInfo getSnapShotInfo() throws IOException
	{
		return m_snapShotInfo;
	}

	/**
	 * return all of the properties for the category.
	 * @param category
	 * @return
	 */
	public List getProperties(String category) throws IOException
	{
		List retval = null;
		// keep the SnapShot is latest.
		SnapShotInfo info = getSnapShotInfo();
		retval = info.m_confView.queryAll(category + "/@*");
		return retval;
	}

	/**
	 * get the value the SnapShot's property.
	 * @param category
	 * @param key
	 * @return
	 */
	public String getProperty(String category, String key) throws IOException
	{
		return getSnapShotInfo().getProperty(category, key);
	}

	/**
	 * set the value the SnapShot's property.
	 * @param category
	 * @param key
	 * @param val
	 * @return
	 */
	public void setProperty(String category, String key, String val)
			throws IOException
	{
		SnapShotInfo info = getSnapShotInfo();
		info.setProperty(category, key, val);
	}

	protected void setProperties(Map info) throws IOException
	{
		// save the properties if it is provided.
		if (info.size() > 0)
		{
			Iterator it = info.keySet().iterator();
			String key = null;
			String val = null;
			while (it.hasNext())
			{
				key = String.valueOf(it.next());
				val = String.valueOf(info.get(key));
				setProperty(ISnapShot.PROP_CONTENT, key, val);
			}
		}

	}

	/* read/writer the metadata and the properties */
	public final static class SnapShotInfo
	{
		/*
		 * HashMap m_props = new HashMap(); HashMap m_metaDatas = new HashMap();
		 */

		ConfigurationView m_confView;

		public SnapShotInfo()
		{
			m_confView = createConfigurationView();
		}

		protected ConfigurationView createConfigurationView()
		{
			// Create a XML model.
			Dom4jConfiguration conf = new Dom4jConfiguration();
			conf.createNode(null, "SnapShot");

			// use the ConfigurationView to operate.
			ConfigurationView confView = new ConfigurationView(conf,
					"/SnapShot");

			confView.createNode(ISnapShot.PROP_REQMETA);
			confView.createNode(ISnapShot.PROP_RSPMETA);
			confView.createNode(ISnapShot.PROP_CONTENT);

			return confView;
		}

		public ConfigurationView getConfigurationView()
		{
			return m_confView;
		}

		public void write(Writer w) throws IOException,
				UnsupportedEncodingException
		{

			ConfigurationView confView = m_confView;
			if (confView == null)
			{
				confView = createConfigurationView();
			}

			Dom4jConfiguration conf = (Dom4jConfiguration) confView
					.getConfigutation();
			conf.write(w);
		}

		public void write(OutputStream out) throws IOException,
				UnsupportedEncodingException
		{

			ConfigurationView confView = m_confView;
			if (confView == null)
			{
				confView = createConfigurationView();
			}

			Dom4jConfiguration conf = (Dom4jConfiguration) confView
					.getConfigutation();
			conf.write(out);
		}

		public void read(InputStream in) throws IOException
		{
			// Load the XML model.
			Dom4jConfiguration conf = new Dom4jConfiguration(in);
			// use the ConfigurationView to operate.
			ConfigurationView confView = new ConfigurationView(conf,
					"/SnapShot");
			m_confView = confView;
		}

		/**
		 * get the value of specific SnapShot's key.
		 * @param key
		 * @return
		 */
		public String getProperty(String xpath, String key)
		{
			return m_confView.getAttribute(xpath, key);
		}

		/**
		 * set the value of specific SnapShot's key.
		 * @param key
		 * @param val
		 * @return
		 */
		public void setProperty(String xpath, String key, String val)
		{
			Object handler = m_confView.query(xpath);
			m_confView.addAttribute(handler, key, val);
		}

		public void setProperties(String xpath, Map properties)
		{
			Object handler = m_confView.query(xpath);
			Iterator it = properties.keySet().iterator();
			String key = null;
			String val = null;
			while (it.hasNext())
			{
				key = (String) it.next();
				val = (String) properties.get(key);
				m_confView.addAttribute(handler, key, val);
			}
		}
	}
}
