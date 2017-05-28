package org.lx.arch;

public class SimpleSemaphore
{
	private volatile int value = 0;

	private volatile int waitCount = 0;

	public SimpleSemaphore()
	{
		this(0);
	}

	public SimpleSemaphore(int initial)
	{
		if (initial >= 0)
			value = initial;
		else
			throw new IllegalArgumentException("initial < 0");
	}

	public final synchronized int getSignal()
	{
		return value;
	}

	public final synchronized void acquire() throws InterruptedException
	{
		acquire(-1);
	}

	public final synchronized void acquire(int timeout)
			throws InterruptedException
	{
		if (value == 0)
		{
			// wait if no signal
			waitSignal(timeout);
		} else if (value > 0)
		{
			value--;
		}
	}

	protected synchronized void waitSignal(int timeout)
			throws InterruptedException
	{
		waitCount++;
		try
		{
			if (timeout == -1)
			{
				wait();
			} else
			{
				wait(timeout);
			}
		} finally
		{
			waitCount--;
		}
	}

	public final synchronized void releaseAll()
	{
		int count = waitCount == 0 ? 1 : waitCount;
		for (int i = count; i >= 0; i--)
		{
			release();
		}
	}

	public final synchronized void release()
	{
		++value;
		notify();
	}

}
