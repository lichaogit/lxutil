package test.org.lx.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.lx.util.JsonUtil;
import org.lx.util.LogicUtil;

public class testLogicUtil extends TestCase
{
	public void testArrayCompare()
	{
		byte[] b1 = { 1, 2, 3, 4 };
		byte[] b2 = { 1, 2, 3, 4 };
		assertEquals(LogicUtil.arrayCompare(b1, b2), 0);

		b2 = new byte[] { 1, 2, 3, 5 };
		assertEquals(LogicUtil.arrayCompare(b1, b2), -1);

		b1 = new byte[] { 2, 2, 3, 5 };
		assertEquals(LogicUtil.arrayCompare(b1, b2), 1);
	}

	public void testfmod()
	{
		double f = LogicUtil.fmod(32.5f, 32.0f);
		f = LogicUtil.fmod(32.5f, 32.1f);
		f = LogicUtil.fmod(31f, 32.1f);

	}

	public void testGetCurPhaseIndex()
	{
		float times[] = { 2, 20, 5, 5 };
		int result = LogicUtil.getCurPhaseIndex(times, 31);
		assertEquals(result, 3);

		result = LogicUtil.getCurPhaseIndex(times, 32);
		assertEquals(result, 3);

		result = LogicUtil.getCurPhaseIndex(times, 32.1f);
		assertEquals(result, -1);

		result = LogicUtil.getCurPhaseIndex(times, 0f);
		assertEquals(result, 0);

		result = LogicUtil.getCurPhaseIndex(times, 1f);
		assertEquals(result, 0);

		result = LogicUtil.getCurPhaseIndex(times, 3f);
		assertEquals(result, 1);

		result = LogicUtil.getCurPhaseIndex(times, 22);
		assertEquals(result, 1);

		result = LogicUtil.getCurPhaseIndex(times, 22.1f);
		assertEquals(result, 2);
	}

	public void testJson()
	{
		JSONObject obj = new JSONObject();
		JSONObject subObj = new JSONObject();
		subObj.put("subkey", "subVal");
		obj.put("key", subObj);

		JSONObject newObj = new JSONObject();
		newObj.putAll(Collections.unmodifiableMap(obj));
		JSONObject newSubObj = (JSONObject) newObj.get("key");
		newSubObj.put("subkey", "subValNew");
		newSubObj.put("subkey1", "subValNew1");
	}

	public void testGetLevelObj()
	{
		String levelStr = "[{\"value\":200,\"permil\":-300},{\"value\":400,\"permil\":-250},{\"value\":600,\"permil\":-200},{\"value\":800,\"permil\":-150},{\"value\":1000,\"permil\":0},{\"value\":1300,\"permil\":300},{\"value\":1500,\"permil\":500},{\"value\":1800,\"permil\":800},{\"value\":3000,\"permil\":900}]";

		JSONArray levels = (JSONArray) JsonUtil.parseJpath(levelStr, null);
		Map oddsMap = (Map) LogicUtil.getLevelObj(levels, "value", -100);
		long permille = ((Number) oddsMap.get("permil")).longValue();
		assertTrue("", permille == -300);

		oddsMap = (Map) LogicUtil.getLevelObj(levels, "value", 0);
		permille = ((Number) oddsMap.get("permil")).longValue();
		assertTrue("", permille == -300);

		oddsMap = (Map) LogicUtil.getLevelObj(levels, "value", 199);
		permille = ((Number) oddsMap.get("permil")).longValue();
		assertTrue("", permille == -300);

		oddsMap = (Map) LogicUtil.getLevelObj(levels, "value", 200);
		permille = ((Number) oddsMap.get("permil")).longValue();
		assertTrue("", permille == -250);

		oddsMap = (Map) LogicUtil.getLevelObj(levels, "value", 3000);
		permille = ((Number) oddsMap.get("permil")).longValue();
		assertTrue("", permille == 900);

		oddsMap = (Map) LogicUtil.getLevelObj(levels, "value", 2999);
		permille = ((Number) oddsMap.get("permil")).longValue();
		assertTrue("", permille == 900);
	}

