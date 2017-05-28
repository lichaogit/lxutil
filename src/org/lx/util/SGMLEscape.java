package org.lx.util;

//URL:http://www.cnblogs.com/anjou/archive/2007/03/15/676476.html
//URL:http://csharpcomputing.com/XMLTutorial/Lesson2.htm
public class SGMLEscape
{

	final char[] c;

	final String[] expansion;

	public SGMLEscape(char[] raw, String[] expansion)
	{
		this.c = raw;
		this.expansion = expansion;
	}

	/**
	 * Quote special XML characters '<', '>', '&', '"' if necessary, and write
	 * to character stream. We write to a character stream rather than simply
	 * returning a stream to avoid creating unneccessary objects.
	 */
	public String encode(String s)
	{
		StringBuffer st = new StringBuffer(s.length() * 2);
		boolean copy = false;
		for (int i = 0; i < s.length(); i++)
		{
			copy = true;
			char ch = s.charAt(i);
			for (int j = 0; j < c.length; j++)
			{
				if (c[j] == ch)
				{
					st.append(expansion[j]);
					copy = false;
					break;
				}
			}
			if (copy)
			{
				st.append(ch);
			}
		}
		return st.toString();
	}

	public String decode(String s)
	{
		String mine = new String(s);
		for (int i = 0; i < c.length; i++)
		{
			mine.replaceAll(expansion[i], new String(c[i] + ""));
		}
		return mine;
	}
}
