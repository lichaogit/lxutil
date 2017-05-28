package test.org.lx.logic;

import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

import org.lx.util.DigestUtil;

public class testDigest extends TestCase
{
	public void testMD5()
	{

		DigestUtil digest = DigestUtil.getInstance();
		String mdstring;
		try
		{
			mdstring = digest.md5("Hello".getBytes("gbk"));
			assertEquals(mdstring, "8b1a9953c4611296a827abf8c47804d7");
		} catch (UnsupportedEncodingException e)
		{
			assertTrue(false);
		}

	}
}
