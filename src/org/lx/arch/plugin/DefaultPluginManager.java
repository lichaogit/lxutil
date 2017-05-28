package org.lx.arch.plugin;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.jaxen.JaxenException;
import org.lx.arch.Configuration;
import org.lx.arch.ConfigurationView;
import org.lx.arch.Dom4jConfiguration;
import org.lx.arch.Dom4jNodeConfiguration;
import org.lx.arch.JSONExchange;
import org.lx.arch.RESULT;
import org.lx.arch.StreamExchange;
import org.lx.util.Base64;
import org.lx.util.Cache;
import org.lx.util.ClassManager;
import org.lx.util.GeneralException;
import org.lx.util.JarUtil;
import org.lx.util.JsonUtil;
import org.lx.util.LogicUtil;
import org.lx.util.LogicUtil.Task;
import org.lx.util.ResourceLoaderManager;
import org.lx.util.StringEx;

/**
 * Load the plugin dynamically from specified location.
 * @author Administrator
 */
@SuppressWarnings("unchecked")
public class DefaultPluginManager implements PluginManager
{
	public static String CMD_SEQ = "\1";

	// {{plugin parameters.
	// command->Plugin
	Map<String, AbstractPlugin> m_commandMap = new ConcurrentHashMap<String, AbstractPlugin>();

	// FilterPlugin
	Map<String, AbstractFilterPlugin> m_filterPlugins = new ConcurrentHashMap<String, AbstractFilterPlugin>();

	protected List<String> _includeFiles = new ArrayList<String>();

	// fullId->IAction
	protected Map<String, IAction> _actionsMap = new ConcurrentHashMap<String, IAction>();

	protected Map<String, List> _cmdFilterParams = new ConcurrentHashMap<String, List>();

	protected Map<String, List<Element>> _cmdParams = new ConcurrentHashMap<String, List<Element>>();

	// save the plugin's default value info.
	protected Map<String, Map> _defaultValueParam = new ConcurrentHashMap<String, Map>();

	// save the mandatory plugin's params
	protected Map<String, List> _mandatoryParam = new ConcurrentHashMap<String, List>();

	// PluginLoader
	protected List<PluginLoader> m_loaders = new ArrayList<PluginLoader>();

	// PluginWrapper
	protected List<PluginWrapper> m_plugins = new ArrayList<PluginWrapper>();

	// }}

	public final static String PLUG_INTERFACES = "Interfaces";

	public final static String PLUG_MAINCLASS = "Main-Class";

	public final static String PLUG_ID = "Id";

	public final static String PARAMETERS_INFO = "paramters-info";

	protected static Log m_log = LogFactory.getLog(DefaultPluginManager.class);

	public static String PARAM_TYPE_BASE64 = "cmd.base64";

	public static String PARAM_TYPE_JSON = "cmd.json";

	protected StreamExchange m_jsonexchange = new JSONExchange();

	protected StreamExchange m_xmlexchange = null;

	protected ConfigurationView m_confView = null;

	ConfigurationView m_runtimeView = null;

	public static class PluginWrapper
	{
		public long timestamp;

		public Object source;

		public Object plugin;

		public String[] interfaces;

		public String id;

		public String parameters_info;

		public ClassManager cm;

		// cache the plugin info.
		protected Cache params_cache = null;

		public PluginWrapper(Class mainClz)
		{
			params_cache = new WrapperParametersCache(mainClz);
			plugin = mainClz;
		}

		public ConfigurationView getConfigView() throws IOException
		{
			return (ConfigurationView) params_cache.get(parameters_info);
		}

		public String[] getDepends() throws IOException
		{
			ConfigurationView defConf = getConfigView();
			String val = defConf.getAttribute("depends");
			return val == null ? null : val.split(",");
		}
	}

	public static class WrapperParametersCache extends Cache
	{
		Class clz;

		public WrapperParametersCache(Class clz)
		{
			this.clz = clz;
		}

