package org.lx.arch.plugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.DocumentException;
import org.jaxen.JaxenException;
import org.lx.arch.ConfigurationView;
import org.lx.arch.RESULT;
import org.lx.util.GeneralException;

@SuppressWarnings("rawtypes")
public abstract class AbstractCommandPlugin extends AbstractPlugin implements
		CommandPlugin
{

	final public static String RESULT_OUT_BUF = "$OUTBUF";

	final public static String PARAMS_CONTEXT = "$C";

	ConfigurationView m_commandsView = null;

	protected String getParameter(Map param, String attrname)
	{
		Object obj = param.get(attrname);
		return obj == null ? null : String.valueOf(obj);
	}

	protected Object getRawParameter(Map param, String attrname)
	{
		return param.get(attrname);
	}

	protected static void setReturnResult(Map param, Object val)
	{
		param.put(RESULT_OUT_BUF, val);
	}

	public static Object getReturnResult(Map param)
	{
		return param.get(RESULT_OUT_BUF);
	}

	protected static void setContext(IActionContext context, Object val)
	{
		context.setParameter(PARAMS_CONTEXT, val);
	}

	protected static Object getContext(IActionContext context)
	{
		return context.getParameter(PARAMS_CONTEXT);
	}

	protected void setParameter(Map param, String attrname, Object val)
	{
		param.put(attrname, val);
	}

	protected ConfigurationView getCommandsView()
	{
		if (m_commandsView == null)
		{
			m_commandsView = getDefinitionView().getSubView("commands");
		}
		return m_commandsView;
	}

	protected Map buildParameterMap(String command, String cmdParams)
			throws IOException
	{
		Map retval = null;
		DefaultPluginManager pluginManager = (DefaultPluginManager) getPluginManager();
		AbstractCommandPlugin plug = pluginManager.getPlugin(command);
		try
		{
			retval = pluginManager.buildParameterMap(plug, command, cmdParams);
		} catch (JaxenException e)
		{
			throw new IOException(e);
		} catch (DocumentException e)
		{
			throw new IOException(e);
		} catch (IOException e)
		{
			throw new IOException(e);
		}
		return retval;
	}

	public abstract RESULT onGlobalCommand(String cmd, IActionContext context)
			throws GeneralException;

	@Override
	final public RESULT onCommand(String cmd, IActionContext context)
			throws GeneralException
	{
		return onGlobalCommand(cmd, context);
	}

	@Override
	public void init(Object params) throws GeneralException
	{
		super.init(params);
		// ConfigurationView commandsView = getCommandsView();
		// build the actions Map
		// initPlugin(commandsView);
	}
}
