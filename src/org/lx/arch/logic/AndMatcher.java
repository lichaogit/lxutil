package org.lx.arch.logic;

import java.util.Iterator;

import org.lx.util.IMatcher;

public class AndMatcher extends ContainerMatcher
{

	public boolean isMatch(Object o)
	{
		boolean retval = false;
		do
		{
			if (this.childs == null || this.childs.size() == 0)
			{
				break;
			}
			Iterator it = this.childs.iterator();
			IMatcher matcher = null;
			retval = true;
			while (it.hasNext())
			{
				matcher = (IMatcher) it.next();
				if (!matcher.isMatch(o))
				{
					retval = false;
					break;
				}
			}
		} while (false);
		return retval;
	}
}
