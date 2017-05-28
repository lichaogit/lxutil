package test.org.lx.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;

import junit.framework.TestCase;

import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.lx.arch.ConfigurationView;
import org.lx.arch.Dom4jConfiguration;
import org.lx.util.StringEx;
import org.lx.util.XmlUtil;

import test.org.lx.arch.testConfigurationView;

public class testXmlUtil extends TestCase
{
	protected String getXMLString()
	{
		String retval = null;
		try
		{
			String model_file = StringEx.getPackagePath(testXmlUtil.class)
					+ "/testXmlUtil.xml";
			InputStream in = testConfigurationView.class
					.getResourceAsStream(model_file);

			Dom4jConfiguration conf = new Dom4jConfiguration(in);
			retval = conf.asXML();
		} catch (Throwable e)
		{
			fail("fail at get the ConfigurationView instance");
		}
		return retval;
	}

	public void testXcapRemove()
	{
		try
		{
			String xmlString = getXMLString();
			// check init string
			Dom4jConfiguration dom4jConf = new Dom4jConfiguration(
					new StringReader(xmlString), null);
			ConfigurationView confView = new ConfigurationView(dom4jConf, null);
			Object obj = confView
					.query("/configuration/frontEnd/TimerService/Triggers/Trigger[@name='crawler_tianya']");
			assertNotNull(obj);
			obj = confView
					.query("/configuration/frontEnd/TimerService/Triggers/Trigger[@name='crawler_baidu']");
			assertNotNull(obj);

			// 1.remove element without attribute info
			String newXML = XmlUtil.xcapRemove(xmlString,
					"/configuration/frontEnd/TimerService/Triggers/Trigger");
			dom4jConf = new Dom4jConfiguration(new StringReader(newXML), null);
			confView = new ConfigurationView(dom4jConf, null);
			obj = confView
					.query("/configuration/frontEnd/TimerService/Triggers/Trigger");
			assertNull(obj);

			// 2. remove non-existed element.
			newXML = XmlUtil.xcapRemove(xmlString,
					"/configuration1/frontEnd/TimerService/Triggers/Trigger");
			dom4jConf = new Dom4jConfiguration(new StringReader(newXML), null);
			confView = new ConfigurationView(dom4jConf, null);
			obj = confView
					.query("/configuration/frontEnd/TimerService/Triggers/Trigger");
			assertNotNull(obj);

			// 3. remove specific element.
			newXML = XmlUtil
					.xcapRemove(xmlString,
							"/configuration/frontEnd/TimerService/Triggers/Trigger[@name='crawler_tianya']");
			dom4jConf = new Dom4jConfiguration(new StringReader(newXML), null);
			confView = new ConfigurationView(dom4jConf, null);
			obj = confView
					.query("/configuration/frontEnd/TimerService/Triggers/Trigger[@name='crawler_baidu']");
			assertNotNull(obj);

			obj = confView
					.query("/configuration/frontEnd/TimerService/Triggers/Trigger[@name='crawler_tianya']");
			assertNull(obj);

		} catch (IOException e)
		{
			assertTrue(false);
		}

	}

