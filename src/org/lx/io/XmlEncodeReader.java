package org.lx.io;

import java.io.IOException;
import java.io.Reader;

import org.lx.util.StringEx;

public class XmlEncodeReader extends BufferedFilterReader
{

	public XmlEncodeReader(Reader r)
	{
		super(r, 1);
	}

	public static boolean isValidXmlChar(int ch)
	{
		boolean retval = true;
		if ((ch >= 0 && ch <= 0x08) || (ch >= 0x10 && ch <= 0x1f)
				|| (ch == 0x0b || ch == 0x0c || ch == 0x0e || ch == 0x0f))
		{
			retval = false;
		}
		return retval;
	}

	protected boolean isStartBufFlag(int ch)
	{
		return !isValidXmlChar(ch);
	}

	protected boolean isEndBufFlag(int c)
	{
		return false;
	}

	public static String xmlDecode(String str)
	{
		String retval = str;
		int ch = str.charAt(0);

		if (!isValidXmlChar(ch))
		{
			retval = '%' + StringEx.byteToHex((byte) ch);
		}
		return retval;
	}

	protected String onFilterBuffer(String buf) throws IOException
	{
		return xmlDecode(buf);
	}

}