		/**
		 * load the plugin wrapper info.
		 * @param key
		 * @return
		 */
		protected Object load(Object key) throws IOException
		{
			Class loadClz = clz == null ? WrapperParametersCache.class : clz;
			InputStream params_info_in = loadClz.getResourceAsStream(key
					.toString());
			Dom4jConfiguration conf = new Dom4jConfiguration(params_info_in);
			/* return the plugin root view */
			return new ConfigurationView(conf, "/config");
		}
	}

	public static interface PluginLoader
	{
		public List getPlugins(Comparable condition);
	}

	protected IAction findAction(String cmd, IActionContext context)
	{
		IAction retval = null;
		do
		{
			StringBuffer id = new StringBuffer();
			id.append(cmd);

			if (_cmdFilterParams.containsKey(cmd))
			{
				List<String> cmdFilterParams = _cmdFilterParams.get(cmd);
				Map params = context.getParameters();

				for (String cmdParam : cmdFilterParams)
				{
					Object val = JsonUtil.parseJpath(params, cmdParam, false);
					if (val == null)
					{
						continue;
					}
					String strVal = val.toString();
					if (strVal.length() == 0)
					{
						continue;
					}
					id.append('$');
					id.append(cmdParam);
					id.append('$');
					id.append(strVal);
				}
			}

			String fullId = id.toString();
			while (retval == null)
			{
				retval = (IAction) _actionsMap.get(fullId);
				if (retval != null)
				{
					break;
				}
				// shrink the id.
				int offset = fullId.lastIndexOf("$");
				if (offset == -1)
				{
					break;
				}
				fullId = fullId.substring(0, offset);
				offset = fullId.lastIndexOf("$");
				if (offset == -1)
				{
					break;
				}
				fullId = fullId.substring(0, offset);
			}
		} while (false);
		return retval;
	}

	public AbstractCommandPlugin getPlugin(String cmd)
	{
		Object obj = m_commandMap.get(cmd);
		return obj == null ? null : (AbstractCommandPlugin) obj;
	}

	protected boolean checkParameters(AbstractCommandPlugin plugin, String cmd,
			Map params)
	{
		boolean retval = true;
		// the mandatory parameter must be present.
		List mandatoryParams = (List) _mandatoryParam.get(cmd);
		if (mandatoryParams != null && mandatoryParams.size() > 0)
		{
			retval = params.keySet().containsAll(mandatoryParams);
		}
		return retval;
	}

	public Map buildParameterMap(AbstractCommandPlugin plug, String cmdOption,
			String cmdParameter) throws JaxenException, DocumentException,
			IOException
	{
		Map retval = null;

		do
		{
			if (cmdParameter == null)
			{
				break;// no data
			}

			// String argv[] = StringEx.parseCmdLineParameters(cmdParameter);
			String argv[] = StringEx.split(cmdParameter, CMD_SEQ);
			if (argv == null || argv.length == 0)
			{
				break;
			}

			String format = argv[0];

			if (PARAM_TYPE_JSON.equals(format))
			{
				// handle the json format command
				// format:cmd.json len data.
				String jsonData = argv[1];
				retval = (Map) m_jsonexchange.fromString(jsonData, Map.class);
				break;
			}

			String cs = "ISO-8859-1";
			String[] params = argv;
			if (PARAM_TYPE_BASE64.equals(format))
			{
				// some commands is from end-user, and some commands is
				// from
				// manager. the end-user cpmmands need the BASE 64
				// encoding.
				String parameters_b64 = null;
				if (argv.length > 2)
				{
					cs = argv[1];
					parameters_b64 = argv[2];
				}

				// recreate the cmdData
				cmdParameter = Base64.decodeString(parameters_b64, cs);
				// params = StringEx.parseCmdLineParameters(cmdParameter);
				params = StringEx.split(cmdParameter, CMD_SEQ);
			}
			retval = buildParameterMap(plug, cmdOption, cs, params);
		} while (false);

		return retval;
	}

