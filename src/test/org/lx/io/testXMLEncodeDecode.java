package test.org.lx.io;

import java.io.CharArrayReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.lx.io.XmlDecodeWriter;
import org.lx.io.XmlEncodeReader;

public class testXMLEncodeDecode extends TestCase
{
	public static void main(String[] argv)
	{
		if (argv.length >= 2)
		{
			String file = argv[0];
			String file1 = argv[1];
			try
			{
				XmlEncodeReader xer = new XmlEncodeReader(new FileReader(file));
				FileWriter w = new FileWriter(file1);
				int c = 0;
				char buf[] = new char[2048];
				while ((c = xer.read(buf, 0, 2048)) != -1)
				{
					w.write(buf, 0, c);
					w.flush();
				}
				w.close();
				xer.close();

			} catch (Exception e)
			{

			}
		}
	}

	public void testXMLDecodeWriter()
	{
		try
		{
			StringWriter w = new StringWriter();
			XmlDecodeWriter xdw = new XmlDecodeWriter(w);
			xdw.write("%00%08%11%1f%0b%0c%0e");
			xdw.flush();
			char c[] = new char[] { 0, 0x08, 0x11, 0x1f, 0x0b, 0x0c, 0x0e };
			assertEquals(new String(c), w.getBuffer().toString());

			w = new StringWriter();
			xdw = new XmlDecodeWriter(w);
			xdw.write("<xml>%0e</xml>");
			xdw.flush();
			char b = 0x0e;
			assertEquals("<xml>" + b + "</xml>", w.getBuffer().toString());

		} catch (IOException e)
		{
			assertTrue(false);
		}
	}

	public void testXmlEncodeReader()
	{
		try
		{
			char buf[] = null;
			StringBuffer sb = null;
			Reader r = null;
			XmlEncodeReader xer = null;
			int c = 0;

			buf = new char[] { 0, 0x08, 0x11, 0x1f, 0x0b, 0x0c, 0x0e };
			sb = new StringBuffer();
			r = new CharArrayReader(buf);

			xer = new XmlEncodeReader(r);
			c = 0;
			while ((c = xer.read()) != -1)
			{
				sb.append((char) c);
			}
			assertEquals("%00%08%11%1f%0b%0c%0e", sb.toString());
			sb.delete(0, sb.length());

			String str_buf = "<xml>\u000e</xml>";
			sb = new StringBuffer();
			r = new StringReader(str_buf);

			xer = new XmlEncodeReader(r);
			c = 0;
			while ((c = xer.read()) != -1)
			{
				sb.append((char) c);
			}
			assertEquals("<xml>%0e</xml>", sb.toString());
			sb.delete(0, sb.length());

		} catch (IOException e)
		{
			assertTrue(false);
		}

	}
}
