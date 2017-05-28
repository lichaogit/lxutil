package org.lx.arch.logic;

import java.util.Iterator;

import org.lx.util.IMatcher;

public class NotMatcher extends ContainerMatcher
{
	public void addChild(IMatcher matcher) throws ConditionParserException
	{
		if (this.childs.size() >= 1)
		{
			throw new ConditionParserException("");
		}
		super.addChild(matcher);
	}

	public boolean isMatch(Object o)
	{
		boolean retval = false;
		do
		{
			if (this.childs == null || this.childs.size() != 1)
			{
				break;
			}
			Iterator it = this.childs.iterator();
			IMatcher matcher = (IMatcher) it.next();
			retval = !matcher.isMatch(o);
		} while (false);
		return retval;
	}

}
