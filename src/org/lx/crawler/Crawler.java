package org.lx.crawler;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.lx.arch.ConfigurationView;
import org.lx.arch.Dom4jConfiguration;
import org.lx.arch.RESULT;
import org.lx.arch.ThreadPool;
import org.lx.arch.UrlRule;
import org.lx.arch.expr.ExprException;
import org.lx.arch.expr.ExprParser;
import org.lx.arch.expr.IExprFunction;
import org.lx.arch.logic.ConditionBuilder;
import org.lx.arch.logic.ConditionParserException;
import org.lx.arch.logic.ExpressItemMatcher;
import org.lx.arch.plugin.AppHelper;
import org.lx.http.HttpClient.HttpEvent;
import org.lx.http.HttpEventMatcher;
import org.lx.http.HttpRequest;
import org.lx.util.GeneralException;
import org.lx.util.IMatcher;
import org.lx.util.MapValueRender;
import org.lx.util.ObjectCache;
import org.lx.util.StringEx;
import org.lx.util.ValueRender;

/**
 * shoud support multi-protocol. cache and threadPool
 * @author Administrator
 */
public class Crawler
{
	static Log m_log = LogFactory.getLog(Crawler.class);

	static ConditionBuilder m_httpRspMatcherBuilder = null;

	protected Proxy m_defaultProxy = null;

	public class SnapshotBusyPipeFunction implements IExprFunction
	{
		public Object handle(Object[] params) throws ExprException
		{
			Object retval = Boolean.FALSE;
			String url = (String) params[0];
			String step = (String) params[1];
			// 1.get the snapshot create time.
			try
			{
				HttpRequest req = new HttpRequest(url, null, null);
				req.setProxy(Crawler.this.getProxy());
				AbstractSnapShot snapshot = (AbstractSnapShot) Crawler.this.m_snapshotMgr
						.getSnapShot(req, null);
				if (snapshot == null)
				{
					retval = Boolean.TRUE;
				} else
				{
					long curTime = System.currentTimeMillis();
					retval = Boolean.valueOf((curTime - snapshot
							.getSnapShotModified()) >= Long.parseLong(step));
				}
			} catch (IOException e)
			{
				throw new ExprException(e.getLocalizedMessage(), e);
			}
			return retval;
		}
	}

	public class SnapshotPropFunction implements IExprFunction
	{
		public Object handle(Object[] params) throws ExprException
		{
			Object retval = null;
			// snapshotProp
			String url = (String) params[0];
			String prop = (String) params[1];
			try
			{
				HttpRequest req = new HttpRequest(url, null, null);
				req.setProxy(Crawler.this.getProxy());
				AbstractSnapShot snapshot = (AbstractSnapShot) Crawler.this.m_snapshotMgr
						.getSnapShot(req, null);
				retval = snapshot.getProperty(ISnapShot.PROP_CONTENT, prop);

			} catch (IOException e)
			{
				throw new ExprException(e.getLocalizedMessage(), e);
			}
			return retval;
		}
	}

	HashMap protocolHandler = new HashMap();

	ConfigurationView m_confView;

	/* The SnapShot manager for the HttpClient */
	ISnapShotManager m_snapshotMgr;

	UrlRule m_urlRule;

	ThreadPool m_td;

	CrawlerFunction m_function = new CrawlerFunction();

	HashMap m_evtHandlers = new HashMap();

	protected Collection getEventHandles(IMatcher matcher)
	{
		synchronized (m_evtHandlers)
		{
			Object obj = m_evtHandlers.get(matcher);
			return obj == null ? null : (Collection) obj;
		}
	}

	public void addEventListener(IMatcher matcher, IEventHandler handle)
	{
		Collection list = getEventHandles(matcher);
		synchronized (m_evtHandlers)
		{
			if (list == null)
			{
				list = new ArrayList(2);
				m_evtHandlers.put(matcher, list);
			}
			list.add(handle);
		}
	}

	public void removeEventListener(IEventHandler handle)
	{
		synchronized (m_evtHandlers)
		{

			Collection values = m_evtHandlers.values();
			Iterator it = values.iterator();
			Collection list = null;
			while (it.hasNext())
			{
				list = (Collection) it.next();
				list.remove(handle);
			}
		}
	}

	protected ISnapShotManager getSnapShotManager()
	{
		return m_snapshotMgr;
	}

	protected void setSnapShotManager(ISnapShotManager snapshotManager)
	{
		m_snapshotMgr = snapshotManager;
	}

