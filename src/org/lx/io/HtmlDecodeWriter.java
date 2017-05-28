package org.lx.io;

import java.io.IOException;
import java.io.Writer;

public class HtmlDecodeWriter extends BufferedFilterWriter
{

	public HtmlDecodeWriter(Writer w)
	{
		super(w, 6);
	}

	protected boolean isStartBufFlag(int c)
	{
		boolean retval = false;
		if (c == '&')
		{
			if (size() > 0)
			{
				try
				{
					flush();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			retval = true;
		}
		return retval;
	}

	protected boolean isEndBufFlag(int c)
	{
		return c == ';';
	}

	public static String htmlDecode(String str)
	{
		String retval = null;
		if ("&lt;".equals(str))
		{
			retval = "<";
		} else if ("&gt;".equals(str))
		{
			retval = ">";
		} else if ("&amp;".equals(str))
		{
			retval = "&";
		} else if ("&quot;".equals(str))
		{
			retval = "\"";
		} else if ("&#39;".equals(str))
		{
			retval = "\'";
		}

		return retval;
	}

	protected String onFilterBuffer(String str) throws IOException
	{
		String retval = htmlDecode(str);
		if (retval == null)
		{
			retval = str;
		}
		return retval;

	}
}