	/**
	 * @param cmdOption
	 * @param params
	 * @param result
	 * @return return null means the command is unknown.
	 */
	public RESULT dispatchCommand(String cmdOption, IActionContext context,
			Object outCtx) throws GeneralException
	{

		if (outCtx != null && context != null)
		{
			AbstractCommandPlugin.setContext(context, outCtx);
		}

		return dispatchCommand(cmdOption, context);
	}

	/**
	 * dispatch the command to the specific plugin, return null if parameters
	 * don't match.
	 */
	public RESULT dispatchCommand(String cmd, IActionContext context)
			throws GeneralException
	{
		RESULT retval = RESULT.RESULT_FAIL;
		do
		{
			// add filter

			if (!isPass(cmd, context))
			{
				retval = RESULT.RESULT_FILTER_BLOCKED;
				break;
			}

			AbstractCommandPlugin plug = getPlugin(cmd);
			if (plug == null)
			{
				retval = RESULT.RESULT_NULL_PLUGIN;
				break;
			}
			Map newParams = new HashMap();
			// 1.set the default value;
			AbstractCommandPlugin cmdPlug = ((AbstractCommandPlugin) plug);
			Map defParams = _defaultValueParam.get(cmd);

			if (defParams != null)
			{
				newParams.putAll(defParams);
			}

			// 2.overwrite the value with the given values.
			Map inputParams = context.getParameters();
			if (inputParams != null)
			{
				newParams.putAll(inputParams);
			}
			IActionContext newContext = new ActionContext(newParams);

			// 3.check the parameter.
			if (checkParameters(plug, cmd, newParams))
			{
				if (!plug.isReady())
				{
					// setReturnResult(result,
					// _cmdUtils.buildWaitNextRound(LogicUtil.random(3, 10)));
					retval = RESULT.RESULT_FAIL;
					m_log.warn("Exception:Plugin is initializing, just wait");
					break;
				}

				IAction action = findAction(cmd, context);
				if (action == null)
				{
					retval = plug.onCommand(cmd, newContext);
				} else
				{
					retval = action.exec(cmd, newContext);
				}

				// pass the reuslt.
				context.setReturnResult(newContext.getReturnResult());
			} else
			{
				retval = RESULT.RESULT_INVALID_PARAMETER;
			}
		} while (false);
		return retval;
	}

	protected Collection queryPlugin(String socketName, Comparable condition)
			throws GeneralException
	{
		load(condition);
		return query(socketName);
	}

	protected void init(ConfigurationView confView,
			ConfigurationView runtimeView)
	{
		m_confView = confView;
		m_runtimeView = runtimeView;
	}

	// handle the filter plugin.
	protected boolean isPass(String cmd, IActionContext context)
	{
		boolean retval = true;
		for (Entry<String, AbstractFilterPlugin> e : m_filterPlugins.entrySet())
		{
			FilterPlugin plug = e.getValue();
			if (!plug.isPass(cmd, context))
			{
				retval = false;
				break;
			}
		}
		return retval;
	}

	protected void initFilterPlugin(String socketName, Comparable condition)
			throws GeneralException
	{
		initPlugin(socketName, condition, new FilterPluginCallBack());
	}

	protected void initCommandPlugin(String socketName, Comparable condition)
			throws GeneralException
	{
		initPlugin(socketName, condition, new CommandPluginCallBack());
	}

	public interface CallBack
	{
		public void onEvent(IPlugin plugin, Object ctx);
	}

	class FilterPluginCallBack implements CallBack
	{
		public void onEvent(IPlugin plugin, Object ctx)
		{
			// init the filter plugin.
			AbstractFilterPlugin plug = (AbstractFilterPlugin) plugin;
			m_filterPlugins.put(plug.getClass().getName(), plug);
		}
	}