	public void testXcapAdd()
	{
		String xmlString = getXMLString();
		String valToAdd = "<Trigger name=\"test_add_demo\" group=\"daily\" value=\"0/20 * * * * ?\"/>";
		String valToAdd2 = "<Trigger name=\"test_add_demo\" group=\"daily1\" value=\"0/20 * * * * ?\"/>";

		String multiValues = valToAdd + valToAdd2;

		try
		{
			// 1.add Element to a exist ancient--overwrite.
			String newXML = XmlUtil.xcapAdd(xmlString,
					"/configuration/frontEnd/TimerService/Triggers/Trigger",
					valToAdd);

			Dom4jConfiguration dom4jConf = new Dom4jConfiguration(
					new StringReader(newXML), null);
			ConfigurationView confView = new ConfigurationView(dom4jConf, null);
			Object obj = confView
					.query("/configuration/frontEnd/TimerService/Triggers/Trigger[@name='test_add_demo']");
			assertEquals(valToAdd, ((Node) obj).asXML());

			obj = confView
					.query("/configuration/frontEnd/TimerService/Triggers/Trigger[@name='crawler_tianya']");
			assertNull(obj);

			// 1.1 add a new node.
			newXML = XmlUtil
					.xcapAdd(
							xmlString,
							"/configuration/frontEnd/TimerService/Triggers/Trigger[false]",
							valToAdd);

			dom4jConf = new Dom4jConfiguration(new StringReader(newXML), null);
			confView = new ConfigurationView(dom4jConf, null);
			obj = confView
					.query("/configuration/frontEnd/TimerService/Triggers/Trigger[@name='test_add_demo']");
			assertEquals(valToAdd, ((Node) obj).asXML());
			obj = confView
					.query("/configuration/frontEnd/TimerService/Triggers/Trigger[@name='crawler_tianya']");
			assertNotNull(obj);

			// 2.add Element to a non-exist ancient.
			newXML = XmlUtil.xcapAdd(xmlString,
					"/configuration/frontEnd1/TimerService/Triggers/Trigger",
					valToAdd,true);
			dom4jConf = new Dom4jConfiguration(new StringReader(newXML), null);
			confView = new ConfigurationView(dom4jConf, null);
			obj = confView
					.query("/configuration/frontEnd1/TimerService/Triggers/Trigger[@name='test_add_demo']");
			assertEquals(valToAdd, ((Node) obj).asXML());

			// 3.add Attribute to a exist Element.
			newXML = XmlUtil
					.xcapAdd(
							xmlString,
							"/configuration/frontEnd/TimerService/Triggers/Trigger[@name='crawler_tianya']/@testAttr",
							"testAtteValue");
			dom4jConf = new Dom4jConfiguration(new StringReader(newXML), null);
			confView = new ConfigurationView(dom4jConf, null);
			obj = confView
					.query("/configuration/frontEnd/TimerService/Triggers/Trigger[@name='crawler_tianya']/@testAttr");
			assertEquals("testAtteValue", obj.toString());

			// 4.change the attribute.
			newXML = XmlUtil
					.xcapAdd(
							xmlString,
							"/configuration/frontEnd/TimerService/Triggers/Trigger[@name='crawler_tianya']/@value",
							"crawler_tianya_new_val");
			dom4jConf = new Dom4jConfiguration(new StringReader(newXML), null);
			confView = new ConfigurationView(dom4jConf, null);
			obj = confView
					.query("/configuration/frontEnd/TimerService/Triggers/Trigger[@name='crawler_tianya']/@value");
			assertEquals("crawler_tianya_new_val", obj.toString());

			// 5.add Attribute to a non-exist Element(it should fail to add).
			newXML = XmlUtil
					.xcapAdd(
							xmlString,
							"/configuration/frontEnd1/TimerService/Triggers/Trigger[@name='crawler_tianya']/@testAttr",
							"testAtteValue");
			assertNull(newXML);

			// 6.add multi values.
			newXML = XmlUtil.xcapAdd(xmlString,
					"/configuration/frontEnd/TimerService/Triggers/Trigger",
					multiValues);
			dom4jConf = new Dom4jConfiguration(new StringReader(newXML), null);
			confView = new ConfigurationView(dom4jConf, null);
			List results = confView
					.queryAll("/configuration/frontEnd/TimerService/Triggers/Trigger[@name='test_add_demo']");
			assertEquals(valToAdd, ((Node) results.get(0)).asXML());
			assertEquals(valToAdd2, ((Node) results.get(1)).asXML());

			// 7.add to a null string.
			newXML = XmlUtil.xcapAdd(null,
					"/configuration/frontEnd/TimerService/Triggers/Trigger",
					multiValues,true);
			dom4jConf = new Dom4jConfiguration(new StringReader(newXML), null);
			confView = new ConfigurationView(dom4jConf, null);
			results = confView
					.queryAll("/configuration/frontEnd/TimerService/Triggers/Trigger[@name='test_add_demo']");
			assertEquals(valToAdd, ((Node) results.get(0)).asXML());
			assertEquals(valToAdd2, ((Node) results.get(1)).asXML());

			// 8.add to a null string only one steps.
			String val="<blacklist>127.0.0.1;192.168.2.1</blacklist>";
			String path="/blacklist";
			newXML = XmlUtil.xcapAdd(null, path,
					val,true);
			dom4jConf = new Dom4jConfiguration(new StringReader(newXML), null);
			confView = new ConfigurationView(dom4jConf, null);
			results = confView.queryAll(path);
			assertEquals(((Node)results.get(0)).asXML(),val);
			
			//9.add to a null string with property.
			
			val="rule string";
			path="config/gamerule/@rule";
			newXML = XmlUtil.xcapAdd(null,path,val,true);
			dom4jConf = new Dom4jConfiguration(new StringReader(newXML), null);
			confView = new ConfigurationView(dom4jConf, null);
			results = confView.queryAll(path);
			assertEquals(((Attribute)results.get(0)).getText(),val);

		} catch (DocumentException e)
		{
			assertTrue(false);
		} catch (IOException e)
		{
			assertTrue(false);
		}
	}
}
