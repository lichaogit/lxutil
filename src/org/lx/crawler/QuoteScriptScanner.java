package org.lx.crawler;

import org.htmlparser.scanners.ScriptScanner;

public class QuoteScriptScanner extends ScriptScanner
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3459215452344732987L;

	public QuoteScriptScanner()
	{
		// do not use the strict mode.
		STRICT = false;
	}
}