	class CommandPluginCallBack implements CallBack
	{
		public void onEvent(IPlugin plugin, Object ctx)
		{
			// init the command plugin.
			AbstractCommandPlugin plug = (AbstractCommandPlugin) plugin;

			List<Element> commands = (List) ctx;
			for (Element cmd : commands)
			{
				Attribute attr = cmd.attribute("name");
				m_commandMap.put(attr.getText(), plug);
			}
		}
	}

	public class InitPluginTask implements LogicUtil.Task
	{
		PluginWrapper wrapper;

		CallBack cb;

		ConfigurationView pluginRootView;

		ConfigurationView pluginRuntimeRootView;

		Map pluginsTask;

		public InitPluginTask(PluginWrapper wrapper, CallBack cb,
				ConfigurationView pluginRootView,
				ConfigurationView pluginRuntimeRootView, Map pluginsTask)
		{
			this.wrapper = wrapper;
			this.cb = cb;
			this.pluginRootView = pluginRootView;
			this.pluginRuntimeRootView = pluginRuntimeRootView;
			this.pluginsTask = pluginsTask;
		}

		public Task[] getDepends() throws Exception
		{
			Task[] retval = null;
			String[] depends = wrapper.getDepends();
			if (depends != null && depends.length > 0)
			{
				ArrayList al = new ArrayList();
				Task dependTask = null;
				for (int i = 0; i < depends.length; i++)
				{
					dependTask = (Task) pluginsTask.get(depends[i]);
					if (dependTask == null)
					{
						throw new IOException("'" + wrapper.id
								+ "':Dependent plugin '" + depends[i]
								+ "' is missing!");
					}
					al.add(dependTask);
				}
				retval = (Task[]) al.toArray(new Task[0]);
			}
			return retval;
		}

		public void doTask() throws Exception
		{
			try
			{
				initPlugin(wrapper, pluginsTask, cb, pluginRootView,
						pluginRuntimeRootView);
			} catch (Throwable e)
			{
				m_log.warn(e.getLocalizedMessage(), e);
				throw new Exception(e);
			}
		}

		public boolean isMandatory()
		{
			ConfigurationView pluginCfgView = pluginRootView
					.getSubView(wrapper.id);
			String mandatory = pluginCfgView.getAttribute("mandatory");
			return mandatory == null ? false : Boolean.parseBoolean(mandatory);
		}

		protected void addGroup(Map context, String key, Object val)
		{
			if (context.size() == 0)
			{
				context.put(key, new ArrayList());
			}
			List groups = (List) context.get(key);
			groups.add(val);
		}

