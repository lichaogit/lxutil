package org.lx.arch.plugin;

import org.lx.arch.RESULT;
import org.lx.util.GeneralException;

public interface CommandPlugin extends IPlugin
{
	public RESULT onCommand(String cmd, IActionContext context)
			throws GeneralException;

}
