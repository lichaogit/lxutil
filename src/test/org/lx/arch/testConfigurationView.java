package test.org.lx.arch;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jaxen.JaxenException;
import org.jaxen.dom4j.Dom4jXPath;
import org.lx.arch.Configuration;
import org.lx.arch.ConfigurationView;
import org.lx.arch.Dom4jConfiguration;
import org.lx.arch.Dom4jNodeConfiguration;
import org.lx.arch.Dom4jTreeModel;
import org.lx.arch.MapConfiguration;
import org.lx.util.StringEx;

public class testConfigurationView extends TestCase
{

	protected ConfigurationView getInstance()
	{
		ConfigurationView confView = null;
		try
		{
			String model_file = StringEx
					.getPackagePath(testConfigurationView.class)
					+ "/testConfigurationView.xml";
			InputStream in = testConfigurationView.class
					.getResourceAsStream(model_file);

			Dom4jConfiguration conf = new Dom4jConfiguration(in);

			confView = new ConfigurationView(conf, "/configuration");
		} catch (Throwable e)
		{
			fail("fail at get the ConfigurationView instance");
		}
		return confView;
	}

	public void testQueryAllStringITransform()
	{
		ConfigurationView confView = getInstance();
		List list = confView.queryAll("frontEnd/plugins/builtin/*", null);
		assertEquals(list.size(), 6);

		// test the string convert capability.
		list = confView.queryAll(
				"frontEnd/plugins/builtin/httpPassthroughHeaders/*",
				ConfigurationView.STRING_TRANSFORM);
		assertEquals(list.size(), 6);

		assertEquals(list.get(0), "Content-Type");
		assertEquals(list.get(1), "User-Agent");
		assertEquals(list.get(2), "Accept");
		assertEquals(list.get(3), "Accept-Charset");
		assertEquals(list.get(4), "Accept-Language");
		assertEquals(list.get(5), "Authorization");
	}

	public void testGetXPath()
	{
		ConfigurationView confView = getInstance();
		Object obj = confView.query("/configuration/frontEnd");
		String path = confView.getXPath(obj);
		assertEquals(path, "/configuration/frontEnd");
	}

	public void testAddAttributeObjectStringString()
	{
		ConfigurationView confView = getInstance();
		Object obj = confView.query("/configuration/frontEnd");

		Dom4jTreeModel dt = new Dom4jTreeModel((Element) obj);
		Dom4jNodeConfiguration nodeConf = new Dom4jNodeConfiguration(dt);
		ConfigurationView newConfView;
		try
		{
			newConfView = new ConfigurationView(nodeConf, null);

			String key = "testAttr";
			String val = "testVal";
			newConfView.addAttribute(key, val);
			String newVal = newConfView.getAttribute("testAttr");
			assertEquals(val, newVal);

			Object branchObj = newConfView.query("threadPool");
			newConfView.addAttribute(branchObj, key, val);
			newVal = newConfView.getAttribute("threadPool", key);
			assertEquals(val, newVal);
		} catch (Exception e)
		{
			this.assertTrue(false);
		}

	}

	public void testGetAttributeStringString()
	{
		ConfigurationView confView = getInstance();
		Object obj = confView.query("/configuration/frontEnd");

		Dom4jTreeModel dt = new Dom4jTreeModel((Element) obj);
		Dom4jNodeConfiguration nodeConf = new Dom4jNodeConfiguration(dt);
		ConfigurationView newConfView;
		try
		{
			newConfView = new ConfigurationView(nodeConf, null);

			String port = newConfView.getAttribute("port");
			assertEquals(port, "9002");

			String val = newConfView.getAttribute("threadPool", "threadCount");
			assertEquals(val, "2");

			val = newConfView.getAttribute(
					"plugins/builtin/xslServer/threadPool", "threadCount");
			assertEquals(val, "5");
		} catch (Exception e)
		{
			assertTrue(false);
		}

	}

	/**
	 * check the GetAttribute with nodePath created by getSubView.
	 */
	public void testGetAttributeWithSubView()
	{
		ConfigurationView confView = getInstance();
		ConfigurationView newConfView;
		try
		{
			newConfView = confView.getSubView("frontEnd");

			String port = newConfView.getAttribute("port");
			assertEquals(port, "9002");

			String val = newConfView.getAttribute("threadPool", "threadCount");
			assertEquals(val, "2");

			val = newConfView.getAttribute(
					"plugins/builtin/xslServer/threadPool", "threadCount");
			assertEquals(val, "5");
		} catch (Exception e)
		{
			this.assertTrue(false);
		}

	}

	public void testGetAttributeWithSubViewAndCompleteXPath()
	{
		ConfigurationView confView = getInstance();
		ConfigurationView newConfView;
		try
		{
			newConfView = confView
					.getSubView("frontEnd/plugins/builtin/TimerService/schedules/schedule[@name='crawler_tianya']");

			String port = newConfView.getAttribute("value");
			assertEquals(port, "0/20 * * * * ?");
		} catch (Exception e)
		{
			this.assertTrue(false);
		}

	}