		// load
		protected void loadPluginClass(AbstractPlugin plug,
				ConfigurationView commandsView, ClassManager cm, List commands)
		{
			do
			{
				if (commandsView == null)
				{
					break;
				}

				// 1.load from depends commands configuration.
				String includesStr = commandsView.getAttribute("includes");
				if (includesStr != null)
				{
					String[] files = includesStr.split(";");
					ResourceLoaderManager rlm = new ResourceLoaderManager();
					rlm.addLoader(new ResourceLoaderManager.JarLoader("/"));

					for (String file : files)
					{
						try
						{
							InputStream in = rlm.load(file);
							if (in == null)
							{
								m_log.warn("Can not include actions file:"
										+ file);
								continue;
							}
							if (_includeFiles.contains(file))
							{
								m_log.warn("ref:" + file);
								continue;
							}
							m_log.warn("include:" + file);
							_includeFiles.add(file);
							Configuration cfg = new Dom4jConfiguration(in);
							ConfigurationView includeCmdView = new ConfigurationView(
									cfg, "/config/commands");
							loadPluginClass(plug, includeCmdView, cm, commands);

						} catch (IOException e)
						{
							m_log.warn(e.getLocalizedMessage(), e);
						}
					}
				}

				List<Element> cmds = commandsView.queryAll("command");
				if (cmds == null || cmds.size() == 0)
				{
					break;
				}

				commands.addAll(cmds);

				// 1.build a actionMap for each action
				for (Element cmd : cmds)
				{
					ConfigurationView cmdView = new ConfigurationView(
							new Dom4jNodeConfiguration(cmd), null);

					// 1.init the mandatory parameters.
					// the property that have no 'defaultValue' or the
					// mandatory=true
					String xpath = "parameter[not(@defaultValue) and (not(@mandatory) or @mandatory='true')]/@name";
					List mandatory = cmdView.queryAll(xpath,
							ConfigurationView.STRING_TRANSFORM);

					String cmdName = cmd.attributeValue("name");
					if (mandatory != null && mandatory.size() > 0)
					{
						_mandatoryParam.put(cmdName, mandatory);
					}

					List params = cmdView.queryAll("parameter");
					if (params != null && params.size() > 0)
					{
						_cmdParams.put(cmdName, params);
					}

					// 2.init the default value.
					Map defParams = cmdView.getNameValuePaired("parameter",
							"name", "defaultValue");
					if (defParams != null && defParams.size() > 0)
					{
						_defaultValueParam.put(cmdName, defParams);
					}

					// 3.init action instance
					List<Element> cmdActions = cmdView
							.queryAll("actions/action");
					if (cmdActions == null || cmdActions.size() == 0)
					{
						m_log.warn(cmdName + "->" + plug.getClass().getName());
						continue;
					}

					for (Element actEle : cmdActions)
					{
						StringBuffer id = new StringBuffer();
						String filterInfo = actEle.attributeValue("params");
						String className = actEle.attributeValue("class");
						if (className == null || className.length() == 0)
						{
							continue;
						}
						id.append(cmdName);

						if (filterInfo != null && filterInfo.length() > 0)
						{
							id.append('$');
							id.append(filterInfo);
						}

						Class actionClass = null;
						try
						{
							actionClass = (Class) cm.get(className);
						} catch (IOException e)
						{
							m_log.warn(e.getLocalizedMessage(), e);
						}

						if (actionClass == null)
						{
							continue;
						}

						// build the filter params info.
						if (filterInfo != null)
						{
							String[] paramsArray = filterInfo.split("\\$");
							List filterParams = (List) _cmdFilterParams
									.get(cmdName);

							if (filterParams == null)
							{
								filterParams = new ArrayList();
								_cmdFilterParams.put(cmdName, filterParams);
							}

							for (int i = 0; i < paramsArray.length; i++)
							{
								if (i % 2 != 0)
								{
									// skip value
									continue;
								}
								if (filterParams.contains(paramsArray[i]))
								{
									// skip existed param
									continue;
								}
								filterParams.add(paramsArray[i]);
							}
						}
						// build action instance info
						try
						{
							_actionsMap.put(id.toString(),
									(IAction) actionClass.newInstance());
							m_log.warn(id.toString() + "->"
									+ actionClass.getName());
						} catch (InstantiationException e)
						{
							m_log.warn(e.getLocalizedMessage(), e);
						} catch (IllegalAccessException e)
						{
							m_log.warn(e.getLocalizedMessage(), e);
						}
					}
				}
			} while (false);
		}

		protected void initPlugin(AbstractPlugin plug,
				ConfigurationView commandsView, ClassManager cm)
		{
			ClassLoader curCl = plug.getClass().getClassLoader();
			cm.setClassLoader(curCl);
			List<Element> commands = new ArrayList();
			// 1.load from current configuration.
			loadPluginClass(plug, commandsView, cm, commands);

			// 2.notify the plugin load class finish.
			if (cb != null)
			{
				cb.onEvent(plug, commands);
			}
		}

