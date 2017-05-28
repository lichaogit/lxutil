package test.org.lx.arch.expr;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import junit.framework.TestCase;

import org.lx.arch.expr.ExprParser;
import org.lx.util.MapValueRender;

import test.org.lx.util.testStringEx.GetConfigFunction;
import test.org.lx.util.testStringEx.testFunction;

public class ExprParserTest extends TestCase
{

	protected long getcheksum(String str, Checksum checksumEngine)
			throws UnsupportedEncodingException
	{
		byte[] bin = str.getBytes("UTF-8");
		checksumEngine.reset();
		checksumEngine.update(bin, 0, bin.length);
		return checksumEngine.getValue();
	}

	public void testDefaultParse()
	{
		try
		{
			String testString = "hello";
			Checksum checksumEngine = new Adler32();

			long checksum = getcheksum(testString, checksumEngine);

			Map mapInfo = new HashMap();
			mapInfo.put("str", testString);
			mapInfo.put("expected", Long.valueOf(checksum));

			MapValueRender params = new MapValueRender(mapInfo);
			ExprParser parser = ExprParser.getDefaultExprParser();

			// 1.test for equals/compare/crc
			// 1.1 check parse with parameter.
			String eval = parser.parse("crc($(str))", params);
			assertEquals(checksum, Long.parseLong(eval));

			eval = parser.parse("crc($(str)1)", params);
			checksum = getcheksum(testString + "1", checksumEngine);
			assertEquals(checksum, Long.parseLong(eval));

			// 1.2 check parse without parameter.
			eval = parser.parse("crc($(str))", null);
			checksum = getcheksum("$(str)", checksumEngine);
			assertEquals(checksum, Long.parseLong(eval));

			eval = parser.parse("crc($(str)1)", null);
			checksum = getcheksum("$(str)1", checksumEngine);
			assertEquals(checksum, Long.parseLong(eval));

			eval = parser.parse("compare(crc($(str)),$(expected))", params);
			assertEquals("0", eval);

			String expr = "equals(compare(crc($(str)),$(expected)),0)";
			eval = parser.parse(expr, params);
			assertEquals("true", eval);
			// 1.3 test equals for .
			mapInfo.clear();
			mapInfo.put("deep", new Integer(1));
			eval = parser.parse("equals($(deep),1)", params);
			assertEquals("true", eval);

			// 2.test for isInArray
			mapInfo.clear();
			String key = "key1";
			String[] array = new String[] { key, "key2" };
			mapInfo.put("key", key);
			mapInfo.put("params", array);

			expr = "isInArray($(key),$(params))";
			eval = parser.parse(expr, params);
			assertEquals("true", eval);

			// 3.test for propertyAt
			Map props = new HashMap();
			props.put("size", "1024");
			props.put("type", "image");
			mapInfo.clear();
			mapInfo.put("props", props);
			eval = parser.parse("propertyAt($(props),size)", params);
			assertEquals("1024", eval);
			eval = parser.parse("propertyAt($(props),type)", params);
			assertEquals("image", eval);

		} catch (Exception e)
		{
			assertTrue(false);
		}
	}

	//
	public void testRegExFunctionExpress()
	{
		String str = "isInArray($(ip),testFunc(getConfig(\"blacklist\"),\"test\"))";
		Map vars = new HashMap();
		vars.put("ip", "127.0.0.1");
		vars.put("user", "anonymous");
		MapValueRender params = new MapValueRender(vars);
		String result;
		try
		{
			ExprParser parser = ExprParser.getDefaultExprParser();
			parser.registerFunction("testFunc", new testFunction());
			parser.registerFunction("getConfig", new GetConfigFunction());

			result = parser.parse(str, params);
			assertEquals("true", result);
		} catch (Exception e)
		{
			assertTrue(false);
		}
	}
}
