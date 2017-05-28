package test.org.lx.arch;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.lx.arch.RESULT;
import org.lx.arch.ThreadPool;
import org.lx.util.GeneralException;

public class testThreadPool extends TestCase
{
	ThreadPool pool = null;

	public class TestCF implements ThreadPool.CommandFunction
	{
		public void complete(Object cmd, Object result) throws GeneralException
		{

		}
	}

	public class TestFunction implements ThreadPool.WorkFunction
	{
		/**
		 * handle the command
		 * @param cmd
		 * @param cf
		 *        use to complete the specified command. the command functions
		 */
		public RESULT process(Object cmd) throws GeneralException

		{
			TestCommand testCmd = (TestCommand) cmd;
			// System.out.println(System.currentTimeMillis() + ":"
			// + Thread.currentThread() + ":"
			// + Thread.currentThread().getPriority() + "--process:"
			// + testCmd.name);
			if (testCmd.duration > 0)
			{
				try
				{
					Thread.sleep(testCmd.duration);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			return RESULT.RESULT_SUCCESS;
		}

		/**
		 * if the command have not been dispatch to work thread in specific
		 * time, then timeout will be trigged.
		 * @param cmd
		 * @param cf
		 *        the command functions
		 */
		public void timeout(Object cmd) throws GeneralException

		{

		}
	}

	protected void setUp() throws Exception
	{
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() throws Exception
	{
	}

	/**
	 * return immediately if the same command exist the queue.
	 */
	public void testAddSameInstance()
	{
		Object handle1 = null;
		Object handle2 = null;
		// stop the work Thread before test.
		ThreadPool threadPool = getThreadPool();
		try
		{
			handle1 = threadPool.addCommand("testCmd", 1000, null);
			assertEquals(1, threadPool.getPendingCmdCount());
			handle2 = threadPool.addCommand("testCmd", 1000, null);
			assertEquals(1, threadPool.getPendingCmdCount());
			assertEquals(handle1, handle2);

			handle2 = threadPool.addCommand("testCmd1", 1000, null);
			assertEquals(2, threadPool.getPendingCmdCount());
			assertTrue(handle1 != handle2);

			handle2 = threadPool.addCommand("testCmd", 2000, null);
			assertEquals(3, threadPool.getPendingCmdCount());
			assertTrue(handle1 != handle2);

			handle2 = threadPool.addCommand("testCmd", 1000, new TestCF());
			assertEquals(4, threadPool.getPendingCmdCount());
			assertTrue(handle1 != handle2);
		} catch (InterruptedException e)
		{
			assertTrue(false);
		}

	}

	public static class TestCommand
	{
		public String name;

		public long duration;

		public TestCommand(String commandName, long duration)
		{
			this.name = commandName;
			this.duration = duration;
		}
	}

	public static class TestCommandFunction implements
			ThreadPool.CommandFunction
	{
		ArrayList cmds = new ArrayList();

		public void complete(Object cmd, Object result) throws GeneralException
		{
			TestCommand testCmd = (TestCommand) cmd;
			synchronized (cmds)
			{
				System.out.println("complete:" + testCmd.name);
				cmds.add(cmd);
			}
		}
	}

	public void testFetchByPriority()
	{
		TestCommandFunction testCF = new TestCommandFunction();
		// stop the work Thread before test.
		ThreadPool threadPool = getThreadPool();
		TestCommand cn = new TestCommand("testCmd_Normal", 0);
		TestCommand cl = new TestCommand("testCmd_Low", 0);
		TestCommand ch = new TestCommand("testCmd_High", 0);

		Object handle_n;
		try
		{
			handle_n = threadPool.addCommand(2, cn, 10000, testCF);
			Object handle_l = threadPool.addCommand(1, cl, 10000, testCF);
			Object handle_h = threadPool.addCommand(3, ch, 10000, testCF);
			assertEquals(3, threadPool.getPendingCmdCount());
			threadPool.start();

			ThreadPool.wait(handle_l);
			ThreadPool.wait(handle_n);
			ThreadPool.wait(handle_h);

			assertEquals(3, testCF.cmds.size());
			// we can't assume the command be execute by the specific priority
			// assertEquals(ch.name, ((TestCommand) testCF.cmds.get(0)).name);
			// assertEquals(cn.name, ((TestCommand) testCF.cmds.get(1)).name);
			// assertEquals(cl.name, ((TestCommand) testCF.cmds.get(2)).name);
		} catch (InterruptedException e)
		{
			assertTrue(false);
		}

	}

	// TODO:releaseAll will do nothing if none are wait? Semaphore for release
	// All.

	public void testCommandFishedFasterThanWait()
	{
		try
		{
			ThreadPool threadPool = getThreadPool();
			threadPool.start();
			// 1.exec faster than caller.
			int cmdDuration = 0;
			int callerWait = 100;
			TestCommand cmd = new TestCommand("testCmd_fast", cmdDuration);
			Object handle = threadPool.addCommand(cmd, 1000, null);
			Thread.sleep(callerWait);
			long oldPoint = System.currentTimeMillis();
			ThreadPool.wait(handle);
			long wait = System.currentTimeMillis() - oldPoint;
			assertTrue(wait < 10);

			// 2.exec slower that caller.
			cmdDuration = 300;
			callerWait = 100;
			cmd = new TestCommand("testCmd_slow", cmdDuration);
			handle = threadPool.addCommand(cmd, 10000, null);
			Thread.sleep(callerWait);
			oldPoint = System.currentTimeMillis();
			ThreadPool.wait(handle);
			wait = System.currentTimeMillis() - oldPoint;
			assertTrue("wait=" + wait,
					Math.abs(wait - (cmdDuration - callerWait)) < 10);

		} catch (InterruptedException e)
		{
			assertTrue(false);
		}

	}

	protected ThreadPool getThreadPool()
	{
		int maxcmd = 10;
		int checkInterval = 1000;// 1s
		int threadCount = 5;
		ThreadPool threadPool = new ThreadPool(new TestFunction(), threadCount,
				checkInterval, null, 3, maxcmd);
		return threadPool;

	}

	public void testThreadExit()
	{
		ThreadPool threadPool = getThreadPool();
		threadPool.start();
		threadPool.stop();
		assertEquals(0, threadPool.getThreadCount());
	}

	public void testReservedThreadForHighPriorityCommand()
	{
		ThreadPool threadPool = getThreadPool();
		TestCommandFunction testCF = new TestCommandFunction();
		// stop the work Thread before test.
		threadPool.stop();
		threadPool.start();

		TestCommand cn = null;
		int busyCmd = 6;
		try
		{
			for (int i = 0; i < busyCmd; i++)
			{
				cn = new TestCommand("testCmd_Normal-" + (i + 1), 100000);
				threadPool.addCommand(1, cn, 10000, null);
			}

			TestCommand ch = new TestCommand("testCmd_High", 0);
			long oldPoint = System.currentTimeMillis();
			// the high priority command should be executed immediately.
			Object handle;
			handle = threadPool.addCommand(3, ch, 100000, testCF);
			ThreadPool.wait(handle);
			long duration = System.currentTimeMillis() - oldPoint;
			assertTrue("duration=" + duration, duration < 10);
		} catch (InterruptedException e)
		{
			assertTrue(false);
		}
	}

	public void testPerformance()
	{
		ThreadPool threadPool = getThreadPool();
		threadPool.start();

		long count = 1 * 60 * 60 * 24;
		long oldPoint = System.currentTimeMillis();
		for (long i = 0; i < count; i++)
		{
		}
		long step1 = System.currentTimeMillis() - oldPoint;
		oldPoint = System.currentTimeMillis();
		Object handle = null;
		TestCommand cn = null;
		try
		{
			for (long i = 0; i < count; i++)
			{
				cn = new TestCommand("testCmd_Normal-" + (i + 1), 0);
				handle = threadPool.addCommand(1, cn, 10000, null);
				ThreadPool.wait(handle);
			}
			step1 = System.currentTimeMillis() - oldPoint;
		} catch (InterruptedException e)
		{
			assertTrue(false);
		}
	}

	public void testQueueFullAndEmpty()
	{
		// the default maxcmd is 10.
		ThreadPool threadPool = getThreadPool();
		TestCommand cn = null;
		try
		{
			int count = 10;
			int execTime = 100;
			for (long i = 0; i < count; i++)
			{
				cn = new TestCommand("testCmd_Normal-" + (i + 1), execTime);
				threadPool.addCommand(1, cn, 10000, null);
			}
			threadPool.start();

			cn = new TestCommand("testCmd_Addition", 0);
			long oldPoint = System.currentTimeMillis();
			Object handler = threadPool.addCommand(1, cn, 10000, null);
			ThreadPool.wait(handler);
			long duration = System.currentTimeMillis() - oldPoint;
			// the max_command is 10.
			assertTrue(Math.abs(duration - execTime * 2) < 10);
		} catch (InterruptedException e)
		{
			assertTrue(false);
		}
	}
}