		protected void initPlugin(PluginWrapper wrapper, Map plugins,
				CallBack cb, ConfigurationView pluginRootView,
				ConfigurationView pluginRuntimeRootView)
				throws InstantiationException, IllegalAccessException,
				GeneralException, IOException
		{

			ConfigurationView pluginCfgView = pluginRootView
					.getSubView(wrapper.id);
			ConfigurationView pluginRuntimeView = pluginRuntimeRootView
					.getSubView(wrapper.id);
			if (pluginRuntimeView == null)
			{
				pluginRuntimeRootView.createNode(wrapper.id);
				pluginRuntimeView = pluginRuntimeRootView
						.getSubView(wrapper.id);
			}

			HashMap params = new HashMap();
			Class cls = (Class) wrapper.plugin;
			AbstractPlugin plug = (AbstractPlugin) cls.newInstance();

			// add the db config to pluginCfg.
			// use the getConfig to get the config from db.
			Map configParam = new HashMap();
			configParam.put("name", "config-plugin-" + wrapper.id);
			configParam.put("xpath", "/");
			RESULT result = dispatchCommand("getConfig", new ActionContext(
					configParam));
			if (result.getCode() == 0)
			{
				// success.
				Object obj = AbstractCommandPlugin.getReturnResult(configParam);

				String val = obj2String(obj);
				if (val != null && val.length() > 0)
				{
					StringReader r = new StringReader(val);
					Dom4jConfiguration dbConf = new Dom4jConfiguration(r, null);
					ConfigurationView dbConfigView = new ConfigurationView(
							dbConf, null);
					dbConfigView.setParent(pluginCfgView);
					pluginCfgView = dbConfigView;
				}
			}

			// do the init.
			if (wrapper.interfaces != null)
			{
				// build the init parameter for plugin
				params.clear();

				if (pluginCfgView != null)
				{
					params.put(AbstractPlugin.CONFVIEW, pluginCfgView);
				}
				params.put(AbstractPlugin.RUNTIMEVIEW, pluginRuntimeView);
				params.put(AbstractPlugin.PLUGMANAGER,
						DefaultPluginManager.this);

				// build the interfaces info by parse the parameter
				// info.

				ConfigurationView plugCommandsView = wrapper.getConfigView();

				params.put(AbstractPlugin.DEFINITION_VIEW, plugCommandsView);
				params.put(AbstractPlugin.CLASSLOADER, wrapper.cm);

				m_log.warn("Init plugin:" + wrapper.id + "->" + wrapper.plugin);

				ConfigurationView commandView = plugCommandsView
						.getSubView("commands");

				initPlugin(plug, commandView, wrapper.cm);
				plug.init(params);

				plug.start();
			}

		}
	}

	protected void initPlugin(String socketName, Comparable condition,
			CallBack cb) throws GeneralException
	{
		Collection plugins = queryPlugin(socketName, condition);
		if (plugins != null && plugins.size() > 0)
		{
			m_log.warn("Init socketName:" + socketName);

			ConfigurationView pluginRootView = m_confView;
			ConfigurationView pluginRuntimeRootView = m_runtimeView;

			PluginWrapper wrapper = null;

			Iterator it = plugins.iterator();
			Map pluginsMap = new HashMap();
			InitPluginTask task = null;
			while (it.hasNext())
			{
				wrapper = (PluginWrapper) it.next();
				task = new InitPluginTask(wrapper, cb, pluginRootView,
						pluginRuntimeRootView, pluginsMap);
				pluginsMap.put(wrapper.id, task);
			}

			try
			{
				LogicUtil.doDependsTask(pluginsMap);
			} catch (Exception e)
			{
				m_log.warn(e.getLocalizedMessage(), e);
			}
		}
	}

	public String obj2String(Object obj)
	{
		// convert
		String retval = null;
		if (obj != null)
		{
			if (obj instanceof List)
			{
				StringBuffer sb = new StringBuffer();
				List items = (List) obj;
				Iterator it = items.iterator();
				while (it.hasNext())
				{
					obj = it.next();
					if (obj instanceof Node)
					{
						sb.append(((Node) obj).asXML());
					} else
					{
						sb.append(obj.toString());
					}
				}
				retval = sb.toString();
			} else
			{
				retval = obj.toString();
			}
		}
		return retval;
	}

