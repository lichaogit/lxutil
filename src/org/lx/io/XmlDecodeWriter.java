package org.lx.io;

import java.io.IOException;
import java.io.Writer;

public class XmlDecodeWriter extends BufferedFilterWriter
{

	final static String m_charset = "ISO-8859-1";

	public XmlDecodeWriter(Writer w)
	{
		super(w, 3);
	}

	/* do not buffered as default */
	protected boolean isStartBufFlag(int c)
	{
		return '%' == c;
	}

	protected boolean isEndBufFlag(int c)
	{
		return false;
	}

	protected String onFilterBuffer(String str) throws IOException
	{
		String retval = str;
		if (str.charAt(0) == '%' && str.length() == 3)
		{
			byte b = (byte) Integer.parseInt(str.substring(1),16);
			retval = new String(new byte[] { b }, m_charset);
		}
		return retval;
	}

}
