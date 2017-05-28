package org.lx.arch.plugin;

import org.lx.arch.RESULT;
import org.lx.util.GeneralException;

public interface PluginManager
{
	public RESULT dispatchCommand(String cmd, IActionContext context)
			throws GeneralException;

}