	protected void destoryPlugins()
	{
		HashSet set = new HashSet();
		set.addAll(m_commandMap.values());
		Iterator it = set.iterator();
		CommandPlugin plug = null;
		while (it.hasNext())
		{
			plug = (CommandPlugin) it.next();

			if (plug != null)
			{
				try
				{
					plug.stop();
					plug.destory();
				} catch (GeneralException e)
				{
					m_log.warn(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	public static class JarPluginLoader implements PluginLoader
	{
		String m_base;

		ClassManager m_classManager = new ClassManager();

		public JarPluginLoader(String base)
		{
			m_base = base;
		}

		public List getPlugins(Comparable condition)
		{
			List retval = null;
			do
			{

				File[] jarFiles = listJarFiles(m_base);
				if (jarFiles == null || jarFiles.length == 0)
				{
					break;
				}

				retval = new ArrayList();
				ResourceLoaderManager rlm = null;
				ResourceLoaderManager.Loader loader = null;
				int index = 0;
				InputStream in = null;
				Manifest manifest = null;
				for (; index < jarFiles.length; index++)
				{
					// load the
					rlm = new ResourceLoaderManager();
					if (jarFiles[index].isDirectory())
					{
						loader = new ResourceLoaderManager.FileLoader(
								jarFiles[index].getAbsolutePath());

					} else if (jarFiles[index].isFile())
					{
						try
						{
							loader = new ResourceLoaderManager.JarFileLoader(
									jarFiles[index].getAbsolutePath());
						} catch (Throwable e)
						{
							m_log.warn(e.getLocalizedMessage(), e);
						}
					}
					rlm.addLoader(loader);

					try
					{
						in = rlm.load(JarFile.MANIFEST_NAME);
						if (in == null)
						{
							continue;
						}
						do
						{
							manifest = new Manifest(in);

							if (condition != null
									&& condition.compareTo(manifest) != 0)
							{
								break;
							}

							String ifStr = JarUtil.getJarMainAttribute(
									manifest, PLUG_INTERFACES);
							String[] interfaces = null;
							if (ifStr != null)
							{
								interfaces = StringEx.split(ifStr, ",");
							}
							String idStr = JarUtil.getJarMainAttribute(
									manifest, PLUG_ID);

							ClassLoader cl = new URLClassLoader(
									new URL[] { jarFiles[index].toURI().toURL() },
									DefaultPluginManager.class.getClassLoader());

							String plugClsName = JarUtil.getJarMainAttribute(
									manifest, PLUG_MAINCLASS);
							Class mainClz = null;
							if (plugClsName != null)
							{
								// the Class.forName will initialize the static
								// var.
								m_classManager.setClassLoader(cl);
								mainClz = m_classManager.loadClass(plugClsName);
							}
							PluginWrapper plug = new PluginWrapper(mainClz);
							plug.timestamp = jarFiles[index].lastModified();
							plug.source = jarFiles[index];
							plug.interfaces = interfaces;
							plug.id = idStr;
							plug.parameters_info = JarUtil.getJarMainAttribute(
									manifest, PARAMETERS_INFO);
							plug.cm = m_classManager;
							retval.add(plug);
						} while (false);
						in.close();
					} catch (Throwable e)
					{
						m_log.warn(e.getLocalizedMessage(), e);
					}

				}
			} while (false);

			return retval;
		}

		/**
		 * get the file or directory that end with .jar
		 * @param strdir
		 * @return
		 * @throws IllegalArgumentException
		 */
		protected static File[] listJarFiles(String strdir)
				throws IllegalArgumentException
		{
			File dir = new File(strdir);

			// This filter only returns directories
			FileFilter fileFilter = new FileFilter() {
				public boolean accept(File file)
				{
					String fileName = file.getAbsolutePath();
					return fileName.length() > 4
							&& fileName.substring(fileName.length() - 4)
									.equalsIgnoreCase(".jar");
				}
			};
			// list all jar files
			return dir.listFiles(fileFilter);
		}
	}

	public synchronized void addLoader(PluginLoader loader)
	{
		m_loaders.add(loader);
	}

	public synchronized void removeLoader(PluginLoader loader)
	{
		m_loaders.remove(loader);
	}

	/**
	 * refresh the plugin info.
	 */
	public synchronized void load(Comparable condition)
	{
		for (PluginLoader loader : m_loaders)
		{
			List plugins = loader.getPlugins(condition);
			if (plugins == null)
			{
				continue;
			}
			mergePlugins(m_plugins, plugins);
		}
	}

	protected void mergePlugins(List existedPlugin, List plugins)
	{
		PluginWrapper plugin = null;
		PluginWrapper newPlugin = null;
		Iterator it = plugins.iterator();
		Iterator existedIt = null;

		while (it.hasNext())
		{
			newPlugin = (PluginWrapper) it.next();
			existedIt = existedPlugin.iterator();
			while (existedIt.hasNext())
			{
				plugin = (PluginWrapper) existedIt.next();
				if (!plugin.source.toString().equals(
						newPlugin.source.toString()))
				{
					continue;
				}
				if (plugin.timestamp < newPlugin.timestamp)
				{
					existedIt.remove();
				} else
				{
					it.remove();
				}
			}
		}
		existedPlugin.addAll(plugins);
	}

	public synchronized List query(String cond)
	{
		List retval = new ArrayList();
		for (PluginWrapper plugin : m_plugins)
		{
			if (plugin.interfaces == null)
			{
				continue;
			}
			if (false == LogicUtil.isInArray(plugin.interfaces, cond))
			{
				continue;
			}
			retval.add(plugin);
		}
		return retval;
	}

	public synchronized PluginWrapper getWrapper(Class plugin)
	{
		PluginWrapper retval = null;
		for (PluginWrapper wrapper : m_plugins)
		{
			if (wrapper.plugin == plugin)
			{
				retval = wrapper;
				break;
			}
		}
		return retval;
	}

	protected Object processParameterType(String type, String paramStream,
			String charset) throws JaxenException, DocumentException,
			IOException
	{
		Object retval = null;
		if ("b64array".equals(type))
		{
			retval = StringEx.parseBase64Param(paramStream, charset);
		} else if ("base64".equals(type))
		{
			retval = Base64.decodeString(paramStream, charset);
		} else if ("json".equals(type))
		{
			retval = (Map) m_jsonexchange.fromString(paramStream, null);
		}
		return retval;
	}

	protected List getParameters(String cmdOption)
	{
		return _cmdParams.get(cmdOption);
	}

	protected Map buildParameterMap(AbstractCommandPlugin plug,
			String cmdOption, String cs, String[] parameters)
			throws JaxenException, DocumentException, IOException
	{
		Map retval = null;
		// check the parameters.
		ConfigurationView plugCommandsView = plug.getCommandsView();

		if (parameters.length > 0 && plugCommandsView != null)
		{
			retval = new HashMap();

			List paramsList = getParameters(cmdOption);

			int len = Math.min(parameters.length, paramsList.size());

			for (int i = 0; i < len; i++)
			{
				Object param = paramsList.get(i);
				if (param == null)
				{
					continue;
				}
				// check the parameters info
				Element ele = (Element) param;
				Attribute attr = ele.attribute("name");
				if (attr == null)
				{
					break;
				}
				String paramName = attr.getText();
				Object val = parameters[i];

				attr = ele.attribute("type");
				// convert the input parameter base on the config.
				if (attr != null)
				{
					String type = attr.getText();
					Object newVal = processParameterType(type, parameters[i],
							cs);
					if (newVal != null)
					{
						val = newVal;
					}
				}
				retval.put(paramName, val);
			}
		}
		return retval;
	}
}
