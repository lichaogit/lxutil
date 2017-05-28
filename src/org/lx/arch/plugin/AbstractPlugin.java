package org.lx.arch.plugin;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lx.arch.ConfigurationView;
import org.lx.util.ClassManager;
import org.lx.util.GeneralException;

public abstract class AbstractPlugin implements IPlugin
{
	protected static Log m_log = LogFactory.getLog(AbstractPlugin.class);

	private PluginManager m_pluginManager = null;

	private ConfigurationView m_definitionView = null;

	private ConfigurationView m_confView = null;

	private ConfigurationView m_runtimeView = null;

	protected ClassManager m_cm = null;

	// the parameters passed to the plugin when init.
	final public static String CONFVIEW = "a";

	final public static String RUNTIMEVIEW = "b";

	final public static String PLUGMANAGER = "c";

	final public static String DEFINITION_VIEW = "d";

	final public static String CLASSLOADER = "e";

	public void init(Object params) throws GeneralException
	{
		Map paramsMap = null;
		if (params instanceof Map)
		{
			paramsMap = (Map) params;
		}
		// from external config
		m_confView = (ConfigurationView) paramsMap.get(CONFVIEW);
		m_runtimeView = (ConfigurationView) paramsMap.get(RUNTIMEVIEW);
		m_pluginManager = (PluginManager) paramsMap.get(PLUGMANAGER);
		// front internal config.
		m_definitionView = (ConfigurationView) paramsMap.get(DEFINITION_VIEW);
		m_cm = (ClassManager) paramsMap.get(CLASSLOADER);
	}

	public PluginManager getPluginManager()
	{
		return m_pluginManager;
	}

	protected ConfigurationView getConfView()
	{
		return m_confView;
	}

	protected ConfigurationView getRuntimeView()
	{
		return m_runtimeView;
	}

	protected ConfigurationView getDefinitionView()
	{
		return m_definitionView;
	}

	public void start() throws GeneralException
	{

	}

	public void stop() throws GeneralException
	{

	}

	@Override
	public boolean isReady()
	{
		// ready by default.
		return true;
	}

}
