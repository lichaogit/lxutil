package test.org.lx.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.lx.arch.ConfigurationView;
import org.lx.arch.Dom4jConfiguration;
import org.lx.arch.expr.ExprException;
import org.lx.arch.expr.IExprFunction;
import org.lx.util.Base64;
import org.lx.util.StringEx;
import org.lx.util.StringValueRender;

public class testStringEx extends TestCase
{
	ConfigurationView m_conf = null;

	protected ConfigurationView getConfiginfo()
	{
		ConfigurationView retval = null;
		try
		{
			if (m_conf == null)
			{
				String model_file = StringEx.getPackagePath(testXmlUtil.class)
						+ "/testStringEx.xml";
				InputStream in = testStringEx.class
						.getResourceAsStream(model_file);

				Dom4jConfiguration conf = new Dom4jConfiguration(in);
				m_conf = new ConfigurationView(conf, "/testItem");
			}
			retval = m_conf;

		} catch (Throwable e)
		{
			fail("fail at get the ConfigurationView instance");
		}
		return retval;
	}

	public void testMIMEMatcher()
	{
		ConfigurationView conf = getConfiginfo();
		Map map = conf.getNameValuePaired("testMIMEMatcher/item", "name",
				"type");

		Iterator it = map.keySet().iterator();
		String key = null;
		String expectType = null;
		String currentType = null;
		while (it.hasNext())
		{
			key = (String) it.next();
			expectType = (String) map.get(key);
			if (key.equals("text/html; Charset=gbk"))
			{
				int i = 0;
			}
			currentType = StringEx.contentType2dataType(key);
			assertEquals(key + " Error:", expectType, currentType);
		}
	}

	protected String getRegex(String regexName)
	{
		String retval = null;
		final ConfigurationView conf = getConfiginfo();

		StringValueRender rr = new StringValueRender() {
			public String get(String matchedStr)
			{
				Object obj = conf
						.query("testRegex/regex/item[@name='" + matchedStr
								+ "']", ConfigurationView.STRING_TRANSFORM);
				String retval = obj == null ? null : ((String) obj).trim();
				// convert the \ to \\ because the return value will be used as
				// the regex.
				if (retval != null)
				{
					retval = retval.replace("\\", "\\\\");

				}
				return retval;
			}
		};

		retval = (String) conf.query("testRegex/regex/item[@name='" + regexName
				+ "']", ConfigurationView.STRING_TRANSFORM);

		retval = StringEx.replaceAll(retval.trim(), rr);
		return retval;
	}

	public void testRegExReplace()
	{
		ConfigurationView conf = getConfiginfo();
		List items = conf.queryAll("testRegex/replace/item");
		Iterator it = items.iterator();
		Object ele = null;
		String regexName = null;
		String regex = null;

		String src = null;
		String resultExp = null;
		String result = null;
		String with = null;
		Object obj = null;
		// 分割参数:
		// ,不在""里面
		// ，不在()里面
		// (?=([^\]")).+\1

		while (it.hasNext())
		{
			ele = it.next();
			regexName = conf.getAttribute(".", "regex", ele);

			src = (String) conf.query("src", ele,
					ConfigurationView.STRING_TRANSFORM);
			src = src.trim();

			resultExp = (String) conf.query("result", ele,
					ConfigurationView.STRING_TRANSFORM);
			resultExp = resultExp.trim();

			with = conf.getAttribute(".", "with", ele);

			regex = getRegex(regexName);
			result = src.replaceAll(regex, with);
			assertEquals(resultExp, result);
		}
	}