	public class CrawlerEventHandler implements IEventHandler
	{
		// notify all of the register EventHandler.
		public void onEvent(EventObject ev)
		{
			onResponseEvent(ev);
		}
	}

	protected CrawlerEventHandler m_EventHandler = new CrawlerEventHandler();

	/**
	 * get the reader resource for the specific url.
	 * @param html_url
	 * @return
	 */
	public ISnapShot getSnapShot(Request request, String ver, boolean offline,
			int timeout) throws Exception
	{
		// return SnapShot if the SnapShot isn't empty and then start the update
		// thread.
		// The resource's inputStream may be from URLConnection or SnapShot
		// file.
		ISnapShot retval = null;
		String str_url = request.getURI().toString();
		try
		{

			/* trigger a crawl action if current snapshot need update */
			if (!offline && m_snapshotMgr.needUpdate(request))
			{
				retval = execRequest(request);
			} else
			{
				// return the snapshot.
				retval = m_snapshotMgr.getSnapShot(request, ver);
			}

			// wait only if the SnapShot have some problem
			if (retval == null || !retval.checkIntegrity())
			{
				m_log.debug("fail at get " + str_url);
			}

		} finally
		{
			// remove the SnapShot file when any exception occur.
			if (retval != null && !retval.checkIntegrity())
			{
				retval.remove();
			}
		}
		return retval;
	}

	public class CrawlerFunction implements ThreadPool.WorkFunction
	{
		/**
		 * if the command have not been dispatch to work thread in specific
		 * time, then timeout will be trigged.
		 * @param cmd
		 */
		public void timeout(Object cmd)
		{
			if (cmd instanceof HttpRequest)
			{
				HttpRequest p = (HttpRequest) cmd;
				m_log.debug("timeout for " + p.getURI().toString());
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
				Request req = (Request) cmd;
				// trigger a crawl with timeout-3 minuts.
				ISnapShot snapshot = getSnapShot(req, null, false,
						3 * 60 * 1000);
				req.result = snapshot;
				retval = snapshot != null ? RESULT.RESULT_SUCCESS
						: RESULT.RESULT_FAIL;
			} catch (Throwable e)
			{
				throw new GeneralException(e);
			}
			return retval;
		}
	}

	public static interface ICacheCallback extends EventListener
	{
		public void exec(String key, Object obj);
	}

	public static class EventDispatcherCacheCallback implements ICacheCallback
	{
		ConfigurationView confView = null;

		Crawler crawler = null;

		public EventDispatcherCacheCallback(Crawler crawler,
				ConfigurationView confView)
		{
			this.confView = confView;
			this.crawler = crawler;
		}

		public void exec(String key, Object obj)
		{
			try
			{
				Element matcherModel = (Element) confView
						.query("extension/HttpMatcher/Matcher[@name='" + key
								+ "']/*[1]");
				IMatcher matcher = m_httpRspMatcherBuilder
						.compile(matcherModel);
				crawler.addEventListener(matcher, (IEventHandler) obj);

			} catch (ConditionParserException e)
			{
				m_log.warn("parse model error", e);
			}
		}
	}

	public static interface IEventHandler extends EventListener
	{
		public void onEvent(EventObject ev) throws IOException;
	}

	public static interface IProcessor
	{
		/**
		 * get a snapshot for the request
		 * @param request
		 * @return
		 */
		public ISnapShot getSnapShot(Request request, ISnapShot snapshot)
				throws Exception;

		public void setEventListener(IEventHandler handler);
	}

	public void registerProtocolHandler(String protocol, IProcessor processor)
	{
		processor.setEventListener(m_EventHandler);
		protocolHandler.put(protocol, processor);
	}

	protected IProcessor getProtocolHandler(String protocol)
	{
		Object obj = protocolHandler.get(protocol);
		return obj == null ? null : (IProcessor) obj;
	}

	public void unregisterProtocolHandler(String protocol)
	{
		protocolHandler.remove(protocol);
	}

	protected Crawler(UrlRule urlRule, ConfigurationView confView)
	{
		this(urlRule, confView, null);
	}

	protected Crawler(UrlRule urlRule, ConfigurationView confView,
			ThreadPool threadPool)
	{
		// XslSnapShotManager snapShotManager = new XslSnapShotManager(
		// m_xslLib);
		String dbName = null;
		m_snapshotMgr = new FileSnapShotManager(urlRule);
		m_urlRule = urlRule;
		m_confView = confView;
		if (threadPool != null)
		{
			threadPool.setWorkFunction(new CrawlerFunction());
			m_td = threadPool;
		}

		m_httpRspMatcherBuilder = new ConditionBuilder() {
			protected IMatcher createItemMatcher(Map attrs)
			{
				String type = (String) attrs.get("type");
				String name = (String) attrs.get("name");
				String val = (String) attrs.get("value");
				return new HttpEventMatcher(type, name, val);
			}
		};

	}

