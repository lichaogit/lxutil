package org.lx.crawler;

import org.htmlparser.tags.ScriptTag;

public class QuoteScriptTag extends ScriptTag
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5757419234675281968L;

	/**
	 * Create a new script tag.
	 */
	public QuoteScriptTag()
	{
		setThisScanner(new QuoteScriptScanner());
	}

}