	public void testRegExParser()
	{
		final ConfigurationView conf = getConfiginfo();
		List items = conf.queryAll("testRegex/parser/item");
		Iterator it = items.iterator();
		Object curEle = null;
		Object groupEle = null;
		String regexName = null;
		String regex = null;
		String str = null;
		Matcher matcher = null;
		Pattern p = null;

		Object obj = null;

		while (it.hasNext())
		{
			curEle = it.next();
			regexName = conf.getAttribute(".", "regex", curEle);
			regex = getRegex(regexName);

			obj = conf.query("string", curEle,
					ConfigurationView.STRING_TRANSFORM);
			str = obj == null ? null : ((String) obj).trim();

			p = Pattern.compile(regex);
			matcher = p.matcher(str);
			// check the first matcher.
			int matcherIndex = 0;
			while (matcher.find())
			{
				matcherIndex++;
				int count = matcher.groupCount();
				String groupVal = null;
				String groupIndex = null;
				String value = null;
				int groupCount = ((Double) conf
						.query("count(matcher[position()=" + matcherIndex
								+ "]/group)", curEle, null)).intValue();

				for (int i = 0; i < groupCount; i++)
				{
					groupEle = conf.query("matcher[position()=" + matcherIndex
							+ "]/group[position()=" + (i + 1) + "]", curEle,
							null);
					groupVal = conf.getAttribute(".", "value", groupEle);
					// use the cdata if the value attribute is null.
					if (groupVal == null && groupEle != null)
					{
						groupVal = (String) conf.query(".", groupEle,
								ConfigurationView.STRING_TRANSFORM);
						groupVal = groupVal.trim();
					}
					groupIndex = conf.getAttribute(".", "index", groupEle);
					value = groupIndex == null ? matcher.group(i) : matcher
							.group(Integer.parseInt(groupIndex));
					if (value != null)
					{
						value = value.trim();
					}
					// skip the null group
					assertEquals("", groupVal, value);
				}
			}
			// check the matcher count.
			long matcherCount = Math.round(((Double) conf.query(
					"count(matcher)", curEle, null)).doubleValue());

			assertEquals("Matcher count:", matcherCount, matcherIndex);

		}
	}

	public void testRegExDomainMatcher()
	{
		String regexUrlDomain = StringEx.DEFAULT_URLDOMAIN_MATCHER;

		String protocol = StringEx.regexFind(
				"http://a.b.com:888/as.aspx?sds=23&d=2&c=%98", regexUrlDomain,
				1);
		String domain = StringEx.regexFind(
				"http://a.b.com:888/as.aspx?sds=23&d=2&c=%98", regexUrlDomain,
				2);
		String port = StringEx.regexFind(
				"http://a.b.com:888/as.aspx?sds=23&d=2&c=%98", regexUrlDomain,
				3);
		assertEquals("http", protocol);
		assertEquals("a.b.com", domain);
		assertEquals("888", port);

		String regexProtocol = "(?:(https?|ftp|file)\\:\\/\\/){0,1}";

		String[][] urlTestItems = {
				{ regexProtocol, "http://a.b.com", "http://", "http" },
				{ regexProtocol, "https://a.b.com", "https://", "https" },
				{ regexProtocol, "ftp://a.b.com", "ftp://", "ftp" },
				{ regexProtocol, "file://a.b.com", "file://", "file" },

				{ regexUrlDomain, "http://a.b.com/as.aspx?sds=23",
						"http://a.b.com", "http", "a.b.com" },
				{ regexUrlDomain, "http://a.b.com./as.aspx?sds=23",
						"http://a.b.com.", "http", "a.b.com." },
				{ regexUrlDomain,
						"http://a.b.com:888/as.aspx?sds=23&d=2&c=%98",
						"http://a.b.com:888", "http", "a.b.com", "888" },
				{ regexUrlDomain, "http://a.b.com.:888/as.aspx?sds=23",
						"http://a.b.com.:888", "http", "a.b.com.", "888" } };

		String[] item = null;
		Matcher matcher = null;
		Pattern p = null;
		for (int i = 0; i < urlTestItems.length; i++)
		{
			item = urlTestItems[i];
			p = Pattern.compile(item[0]);
			matcher = p.matcher(item[1]);
			if (matcher.find())
			{
				if (i == 7)
				{
					int kk = 0;
				}
				for (int j = 0; j < item.length - 2; j++)
				{
					assertEquals(item[j + 2], matcher.group(j));
				}

			}

		}
	}

	public static class GetConfigFunction implements IExprFunction
	{
		public Object handle(Object[] params) throws ExprException
		{
			return "127.0.0.1;192.168.2.1,111test";
		}
	}

	public static class testFunction implements IExprFunction
	{
		public Object handle(Object[] params) throws ExprException
		{
			return params[0];
		}
	}

