package org.lx.util;

import java.util.*;

/**
 * <p>Title: BSMAgent</p> <p>Description: provide the BSM data to Database
 * Server and accept the Database Server's command</p> <p>Copyright: Copyright
 * (c) 2003</p> <p>Company: Shanghai Bell Samsung Mobile Communications
 * Co.,Ltd</p>
 * @author unascribed
 * @version 1.0
 */

public class StackEx extends Stack
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8587463174283240141L;

	// member variable
	private char m_Sep;

	// member method
	public StackEx()
	{
		m_Sep = '.';
	}

	public StackEx(char c)
	{
		m_Sep = c;
	}

	public String getPathString()
	{
		StringBuffer buffer = new StringBuffer();
		Enumeration e = this.elements();
		// get first element
		if (e.hasMoreElements())
		{
			buffer.append(e.nextElement());
		}

		while (e.hasMoreElements())
		{
			buffer.append(m_Sep + (String) e.nextElement());
		}
		return buffer.toString();
	}

	public String getPathString(int iBeginIndex, int iEndIndex)
	{
		StringBuffer buffer = new StringBuffer();
		Enumeration e = this.elements();
		for (int i = 0; e.hasMoreElements(); i++)
		{
			if (i <= iEndIndex && i >= iBeginIndex)
			{
				if (i == iBeginIndex)
				{
					buffer.append((String) e.nextElement());
				} else
				{
					buffer.append(m_Sep + (String) e.nextElement());
				}
			} else
			{
				e.nextElement();

			}
		}
		return buffer.toString();

	}
}