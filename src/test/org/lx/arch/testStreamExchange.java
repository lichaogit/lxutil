package test.org.lx.arch;

import java.util.HashMap;

import junit.framework.TestCase;

import org.lx.arch.FastJsonExchange;
import org.lx.arch.JSONExchange;
import org.lx.arch.StreamExchange;
import org.lx.arch.XstreamExchange;

public class testStreamExchange extends TestCase
{
	public void testFromStringPerformance()
	{
		StreamExchange exchange = null;
		String express = "{\"key2\":\"val2\",\"key1\":\"val1\"}";
		int count = 100000;
		HashMap jsonMap = null;

		// json
		exchange = new JSONExchange();
		long old = System.currentTimeMillis();
		for (int i = 0; i < count; i++)
		{
			jsonMap = (HashMap) exchange.fromString(express, HashMap.class);
		}
		long jsonDuration = System.currentTimeMillis() - old;

		// fastjson
		exchange = new FastJsonExchange();
		old = System.currentTimeMillis();
		for (int i = 0; i < count; i++)
		{
			jsonMap = (HashMap) exchange.fromString(express, HashMap.class);
		}
		long fastJsonDuration = System.currentTimeMillis() - old;

		assertTrue("fastJsonDuration=" + fastJsonDuration + ",jsonDuration="
				+ jsonDuration, jsonDuration < fastJsonDuration);
	}

	public void testToStringPerformance()
	{
		HashMap val = new HashMap();
		val.put("key1", "val1");
		val.put("key2", "val2");
		int count = 100000;

		// json
		StreamExchange json = new JSONExchange();
		long old = System.currentTimeMillis();
		for (int i = 0; i < count; i++)
		{
			json.toString(val, null);
		}

		long jsonDuration = System.currentTimeMillis() - old;

		// json
		StreamExchange fastjson = new FastJsonExchange();
		old = System.currentTimeMillis();
		for (int i = 0; i < count; i++)
		{
			fastjson.toString(val, null);
		}

		long fastJsonDuration = System.currentTimeMillis() - old;

		assertTrue(fastJsonDuration < jsonDuration);

	}

	public void testFromString()
	{
		HashMap val = new HashMap();
		val.put("key1", "val1");
		val.put("key2", "val2");

		StreamExchange exchange = null;
		String express = null;
		Object obj = null;

		// json
		exchange = new JSONExchange();
		express = exchange.toString(val, null);
		assertEquals("{\"key2\":\"val2\",\"key1\":\"val1\"}", express);

		obj = exchange.fromString(express, HashMap.class);
		HashMap jsonMap = (HashMap) obj;
		assertEquals(jsonMap.get("key2"), "val2");

		// fastjson
		exchange = new FastJsonExchange();
		express = exchange.toString(val, null);
		assertEquals("{\"key2\":\"val2\",\"key1\":\"val1\"}", express);

		obj = exchange.fromString(express, HashMap.class);
		jsonMap = (HashMap) obj;
		assertEquals(jsonMap.get("key2"), "val2");

		// xstream
		exchange = new XstreamExchange();
		express = exchange.toString(val, null);

		// obj = exchange.fromString(express, HashMap.class);

	}

	public void testToString()
	{
		HashMap map = new HashMap();
		map.put("key1", "val1");
		map.put("key2", "val2");

		StreamExchange exchange = null;
		String express = null;
		Object obj = null;

		String expect = "{\"key2\":\"val2\",\"key1\":\"val1\"}";

		// json
		exchange = new JSONExchange();
		express = exchange.toString(map, null);
		assertEquals(expect, express);

		// fastjson
		exchange = new FastJsonExchange();
		express = exchange.toString(map, null);
		assertEquals(expect, express);

	}

}
