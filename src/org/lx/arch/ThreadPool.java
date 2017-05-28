/**
 * notify/wait must be invoked within the same object.
 * such as
 * synchronized (obj)
 * {
 * obj.wait();
 * }
 */
package org.lx.arch;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lx.util.GeneralException;
import org.lx.util.SimpleQueue;

public class ThreadPool
{
	int m_max_thread_count;

	ArrayList m_wk_threads;

	ConfigurationView m_runtimeView;

	MonitorThread m_monitor_thread;

	int m_checkInterval;

	int m_max_cmd_count;

	WorkFunction m_wk_func;

	// store the public commands with priority.
	SimpleQueue[] m_cmd_list_array;

	// store the worker thread private queue.
	SimpleQueue[] m_workerQueues;

	SimpleSemaphore m_cmd_list_available;

	// for statistics.
	int m_st_pendingCmd_peak = 0;

	int m_st_cnt = 0;

	int m_st_cmd_timeout_cnt = 0;

	int[] m_st_cmd_cnt;

	int[] m_st_cmd_err_cnt;

	long m_waitTime;

	static Log m_log = LogFactory.getLog(ThreadPool.class);

	Integer EXIT_CMD = new Integer(-1);

	protected void addConfAttribute(String key, int val)
	{
		m_runtimeView.addAttribute(key, String.valueOf(val));
	}

	protected void addConfAttribute(Object handle, String key, int val)
	{
		m_runtimeView.addAttribute(handle, key, String.valueOf(val));
	}

	public class CommandWrapper
	{
		long addTime;

		long step;

		Object cmd;

		CommandFunction cf;

		RESULT result;

		int priority;

		SimpleSemaphore available;

		public CommandWrapper(int priority, Object cmd, long step,
				CommandFunction cf)
		{
			this.addTime = new Date().getTime();
			this.step = step;
			this.cmd = cmd;
			this.cf = cf;
			this.result = RESULT.RESULT_PENDING;
			this.available = new SimpleSemaphore();
			this.priority = priority;

		}

		public int hashCode()
		{
			int retval = 0;
			retval = cmd.hashCode() + new Long(step).hashCode();
			if (cf != null)
			{
				retval += cf.hashCode();
			}
			return retval;
		}

		public boolean equals(Object obj)
		{
			boolean retval = false;
			if (obj instanceof CommandWrapper)
			{
				CommandWrapper wrapper = (CommandWrapper) obj;
				do
				{
					if (this.cmd.equals(wrapper.cmd) == false)
					{
						break;
					}

					if (this.step != wrapper.step)
					{
						break;
					}

					if (this.cf != wrapper.cf)
					{
						break;
					}

					retval = true;
				} while (false);
			}
			return retval;
		}
	}

	public static interface CommandFunction
	{
		/**
		 * finish the specified command, you may release the related-resource.
		 * @param cmd
		 * @throws WcException
		 */
		public void complete(Object cmd, Object result) throws GeneralException;

	}

	public static interface WorkFunction
	{
		/**
		 * handle the command
		 * @param cmd
		 * @param cf
		 *        use to complete the specified command. the command functions
		 */
		public RESULT process(Object cmd) throws GeneralException;

		/**
		 * if the command have not been dispatch to work thread in specific
		 * time, then timeout will be trigged.
		 * @param cmd
		 * @param cf
		 *        the command functions
		 */
		public void timeout(Object cmd) throws GeneralException;
	}

	/**
	 * This class is used to support the execution timeout feature
	 * @author achilles.zhao
	 */
	public class MonitorThread extends Thread
	{
		protected final static String MODULE_NAME = "MonitorThread";

		boolean m_exit;

		Object m_monitorConfHandler = null;

		public MonitorThread()
		{
			m_monitorConfHandler = createEntry(this);
			m_exit = false;
		}

		public synchronized void setExit()
		{
			m_exit = true;
			notify();
		}

		protected Object createEntry(Object obj)
		{
			Object retval = null;

			// sync info to the runtime configuration.
			if (m_runtimeView != null)
			{
				Configuration conf = m_runtimeView.getConfigutation();
				retval = m_runtimeView.createNode(MODULE_NAME);
				conf.addAttribute(retval, ATTR_ID,
						Integer.toHexString(obj.hashCode()));
			}
			return retval;
		}

