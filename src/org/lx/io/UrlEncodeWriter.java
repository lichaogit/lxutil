package org.lx.io;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;

public class UrlEncodeWriter extends FilterWriter
{

	public UrlEncodeWriter(Writer w)
	{
		super(w);
	}

	public void write(String str) throws IOException
	{
		write(URLEncoder.encode(str));
	}

	public void write(String str, int off, int len) throws IOException
	{
		write(str.toCharArray(), off, len);
	}

	public void write(char[] cbuf, int off, int len) throws IOException
	{
		write(new String(cbuf, off, len));
	}

}