	public void testMapGetKeys()
	{
		String mapStr = "{\"0\":1,\"16\":1,\"17\":1,\"18\":1,\"19\":1,\"20\":1,\"21\":1,\"22\":2,\"23\":3,\"24\":3,\"32\":3,\"33\":3,\"34\":3,\"48\":3,\"64\":3}";
		Map map = (Map) JsonUtil.parseJpath(mapStr, null);
		List keys = LogicUtil.mapGetKeys(map, 1);
		assertEquals(keys.size(), 7);

		keys = LogicUtil.mapGetKeys(map, 0);
		assertEquals(keys.size(), 0);

		keys = LogicUtil.mapGetKeys(map, 3);
		Object maxKey = Collections.max(keys);
		Object minKey = Collections.min(keys);
		assertEquals(maxKey, "64");
		assertEquals(minKey, "23");

	}

	public void testMapGetMatchedKey()
	{
		String mapStr = "{\"1\":-1000,\"2\":-800,\"3\":-600,\"4\":-400,\"5\":-200,\"6\":0,\"7\":200,\"8\":400,\"10\":600,\"11\":800,\"12\":1000}";
		Map map = (Map) JsonUtil.parseJpath(mapStr, null);
		List matchedKeys = LogicUtil.mapGetMatchedKeys(map,
				LogicUtil.MAP_OP_KEY_INCLUDE, null, 1, 101);
		assertEquals(matchedKeys.get(0), "7");// 200

		matchedKeys = LogicUtil.mapGetMatchedKeys(map,
				LogicUtil.MAP_OP_KEY_INCLUDE, null, 1, 99);
		assertEquals(matchedKeys.get(0), "6");// 0

		matchedKeys = LogicUtil.mapGetMatchedKeys(map,
				LogicUtil.MAP_OP_KEY_INCLUDE, null, 1, -99);
		assertEquals(matchedKeys.get(0), "6");// 0

		matchedKeys = LogicUtil.mapGetMatchedKeys(map,
				LogicUtil.MAP_OP_KEY_INCLUDE, null, 1, -101);
		assertEquals(matchedKeys.get(0), "5");// -200

		matchedKeys = LogicUtil.mapGetMatchedKeys(map,
				LogicUtil.MAP_OP_KEY_INCLUDE, null, 1, -1001);
		assertEquals(matchedKeys.get(0), "1");// -1200

		matchedKeys = LogicUtil.mapGetMatchedKeys(map,
				LogicUtil.MAP_OP_KEY_INCLUDE, null, 1, 1001);
		assertEquals(matchedKeys.get(0), "12");// 1000

		// test exclude&include;
		ArrayList keys = new ArrayList();
		keys.add("5");
		matchedKeys = LogicUtil.mapGetMatchedKeys(map,
				LogicUtil.MAP_OP_KEY_EXCLUDE, keys, 1, -101);
		assertEquals(matchedKeys.get(0), "6");// 0

		matchedKeys = LogicUtil.mapGetMatchedKeys(map,
				LogicUtil.MAP_OP_KEY_INCLUDE, keys, 1, -101);
		assertEquals(matchedKeys.get(0), "5");// -200

	}