		/**
		 * scan the m_cmd_list to check whether there is command will timeout.
		 */
		protected void scanTimeout() throws GeneralException
		{
			for (int i = 0; i < m_cmd_list_array.length; i++)
			{
				SimpleQueue cmd_list = m_cmd_list_array[i];
				synchronized (cmd_list)
				{
					Iterator cmd_it = cmd_list.iterator();

					// for statistics
					int pendingCnt = cmd_list.size();
					if (m_st_pendingCmd_peak < pendingCnt)
					{
						m_st_pendingCmd_peak = pendingCnt;
						if (m_runtimeView != null)
						{
							addConfAttribute(ATTR_CNT_PEAK,
									m_st_pendingCmd_peak);
						}
					}

					// dispatch the commands.
					while (cmd_it.hasNext())
					{
						CommandWrapper wk_cmd = (CommandWrapper) cmd_it.next();
						String cmdId = buildCmdId(wk_cmd.cmd);

						// timeout if the command wait too long.
						if (wk_cmd != null
								&& wk_cmd.step > 0
								&& (System.currentTimeMillis() - wk_cmd.addTime > wk_cmd.step))
						{
							try
							{
								// statistics
								addConfAttribute(m_monitorConfHandler,
										ATTR_CNT_TIMEOUT,
										m_st_cmd_timeout_cnt++);

								m_log.warn(MODULE_NAME + ":" + cmdId
										+ " timeout(" + wk_cmd.step + ")!");
								printWorkerStack();
								m_wk_func.timeout(wk_cmd.cmd);

							} finally
							{
								// release the resource about this cmd if the
								// command
								// need not handle.
								if (wk_cmd.result != RESULT.RESULT_PENDING
										&& wk_cmd.cf != null)
								{
									wk_cmd.cf.complete(wk_cmd.cmd,null);
								}

								// release the waiting thread in any case.
								wk_cmd.available.releaseAll();
								cmd_it.remove();
								m_log.warn(MODULE_NAME + ":" + cmdId
										+ ".notifyAll");
							}
						}
					}
				}
			}

		}

		public void run()
		{
			String threadId = buildThreadId(this);

			m_log.warn(getName() + ":Enter " + threadId);

			while (!m_exit)
			{
				try
				{
					synchronized (this)
					{
						if (m_checkInterval <= 0)
						{
							wait();
						} else
						{
							wait(m_checkInterval);
						}
						scanTimeout();
					}
				} catch (Throwable e)
				{
					m_log.warn(e.getLocalizedMessage(), e);
				}
			}
			m_log.warn(getName() + ":Exit " + threadId);

		}
	}

	final static String ATTR_ID = "id";

	final static String ATTR_SETP = "step";

	final static String ATTR_CNT = "count";

	final static String ATTR_CNT_TIMEOUT = "timeoutCount";

	final static String ATTR_CNT_PEAK = "peakCount";

	final static String ATTR_CNT_ERR = "errorCount";

	public class WorkerThread extends Thread
	{
		protected final static String MODULE_NAME = "WorkerThread";

		volatile boolean m_exit;

		int m_debug_point = 0;

		public synchronized boolean isIdle()
		{
			return getState() == State.WAITING;
		}

		/**
		 * notify the thread to exit.
		 */
		public synchronized void setExit()
		{
			m_exit = true;
		}

		protected Object createEntry(Object obj)
		{
			Object retval = null;
			if (m_runtimeView != null)
			{
				retval = m_runtimeView.createNode(MODULE_NAME);
				m_runtimeView.getConfigutation().addAttribute(retval, ATTR_ID,
						Integer.toHexString(obj.hashCode()));
			}
			return retval;
		}

		Object m_wrapperConfHandler;

		public WorkerThread()
		{
			m_wrapperConfHandler = createEntry(this);
		}

