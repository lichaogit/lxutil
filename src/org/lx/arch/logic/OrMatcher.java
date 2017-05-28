package org.lx.arch.logic;

import java.util.Iterator;

import org.lx.util.IMatcher;

public class OrMatcher extends ContainerMatcher
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
			while (it.hasNext())
			{
				matcher = (IMatcher) it.next();
				if (matcher.isMatch(o))
				{
					retval = true;
					break;
				}
			}
		} while (false);
		return retval;
	}
}
