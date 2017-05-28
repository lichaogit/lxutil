package org.lx.util;

public class XMLEscape extends SGMLEscape
{
	final static char c[] = { '<', '>', '&', '\"', '\'' };

	final static String expansion[] = { "&lt;", "&gt;", "&amp;", "&quot;",
			"&apos;" };

	public XMLEscape()
	{
		super(c, expansion);
	}
}