		public void run()
		{
			String threadId = buildThreadId(this);
			m_log.warn(getName() + ":Enter " + threadId);

			while (!m_exit)
			{
				m_debug_point = 0;
				try
				{
					// for debug
					// if (m_runtimeView != null)
					// {
					// addConfAttribute(m_wrapperConfHandler, ATTR_SETP,
					// m_debug_point++);
					// }

					// 1. get a command from the command List.
					CommandWrapper internalCmd = fetchCommand(-1);
					if (internalCmd == null || internalCmd.cmd == null)
					{
						continue;
					}

					String cmdId = buildCmdId(internalCmd.cmd);

					// 2. change the current thread priority if necessary.
					int cmdPriority = internalCmd.priority;
					int priorityCount = m_cmd_list_array.length;
					int priority = (Thread.MAX_PRIORITY - Thread.MIN_PRIORITY)
							* cmdPriority / priorityCount;
					priority += Thread.MIN_PRIORITY;

					if (priority != getPriority())
					{
						setPriority(priority);
					}

					if (m_runtimeView != null)
					{
						addConfAttribute(m_wrapperConfHandler, ATTR_SETP,
								m_debug_point++);
					}
					// {{debug
					if (m_log.isDebugEnabled())
					{
						m_log.debug(getName() + ":-->" + cmdId);
					}
					// }}

					int index = m_wk_threads.indexOf(this);
					try
					{
						if (m_runtimeView != null)
						{

							addConfAttribute(m_wrapperConfHandler, ATTR_SETP,
									m_debug_point++);
							// statistics
							addConfAttribute(m_wrapperConfHandler, ATTR_CNT,
									++m_st_cmd_cnt[index]);
						}
						internalCmd.result = m_wk_func.process(internalCmd.cmd);
					} catch (Throwable e)
					{
						// statistics
						if (m_runtimeView != null)
						{
							addConfAttribute(m_wrapperConfHandler,
									ATTR_CNT_ERR, m_st_cmd_err_cnt[index]++);
						}
						m_log.warn(e.getLocalizedMessage(), e);
					} finally
					{
						// release the resource about this cmd if the command
						// need not handle.
						if (internalCmd.result != RESULT.RESULT_PENDING
								&& internalCmd.cf != null)
						{
							internalCmd.cf.complete(internalCmd.cmd,internalCmd.result);
						}

						if (m_runtimeView != null)
						{
							addConfAttribute(m_wrapperConfHandler, ATTR_SETP,
									m_debug_point++);
						}
						if (m_log.isDebugEnabled())
						{
							m_log.debug(getName() + ":<--" + cmdId);
							m_log.debug(getName() + ":" + cmdId + " notifyAll");
						}
						// notify the waiting thread in any case.
						internalCmd.available.releaseAll();
					}

				} catch (Throwable e)
				{
					m_log.warn(e.getLocalizedMessage(), e);
				}
			}
			if (m_runtimeView != null)
			{
				ThreadPool.this.addConfAttribute(m_wrapperConfHandler,
						ATTR_SETP, m_debug_point++);
			}
			m_log.warn(getName() + ":Exit " + threadId);
		}

		protected CommandWrapper fetchCommand(int timeout)
				throws InterruptedException
		{
			CommandWrapper retval = null;

			do
			{
				// 1.only the WrapperThread can fetch commands.
				Thread curThread = Thread.currentThread();
				int index = m_wk_threads.indexOf(curThread);
				if (index == -1)
				{
					break;
				}
				// 2.fetch command from broadcast list.
				if (m_workerQueues[index].size() > 0)
				{
					retval = (CommandWrapper) m_workerQueues[index].poll();
					break;
				}
				// 3.fetch one command from m_cmd_list_array.
				for (int i = m_cmd_list_array.length - 1; i >= 0; i--)
				{
					SimpleQueue cmd_list = m_cmd_list_array[i];

					// skip the blank list.
					if (cmd_list.size() == 0)
					{
						continue;
					}
					retval = (CommandWrapper) cmd_list.poll();
					break;
				}
				if (retval != null)
				{
					break;
				}
				// wait until there are some commands need to be processed.
				m_cmd_list_available.acquire(timeout);
			} while (false);
			return retval;
		}
	}

	public ThreadPool(WorkFunction f, int thread_count,
			ConfigurationView runtimeView)
	{
		// 3 priority by default.
		this(f, thread_count, 10 * 1000, runtimeView, 3, 1024);
	}

	/**
	 * the pending commands will can not be processed if the check_intval less
	 * than 0 and no other commands input,
	 * @param f
	 *        The work thread's core logic.
	 * @param thread_count
	 *        the thread count for WorkFunction.
	 * @param check_intval
	 *        the timeout check routine will executed per check_intval
	 */
	public ThreadPool(WorkFunction f, int thread_count, int check_intval,
			ConfigurationView runtimeView, int priority, int maxCmd)
	{
		m_max_thread_count = thread_count;
		m_checkInterval = check_intval;
		m_wk_threads = new ArrayList(m_max_thread_count);
		m_max_cmd_count = maxCmd;
		m_wk_func = f;

		m_cmd_list_array = new SimpleQueue[priority];
		for (int i = 0; i < priority; i++)
		{
			m_cmd_list_array[i] = new SimpleQueue(maxCmd);
		}

		// create worker thread command queue.
		m_workerQueues = new SimpleQueue[m_max_thread_count];
		for (int i = 0; i < m_workerQueues.length; i++)
		{
			m_workerQueues[i] = new SimpleQueue(maxCmd);
		}

		m_cmd_list_available = new SimpleSemaphore();

		// for statistics
		m_st_cmd_cnt = new int[m_max_thread_count];
		m_st_cmd_err_cnt = new int[m_max_thread_count];

		m_runtimeView = runtimeView;
	}