	public void start()
	{
		System.setProperty("sun.net.client.defaultConnectTimeout", "30000");
		System.setProperty("sun.net.client.defaultReadTimeout", "300000");
		if (m_td != null)
		{
			m_td.start();
		}
	}

	public void stop()
	{
		if (m_td != null)
		{
			m_td.stop();
		}
	}

	/**
	 * execute the crawl action.
	 * @param request
	 * @return
	 * @throws Exception
	 */
	protected ISnapShot execRequest(Request request) throws Exception
	{
		ISnapShot retval = null;
		String protocol = request.getProtocol();
		IProcessor processor = getProtocolHandler(protocol);
		if (processor != null)
		{
			ISnapShot snapshot = m_snapshotMgr.getSnapShot(request, null);
			if (snapshot == null)
			{
				snapshot = m_snapshotMgr.newSnapShot(request);
			}
			/*
			 * get the new snapshot through processor. some protocol will use
			 * the cache to control the request,such as http etag.
			 */
			retval = processor.getSnapShot(request, snapshot);
		} else
		{
			// TODO:default protocol. get the binary
			// other protocol.
			// in = getInputStream(uc);

			// write the SnapShot firstly.
			// snapshot.save(uc.getURL().toString(), in, null);

		}
		return retval;
	}

	protected List parseUrls(InputStream in, Map namespaces) throws IOException
	{
		Dom4jConfiguration conf = namespaces == null ? new Dom4jConfiguration(
				in) : new Dom4jConfiguration(in, namespaces);
		// all of the 'A' node with href attribute in any namespace.
		String linkQueryXpath = m_confView.getAttribute("linkParser");
		ConfigurationView confView = new ConfigurationView(conf, null);
		return confView.queryAll(linkQueryXpath,
				ConfigurationView.STRING_TRANSFORM);
	}

	/**
	 * check whether the url will be crawled.
	 * @param url
	 * @return
	 */
	protected boolean matches(String url, String user, int deep)
	{
		boolean retval = false;
		do
		{
			Element node = m_urlRule.getCrawlerFilterModel(url);
			if (node == null)
			{
				break;
			}
			// check the model.
			IMatcher matcher = getMatcher(node);
			Map params = new HashMap();

			params.put("url", url);
			params.put("user", user);
			params.put("deep", Integer.valueOf(deep));

			retval = matcher.isMatch(new MapValueRender(params));
		} while (false);
		return retval;
	}

	protected Request buildRequest(String uri, String user, Map params)
	{
		Request retval = null;
		String protocol = Request.getProtocol(uri);
		if ("http".equals(protocol) || "https".equals(protocol))
		{
			retval = new HttpRequest(uri, user, "GET", null, null, null, null);
			((HttpRequest) retval).setProxy(getProxy());
		}
		return retval;
	}

	public class CrawlerCompleteCallBack implements ThreadPool.CommandFunction
	{
		int deep;

		public CrawlerCompleteCallBack(int deep)
		{
			this.deep = deep;
		}

		public void complete(Object cmd, Object result) throws GeneralException
		{
			Request req = (Request) cmd;

			ISnapShot snapshot = (ISnapShot) req.result;
			String user = req.user;

			// get the command's exec result.
			InputStream snapShotIn;
			try
			{
				snapShotIn = snapshot.getInputStream();
				// get the links from the snapShotIn
				HashMap namespaces = new HashMap(1);
				namespaces.put("xhtml", "http://www.w3.org/1999/xhtml");
				List urls = parseUrls(snapShotIn, namespaces);

				Iterator it = urls.iterator();
				while (it.hasNext())
				{
					try
					{
						// and crawl the links with deep-1.
						addRequest(String.valueOf(it.next()), user, deep - 1);
					} catch (MalformedURLException e)
					{
						m_log.warn(e.getLocalizedMessage(), e);
					} catch (Exception e)
					{
						m_log.warn(e.getLocalizedMessage(), e);
					}
				}
			} catch (IOException e1)
			{
				m_log.warn(e1.getLocalizedMessage(), e1);
			}
		}
	}