	public void testRegExVarMatcher()
	{
		// 1. test the var $(x) mode.
		String regex = StringEx.DEFAULT_VAR_MATCHER;// "\\$\\((\\w+)\\)"
		Pattern p = Pattern.compile(regex);
		Matcher matcher = p.matcher("$(abc) is $(cd)");
		int i = 0;
		while (matcher.find())
		{
			switch (i++)
			{
			case 0:
			{
				assertEquals("$(abc)", matcher.group(0));
				assertEquals("abc", matcher.group(1));
				break;
			}
			case 1:
			{
				assertEquals("cd", matcher.group(1));
				break;
			}
			}
		}
	}

	public void testRegExXpathMatcher()
	{
		// 2. test the Xpath element.
		// there are two groups:
		// group1:element name
		// group2:condition.
		String regex = StringEx.DEFAULT_XPATH_NODETEST_MATCHER;// "/*(\\w+)?(\\[[^\\[\\]]+\\]*)?";
		Pattern p = Pattern.compile(regex);
		Matcher matcher = p
				.matcher("/TimerService/Jobs[@name='action' or @val='a']/Job[@name='demo']");
		int i = 0;
		while (matcher.find())
		{
			switch (i++)
			{
			case 0:
			{
				assertEquals("TimerService", matcher.group(1));
				break;
			}
			case 1:
			{
				assertEquals("Jobs", matcher.group(1));
				assertEquals("[@name='action' or @val='a']", matcher.group(2));
				break;
			}
			case 2:
			{
				assertEquals("Job", matcher.group(1));
				assertEquals("[@name='demo']", matcher.group(2));
				break;
			}

			}
		}

	}

	public void testParseParameters()
	{
		String testStr = null;
		String[] params = null;
		try
		{
			testStr = "$(ip),dummy(getconfig(blacklist.ip),test)";
			params = StringEx.parseParameters(testStr, ',', '(', ')');
			assertEquals(2, params.length);
			assertEquals("$(ip)", params[0]);
			assertEquals("dummy(getconfig(blacklist.ip),test)", params[1]);

			testStr = "$(ip),dummy(getconfig(blacklist.ip,\"test\"),test)";
			params = StringEx.parseParameters(testStr, ',', '(', ')');
			assertEquals(2, params.length);
			assertEquals("$(ip)", params[0]);
			assertEquals("dummy(getconfig(blacklist.ip,\"test\"),test)",
					params[1]);

			testStr = "blacklist/user[prop/@female]/@name";
			params = StringEx.parseParameters(testStr, '/', '[', ']');
			assertEquals(3, params.length);
			assertEquals("blacklist", params[0]);
			assertEquals("user[prop/@female]", params[1]);
			assertEquals("@name", params[2]);

		} catch (Exception e)
		{
			assertTrue(false);
		}

		testStr = "$(ip),dummy(getconfig(blacklist.ip),test))";
		try
		{
			params = StringEx.parseParameters(testStr, ',', '(', ')');
			assertTrue("Exception should be throwed", false);
		} catch (Exception e)
		{
			assertTrue(e.getMessage(), true);
		}
		testStr = "$(ip),dummy((getconfig(blacklist.ip),test)";
		try
		{
			params = StringEx.parseParameters(testStr, ',', '(', ')');
			assertTrue("Exception should be throwed", false);
		} catch (Exception e)
		{
			assertTrue(e.getMessage(), true);
		}

	}

