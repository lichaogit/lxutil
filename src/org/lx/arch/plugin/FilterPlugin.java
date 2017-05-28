package org.lx.arch.plugin;

public interface FilterPlugin extends IPlugin
{
	public boolean isPass(String cmd, IActionContext context);
}