	public void testGetAttributeWithParentView()
	{
		try
		{
			ConfigurationView confView = getInstance();
			ConfigurationView newConfView = confView
					.getSubView("frontEnd/plugins/builtin/TimerService/schedules/schedule[@name='crawler_tianya']");

			HashMap map = new HashMap();
			String newVal = "xxx";
			map.put("value", newVal);

			Configuration cmdLineCfg = new MapConfiguration(map);
			ConfigurationView cmdLineView = new ConfigurationView(cmdLineCfg,
					null);
			cmdLineView.setParent(confView);

			String port = cmdLineView.getAttribute("value");
			assertEquals(port, newVal);
		} catch (Exception e)
		{
			assertTrue(false);
		}
	}

	public void testQueryRecursive()
	{
		String newConf = "<schedules>"
				+ "<!--schedule name=\"\" value=\"cron express\"-->"
				+ "<schedule name=\"crawler_tianya\" group=\"daily\" value=\"0/200 * * * * ?\"/>"
				+ "</schedules>";

		Dom4jConfiguration conf;
		try
		{
			ConfigurationView confView = getInstance();
			ConfigurationView confViewParent = confView
					.getSubView("frontEnd/plugins/builtin/TimerService/schedules");

			conf = new Dom4jConfiguration(new StringReader(newConf), null);
			ConfigurationView confViewNew = new ConfigurationView(conf,
					"/schedules");
			confViewNew.setParent(confViewParent);
			String newVal = (String) confViewNew.query(
					"schedule[@name='crawler_tianya']/@value",
					ConfigurationView.STRING_TRANSFORM);
			assertEquals("0/200 * * * * ?", newVal);
			newVal = (String) confViewNew.query(
					"schedule[@name='crawler_baidu']/@value",
					ConfigurationView.STRING_TRANSFORM);
			assertEquals("0/10 * * * * ?", newVal);

		} catch (IOException e)
		{
			assertTrue(false);
		}
	}

	public void testBaseObject()
	{
		ConfigurationView confView = getInstance();
		ConfigurationView newConfView = confView
				.getSubView("frontEnd/plugins/builtin/TimerService/schedules/schedule[@name='crawler_tianya']");
		String expect = "<schedule name=\"crawler_tianya\" group=\"daily\" value=\"0/20 * * * * ?\"/>";
		String xml = newConfView.asXML();
		assertEquals(expect, xml);
	}

	public void testDom4j()
	{
		try
		{
			String newConf = "<schedules>"
					+ "<!--schedule name=\"\" value=\"cron express\"-->"
					+ "<schedule name=\"crawler_tianya\" group=\"daily\" value=\"0/200 * * * * ?\"/>"
					+ "</schedules>";
			SAXReader saxReader = new SAXReader();
			Document doc = saxReader.read(new StringReader(newConf));
			Dom4jXPath xpath = new Dom4jXPath("/");
			Object obj = xpath.selectSingleNode(doc);
			assertTrue(obj instanceof Document);
			xpath = new Dom4jXPath("/schedules");
			obj = xpath.selectSingleNode(doc);
			assertTrue(obj instanceof Element);

		} catch (DocumentException e)
		{
			assertTrue(false);
		} catch (JaxenException e)
		{
			assertTrue(false);
		}
	}

	public void testQuery()
	{
		String newConf = "<schedules>"
				+ "<!--schedule name=\"\" value=\"cron express\"-->"
				+ "<schedule name=\"crawler_tianya\" group=\"daily\" value=\"0/200 * * * * ?\"/>"
				+ "</schedules>";

		try
		{
			Dom4jConfiguration conf = new Dom4jConfiguration(new StringReader(
					newConf), null);

			ConfigurationView confViewNew1 = new ConfigurationView(conf,
					"/schedules");
			String val = confViewNew1.getAttribute("schedule", "name");
			assertEquals("crawler_tianya", val);

			ConfigurationView confViewNew2 = new ConfigurationView(conf, "/");
			val = confViewNew2.getAttribute("/schedules/schedule", "name");
			assertEquals("crawler_tianya", val);
			val = confViewNew2.getAttribute("schedules/schedule", "name");
			assertEquals("crawler_tianya", val);

			ConfigurationView confViewNew3 = new ConfigurationView(conf, null);
			val = confViewNew3.getAttribute("/schedules/schedule", "name");
			assertEquals("crawler_tianya", val);
			val = confViewNew3.getAttribute("/schedules/schedule", "name");
			assertEquals("crawler_tianya", val);

		} catch (IOException e)
		{
			assertTrue(false);
		}
	}
}