	public void testParseCmdLineParameters()
	{
		StringBuffer sb = new StringBuffer();
		String te = "012345678";
		sb.append(te);
		sb.append(0);
		sb.delete(0, te.length());
		sb.append(te);
		String test = sb.toString();

		String cmdline = null;
		String[] parameters = null;

		cmdline = "GB18030 '' '09-09-2009 00:00:00' 'zhaolichao name' ";
		parameters = StringEx.parseCmdLineParameters(cmdline);
		assertEquals(parameters.length, 4);

		cmdline = "cmd \'p1\' p2 p3";
		parameters = StringEx.parseCmdLineParameters(cmdline);
		assertEquals(parameters.length, 4);
		cmdline = "cmd \'p1 p2\' p3";
		parameters = StringEx.parseCmdLineParameters(cmdline);
		assertEquals(parameters.length, 3);

		cmdline = "cmd \"p1 p2\" p3";
		parameters = StringEx.parseCmdLineParameters(cmdline);
		assertEquals(parameters.length, 3);

		cmdline = "cmd \'\' p1";
		parameters = StringEx.parseCmdLineParameters(cmdline);
		assertEquals(parameters.length, 3);

		cmdline = "cmd \'p1 \\\'p2\' p3";
		parameters = StringEx.parseCmdLineParameters(cmdline);
		assertEquals(parameters.length, 3);
		assertEquals(parameters[1], "p1 \\\'p2");

		cmdline = "GB18030 \'\' \'09-09-2009 00:00:00\' \'\' ";
		parameters = StringEx.parseCmdLineParameters(cmdline);
		assertEquals(parameters.length, 4);

		cmdline = "cmd ";
		parameters = StringEx.parseCmdLineParameters(cmdline);
		assertEquals(parameters.length, 1);

		cmdline = "\"0/10 * * * * ?\" echo echo";
		parameters = StringEx.parseCmdLineParameters(cmdline);
		assertEquals(parameters.length, 3);

		cmdline = "setConfig  TimerService \"/TimerService/Jobs/Job[@name='crawler_baidu111111' or @name='b'] [{\"attribute_id\":\"crawler_baidu\",\"trigger\":\"crawler_baidu\",\"name\":\"crawler_baidu111111\",\"command\":\"crawl111\",\"group\":\"daily\"},{\"trigger\":\"a\",\"name\":\"b\",\"command\":\"c\",\"attribute_id\":\"d\",\"group\":\"e\"}]";

		// Pattern p = Pattern.compile("[\"]?(\\S+)[\"]?");
		// Matcher matcher = p.matcher(cmdline);
		// int count=matcher.groupCount();
		// while(matcher.find())
		// {
		// matcher.group(0);
		// }
		// parameters = StringEx.parseParameters(cmdline);
		// assertEquals(parameters.length, 3);
	}

	public void testConvert()
	{
		String str = "中华人民共和国赵立超中華人民共和國趙立超abc!@";
		try
		{
			byte[] utf8raw = str.getBytes("UTF-8");
			byte[] gbkraw = str.getBytes("GBK");

			try
			{
				StringBuffer sb = new StringBuffer();
				ByteArrayInputStream bi = new ByteArrayInputStream(utf8raw);
				byte[] bBuf = new byte[3];
				int byteread = 0;
				while ((byteread = bi.read(bBuf)) != -1)
				{
					sb.append(new String(bBuf, 0, byteread, "ISO-8859-1"));
				}
				byte[] b = sb.toString().getBytes("ISO-8859-1");
				String utf8New = new String(b, "UTF-8");
				assertEquals(str, utf8New);

				// read char from inputStream.
				InputStreamReader reader = new InputStreamReader(
						new ByteArrayInputStream(utf8raw), "UTF-8");
				char[] cbuf = new char[4];
				sb.delete(0, sb.length());
				while ((byteread = reader.read(cbuf)) != -1)
				{
					sb.append(cbuf, 0, byteread);
				}
				assertEquals(str, sb.toString());

			} catch (IOException e)
			{
				assertTrue(false);
			}

			String utf8str = new String(utf8raw, "UTF-8");
			String gbkstr = new String(gbkraw, "GBK");
			assertEquals(str, utf8str);
			assertEquals(str, gbkstr);

			String utf8plainString = new String(utf8raw, "ISO-8859-1");
			String gbkplainString = new String(gbkraw, "ISO-8859-1");

			String utf8str1 = new String(
					utf8plainString.getBytes("ISO-8859-1"), "UTF-8");
			String gbkstr1 = new String(gbkplainString.getBytes("ISO-8859-1"),
					"GBK");
			assertEquals(str, utf8str1);
			assertEquals(str, gbkstr1);

			// test base64
			String utf8b64 = Base64.encodeString(str, "UTF-8");
			String gbkb64 = Base64.encodeString(str, "GBK");

			String utf8 = Base64.decodeString(utf8b64, "UTF-8");
			String gbk = Base64.decodeString(gbkb64, "GBK");

			assertEquals(str, utf8);
			assertEquals(str, gbk);

		} catch (UnsupportedEncodingException e)
		{
			assertTrue(false);
		}

	}

