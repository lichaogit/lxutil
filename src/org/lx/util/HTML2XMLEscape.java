package org.lx.util;

public class HTML2XMLEscape extends SGMLEscape
{
	final static char c[] = { '\'' };

	final static String expansion[] = { "&apos;" };

	public HTML2XMLEscape()
	{
		super(c, expansion);
	}

}
