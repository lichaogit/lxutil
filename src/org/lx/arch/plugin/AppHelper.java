package org.lx.arch.plugin;

import java.util.HashMap;
import java.util.Map;

import org.lx.arch.RESULT;
import org.lx.arch.ServiceProvider;
import org.lx.arch.ThreadPool;
import org.lx.arch.ThreadPool.CommandFunction;
import org.lx.util.GeneralException;

public class AppHelper
{
	static PluginManager m_pm = null;

	public static String CMD_AT_EXEC = "at.ExecCmd";

	public static String CMD_ASYNEXEC = "asynExec";

	public static RESULT exec(String cmd, IActionContext context)
			throws GeneralException
	{
		if (m_pm == null)
		{
			ServiceProvider sp = ServiceProvider.getInstance();
			m_pm = (PluginManager) sp.getService(PluginManager.class.getName());
		}
		return m_pm.dispatchCommand(cmd, context);
	}

	/**
	 * the return object is used to be waited because the real execuation will
	 * be finished asynchronized.
	 * @param function
	 * @param cmd
	 * @param timeout
	 * @param cf
	 * @return
	 * @throws Exception
	 */
	public static RESULT asynExec(ThreadPool.WorkFunction function, Object cmd,
			int timeout, CommandFunction cf, int priority)
			throws GeneralException
	{
		Map params = new HashMap();
		params.put("function", function);
		params.put("command", cmd);
		params.put("timeout", new Integer(timeout));
		params.put("callback", cf);
		if (priority > 0)
		{
			params.put("priority", new Integer(priority));
		}
		
		return exec(CMD_ASYNEXEC, new ActionContext(params));
	}

	public static RESULT asynExec(ThreadPool.WorkFunction function, Object cmd,
			int timeout, CommandFunction cf) throws GeneralException
	{
		// use the default priority.
		return asynExec(function, cmd, timeout, cf, -1);
	}

	public static RESULT atExec(String cronExpress, Object cmd, Map cmdParams)
			throws Exception
	{
		Map params = new HashMap();
		params.put("trigger", cronExpress);
		params.put("command", cmd);
		params.put("parameters", cmdParams);
		return exec(CMD_AT_EXEC, new ActionContext(params));
	}

	public static class PluginCmdFunction implements ThreadPool.WorkFunction
	{
		/**
		 * if the command have not been dispatch to work thread in specific
		 * time, then timeout will be trigged.
		 * @param cmd
		 */
		public void timeout(Object cmd)
		{
			if (cmd instanceof PluginCmd)
			{
			}
		}

		/**
		 * process the socket until exit£¬ the error code should be returned in
		 * this method.
		 */
		public RESULT process(Object cmd) throws GeneralException
		{
			RESULT retval = RESULT.RESULT_FAIL;
			try
			{
				PluginCmd pc = (PluginCmd) cmd;
				String cmdString = pc.cmd;
				Map cmdParams = null;
				Object obj = pc.params;
				if (obj instanceof Map)
				{
					cmdParams = (Map) obj;
				} else if (obj != null)
				{
					// convert the string to Map.
					DefaultPluginManager pluginManager = (DefaultPluginManager) m_pm;
					AbstractCommandPlugin plug = pluginManager
							.getPlugin(cmdString);
					cmdParams = pluginManager.buildParameterMap(plug,
							cmdString, obj.toString());
				}

				retval = AppHelper
						.exec(cmdString, new ActionContext(cmdParams));
			} catch (Exception e)
			{
				throw new GeneralException(e);
			}
			return retval;
		}
	}

	public static class PluginCmd
	{
		protected String cmd;

		protected Object params;

	}

	static PluginCmdFunction m_PluginCmdFunction = new PluginCmdFunction();

	public static RESULT asynExecPluginCmd(String cmd, Object params,
			int timeout, CommandFunction cf) throws GeneralException
	{
		PluginCmd pluginCmd = new PluginCmd();
		pluginCmd.cmd = cmd;
		pluginCmd.params = params;
		return asynExec(m_PluginCmdFunction, pluginCmd, timeout, cf);
	}
}