	public void testURLMatcher()
	{
		String[][] proxys = {
				{
						"http://domain\\user._-zhao:passwd!!@#$%^&*()_+-=,./<>?@www.abc.com:8888",
						"http", "domain\\user._-zhao",
						"passwd!!@#$%^&*()_+-=,./<>?", "www.abc.com", "8888" },
				{ "socks5://user:passwd@www.abc.com:8888", "socks5", "user",
						"passwd", "www.abc.com", "8888" },
				{ "socks5://www.abc.com:8888", "socks5", null, null,
						"www.abc.com", "8888" },
				{ "socks5://user:passwd@www.abc.com", "socks5", "user",
						"passwd", "www.abc.com", null },
				{ "socks5://www.abc.com", "socks5", null, null, "www.abc.com",
						null },
				{ "socks4://user:passwd@www.abc.com:8888", "socks4", "user",
						"passwd", "www.abc.com", "8888" },
				{ "http://user:passwd@www.abc.com:8888", "http", "user",
						"passwd", "www.abc.com", "8888" },
				{ "http://user:passwd@127.0.0.1:8888", "http", "user",
						"passwd", "127.0.0.1", "8888" },
				{ "http://127.0.0.1:8888", "http", null, null, "127.0.0.1",
						"8888" },
				{ "https://user:passwd@www.abc.com:8888", "https", "user",
						"passwd", "www.abc.com", "8888" } };

		String proxy = null;
		String protocol = null;
		String user = null;
		String passwd = null;
		String host = null;
		String port = null;
		for (int i = 0; i < proxys.length; i++)
		{
			int group = 1;

			proxy = proxys[i][0];
			String[] proxyInfos = StringEx.regexFind(proxy,
					StringEx.DEFAULT_PROXYSERVER_MATCHER, new int[] { 1, 2, 3,
							4, 5 });
			assertEquals(proxyInfos[0], proxys[i][1]);
			assertEquals(proxyInfos[1], proxys[i][2]);
			assertEquals(proxyInfos[2], proxys[i][3]);
			assertEquals(proxyInfos[3], proxys[i][4]);
			assertEquals(proxyInfos[4], proxys[i][5]);

			protocol = StringEx.regexFind(proxy,
					StringEx.DEFAULT_PROXYSERVER_MATCHER, 1);
			assertEquals(protocol, proxys[i][group++]);

			user = StringEx.regexFind(proxy,
					StringEx.DEFAULT_PROXYSERVER_MATCHER, 2);
			assertEquals(user, proxys[i][group++]);
			passwd = StringEx.regexFind(proxy,
					StringEx.DEFAULT_PROXYSERVER_MATCHER, 3);
			assertEquals(passwd, proxys[i][group++]);
			host = StringEx.regexFind(proxy,
					StringEx.DEFAULT_PROXYSERVER_MATCHER, 4);
			assertEquals(host, proxys[i][group++]);
			port = StringEx.regexFind(proxy,
					StringEx.DEFAULT_PROXYSERVER_MATCHER, 5);
			assertEquals(port, proxys[i][group++]);
		}

	}

	public void testToBase62String()
	{
		String base62 = StringEx.toBase62String(Long.MAX_VALUE);
		long maxLong = StringEx.toBase10(base62);
		assertEquals(maxLong, Long.MAX_VALUE);

		base62 = StringEx.toBase62String(92990652);

		int debug = 0;

	}

	public void testReplaceAll()
	{

		final Map params = new HashMap();
		params.put("var1", 2);
		params.put("var2", "val2");

		String result = StringEx.replaceAll("$(var$(var1))$(var3)",
				new StringValueRender() {

					@Override
					public String get(String key)
					{
						String retval = null;
						do
						{
							if (!params.containsKey(key))
							{
								break;
							}
							Object obj = params.get(key);
							retval = obj == null ? null : obj.toString();
						} while (false);
						return retval;
					}
				});
		assertEquals("val2$(var3)", result);
	}
}
