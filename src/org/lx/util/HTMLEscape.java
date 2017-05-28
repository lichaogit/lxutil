package org.lx.util;

public class HTMLEscape extends SGMLEscape
{
	final static char c[] = { '<', '>', '&', '\"', ' ',  '\u00a9',
			'\u00ae' };

	final static String expansion[] = { "&lt;", "&gt;", "&amp;", "&quot;",
			"&nbsp;", "&copy;", "&reg;" };

	public HTMLEscape()
	{
		super(c, expansion);
	}
}
