package test.org.lx.arch.logic;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import junit.framework.TestCase;

import org.dom4j.Element;
import org.lx.arch.ConfigurationView;
import org.lx.arch.Dom4jConfiguration;
import org.lx.arch.expr.ExprParser;
import org.lx.arch.logic.ConditionBuilder;
import org.lx.arch.logic.ConditionParserException;
import org.lx.arch.logic.ExpressItemMatcher;
import org.lx.util.IMatcher;
import org.lx.util.MapValueRender;
import org.lx.util.StringEx;

import test.org.lx.arch.testConfigurationView;

public class ConditionMatcherTest extends TestCase
{
	ConfigurationView confView;

	protected void setUp() throws Exception
	{
		super.setUp();
		String model_file = StringEx.getPackagePath(ConditionMatcherTest.class)
				+ "/ConditionMatcherTest.xml";
		InputStream in = testConfigurationView.class
				.getResourceAsStream(model_file);

		Dom4jConfiguration conf = new Dom4jConfiguration(in);

		confView = new ConfigurationView(conf, "/HttpMatcher");

	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	public void testMatcher()
	{
		ConditionBuilder builder = new ConditionBuilder() {
			protected IMatcher createItemMatcher(Map attrs)
			{
				return new ExpressItemMatcher(attrs,
						ExprParser.getDefaultExprParser());
			}
		};

		try
		{
			IMatcher matcher = builder.compile((Element) confView
					.query("/HttpMatcher/Matcher[@name='binExpr']/*[1]"));
			HashMap param = new HashMap();

			Checksum checksumEngine = new Adler32();
			String str = "hello";
			byte[] content = str.getBytes("UTF-8");
			checksumEngine.update(content, 0, content.length);
			long crc = checksumEngine.getValue();

			param.put("size", Integer.valueOf(100));
			param.put("Content-Type", "image/gif");
			param.put("crc", Long.valueOf(crc));
			param.put("content", str);

			boolean result = matcher.isMatch(new MapValueRender(param));
			assertEquals(true, result);

			param.put("size", Integer.valueOf(20000));
			result = matcher.isMatch(new MapValueRender(param));
			assertEquals(false, result);

			param.put("size", Integer.valueOf(100));
			param.put("Content-Type", "image/jpeg");
			result = matcher.isMatch(new MapValueRender(param));
			assertEquals(true, result); 
			
		} catch (ConditionParserException e)
		{
			assertTrue(false);
		} catch (UnsupportedEncodingException e)
		{
			assertTrue(false);
		}

	}
}