	public void setWorkFunction(WorkFunction func)
	{
		m_wk_func = func;
	}

	public WorkFunction getWorkFunction()
	{
		return m_wk_func;
	}

	public void start()
	{
		Thread thread = null;
		// start the work thread.
		for (int i = 0; i < m_max_thread_count; i++)
		{
			thread = new WorkerThread();

			String name = WorkerThread.MODULE_NAME + "_"
					+ Integer.toHexString(thread.hashCode());

			thread.setName(name);
			m_wk_threads.add(thread);
			thread.start();
		}

		// start the monitor thread if need.
		if (m_checkInterval > 0)
		{
			m_monitor_thread = new MonitorThread();

			String monitorThreadName = MonitorThread.MODULE_NAME + "_"
					+ Integer.toHexString(m_monitor_thread.hashCode());
			m_monitor_thread.setName(monitorThreadName);

			m_monitor_thread.start();
		}
	}

	public void printWorkerStack()
	{
		// log all worker thread call stack.
		Iterator it = m_wk_threads.iterator();
		StringBuffer sb = new StringBuffer();
		while (it.hasNext())
		{
			Thread thread = (Thread) it.next();
			sb.append(thread.getName() + " stack:\n");
			StackTraceElement[] trace = thread.getStackTrace();
			for (int i = 0; i < trace.length; i++)
			{
				sb.append("\tat " + trace[i] + "\n");
			}
		}
		m_log.warn(sb.toString());
	}

	protected static String buildCmdId(Object cmd)
	{
		return "cmd[" + Integer.toHexString(cmd.hashCode()) + "]";
	}

	protected static String buildThreadId(Runnable thread)
	{
		return "thread[" + Integer.toHexString(thread.hashCode()) + "]";
	}

	public static void wait(Object handler)
	{
		try
		{
			((CommandWrapper) handler).available.acquire();
		} catch (InterruptedException e)
		{
			m_log.warn(e.getLocalizedMessage(), e);
		}
	}

	public static synchronized RESULT getResult(Object handler)
	{
		RESULT retval = RESULT.RESULT_FAIL;
		if (handler instanceof CommandWrapper)
		{
			retval = ((CommandWrapper) handler).result;
		}
		return retval;
	}

	public Object addCommand(Object cmd, long timeout, CommandFunction cf)
			throws InterruptedException
	{
		int nomalPriority = (m_cmd_list_array.length) / 2 + 1;
		return addCommand(nomalPriority, cmd, timeout, cf);
	}

	/**
	 * broadcastCommand is triggered by the normal command.
	 * @param priority
	 * @param cmd
	 * @param timeout
	 * @param cf
	 * @throws InterruptedException
	 */
	public void addBroadcastCommand(int priority, Object cmd, long timeout,
			CommandFunction cf) throws InterruptedException
	{
		long realTimeout = timeout != 0 ? timeout : m_waitTime;
		CommandWrapper retval = new CommandWrapper(priority, cmd, realTimeout,
				cf);

		int count = m_wk_threads.size();
		for (int i = 0; i < count; i++)
		{
			m_workerQueues[i].put(retval);
			m_cmd_list_available.release();
		}
	}

	/**
	 * add a cmd to the thread pool's cmdlist
	 * @param priority
	 *        from 1-3
	 * @param cmd
	 * @param timeout
	 * @param cf
	 * @return
	 * @throws InterruptedException
	 */
	public Object addCommand(int priority, Object cmd, long timeout,
			CommandFunction cf) throws InterruptedException
	{
		CommandWrapper retval = null;

		SimpleQueue cmd_list = m_cmd_list_array[priority - 1];

		if (cmd_list.size() >= m_max_cmd_count)
		{
			return null;
		}
		String cmdId = buildCmdId(cmd);
		// add the cmd to command list
		m_log.debug("Input " + cmdId + "=" + cmd);
		long realTimeout = timeout != 0 ? timeout : m_waitTime;
		retval = new CommandWrapper(priority, cmd, realTimeout, cf);

		if (!cmd_list.contains(retval))
		{
			// block if the queue full.
			cmd_list.put(retval);
			/*
			 * wake up a worker Thread from ThreadPool if the cmd is high level
			 * or the count of idle thread >1.
			 */
			int idleCount = getIdleThread();
			int signal = m_cmd_list_available.getSignal();
			int highPriority = m_cmd_list_array.length;
			// System.out.println(System.currentTimeMillis() + "-"
			// + Thread.currentThread() + ":release thread-idle="
			// + idleCount + ",signal=" + signal);

			// reserve 1 thread for high priority command.
			if ((idleCount - signal) > 1 || priority == highPriority)
			{
				m_cmd_list_available.release();
			}
		} else
		{
			// the current command exists.
			retval = (CommandWrapper) cmd_list.peek(retval);
		}

		// statistics
		if (m_runtimeView != null)
		{
			addConfAttribute(ATTR_CNT, ++m_st_cnt);
		}
		// trigger the monitor thread if it is existed
		if (m_monitor_thread != null)
		{
			synchronized (m_monitor_thread)
			{
				m_monitor_thread.notify();
			}
		}
		return retval;
	}