	/**
	 * crawl the URL with the specific deep.
	 * @param url
	 * @param deep
	 * @return
	 * @throws Exception
	 */
	public RESULT addRequest(String url, String user, int deep)
			throws Exception
	{
		RESULT retval = null;
		do
		{
			try
			{
				// get the matcher rule for the specific url.
				if (!matches(url, user, deep))
				{
					m_log.info("ignore crawl for [user:" + user + "] url:"
							+ url);
					break;
				}
				// build the crawler parameter base on the rule.
				Request request = buildRequest(url, user, null);

				// build the asynchronous commands.
				CrawlerCompleteCallBack cb = null;
				if (deep > 0)
				{
					// create the other crawl command in the cf callback.
					cb = new CrawlerCompleteCallBack(deep);
				}
				retval = AppHelper
						.asynExec(m_function, request, 120 * 1000, cb);

			} catch (Exception e1)
			{
				m_log.warn(e1);
			}

		} while (false);
		return retval;
	}

	static ObjectCache m_objCache = null;

	protected static void initObjectCache(ConfigurationView confView)
	{
		Map classInfo = confView.getNameValuePaired("ObjectCache/property",
				"name", "value");
		if (classInfo != null)
		{
			m_objCache = new ObjectCache(classInfo,
					Crawler.class.getClassLoader());
		}
	}

	/**
	 * <instances name="ProtocolHandler" interfaceid="ProtocolHandler">
	 * <instance name="https" classid="HttpHandler"/> </instances>
	 * @param confView
	 * @return
	 */
	protected static List loadObject(ConfigurationView confView,
			ICacheCallback callback)
	{
		Map objects = confView
				.getNameValuePaired("instance", "name", "classid");
		ArrayList array = new ArrayList(objects.size());
		String interfaceName = confView.getAttribute("interfaceid");
		// 1.get the interface info.
		Class interfaceClazz = null;
		if (interfaceName != null)
		{
			try
			{
				interfaceClazz = m_objCache.getClass(interfaceName);
			} catch (IOException e)
			{
				m_log.warn("failed when load interface:" + interfaceName, e);
			}
		}

		Iterator it = objects.keySet().iterator();
		String key = null;
		String classid = null;
		Object inst = null;
		while (it.hasNext())
		{
			try
			{
				key = (String) it.next();
				classid = (String) objects.get(key);

				inst = m_objCache.get(classid);

				if (inst == null)
				{
					continue;
				}
				if (!interfaceClazz.isAssignableFrom(inst.getClass()))
				{
					m_log.warn("skip load " + interfaceName + ":" + key + "->"
							+ inst);
					continue;
				}
				if (callback != null)
				{
					callback.exec(key, inst);
				}
				array.add(inst);
			} catch (IOException e)
			{
				m_log.warn("failed when load Object:" + key, e);
			}
		}
		return array;
	}

	/**
	 * create a Crawler instance by the config info.
	 * @param snapShotManager
	 * @param confView
	 * @param runtimeView
	 * @return
	 * @throws Exception
	 */
	public static Crawler createInstance(UrlRule urlRule,
			ConfigurationView confView, ConfigurationView runtimeView)
			throws Exception
	{

		// create the crawler instance
		ConfigurationView crawlerRuntimeView = runtimeView == null ? null
				: runtimeView.createSubView("crawler");

		// load the IProcessor from config.
		final Crawler crawler = new Crawler(urlRule, confView);
		initObjectCache(confView);

		// 1.crawler.registerProtocolHandler with ObjectCache.
		ConfigurationView protocoHandlerlView = confView
				.getSubView("extension/instances[@name='ProtocolHandler']");

		ICacheCallback cb = new ICacheCallback() {
			public void exec(String key, Object obj)
			{
				crawler.registerProtocolHandler(key, (IProcessor) obj);
			}
		};
		loadObject(protocoHandlerlView, cb);

		/**
		 * 2.create a EventMatcher base on the HttpResult(XML build the logic
		 * string)
		 */
		// crawler.addEventListener
		ConfigurationView eventHandlerlView = confView
				.getSubView("extension/instances[@name='EventDispatcher']");
		cb = new EventDispatcherCacheCallback(crawler, confView);
		loadObject(eventHandlerlView, cb);

		// 3. init the proxy setting.
		String proxyURL = confView.getAttribute("proxy", "url");
		Proxy proxy = Crawler.buildProxy(proxyURL);
		crawler.setProxy(proxy);
		return crawler;
	}