	public void testGetConfMatchItem()
	{
		String filterStr = "{\"$(clientInfo/version/channel)\":{\"08\":{\"$(clientInfo/version/eng)\":{\">=1.3200\":\"niu_ios_qq\"}},\"09\":\"slwh_ios\",\"03\":\"slwh_android\",\"ex01\":\"ex01\",\"dev\":\"dev\",\"(ex)?\\\\d+\":\"default\"}}";
		Map filter = (Map) JsonUtil.parseJpath(filterStr, null);

		String inputStr = "{\"sso\":{\"cmd\":\"getParams\",\"ch\":\"02\",\"id\":{\"qq\":1,\"wx\":1}},\"clientInfo\":{\"netInfo\":{\"active\":\"WIFI\",\"nets\":{\"WIFI\":{\"rm\":0,\"desc\":0,\"av\":1,\"ssid\":\"testWIFI123456789012345678901234567890\",\"conn\":1,\"tn\":\"WIFI\",\"t\":1,\"ext\":\"888\",\"stn\":\"\",\"st\":0},\"mobile_supl\":{\"conn\":0,\"tn\":\"mobile_supl\",\"desc\":0,\"rm\":0,\"st\":2,\"stn\":\"EDGE\",\"t\":3,\"av\":1},\"mobile_hipri\":{\"conn\":0,\"tn\":\"mobile_hipri\",\"desc\":0,\"rm\":0,\"st\":2,\"stn\":\"EDGE\",\"t\":5,\"av\":1},\"mobile\":{\"rm\":0,\"desc\":0,\"av\":1,\"conn\":0,\"tn\":\"mobile\",\"t\":0,\"ext\":\"cmwap\",\"stn\":\"EDGE\",\"st\":2},\"mobile_mms\":{\"conn\":0,\"tn\":\"mobile_mms\",\"desc\":0,\"rm\":0,\"st\":2,\"stn\":\"EDGE\",\"t\":2,\"av\":1},\"mobile_dun\":{\"conn\":0,\"tn\":\"mobile_dun\",\"desc\":0,\"rm\":0,\"st\":2,\"stn\":\"EDGE\",\"t\":4,\"av\":1},\"WIFI_P2P\":{\"conn\":0,\"tn\":\"WIFI_P2P\",\"desc\":0,\"rm\":0,\"st\":0,\"stn\":\"\",\"t\":13,\"av\":1},\"mobile_ims\":{\"conn\":0,\"tn\":\"mobile_ims\",\"desc\":0,\"rm\":0,\"st\":2,\"stn\":\"EDGE\",\"t\":11,\"av\":1},\"mobile_cbs\":{\"conn\":0,\"tn\":\"mobile_cbs\",\"desc\":0,\"rm\":0,\"st\":2,\"stn\":\"EDGE\",\"t\":12,\"av\":1},\"mobile_fota\":{\"conn\":0,\"tn\":\"mobile_fota\",\"desc\":0,\"rm\":0,\"st\":2,\"stn\":\"EDGE\",\"t\":10,\"av\":1}}},\"version\":{\"inengver\":\"1.3200\",\"eng\":\"1.3200\",\"cusso\":1,\"channel\":\"02\",\"ssoStatusInit\":{\"qq\":1,\"wx\":1}},\"device\":{\"tmSerial\":\"test00810908f2180781\",\"newUser\":\"0\",\"c\":\"cefaca756873d5b170670fcbe8f9115d36eaee2689d980a1da8141bc16f6c31112a5b035619980045b3cca9cb4c0e6fc0a76af0913c3ebcac5babe335be22a08d113cca03f9400071db4d6c7d92ad9bdbdfca12f8a765c96b7b94968a71519c427145c8016852c2969be196e1bf056963\",\"model\":\"Coolpad 8297W\",\"device\":\"test36026462119\",\"imsi\":\"test08239691126\",\"device2\":\"test36026513704\",\"ChannelID\":\"A001\",\"b\":\"006d8b0d133a5e53257\",\"android_id\":\"test6d1ae9f39752\",\"versionName\":\"1.0\",\"a\":\"8380f41bb5\",\"brand\":\"Coolpad\",\"release\":\"4.2.2\",\"uuid\":\"bb056d1ae9f30252\",\"versionCode\":1,\"mobile\":\"+8613818254872\",\"vga\":\"1280*720\"}}}";
		Map input = (Map) JsonUtil.parseJpath(inputStr, null);
		Object item = LogicUtil.getConfMatchItem(filter, input);
		assertEquals(item, "default");

		inputStr = "{\"sso\":{\"cmd\":\"getParams\",\"ch\":\"08\",\"id\":{\"qq\":1,\"wx\":1}},\"clientInfo\":{\"netInfo\":{\"active\":\"WIFI\",\"nets\":{\"WIFI\":{\"rm\":0,\"desc\":0,\"av\":1,\"ssid\":\"testWIFI123456789012345678901234567890\",\"conn\":1,\"tn\":\"WIFI\",\"t\":1,\"ext\":\"888\",\"stn\":\"\",\"st\":0},\"mobile_supl\":{\"conn\":0,\"tn\":\"mobile_supl\",\"desc\":0,\"rm\":0,\"st\":2,\"stn\":\"EDGE\",\"t\":3,\"av\":1},\"mobile_hipri\":{\"conn\":0,\"tn\":\"mobile_hipri\",\"desc\":0,\"rm\":0,\"st\":2,\"stn\":\"EDGE\",\"t\":5,\"av\":1},\"mobile\":{\"rm\":0,\"desc\":0,\"av\":1,\"conn\":0,\"tn\":\"mobile\",\"t\":0,\"ext\":\"cmwap\",\"stn\":\"EDGE\",\"st\":2},\"mobile_mms\":{\"conn\":0,\"tn\":\"mobile_mms\",\"desc\":0,\"rm\":0,\"st\":2,\"stn\":\"EDGE\",\"t\":2,\"av\":1},\"mobile_dun\":{\"conn\":0,\"tn\":\"mobile_dun\",\"desc\":0,\"rm\":0,\"st\":2,\"stn\":\"EDGE\",\"t\":4,\"av\":1},\"WIFI_P2P\":{\"conn\":0,\"tn\":\"WIFI_P2P\",\"desc\":0,\"rm\":0,\"st\":0,\"stn\":\"\",\"t\":13,\"av\":1},\"mobile_ims\":{\"conn\":0,\"tn\":\"mobile_ims\",\"desc\":0,\"rm\":0,\"st\":2,\"stn\":\"EDGE\",\"t\":11,\"av\":1},\"mobile_cbs\":{\"conn\":0,\"tn\":\"mobile_cbs\",\"desc\":0,\"rm\":0,\"st\":2,\"stn\":\"EDGE\",\"t\":12,\"av\":1},\"mobile_fota\":{\"conn\":0,\"tn\":\"mobile_fota\",\"desc\":0,\"rm\":0,\"st\":2,\"stn\":\"EDGE\",\"t\":10,\"av\":1}}},\"version\":{\"inengver\":\"1.3200\",\"eng\":\"1.3200\",\"cusso\":1,\"channel\":\"08\",\"ssoStatusInit\":{\"qq\":1,\"wx\":1}},\"device\":{\"tmSerial\":\"test00810908f2180781\",\"newUser\":\"0\",\"c\":\"cefaca756873d5b170670fcbe8f9115d36eaee2689d980a1da8141bc16f6c31112a5b035619980045b3cca9cb4c0e6fc0a76af0913c3ebcac5babe335be22a08d113cca03f9400071db4d6c7d92ad9bdbdfca12f8a765c96b7b94968a71519c427145c8016852c2969be196e1bf056963\",\"model\":\"Coolpad 8297W\",\"device\":\"test36026462119\",\"imsi\":\"test08239691126\",\"device2\":\"test36026513704\",\"ChannelID\":\"A001\",\"b\":\"006d8b0d133a5e53257\",\"android_id\":\"test6d1ae9f39752\",\"versionName\":\"1.0\",\"a\":\"8380f41bb5\",\"brand\":\"Coolpad\",\"release\":\"4.2.2\",\"uuid\":\"bb056d1ae9f30252\",\"versionCode\":1,\"mobile\":\"+8613818254872\",\"vga\":\"1280*720\"}}}";
		input = (Map) JsonUtil.parseJpath(inputStr, null);
		item = LogicUtil.getConfMatchItem(filter, input);
		assertEquals(item, "default");

		String niu_ios_non_qq_inputStr = "{\"sso\":{\"cmd\":\"getParams\",\"ch\":\"02\",\"id\":{\"qq\":1,\"wx\":1}},\"clientInfo\":{\"netInfo\":{\"active\":\"WIFI\",\"nets\":{\"WIFI\":{\"rm\":0,\"desc\":0,\"av\":1,\"ssid\":\"testWIFI123456789012345678901234567890\",\"conn\":1,\"tn\":\"WIFI\",\"t\":1,\"ext\":\"888\",\"stn\":\"\",\"st\":0},\"mobile_supl\":{\"conn\":0,\"tn\":\"mobile_supl\",\"desc\":0,\"rm\":0,\"st\":2,\"stn\":\"EDGE\",\"t\":3,\"av\":1},\"mobile_hipri\":{\"conn\":0,\"tn\":\"mobile_hipri\",\"desc\":0,\"rm\":0,\"st\":2,\"stn\":\"EDGE\",\"t\":5,\"av\":1},\"mobile\":{\"rm\":0,\"desc\":0,\"av\":1,\"conn\":0,\"tn\":\"mobile\",\"t\":0,\"ext\":\"cmwap\",\"stn\":\"EDGE\",\"st\":2},\"mobile_mms\":{\"conn\":0,\"tn\":\"mobile_mms\",\"desc\":0,\"rm\":0,\"st\":2,\"stn\":\"EDGE\",\"t\":2,\"av\":1},\"mobile_dun\":{\"conn\":0,\"tn\":\"mobile_dun\",\"desc\":0,\"rm\":0,\"st\":2,\"stn\":\"EDGE\",\"t\":4,\"av\":1},\"WIFI_P2P\":{\"conn\":0,\"tn\":\"WIFI_P2P\",\"desc\":0,\"rm\":0,\"st\":0,\"stn\":\"\",\"t\":13,\"av\":1},\"mobile_ims\":{\"conn\":0,\"tn\":\"mobile_ims\",\"desc\":0,\"rm\":0,\"st\":2,\"stn\":\"EDGE\",\"t\":11,\"av\":1},\"mobile_cbs\":{\"conn\":0,\"tn\":\"mobile_cbs\",\"desc\":0,\"rm\":0,\"st\":2,\"stn\":\"EDGE\",\"t\":12,\"av\":1},\"mobile_fota\":{\"conn\":0,\"tn\":\"mobile_fota\",\"desc\":0,\"rm\":0,\"st\":2,\"stn\":\"EDGE\",\"t\":10,\"av\":1}}},\"version\":{\"inengver\":\"1.3200\",\"eng\":\"1.3200\",\"cusso\":1,\"channel\":\"02\",\"ssoStatusInit\":{\"qq\":1,\"wx\":1}},\"device\":{\"tmSerial\":\"test00810908f2180781\",\"newUser\":\"0\",\"c\":\"cefaca756873d5b170670fcbe8f9115d36eaee2689d980a1da8141bc16f6c31112a5b035619980045b3cca9cb4c0e6fc0a76af0913c3ebcac5babe335be22a08d113cca03f9400071db4d6c7d92ad9bdbdfca12f8a765c96b7b94968a71519c427145c8016852c2969be196e1bf056963\",\"model\":\"Coolpad 8297W\",\"device\":\"test36026462119\",\"imsi\":\"test08239691126\",\"device2\":\"test36026513704\",\"ChannelID\":\"A001\",\"b\":\"006d8b0d133a5e53257\",\"android_id\":\"test6d1ae9f39752\",\"versionName\":\"1.0\",\"a\":\"8380f41bb5\",\"brand\":\"Coolpad\",\"release\":\"4.2.2\",\"uuid\":\"bb056d1ae9f30252\",\"versionCode\":1,\"mobile\":\"+8613818254872\",\"vga\":\"1280*720\"}}}";
		input = (Map) JsonUtil.parseJpath(niu_ios_non_qq_inputStr, null);
		item = LogicUtil.getConfMatchItem(filter, input);
		assertTrue(item == null);

	}