	public void stop()
	{
		WorkerThread thread = null;
		// 1. stop the monitor thread.
		if (m_monitor_thread != null)
		{
			m_monitor_thread.setExit();
		}

		// 2.stop the work thread.
		// 2.1 mark the exit flag for work threads.
		Iterator it = m_wk_threads.iterator();
		while (it.hasNext())
		{
			thread = (WorkerThread) it.next();
			thread.setExit();
		}

		// 2.2 notify the Worker thread.
		m_cmd_list_available.releaseAll();

		// 2.3 destroy the wrapper thread.
		it = m_wk_threads.iterator();
		while (it.hasNext())
		{
			try
			{
				thread = (WorkerThread) it.next();
				thread.join();
				it.remove();
			} catch (InterruptedException e)
			{
				m_log.warn("InterruptedException:", e);
			}
		}
		m_log.info("All Worker thread exit!");

	}

	public int getThreadCount()
	{
		return m_wk_threads.size();
	}

	public int getMaxThreadCOunt()
	{
		return m_max_thread_count;
	}

	public int getTimeOutCheckInterval()
	{
		return m_checkInterval;
	}

	public int getPendingCmdCount()
	{
		int retval = 0;
		SimpleQueue cmd_list = null;
		for (int i = 0; i < m_cmd_list_array.length; i++)
		{
			cmd_list = m_cmd_list_array[i];
			synchronized (cmd_list)
			{
				retval += cmd_list.size();
			}
		}
		return retval;
	}

	public int getCmdTimeoutCnt()
	{
		return m_st_cmd_timeout_cnt;
	}

	public int getPendingCmdPeak()
	{
		return m_st_pendingCmd_peak;
	}

	public int[] getThreadDebugPoint()
	{
		int[] retval = new int[m_wk_threads.size()];
		WorkerThread thread = null;
		for (int i = 0; i < retval.length; i++)
		{
			thread = (WorkerThread) m_wk_threads.get(i);
			retval[i] = thread.m_debug_point;
		}
		return retval;
	}

	public int[] getThreadCmdsCount()
	{
		return m_st_cmd_cnt;
	}

	public int[] getThreadCmdsErrCount()
	{
		return m_st_cmd_err_cnt;
	}

	public static final String THREAD_COUNT = "@threadCount";

	public static final String POLLINTERVAL = "@pollInterval";

	public static final String WAITTIME = "@waitTime";

	public static final String MAX_CMD_COUNT = "maxCmdCount";

	public static ThreadPool createInstance(WorkFunction function,
			ConfigurationView cfgView, ConfigurationView runtimeView)
	{
		ThreadPool retval = null;
		int count = Integer.parseInt(cfgView.query(THREAD_COUNT).toString());
		int pollInterval = Integer.parseInt(cfgView.query(POLLINTERVAL)
				.toString());

		String maxCmdCountStr = cfgView.getAttribute(MAX_CMD_COUNT);
		int maxCmdCount = maxCmdCountStr == null ? 1024 : Integer
				.parseInt(maxCmdCountStr);

		long waitTime = Long.parseLong(cfgView.query(WAITTIME).toString());
		retval = new ThreadPool(function, count, pollInterval, runtimeView, 3,
				maxCmdCount);
		retval.m_waitTime = waitTime;
		return retval;
	}

	public int getIdleThread()
	{
		int retval = 0;
		Iterator it = m_wk_threads.iterator();
		WorkerThread wk = null;
		while (it.hasNext())
		{
			wk = (WorkerThread) it.next();
			if (wk.isIdle())
			{
				retval++;
			}
		}
		return retval;
	}
}