	public static Proxy buildProxy(String proxyURL)
			throws NumberFormatException, UnknownHostException
	{
		String proxyInfos[] = StringEx.regexFind(proxyURL,
				StringEx.DEFAULT_PROXYSERVER_MATCHER,
				new int[] { 1, 2, 3, 4, 5 });
		String type = proxyInfos[0];
		String host = proxyInfos[3];
		String port = proxyInfos[4];

		Proxy.Type pType = null;
		if ("http".equalsIgnoreCase(type))
		{
			pType = Proxy.Type.HTTP;
		} else if ("socks5".equalsIgnoreCase(type)
				|| "socks4".equalsIgnoreCase(type))
		{
			pType = Proxy.Type.SOCKS;
		}
		InetSocketAddress socketAddress = new InetSocketAddress(
				InetAddress.getByName(host), Integer.parseInt(port));
		return new Proxy(pType, socketAddress);
	}

	public class HttpEventValueRender implements ValueRender
	{
		HttpEvent ev;

		public HttpEventValueRender(HttpEvent ev)
		{
			this.ev = ev;
		}

		public Object get(String key)
		{
			Object retval = null;
			// provide the attributes for HttpEvent Object
			// $(url)/$(request)[size|type]/$(response)[...]/$step
			URLConnection uc = (URLConnection) ev.getSource();
			URL url = uc.getURL();
			if ("url".equals(key))
			{
				retval = url;
			} else if ("request".equals(key))
			{
				retval = uc.getRequestProperties();
			} else if ("response".equals(key))
			{
				retval = uc.getHeaderFields();
			} else if ("connectTimeout".equals(key))
			{
				retval = Integer.valueOf(uc.getConnectTimeout());
			}
			return retval;
		}
	}

	HashMap m_eventModelMap = new HashMap();

	void onResponseEvent(EventObject ev)
	{
		// 1.filter the ev.
		URLConnection uc = (URLConnection) ev.getSource();
		String url = uc.getURL().toString();
		Element model = m_urlRule.getSaveFilterModel(url);
		IMatcher evMatcher = getMatcher(model);
		if (evMatcher != null)
		{
			HttpEventValueRender vr = new HttpEventValueRender((HttpEvent) ev);
			if (!evMatcher.isMatch(vr))
			{
				m_log.warn("Event skip");
				return;
			}
		}

		// 2.matches the EventHandlers.
		synchronized (m_evtHandlers)
		{
			Collection matchers = m_evtHandlers.keySet();
			Iterator it = matchers.iterator();
			Iterator it_handle = null;
			IEventHandler handle = null;
			IMatcher matcher = null;
			Collection handles = null;
			while (it.hasNext())
			{
				matcher = (IMatcher) it.next();
				if (matcher.isMatch(ev))
				{
					// notify the callback.
					handles = getEventHandles(matcher);
					it_handle = handles.iterator();
					while (it_handle.hasNext())
					{
						handle = (IEventHandler) it_handle.next();
						try
						{
							handle.onEvent(ev);
						} catch (Throwable e)
						{
							m_log.warn(e);
						}
					}
				}
			}
		}
	}

	ConditionBuilder m_eventFilterBuilder = null;

	protected IMatcher getMatcher(Element model)
	{
		IMatcher evMatcher = null;
		try
		{
			Object obj = m_eventModelMap.get(model);
			// get from cache firstly.
			if (obj == null)
			{
				if (m_eventFilterBuilder == null)
				{
					// build the glueExpress for filter snapshotMgr's event.
					final ExprParser glueExprParser = ExprParser
							.getDefaultExprParser();
					glueExprParser.registerFunction("snapshotBusyPipe",
							new SnapshotBusyPipeFunction());
					glueExprParser.registerFunction("snapshotProp",
							new SnapshotPropFunction());

					// build the IMatcher for the model.
					m_eventFilterBuilder = new ConditionBuilder() {
						protected IMatcher createItemMatcher(Map attrs)
						{
							return new ExpressItemMatcher(attrs, glueExprParser);
						}
					};
				}

				evMatcher = m_eventFilterBuilder.compile(model);
				m_eventModelMap.put(model, evMatcher);
			} else
			{
				evMatcher = (IMatcher) obj;
			}
		} catch (ConditionParserException e)
		{
			m_log.warn(e.getLocalizedMessage(), e);
		}
		return evMatcher;
	}

	public Proxy getProxy()
	{
		return m_defaultProxy;
	}

	public void setProxy(Proxy proxy)
	{
		m_defaultProxy = proxy;
	}

}