	public void testMapSum()
	{
		String mapStr = "{\"1\":-1,\"2\":-2,\"3\":-3,\"4\":-4,\"5\":-5,\"6\":0,\"7\":1,\"8\":2,\"10\":3,\"11\":4,\"12\":6}";
		Map map = (Map) JsonUtil.parseJpath(mapStr, null);
		long sum = LogicUtil.mapSum(map, false, LogicUtil.MAP_OP_KEY_INCLUDE,
				null);
		assertEquals(sum, 1);

		ArrayList keys = new ArrayList();
		keys.add("5");
		sum = LogicUtil.mapSum(map, false, LogicUtil.MAP_OP_KEY_EXCLUDE, keys);
		assertEquals(sum, 1 + 5);
		sum = LogicUtil.mapSum(map, false, LogicUtil.MAP_OP_KEY_INCLUDE, keys);
		assertEquals(sum, -5);

		sum = LogicUtil.mapSum(map, true, LogicUtil.MAP_OP_KEY_INCLUDE, null);
		assertEquals(sum, 31);

		sum = LogicUtil.mapSum(map, true, LogicUtil.MAP_OP_KEY_EXCLUDE, keys);
		assertEquals(sum, 26);
	}

	public void testMapGetMaxMinKeys()
	{
		HashMap testMap = new HashMap();
		testMap.put("a", 8);
		testMap.put("b", 10);
		testMap.put("c", 10);
		testMap.put("d", 7);
		testMap.put("e", 4);
		testMap.put("f", 5);
		testMap.put("g", 6);
		testMap.put("h", 6);

		ArrayList keys = new ArrayList();
		keys.add("c");
		keys.add("d");

		List result = LogicUtil.mapGetMaxKeys(testMap, true, null, 1);
		assertEquals(result.size(), 2);
		assertTrue(result.contains("b"));
		assertTrue(result.contains("c"));

		result = LogicUtil.mapGetMaxKeys(testMap, true, null, 2);
		assertEquals(result.size(), 3);

		assertTrue(result.contains("b"));
		assertTrue(result.contains("c"));
		assertTrue(result.contains("a"));

		result = LogicUtil.mapGetMaxKeys(testMap, true, null, 3);
		assertEquals(result.size(), 4);
		assertTrue(result.contains("b"));
		assertTrue(result.contains("c"));
		assertTrue(result.contains("a"));
		assertTrue(result.contains("d"));

		result = LogicUtil.mapGetMaxKeys(testMap, true, null, 4);
		assertEquals(result.size(), 6);
		assertTrue(result.contains("b"));
		assertTrue(result.contains("c"));
		assertTrue(result.contains("a"));
		assertTrue(result.contains("d"));
		assertTrue(result.contains("g"));
		assertTrue(result.contains("h"));

		// 2.test include

		result = LogicUtil.mapGetMaxKeys(testMap, true, keys, 1);
		assertEquals(result.size(), 1);
		assertTrue(result.contains("c"));

		result = LogicUtil.mapGetMaxKeys(testMap, true, keys, 2);
		assertEquals(result.size(), 2);
		assertTrue(result.contains("c"));
		assertTrue(result.contains("d"));

		// 3.test exclude
		result = LogicUtil.mapGetMaxKeys(testMap, false, keys, 1);
		assertEquals(result.size(), 1);
		assertTrue(result.contains("b"));

		result = LogicUtil.mapGetMaxKeys(testMap, false, keys, 2);
		assertEquals(result.size(), 2);
		assertTrue(result.contains("b"));
		assertTrue(result.contains("a"));

		result = LogicUtil.mapGetMaxKeys(testMap, false, keys, 3);
		assertEquals(result.size(), 4);
		assertTrue(result.get(0).equals("b"));
		assertTrue(result.get(1).equals("a"));
		assertTrue(result.contains("g"));
		assertTrue(result.contains("h"));

		result = LogicUtil.mapGetMinKeys(testMap, true, keys, 1);
		assertEquals(result.size(), 1);
		assertTrue(result.contains("d"));

		// 4.get Max
		result = LogicUtil.mapGetMaxKeys(testMap, false, keys,
				Integer.MAX_VALUE);
		assertEquals(result.size(), 6);
		assertTrue(result.get(0).equals("b"));
		assertTrue(result.get(1).equals("a"));
		assertTrue(result.contains("g"));
		assertTrue(result.contains("h"));
		assertTrue(result.get(4).equals("f"));
		assertTrue(result.get(5).equals("e"));

		result = LogicUtil.mapGetMinKeys(testMap, false, keys,
				Integer.MAX_VALUE);
		assertEquals(result.size(), 6);
		assertTrue(result.get(0).equals("e"));
		assertTrue(result.get(1).equals("f"));
		assertTrue(result.contains("h"));
		assertTrue(result.contains("g"));
		assertTrue(result.get(4).equals("a"));
		assertTrue(result.get(5).equals("b"));

	}

	public void testMapGetMatchedKeys()
	{
		HashMap testMap = new HashMap();
		testMap.put("a", -100);
		testMap.put("b", -50);
		testMap.put("c", 0);
		testMap.put("d", 50);
		testMap.put("e", 100);
		testMap.put("f", 200);

		List result = LogicUtil.mapGetMatchedKeys(testMap, true, null, 1, 26);
		assertEquals(result.size(), 1);
		assertTrue(result.get(0).equals("d"));

		result = LogicUtil.mapGetMatchedKeys(testMap, true, null, 1, 24);
		assertEquals(result.size(), 1);
		assertTrue(result.get(0).equals("c"));

		result = LogicUtil.mapGetMatchedKeys(testMap, true, null, 1, 25);
		assertEquals(result.size(), 1);// c or d

	}
}
